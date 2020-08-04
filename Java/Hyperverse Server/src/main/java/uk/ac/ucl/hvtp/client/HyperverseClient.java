package uk.ac.ucl.hvtp.client;

import uk.ac.ucl.hvtp.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HyperverseClient implements Runnable, IHyperverseClient {
	private static final Logger LOGGER = HyperverseLogger.getLogger(HyperverseClient.class.getName());
	private static final AtomicLong COUNTER = new AtomicLong();

	private final Socket socket;

	private volatile boolean running = true;

	private Thread thread;
	private OnPacketCallback callback;

	public HyperverseClient(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void create() {
		if (running) {
			return;
		}

		running = true;

		thread = new Thread(this);
		thread.setName("Hyperverse Client #" + COUNTER.incrementAndGet());
		thread.start();
	}

	@Override
	public void setCallback(OnPacketCallback callback) {
		this.callback = callback;
	}

	@Override
	public void run() {
		while (running) {
			try {
				Packet packet = receivePacket();

				if (callback != null) {
					callback.onPacketReceived(packet);
				} else {
					LOGGER.log(Level.ALL, "No callback set! Message thrown away...");
				}
			} catch (IOException e) {
				LOGGER.log(Level.ALL, "Error receiving message from client", e);
				e.printStackTrace();
			}
		}
	}

	private Packet receivePacket() throws IOException {
		InputStream inputStream = socket.getInputStream();

		byte[] bytes = inputStream.readNBytes(PacketHeader.SIZE_IN_BYTES);

		PacketHeader header = PacketSerializer.deserializeHeader(bytes);

		byte[] payload = inputStream.readNBytes(header.getLength());

		return new Packet(header, payload);
	}

	public synchronized void sendPacket(Packet packet) throws IOException {
		byte[] bytes = PacketSerializer.serialize(packet);

//		out.write(bytes);
		OutputStream outputStream = socket.getOutputStream();
		outputStream.write(bytes);
	}

	@Override
	public void close() throws IOException {
		if (!running) {
			return;
		}

		running = false;

		try {
			thread.join(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		socket.close();
	}
}