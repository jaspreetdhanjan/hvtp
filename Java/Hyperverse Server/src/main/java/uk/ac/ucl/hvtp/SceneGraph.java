package uk.ac.ucl.hvtp;

import uk.ac.ucl.hvtp.server.HyperverseServer;

import java.io.*;

/**
 * Represents a GLB scene.
 *
 * @author Jaspreet Dhanjan, University College London (UCL)
 */

public class SceneGraph {
	private static final String PATH = "/hyperverse.glb";

	private byte[] bytes;

	/**
	 * We need a path on disk to save a persistent store of the scene-graph.
	 * <p>
	 * For speed, keep a copy of the scene-graph bytes on the heap. Once we are done,
	 * copy this back into memory so we have a copy of it.
	 */

	public SceneGraph() {
		load();

		if (!isValid())
			throw new RuntimeException("Not correct GLB format");

	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
		//saveBytes();
	}

	private void saveBytes() {
		new Thread(this::save).start();
	}

	public byte[] getBytes() {
		return bytes;
	}

	private void load() {
		try {
			synchronized (this) {
				InputStream is = HyperverseServer.class.getResourceAsStream(PATH);
				bytes = is.readAllBytes();
				is.close();
			}
		} catch (IOException e) {
			System.err.print("Fatal: Could not load scene-graph bytes from disk. Cannot startup server. " + e);
			throw new RuntimeException(e);
		}
	}

	private void save() {
		try {
			synchronized (this) {
				OutputStream os = new FileOutputStream(PATH, false);
				os.write(bytes);
				os.close();
			}
		} catch (IOException e) {
			System.err.print("Fatal: Could not load scene-graph bytes from disk. Cannot startup server. " + e);
			throw new RuntimeException(e);
		}
	}

	private boolean isValid() {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		try {
			String magic = PacketSerializer.fromAscii(byteArrayInputStream.readNBytes(4));
			return magic.equals("glTF");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}
}