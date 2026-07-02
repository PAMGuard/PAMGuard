package networkTransfer.receive;

import networkTransfer.NetworkObject;
import networkTransfer.NetworkReceiverInterface;
import networkTransfer.receive.status.BuoyStatusDataBlock;
import networkTransfer.receive.status.BuoyStatusDataUnit;
import networkTransfer.receive.status.BuoyStatusLogging;
import networkTransfer.receive.status.base.NetReceiverStatusManager;
import networkTransfer.receive.swing.NetworkRXTabPanel;
import networkTransfer.receive.swing.NetworkReceiveDialog;
import networkTransfer.receive.swing.NetworkReceiveSidePanel;
import networkTransfer.receive.swing.RXTableMouseListener;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import AIS.AISDataBlock;
import AIS.AISDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.BinaryStore;
import Acquisition.DaqStatusDataUnit;
import Array.ArrayManager;
import Array.PamArray;
import Array.Streamer;
import Array.streamerOrigin.OriginSettings;
import Array.streamerOrigin.StaticOriginSettings;
import GPS.GpsData;
import GPS.GpsDataUnit;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliser;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliserSelector;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.PamSidePanel;
import PamView.PamTabPanel;
import PamView.dialog.warn.WarnOnce;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.AcousticDataUnit;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.debug.Debug;

/**
 * Receive near real time data over the network in the form of packaged PamDataUnits. 
 * @author Doug Gillespie
 *
 */
public class NetworkReceiver extends PamControlledUnit implements PamSettings {

	private ArrayList<PamDataBlock> rxDataBlocks;
	
	private HashMap<Integer,PamDataBlock> rxDataBlockMap;

	//	private Vector<BuoyRXStats> buoyRXStats = new Vector<BuoyRXStats>();

	private int[] quickBlockIds;

	private NetworkReceiveParams networkReceiveParams = new NetworkReceiveParams();

	private NetworkRXTabPanel networkRXTabPanel;

	private NetworkReceiveProcess networkReceiveProcess;

	public NetworkReceiveProcess getNetworkReceiveProcess() {
		return networkReceiveProcess;
	}

	public static String unitTypeString = "Network Receiver";

	private BuoyStatusDataBlock buoyStatusDataBlock;

	public NetworkReceiverInterface connectionThread;

	private NetworkReceiveSidePanel networkReceiveSidePanel;

	private ArrayList<PairedValueInfo> extraTableInfo = new ArrayList<PairedValueInfo>();
	
	private ArrayList<NetworkDataUser> extraDataUsers = new ArrayList<NetworkDataUser>();
	
	private RXTableMouseListener<BuoyStatusDataUnit> tableMouseListener;
	
	private NetReceiverStatusManager netRxStatusProcess;

	/**
	 * Flags for dataType1 - these must match equivalent commands in 
	 * other network C++ code, so don't mess with them !
	 */
	public static final int  NET_PAM_DATA       = 1;
	public static final int  NET_PAM_COMMAND    = 5;
	public static final int  NET_AUDIO_DATA     = 10; // audio data sent over network from remote station. 


	public static final int NET_PAM_COMMAND_STOP = 0;
	public static final int NET_PAM_COMMAND_PREPARE = 1;
	public static final int NET_PAM_COMMAND_START = 2;

	/**
	 * Some very basic commands to send straight through to receiving stations
	 * @param unitName
	 */
	public NetworkReceiver(String unitName) {
		super(unitTypeString, unitName);
		
		PamSettingManager.getInstance().registerSettings(this);

		networkReceiveProcess = new NetworkReceiveProcess(this);
		addPamProcess(networkReceiveProcess);

		networkRXTabPanel = new NetworkRXTabPanel(this);

		networkReceiveSidePanel = new NetworkReceiveSidePanel(this);

		BuoyDataSerialiser buoyDataSerialiser = new BuoyDataSerialiser(this);
		
		netRxStatusProcess = new NetReceiverStatusManager(this);
		addPamProcess(this.netRxStatusProcess);
		
		PamSettingManager.getInstance().registerSettings(buoyDataSerialiser);
		this.setTableMouseListener(new TableMouseListener());
	}

	@Override
	public PamTabPanel getTabPanel() {
		return networkRXTabPanel;
	}


	@Override
	public PamSidePanel getSidePanel() {
		return networkReceiveSidePanel;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			findDataSources();
			networkRXTabPanel.notifyModelChanged(changeType);
			//startConnectionThread();
		}
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " Settings ...");
		menuItem.addActionListener(new DetectionMenu(parentFrame));
		return menuItem;
	}

	/**
	 * @return the buoyStatusDataBlock
	 */
	public BuoyStatusDataBlock getBuoyStatusDataBlock() {
		return buoyStatusDataBlock;
	}

	private class DetectionMenu implements ActionListener {
		private Frame parentFrame;

		public DetectionMenu(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			detectionDialog(parentFrame);
		}

	}

	/**
	 * Display the network dialog and act if things change.
	 * @param parentFrame parent frame to own dialog
	 */
	public void detectionDialog(Frame parentFrame) {
		NetworkReceiveParams newParams = NetworkReceiveDialog.showDialog(parentFrame, this);
		if (newParams != null) {
			networkReceiveParams = newParams.clone();
			newParams();
		}
	}

	/**
	 * Called when parameters change
	 */
	private void newParams() {
		/*if (connectionThread == null) {
			startConnectionThread();
			return;
		}
		if (connectionThread instanceof StandardTCPReceiver &&
				((StandardTCPReceiver) connectionThread).currentParams.receivePort == networkReceiveParams.receivePort) {
			return; // no need to restart if port number is the same. 
		}

		stopAllConnectionThreads();
		/*
		 * Then start a new connection thread. 
		 */
		//startConnectionThread();
	}

	private void stopAllConnectionThreads() {
		/*
		 * Now need to stop the existing connection thread and restart with the new port number
		 */
		System.out.println("Stop all connection threads !!!!");
		if (connectionThread != null) {
			connectionThread.stopConnectionThread();
		}
		
	}

	private void startConnectionThread() {
		if(this.networkReceiveParams.connectionType==NetworkReceiveParams.CONNECTIONTYPE_STANDARD_TCP) {
			connectionThread = new StandardTCPReceiver(networkReceiveParams,this);
		}else {
			if(connectionThread==null) {
				connectionThread = new MqttNetReceiver(networkReceiveParams,this);
			}
		}
		connectionThread.runReceiver();
	}
	
	private void generateDataBlockMap() {
		rxDataBlockMap = new HashMap<Integer,PamDataBlock>();
		if(rxDataBlocks==null) {
			return;
		}
		for(PamDataBlock nextBlock:this.rxDataBlocks) {
			rxDataBlockMap.put(nextBlock.getQuickId2(), nextBlock);
		}
	}

	/**
	 * Find all data sources / sinks which may be about to use data from the network. 
	 */
	private void findDataSources() {
		rxDataBlocks = BinaryStore.getStreamingDataBlocks(false);
		int n = rxDataBlocks.size();
		quickBlockIds = new int[n*2];
		/*
		 * For now use both ID's - the one based on the full long data name and the one on the shorter 
		 * name until all Decimus are using the new long name system. 
		 */
		for (int i = 0, j = n; i < n; i++, j++) {
			quickBlockIds[i] = rxDataBlocks.get(i).getQuickId2();
			quickBlockIds[j] = rxDataBlocks.get(i).getQuickId();
//			System.out.printf("Network stream %s has ids 0x%X and 0x%x\n", rxDataBlocks.get(i).getDataName(), quickBlockIds[i], quickBlockIds[j]);
		}
		
		generateDataBlockMap();
		//		initRXStats();
	}

	private int findStreamDataBlock(int quickId) {
		if (quickBlockIds == null) {
			return -1;
		}
		int blockId = -1;
		for (int i = 0; i < quickBlockIds.length; i++) {
			if (quickBlockIds[i] == quickId) {
				blockId = i;
				break;
			}
		}
		/**
		 * Quick bodge to all for both the old and the new 
		 * quick id's being currently used. 
		 */
		if (blockId >= rxDataBlocks.size()) {
			blockId -= rxDataBlocks.size();
		}
		return blockId;
	}


	//	/**
	//	 * Check the array has enough hydrophones.
	//	 * If it hasn't, then make them ! 
	//	 * @param channel
	//	 */
	//	private void checkArrayChannel(int channel) {
	//		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
	//		ArrayList<Hydrophone> phones = array.getHydrophoneArray();
	//		Hydrophone newPhone;
	//		Hydrophone anyPhone = array.getHiddenHydrophone(0);
	//		if (anyPhone == null) {
	//			double[] bw = {0., 500000.};
	//			anyPhone = new Hydrophone(0, 0, 0, 0, "Made up", -170, bw, 0);
	//		}
	//		while (phones.size() < channel+1) {
	//			newPhone = new Hydrophone(phones.size(), channel*10., channel*10., anyPhone.getDepth(), 
	//					anyPhone.getType(), anyPhone.getSensitivity(), anyPhone.getBandwidth(), anyPhone.getPreampGain());
	//			array.addHydrophone(newPhone);
	//		}
	//	}

	/**
	 * Called whenever the command status of a buoy changes. 
	 * May require us to open or close data files, etc. 
	 * @param changeTime 
	 * @param bsdu
	 */
	private synchronized void commandStateChanged(long changeTime, BuoyStatusDataUnit bsdu) {
		int nStopped = 0;
		int nPrepared = 0;
		int nStarted = 0;
		ListIterator<BuoyStatusDataUnit> it = buoyStatusDataBlock.getListIterator(0);
		BuoyStatusDataUnit b;
		while (it.hasNext()) {
			b = it.next();
			switch(b.getCommandStatus()) {
			case NET_PAM_COMMAND_PREPARE:
				nPrepared++;
				break;
			case NET_PAM_COMMAND_START:
				nStarted++;
				break;
			case NET_PAM_COMMAND_STOP:
				nStopped++;
				break;
			}
		}

		/**
		 * May now need to tell the overall pam controller that things are starting or
		 * stopping !
		 */
		if(networkReceiveParams.connectionType==NetworkReceiveParams.CONNECTIONTYPE_STANDARD_TCP) {
			PamController.getInstance().netReceiveStatus(changeTime, nPrepared, nStarted, nStopped);
		}
	}

	/**
	 * 
	 * @return The total number of bytes received since the last call to this function
	 */
	public synchronized int getRecentDataBytes() {
		return 0;
	}

	public synchronized int getRecentPackets() {
		return 0;
	}
	

	int findErrors = 0;
	
	
	
	
	//Code here has been commented out to resolve errors with Sam modifications not compatible with Doug's revamped network NI card code. 
	//In Merge between SMRUC and Main Fork June 2026, following is not complete, tested, or functional:
	/*
	 * ======BEGIN BLOCK=====
	 */
	private NetworkAudioInterpreter networkAudioInterpreter;

//	@Override
//	public synchronized NetworkObject interpretData(NetworkObject receivedObject) {
//		
//		/**
//		 * Although there are many NetworkReceiveThread's there is only one
//		 * of thee, so at this point data from all senders end up in the same
//		 * thread. 
//		 */
//
//		//addPacketStats(receivedObject.getTransferedLength());
//		
//		BuoyStatusDataUnit buoyStatusDataUnit = findBuoyStatusDataUnit(receivedObject.getBuoyId1(), receivedObject.getBuoyId2(), true);
//		buoyStatusDataUnit.setSocket(receivedObject.getSocket());
//		// work out how long different socket asking questions take. 
////		long t1 = System.currentTimeMillis();
////		InetAddress inAddr = socket.getInetAddress();
////		long t2 = System.currentTimeMillis();
////		byte[] addr = inAddr.getAddress();
////		String addrS = new String(addr);
////		long t3 = System.currentTimeMillis();
////		String hostName = inAddr.getHostName(); // takes 4.5s
////		long t4 = System.currentTimeMillis();
////		String canonicalName = inAddr.getCanonicalHostName(); // takes 4.5s
////		long t5 = System.currentTimeMillis();
////		String hostAddr = inAddr.getHostAddress(); // takes 0ms. 
////		long t6 = System.currentTimeMillis();
////		System.out.printf("Host time: inAddr %d, getAddress %d, hostName %s %d, CanonicalHostName %s %d, hostAddress %s %dms\n",
////				t2-t1, t3-t2, hostName, t4-t3, canonicalName, t5-t4, hostAddr, t6-t5);
//		/*
//		 * Use getHostAddress() since it's faster. 
//		 */
//		
//		
//		//		if (dataId1 != NET_PAM_DATA) {
////					String str = new String(duBuffer);
////					str = str.substring(0, dataLen);
////					System.out.println(String.format("Buoy data arrived id %d / %d, %s", dataId1, dataId2, str));
//		//		}
//
//		NetworkObject retObject = null;
//		switch(receivedObject.getDataType1()) {
//		case NET_PAM_DATA:
//			retObject = interpretPamData(receivedObject, buoyStatusDataUnit);
//			break;
//		case NET_PAM_COMMAND:
//			retObject = interpretPamCommand(receivedObject, buoyStatusDataUnit);
//			break;
//		case NET_AUDIO_DATA:
//			if (networkAudioInterpreter == null) {
////				networkAudioInterpreter = new CRioAudioInterpreter(this);
//				return null;
//			}
//			//networkAudioInterpreter.interpretData(receivedObject, buoyStatusDataUnit);
//		}
//		for (NetworkDataUser extraUser : extraDataUsers) {
//			retObject = extraUser.interpretData(receivedObject);
//		}
//		buoyStatusDataBlock.updatePamData(buoyStatusDataUnit, PamCalendar.getTimeInMillis());
//		return retObject;
//	}

	/**
	 * Before opening a new thread for this client, check to see 
	 * if that client already has a receive thread open and then
	 * if it has close it 
	 * @param clientSocket
	 * @param wantedThread The thread we just made, so want to keep. 
	 */
	/*public synchronized void checkExistingThreads(Socket clientSocket, NetworkReceiveThread wantedThread) {
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
	}*/


	/*
	 * ===== END BLOCK =====
	 */

	
	/**
	 * 
	 * @param socket Socket data were read from
	 * @param dataVersion2 This is from the Network packet header and should be used the same way as the HeaderVersion, i.e. for major
	 * structural changes to the data format. 
	 * @param buoyStatusDataUnit data unit holding information about this sending station
	 * @param dataId1  station id 1
	 * @param dataId2 station id 2
	 * @param dataLen data length 
	 * @param duBuffer data, including additional header information. 
	 * @return 
	 */
	public  NetworkObject interpretPamData(NetworkObject receivedData, BuoyStatusDataUnit buoyStatusDataUnit) {
		PamDataBlock dataBlock = rxDataBlockMap.get(receivedData.getDataType2());
		if(dataBlock==null) {
			System.out.println("Received data of unknown type: "+receivedData.getDataType2()+". Make sure that you have a module on the base station for each of the modules you have for the CABs");
			return null;
		}
		BinaryDataSource dataSource = dataBlock.getBinaryDataSource();
		PamProcess parentProcess = dataBlock.getParentProcess();
		// we'll want to see if we can find the latest acquisition status data unit. 

		// now need to pack that up into something very similar to the data we'd read from a binary file. 
		ByteArrayInputStream bis = new ByteArrayInputStream(receivedData.getData());
		DataInputStream ds = new DataInputStream(bis);
		// unpack the header information
		int objectId = 0; 
		int moduleVersion = 0;
//		long millis = 0;
//		Long nanos = null;
//		int channelMap = 0;
		int dataLength = 0;
		byte[] data = null;
		DataUnitBaseData baseData = new DataUnitBaseData();
		try {
			objectId = ds.readInt();
			moduleVersion = ds.readInt();
//			millis = ds.readLong();
//			if (dataVersion2 >= 2) {
//				nanos = ds.readLong();
//				channelMap = ds.readInt();
//			}
			baseData.readBaseData(ds, receivedData.getDataVersion());
			dataLength = ds.readInt();
			data = new byte[dataLength];
			int bytesRead = ds.read(data);
//			System.out.printf("NetRX read %d of expected %d bytes\n", bytesRead, dataLength);
//			if (1>0) return null;
			ds.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		BinaryObjectData bod = new BinaryObjectData(receivedData.getDataVersion(), objectId, baseData, 0, data, dataLength);
//		baseData.setTimeNanoseconds(nanos);
//		baseData.setChannelBitmap(channelMap);

		//		String str = String.format("Net data received a %s from buoy %d(%d) type %d(%d), data length = %d for %s",
		//				PamCalendar.formatDateTime(millis),buoyId1, buoyId2, dataId1, dataId2, dataLen, dataBlock.getDataName());
		//		System.out.println(str);

		PamDataUnit dataUnit = dataSource.sinkData(bod, null, moduleVersion);
		if (dataUnit == null) {
			System.out.println("Null data unit from dataSource: " + dataSource.getModuleName());
			return null;
		}
//		if (dataUnit.getChannelBitmap() == 0) {
//			System.out.println("Zero channel bitmap in data unit " + dataUnit.getClass().getName());
//		}
		//		
		//		BuoyStatusDataUnit buoyStats = findBuoyStatusDataUnit(buoyId1, buoyId2, true);
		//		if (dataUnit.getClass().)

		buoyStatusDataUnit.setTimeMilliseconds(dataUnit.getTimeMilliseconds());
//		buoyStatusDataUnit.setSocket(receivedData.getSocket());
		
		DaqStatusDataUnit previousDaqStatus = (DaqStatusDataUnit) buoyStatusDataUnit.findLastDataUnit(DaqStatusDataUnit.class);
		
		buoyStatusDataUnit.newDataObject(dataBlock, dataUnit, receivedData.getTransferedLength());
		if (buoyStatusDataUnit.getCommandStatus() != NET_PAM_COMMAND_START) {
			buoyStatusDataUnit.setCommandStatus(NET_PAM_COMMAND_START);
			commandStateChanged(dataUnit.getTimeMilliseconds(), buoyStatusDataUnit);
		}

		if (networkReceiveParams.channelNumberOption == NetworkReceiveParams.CHANNELS_RENUMBER) {
			dataUnit.setChannelBitmap(dataUnit.getChannelBitmap()<<buoyStatusDataUnit.getLowestChannel());
			dataUnit.clearOandAngles();
		}

		// always take the channel numbers from the buoy status data unit.
		// just shift channel numbers by number of the first channel. 
		//		int firstchan = buoyStatusDataUnit. 
		// pass the unit on to any external process that might want to fiddle with it
		for (NetworkDataUser ndu : extraDataUsers) {
			ndu.newReceivedDataUnit(buoyStatusDataUnit, dataUnit);
		}

		Long sampleNumber = null;
		if (PamDataUnit.class.isAssignableFrom(dataUnit.getClass())) {
			sampleNumber = ((PamDataUnit) dataUnit).getStartSample();
			checkAcousticBuoyStats(buoyStatusDataUnit);
		}
		if (sampleNumber != null) {
			dataBlock.masterClockUpdate(dataUnit.getTimeMilliseconds(), sampleNumber);
		}

		//		System.out.println("New data from " + dataSource.getModuleName() + " on channels " + dataUnit.getChannelBitmap());

		DaqStatusDataUnit daqStatus = null;
		if (dataBlock.getUnitClass() == DaqStatusDataUnit.class) {
//			((DaqStatusDataUnit) dataUnit).calculateTrueSampleRate(previousDaqStatus);
		}
		else {
			daqStatus = (DaqStatusDataUnit) buoyStatusDataUnit.findLastDataUnit(DaqStatusDataUnit.class);
		}
		if (daqStatus != null) {
			// now update the millisecond and nanosecond times in the current data unit. 
			if (daqStatus.getGpsPPSMilliseconds() != null 
					&& daqStatus.getGpsPPSMilliseconds() != 0 // was causing terrible trouble by setting time to 0 !! 
					&& daqStatus.sampleRate > 0) {
				long oldMillis = dataUnit.getTimeMilliseconds();
				long millisOff = daqStatus.getGpsPPSMilliseconds() - daqStatus.getTimeMilliseconds();
				dataUnit.setTimeMilliseconds(oldMillis + millisOff);
				if (AcousticDataUnit.class.isAssignableFrom(dataUnit.getClass())) {
					// also update the nanosecond time stamp in data unit.
					//AcousticDataUnit aDataUnit = (AcousticDataUnit) dataUnit; 
					if (dataUnit.getStartSample() != 0) {
						double dataFS = dataBlock.getSampleRate();
						double ppsFS = daqStatus.sampleRate;
						long nanosOff = (long) ((dataUnit.getStartSample()/dataFS - daqStatus.getSamples()/ppsFS) * 1.e9);
//						if (daqStatus.gett)
						long trueNanos = daqStatus.getGpsPPSMilliseconds() * 1000000 + nanosOff;
					}
				}
				else {
					// can still set an updated nanosecond time based on the millisecond time. 
					
					
				}
			}
		}
		
		/* 
		 * GPS data get handled in a special way ...
		 * This method is no longer used since gps data get sent direct interpreted 
		 * with other system data direct from the buoy control systems and don't generally come in as 
		 * data units. 
		 * This could still get used if GPS data were sent from PAMGuard rather than PAMBuoy. 
		 */
		if (dataUnit.getClass() == GpsDataUnit.class) {
			System.out.println("new gps data");
			useBuoyGPSData(buoyStatusDataUnit, (GpsDataUnit) dataUnit);
			//			networkReceiveProcess.newGpsData((GpsDataUnit) dataUnit);
		}
		else {
			parentProcess.processNewBuoyData(buoyStatusDataUnit, dataUnit);
			if(networkReceiveParams.channelNumberOption==networkReceiveParams.CHANNELS_RENUMBER) {
				dataUnit.setUID(0);
			}
			if(dataBlock instanceof AISDataBlock) {
				((AISDataBlock) dataBlock).addAISData((AISDataUnit) dataUnit);
			}else {
				dataBlock.addPamData(dataUnit);
			}
			//dataBlock.addPamData(dataUnit);
			//			System.out.println("DAta added to data block " + dataBlock.getDataName());
		}
		return null;
	}

	public NetworkObject interpretPamCommand(NetworkObject receivedData, BuoyStatusDataUnit buoyStatusDataUnit) {
//			System.out.println("Network command received: " + getPamCommandString(dataId2));
		buoyStatusDataUnit.setCommandStatus(receivedData.getDataType2());
		long time = PamCalendar.getTimeInMillis();
		if (receivedData.getData() != null && receivedData.getDataLength() >= 8) {
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(receivedData.getData()));
			try {
				time = dis.readLong();
				dis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		buoyStatusDataBlock.updatePamData(buoyStatusDataUnit, time);
		/**
		 * Now have a wee think about what to do based on the overall status
		 * of multiple buoys 
		 */
		commandStateChanged(time, buoyStatusDataUnit);
		
		return null;
	}

	public static String getPamCommandString(int command) {
		switch(command) {
		case NET_PAM_COMMAND_PREPARE:
			return "Prepare";
		case NET_PAM_COMMAND_START:
			return "Running";
		case NET_PAM_COMMAND_STOP:
			return "Stopped";
		}
		return null;
	}

	/**
	 * Find buoy status data and optionally create it if it doesn't exist. 
	 * @param buoyId1 buoy Id 1
	 * @param buoyId2 buoy Id 2
	 * @param create automatically create
	 * @return status data unit or null if not found. 
	 */
	public BuoyStatusDataUnit findBuoyStatusDataUnit(int buoyId1, int buoyId2, boolean create) {
		BuoyStatusDataUnit du = buoyStatusDataBlock.findBuoyStatusData(buoyId1, buoyId2);
		if (du != null) {
			du.setNetworkReceiver(this);
			return du;
		}
		if (create == false) {
			return null;
		}

		du = new BuoyStatusDataUnit(this, buoyId1, buoyId2, 0);
		Streamer buoyStreamer = findBuoyStreamer(du);
		int phones = buoyStreamer.getStreamerHydrophones();
		if (phones == 0) {
			phones = 1<<findFirstFreeChannel();
		}
		du.setHydrophoneStreamer(buoyStreamer);
		du.setChannelBitmap(phones);

		buoyStatusDataBlock.addPamData(du);
		return du;
	}

	/**
	 * Find a buoy status data unit from it's inet address.
	 * @param address
	 * @return status data unit. 
	 */
	public BuoyStatusDataUnit findBuoyStatusDataUnit(InetAddress address) {
		return buoyStatusDataBlock.findBuoyStatusData(address);
	}

	/**
	 * Check the hydrophone array configuration for this status information.<p> 
	 * What happens is the following:
	 * <br> Each buoy associates itself with one streamer in the array manager. 
	 * <br> The buoy stats searches for a streamer with it's own identifier and links 
	 * to it. 
	 * <br> if it can't find a streamer with it's own id, it clones the new one 
	 * and any associated hydrophones. The exception to this is if the first streamer 
	 * does not have a buoy id, in which case it steals it. 
	 * <br> Streamers must either be set up explicitly for each buoy in a system or must
	 * at least have the same number of channels and hydrophone layout as the first streamer. 
	 * @param buoyStats buoy status data. 
	 */
	private void checkHydrophoneArray(BuoyStatusDataUnit buoyStats) {
		findBuoyStreamer(buoyStats);
	}

	/**
	 * this gets called whenever a new buoy appears and attempts to find
	 * the appropriate streamer for that buoy. If there isn't a streamer with this
	 * buoy id, it will clone the streamer and make a new one. 
	 * @param du
	 */
	private Streamer findBuoyStreamer(BuoyStatusDataUnit du) {
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
		Streamer streamer = currentArray.findStreamer(du.getBuoyName());
		if (streamer != null) {
			du.setHydrophoneStreamer(streamer);
			return streamer;
		}
		streamer = currentArray.cloneStreamer();
		streamer.setStreamerName(du.getBuoyName());
		/*
		 * Now need to tell the acquisition module that another streamer has been 
		 * added and tell it to associate the channels of hydrophones on this 
		 * streamer with the channel numbers of this buoy.  
		 * 
		 * For some reason, this may not be actually necessary - need to remember how
		 * it handles channel numbers if the acquisition doesn't really support it. 
		 * Seem to remember there is some bodge since it's likely that the click 
		 * detector wn't have a clue as to the which channels are present either.  
		 */

		return streamer;

	}

	/**
	 * If data units coming out of this buoy are acoustic, then it will need
	 * hydrophones. Note that all this code needs to work with non acoustic 
	 * data too - such as logger forms, in which case it wn' tbe necessary
	 * to have hydrophones. 
	 * @param buoyStatusDataUnit status data. 
	 */
	public void checkAcousticBuoyStats(BuoyStatusDataUnit buoyStatusDataUnit) {
		// check that the buoy id can be associated with the right hydrophones
		// and create a bearing localiser.  
		if (buoyStatusDataUnit.getHydrophoneStreamer() != null) {
			return;
		}
		checkHydrophoneArray(buoyStatusDataUnit);
		int phones = ArrayManager.getArrayManager().getCurrentArray().getPhonesForStreamer(buoyStatusDataUnit.getHydrophoneStreamer());
		int[] phonesList = PamUtils.getChannelArray(phones);
		BearingLocaliser bearingLocaliser = BearingLocaliserSelector.createBearingLocaliser(phonesList, 1e-5);
	}

	/**
	 * find the first free channel number for a new Buoy status object.
	 * @return new unassigned channel
	 */
	private int findFirstFreeChannel() {
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if (buoyStatusDataBlock.findDataUnit(1<<i) == null) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @return the rxDataBlocks
	 */
	public ArrayList<PamDataBlock> getRxDataBlocks() {
		return rxDataBlocks;
	}

	private class NetworkReceiveProcess extends PamProcess {

		public NetworkReceiveProcess(NetworkReceiver networkReceiver) {
			super(networkReceiver, null);
			buoyStatusDataBlock = new BuoyStatusDataBlock(this,networkReceiver);
			addOutputDataBlock(buoyStatusDataBlock);
			buoyStatusDataBlock.setOverlayDraw(new NetworkGPSDrawing(networkReceiver));
			buoyStatusDataBlock.setPamSymbolManager(new StandardSymbolManager(buoyStatusDataBlock, NetworkGPSDrawing.defaultSymbol, true));
			buoyStatusDataBlock.SetLogging(new BuoyStatusLogging(networkReceiver, buoyStatusDataBlock));
		}

		@Override
		public void pamStart() {
			startConnectionThread();
		}

		@Override
		public void pamStop() {
			stopAllConnectionThreads();
		}

	}

	/**
	 * @return the networkReceiveParams
	 */
	public NetworkReceiveParams getNetworkReceiveParams() {
		return networkReceiveParams;
	}

	@Override
	public Serializable getSettingsReference() {
		return networkReceiveParams;
	}

	@Override
	public long getSettingsVersion() {
		return NetworkReceiveParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		networkReceiveParams = ((NetworkReceiveParams) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}


	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#pamClose()
	 */
	@Override
	public void pamClose() {

		super.pamClose();
		stopAllConnectionThreads();
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#canClose()
	 */
	@Override
	public boolean canClose() {
		// TODO Auto-generated method stub
		return super.canClose();
	}

	public ArrayList<PairedValueInfo> getExtraTableInfo() {
		return extraTableInfo;
	}
	
	/**
	 * Add extra data fields to the table, data units, etc. this call primarily adds
	 * them to the list of stuff that will get displayed. 
	 * @param pairedValueInfo Information about a value that will be stored in the 
	 * appropriate BuoyStatusDataunit
	 */
	public void addExtraTableInfo(PairedValueInfo pairedValueInfo) {
		extraTableInfo.add(pairedValueInfo);
		if (networkRXTabPanel != null) {
			networkRXTabPanel.configurationChange();
		}
	}
		
	/**
	 * Add an extra data user which will get called with ALL data arriving 
	 * over the network so can handle additional functions / data types not 
	 * managed by the standard build in data user which doesn't know much 
	 * about anything but PAMDataUnits. 
	 * @param extraDataUser
	 */
	public void addExtraDataUser(NetworkDataUser extraDataUser) {
		extraDataUsers.add(extraDataUser);
	}
	
	public ArrayList<NetworkDataUser> getExtraDataUsers() {
		return extraDataUsers;
	}

	public boolean useBuoyGPSData(BuoyStatusDataUnit buoyStatusDataUnit, GpsDataUnit gpsDataUnit) {
		GpsData gpsData = gpsDataUnit.getGpsData();
		if (gpsData == null || gpsData.isDataOk() == false) {
			return false;
		}
		if (gpsData.isDataOk()) {			
			gpsDataUnit.setChannelBitmap(1<<buoyStatusDataUnit.getLowestChannel());
			buoyStatusDataUnit.setGpsData(gpsDataUnit.getTimeMilliseconds(), gpsData);
			buoyStatusDataBlock.updatePamData(buoyStatusDataUnit, System.currentTimeMillis());
			// now also have to update the streamer data directly. 
			Streamer streamer = buoyStatusDataUnit.getHydrophoneStreamer();
			// don't check for null streamer since I want it to crash if there isn't one !
			// create a Static origin settings object. 
			OriginSettings os = streamer.getOriginSettings();
			StaticOriginSettings sos;
			if (os != null && StaticOriginSettings.class.isAssignableFrom(os.getClass())) {
				sos = (StaticOriginSettings) os;
			}
			else {
				sos = new StaticOriginSettings();
			}
			sos = sos.clone();
			sos.setStaticPosition(streamer, gpsData);
			ArrayManager.getArrayManager().getCurrentArray().newStreamerFromUpdate(gpsDataUnit.getTimeMilliseconds(), 
					streamer.getStreamerIndex(), null, null, null, sos, null);
		}
		return true;
	}
	
	private class TableMouseListener implements RXTableMouseListener<BuoyStatusDataUnit> {

		@Override
		public void popupMenuAction(MouseEvent e, BuoyStatusDataUnit dataUnit, String colName) {
			//			System.out.println("Popup menu on column " + colName + " " + dataUnit);
			showDecimusPopup(e, dataUnit, colName);
		}

	}
	
	public void showDecimusPopup(MouseEvent e, BuoyStatusDataUnit dataUnit, String colName) {
		String station = null;
		JPopupMenu popMenu = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("Clear list ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeDataUnits(e, null);
			}
		});
		popMenu.add(menuItem);

		popMenu.show(e.getComponent(), e.getX(), e.getY());
	}
	
	protected void removeDataUnits(ActionEvent e, BuoyStatusDataUnit dataUnit) {
		String warnMsg;
		if (this.getBuoyStatusDataBlock() == null) {
			return;
		}
		if (dataUnit == null) {
			warnMsg = "Delete all Decimus devices from list";
		}
		else {
			warnMsg = String.format("Delete Decimus %d(%d) from list", dataUnit.getBuoyId1(), dataUnit.getBuoyId2());
		}
		int ans = WarnOnce.showWarning(getGuiFrame(), "Remove Deimus units", warnMsg, WarnOnce.OK_CANCEL_OPTION);
		if (ans != WarnOnce.OK_OPTION) {
			return;
		}
		if (dataUnit == null) {
			this.getBuoyStatusDataBlock().clearAll();
		}
		else {
			this.getBuoyStatusDataBlock().remove(dataUnit);
		}
		this.getBuoyStatusDataBlock().notifyObservers();
	}

	/**
	 * @return the tableMouseListener
	 */
	public RXTableMouseListener<BuoyStatusDataUnit> getTableMouseListener() {
		return tableMouseListener;
	}

	/**
	 * @param tableMouseListener the tableMouseListener to set
	 */
	public void setTableMouseListener(RXTableMouseListener<BuoyStatusDataUnit> tableMouseListener) {
		this.tableMouseListener = tableMouseListener;
	}

	public NetworkAudioInterpreter getNetworkAudioInterpreter() {
		return networkAudioInterpreter;
	}

	public void setNetworkAudioInterpreter(NetworkAudioInterpreter networkAudioInterpreter) {
		this.networkAudioInterpreter = networkAudioInterpreter;
	}
}
