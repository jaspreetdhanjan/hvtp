package uk.ac.ucl.hvtp;

import uk.ac.ucl.hvtp.server.HyperverseServer;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

/**
 * Represents a GLB scene.
 *
 * @author Jaspreet Dhanjan, University College London (UCL)
 */

public class SceneGraph {
	//	private static final String PATH = "/test/ClientScene.glb";
	private static final String PATH = "/floor.glb";
//	private static final String PATH = "/hyperverse.glb";

	private volatile byte[] bytes;

	/**
	 * We need a path on disk to save a persistent store of the scene-graph.
	 * <p>
	 * For speed, keep a copy of the scene-graph bytes on the heap. Once we are done,
	 * copy this back into memory so we have a copy of it.
	 */

	public SceneGraph() {
		bytes = load();
	}

	public synchronized void setBytes(byte[] bytes) {
		this.bytes = bytes;
//		saveBytes();
	}

	public synchronized void applyUpdate(byte[] xorBytesCompressed) {
		// Step 1: Dezip

		byte[] decompressedBytes = null;

		try (ByteArrayInputStream bis = new ByteArrayInputStream(xorBytesCompressed);
		     ByteArrayOutputStream bos = new ByteArrayOutputStream();
		     GZIPInputStream gzipIS = new GZIPInputStream(bis)) {

			byte[] buffer = new byte[1024];
			int len;
			while ((len = gzipIS.read(buffer)) != -1) {
				bos.write(buffer, 0, len);
			}

			decompressedBytes = bos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Error decompressing bytes from UPDT packet type", e);
		}

		int minSize = Math.min(decompressedBytes.length, bytes.length);
		int maxSize = Math.max(decompressedBytes.length, bytes.length);

		byte[] updatedSceneGraph = new byte[maxSize];

		// The inverse of XOR is... XOR!

		int sourcePointer;
		for (sourcePointer = 0; sourcePointer < minSize; sourcePointer++) {
			updatedSceneGraph[sourcePointer] = (byte) (bytes[sourcePointer] ^ decompressedBytes[sourcePointer]);
		}

		// Any leftover bytes are just a copy of the largest GLB file.

		while (sourcePointer < maxSize) {
			if (decompressedBytes.length > bytes.length) {
				updatedSceneGraph[sourcePointer] = decompressedBytes[sourcePointer];
			} else if (bytes.length > decompressedBytes.length) {
				updatedSceneGraph[sourcePointer] = bytes[sourcePointer];
			}
			sourcePointer++;
		}

		setBytes(updatedSceneGraph);
	}

	private void saveBytes() {
		new Thread(() -> SceneGraph.save(bytes)).start();
	}

	public byte[] getBytes() {
		return bytes;
	}

	private static synchronized byte[] load() {
		try {
			System.out.println("Loading GLB bytes from disk...");

			InputStream is = HyperverseServer.class.getResourceAsStream(PATH);
			byte[] bytes = is.readAllBytes();
			is.close();

			// Check if what we have is an actual GLB file.
			if (!isMagicValid(Arrays.copyOfRange(bytes, 0, 4))) {
				throw new RuntimeException("Not correct GLB format");
			}

			return bytes;
		} catch (IOException e) {
			System.err.print("Fatal: Could not load scene-graph bytes from disk. Cannot startup server. " + e);
			throw new RuntimeException(e);
		}
	}

	private static boolean isMagicValid(byte[] magic) {
		return new String(magic, StandardCharsets.US_ASCII).equals("glTF");
	}

	private static synchronized void save(byte[] bytes) {
		try {
			System.out.println("Saving GLB bytes to disk...");

			FileOutputStream os = new FileOutputStream(PATH, false);
			FileChannel channel = os.getChannel();

			FileLock lock = null;
			try {
				lock = channel.tryLock();
			} catch (OverlappingFileLockException ofle) {
				os.close();
				channel.close();
			}

			os.write(bytes);

			if (lock != null) {
				lock.release();
			}
			channel.close();
			os.close();

		} catch (IOException e) {
			System.err.print("Fatal: Could not save scene-graph bytes from disk. Cannot startup server. " + e);
			throw new RuntimeException(e);
		}
	}

//	private boolean isValid() {
//		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
//		try {
//			String magic = PacketSerializer.fromAscii(byteArrayInputStream.readNBytes(4));
//			return magic.equals("glTF");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		return false;
//	}
}