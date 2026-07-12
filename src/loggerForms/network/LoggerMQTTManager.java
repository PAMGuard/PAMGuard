package loggerForms.network;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.Timer;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import networkTransfer.NetworkParams;
import networkTransfer.receive.NetworkReceiveParams;
import networkTransfer.send.ClientConnectFailedException;
import networkTransfer.send.NetTransmitException;
import pamguard.Pamguard;

public class LoggerMQTTManager extends LoggerNetworkManager {

	private LoggerMqttClient mqttClient;
	
	private NetworkReceiveParams networkParams;

	private MQTTSidePanel mqttSidePanel;
	
	private Timer reconnectTimer;
	
	public LoggerMQTTManager() {
		
		networkParams = new NetworkReceiveParams();
		networkParams.baseTopic = "Logger";
		networkParams.ipAddress = "localhost";
		networkParams.portNumber = 1883;
		networkParams.persistenceDirectory = Pamguard.getSettingsFolder();
		
		mqttClient = new LoggerMqttClient(this, networkParams);
		
		reconnectTimer = new Timer(20000, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				reconnect();
			}
		});
		reconnectTimer.start();
	}

	
	protected void reconnect() {
		if (mqttClient.isConnected()) {
			reconnectTimer.stop();
			return;
		}
		checkMQTTClient();
		if (mqttClient.isConnected()) {
			reconnectTimer.stop();
			return;
		}
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
		boolean isCon =  mqttClient.isConnected();
		if (isCon) {
			subsribeTopic("$SYS/broker/clients/connected", new LoggerNetworkReceiver() {
				
				@Override
				public boolean newMessage(LoggerNetworkMessage message) {
					String strClient = null;
					try {
						strClient = new String(message.getData());
						int nClient = Integer.valueOf(strClient);
						updateState(mqttClient.isConnected(), nClient);
//						System.out.printf("%s: %d\n",message.getTopic(), (int) message.getData()[0]);
					}
					catch (Exception e) {
						System.out.println(e.getMessage());
					}
					return true;
				}
			});
		}
		return isCon;
	}

	@Override
	public boolean sendData(String station, String topic, byte[] payload) {
		if (!checkMQTTClient()) {
			return false;
		}
		MqttMessage message = new MqttMessage(payload);
		try {
			mqttClient.sendMqttMessage(topic, message);
		} catch (NetTransmitException e) {
			System.out.println("Error sending MQTT Message: " + e.getMessage());
			return false;
		}
		return true;
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
	
	public String getStatus() {
		LoggerMqttClient client = mqttClient;
		if (client == null) {
			return "No MQTT Client";
		}
		return client.getStatus();
	}

	@Override
	public JComponent getSideComponent() {
		if (mqttSidePanel == null) {
			mqttSidePanel = new MQTTSidePanel(this);
		}
		return mqttSidePanel.getPanel();
	}

	public int getNConnections() {
		LoggerMqttClient client = mqttClient;
		if (client == null) {
			return 0;
		}
//		client.
		return 0;
	}
	/**
	 * notify all observers
	 */
	@Override
	public final void updateState(boolean connected, int nClient) {
		super.updateState(connected, nClient);
		if (connected == false && reconnectTimer.isRunning() == false) {
			reconnectTimer.start();
		}
	}
}
