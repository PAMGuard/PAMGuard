package networkTransfer.receive;

import java.net.Socket;

import PamguardMVC.PamDataUnit;
import networkTransfer.NetworkObject;

/**
 * Interface for classes using the NetworkReceiveThread. 
 * @author dg50
 *
 */
public interface NetworkDataUser {
	
	public void socketClosed(NetworkReceiveThread networkReceiveThread);

	public NetworkObject interpretData(NetworkObject receivedObject);

	public void newReceivedDataUnit(BuoyStatusDataUnit buoyStatusDataUnit, PamDataUnit dataUnit);
	
}
