package networkTransfer.receive;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import GPS.GpsData;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import networkTransfer.NetworkObject;
import networkTransfer.NetworkReceiverInterface;
import networkTransfer.mqttClient.PamMqttClient;
import networkTransfer.receive.status.BuoyStatusDataBlock;
import networkTransfer.receive.status.BuoyStatusDataUnit;
import networkTransfer.send.ClientConnectFailedException;

public class MqttNetReceiver extends PamMqttClient implements NetworkReceiverInterface,NetworkDataUser{

	private NetworkReceiveParams netParams;
	private NetworkReceiver netReceiver;
	private NetworkAudioInterpreter networkAudioInterpreter;
	private BuoyStatusDataBlock buoyStatusDataBlock;
	private BaseListener baseListener;
	private StatusListener statusListener;
	
	
	public MqttNetReceiver(NetworkReceiveParams netRxParams, NetworkReceiver netReceiver) {
		super(netRxParams);
		this.netParams = netRxParams;
		this.netReceiver = netReceiver;
		buoyStatusDataBlock = this.netReceiver.getBuoyStatusDataBlock();
	}

	@Override
	public void stopConnectionThread() {

		this.disconnect();
	}

	@Override
	public int getNDataIn() {

		return 0;
	}

	@Override
	public int getNPacketsIn() {

		return 0;
	}

	@Override
	public void runReceiver() {
		if(!this.isConnected()) {
			this.configureClient(this.netParams);
			try {
				this.connect();
			} catch (ClientConnectFailedException e) {
				e.printStackTrace();
			}
		}
		
		
		try {
			baseListener = new BaseListener(this);
			statusListener = new StatusListener();
			System.out.println("Setting up receiver subscribers.");
			this.subscribeListener(netParams.baseTopic+"/+/pamData/#", baseListener);
			this.subscribeListener(netParams.baseTopic+"/+/status", statusListener);
			this.subscribeListener(netParams.baseTopic+"/+/router/#", statusListener);
		} catch (MqttException e) {
			e.printStackTrace();
		}
		
	}
	
	private class StatusListener implements IMqttMessageListener{
		
		private int getId(String topic) {
			for(String item:topic.split("/")) {
				if(item.contains("pb")) {
					try {
						return Integer.valueOf(item.split("b")[1]);
					}catch(NumberFormatException e) {
						return 0;
					}
				}
			}
			return 0;
		}
		
		
		public void handleRouterMessage(String routerVar, MqttMessage message, int pbIdInteger) {
			
			if(!routerVar.equals("signal") && !routerVar.equals("wan")) {
				return;
			}
			
			BuoyStatusDataUnit buoyStatusDataUnit = netReceiver.findBuoyStatusDataUnit(pbIdInteger, pbIdInteger, true);
			buoyStatusDataUnit.setLastCommsPing(System.currentTimeMillis());
			if(routerVar.equals("signal")) {
				try {
					double commStrength = Double.valueOf(message.toString());
					buoyStatusDataUnit.receivedCommsPing(System.currentTimeMillis(), commStrength);
				}catch(NumberFormatException e) {
					System.out.println("Could not interpret communication strength "+message.toString()+" as double.");
				}
				
			}
			if(routerVar.equals("wan")){
				try {
					InetAddress buoyWan = InetAddress.getByName(message.toString());
					buoyStatusDataUnit.setIpAddr(buoyWan);
				}catch(UnknownHostException | SecurityException e) {
					System.out.println("Could not interpret WAN Ip Address "+message.toString());
				}
				
			}
			buoyStatusDataBlock.updatePamData(buoyStatusDataUnit, PamCalendar.getTimeInMillis());
		}

		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			String[] topicLevels = topic.split("/");
			int pbIdInteger = getId(topic);
			boolean hasRouterMessage = false;
			for(String topiclevel:topicLevels) {
				if(!topiclevel.equals("router")) {
					hasRouterMessage = true;
				}
			}
			if(hasRouterMessage) {
				String routerVar = topicLevels[topicLevels.length-1];
				handleRouterMessage(routerVar,message,pbIdInteger);
			}
			
			if(message.toString().contains("latitude") && message.toString().contains("longitude")) {
				handleFullStatusMessage(pbIdInteger,message);
			}
			
		}
		
		private void handleFullStatusMessage(int pbIdInteger, MqttMessage message) {
			BuoyStatusDataUnit buoyStatusDataUnit = netReceiver.findBuoyStatusDataUnit(pbIdInteger, pbIdInteger, true);

			String hasFixString = getJsonValue(message.toString(),"hasFix");
			boolean hasFix = hasFixString.equalsIgnoreCase("true");
			if(hasFix) {
				String latString = getJsonValue(message.toString(),"latitude");
				double lat = Double.valueOf(latString);
				String lonString = getJsonValue(message.toString(),"longitude");
				double lon = -1*Double.valueOf(lonString);
				LatLong pos = new LatLong(lat,lon);
				GpsData buoyPos = new GpsData(pos);
				buoyStatusDataUnit.setGpsData(PamCalendar.getTimeInMillis(), buoyPos);
			}
			
			JSONObject messageJson = new JSONObject(message.toString());
			JSONObject telebuoyStatus = messageJson.getJSONObject("telebuoyStatus");		
			double power = telebuoyStatus.getDouble("houdingPower");
			double temperatureC = telebuoyStatus.getDouble("temperatureC");
			double humidityPercent = telebuoyStatus.getDouble("humidityPercent");
			double supplyVoltage = telebuoyStatus.getDouble("supplyVoltage");
			
			buoyStatusDataUnit.setHousingMeasurements(supplyVoltage,power,temperatureC,humidityPercent);
			
			buoyStatusDataBlock.updatePamData(buoyStatusDataUnit, PamCalendar.getTimeInMillis());

		}
		
		private String getJsonValue(String fullJson, String key) {
			int keyIndex = fullJson.indexOf(key);
			String jsonStartingAtKey = fullJson.substring(keyIndex-1);
			int colonIndex = jsonStartingAtKey.indexOf(':');
			String jsonStartingAtVal = jsonStartingAtKey.substring(colonIndex+1);
			boolean valueIsJsonObject = jsonStartingAtVal.replaceAll(" ", "").startsWith("{");
			if(valueIsJsonObject) {
				int valueEndIdx = jsonStartingAtVal.indexOf("}");
				return jsonStartingAtVal.substring(1,valueEndIdx);
			}
			int endValIdx = jsonStartingAtVal.indexOf(',');
			return jsonStartingAtVal.substring(0, endValIdx);
		}
		
	}
	
	private class BaseListener implements IMqttMessageListener{

		HashMap<String,MqttReceiveThread> buoyReceivers;
		MqttNetReceiver mqttReceiver;
		
		public BaseListener(MqttNetReceiver mqttReceiver) {
			buoyReceivers = new HashMap<String,MqttReceiveThread>();
			this.mqttReceiver = mqttReceiver;
		}
		
		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			String buoyId = topic.split("/")[1];
			if(!buoyReceivers.containsKey(buoyId)) {
				buoyReceivers.put(buoyId, new MqttReceiveThread(buoyId,mqttReceiver));
			}
			
			buoyReceivers.get(buoyId).newMessage(topic,message);
			
			
		}
		
	}

	@Override
	public void socketClosed(NetworkReceiveThread networkReceiveThread) {
		// TODO Auto-generated method stub
		
	}

	//In 
	@Override
	public NetworkObject interpretData(NetworkObject receivedObject) {
		BuoyStatusDataUnit buoyStatusDataUnit = netReceiver.findBuoyStatusDataUnit(receivedObject.getBuoyId1(), receivedObject.getBuoyId2(), true);
		buoyStatusDataUnit.setSocket(receivedObject.getSocket());
	
		NetworkObject retObject = null;
		switch(receivedObject.getDataType1()) {
		case NetworkReceiver.NET_PAM_DATA:
			retObject = netReceiver.interpretPamData(receivedObject, buoyStatusDataUnit);
			break;
		case NetworkReceiver.NET_PAM_COMMAND:
			retObject = netReceiver.interpretPamCommand(receivedObject, buoyStatusDataUnit);
			break;
		case NetworkReceiver.NET_AUDIO_DATA:
			if (networkAudioInterpreter == null) {
				return null;
//				networkAudioInterpreter = new NetworkAudioInterpreter(netReceiver);
			}else {
				networkAudioInterpreter.interpretData(receivedObject, buoyStatusDataUnit);
			}
		}
		for (NetworkDataUser extraUser : netReceiver.getExtraDataUsers()) {
			retObject = extraUser.interpretData(receivedObject);
		}
		buoyStatusDataBlock.updatePamData(buoyStatusDataUnit, PamCalendar.getTimeInMillis());
		return retObject;
	}

	
	@Override
	public void newReceivedDataUnit(BuoyStatusDataUnit buoyStatusDataUnit, PamDataUnit dataUnit) {
		// TODO Auto-generated method stub
		
	}

	public MqttReceiveThread getBuoyReceiveThread(String pbId) {
		return baseListener.buoyReceivers.get(pbId);
	}
	
	public MqttReceiveThread getBuoyReceiveThread(int idInteger) {
		String pbId = "pb"+idInteger;
		return baseListener.buoyReceivers.get(pbId);

	}
	
}
