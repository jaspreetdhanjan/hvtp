package uk.ac.ucl.hvtp.server;

import uk.ac.ucl.hvtp.HyperverseLogger;
import uk.ac.ucl.hvtp.OnPacketCallback;
import uk.ac.ucl.hvtp.Packet;
import uk.ac.ucl.hvtp.PacketHeader;
import uk.ac.ucl.hvtp.client.HyperverseClient;
import uk.ac.ucl.hvtp.client.IHyperverseClient;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HyperverseServer implements Runnable, IHyperverseServer {
	private static final Logger LOGGER = HyperverseLogger.getLogger(HyperverseServer.class.getName());

	private final byte[] initialPayload;

	private volatile boolean running = false;
	private ServerSocket socket;
	private List<IHyperverseClient> clients = new ArrayList<>();

	private Queue<Packet> packetQueue = new ConcurrentLinkedDeque<>();

	private OnPacketCallback callback = packet -> {
		LOGGER.log(Level.ALL, String.format("Received %s message from client", packet.getHeader().getType()));
		packetQueue.add(packet);
	};

	public HyperverseServer() {
		this.initialPayload = loadSceneGraph();
	}

	private static byte[] loadSceneGraph() {
		InputStream is = HyperverseServer.class.getResourceAsStream("/hyperverse.glb");

		try {
			return is.readAllBytes();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void create(String hostname, int port) {
		if (running) {
			return;
		}

		try {
			socket = new ServerSocket(port);
			running = true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		LOGGER.log(Level.ALL, "Starting up server...");
	}

	@Override
	public void run() {
		while (running) {
			try {
				update();
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.log(Level.ALL, "An error occurred within the server update", e);
				break;
			}
		}
	}

	private void update() throws IOException {
		// We will wait here until a new TCP connection is opened.

		Socket newSocket = socket.accept();

		IHyperverseClient client = new HyperverseClient(newSocket);
		client.create(callback);
		client.sendPacket(newInitPacket());

		clients.add(client);

		LOGGER.log(Level.ALL, String.format("New client (%s) connected to server!", newSocket.getInetAddress().getHostName()));
		LOGGER.log(Level.ALL, String.format("Sending INIT packet to client (%s)", newSocket.getInetAddress().getHostName()));
	}

	private Packet newInitPacket() {
		PacketHeader header = new PacketHeader("HVTP", 1, initialPayload.length, "INIT");
		return new Packet(header, initialPayload);
	}

	@Override
	public Collection<IHyperverseClient> getClients() {
		return clients;
	}

	@Override
	public int getPort() {
		return socket.getLocalPort();
	}

	@Override
	public String getHostname() {
		return socket.getInetAddress().getHostName();
	}

	@Override
	public void close() throws IOException {
		running = false;

		for (Closeable client : clients) {
			client.close();
		}

		LOGGER.log(Level.ALL, "Shutting down server...");
	}

	public static void main(String[] args) {
		HyperverseServer server = new HyperverseServer();
		server.create("localhost", 8088);

		new Thread(server).start();
	}
}