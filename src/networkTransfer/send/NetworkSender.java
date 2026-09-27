package networkTransfer.send;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.Timer;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamModel.SMRUEnable;
import PamView.PamSidePanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import networkTransfer.NetworkClient;
import networkTransfer.NetworkParams;
import networkTransfer.emulator.NetworkEmulator;
import networkTransfer.mqttClient.PamMqttClient;
import pamguard.GlobalArguments;
import warnings.PamWarning;
import warnings.WarningSystem;

/**
 * Send near real time data over the network to another PAMGUARD configuration.
 * <p>Not currently configured in Java.  
 * @author Doug Gillespie
 *
 */
public class NetworkSender extends PamControlledUnit implements PamSettings {

	/**
	 * These two left in since they are used in the BatchProcessing plugin. 
	 * The batch processor has been updated to use the newer definitions in 
	 * the NetSendCommandParam enum, so will be OK in new releases, but current
	 * versions of the BP will fail with this PG. So leave these in for a couple
	 * of years until people are likely to have updated their BP. DG 2026-07-09
	 */
	@Deprecated
	public static final String ID1 = "-netSend.id1";
	@Deprecated
	public static final String ID2 = "-netSend.id2";
	/*public static final String ADDRESS = "-netSend.address";
	public static final String PORT = "-netSend.port";
	public static final String USER = "-netSend.user";
	public static final String PASSWORD = "-netSend.password";

	public static final String USESSL = "-netSend.ssl";
	public static final String USEMQTT = "-netSend.mqtt";
	public static final String TRUSTPATH = "-netSend.trustPath";
	public static final String TRUSTPASS = "-netSend.trustPass";
	public static final String KEYPATH = "-netSend.keyPath";
	public static final String KEYPASS = "-netSend.keyPass";
	public static final String SENDJSON = "-netSend.json";
	public static final String PERSISTANCE_DIRECTORY = "-netSend.percistanceDir";*/


	protected NetworkSendParams networkSendParams = new NetworkSendParams();
	private NetworkEmulator networkEmulator;
	private boolean initialisationComplete = false;
	private NetworkSendSidePanel sidePanel;
	private NetworkSendProcess commandProcess;
	//PamWarning sendWarning;
	public NetworkClient client;
	
	public static final String UNIT_TYPE = "Network Sender";
	
	public NetworkSender(String unitName) {
		super(UNIT_TYPE, unitName);

		PamSettingManager.getInstance().registerSettings(this);
		
		checkCommandLineOptions();
		
//		if(this.networkSendParams.hasSendFormat(NetworkSendParams.NETWORKSEND_BYTEARRAY)) {
//		if(this.networkSendParams.getSendSelection(null, getQueueLength())) {
		// not really got an option for this - do we always want to send the command data, or does this need to be optionsl ? 
			commandProcess = new NetworkSendProcess(this, null,NetworkSendParams.NETWORKSEND_BYTEARRAY);
			commandProcess.setCommandProcess(true);
			addPamProcess(commandProcess);
//		}
		
		sidePanel = new NetworkSendSidePanel(this);
		
		//ST added 8/13/2026 -- allow configuration to effectively turn off the net send client. 
		//If net sending is enabled then let this run, but otherwise just skip past it.. 
		if(this.networkSendParams.isModuleActivated()) {
			initializeClient();
		}
		
	}
	
	private boolean activatedByCommand = false;
	
	/**
	 * Do this here, not in restore settings, otherwise it's not called the first time the module is added. 
	 */
	private void checkCommandLineOptions() {
		String address = GlobalArguments.getParam(NetSendCommandParam.ADDRESS.arg);
		String portString = GlobalArguments.getParam(NetSendCommandParam.PORT.arg);
		String id1String = GlobalArguments.getParam(NetSendCommandParam.ID1.arg);
		String id2String = GlobalArguments.getParam(NetSendCommandParam.ID2.arg);
		String usesslString = GlobalArguments.getParam(NetSendCommandParam.USESSL.arg);
		String usemqttString = GlobalArguments.getParam(NetSendCommandParam.USEMQTT.arg);
		String trustStorePathString = GlobalArguments.getParam(NetSendCommandParam.TRUSTPATH.arg);
		String trustStorePassString = GlobalArguments.getParam(NetSendCommandParam.TRUSTPASS.arg);
		String keyPathString = GlobalArguments.getParam(NetSendCommandParam.KEYPATH.arg);
		String keyPassString = GlobalArguments.getParam(NetSendCommandParam.KEYPASS.arg);
		String user = GlobalArguments.getParam(NetSendCommandParam.USER.arg);
		String password = GlobalArguments.getParam(NetSendCommandParam.PASSWORD.arg);
		String useJson = GlobalArguments.getParam(NetSendCommandParam.SENDJSON.arg);
		String persistenceDir = GlobalArguments.getParam(NetSendCommandParam.PERSISTANCE_DIRECTORY.arg);
		String senderActive = GlobalArguments.getParam(NetSendCommandParam.ACTIVE.arg);

		/*
		 * If a runtime arg is passed for activating/deactivating the network sender, then pass to the parameters.
		 * Assume by default that if this module is present then it should be set to active.
		 * ST Aug 13 2026
		 */
		if(senderActive!=null) {
			boolean isActive = Boolean.valueOf(senderActive);
			networkSendParams.setModuleActivated(isActive);
			if(isActive) {
				this.activatedByCommand = true;
				System.out.println("Pamguard is running with network sending ENABLED");
			}else {
				System.out.println("Pamguard is running with network sending DISABLED");
			}
		}else {
			networkSendParams.setModuleActivated(true);
		}
		
		if(user!=null) {
			networkSendParams.userId = user;
		}
		
		if(password!=null) {
			networkSendParams.password = password;
		}
	
		if (address != null) {
			networkSendParams.ipAddress = address; // remember it. 
		}
		
		if(portString != null) {
			networkSendParams.portNumber = Integer.valueOf(portString);
		}
		
		if(id1String!=null) {
			networkSendParams.stationId1 = Integer.valueOf(id1String);
		}
		
		if(id2String!=null) {
			networkSendParams.stationId2 = Integer.valueOf(id2String);
		}
		
		if(usesslString != null) {
			networkSendParams.useSSL = Boolean.valueOf(usesslString);
		}
		
		if(usemqttString!=null) {
			networkSendParams.mqtt = Boolean.valueOf(usemqttString);
		}
		
		if(trustStorePathString!=null) {
			networkSendParams.trustStorePath = trustStorePathString;
		}
		
		if(trustStorePassString!=null) {
			networkSendParams.trustStorePassword = trustStorePassString;
		}
		
		if(keyPathString!=null) {
			networkSendParams.keyStorePath = keyPathString;
		}
		
		if(keyPassString!=null) {
			networkSendParams.keyStorePassword = keyPassString;
		}
		
		if(persistenceDir!=null) {
			networkSendParams.persistenceDirectory = persistenceDir;
		}
		
		/*
		 * In most contexts the persistence directory is just going to be pamguard home. 
		 * With configs passed back and forth between windows and linux machines, just set the persistence dir to PG home if the filepath format is the wrong OS type
		 * If the path is a windows-like path, and looks like it is a pamguard home path, make sure that it is the pamguard home path for the current machine
		 * (annoying when a new user folder is created when a config is passed to a new computer)
		 * 
		 * If persistence directory is NOT set, then set it to pamguard home
		 * If persistence directory is set, and the filepath is consistent with the current OS, and it is not a pamguard home-like directory, then move on
		 */
		networkSendParams.verifyCorrectPersistanceDirectory();
		System.out.println("Decided to set persistance directory to "+networkSendParams.persistenceDirectory);
		//Make it easy on users who may not know the details of MQTT -- if station ID is NOT set, then just set it to 'BaseStation' (the CAB/APS will set its ID to the pb###)
		//If stationID is set, then move on
		networkSendParams.checkStationID();
		//Make it easy on users who may not know the details of MQTT -- if base topic is NOT set, then just set it to 'APS'
		//If base topic is set, then move on.
		networkSendParams.checkBaseTopic();
		
		/**
		 * Do we need code here that will allow both ? 
		 * This will need updated to allow both, since send fmt is now on a block by block basis, so these commands
		 * have become irrelevent. 
		 */
//		boolean isSetJson = (networkSendParams.getSendingFormat() & NetworkSendParams.NETWORKSEND_JSON) != 0;
//		if(useJson!=null) {
//			isSetJson = Boolean.valueOf(useJson);
//		}
//		
//		
//		int sendFmt = 0;
//		if(isSetJson) {
//			sendFmt = NetworkSendParams.NETWORKSEND_JSON;
//		}
//		else {
//			sendFmt = NetworkSendParams.NETWORKSEND_BYTEARRAY;
//		}
//		networkSendParams.setSendingFormat(sendFmt);		
	}

	public void initializeClient() {
		if(client!=null && !client.requireReconnect) {
			return;
		}
		if(this.networkSendParams.mqtt) {
			client = new PamMqttClient(this.networkSendParams);
		}else {
			client = new TCPSendClient(this.networkSendParams);
		}
	}
	
	public void closeClient() {
		//ST added 8/13/2026 -- allow configuration to effectively turn off the net send client.
		//Client may have never been initialized
		if(client!=null) {
			this.client.close();
		}
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " Settings ...");
		menuItem.addActionListener(new SenderSettings(parentFrame));
		if (SMRUEnable.isEnable() && isViewer) {
			JMenu menu = new JMenu(getUnitName());
			menu.add(menuItem);
			menuItem = new JMenuItem("Emulate Transmitted Data ...");
			menuItem.addActionListener(new MitigateEmulateMenu(parentFrame));
			menu.add(menuItem);
			return menu;
		}
		else {
			return menuItem;
		}
	}
	
	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#getSidePanel()
	 */
	@Override
	public PamSidePanel getSidePanel() {
		return sidePanel;
	}

	private class SenderSettings implements ActionListener {

		private Frame parentFrame;

		public SenderSettings(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			senderSettings(parentFrame);			
		}
		
	}

	public void senderSettings(Frame parentFrame) {
		NetworkSendParams p = NetworkSendDialog.showDialog(parentFrame, this, networkSendParams);
		if (p != null) {
			networkSendParams = (NetworkSendParams) p.clone();
			sortDataSources();
		}
	}

	private class MitigateEmulateMenu implements ActionListener {
		
		private Frame parentFrame;

		public MitigateEmulateMenu(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			mitigateEmulate(parentFrame);			
		}
	}
	
	/**
	 * Call the emulator to pop up a dialog which willcontrol everything. 
	 * @param parentFrame
	 */
	public void mitigateEmulate(Frame parentFrame) {
		getNetworkEmulator().showEmulateDialog(parentFrame);
	}
	
	/**
	 * Get  / create the NetworkEmulator. 
	 * @return
	 */
	private NetworkEmulator getNetworkEmulator() {
		if (networkEmulator == null) {
			//networkEmulator = new NetworkEmulator(this);
		}
		return networkEmulator;
	}

	@Override
	public Serializable getSettingsReference() {
		NetworkSendParams p = (NetworkSendParams) networkSendParams.clone();
		if (p.savePassword == false) {
			p.password = null;
		}
		return p;
	}

	@Override
	public long getSettingsVersion() {
		return NetworkSendParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		networkSendParams = (NetworkSendParams) ((NetworkParams) pamControlledUnitSettings.getSettings()).clone();
		
		
		return (networkSendParams != null);
	}

	/**
	 * @return the networkSendParams
	 */
	public NetworkSendParams getNetworkSendParams() {
		return networkSendParams;
	}


	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			sortDataSources();
			if(client!=null) {
				client.notifyModelChanged(changeType);
			}
			initialisationComplete  = true;
			break;
		case PamController.REMOVE_CONTROLLEDUNIT:
		case PamController.ADD_CONTROLLEDUNIT:
			if (initialisationComplete) {
				sortDataSources();
			}
			break;
		case PamController.CHANGED_PROCESS_SETTINGS:
			//this.client.updateParams(this.getNetworkSendParams());
			//this.client.configureClient();
		}
		
	}

	/**
	 * Called when the network sender has no sending blocks configured and the sender was activated by command line.<br><br>
	 * Intent here is that on the CAB, someone could take a non-network-sending config and select "send data" on the cab, 
	 * and this module would create itself, and configure so that it sends all non-raw data blocks. Then they could download, edit manually, etc.<br><br>
	 * ST 9/27/26 -- This isn't the safest or smartest way to implement this, but I was tasked with simplifying the net send/receive configuration on CAB. 
	 * Could see this leading to unwanted overwhelm of data transmission if there are non-raw but high-rate blocks. 
	 * @param wanted all the blocks in the model
	 * @return totalFormat (it will be binary) 
	 */
	private int defaultPopulateBinarySenders(ArrayList<PamDataBlock> wanted) {
		int totalFormat = 0;
		for (PamDataBlock aBlock:wanted) {
			boolean blockHasBinarySource = aBlock.getBinaryDataSource()!=null;
			boolean blockIsRaw = (aBlock instanceof PamRawDataBlock);
			if(blockHasBinarySource && !blockIsRaw) {
				System.out.println("Adding output data "+aBlock.getLongDataName()+" to the network sender.");
				int fmt = networkSendParams.getSendSelection(aBlock);
				if (fmt > 0) {
					addPamProcess(new NetworkSendProcess(this, aBlock, fmt));
				}
				totalFormat |= fmt;
			}
		}
		return totalFormat;
	}

	private void sortDataSources() {
		ArrayList<PamDataBlock> wanted = PamController.getInstance().getDataBlocks(); // get everything !
		int nProcess = getNumPamProcesses();
		// remove all the old processes. 
		for (int i = nProcess - 1; i >= 1; i--) {
			removePamProcess(getPamProcess(i));
		}
		int totalFormat = 0;
		for (PamDataBlock aBlock:wanted) {
			int fmt = networkSendParams.getSendSelection(aBlock);
			if (fmt > 0) {
				addPamProcess(new NetworkSendProcess(this, aBlock, fmt));
			}
			totalFormat |= fmt;
		}
		
		/*
		 * ST added 9/27/26: added functionality to the CAB to automatically add a network sender to a configuration if user wants to send data
		 * Because sending format is configured block-block, and that makes a lot of sense, still want to give the CAB an easy way to 'get off the ground' 
		 * without too much hastle. If they don't end up wanting this, that makes sense, but giving them the button for now. 
		 */
		if(getNumPamProcesses()==0 && this.activatedByCommand) {
			totalFormat = defaultPopulateBinarySenders(wanted);
		}
		
		// set the command process to use the same format as all of the new processes
		if(this.commandProcess!=null) {
			commandProcess.setOutputFormat(totalFormat);
		}
	}

//	public ArrayList<PamDataBlock> listWantedDataSources() {
//		ArrayList<PamDataBlock> possibles = listPossibleDataSources(networkSendParams.getSendingFormat());
//		ArrayList<PamDataBlock> wants = new ArrayList<PamDataBlock>();
//		for (PamDataBlock aBlock:possibles) {
//			if (networkSendParams.findDataBlock(aBlock) != null) {
//				wants.add(aBlock);
//			}
//		}
//		return wants;
//	}
//	
//	
	public ArrayList<PamDataBlock> listPossibleDataSources(int outputFormat) {
		ArrayList<PamDataBlock> possibles = new ArrayList<PamDataBlock>();
		ArrayList<PamDataBlock> allDataBlocks = PamController.getInstance().getDataBlocks();
		boolean sendBytes = ((outputFormat & NetworkSendParams.NETWORKSEND_BYTEARRAY) != 0);
		boolean sendJson = ((outputFormat & NetworkSendParams.NETWORKSEND_JSON) != 0);
		for (PamDataBlock aBlock:allDataBlocks) {
			
			Boolean hasJson = aBlock.getJSONDataSource() != null;
			Boolean hasBytes = aBlock.getBinaryDataSource() != null;
//			if (hasJson | hasBytes) {
//				System.out.printf("Block %s has JSON: %s, has Binary: %s\n", aBlock.getDataName(), hasJson.toString(), hasBytes.toString());
//			}
			
			// if the data block has a binary source, add it to the list of potential outputs
			if ( (sendJson && hasJson) ||
				 (sendBytes && hasBytes)) {
				possibles.add(aBlock);
			}
			
			// if the data block also has a background manager, add it's data block to the list as well (json-output only for now)
			if (aBlock.getBackgroundManager()!=null) {
				if (sendJson && aBlock.getBackgroundManager().getBackgroundDataBlock().getJSONDataSource() != null) {
					possibles.add(aBlock.getBackgroundManager().getBackgroundDataBlock());
				}
			}
		}
		return possibles;
	}
	
	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#pamClose()
	 */
	@Override
	public void pamClose() {
		super.pamClose();
		if(client!=null) {
			client.close();
		}
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#pamHasStopped()
	 */
	@Override
	public void pamHasStopped() {
		if(client!=null) {
			client.disconnect();
		}
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#pamToStart()
	 */
	@Override
	public void pamToStart() {
		super.pamToStart();
		//ST added 8/13/2026 -- allow configuration to effectively turn off the net send client. 
		//If net sending is enabled then let this run, but otherwise just skip past it.. 
		if(this.networkSendParams.isModuleActivated() && this.client!=null) {
			this.client.configureClient(this.networkSendParams);
			runClient();
		}
	}
	
	public long lastTransmitErrorPrint = 0;

	public void transmitData(NetworkQueuedObject qo) {
		//ST added 8/13/2026 -- allow configuration to effectively turn off the net send client. 
		//Dont do anything if the sender is disabled
		if(this.networkSendParams.isModuleActivated() != true) {
			return;
		}
		if(client==null) {
			System.out.println("Client is null. Likely due to restarting client");
			return;
		}
		try {
			client.sendNetworkQueuedObject(qo);
		} catch (NetTransmitException e) {
			if(System.currentTimeMillis()-this.lastTransmitErrorPrint>1000*60) {
				System.out.println("Could not transmit message. Error: "+e.getMessage());
				lastTransmitErrorPrint = System.currentTimeMillis();
			}
			/*if(client!=null) {
				client.setWarning("Error transmitting data. "+e.getMessage());
			}*/
		}
	}

	public String getStatus() {
		if(this.networkSendParams.isModuleActivated() != true) {
			return "Net Send Manually Disabled";
		}
		if(client==null) {
			return "Disconnected";
		}
		
		return client.getStatus();
	}

	public void runClient() {
		//ST added 8/13/2026 -- allow configuration to effectively turn off the net send client. 
		//Dont do anything if the sender is disabled
		if(this.networkSendParams.isModuleActivated() != true) {
			return;
		}
		if(client==null) {
			this.initializeClient();
		}
		if(client.isConnected()) {
			return;
		}
		try {
			client.connect();
		} catch (ClientConnectFailedException e) {
			System.out.println("Could not connect client to server. Data will exist in buffer until connection is obtained");
		}
	}

	public String executeExternalCommand(String command) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getQueueLength() {
		if(client==null) {
			return -1;
		}
		return client.getQueueLength();
	}

	public int getQueueSize() {
		if(client==null) {
			return -1;
		}
		return client.getQueueSize();
	}

	
	@Override
	public String getModuleSummary(boolean clear, String format) {
		if(format.equals("json")) {
			String jsonString = String.format("{\"active\":%b,", this.networkSendParams.isModuleActivated());
			if(this.networkSendParams.isModuleActivated()) {
				jsonString += String.format("\"connected\":%b,\"serverHost\":\"%s\",\"serverPort\":%d", 
						this.client.isConnected(),
						this.networkSendParams.ipAddress,
						this.networkSendParams.portNumber);
			}
			jsonString += "}";
			return jsonString;
		}
		
		return super.getModuleSummary(clear, format);
	}
	
	
}
