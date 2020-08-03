package uk.ac.ucl.hvtp;

import java.io.Serializable;

public class PacketHeader implements Serializable {
	public static final int SIZE_IN_BYTES = 16;

	private final String magic;
	private final int version;
	private final int length;
	private final String type;

	public PacketHeader(String magic, int version, int length, String type) {
		this.magic = magic;
		this.version = version;
		this.length = length;
		this.type = type;
	}

	public String getMagic() {
		return magic;
	}

	public int getVersion() {
		return version;
	}

	public int getLength() {
		return length;
	}

	public String getType() {
		return type;
	}
}