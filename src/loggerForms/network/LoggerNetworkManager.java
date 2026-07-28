package loggerForms.network;

import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

/**
 * A new network manager to receive data from external devices, primarily a mobile phone
 * app that will provide button presses and camera images. This will use either UDP multicast
 * or possible MQTT. This has been developed separately from the main NetworkReciever since that
 * other has become hard to manage, and is also being modified by others, so start clean here, and 
 * once it's working, might try to merge functionality with the main receiver. 
 * <p>
 * Significantly, this is simpler, doesn't have a GUI (or not much of one), etc. 
 */
abstract public class LoggerNetworkManager implements LoggerNetworkObserver {

	private static LoggerNetworkManager singleInstance;
	
	private ArrayList<LoggerNetworkObserver> netObservers = new ArrayList();

	/**
	 * Send data
	 * @param station sending station id (e.g. base)
	 * @param topic topic
	 * @param payload data, can be null
	 * @return true if it seems to have sent. 
	 */
	abstract public boolean sendData(String station, String topic, byte[] payload); 
	
	/**
	 * Subscribe a topic. Notifications will be sent to messageuser when data with this topic arrive. 
	 * @param topic
	 * @param messageUser
	 */
	abstract public void subsribeTopic(String topic, LoggerNetworkReceiver messageUser);
	
	/**
	 * Unsubscribe. topic OR messageUser can be null in which case all users of that topic or
	 * all topics for that user will be unsubscribed
	 * @param topic
	 * @param messageUser
	 * @return
	 */
	abstract public boolean unsubscribeTopic(String topic, LoggerNetworkReceiver messageUser);
	
	/**
	 * Setup necessary receive threads. Also used to restart after any config changes. 
	 * @return
	 */
	abstract public boolean setupListener();
	
	/**
	 * Close down any receiving threads. 
	 * @return
	 */
	abstract public boolean closeListener();
	
	abstract public JMenuItem getConfigMenu();
	
	/**
	 * get a component to include in the logger side panel.
	 * @return
	 */
	public JComponent getSideComponent() {
		return null;
	}
	
	/**
	 * Add a network state observer
	 * @param netObserver
	 */
	public void addNetworkObserver(LoggerNetworkObserver netObserver) {
		if (netObservers.contains(netObserver) == false) {
			netObservers.add(netObserver);
		}
	}

	/**
	 * Remove a network state observer
	 * @param netObserver
	 */
	public boolean removeNetworkObserver(LoggerNetworkObserver netObserver) {
		return netObservers.remove(netObserver);
	}

	/**
	 * notify all observers
	 */
	@Override
	public void updateState(boolean connected, int nClient) {
		for (LoggerNetworkObserver obs : netObservers) {
			obs.updateState(connected, nClient);
		}
	}
	
	
	
}
