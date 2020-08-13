package uk.ac.ucl.hvtp;

import uk.ac.ucl.hvtp.client.HyperverseClient;

public interface OnPacketCallback {
	/**
	 *
	 * @param packet
	 */
	void onPacketReceived(HyperverseClient client, Packet packet);
}