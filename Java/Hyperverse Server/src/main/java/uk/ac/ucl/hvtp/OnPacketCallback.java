package uk.ac.ucl.hvtp;

public interface OnPacketCallback {
	/**
	 *
	 * @param packet
	 */
	void onPacketReceived(Packet packet);
}