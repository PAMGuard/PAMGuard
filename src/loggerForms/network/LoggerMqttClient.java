package loggerForms.network;

import networkTransfer.NetworkParams;
import networkTransfer.mqttClient.PamMqttClient;

public class LoggerMqttClient extends PamMqttClient {

	public LoggerMqttClient(NetworkParams networkParams) {
		super(networkParams);
		configureClient(networkParams);
		initializing = true;
	}

	@Override
	public String getBaseTransmitTopic() {
		return super.getBaseTransmitTopic();
	}

}
