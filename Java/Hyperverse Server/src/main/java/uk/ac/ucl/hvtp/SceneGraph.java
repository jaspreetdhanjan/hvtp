package uk.ac.ucl.hvtp;

import uk.ac.ucl.hvtp.server.HyperverseServer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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