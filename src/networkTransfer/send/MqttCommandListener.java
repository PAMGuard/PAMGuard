package networkTransfer.send;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import PamController.PamController;
import PamController.command.CommandManager;
import PamController.command.ExtCommand;
import networkTransfer.mqttClient.PamMqttClient;

public class MqttCommandListener extends CommandManager implements IMqttMessageListener {
		
	private PamMqttClient pamMqttClient;
	
	private MessageHold currentHeldMessage;
	
	private static MqttCommandListener singleInstance;

	public MqttCommandListener(PamMqttClient pamMqttClient) {
		super(PamController.getInstance(), "MqttCommandListener");
		this.pamMqttClient = pamMqttClient;
	}
	
	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		currentHeldMessage = new MessageHold(topic,message);
		this.interpretCommand(new String(message.getPayload()));
		
	}

	@Override
	public boolean sendData(ExtCommand extCommand, String dataString) {
		MqttMessage message = new MqttMessage(dataString.getBytes());
		currentHeldMessage.message.setPayload(dataString.getBytes());
		try {
			this.pamMqttClient.sendStringMessage(currentHeldMessage.topic+"/response", message.toString());
		} catch (NetTransmitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//
		currentHeldMessage.message.notifyAll();
		return true;
		
	}
	
	public void holdMessage(String topic, MqttMessage message) {
		try {
			this.pamMqttClient.sendStringMessage(topic,message.toString());
		} catch (NetTransmitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private class MessageHold{
		public MessageHold(String topic2, MqttMessage message2) {
			this.message = message2;
			this.topic = topic2;
			responded = false;
		}
		
		public MqttMessage message;
		public String topic;
		public boolean responded;
		
	}

	public static IMqttMessageListener getInstance(PamMqttClient pamMqttClient) {
		if(singleInstance!=null) {
			return singleInstance;
		}
		singleInstance = new MqttCommandListener(pamMqttClient);
		return singleInstance;
	}
	
}
