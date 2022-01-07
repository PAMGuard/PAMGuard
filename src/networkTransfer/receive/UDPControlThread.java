package networkTransfer.receive;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import PamController.pamBuoyGlobals;

/**
 * Thread for receiving and responding to UDP commands and requests over
 * the network <p>
 * Currently only supporting NEtwork discovery. 
 * @author Doug Gillespie
 *
 */
public class UDPControlThread {

	private static final int MAX_COMMAND_LENGTH = 256;
	private NetworkReceiver networkReceiver;
	private volatile boolean keepRunning;
	private volatile boolean isRunning;
	private int currentPort;
	private Thread udpThread;
	private DatagramSocket receiveSocket;
	
	public UDPControlThread(NetworkReceiver networkReceiver) {
		this.networkReceiver = networkReceiver;
	}

	private class UDPThread implements Runnable {
		@Override
		public void run() {
			isRunning = true;
			runThreadLoop();
			isRunning = false;
		}
	}

	/**
	 * Start the udp thread. If its running, then leave it running. 
	 * @param udpPort
	 */
	public boolean startThread(int udpPort) {
		UDPThread threadObject;
		if (udpThread != null) {
			if (currentPort == udpPort && isRunning && udpThread.isAlive()) {
				return true;
			}
			else {
				stopThread();
			}
		}
		/*
		 *  open the port before starting the thread so we can return false
		 *  if the port won't open. 
		 */
		if (openUDPPort(udpPort) == false) {
			return false;
		}
		udpThread = new Thread(new UDPThread());
		udpThread.start();
		return true;
	}

	private void stopThread() {
		if (udpThread == null || udpThread.isAlive() == false) {
			udpThread = null;
			return;
		}
		// close the port and set flag to keep running false
		// and it should crap out. 
		keepRunning = false;
		closeUDPPort();
		/*
		 * Wait for up to two seconds to check 
		 * it's closed
		 */
		for (int i = 0; i < 20; i++) {
			if (udpThread.isAlive() == false) {
				break;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		if (udpThread.isAlive()) { // try to kill it anyway
			udpThread.interrupt();
		}
		udpThread = null;
	}

	/**
	 * This is where it sits for ever - or until 
	 * it's told to bomb out, e.g. to 
	 * restart with a different port address. 
	 * @param udpPort 
	 */
	public void runThreadLoop() {
		byte[] byteBuffer = new byte[MAX_COMMAND_LENGTH];
		DatagramPacket udpPacket = new DatagramPacket(byteBuffer , MAX_COMMAND_LENGTH);
		while (keepRunning) {
			try {
				receiveSocket.receive(udpPacket);
			} catch (IOException e) {
				e.printStackTrace();
				closeUDPPort();
				break;
			}
		}
	}

	/**
	 * Open the udp port
	 * @return true if successful. 
	 */
	private boolean openUDPPort(int udpPort) {
		try {
			receiveSocket = new DatagramSocket(udpPort);
			receiveSocket.setSoTimeout(0);
		} catch (SocketException e) {
			System.err.println("Error opening udp port " + udpPort + " - " + e.getMessage());
			return false;
		}
		currentPort = udpPort;
		return true;
	}

	/**
	 * Close the UDP port. 
	 */
	private void closeUDPPort() {
		if (receiveSocket != null) {
			receiveSocket.close();
		}
		currentPort = 0;
	}
}
