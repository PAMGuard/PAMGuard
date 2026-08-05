package networkTransfer.receive;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import networkTransfer.NetworkObject;
import networkTransfer.NetworkReceiverInterface;
import networkTransfer.receive.status.BuoyStatusDataBlock;
import networkTransfer.receive.status.BuoyStatusDataUnit;

/**
 * The connection thread does not receive data, it just listens 
 * for socket requests when clients attempt to open a connection. 
 * When it get's a connection, it then opens a new ReceiveThread
 * which will receive the actual data. 
 * @author Doug Gillespie
 *
 */
class StandardTCPReceiver extends Thread implements NetworkDataUser,NetworkReceiverInterface {

	private List<NetworkReceiveThread> receiveThreads;
	
	protected NetworkReceiveParams currentParams;

	private volatile boolean keepRunning = true;

	ServerSocket serverSocket;
	
	private NetworkReceiver netReceiver;
	
	private BuoyStatusDataBlock buoyStatusDataBlock;
	
	private int recentPackets, recentDataBytes;


	/**
	 * @param currentParams
	 */
	public StandardTCPReceiver(NetworkReceiveParams currentParams, NetworkReceiver netReceiver) {
		super();
		this.currentParams = currentParams.clone();
		receiveThreads = Collections.synchronizedList(new Vector<NetworkReceiveThread>());
		this.netReceiver = netReceiver;
		this.buoyStatusDataBlock = netReceiver.getBuoyStatusDataBlock();

	}

	/**
	 * Stop the connection thread by closing the
	 * server socket and telling it to break out of the loop. 
	 */
	public void stopConnectionThread() {
		/*
		 * And all the client threads too !
		 */
		System.out.println("Stop all client threads !!!!");
		synchronized(receiveThreads) {
			if (receiveThreads != null) {
				ListIterator<NetworkReceiveThread> rxIt = receiveThreads.listIterator();
				NetworkReceiveThread rxThread;
				while (rxIt.hasNext()) {
					rxThread = rxIt.next();
					rxIt.remove();
					rxThread.stopThread();
				}		
			}
		}
		keepRunning = false;
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void newReceivedDataUnit(BuoyStatusDataUnit buoyStatusDataUnit, PamDataUnit dataUnit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() {
		runConnectionThread();

	}

	/**
	 * Connection thread - sits in loop waiting for connections. 
	 * There is one of these but many NetworkReceiveThread's (one for each client)
	 */
	public void runConnectionThread() {
		int bufferSize = 1024;
		byte[] dgBuffer = new byte[21];

		Socket clientSocket;
		NetworkReceiveThread rxThread;
		try {
			serverSocket = new ServerSocket(currentParams.receivePort);
			System.out.println("Waiting for client contact on port " + currentParams.receivePort);
			while (keepRunning) {
				/*
				 * This next function blocks until either something connects, or 
				 * the serverSocket is closed. 
				 */
				clientSocket = serverSocket.accept();

//				System.out.printf("Opening client socket for %s on port %d at %s\n",
//						clientSocket.getInetAddress().getHostAddress(),
//						clientSocket.getPort(), PamCalendar.formatTime(System.currentTimeMillis(), true));
				

//				System.out.printf("Opening client socket for %s on port %d at %s\n",
//						clientSocket.getInetAddress().getHostAddress(),
//						clientSocket.getPort(), PamCalendar.formatTime(System.currentTimeMillis(), true));

				rxThread = new NetworkReceiveThread(clientSocket, this);
//				System.out.printf("Thread created at %s\n", PamCalendar.formatTime(System.currentTimeMillis(), true));
				Thread t = new Thread(rxThread);
				t.setPriority(Thread.MAX_PRIORITY);
//				System.out.printf("Call start thread at %s\n", PamCalendar.formatTime(System.currentTimeMillis(), true));
				t.start();
//				System.out.printf("Called start thread at %s\n", PamCalendar.formatTime(System.currentTimeMillis(), true));
//				synchronized(receiveThreads) {
//					receiveThreads.add(rxThread);
//				}

//				checkExistingThreads(clientSocket, rxThread);
			}
		} catch (IOException e) {
			System.out.println("IOException in NeetworkReceiver" + e.getMessage());
		}

	}
	
	/**
	 * Before opening a new thread for this client, check to see 
	 * if that client already has a receive thread open and then
	 * if it has close it 
	 * @param clientSocket
	 * @param wantedThread The thread we just made, so want to keep. 
	 */
	public synchronized void checkExistingThreads(Socket clientSocket, NetworkReceiveThread wantedThread) {
		String newHost = clientSocket.getInetAddress().getHostAddress();
		ArrayList<NetworkReceiveThread> stopList = new ArrayList<NetworkReceiveThread>();
		synchronized(receiveThreads) {
			System.out.printf("Checking %d existing recieve threads\n", receiveThreads.size());
			ListIterator<NetworkReceiveThread> threadIt = receiveThreads.listIterator();
			while (threadIt.hasNext()) {
				NetworkReceiveThread rxThread = threadIt.next();
				if (rxThread == wantedThread) {
					continue;
				}
				Socket rxClient = rxThread.getClientSocket();
				String rxHost = rxClient.getInetAddress().getHostAddress();
				if (newHost.equals(rxHost)) {
					System.out.println("*********  Server thread already open for " + rxHost + " force thread stop. ***********");
					stopList.add(rxThread);
					rxThread.stopThread();
//					threadIt.remove();
					
				}
			}
			if (stopList.size() > 0) {
				for (NetworkReceiveThread rxThread:stopList) {
					rxThread.stopThread();
				}
			}
		}
	}
	
	public void socketClosed(NetworkReceiveThread networkReceiveThread) {
		/*
		 * Once the thread has ended, tell the stats objects that
		 * they are no longer connected to anything. 
		 */
		ListIterator<BuoyStatusDataUnit> it = buoyStatusDataBlock.getListIterator(0);
		BuoyStatusDataUnit b = null;
		while (it.hasNext()) {
			b = it.next();
			if (b.getSocket() == networkReceiveThread.getClientSocket()) {
				b.setSocket(null);
			}
		}
		if (b == null) {
			System.out.println(String.format("Rx socket closed for unknown sender on %s", toString()));
		}
		else {
//			System.out.println(String.format("Rx socket closed for sender %d(%d) on %s", b.getBuoyId1(), b.getBuoyId2(), toString()));
		}
		synchronized(receiveThreads) {
			receiveThreads.remove(networkReceiveThread);
		}
	}
	
	private synchronized void addPacketStats(int packetSize) {
		recentPackets++;
		recentDataBytes += packetSize;
	}

	/**
	 * @return the number of data packets received since the last call to this function
	 */
	public synchronized int getRecentPackets() {
		int n = recentPackets;
		recentPackets = 0;
		return n;
	}
	
	private NetworkAudioInterpreter networkAudioInterpreter;
	@Override
	public synchronized NetworkObject interpretData(NetworkObject receivedObject) {
		
		/**
		 * Although there are many NetworkReceiveThread's there is only one
		 * of thee, so at this point data from all senders end up in the same
		 * thread. 
		 */

		addPacketStats(receivedObject.getTransferedLength());
		
		BuoyStatusDataUnit buoyStatusDataUnit = netReceiver.findBuoyStatusDataUnit(receivedObject.getBuoyId1(), receivedObject.getBuoyId2(), true);
		buoyStatusDataUnit.setSocket(receivedObject.getSocket());
		// work out how long different socket asking questions take. 
//		long t1 = System.currentTimeMillis();
//		InetAddress inAddr = socket.getInetAddress();
//		long t2 = System.currentTimeMillis();
//		byte[] addr = inAddr.getAddress();
//		String addrS = new String(addr);
//		long t3 = System.currentTimeMillis();
//		String hostName = inAddr.getHostName(); // takes 4.5s
//		long t4 = System.currentTimeMillis();
//		String canonicalName = inAddr.getCanonicalHostName(); // takes 4.5s
//		long t5 = System.currentTimeMillis();
//		String hostAddr = inAddr.getHostAddress(); // takes 0ms. 
//		long t6 = System.currentTimeMillis();
//		System.out.printf("Host time: inAddr %d, getAddress %d, hostName %s %d, CanonicalHostName %s %d, hostAddress %s %dms\n",
//				t2-t1, t3-t2, hostName, t4-t3, canonicalName, t5-t4, hostAddr, t6-t5);
		/*
		 * Use getHostAddress() since it's faster. 
		 */
		
		
		//		if (dataId1 != NET_PAM_DATA) {
//					String str = new String(duBuffer);
//					str = str.substring(0, dataLen);
//					System.out.println(String.format("Buoy data arrived id %d / %d, %s", dataId1, dataId2, str));
		//		}

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
			}
			networkAudioInterpreter.interpretData(receivedObject, buoyStatusDataUnit);
		}
		for (NetworkDataUser extraUser : netReceiver.getExtraDataUsers()) {
			retObject = extraUser.interpretData(receivedObject);
		}
		buoyStatusDataBlock.updatePamData(buoyStatusDataUnit, PamCalendar.getTimeInMillis());
		return retObject;
	}

	@Override
	public int getNDataIn() {
		int n = recentDataBytes;
		recentDataBytes = 0;
		return n;
	}

	@Override
	public int getNPacketsIn() {
		return getRecentPackets();
	}

	@Override
	public void runReceiver() {
		this.start();
		
	}
	
	

}
