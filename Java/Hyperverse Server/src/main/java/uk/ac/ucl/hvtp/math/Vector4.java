package uk.ac.ucl.hvtp.math;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Vector4 {
	private float x, y, z, w;

	public Vector4(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	public float getW() {
		return w;
	}

	public static Vector4 readBytes(DataInputStream stream) throws IOException {
		float x = stream.readFloat();
		float y = stream.readFloat();
		float z = stream.readFloat();
		float w = stream.readFloat();

		return new Vector4(x, y, z, w);
	}

	public String toString() {
		return "(" + x + ", " + y + ", " + z + ", " + w + ")";
	}
}
