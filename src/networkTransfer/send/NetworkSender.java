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
	 * These two left in since they are used in the BathProcessing plugin. 
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
	
	public NetworkSender(String unitName) {
		super("Network Sender", unitName);
		if(this.networkSendParams.sendingFormat==NetworkSendParams.NETWORKSEND_BYTEARRAY) {
			commandProcess = new NetworkSendProcess(this, null,NetworkSendParams.NETWORKSEND_BYTEARRAY);
			commandProcess.setCommandProcess(true);
			addPamProcess(commandProcess);
		}
		PamSettingManager.getInstance().registerSettings(this);
		sidePanel = new NetworkSendSidePanel(this);
		initializeClient();
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
		this.client.close();
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
		
		boolean isSetJson = networkSendParams.sendingFormat == NetworkSendParams.NETWORKSEND_JSON;
		if(useJson!=null) {
			isSetJson = Boolean.valueOf(useJson);
		}
		
		if(isSetJson) {
			networkSendParams.sendingFormat = NetworkSendParams.NETWORKSEND_JSON;
		}else {
			networkSendParams.sendingFormat = NetworkSendParams.NETWORKSEND_BYTEARRAY;
		}
		
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

	

	private void sortDataSources() {
		ArrayList<PamDataBlock> wanted = listWantedDataSources();
		int nProcess = getNumPamProcesses();
		for (int i = nProcess - 1; i >= 1; i--) {
			removePamProcess(getPamProcess(i));
		}
		for (PamDataBlock aBlock:wanted) {
			addPamProcess(new NetworkSendProcess(this, aBlock,networkSendParams.sendingFormat));

		}
		
		// set the command process to use the same format as all of the new processes
		if(this.commandProcess!=null) {
			commandProcess.setOutputFormat(networkSendParams.sendingFormat);
		}
	}

	public ArrayList<PamDataBlock> listWantedDataSources() {
		ArrayList<PamDataBlock> possibles = listPossibleDataSources(networkSendParams.sendingFormat);
		ArrayList<PamDataBlock> wants = new ArrayList<PamDataBlock>();
		for (PamDataBlock aBlock:possibles) {
			if (networkSendParams.findDataBlock(aBlock) != null) {
				wants.add(aBlock);
			}
		}
		return wants;
	}
	
	
	public ArrayList<PamDataBlock> listPossibleDataSources(int outputFormat) {
		ArrayList<PamDataBlock> possibles = new ArrayList<PamDataBlock>();
		ArrayList<PamDataBlock> allDataBlocks = PamController.getInstance().getDataBlocks();
		for (PamDataBlock aBlock:allDataBlocks) {
			
			Boolean hasJson = aBlock.getJSONDataSource() != null;
			Boolean hasBytes = aBlock.getBinaryDataSource() != null;
			if (hasJson | hasBytes) {
				System.out.printf("Block %s has JSON: %s, has Binary: %s\n", aBlock.getDataName(), hasJson.toString(), hasBytes.toString());
			}
			
			// if the data block has a binary source, add it to the list of potential outputs
			if ( (outputFormat == NetworkSendParams.NETWORKSEND_BYTEARRAY && aBlock.getBinaryDataSource() != null) ||
				 (outputFormat == NetworkSendParams.NETWORKSEND_JSON && aBlock.getJSONDataSource() != null)) {
				possibles.add(aBlock);
			}
			
			// if the data block also has a background manager, add it's data block to the list as well (json-output only for now)
			if (aBlock.getBackgroundManager()!=null) {
				if (outputFormat == NetworkSendParams.NETWORKSEND_JSON && aBlock.getBackgroundManager().getBackgroundDataBlock().getJSONDataSource() != null) {
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
		this.client.configureClient(this.networkSendParams);
		runClient();
		
	}
	
	public long lastTransmitErrorPrint = 0;

	public void transmitData(NetworkQueuedObject qo) {
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
		if(client==null) {
			return "Disconnected";
		}
		return client.getStatus();
	}

	public void runClient() {
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

	
	
}
