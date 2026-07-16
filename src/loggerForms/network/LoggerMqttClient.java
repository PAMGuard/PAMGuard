package loggerForms.network;

import networkTransfer.NetworkParams;
import networkTransfer.mqttClient.PamMqttClient;
import networkTransfer.send.ClientConnectFailedException;

public class LoggerMqttClient extends PamMqttClient {

	private LoggerMQTTManager loggerMQTTManager;

	public LoggerMqttClient(LoggerMQTTManager loggerMQTTManager, NetworkParams networkParams) {
		super(networkParams);
		this.loggerMQTTManager = loggerMQTTManager;
		configureClient(networkParams);
		initializing = true;
	}

	@Override
	public String getBaseTransmitTopic() {
		String trueBase = this.networkParams.baseTopic+"/";
		return trueBase;
	}

	@Override
	public boolean connect() throws ClientConnectFailedException {
		return super.connect();
	}

	@Override
	public void connectionLost(Throwable cause) {
		super.connectionLost(cause);
		loggerMQTTManager.updateState(false, 0);
	}

}
