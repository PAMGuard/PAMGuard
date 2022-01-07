package networkTransfer.emulator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;

import javax.swing.Timer;

import networkTransfer.receive.NetworkReceiver;
import networkTransfer.send.NetworkObjectPacker;
import networkTransfer.send.NetworkQueuedObject;
import networkTransfer.send.NetworkSendParams;

import GPS.GpsData;
import PamUtils.LatLong;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * Handles generation of a single buoy emulation.
 * @author Doug Gillespie
 *
 */
public class EmulatorStream {

	private NetworkEmulator networkEmulator;
	private EmBuoyStatus status;
	private ArrayList<PamDataBlock> usedDataBlocks;
	private long startTime, endTime;
	private long currentTime;
	private long currentRealTime;
	private Timer emTimer;
	private EmulatedIterator[] emulatedIterators;
	private Socket emSocket;
	private NetworkObjectPacker netObjectPacker;
	private long lastStatusTime = 0;
	private long statusIntervalMillis;
	/**
	 * @param networkEmulator
	 * @param buoyId
	 * @param emLatLong 
	 */
	public EmulatorStream(NetworkEmulator networkEmulator, int buoyId, LatLong emLatLong) {
		super();
		this.networkEmulator = networkEmulator;
		netObjectPacker = new NetworkObjectPacker();
		status = new EmBuoyStatus(buoyId, emLatLong.clone());
		emTimer = new Timer(1000, new EmTimer());
		emTimer.stop();
	}
	/**
	 * @return the networkEmulator
	 */
	public NetworkEmulator getNetworkEmulator() {
		return networkEmulator;
	}
	/**
	 * @return the buoyId
	 */
	public int getBuoyId() {
		return status.getBuoyId();
	}
	/**
	 * @return the status
	 */
	public EmBuoyStatus getStatus() {
		return status;
	}

	public boolean start() {

		usedDataBlocks = networkEmulator.getUsedDataBlocks();
		if (usedDataBlocks.size() == 0) {
			return false;
		}
		startTime = usedDataBlocks.get(0).getCurrentViewDataStart();
		endTime = usedDataBlocks.get(0).getCurrentViewDataEnd();
		for (PamDataBlock aBlock:usedDataBlocks) {
			startTime = Math.max(startTime, aBlock.getCurrentViewDataStart());
			endTime = Math.max(endTime, aBlock.getCurrentViewDataEnd());
		}
		if (endTime < startTime) {
			return false;
		}

		currentRealTime = System.currentTimeMillis();
		currentTime = startTime + (long) (Math.random() * (endTime-startTime));
		//		currentTime = startTime + 52000;

		emulatedIterators = new EmulatedIterator[usedDataBlocks.size()];
		for (int i = 0; i < usedDataBlocks.size(); i++) {
			emulatedIterators[i] = new EmulatedIterator(usedDataBlocks.get(i), startTime);
		}
		statusIntervalMillis = networkEmulator.emulatorParams.statusIntervalSeconds * 1000;
		lastStatusTime = currentRealTime - (long) (Math.random() * statusIntervalMillis);

		emTimer.start();

		boolean openOk = openSocket();

		if (openOk) {
			sendPamCommand(NetworkReceiver.NET_PAM_COMMAND_PREPARE);
			sendPamCommand(NetworkReceiver.NET_PAM_COMMAND_START);
		}

		return openOk;
	}

	public void stop() {

		sendPamCommand(NetworkReceiver.NET_PAM_COMMAND_STOP);

		emTimer.stop();
		closeSocket();

	}

	private void sendPamCommand(int command) {
		byte[] data = netObjectPacker.packData(status.getBuoyId(), 0, 
				(short) NetworkReceiver.NET_PAM_COMMAND, command, null);
		if (data == null) {
			return;
		}
		try {
			emSocket.getOutputStream().write(data);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	private boolean openSocket() {
		NetworkSendParams sktParams = networkEmulator.getNetworkSender().getNetworkSendParams();
		try {
			emSocket = new Socket(sktParams.ipAddress, sktParams.portNumber);
		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		if (emSocket != null) {
			status.socketStatus = emSocket.isConnected();
			status.localPort = emSocket.getLocalPort();
			status.remotePort = emSocket.getPort();
		}
		else {
			status.socketStatus = false;
		}
		return status.socketStatus;
	}

	private void closeSocket() {
		if (emSocket == null) {
			return;
		}
		try {
			emSocket.shutdownOutput();
			emSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		emSocket = null;
		status.socketStatus = false;
	}


	public int sendDataUnit(PamDataBlock dataBlock, PamDataUnit dataUnit, long timeOffset) {
		if (emSocket == null) {
			return 0;
		}
		if (status.socketStatus == false) {
			return -1;
		}
		int bytesWrote = 0;
		/**
		 * need to temporarily reset the time in the data units so that it's more like 
		 * now. Will therefore synchronise around the data unit while we temporarily 
		 * change the time so that we don't get two simulators doing the same
		 * thing at the same time. 
		 */
		byte[] data;
		long unitTime;
		synchronized (dataUnit) {
			unitTime = dataUnit.getTimeMilliseconds();
			dataUnit.setTimeMilliseconds(unitTime + timeOffset);
			data = netObjectPacker.packDataUnit(status.getBuoyId(), 0, dataBlock, dataUnit);
			dataUnit.setTimeMilliseconds(unitTime);
		}
		try {
			emSocket.getOutputStream().write(data);
			bytesWrote = data.length;
		} catch (IOException e) {
//			e.printStackTrace();
		}
		status.totalBytes += bytesWrote;
		return bytesWrote;

	}
	/**
	 * timer callback, which will set a time to send
	 * data over. 
	 */
	private void emTimerEvent() {
		long now = System.currentTimeMillis();
		long elapsed = now - currentRealTime;
		long newDataTime = currentTime + elapsed;
		long timeOffset = now - newDataTime;
		int sentUnits = 0;

		if (now - lastStatusTime > statusIntervalMillis) {
			sendStatusData(now);
			lastStatusTime = now;
		}

		for (int i = 0; i < emulatedIterators.length; i++) {
			sentUnits += emulatedIterators[i].sendData(currentTime, newDataTime, timeOffset);
		}
		status.unitsSent += sentUnits;
		status.currentDataTime = currentTime;
		currentTime = newDataTime;
		if (currentTime > endTime) {
			currentTime = startTime;
		}
		currentRealTime = now;

		if (emSocket != null && emSocket.isConnected()) {
			status.socketStatus = true;
		}
		else {
			status.socketStatus = false;
		}
	}

	private void sendStatusData(long now) {
		sendGPSPosition(now);
		sendCompassData(now);
	}

	public static final int  NET_SYSTEM_DATA    = 4;
	public static final String COMPASSID = "HMC";
	public static final int  SYSTEM_GPSDATA     = 1;
//	public static final int  SYSTEM_GPSDATA_ALT = 0; // some confusion if it's 0 or 1!
//	public static final int  SYSTEM_BATTERYDATA = 4;
	public static final int  SYSTEM_COMPASSDATA = 16; 
	double emHead = 0;
	Random random = new Random();
	private void sendCompassData(long now) {
		if (emSocket == null) {
			return;
		}
		emHead = emHead + 1.1;
		while (emHead >= 360) {
			emHead -= 360;
		}
		double pitch = random.nextGaussian()*3;
		double roll = random.nextGaussian()*6;
		String compassString = String.format("%s,3,%3.1f,%3.1f,%3.1f", COMPASSID, emHead, pitch, roll);
		byte[] compassBytes = compassString.getBytes();
		byte[] data = netObjectPacker.packData(getBuoyId(), 0, (short) NET_SYSTEM_DATA, 
				SYSTEM_COMPASSDATA, compassBytes);
		try {
			emSocket.getOutputStream().write(data);
		} catch (IOException e) {
//			e.printStackTrace();
		}
		
	}
	/**
	 * Send the GPS position from the buoy
	 * @param now current time in milliseconds. 
	 */
	private void sendGPSPosition(long now) {
		if (emSocket == null) {
			return;
		}
		LatLong ll = status.getLatLong();
		if (ll == null) {
			ll = new LatLong();
		}
		ll = ll.travelDistanceMeters(Math.random()*360, 10.); // add some jitter !
		GpsData gpsData = new GpsData(ll.getLatitude(), ll.getLongitude(), 0, now);
		String rmcString = gpsData.gpsDataToRMC(3);
		byte[] rmcBytes = rmcString.getBytes();
		byte[] data = netObjectPacker.packData(getBuoyId(), 0, (short) NET_SYSTEM_DATA, 
				SYSTEM_GPSDATA, rmcBytes);
		try {
			emSocket.getOutputStream().write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class EmulatedIterator {
		private ListIterator<PamDataUnit> listIterator;
		private PamDataBlock dataBlock;
		private PamDataUnit currentDataUnit;
		/**
		 * @param dataBlock
		 */
		public EmulatedIterator(PamDataBlock dataBlock, long startTime) {
			super();
			this.dataBlock = dataBlock;
			listIterator = dataBlock.getListIterator(0);

		}
		public int sendData(long dataStart, long dataEnd, long timeOffset) {
			int sentUnits = 0;
			//			if (currentDataUnit != null && currentDataUnit.getTimeMilliseconds() < dataEnd) {
			//				sendDataUnit(currentDataUnit, timeOffset);
			//				sentUnits++;
			//			}
			listIterator = dataBlock.getListIterator(0);
			while (listIterator.hasNext()) {
				currentDataUnit = listIterator.next();
				if (currentDataUnit.getTimeMilliseconds() >= dataEnd) {
					break;
				}
				else if (currentDataUnit.getTimeMilliseconds() < dataStart) {
					continue;
				}
				sendDataUnit(dataBlock, currentDataUnit, timeOffset);
				sentUnits++;
			}
			//			if (dataEnd > endTime || listIterator.hasNext() == false) {
			//				// reset the iterator to be at the start of the data. 
			//				listIterator = dataBlock.getListIterator(0);
			//				currentDataUnit = null;
			//			}
			return sentUnits;
		}

	}

	private class EmTimer implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			emTimerEvent();
		}
	}
}
