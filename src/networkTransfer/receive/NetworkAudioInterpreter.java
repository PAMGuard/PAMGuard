package networkTransfer.receive;

import networkTransfer.NetworkObject;

/**
 * interface for any network audio interpreter. The interpreter will have 
 * to set itself in the NetworkReceiver if it thinks it's going to be used. A bit messy
 * since 
 */
public interface NetworkAudioInterpreter {

	public NetworkObject interpretData(NetworkObject receivedObject, BuoyStatusDataUnit buoyStatusDataUnit);

}
