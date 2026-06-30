package loggerForms.network;

import javax.swing.JMenuItem;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import networkTransfer.NetworkParams;
import networkTransfer.receive.NetworkReceiveParams;
import networkTransfer.send.ClientConnectFailedException;
import pamguard.Pamguard;

public class LoggerMQTTManager extends LoggerNetworkManager {

	private LoggerMqttClient mqttClient;
	
	private NetworkReceiveParams networkParams;
	
	public LoggerMQTTManager() {
		
		networkParams = new NetworkReceiveParams();
		networkParams.baseTopic = "Logger";
		networkParams.ipAddress = "localhost";
		networkParams.portNumber = 1883;
		networkParams.persistenceDirectory = Pamguard.getSettingsFolder();
		
		mqttClient = new LoggerMqttClient(networkParams);
	}
	
	private boolean checkMQTTClient() {
		if (mqttClient.isConnected()) {
			return true;
		}
		try {
			mqttClient.configureClient(networkParams);
			mqttClient.connect();
		} catch (ClientConnectFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return mqttClient.isConnected();
	}

	@Override
	public boolean sendData(String station, String topic, byte[] payload) {
		checkMQTTClient();
		return false;
	}

	@Override
	public void subsribeTopic(String topic, LoggerNetworkReceiver messageUser) {
		checkMQTTClient();
		try {
			mqttClient.subscribeListener(topic, new TopicListener(topic, messageUser));
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	private class TopicListener implements IMqttMessageListener {
		
		private String topic;
		private LoggerNetworkReceiver messageUser;

		/**
		 * @param topic
		 */
		public TopicListener(String topic, LoggerNetworkReceiver messageUser) {
			super();
			this.topic = topic;
			this.messageUser = messageUser;
		}

		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			byte[] data = message.getPayload();
			messageUser.newMessage(new LoggerNetworkMessage(topic, data, null));			
		}
		
	}

	@Override
	public boolean unsubscribeTopic(String topic, LoggerNetworkReceiver messageUser) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setupListener() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean closeListener() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public JMenuItem getConfigMenu() {
		// TODO Auto-generated method stub
		return null;
	}

}
