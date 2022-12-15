/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package Acquisition;

import hfDaqCard.SmruDaqSystem;
//import mcc.mccacquisition.MCCDaqSystem;
import mcc.mccacquisition.MCCDaqSystem;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import org.pamguard.x3.sud.Chunk;
import org.pamguard.x3.sud.SudFileListener;

import dataGram.DatagramManager;
import dataMap.OfflineDataMapPoint;
import dataMap.filemaps.OfflineFileServer;
import dataPlotsFX.data.TDDataProviderRegisterFX;
import dataPlotsFX.rawDataPlotFX.RawSoundProviderFX;
import pamScrollSystem.ViewLoadObserver;
import simulatedAcquisition.SimProcess;
//import xarraydaq.XArrayDaq;
import asiojni.ASIOSoundSystem;
import asiojni.NewAsioSoundSystem;
import nidaqdev.NIDAQProcess;
import Acquisition.filedate.FileDate;
import Acquisition.filedate.StandardFileDate;
import Acquisition.filetypes.SoundFileTypes;
import Acquisition.layoutFX.AquisitionGUIFX;
import Acquisition.offlineFuncs.OfflineWavFileServer;
import Acquisition.rona.RonaOfflineFileServer;
import Acquisition.sud.SUDNotificationManager;
import Array.ArrayManager;
import Array.PamArray;
import Array.Preamplifier;
import PamController.OfflineFileDataStore;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitGUI;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamModel.PamModel;
import PamModel.SMRUEnable;
import PamUtils.FrequencyFormat;
import PamUtils.PamCalendar;
import PamUtils.PlatformInfo;
import PamUtils.PlatformInfo.OSType;
import PamView.MenuItemEnabler;
import PamView.PamStatusBar;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import PamView.panel.PamProgressBar;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;

/**
 * Main data acquisition control to get audio data from sound cards,
 * NI cards (via UDP), files, directories of files, etc.
 * <p>
 * Uses a plug in architecture to allow new types to be added. This
 * is done through RegisterDaqType().
 * 
 * @author Doug Gillespie
 * 
 * @see Acquisition.DaqSystem
 *
 */
public class AcquisitionControl extends PamControlledUnit implements PamSettings, OfflineFileDataStore {

	protected ArrayList<DaqSystem> systemList;

	public AcquisitionParameters acquisitionParameters = new AcquisitionParameters();

	protected AcquisitionProcess acquisitionProcess;

	protected MenuItemEnabler daqMenuEnabler;

	protected OfflineFileServer offlineFileServer;

	private PamLabel statusBarText;

	private PamProgressBar levelBar;

	public static final String unitType = "Data Acquisition";

	protected AcquisitionControl acquisitionControl;

	protected Component statusBarComponent;

	protected PamController pamController;

	protected DAQChannelListManager daqChannelListManager;

	protected FolderInputSystem folderSystem;

	private DCL5System dclSystem;
	
	protected FileDate fileDate;
	
	/**
	 * The JavaFX GUI for the sound acquisition module. 
	 */
	private AquisitionGUIFX aquisitionGUIFX;
	
	private SUDNotificationManager sudNotificationManager;
	
	protected SoundFileTypes soundFileTypes;
	
	/**
	 * Main control unit for audio data acquisition.
	 * <p>
	 * It is possible to instantiate several instances of this, preferably
	 * with different names to simultaneously acquire sound from a number of
	 * sources such as multiple sound cards, fast ADC boards, etc. 
	 * <p>
	 * Each different acquisition device must implement the DaqSystem interface 
	 * and register with each AcquisitionControl.
	 * @param name name of the Acquisition control that will appear in menus. These should be
	 * different for each instance of AcquistionControl since the names are used by PamProcesses
	 * to find the correct data blocks.
	 * @see DaqSystem
	 */
	public AcquisitionControl(String name) {

		super(unitType, name);

		acquisitionControl = this;
		
		fileDate = new StandardFileDate(this);

		pamController = PamController.getInstance();
		
		soundFileTypes = new SoundFileTypes(this);
		
		registerDaqSystem(new SoundCardSystem(this));
		if (PlatformInfo.calculateOS() == OSType.WINDOWS) {
			registerDaqSystem(new ASIOSoundSystem(this));
			registerDaqSystem(new NewAsioSoundSystem(this));
		}
		if(PamController.getInstance().getRunMode()!=PamController.RUN_NOTHING){
			registerDaqSystem(new FileInputSystem(this));
			registerDaqSystem(folderSystem = new FolderInputSystem(this));
		}
//		registerDaqSystem(dclSystem = new DCL5System());
		registerDaqSystem(new NIDAQProcess(this));
		registerDaqSystem(new SmruDaqSystem(this));
		registerDaqSystem(new SimProcess(this));
//		registerDaqSystem(new XArrayDaq(this));
		if (SMRUEnable.isEnable()) {
//			registerDaqSystem(new icListenSystem());
//			registerDaqSystem(new NINetworkDaq(this));
			registerDaqSystem(new MCCDaqSystem(this));
//			registerDaqSystem(new RonaInputSystem(this));
		}
		
		// load the DAQ Systems found in the plugins folder
		loadExternalDaqSystems();

		daqChannelListManager = new DAQChannelListManager(this);

		PamSettingManager.getInstance().registerSettings(this);

		addPamProcess(acquisitionProcess = new AcquisitionProcess(this));

		daqMenuEnabler = new MenuItemEnabler();

		statusBarComponent = getStatusBarComponent();

		if (isViewer) {
			if (this.acquisitionParameters.daqSystemType.equals(RonaInputSystem.systemType)) {
				offlineFileServer = new RonaOfflineFileServer(this, fileDate);
			}
			else {
				offlineFileServer = new OfflineWavFileServer(this, fileDate);
			}
		}
		else {
			PamStatusBar statusBar = PamStatusBar.getStatusBar();

			if (statusBar != null && statusBarComponent != null) {
				statusBar.add(statusBarComponent);
				setupStatusBar();
			}
		}
		setSelectedSystem();
		
		TDDataProviderRegisterFX.getInstance().registerDataInfo(new RawSoundProviderFX(this));

	}
	public AcquisitionControl(String name, boolean isSimulator) {

		super(unitType, name);

		acquisitionControl = this;

	}
	
	/**
	 * Overloaded constructor - used by the STAcquisitionControl class as a way to call
	 * the PamController.PamControlledUnit constructor without all of the AcquisitionControl
	 * code above
	 * 
	 * @param type the type of unit
	 * @param name the name of the unit
	 */
	public AcquisitionControl(String type, String name) {
		super(type, name);
	}
	

	private PamPanel systemPanel;
	
	protected Component getStatusBarComponent() {
		PamPanel p = new PamPanel();
		p.add(statusBarText = new PamLabel());
		p.add(levelBar = new PamProgressBar(-60, 0));
		p.add(systemPanel = new PamPanel());
		levelBar.setOrientation(SwingConstants.HORIZONTAL);
		fillStatusBarText();
		levelBar.setValue(-60);
		return p;
	}

	/**
	 * Registered new DAQ systems and makes them available via the AcquisitionCialog
	 * @param daqSystem
	 */
	public void registerDaqSystem(DaqSystem daqSystem){
		if (systemList == null) {
			systemList = new ArrayList<DaqSystem>();
		}
		systemList.add(daqSystem);
		//daqSystem.getItemsList();
		fillStatusBarText();
	}

	public static ArrayList<AcquisitionControl> getControllers() {
		ArrayList<AcquisitionControl> daqControllers = new ArrayList<AcquisitionControl>();
		PamControlledUnit pcu;
		for (int i = 0; i < PamController.getInstance().getNumControlledUnits(); i++) {
			pcu = PamController.getInstance().getControlledUnit(i);
			if (pcu.getUnitType().equals(unitType)) {
				daqControllers.add((AcquisitionControl) pcu);
			}
		}
		return daqControllers;
	}

	public AcquisitionProcess getDaqProcess() {
		return acquisitionProcess;
	}

	public void setDaqProcess(AcquisitionProcess acquisitionProcess) {
		this.acquisitionProcess = acquisitionProcess;
	}

	/**
	 * Finds a reference to a given DAQ system based on it's type (e.g.  sound card, file, etc.
	 * @param systemType
	 * @return reference to a DaqSystem object 
	 */
	public DaqSystem findDaqSystem(String systemType) {

		if (systemList == null) return null;

		if (systemType == null) systemType = acquisitionParameters.daqSystemType;

		for (int i = 0; i < systemList.size(); i++) {
			if (systemList.get(i).getSystemType().equals(systemType)) return systemList.get(i);
		}

		return null;
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem daqMenu;
		daqMenu = new JMenuItem(getUnitName() + " ...");
		daqMenu.addActionListener(new acquisitionSettings(parentFrame));
		daqMenuEnabler.addMenuItem(daqMenu);
		return daqMenu;
	}


	class acquisitionSettings implements ActionListener {
		Frame parentFrame;

		public acquisitionSettings(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		public void actionPerformed(ActionEvent e) {
			AcquisitionParameters newParameters = AcquisitionDialog.showDialog(parentFrame, acquisitionControl, acquisitionParameters);
			if (newParameters != null) {
				acquisitionParameters = newParameters.clone();
				setSelectedSystem();
				checkArrayChannels(parentFrame);
				acquisitionProcess.setupDataBlock();
				setupStatusBar();
				fillStatusBarText();
				PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);
				if (isViewer) {
					offlineFileServer.createOfflineDataMap(parentFrame);
				}
			}
		}
	}

	private DaqSystem lastSelSystem = null;
	public void setSelectedSystem() {
		DaqSystem selSystem = findDaqSystem(null);
		if (selSystem == lastSelSystem) {
			return;
		}
		if (lastSelSystem != null) {
			lastSelSystem.setSelected(false);
		}
		if (selSystem != null) {
			selSystem.setSelected(true);
		}
		lastSelSystem = selSystem;
	}
	
	
	/**
	 * Check array channels have corresponding hydrophones in the array manager. Does 
	 * not open a dialog to warn or fix. 
	 * @return check array channel. False if array manager 
	 */
	public boolean checkArrayChannels() {
		int error = arrayChannelsOK();
		if (error == ARRAY_ERROR_OK) {
			return true;
		}
		else return false; 
	}		


	/**
	 * Run a check to see that all read out channels are connected to 
	 * a hydrophone and if not, do something about it. 
	 * @return true if OK, or problem resolved. 
	 */
	public boolean checkArrayChannels(Frame parentFrame) {		

		int error = arrayChannelsOK();
		if (error == ARRAY_ERROR_OK) {
			return true;
		}
		String message = new String("Do you want to go to the array manager now to rectify this problem ?");
		message += "\n\nFailure to do so may result in PAMGUARD crashing or features not working correctly";
		int ans = JOptionPane.showConfirmDialog(parentFrame, message, getArrayErrorMessage(error), JOptionPane.YES_NO_OPTION);
		if (ans == JOptionPane.YES_OPTION) {
			ArrayManager.getArrayManager().showArrayDialog(getPamView().getGuiFrame());
			return checkArrayChannels(parentFrame);
		}

		return false;
	}

	private final int ARRAY_ERROR_OK = 0;
	private final int ARRAY_ERROR_NO_ARRAYMANAGER = 1;
	private final int ARRAY_ERROR_NO_ARRAY = 2;
	private final int ARRAY_ERROR_NOT_ENOUGH_HYDROPHONES = 3;

	private int arrayChannelsOK() {
		ArrayManager am = ArrayManager.getArrayManager();
		if (am == null) {
			return ARRAY_ERROR_NO_ARRAY;
		}
		PamArray pamArray  = am.getCurrentArray();
		if (pamArray == null) {
			return ARRAY_ERROR_NO_ARRAYMANAGER;
		}
		int nChannels = acquisitionParameters.nChannels;
		int[] hydrophoneList = acquisitionParameters.getHydrophoneList();
		if (hydrophoneList == null || hydrophoneList.length < nChannels) {
			return ARRAY_ERROR_NOT_ENOUGH_HYDROPHONES;
		}
		for (int iChan = 0; iChan < nChannels; iChan++) {
			if (hydrophoneList[iChan] >= pamArray.getHydrophoneCount()) {
				return ARRAY_ERROR_NOT_ENOUGH_HYDROPHONES;
			}
		}

		return ARRAY_ERROR_OK;
	}

	String getArrayErrorMessage(int error) {
		switch(error) {
		case ARRAY_ERROR_OK:
			return "Array Ok";
		case ARRAY_ERROR_NO_ARRAYMANAGER:
			return "There is no array manager within the system";
		case ARRAY_ERROR_NO_ARRAY:
			return "No PAMGUARD array has been found in the system";
		case ARRAY_ERROR_NOT_ENOUGH_HYDROPHONES:
			return "There are not enough hydrophones in the PAMGUARD array to support this number of channels";
		}
		return null;
	}

	void fillStatusBarText() {
		if (statusBarText != null){
			statusBarText.setText(getStatusBarText());
		}
	}

	@Override
	public String toString() {
		// write current status into the status bar
		String string;

		DaqSystem daqSystem = findDaqSystem(null);
		if (daqSystem == null) {
			string = new String("No Data Acquisition Configured");
			return string;
		}

		String daqName = daqSystem.getSystemName();
		if (daqName == null) daqName = new String("");

		string = String.format("%s %s %.1fkHz, %d channels, %d bit", daqSystem.getSystemType(), daqName,
				acquisitionParameters.sampleRate / 1000., acquisitionParameters.nChannels, daqSystem.getSampleBits());
		//		System.out.println("Set status text to " + string);

		return string;
	}

	/**
	 * Sets a level meter on the status bar
	 * @param peakValue Maximum amplitude fom AcquisitionProcess
	 */
	public void setStatusBarLevel(double peakValue) {
		// convert to dB, status bar is set on a scale of -60 to 0
		double dB = 20. * Math.log10(peakValue);
		levelBar.setValue((int) dB);
	}

	protected void setupStatusBar() {
		if (systemPanel == null) return;
		systemPanel.removeAll();
		DaqSystem daqSys = findDaqSystem(null);
		if (daqSys == null) return;
		Component specialComponent = daqSys.getStatusBarComponent();
		if (specialComponent != null) {
			systemPanel.add(specialComponent);
		}
	}
	/**
	 * Prepares text for the status bar
	 * @return text in the status bar
	 */
	private String getStatusBarText() {

		String statusString = toString();

		if (acquisitionProcess.getRunningSystem() == null) {
			statusString += " : Idle";
		}
		else {
			statusString += String.format(" : Running, buffer %3.1fs", acquisitionProcess.getBufferSeconds());
		}
		return statusString;
	}

	/*
	 *  (non-Javadoc)
	 * @see PamController.PamSettings#GetSettingsReference()
	 */
	public Serializable getSettingsReference() {
		return acquisitionParameters;
	}

	public long getSettingsVersion() {
		return AcquisitionParameters.serialVersionUID;
	}

	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {

		//		if (PamSettingManager.getInstance().isSettingsUnit(this, pamControlledUnitSettings)) {
		try {
			acquisitionParameters = ((AcquisitionParameters) pamControlledUnitSettings.getSettings()).clone();

			if (acquisitionProcess != null) {
				acquisitionProcess.setSampleRate(acquisitionParameters.sampleRate, true);
			}
			setupStatusBar();

			fillStatusBarText();

			return true;
		}
		catch (ClassCastException e) {
			e.printStackTrace();
		}
		return false;
	}


	/* (non-Javadoc)
	 * @see PamguardMVC.PamControlledUnit#SetupControlledUnit()
	 */
	@Override
	public void setupControlledUnit() {
		super.setupControlledUnit();
		fillStatusBarText();
	}

	// converts a list of ADC channels to a list of hydrophones
	public int ChannelsToHydrophones(int channels) {
		int[] hydrophoneList = getHydrophoneList();
		if (hydrophoneList == null) return channels; // they are the same by default
		//In viewer mode sometimes the number of channels in sound aquisition can be lost i.e. 
		//there are 0 channels. This causes bugs so if occurs return standard list.  
		if (acquisitionParameters.nChannels==0) return channels;
		
		int hydrophones = 0;
		int channelListIndex;
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			channelListIndex = acquisitionParameters.getChannelListIndexes(i);
			if ((1<<i & channels) != 0 && channelListIndex >= 0) {
				hydrophones |= 1<<hydrophoneList[channelListIndex];
			}
		}
		return hydrophones;
	}

	/**
	 * Return a list of which channels are connected to which hydrophones in 
	 * the currentarray. 
	 * @return List of hydrophone numbers.
	 */
	public int[] getHydrophoneList() {
		return acquisitionParameters.getHydrophoneList();
	}

	/**
	 * Sets the list of hydrophone numbers.
	 * @param hydrophoneList List of hydrophone numbers in channel order
	 */
	public void setHydrophoneList(int[] hydrophoneList) {
		acquisitionParameters.setHydrophoneList(hydrophoneList);
	}
	/**
	 * 
	 * finds the ADC channel for a given hydrophone. 
	 * Will return -1 if no ADC channel uses this hydrophone
	 * 
	 * @param hydrophoneId Number of a hydrophone in a PamArray
	 * @return the ADC channel for the given hydrophone
	 */
	public int findHydrophoneChannel(int hydrophoneId) {
		// finds the ADC channel for a given hydrophone. 
		// will return -1 if no ADC channel uses this hydrophone
		// if no list, assume 1-1 mapping
		int channelList[] = acquisitionControl.acquisitionParameters.getHardwareChannelList();
		if (acquisitionParameters.getHydrophoneList() == null){
			if (hydrophoneId < acquisitionParameters.nChannels) {
				return hydrophoneId;
			}
			else return -1;
		}
		int channelNumber;
		for (int i = 0; i < acquisitionParameters.getHydrophoneList().length; i++){
			channelNumber = acquisitionParameters.getChannelList(i);
			if (acquisitionParameters.getHydrophone(channelNumber) == hydrophoneId){
				return channelNumber;
			}
		}
		return -1;
	}

	// Dajo, PR,
	// Method: find the phone connected to a given channel
	public int getChannelHydrophone(int channel) {

		//		int channelIndex = this.acquisitionParameters.getChannelListIndexes(channel);
		int phone = acquisitionParameters.getHydrophone(channel);

		return phone;
	}

	public double getPeak2PeakVoltage(int swChannel) {
		return acquisitionProcess.getPeak2PeakVoltage(swChannel);
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#removeUnit()
	 */
	@Override
	public boolean removeUnit() {

		boolean ans = super.removeUnit();

		PamStatusBar statusBar = PamStatusBar.getStatusBar();

		if (statusBar != null) {
			statusBar.remove(statusBarComponent);
			//			statusBar.getToolBar().invalidate();
			statusBar.getToolBar().repaint();
		}

		return ans;
	}

	/**
	 * Getter for acquisition parameters.
	 * @return data acquisition parameters. 
	 */
	public AcquisitionParameters getAcquisitionParameters() {
		return acquisitionParameters;
	}

	public AcquisitionProcess getAcquisitionProcess() {
		return acquisitionProcess;
	}
	public DAQChannelListManager getDaqChannelListManager() {
		return daqChannelListManager;
	}
	/**
	 * @return the offlineFileServer
	 */
	public OfflineFileServer getOfflineFileServer() {
		return offlineFileServer;
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch(changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			if (isViewer) {
				offlineFileServer.createOfflineDataMap(PamController.getMainFrame());
			}
		}
		if (lastSelSystem != null) {
			lastSelSystem.notifyModelChanged(changeType);
		}
	}

	//	/**
//	 * @return the folderSystem
//	 */
//	public FolderInputSystem getFolderSystem() {
//		if (findDaqSystem(null) == dclSystem) {
//			return dclSystem;
//		}
//		else {
//			return folderSystem;
//		}
//	}

	public FolderInputSystem getFolderSystem() {
		return folderSystem;
	}

	@Override
	public void createOfflineDataMap(Window parentFrame) {
		offlineFileServer.createOfflineDataMap(parentFrame);
	}
	@Override
	public String getDataSourceName() {
		if (offlineFileServer == null) {
			return null;
		}
		return offlineFileServer.getDataSourceName();
	}
	@Override
	public boolean loadData(PamDataBlock dataBlock, OfflineDataLoadInfo offlineLoadDataInfo, ViewLoadObserver loadObserver) {
		return offlineFileServer.loadData(dataBlock, offlineLoadDataInfo, loadObserver);
	}
	@Override
	public boolean saveData(PamDataBlock dataBlock) {
		return offlineFileServer.saveData(dataBlock);
	}
	@Override
	public PamRawDataBlock getRawDataBlock() {
		return acquisitionProcess.getRawDataBlock();
	}

	@Override
	public PamProcess getParentProcess() {
		return acquisitionProcess;
	}

	public boolean isStalled() {
		return acquisitionProcess.isStalled();
	}

	@Override
	public boolean rewriteIndexFile(PamDataBlock dataBlock,
			OfflineDataMapPoint dmp) {
		return false;
	}
	@Override
	public DatagramManager getDatagramManager() {
		// TODO Auto-generated method stub
		return null;
	}
	public FileDate getFileDate() {
		return fileDate;
	}
	
	/**
	 * Load any classes that were found in the plugins folder and implement the DaqSystemInterface interface
	 */
	public void loadExternalDaqSystems() {
		
		// get a list of plugins 
		List<DaqSystemInterface> daqList = ((PamModel) PamController.getInstance().getModelInterface()).getDaqList();
		
		// if there are no plugins, return
		if (daqList.isEmpty()) {
			return;
		}
		for (DaqSystemInterface dsi : daqList ) {
			registerDaqSystem(dsi.createDAQControl(this));
		}
	}
	
	/**
	 * Get the available DAQ systems
	 * @return
	 */
	public ArrayList<DaqSystem> getSystemList() {
		return this.systemList;
	}
	
	/**
	 * Set the acquisition parameters. 
	 * @param params - the acquisition params to set. 
	 */
	public void setAquisitionParams(AcquisitionParameters params) {
		this.acquisitionParameters=params;
		
	}
	
	@Override
	public PamControlledUnitGUI getGUI(int flag) {
		if (flag==PamGUIManager.FX) {
			if (aquisitionGUIFX ==null) {
				aquisitionGUIFX= new AquisitionGUIFX(this);
			}
			return aquisitionGUIFX;
		}
		//TODO swing
		return null;
	}
	
	/**
	 * Get a summary of the daq settings for the QA module. 
	 * @return summary of DAQ settings. 
	 */
	public String getDaqSummary() {
		DaqSystem daqSys = findDaqSystem(null);
		if (daqSys == null) {
			return String.format("%s - currently unavailable", acquisitionParameters.daqSystemType);
		}
		String str =  String.format("%s - %s, %3.1fVp-p", 
				acquisitionParameters.daqSystemType, daqSys.getDeviceName(), 
				acquisitionParameters.voltsPeak2Peak);
		Preamplifier preamp = acquisitionParameters.getPreamplifier();
		if (preamp != null) {
			str += String.format(", %3.1fdB gain", preamp.getGain());
		}
		str += ", Sample Rate " + FrequencyFormat.formatFrequency(acquisitionParameters.sampleRate, true);
		return str;
	}
	
	@Override
	public String tellModule(String command) {
		/**
		 * Get timing summary and return as a string. 
		 */
		switch (command) {
		case "gettimeinfo":
			return getTimeInfoString();
		}
		return super.tellModule(command);
	}
	
	
	/**
	 * Get a summary of time information as to what's going on in the DAQ
	 * @return time summary
	 */
	private String getTimeInfoString() {
		/*
		 * 
		sprintf(returned,"%s:%lld,%lld,%ld,%lld",getModuleName(),calendar->getRawStartTime(),
				calendar->getMillisecondTime(),(int)daqProcess->getSampleRate(),acquiredSamples);
		 */
		return String.format("%s:%d,%d,%d,%d", getUnitName(), PamCalendar.getSessionStartTime(), PamCalendar.getTimeInMillis(), 
				(int) acquisitionProcess.getSampleRate(), acquisitionProcess.getTotalSamples(0));
	}
	
	@Override
	public void pamHasStopped() {
		acquisitionProcess.pamHasStopped();
	}
	
	@Override
	public String getModuleSummary(boolean clear) {
		return getDaqProcess().getRawDataBlock().getSummaryString(clear);
	}
	
	/**
	 * Get the SUD processing notification manager. 
	 * @return SUD processing notification manager. 
	 */
	public SUDNotificationManager getSUDNotificationManager() {
		if (sudNotificationManager == null) {
			sudNotificationManager = new SUDNotificationManager();
		}
		return sudNotificationManager;
	}
	
}
