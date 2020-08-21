package uk.ac.ucl.hvtp;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class PacketSerializer {
	private PacketSerializer() {
	}

	public static byte[] toAscii(String string) {
		return string.getBytes(StandardCharsets.US_ASCII);
	}

	public static String fromAscii(byte[] bytes) {
		return new String(bytes, StandardCharsets.US_ASCII);
	}

	public static byte[] serialize(Packet packet) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(byteArrayOutputStream);

		PacketHeader header = packet.getHeader();

		dumpHeaders(System.out, header);

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

	public static Packet deserialize(InputStream stream) throws IOException {
		DataInputStream dis = new DataInputStream(stream);

		String magic = fromAscii(dis.readNBytes(4));
		int version = dis.readInt();
		int length = dis.readInt();
		String type = fromAscii(dis.readNBytes(4));

		PacketHeader header = new PacketHeader(magic, version, length, type);

		dumpHeaders(System.out, header);

		byte[] payload = dis.readNBytes(length);

		return new Packet(header, payload);
	}

	public static PacketHeader deserializeHeader(InputStream stream) throws IOException {
		DataInputStream dis = new DataInputStream(stream);

		String magic = fromAscii(dis.readNBytes(4));
		int version = dis.readInt();
		int length = dis.readInt();
		String type = fromAscii(dis.readNBytes(4));

		return new PacketHeader(magic, version, length, type);
	}

//	public static PacketHeader deserializeHeader(byte[] bytes) throws IOException {
//		return deserializeHeader(new ByteArrayInputStream(bytes));
//	}

	public static void dumpHeaders(PrintStream o, PacketHeader... headers) {
		for (PacketHeader header : headers) {
			o.println("Header is: " + header.getMagic());
			o.println("Version is: " + header.getVersion());
			o.println("Length is: " + header.getLength());
			o.println("Type is: " + header.getType());
		}
	}
}