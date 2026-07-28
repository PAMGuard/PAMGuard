package loggerForms.network;

public interface LoggerNetworkObserver {

	/**
	 * Update network state. 
	 * @param connected
	 * @param nClient
	 */
	public void updateState(boolean connected, int nClient);
	
}
