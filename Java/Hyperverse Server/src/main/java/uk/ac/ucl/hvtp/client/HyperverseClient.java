package uk.ac.ucl.hvtp.client;

import uk.ac.ucl.hvtp.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HyperverseClient implements Runnable, IHyperverseClient {
	private static final Logger LOGGER = HyperverseLogger.getLogger(HyperverseClient.class.getName());

	private static final Logger RESULTS = HyperverseLogger.getBenchmarkLogger("INIT_transform");

	private volatile boolean stop = false;

	private Socket socket;
	private OnPacketCallback callback;

	public HyperverseClient(Socket socket, OnPacketCallback callback) {
		this.socket = socket;
		this.callback = callback;
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				Packet packet = receivePacket();

				System.out.println("RECEIEVED");

				if (callback != null) {
					callback.onPacketReceived(this, packet);
				} else {
					LOGGER.log(Level.ALL, "No callback set! Message thrown away...");
				}
			} catch (IOException e) {
				LOGGER.log(Level.ALL, "Error receiving message from client", e);
				LOGGER.log(Level.ALL, "Closing the connection");

				try {
					close();
				} catch (IOException ex) {
				}
			}
		}
	}

	private Packet receivePacket() throws IOException {
		InputStream in = socket.getInputStream();

		LOGGER.log(Level.ALL, String.format("Received message from %s", socket.toString()));

		PacketHeader header = PacketSerializer.deserializeHeader(in);

		RESULTS.log(Level.ALL, String.valueOf(header.getLength()));

		byte[] payload = in.readNBytes(header.getLength());
		return new Packet(header, payload);
	}

	public synchronized void sendPacket(Packet packet) throws IOException {
		byte[] bytes = PacketSerializer.serialize(packet);

		OutputStream outputStream = socket.getOutputStream();
		outputStream.write(bytes);
	}

	@Override
	public boolean isStopped() {
		return stop;
	}

	@Override
	public void close() throws IOException {
		if (stop) {
			return;
		}
		stop = true;
		socket.close();
		socket = null;
	}
}