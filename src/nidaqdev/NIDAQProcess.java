package nidaqdev;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import soundPlayback.FilePlayback;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackSystem;
import soundPlayback.SoundCardPlayback;


import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionDialog;
import Acquisition.ChannelListPanel;
import Acquisition.DaqSystem;
import Acquisition.AudioDataQueue;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamConstants;

public class NIDAQProcess extends DaqSystem implements PamSettings {

	private JPanel daqDialog;
	
	private JComboBox audioDevices;
	
	private JComboBox inputType;
	
	private JLabel warningText;
		
	private JCheckBox allowMultiBoard;
	
	private NIDaqParams niParameters = new NIDaqParams(sysType);
	
	private AcquisitionControl daqControl;
	
	private AcquisitionDialog acquisitionDialog;
			
	private int rawBufferSize;
	
	private int daqChannels;
	
	private Nidaq nidaq;
	
	private long[] channelSampleCount;
	
	private AudioDataQueue newDataUnits;
	
	private ArrayList<NIDeviceInfo> niDevices;
	
	private NIChannelListPanel niChannelListPanel;

	private int dataUnitSamples;

	private PlaybackSystem playBackSystem = null;
	
	public NIDAQProcess (AcquisitionControl daqControl) {
		this.daqControl = daqControl;
//		setSettingsUnitName();
		nidaq = new Nidaq();
		niDevices = nidaq.getDevicesList();
		PamSettingManager.getInstance().registerSettings(this);
//		niChannelListPanel = new NIChannelListPanel(this, daqControl);
//		playBackSystem = new NIPlaybackSystem(this);
	}
	
	@Override
	public boolean prepareSystem(AcquisitionControl daqControl) {
		this.daqControl = daqControl;
		
		if (niParameters.deviceNumber >= niDevices.size()) {
			return false;
		}
		
		NIDeviceInfo deviceInfo = niDevices.get(niParameters.deviceNumber);
				
		// keep a reference to where data will be put.
		this.newDataUnits = daqControl.getDaqProcess().getNewDataQueue();
		if (this.newDataUnits == null) return false;
		
		daqChannels = daqControl.acquisitionParameters.nChannels;
		int sampleRate = (int) daqControl.acquisitionParameters.sampleRate;	

		dataUnitSamples = sampleRate / 10;
//		dataUnitSamples = Math.max(dataUnitSamples, 1000);
		
		nidaq.setTerminalConfig(niParameters.terminalConfiguration);
		
		/*
		 * This is a list starting from 0, which will
		 * not be the same as the actual names !
		 */
		int[] deviceList = getDeviceList().clone();
		NIDeviceInfo di;
		for (int i = 0; i < deviceList.length; i++) {
			di = niDevices.get(deviceList[i]);
			if (di != null) {
				deviceList[i] = di.getDevNumber();
			}
			else {
				System.out.println(String.format("NI Error: Unable to find device information dev %d", i));
			}
		}
		
		// need to make channel list of correct length, since niDaq
		// sets number of channels based on the length of this array. 
		int[] allChannelList = niParameters.getHwChannelList();
		int[] channelList = new int[daqChannels];
		/*
		 * need to add code here to initialise card, channel lists, etc.
		 * For now, this will be hard wired. 
		 */
		double[] rangesLo = new double[daqChannels];
		double[] rangesHi = new double[daqChannels];
		double[] range;
		for (int i = 0; i < daqChannels; i++) {
			channelList[i] = allChannelList[i];
			range = niParameters.getAIRange(i);
			rangesLo[i] = range[0];
			rangesHi[i] = range[1];
		}
		
		nidaq.prepareDAQ(deviceInfo.getDevNumber(), sampleRate, channelList, rangesLo, rangesHi, deviceList);

		channelSampleCount = new long[PamConstants.MAX_CHANNELS];
		
		newDataUnits = daqControl.getAcquisitionProcess().getNewDataQueue();
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see Acquisition.DaqSystem#getDataUnitSamples()
	 */
	@Override
	public int getDataUnitSamples() {
		return dataUnitSamples;
	}

	@Override
	public boolean startSystem(AcquisitionControl daqControl) {	
		/**
		 * Only one NI daq system can be running real time data. 
		 * Check that nothing else has claimed this honour !
		 */
		NIDAQProcess existingProcess = Nidaq.getNiDaqProcess();
		if (existingProcess != null && existingProcess != this) {
			System.out.println("*** Error ! More than one process is attempting to read NI cards. This is not supportd ***");
			return false;
		}
		Nidaq.setNiDaqProcess(this);
		try {
			Thread captureThread = new Thread(new NICaptureThread());
			captureThread.start();
			
		} catch (Exception Ex) {
			Ex.printStackTrace();
			return false;
		}
		setStreamStatus(STREAM_RUNNING);
		return true;	
	}
	
	
	@Override
	public void stopSystem(AcquisitionControl daqControl) {
		nidaq.stopDAQ();
		Nidaq.setNiDaqProcess(null);
	}
	
	@Override
	public ChannelListPanel getDaqSpecificChannelListPanel(
			AcquisitionDialog acquisitionDialog) {
		return getNiChannelListPanel();
	}
	
	

	@Override
	public JPanel getDaqSpecificDialogComponent(AcquisitionDialog acquisitionDialog) {
		this.acquisitionDialog = acquisitionDialog;
		if (daqDialog == null) {
			daqDialog = createDaqDialogPanel();
		}		
		return daqDialog;		
	}
	
	public void setVP2P(double vp2p) {
		if (acquisitionDialog != null) {
			acquisitionDialog.setVPeak2Peak(vp2p);
		}
	}
	
	private JPanel createDaqDialogPanel() {		
		JPanel p = new JPanel();
		
		p.setBorder(new TitledBorder("Select NI Device"));
		
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
			

		PamDialog.addComponent(p, new JLabel("Master Device"), c);
		c.gridx++;
		PamDialog.addComponent(p, audioDevices = new JComboBox(), c);
		audioDevices.addActionListener(new SelectBoard());
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		PamDialog.addComponent(p, warningText = new JLabel(" ", SwingConstants.CENTER),	c);
		c.gridy++;
		c.gridx=0;
		c.gridwidth = 2;
		PamDialog.addComponent(p, allowMultiBoard = new JCheckBox("Use multiple DAQ boards"), c);
		allowMultiBoard.addActionListener(new AllowMultiBoard());
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		PamDialog.addComponent(p, new JLabel("Terminal Config"), c);
		c.gridx++;
		PamDialog.addComponent(p, inputType = new JComboBox(), c);
//		c.gridy++;
//		c.gridx = 0;
//		PamDialog.addComponent(p, new JLabel("Range"), c);
//		c.gridx++;
//		PamDialog.addComponent(p, rangeList = new JComboBox(), c);
//		double range; 
//		String rangeString;
//		for (int i = 0; i < niRanges.length; i++) {
//			range = niRanges[i];
//			if (range < 1) {
//				rangeString = String.format("+/- %d mV", (int) (range*1000));
//			}
//			else {
//				rangeString = String.format("+/- %d V", (int) range);
//			}
//			rangeList.addItem(rangeString);
//		}
//		rangeList.addActionListener(new RangeAction());
//		p.add(BorderLayout.CENTER, audioDevices = new JComboBox());
		
		JPanel q = new JPanel(new BorderLayout());
		q.add(BorderLayout.CENTER, p);
		return q;
	}
	
	public NIDaqParams getNiParameters() {
		return niParameters;
	}

	@Override
	public void dialogSetParams() {
		// do a quick check to see if the system type is stored in the parameters.  This field was added
		// to the SoundCardParameters class on 23/11/2020, so any psfx created before this time
		// would hold a null.  The system type is used by the getParameterSet method to decide
		// whether or not to include the parameters in the XML output
		if (niParameters.systemType==null) niParameters.systemType=getSystemType();
		
			
		audioDevices.removeAllItems();
		for (int i = 0; i < niDevices.size(); i++) {
//			if (niDevices.get(i).isExists()) {
				audioDevices.addItem(niDevices.get(i));
//			}
		}
		
		if (niParameters.deviceNumber < niDevices.size()) {
			audioDevices.setSelectedItem(niDevices.get(niParameters.deviceNumber));
//			audioDevices.setSelectedIndex(niParameters.deviceNumber);
		}
		
		inputType.removeAllItems();
		inputType.addItem("Referenced single ended");
		inputType.addItem("Non-Referenced single ended");
		inputType.addItem("Differential");
		inputType.addItem("Pseudo Differential");
		switch (niParameters.terminalConfiguration) {
		case NIConstants.DAQmx_Val_RSE:
			inputType.setSelectedIndex(0);
			break;
		case NIConstants.DAQmx_Val_NRSE:
			inputType.setSelectedIndex(1);
			break;
		case NIConstants.DAQmx_Val_Diff:
			inputType.setSelectedIndex(2);
			break;
		case NIConstants.DAQmx_Val_PseudoDiff:
			inputType.setSelectedIndex(3);
			break;
		}
		
//		rangeList.setSelectedIndex(niParameters.rangeIndex);
		
		allowMultiBoard.setSelected(niParameters.enableMultiBoard);
		
		enableMultiBoardOps();
		
		enableMasterDevice();
	}
	
	/**
	 * Get the index of the master device. 
	 * @return
	 */
	public int getMasterDevice() {
		return audioDevices.getSelectedIndex();
	}
	
	public NIDeviceInfo getDeviceInfo(int iDevice) {
		if (niDevices == null || iDevice < 0 || iDevice >= niDevices.size()) {
			return null;
		}
		return niDevices.get(iDevice);
	}
	
	@Override
	public boolean dialogGetParams() {
		niParameters.deviceNumber = audioDevices.getSelectedIndex();
		
		if (niParameters.deviceNumber < 0) {
			return false;
		}

		switch (inputType.getSelectedIndex()) {
		case 0:
			niParameters.terminalConfiguration = NIConstants.DAQmx_Val_RSE;
			break;
		case 1:
			niParameters.terminalConfiguration = NIConstants.DAQmx_Val_NRSE;
			break;
		case 2:
			niParameters.terminalConfiguration = NIConstants.DAQmx_Val_Diff;
			break;
		case 3:
			niParameters.terminalConfiguration = NIConstants.DAQmx_Val_PseudoDiff;
			break;
		default:
			niParameters.terminalConfiguration = NIConstants.DAQmx_Val_NRSE;
				
		}
		
//		niParameters.rangeIndex = rangeList.getSelectedIndex();
		
		niParameters.enableMultiBoard = isEnabledMultiBoardOps();
		
		return true;
	}
	
	
//
//	public double getNIRange() {
//		return getNIRange(niParameters.rangeIndex);
//	}
//	
//	public double getNIRange(int rangeIndex) {
//		return niRanges[rangeIndex];
//	}
	
	public static final String sysType = "National Instruments DAQ Cards";
	@Override
	public String getSystemType() {
		return sysType;
	}
	
	@Override
	public String getSystemName() {
		// return the name of the sound card.
		ArrayList<NIDeviceInfo> devices = nidaq.getDevicesList();
		if (devices == null || niParameters.deviceNumber < 0 ||
				devices.size() <= niParameters.deviceNumber) {
			return new String("No NI card");
		}
		else {
			return devices.get(niParameters.deviceNumber).getName();
		}
	}
		

	@Override
	public boolean canPlayBack(float sampleRate) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public PlaybackSystem getPlaybackSystem(PlaybackControl playbackControl, DaqSystem daqSystem) {
		if (playBackSystem == null) {
//			PlaybackControl pc = (PlaybackControl) PamController.getInstance().findControlledUnit("Sound Playback");
//			if (pc == null) {
//				return null;
//			}
//			playBackSystem = pc.getFilePlayback();
			playBackSystem = playbackControl.getFilePlayback();
//			playBackSystem = new SoundCardPlayback(null);
		}
		return playBackSystem;
	}

	@Override
	public int getMaxChannels() {
		return PARAMETER_UNKNOWN;
	}
	
	@Override
	public int getMaxSampleRate() {
		return PARAMETER_UNKNOWN;
	}
	
	/**
	 * Pass through for easy access from channellistpanel
	 * @return
	 */
	public double readSampleRate() {
		return acquisitionDialog.readSampleRate();
	}
	
	@Override
	public boolean isRealTime() {
		return true;
	}

	public int getChannels() {
		return PARAMETER_UNKNOWN;
	}
	
	public boolean isEnabledMultiBoardOps() {
		return allowMultiBoard.isSelected();
	}
	
	private void enableMultiBoardOps() {
//		boolean b = allowMultiBoard.isSelected();
		getNiChannelListPanel().enableMultiBoardOps();
	}
	
	public int getSampleRate() {
		return PARAMETER_UNKNOWN;
	}
	/* (non-Javadoc)
	 * @see Acquisition.DaqSystem#getPeak2PeakVoltage()
	 */
	@Override
	public double getPeak2PeakVoltage(int swChannel) {
		double[] range = niParameters.getAIRange(swChannel);
		if (range == null) {
			return PARAMETER_UNKNOWN;
		}
		return range[1]-range[0];
	}
	
	public Serializable getSettingsReference() {
		return niParameters;
	}
	
	public long getSettingsVersion() {
		return NIDaqParams.serialVersionUID;
	}
	
	public String getUnitName() {
//		return settingsUnitName;
		return daqControl.getUnitName();
	}
	
	public static final String settingsUnitName = "NI-DAQ Card System";
	
//	/**
//	 * Get unit type for the settings reference. Gets a bit daft 
//	 * since original always returned "Acquisition System" and there
//	 * would be a clash of repeated names in the settings file. 
//	 */	
//	private void setSettingsUnitName() {
//		PamControlledUnit other = PamController.getInstance().findControlledUnit(AcquisitionControl.unitType);
//		if (other != null) {
//			settingsUnitName = daqControl.getUnitName()  + " NI-DAQ";
//		}
//	}

	public String getUnitType() {
//		return "Acquisition System";
		return settingsUnitName;
	}
	
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
//		if (PamSettingManager.getInstance().isSettingsUnit(this, pamControlledUnitSettings)) {
			try {
				niParameters = ((NIDaqParams) pamControlledUnitSettings.getSettings()).clone();
			}
			catch (ClassCastException e) {
				e.printStackTrace();
				System.out.println("This will happen when upgrading to July 2009 NI code and will only happen once");
				niParameters = new NIDaqParams(sysType);
			}
			return true;
//		}
//		return false;
	}
	
	public void useSettings() {
		// TODO Auto-generated method stub		
	}
	
	class SelectBoard implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (allowMultiBoard.isSelected() == false) {
				getNiChannelListPanel().setAllBoards(audioDevices.getSelectedIndex());
			}
			checkDevice();
		}
	}
	private void checkDevice() {
		NIDeviceInfo devInfo = (NIDeviceInfo) audioDevices.getSelectedItem();
		if (devInfo == null) {
			warningText.setText("*** No device selected ***");
		}
		else if (devInfo.isSimulated()) {
			warningText.setText("*** Simulted NI Device ***");
		}
		else if (devInfo.isExists() == false) {
			warningText.setText("*** This NI Device is not currently present ***");
		}
		else {
			warningText.setText("Device present and working");
		}
	}
	public void enableMasterDevice() {
		audioDevices.setEnabled(allowMultiBoard.isSelected() == false);
	}
	
	public void setMasterDevice(int iDevice) {
		if (audioDevices == null || allowMultiBoard == null || allowMultiBoard.isSelected() == false) {
			return;
		}
		if (iDevice >= 0) {
			audioDevices.setSelectedIndex(iDevice);
		}
	}
//	
//	private void fillRangeData() {
//		if (rangeList == null) {
//			return;
//		}
//		rangeList.removeAllItems();
//		int currRange = rangeList.getSelectedIndex();
//		int di = audioDevices.getSelectedIndex();
//		if (di < 0) {
//			return;
//		}
//		NIDeviceInfo deviceInfo = niDevices.get(di);
//		int nr = deviceInfo.getNumAIVoltageRanges();
//		for (int i = 0; i < nr; i++) {
//			rangeList.addItem(deviceInfo.getAIVoltageRangeString(i));
//		}
//		if (currRange > 0 && currRange < nr) {
//			rangeList.setSelectedIndex(currRange);
//		}
//		niRanges = deviceInfo.getAIRangeArray();
//	}
	
//	class RangeAction implements ActionListener {
//
//		@Override
//		public void actionPerformed(ActionEvent arg0) {
//			int iR = rangeList.getSelectedIndex();
//			if (iR >= 0 && niRanges != null && niRanges.length > iR) {
//				double newRange = niRanges[iR];
//				acquisitionDialog.setVPeak2Peak(newRange * 2);
//			}
//		}
//		
//	}
	
	class AllowMultiBoard implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			if (warnMultiBoardOps()) {
				enableMultiBoardOps();
			}
			enableMasterDevice();
		}
	}
	
	/**
	 * If it's true that multi board is enabled, issue a warning
	 * and possibly return false in which case the checkbox will
	 * be unchecked again. 
	 * 
	 * @return
	 */
	private boolean warnMultiBoardOps() {
		if (allowMultiBoard.isSelected()) {
			String warning = "WARNING Running multiple NI data acquisition devices requires " +
			"special hardware configuration.\n\n" +
			"You should refer to the PAMGUARD help file and information in the manual for " +
			"your devices\nbefore proceeding\n";
			int ans = JOptionPane.showConfirmDialog(null, warning, "Warning",
		            JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (ans == JOptionPane.OK_OPTION) {
				return true;
			}
			else {
				allowMultiBoard.setSelected(false);
				return false;
			}
		}
		else return true;
	}
	
	/**
	 * New capture thread for JNI operation
	 * @author Doug Gillespie
	 *
	 */
	class NICaptureThread implements Runnable {

		@Override
		public void run() {
			nidaq.startDAQ();
		}
		
	}
	
	class NITransferThread implements Runnable {
		
		public void run() {
			
		}
	}
	
	/**
	 * Get's called back from the C side. The data
	 * are still in a shared buffer at this point, so 
	 * copy it out into a new array.
	 * <p>
	 * We also need to swap threads at this point, so add the 
	 * new data to a list that some other process will be emptying
	 * on a timer. 
	 * @param iChan channel number
	 * @param data data array. 
	 */
	public void fullBuffer(int iChan, double[] data) {
		// need to make data units here and send them off
		// the incoming data are about to get overwritten, so 
		// need to make a complete copy of it
		long millis = daqControl.getDaqProcess().absSamplesToMilliseconds(channelSampleCount[iChan]);
		RawDataUnit newRawData = new RawDataUnit(millis, 1<<iChan, channelSampleCount[iChan], data.length);
//		double[] newData = new double[data.length];
//		for (int i = 0; i < data.length; i++) {
//			newData[i] = data[i];
//		}
		double[] newData = Arrays.copyOf(data, data.length);
		channelSampleCount[iChan] += data.length;
		newRawData.setRawData(newData);
		newDataUnits.addNewData(newRawData, iChan);
		
	}
	
	@Override
	public void daqHasEnded() {
		// TODO Auto-generated method stub
		
	}
	
	
	public void setDeviceList(int[] deviceList) {
		niParameters.setDeviceList(deviceList);
	}
		
	public int[] getDeviceList() {
		return niParameters.getDeviceList();
	}
	public void setHWChannelList(int[] hwChannelsList) {
		niParameters.setHwChannelList(hwChannelsList);
	}
		
	public int[] getHWChannelList() {
		return niParameters.getHwChannelList();
	}

	@Override
	public boolean supportsChannelLists() {
		return true;
	}

	public ArrayList<NIDeviceInfo> getNiDevices() {
		return niDevices;
	}


	@Override
	public String getDeviceName() {
		return "0";
	}

	public Nidaq getNiDaq() {
		return nidaq;
	}

	/**
	 * @return the niChannelListPanel
	 */
	public NIChannelListPanel getNiChannelListPanel() {
		if (niChannelListPanel == null) {
			niChannelListPanel = new NIChannelListPanel(this, daqControl);
		}
		return niChannelListPanel;
	}
}
