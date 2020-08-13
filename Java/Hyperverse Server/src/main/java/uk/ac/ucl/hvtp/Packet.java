package uk.ac.ucl.hvtp;

import java.io.Serializable;
import java.util.Objects;

public class Packet implements Serializable {
	private final PacketHeader header;
	private final byte[] payload;

	public Packet(PacketHeader header, byte[] payload) {
		Objects.requireNonNull(header);
		Objects.requireNonNull(payload);

		this.header = header;
		this.payload = payload;
	}

	public PacketHeader getHeader() {
		return header;
	}

	public byte[] getPayload() {
		return payload;
	}

	public String toString() {
		return header.toString();
	}
}