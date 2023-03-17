package tethys;

public interface TethysStateObserver {
	
	/**
	 * Receive state updates when Tethys has done something (made a connection, moved some data, etc.)<br>
	 * Note that this is for RECEIVING state updates, not for sending them. To avoid infinite notifications 
	 * loops, use tethysControl.sendStateUpdate(TethysState) if this component knows something. 
	 * @param tethysState
	 */
	public void updateState(TethysState tethysState);
	
}
