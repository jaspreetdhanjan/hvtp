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

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Packet");
		sb.append("\nMagic:\t");
		sb.append(magic);
		sb.append("\nVersion:\t");
		sb.append(version);
		sb.append("\nLength:\t");
		sb.append(length);
		sb.append("\nType:\t");
		sb.append(type);
		return sb.toString();
	}
}