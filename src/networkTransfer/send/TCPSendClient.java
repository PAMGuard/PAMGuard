package networkTransfer.send;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.SwingWorker;

import PamController.PamController;
import networkTransfer.NetworkClient;
import networkTransfer.NetworkParams;
import networkTransfer.receive.NetworkReceiver;

public class TCPSendClient extends NetworkClient{
	
	private Socket tcpSocket;
	
	private DataOutputStream tcpWriter;
	
	List<NetworkQueuedObject> objectList;
	
	int totalQueueSize = 0;
	
	private String currStatus = "Closed";
	
	QueueWorkerThread queueWorkerThread;
	
	public NetworkSendParams networkSendParams;
	
	public TCPSendClient(NetworkParams networkParams) {
		super(networkParams);
		this.networkSendParams = (NetworkSendParams) networkParams;
		objectList = Collections.synchronizedList(new LinkedList<NetworkQueuedObject>());
		
	}
	
	public void additionalClose() {
		if (queueWorkerThread != null) {
			queueWorkerThread.stopThread(true, true);
			queueWorkerThread = null;
		}
	}
	
	
	@Override
	public boolean testClient() throws ClientConnectFailedException {
		int port = 0;
		int timeout = 2000;
		String node = this.networkSendParams.ipAddress;
		try {
			port = this.networkSendParams.portNumber;
		}
		catch (NumberFormatException e) {
			throw new ClientConnectFailedException("Invalid port address: " + this.networkSendParams.portNumber);
		}


		Socket s = null;
		String reason = null ;
		try {
			s = new Socket();
			s.setReuseAddress(true);
			SocketAddress sa = new InetSocketAddress(node, port);
			s.connect(sa, timeout * 1000);
		} catch (IOException e) {
			if ( e.getMessage().equals("Connection refused")) {
				reason = "port " + port + " on " + node + " is closed.";
			};
			if ( e instanceof UnknownHostException ) {
				reason = "node " + node + " is unresolved.";
			}
			if ( e instanceof SocketTimeoutException ) {
				reason = "timeout while attempting to reach node " + node + " on port " + port;
			}
		} finally {
			if (s != null) {
				if ( s.isConnected()) {
					reason = "Port " + port + " on " + node + " is reachable!";
				} else {
					reason = "Port " + port + " on " + node + " is not reachable; reason: " + reason;
				}
				try {
					s.close();
				} catch (IOException e) {
				}
			}
		}
		System.out.println(reason);
		if(reason!=null && !reason.contains("is reachable!")) {
			throw new ClientConnectFailedException(reason);
		}
		
		return true;
		
	}
	
	/**
	 * Called from within the swing worker thread to send data from the queue. 
	 * @return The number of objects sent. 
	 * @throws ClientConnectFailedException 
	 */
	public int sendQueuedData() {
		int objectsSent = 0;
		NetworkQueuedObject anObject;
		while (objectList.size() > 0) {
			anObject = objectList.get(0);
			if (!sendObject(anObject)) {
				return objectsSent;				
			}
			totalQueueSize -= anObject.dataLength;
			objectList.remove(0);
		}
		return objectsSent;
	}

	int retry = 0;
	
	public synchronized boolean openConnection() throws ClientConnectFailedException {
		boolean ok;
		if(this.tcpSocket!=null && this.isConnected()) {
			return true;
		}
		if(networkSendParams.useSSL) {
			ok = openSSLConnection();
			this.setWarning(null);
		}else {
			ok = openNoSSLConnection();
		}
		if(!ok && retry==20) {
			System.out.printf("Cannot connect to base station at host %s:%d\n",networkSendParams.ipAddress, networkSendParams.portNumber);
			retry=0;
		}
		if(ok) {
			retry=0;
		}else {
			retry=retry+1;
		}
		return ok;
	}
	
	/**
	 * Start the queue thread, which takes data from the queue and 
	 * attemts to send it. 
	 */
	private void startQueueThread() {
		queueWorkerThread = new QueueWorkerThread();
		queueWorkerThread.execute();		
	}
	
	
	private boolean sendObject(NetworkQueuedObject anObject) {
		
		// open a writer if necessary. 
		if (tcpWriter == null) {
			try  {
				openConnection();
			}catch (ClientConnectFailedException e){
				return false;
			}
		}

		switch (anObject.format) {
		
		
		// Case 0: byte array data
		case NetworkSendParams.NETWORKSEND_BYTEARRAY:

			// first, re-pack the object ...
			byte[] data = anObject.data;

			/*
			 * Don't we here need to write out a whole load of stuff first to make
			 * the header before the actual data ? ... 'data' includes a header (st response. That is what I say)
			 */
			if (!writeByteData(data)) {
				closeConnection();
				try  {
					openConnection();
				}catch(ClientConnectFailedException e) {
					return false;
				}
				if (!writeByteData(data)) {
					closeConnection();
					return false;
				}
			}
			break;

			
		// Case 1: JSON-formatted text string
		case NetworkSendParams.NETWORKSEND_JSON:
			
			String dataStr = anObject.jsonString;
			if (!writeStringData(dataStr)) {
				closeConnection();
				try  {
					openConnection();
				}catch(ClientConnectFailedException e) {
					return false;
				}
				if (!writeStringData(dataStr)) {
					closeConnection();
					return false;
				}
			}
			break;
			
		}
		
		return true;
	}
	
	private class QueueWorkerThread extends SwingWorker<Integer, QueueStatusData> {

		private volatile boolean stopNow;
		private boolean flushData;
		private boolean flushCommands;

		@Override
		protected Integer doInBackground() {
			while (stopNow == false) {
				if (sendQueuedData() == 0) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			flushQueue(flushData, flushCommands);
			deleteQueue();
			return null;
		}

		public void stopThread(boolean flushData, boolean flushCommands) {
			stopNow = true;
			this.flushData = flushData;
			this.flushCommands = flushCommands;
		}

		@Override
		protected void done() {
			// TODO Auto-generated method stub
			super.done();
		}

		@Override
		protected void process(List<QueueStatusData> chunks) {
			// TODO Auto-generated method stub
			super.process(chunks);
		}
		
	}
	
	boolean openSSLConnection() throws ClientConnectFailedException {
		SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		String [] protocols = new String[]{"TLSv1.2"};
		SSLSocket socket;
		try {
			socket = (SSLSocket)factory.createSocket(networkSendParams.ipAddress, networkSendParams.portNumber);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			throw new ClientConnectFailedException(e1);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new ClientConnectFailedException(e1);

		}
		try {
			socket.setTcpNoDelay(true);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new ClientConnectFailedException(e1);

		}
		socket.setEnabledProtocols(protocols);
		try {
			socket.startHandshake();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new ClientConnectFailedException(e1);

		}
		
		try {
			
			this.tcpSocket = socket;
			tcpWriter = new DataOutputStream(tcpSocket.getOutputStream());
			//tcpWriter.write(new String("Hello from Java netTx").getBytes());
			//tcpWriter.flush();
			currStatus = "Open";
		} catch (IOException e) {
			currStatus = "IO Exception";
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	void closeConnection() {
		if (tcpSocket == null) {
			return;
		}
		try {
			tcpSocket.close();
			tcpWriter.close();
		} catch (IOException e) {
//			e.printStackTrace();
			System.out.println("IOException in NeetworkSender.closeconnection" + e.getMessage());
		}
		tcpSocket = null;
		tcpWriter = null;
	}

	
	/** 
	 * Put data objects in a queue. A different thread will handle
	 * emptying the queue, opening the connection, sending the data, etc. 
	 * @param networkQueuedObject Object to add to the queue
	 */
	public synchronized void queueDataObject(NetworkQueuedObject networkQueuedObject) {
		objectList.add(networkQueuedObject);
		totalQueueSize += networkQueuedObject.dataLength;
		checkQueueSize();
	}
	
	/**
	 * Get the size of the queue as a number of objects. 
	 * @return number of objects in queue.
	 */
	public synchronized int getQueueLength() {
		return objectList.size();
	}
	
	/**
	 * Get the current queue size in kilobytes
	 * @return
	 */
	public synchronized int getSynchQueueSize() {
		return totalQueueSize / 1024;
	}
	
	public int getQueueSize() {
		return getSynchQueueSize();
	}
	
	/**
	 * Check the queue size is no greater than the permitted maximum. If it is, start to throw 
	 * away the oldest data units. 
	 */
	private void checkQueueSize() {
		NetworkQueuedObject removedObject;
		
		while (objectList.size() > networkSendParams.maxQueuedObjects ||
				(networkSendParams.maxQueueSize!=0 && totalQueueSize > networkSendParams.maxQueueSize * 1024)) {
			 removedObject = objectList.remove(0);
			 totalQueueSize -= removedObject.dataLength;
		}
	}
	
	/**
	 * Final flushing of the data queue once DAQ has stopped. 
	 * @param flushData
	 * @param flushCommands
	 * @return number of objects sent. 
	 */
	private int flushQueue(boolean flushData, boolean flushCommands) {
		int objectsSent = 0;
		NetworkQueuedObject anObject;
		while (objectList.size() > 0) {
			anObject = objectList.get(0);
			if (shouldFlush(anObject, flushData, flushCommands)) {
				if (!sendObject(anObject)) {
					return objectsSent;				
				}
			}
			totalQueueSize -= anObject.dataLength;
			objectList.remove(0);
		}	
		return objectsSent;
	}
	
	/**
	 * Delete everything left in the queue.
	 * @return number of items removed. 
	 */
	private int deleteQueue() {
		// now just empty anything left...
		int n = objectList.size();
		objectList.clear();
		totalQueueSize = 0;		
		return n;
	}
	
	boolean shouldFlush(NetworkQueuedObject anObject, boolean flushData, boolean flushCommands) {
		switch(anObject.dataType2) {
		case NetworkReceiver.NET_PAM_DATA:
			return flushData;
		default:
			return flushCommands;
		}
	}

	
	
	
	
	private boolean writeByteData(byte[] data) {
		try {
			tcpWriter.write(data);
//			System.out.println(String.format("Wrote %d bytes to socket", data.length));
			return true;
		} catch (IOException e) {
//			e.printStackTrace();
			System.out.println("IOException in NeetworkSender.writeByteData: " + e.getMessage());
			currStatus = "Socket Closed";
			return false;
		}
	}
	
	private boolean writeStringData(String data) {
		try {
			tcpWriter.writeBytes(data);
//			System.out.println(String.format("Wrote %d characters to socket", data.length()));
			return true;
		} catch (IOException e) {
//			e.printStackTrace();
			System.out.println("IOException in NeetworkSender.writeStringData" + e.getMessage());
			currStatus = "Socket Closed";
			return false;
		}
	}
	
	
	
	boolean openNoSSLConnection() throws ClientConnectFailedException {
		try {
			tcpSocket = new Socket(networkSendParams.ipAddress, networkSendParams.portNumber);
		} catch (UnknownHostException e) {
			currStatus = "Unknown host";
			tcpSocket = null;
			tcpWriter = null;
			throw new ClientConnectFailedException(e);
		} catch (IOException e) {
			currStatus = "IO Exception";
			tcpSocket = null;
			tcpWriter = null;
			throw new ClientConnectFailedException(e);
		}
		try {
			tcpWriter = new DataOutputStream(tcpSocket.getOutputStream());
		} catch (IOException e) {
//			currStatus = "TCP IO Exception";
			currStatus = e.getMessage();
			tcpSocket = null;
			tcpWriter = null;
			throw new ClientConnectFailedException(e);

		}
		currStatus = "Open";
		return true;
	}
	
	public String getStatus() {
		return currStatus;
	}
	
	/**
	 * Execute one of several possible external commands recieved through the PAMGuard terminal 
	 * or UDP interface. 
	 * @param command
	 * @return
	 */
	public String executeExternalCommand(String command) {
		switch (command) {
		case "queuelength":
			return String.format("%d", objectList.size());
		case "queuesize":
			return String.format("%d", totalQueueSize);
		case "getoutputmode":
			return "Unknown";
		case "getmodesend":
			// I think this is supposed to be a snip of XML
			return "Unknown";
		case "getmodesleep":
			// I think this is supposed to be a snip of XML
			return "Unknown";
		}
		return null;
	}

	@Override
	public boolean connect() throws ClientConnectFailedException {
		// TODO Auto-generated method stub
		return openConnection();
	}

	@Override
	public void disconnect() {
		try {
			if(this.tcpSocket!=null) {
				tcpSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean isConnected() {
		return tcpSocket.isConnected();
		
	}

	@Override
	public void sendNetworkQueuedObject(NetworkQueuedObject qo) {
		this.queueDataObject(qo);
		
	}

	@Override
	public void notifyModelChanged(int changeType) {
		if(changeType== PamController.INITIALIZATION_COMPLETE) {
			startQueueThread();
		}
	}

	@Override
	public void configureClient(NetworkParams networkParams) {
		// TODO Auto-generated method stub
		
	}

	

}
