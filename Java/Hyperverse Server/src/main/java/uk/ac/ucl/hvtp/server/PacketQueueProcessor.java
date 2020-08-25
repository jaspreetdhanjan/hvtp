package uk.ac.ucl.hvtp.server;

import uk.ac.ucl.hvtp.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PacketQueueProcessor implements Runnable {
	private Logger LOGGER = HyperverseLogger.getLogger(PacketQueueProcessor.class.getName());

	private final IHyperverseServer server;

	private volatile boolean running = false;
	private Thread thread;
	private final BlockingQueue<Message> queue = new LinkedBlockingQueue<>();

	public PacketQueueProcessor(IHyperverseServer server) {
		this.server = server;
	}

	public void add(Message message) {
		queue.add(message);
		LOGGER.log(Level.ALL, "Added packet to the queue");
	}

	public void start() {
		if (running) {
			return;
		}
		running = true;
		thread = new Thread(this, "PQP Thread");
		thread.start();

		LOGGER.log(Level.ALL, "Packet Queue Processor (PQP) has begun!");
	}

	public void stop() {
		if (!running) {
			return;
		}
		running = false;
		try {
			thread.join(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		LOGGER.log(Level.ALL, "Packet Queue Processor (PQP) has ended!");
	}

	@Override
	public void run() {
		while (running) {
			try {
				Message message = queue.take();
				process(message);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private void process(Message message) {
		Packet packet = message.getPacket();
		PacketHeader header = packet.getHeader();

		LOGGER.log(Level.ALL, "Processing packet... " + header.getType());

		PacketSerializer.dumpHeaders(System.out, header);

		applyChange(header.getType(), packet.getPayload());

//		server.retransmitToAll(message);

		// For now just retransmit the entire scene-graph. Probably need to mention this in the report.

		Packet newPacket = Packet.newInitPacket(server.getSceneGraph().getBytes());
		server.retransmitToAll(new Message(message.getAuthor(), newPacket));
	}

	private void applyChange(String type, byte[] payload) {
		SceneGraph sceneGraph = server.getSceneGraph();

		switch (type) {
			case HVTPConstants.INIT:
				sceneGraph.setBytes(payload);
				break;
			case HVTPConstants.UPDT:
				sceneGraph.applyUpdate(payload);
				break;
			case HVTPConstants.TRNS:
				//sceneGraph.applyTranslation(payload);
				break;
			default:
				LOGGER.log(Level.ALL, "Unsupported message type has been received... What do we do?");
				break;
		}
	}
}