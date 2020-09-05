package uk.ac.ucl.hvtp.server;

import uk.ac.ucl.hvtp.*;
import uk.ac.ucl.hvtp.math.Vector4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

		switch (type) {
			case HVTPConstants.INIT:
				sceneGraph.setBytes(payload);
				break;
			case HVTPConstants.UPDT:
				//sceneGraph.applyUpdate(payload);
				break;
			case HVTPConstants.TRNS:
				// So we can't really modify our scene-graph here, yet. We'll need to import some glTF loader and
				// a vecmath library in order for us to change our local scene-graph.

				DataInputStream stream = new DataInputStream(new ByteArrayInputStream(payload));

				try {
					Vector4 rotation = Vector4.readBytes(stream);
					Vector4 scale = Vector4.readBytes(stream);
					Vector4 position = Vector4.readBytes(stream);
					String nodeName = new String(Arrays.copyOfRange(payload, 48, payload.length), StandardCharsets.US_ASCII);

					System.out.println("Rotation: " + rotation);
					System.out.println("Scale: " + scale);
					System.out.println("Position " + position);
					System.out.println("Name " + nodeName);
				} catch (Exception e) {
					e.printStackTrace();
				}

				//sceneGraph.applyTranslation(payload);
				break;
			default:
				LOGGER.log(Level.ALL, "Unsupported message type has been received... What do we do?");
				break;
		}
	}

	public static void main(String[] args) {
//		PacketQueueProcessor pqp = new PacketQueueProcessor(null);
//		pqp.start();

//		for (int i = 0; i < 10000; i++)
//			pqp.add(new Packet(new PacketHeader("OMNIVERSE", i, 0, "INIT"), new byte[0]));

		//pqp.stop();
	}
}