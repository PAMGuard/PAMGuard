package networkTransfer.receive;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import networkTransfer.NetworkObject;
import networkTransfer.NetworkReceiverInterface;

public class MqttReceiveThread {
	
	private String buoyId;
	private NetworkDataUser dataUser;
	private Timer heartBeatTimer;
	private boolean isAlive;
	private Processor messageProcessor;
	private Thread messageProcessingThread;

	
	private BlockingQueue<MqttMessage> messageQueue;
	
	
	public class Processor implements Runnable{

		@Override
		public void run() {
			while(true) {
				try {
					MqttMessage newMessage = messageQueue.take();
					InputStream inStream = new ByteArrayInputStream(newMessage.getPayload());
					DataInputStream dis = new DataInputStream(inStream);
					int headInt=0;
					try {
						headInt = dis.readInt();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
					if(headInt!=NetworkReceiveThread.HEADID) {
						System.out.println("Received network data without head flag");
						return;
					}
					
					NetworkObject receivedObject;
					
					try {
						receivedObject = NetworkReceiverInterface.readNetworkObject(dis, null);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
					
					
					dataUser.interpretData(receivedObject);
				}catch(Exception e) {
					System.err.println("Error processing new data: "+e.getMessage());
					e.printStackTrace();
				}
				
			}
			
		}
		
	}
	
	
	public MqttReceiveThread(String buoyId, NetworkDataUser dataUser) {
		this.buoyId = buoyId;
		this.dataUser = dataUser;
		startHeartbeatTimer();
		messageQueue = new ArrayBlockingQueue<MqttMessage>(1000);
		messageProcessor = new Processor();
		messageProcessingThread = new Thread(messageProcessor);
		messageProcessingThread.start();
	}

	public void newMessage(String topic, MqttMessage message) {
		
		heartBeatTimer.cancel();
		isAlive = true;
		startHeartbeatTimer();
		
		boolean isPamData = topic.contains("pamData");
		if(isPamData) {
			
			try {
				this.messageQueue.put(message);
			} catch (InterruptedException e) {
				System.err.println("Error queuing new data to process: "+e.getMessage());
			}
		}
		
	}
	
	private void startHeartbeatTimer() {
		heartBeatTimer = new Timer();
		heartBeatTimer.schedule(new HeartbeatTimerTask(), 60*1000);
	}
	
	private class HeartbeatTimerTask extends TimerTask{

		@Override
		public void run() {
			isAlive = false;
			
		}
		
	}

	public boolean isAlive() {
		return isAlive;
	}

}
