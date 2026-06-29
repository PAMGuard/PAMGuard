package networkTransfer.send;

import java.net.UnknownHostException;

import org.eclipse.paho.client.mqttv3.MqttException;

public class ClientConnectFailedException extends Exception{
	
	public ClientConnectFailedException(Exception e) {
		super(e);
	}
	
	public ClientConnectFailedException(String msg) {
		super(msg);
	}

	public ClientConnectFailedException(String readableReason, MqttException e) {
		super(readableReason,e);
	}

}
