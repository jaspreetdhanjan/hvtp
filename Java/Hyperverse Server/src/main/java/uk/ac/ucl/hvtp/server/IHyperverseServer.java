package uk.ac.ucl.hvtp.server;

import uk.ac.ucl.hvtp.client.IHyperverseClient;

import java.io.Closeable;
import java.util.Collection;

public interface IHyperverseServer extends Closeable {
	void create(String hostname, int port);

	Collection<IHyperverseClient> getClients();

	int getPort();

	String getHostname();
}