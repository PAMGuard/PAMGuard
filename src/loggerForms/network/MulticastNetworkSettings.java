package loggerForms.network;

import java.io.Serializable;

/**
 * Settings for logger multicast data exchange. 
 */
public class MulticastNetworkSettings implements Serializable, Cloneable {


	public static final long serialVersionUID = 1L;
	
	private String multicastAddress = "230.0.0.0";
	
	private int multicastPort = 4446;

	/**
	 * @return the multicastAddress
	 */
	public String getMulticastAddress() {
		return multicastAddress;
	}

	/**
	 * @param multicastAddress the multicastAddress to set
	 */
	public void setMulticastAddress(String multicastAddress) {
		this.multicastAddress = multicastAddress;
	}

	/**
	 * @return the multicastPort
	 */
	public int getMulticastPort() {
		return multicastPort;
	}

	/**
	 * @param multicastPort the multicastPort to set
	 */
	public void setMulticastPort(int multicastPort) {
		this.multicastPort = multicastPort;
	}

	public MulticastNetworkSettings() {
		// TODO Auto-generated constructor stub
	}

}
