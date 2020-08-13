package uk.ac.ucl.hvtp.server;

import uk.ac.ucl.hvtp.Packet;
import uk.ac.ucl.hvtp.client.HyperverseClient;

public class Message {
	private final HyperverseClient author;
	private final Packet packet;

	public Message(HyperverseClient author, Packet packet) {
		this.author = author;
		this.packet = packet;
	}

	public HyperverseClient getAuthor() {
		return author;
	}

	public Packet getPacket() {
		return packet;
	}
}
