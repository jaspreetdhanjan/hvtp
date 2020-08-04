package uk.ac.ucl.hvtp;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class PacketSerializer {
	private PacketSerializer() {
	}

	private static byte[] toAscii(String string) {
		return string.getBytes(StandardCharsets.US_ASCII);
	}

	private static String fromAscii(byte[] bytes) {
		return new String(bytes, StandardCharsets.US_ASCII);
	}

	public static byte[] serialize(Packet packet) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(byteArrayOutputStream);

		PacketHeader header = packet.getHeader();

		dos.write(toAscii(header.getMagic()));
		dos.writeInt(header.getVersion());
		dos.writeInt(header.getLength());
		dos.write(toAscii(header.getType()));

		dos.write(packet.getPayload());

		return byteArrayOutputStream.toByteArray();
	}

//	public static Packet deserialize(byte[] bytes) throws IOException {
//		PacketHeader header = deserializeHeader(bytes);
//
//		int payloadSize = bytes.length - PacketHeader.SIZE_IN_BYTES;
//		byte[] payload = new byte[payloadSize];
//
//		System.arraycopy(bytes, PacketHeader.SIZE_IN_BYTES, payload, 0, payloadSize);
//
//		return new Packet(header, payload);
//	}

//	public static byte[] serializeHeader(PacketHeader header) throws IOException {
//		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//		DataOutputStream dos = new DataOutputStream(byteArrayOutputStream);
//
//		dos.write(toAscii(header.getMagic()));
//		dos.writeInt(header.getVersion());
//		dos.writeInt(header.getLength());
//		dos.write(toAscii(header.getType()));
//
//		return byteArrayOutputStream.toByteArray();
//	}

	public static PacketHeader deserializeHeader(byte[] bytes) throws IOException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(byteArrayInputStream);

		String magic = fromAscii(dis.readNBytes(4));
		int version = dis.readInt();
		int length = dis.readInt();
		String type = fromAscii(dis.readNBytes(4));

		return new PacketHeader(magic, version, length, type);
	}

	public static void dumpHeaders(PrintStream o, PacketHeader... headers) {
		for (PacketHeader header : headers) {
			o.println(header.getMagic());
			o.println(header.getVersion());
			o.println(header.getLength());
			o.println(header.getType());
		}
	}

	public static void main(String[] args) throws Exception {
		PacketHeader header = new PacketHeader("HVTP", 1, 0, "INIT");
		byte[] bytes = serialize(new Packet(header, new byte[0]));

		dumpHeaders(System.out, deserializeHeader(bytes));

	}
}