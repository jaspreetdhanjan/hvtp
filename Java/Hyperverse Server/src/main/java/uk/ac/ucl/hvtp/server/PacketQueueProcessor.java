package uk.ac.ucl.hvtp.server;

import uk.ac.ucl.hvtp.*;

import java.io.FileOutputStream;
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

		applyChange(header.getType(), packet.getPayload());

		server.retransmitToAll(message);
	}

	private void applyChange(String type, byte[] payload) {
		SceneGraph sceneGraph = server.getSceneGraph();

		if(payload.length < 20000) {
			dump(payload, true);
		} else {
			dump(payload, false);
		}

		switch (type) {
			case HVTPConstants.INIT:
				sceneGraph.setBytes(payload);
				break;
			case HVTPConstants.UPDT:
				//sceneGraph.applyUpdate(payload);
				break;
			case HVTPConstants.TRNS:
				//sceneGraph.applyTranslation(payload);
				break;
			default:
				LOGGER.log(Level.ALL, "Unsupported message type has been received... What do we do?");
				break;
		}
	}

	private void dump(byte[] payload, boolean isMin) {
		try {
			String name = isMin ? "min-"+System.currentTimeMillis() : "normal-"+System.currentTimeMillis();
			FileOutputStream fos = new FileOutputStream(name+".glb");
			fos.write(payload);
			fos.close();
		} catch(Exception e) {
			e.printStackTrace();
		}

		System.out.println("Dumped");
	}

	public static void main(String[] args) {
//		PacketQueueProcessor pqp = new PacketQueueProcessor(null);
//		pqp.start();

//		for (int i = 0; i < 10000; i++)
//			pqp.add(new Packet(new PacketHeader("OMNIVERSE", i, 0, "INIT"), new byte[0]));

		//pqp.stop();
	}
}