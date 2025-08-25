package networkTransfer.mqttClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import networkTransfer.NetworkClient;
import networkTransfer.NetworkParams;
import networkTransfer.receive.NetworkReceiveParams;
import networkTransfer.send.ClientConnectFailedException;
import networkTransfer.send.NetTransmitException;
import networkTransfer.send.NetworkQueuedObject;
import networkTransfer.send.NetworkSendParams;
import pamguard.Pamguard;

public class PamMqttClient extends NetworkClient  implements MqttCallback{

	private String stationId;
	private String serverURI;
	private String mqttConnectionId;
	
	protected MqttAsyncClient mqttClient;
	private MqttConnectOptions mqttOptions;
	private IMqttToken connectToken;
	private CustomFilePersistence persistence;
	
	public NetworkSendParams networkSendParams;
	public NetworkReceiveParams networkReceiveParams;
	
	private String mqttConfigureError;
	
	private boolean isAlsoNetRx;

	public PamMqttClient(NetworkParams networkParams){
		super(networkParams);
		isAlsoNetRx = PamController.PamController.getInstance().getRunMode()==PamController.PamController.RUN_NETWORKRECEIVER;
		if(networkParams instanceof NetworkSendParams) {
			this.networkSendParams = (NetworkSendParams) networkParams;
			stationId = "pb"+networkSendParams.stationId1;
			if(isAlsoNetRx) {
				this.stationId = this.networkParams.stationId;
			}
			mqttConnectionId = this.stationId+"PAM"+getRememberedStationKey();
			System.out.println("Network send station id "+this.mqttConnectionId);
		}else {
			this.networkReceiveParams = (NetworkReceiveParams) networkParams;
			stationId = networkReceiveParams.stationId;
			mqttConnectionId = this.stationId+getRememberedStationKey();
			System.out.println("Network receive station id "+this.mqttConnectionId);
		}
		requireReconnect = false;
		this.configureClient(networkParams);
	}
	
	private static File rememberKey = Paths.get(Pamguard.getSettingsFolder(),"mqttStation.txt").toFile();
	
	private String getRememberedStationKey() { 
		if(rememberKey.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(rememberKey))) {
	            String line = reader.readLine();
	            if(line!=null) {
	            	return line;
	            }else {
	            	return generateStationKeyFile();
	            }
	        } catch (Exception e) {
	            return generateStationKeyFile();
	        }
		}else {
			return generateStationKeyFile();
		}
	}
	
	private String generateStationKeyFile() {
		String thisNewKey = getRandomLongString();
		if(rememberKey.exists()) {
			rememberKey.delete();
		}
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(rememberKey))) {
            writer.write(thisNewKey);
            System.out.println("Generated unique MQTT station ID: "+thisNewKey);
        } catch (IOException e) {
            System.err.println("Error writing to MQTT key file: " + e.getMessage());
        }
		
		return thisNewKey;
	}
	
	private String getRandomLongString() {
		double rand = Math.random();
		rand = rand*10000000L;
		long longRand = Math.round(rand);
		return "_"+String.valueOf(longRand);
	}
	
	@Override
	public int getQueueLength() {
		if(this.mqttClient==null) {
			return -1;
		}
		try {
			return this.mqttClient.getInFlightMessageCount();
		}catch(NullPointerException e) {
			return -1;
		}
	}

	@Override
	public int getQueueSize() {
		return -1;
	}
	
	@Override
	public void configureClient(NetworkParams networkParams) {
		this.networkParams = networkParams;
		mqttConfigureError = null;
		this.setWarning("Attempting initial configure...",1);
		if(initializing) {
			return;
		}
		try {
			if(this.mqttClient!=null) {
				resetClient();
			}
			
			initializing = true;
			
			generateServerURI();
			
			generateClientPersistence();
	        
			generateMqttClient();
			
			generateMqttOptions();
		}catch(Exception e) {
			this.mqttConfigureError = e.getMessage();
			System.out.println("Encountered error configuring MQTT client. Error message: "+e.getMessage());
		}
		
		if(mqttConfigureError!=null) {
			this.setWarning(mqttConfigureError,2);
			initializing = false;
			requireReconnect = true;
		}else {
			this.removeWarning();
		}
		
	}

	@Override
	public boolean connect() throws ClientConnectFailedException{
		
		if(!initializing) {
			System.out.println("Client cannot connect before it is in initializing state.");
			requireReconnect = true;
			return false;
		}
		
		if(connectToken!=null && !connectToken.isComplete()) {
			System.out.println("Attempting to call connect routine while connection is still working. Will not attempt to connect.");
			return false;
		}
		
		if(mqttConfigureError!=null) {
			configureClient(this.networkParams);
			if(mqttConfigureError!=null) {
				return false;
			}
		}
		
		try {
			connectToken = mqttClient.connect(mqttOptions);
			connectToken.waitForCompletion(10000L);
			this.persistence.open(mqttConnectionId, serverURI);
			initializing = false;
		} catch (MqttSecurityException e1) {
			e1.printStackTrace();
			throw new ClientConnectFailedException(e1);
		} catch (MqttException e1) {
			e1.printStackTrace();
			throw new ClientConnectFailedException(e1);
		} catch(NullPointerException e1) {
			throw new ClientConnectFailedException(e1);
		}

		System.out.println("MQTT Client connected to broker.");
		return true;
	}

	@Override
	public void disconnect() {
		if(mqttClient==null) {
			return;
		}
		if(!this.mqttClient.isConnected()) {
			return;
		}
		try {
			IMqttToken disconnectToken = mqttClient.disconnect();
			disconnectToken.waitForCompletion(1000L);
		} catch (MqttException e) {
			try {
				if(this.mqttClient.isConnected()) {
					System.out.println("Timeout disconnecting client from broker, 1 second. Going to attempt forceful disconnection. Error: "+e.getMessage());
					mqttClient.disconnectForcibly();
				}
			} catch (MqttException e1) {
				System.out.println("Error disconnecting client forcibly. Going to proceed to closing. Error: "+e1.getMessage());
			}
		}
		try {
			mqttClient.close(true);
		} catch (MqttException e) {
			System.out.println("Mqtt client could not close. Pamguard will not function properly. Error: "+e.getMessage());
		}
	}
	
	@Override
	public boolean isConnected() {
		if(mqttClient==null) {
			return false;
		}
		return mqttClient.isConnected();
	}

	@Override
	public void sendNetworkQueuedObject(NetworkQueuedObject qo) throws NetTransmitException{
		
		MqttMessage message;
		if(qo.data!=null) {
			message = new MqttMessage(qo.data);
			message.setQos(1);
		}else {
			message = new MqttMessage(qo.jsonString.getBytes());
			message.setQos(1);
		}
		String type = qo.streamName.replace(" ", "");
		
		this.sendMqttMessage(type, message);

	}

	@Override
	public void additionalClose() {
		if(this.mqttClient==null) {
			return;
		}
		try {
			//persistence.close();
			this.mqttClient.close(true);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Override
	public boolean testClient() throws ClientConnectFailedException {
		if(this.mqttClient==null) {
			throw new ClientConnectFailedException("Could not establish client, client is null");
		}
		if(this.connectToken!=null) {
			try {
				this.setWarning("Waiting for client connection. Timeout after 10 seconds",1);
				this.connectToken.waitForCompletion(10000L);
			} catch (MqttException e) {
				this.setWarning("Client test failed. "+e.getMessage(),2);
				throw new ClientConnectFailedException("Could not connect to server after 10 seconds. Error: "+e.getMessage());
			}
		}
		if(this.mqttClient.isConnected()) {
			this.removeWarning();
			try {
				sendStringMessage("test","Pamguard client test at "+Instant.now().toString());
			} catch (NetTransmitException e) {
				throw new ClientConnectFailedException(e.getMessage());
			}
			return true;
		}
		throw new ClientConnectFailedException("Could not connect to server. Check your server parameters and your network connection.");
	}

	@Override
	public void notifyModelChanged(int changeType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void connectionLost(Throwable cause) {
		setWarning("Connection to MQTT broker was lost. "+cause.getMessage(),1);	
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
		
	}
	
	public void subscribeListener(String topic, IMqttMessageListener listener) throws MqttException {
		mqttClient.subscribe(topic, 2, listener);
	}
	
	private String getBaseTransmitTopic() {
		String trueBase = this.networkParams.baseTopic+"/"+this.stationId+"/";
		if(isAlsoNetRx) {
			return trueBase+"baseData/";
		}else {
			return trueBase+"pamData/";
		}
	}

	public void sendStringMessage(String topicExtension, String string) throws NetTransmitException {
		MqttMessage message = new MqttMessage(string.getBytes());
		
		sendMqttMessage(topicExtension,message);
	}
	
	public void sendMqttMessage(String topicExtension, MqttMessage message) throws NetTransmitException {
				
		boolean persistenceOpened = true;
		
		if(this.mqttClient==null) {
			throw new NetTransmitException("Mqtt client is not initialized",new NullPointerException());
		}
		
		if(this.mqttClient.isConnected()) {
			this.removeWarning();
		}else if(this.connectToken==null) {
			this.setWarning("Mqtt client initializing. Message will not buffer", 1);
			return;
		}else {
			if(this.connectToken.getException()!=null) {
				Exception connectException = this.connectToken.getException();
				requireReconnect = true;
				this.setWarning(connectException.getCause().getMessage()+". Messages will buffer.", 1);
				
			}else {
				this.setWarning("Client is not connected to broker. Messages will buffer until it is.", 1);
			}
		}
		
		String topic = getBaseTransmitTopic()+topicExtension;
		
		/*if(requireReconnect && persistenceOpened) {
			int keyIdx = 0;
				try {
				while(this.persistence.keys().hasMoreElements()) {
					String key = (String) this.persistence.keys().nextElement();
					keyIdx = Integer.valueOf(key.split("-")[1]);
				}
				String key = "s-"+(keyIdx+1);
				MqttPublish persistableMessage =  new MqttPublish(topic, message);
				this.persistence.put(key, persistableMessage);
				mqttClient.com
			} catch (MqttPersistenceException e) {
				System.out.println("Attempted to commit message to persistence directory, but failed. Error: "+e.getMessage());
			}
		}else {*/
			try {
				mqttClient.publish(topic,message.getPayload(),1,false);
			}catch (MqttPersistenceException e) {
				System.out.println("Persistance exception on mqtt publish. "+e.getMessage());
			}catch (MqttException e) {
				throw new NetTransmitException(e);
			}
		//}
	}

	public static void test(NetworkParams networkParams) throws ClientConnectFailedException {
		PamMqttClient testClient = new PamMqttClient(networkParams);
		testClient.connect();
		testClient.testClient();
		testClient.disconnect();
		testClient.close();
		
	}
	
	private String generateServerURI() throws Exception{
		if(this.networkParams.useSSL) {
			serverURI = "ssl://"+this.networkParams.ipAddress+":"+this.networkParams.portNumber;
		}else {
			serverURI = "tcp://"+this.networkParams.ipAddress+":"+this.networkParams.portNumber;
		}
		return this.serverURI;
	}
	
	private void resetClient() throws ClientConnectFailedException{
		if(connectToken!=null && !connectToken.isComplete()) {
			throw new ClientConnectFailedException("Must wait for previous instance to finish connecting before connecting again.");
		}
		try {
			this.mqttClient.close(true);
		} catch (MqttException e) {
			this.setWarning("Couldn't override existing client: "+e.getMessage(),2);
			throw new ClientConnectFailedException("Couldn't override existing client: "+e.getMessage());
		}
	}
	
	private void generateClientPersistence() throws Exception{
		if(this.networkParams.persistenceDirectory!=null) {
			Paths.get(networkParams.persistenceDirectory).toFile().mkdirs();
        	System.out.println("Setting memory persistance directory to "+this.networkParams.persistenceDirectory);
        	persistence = new CustomFilePersistence(this.networkParams.persistenceDirectory);
        }else {
        	//persistence = new MemoryPersistence();
        }
	}
	
	private void generateMqttClient() throws Exception{
		try {
			if(this.persistence==null) {
				mqttClient = new MqttAsyncClient(serverURI,mqttConnectionId);
			}else {
				mqttClient = new MqttAsyncClient(serverURI,mqttConnectionId,persistence);
			}
			mqttClient.setCallback(this);
		} catch (MqttException e) {
			e.printStackTrace();
			mqttConfigureError = e.getMessage();
		}
	}
	
	private void generateMqttOptions() throws Exception{
		mqttOptions = new MqttConnectOptions();
		mqttOptions.setAutomaticReconnect(true);
		mqttOptions.setCleanSession(false);
		mqttOptions.setConnectionTimeout(0);
		mqttOptions.setMaxInflight(65535);
		mqttOptions.setMaxReconnectDelay(30000);
		//mqttOptions.setKeepAliveInterval(0);
		//mqttOptions.
		if(this.networkParams.userId!=null&&this.networkParams.password!=null&&!this.networkParams.userId.isEmpty()&&!this.networkParams.password.isEmpty()) {
			mqttOptions.setUserName(this.networkParams.userId);
			mqttOptions.setPassword(this.networkParams.password.toCharArray());

		}
		
		if(this.networkParams.useSSL) {
			mqttOptions.setSocketFactory(this.getSSLSocketFactory());
		}
	}

}
