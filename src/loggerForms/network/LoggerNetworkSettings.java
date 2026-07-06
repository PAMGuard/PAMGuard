package loggerForms.network;

import java.io.Serializable;

public class LoggerNetworkSettings implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;
	
	public int loggerNetworkType = LOG_NET_NONE;
	
	public static final int LOG_NET_NONE = 0;
	public static final int LOG_NET_MQTT = 1;
	public static final int LOG_NET_UDP = 2;
	

	public LoggerNetworkSettings() {
	}

}
