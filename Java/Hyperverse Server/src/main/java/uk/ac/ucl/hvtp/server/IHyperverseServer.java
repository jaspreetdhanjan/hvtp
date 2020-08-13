package uk.ac.ucl.hvtp.server;

import uk.ac.ucl.hvtp.Packet;
import uk.ac.ucl.hvtp.SceneGraph;
import uk.ac.ucl.hvtp.client.HyperverseClient;
import uk.ac.ucl.hvtp.client.IHyperverseClient;

import java.io.Closeable;
import java.util.Collection;

public interface IHyperverseServer extends Closeable {
	void create(String hostname, int port);

	Collection<IHyperverseClient> getClients();

	Packet newInitPacket();

	void retransmitToAll(Message message);

	SceneGraph getSceneGraph();

	int getPort();

	String getHostname();

	void addClient(IHyperverseClient hyperverseClient);
}