package uk.ac.ucl.hvtp.websocket;

import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import uk.ac.ucl.hvtp.Packet;
import uk.ac.ucl.hvtp.PacketHeader;
import uk.ac.ucl.hvtp.PacketSerializer;
import uk.ac.ucl.hvtp.server.HyperverseServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@WebSocket
public class HyperverseWebSocket {
	// Store sessions if you want to, for example, broadcast a message to all users
	private  static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();

	public static void broadcastAll(Packet packet) throws IOException{
		for (Session session : sessions) {
			broadcast(session, packet);
		}
	}

	public static void broadcast(Session session, Packet packet) throws IOException {
		byte[] serialisedBytes = PacketSerializer.serialize(packet);

		RemoteEndpoint remote = session.getRemote();
		remote.sendBytes(ByteBuffer.wrap(serialisedBytes));
	}

	@OnWebSocketConnect
	public void connected(Session session) {
		sessions.add(session);

//		try {
//			session.getRemote().sendString("HEY THANKS FOR JOINING!");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		byte[] payload = HyperverseServer.sceneGraph.getBytes();

		PacketHeader header = new PacketHeader("HVTP", 1, payload.length, "INIT");
		Packet packet = new Packet(header, payload);

		try {
			broadcast(session, packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@OnWebSocketClose
	public void closed(Session session, int statusCode, String reason) {
		sessions.remove(session);
	}

	@OnWebSocketMessage
	public void message(Session session, String message) throws IOException {
//		System.out.println("Got: " + message);   // Print message
//		session.getRemote().sendString(message); // and send it back
	}

}