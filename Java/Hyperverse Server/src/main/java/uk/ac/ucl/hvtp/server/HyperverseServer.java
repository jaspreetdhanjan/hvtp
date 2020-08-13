package uk.ac.ucl.hvtp.server;

import uk.ac.ucl.hvtp.*;
import uk.ac.ucl.hvtp.client.HyperverseClient;
import uk.ac.ucl.hvtp.client.IHyperverseClient;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HyperverseServer implements Runnable, IHyperverseServer {
	private static final Logger LOGGER = HyperverseLogger.getLogger(HyperverseServer.class.getName());

	private final SceneGraph sceneGraph;

	private volatile boolean running = false;
	private ServerSocket socket;
	private List<IHyperverseClient> clients = new ArrayList<>();
	private volatile PacketQueueProcessor pqp = new PacketQueueProcessor(this);

	private OnPacketCallback callback = (client, packet) -> {
		LOGGER.log(Level.ALL, String.format("Received %s message from client", packet.getHeader().getType()));
		pqp.add(new Message(client, packet));
	};

	public HyperverseServer(SceneGraph sceneGraph) {
		this.sceneGraph = sceneGraph;
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
			LOGGER.log(Level.ALL, "Fatal: Could not start server at port " + port, e);
			throw new RuntimeException(e);
		}

		pqp.start();

		LOGGER.log(Level.ALL, "Starting up server...");
	}

	@Override
	public void run() {
		while (running) {
			try {
				// We will block here until a new TCP connection is opened.

				Socket newSocket = socket.accept();

				HyperverseClient client = new HyperverseClient(newSocket, callback);

				new Thread(client).start();

				// Keep track of this client.

				clients.add(client);

				// Pass an INIT message to the client containing the entire scene-graph.

				client.sendPacket(newInitPacket());

				LOGGER.log(Level.ALL, String.format("New client (%s) connected to server!", newSocket.getInetAddress()));
				LOGGER.log(Level.ALL, String.format("Sending INIT packet to client (%s)", newSocket.getInetAddress()));
			} catch (IOException e) {
				LOGGER.log(Level.ALL, "An error occurred within the server update", e);
			}
		}
	}

	public Packet newInitPacket() {
		byte[] payload = sceneGraph.getBytes();

		PacketHeader header = new PacketHeader(HVTPConstants.MAGIC,
				HVTPConstants.VERSION_1,
				payload.length,
				HVTPConstants.INIT);

		return new Packet(header, payload);
	}

	@Override
	public Collection<IHyperverseClient> getClients() {
		return clients;
	}

	@Override
	public void retransmitToAll(Message message) {
		IHyperverseClient except = message.getAuthor();

		Iterator<IHyperverseClient> iterator = clients.iterator();

		while (iterator.hasNext()) {
			IHyperverseClient client = iterator.next();

			if (client.isStopped()) {
				iterator.remove();
				continue;
			}

			if (client == except) {
				continue;
			}

			try {
				client.sendPacket(message.getPacket());
			} catch (IOException e) {
				LOGGER.log(Level.ALL, "ERROR retransmitting message to other clients");
				e.printStackTrace();
			}
		}
	}

	@Override
	public SceneGraph getSceneGraph() {
		return sceneGraph;
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
	public void addClient(IHyperverseClient client) {
		clients.add(client);
	}

	@Override
	public void close() throws IOException {
		running = false;

		pqp.stop();

		for (Closeable client : clients) {
			client.close();
		}

		LOGGER.log(Level.ALL, "Shutting down server...");
	}

	public static void main(String[] args) {
		HyperverseServer server = new HyperverseServer(new SceneGraph());
		server.create("localhost", 8088);

		new Thread(server, "Hyperverse Server Thread").start();
	}
}