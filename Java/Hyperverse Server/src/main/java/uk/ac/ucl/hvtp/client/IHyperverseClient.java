package uk.ac.ucl.hvtp.client;

import uk.ac.ucl.hvtp.OnPacketCallback;
import uk.ac.ucl.hvtp.Packet;

import java.io.Closeable;
import java.io.IOException;

/**
 *
 */

public interface IHyperverseClient extends Closeable {
	/**
	 *
	 */
	void create();

	/**
	 *
	 * @param callback
	 */
	void setCallback(OnPacketCallback callback);

	/**
	 *
	 * @param packet
	 * @throws IOException
	 */
	void sendPacket(Packet packet) throws IOException;
}
