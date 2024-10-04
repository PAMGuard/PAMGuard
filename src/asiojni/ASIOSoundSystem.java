package asiojni;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionDialog;
import Acquisition.AudioDataQueue;
import Acquisition.DaqSystem;
import Acquisition.SoundCardParameters;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import PamUtils.PamCalendar;
import PamguardMVC.PamConstants;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackSystem;

/**
 * Everything and everything to do with controlling and reading ASIO 
 * sound cards.
 * 
 * @author Doug Gillespie
 * @see Acquisition.DaqSystem
 * @see Acquisition.AcquisitionProcess
 *
 */
public class ASIOSoundSystem extends DaqSystem implements PamSettings {

	private JPanel daqDialog;

	private JComboBox audioDevices;

	private SoundCardParameters soundCardParameters = new SoundCardParameters(getSystemType());

	private AudioFormat audioFormat;

	private AudioDataQueue newDataUnits;

	private volatile boolean stopCapture;

	private AcquisitionControl acquisitionControl;

	private int rawBufferSizeInBytes;
	// Might need this to be more flexible
	private final int sampleSizeInBytes = 2;

	private int daqChannels;
	
	/**
	 * collect data using zero channel indexes rather than 
	 * the annoying hardware channel numbers. 
	 */
	static private final boolean ZeroIndexChannels = true;
	
	/**
	 * locally held channel list i.e. hardware channels for software channels.
	 */
	private int[] channelList; 
	
	/**
	 * locally held convert hardware channels to software channels
	 */
	private int[] reverseChannelList; 

	private AsioJniInterface asioJniInterface;
	
	private ASIOPlaybackSystem asioPlaybackSystem;

	private int dataUnitSamples;


	public ASIOSoundSystem (AcquisitionControl acquisitionControl) {
		this.acquisitionControl = acquisitionControl;
		asioJniInterface = new AsioJniInterface(this);
		PamSettingManager.getInstance().registerSettings(this);
		asioPlaybackSystem = new ASIOPlaybackSystem(this);
	}


	// IS THIS REQUIRED IS ASIO
	// PREPARATION AND START COMBINED
	@Override
	public boolean prepareSystem(AcquisitionControl daqControl) {
//		System.out.println("JAVA : ASIOSoundSystem::prepareSystem(AcquisitionControl daqControl)");
		
		this.acquisitionControl = daqControl;

		// keep a reference to where data will be put.
		this.newDataUnits = daqControl.getDaqProcess().getNewDataQueue();
		if (this.newDataUnits == null) return false;
		daqChannels = daqControl.acquisitionParameters.nChannels;
		float sampleRate = daqControl.acquisitionParameters.sampleRate;

		channelList = acquisitionControl.acquisitionParameters.getHardwareChannelList();
		
		reverseChannelList = acquisitionControl.acquisitionParameters.getChannelListIndexes();

		audioFormat = new AudioFormat(sampleRate, 16, daqChannels, true, true);

		dataUnitSamples = (int) (acquisitionControl.acquisitionParameters.sampleRate / 10);
		dataUnitSamples = Math.max(dataUnitSamples, 1000);
		// Buffer size to hold 1/10th of a second
		rawBufferSizeInBytes = dataUnitSamples  * daqChannels * sampleSizeInBytes;

		/*ArrayList<AsioDriverInfo> asioDriverListStrings = asioJniInterface.getAsioDriverInfos().getCurrentAsioDriverList();

		for(int i = 0; i<asioDriverListStrings.size();i++){
			//System.outprintln("ASIO drivers: " + asioDriverListStrings.get(i).driverName);
		}*/

		asioJniInterface.setPlaybackChannels(null);
		
		return true;
	}

	/* (non-Javadoc)
	 * @see Acquisition.DaqSystem#getDataUnitSamples()
	 */
	@Override
	public int getDataUnitSamples() {
		return dataUnitSamples;
	}


	public boolean preparePlayback(int channels, float sampleRate) {
//		System.out.println("JAVA : ASIOSoundSystem::preparePlayback(int channels, float sampleRate)");
		
		if (sampleRate != this.acquisitionControl.acquisitionParameters.sampleRate) {
			return false;
		}
		int playbackChannels[] = new int[channels];
		for (int i = 0; i < channels; i++) {
			playbackChannels[i] = i;
		}
		
		asioJniInterface.setPlaybackChannels(playbackChannels);
		
		return true;
	}

	@Override
	public boolean areSampleSettingsOk(int numInputsRequested, float sampleRateRequested){

		boolean canSample = false;

		int driverIndex = audioDevices.getSelectedIndex();
		AsioDriverInfo asioDriverInfo = asioJniInterface.getAsioDriverInfos().getCurrentAsioDriverList().get(driverIndex);

		for(int i=0;i<asioDriverInfo.sampleRateInfo.size();i++){
			//System.outprintln(asioDriverInfo.maxChannels.get(i) + " requested: " + numInputsRequested + "   " 
			//		+ asioDriverInfo.sampleRateInfo.get(i) + "  requested: " + (int)sampleRateRequested);
			if(numInputsRequested <=asioDriverInfo.maxChannels.get(i) 
					&& asioDriverInfo.sampleRateInfo.get(i).equals((int)sampleRateRequested)){

				canSample = true;
			}
		}



		return canSample;
	}

	@Override
	public void showSampleSettingsDialog(AcquisitionDialog acquisitionDialog){

		int driverIndex = audioDevices.getSelectedIndex();
//		System.out.println("showSampleSettingsDialog() calling getAsioDriverList()");
//		System.out.flush();
		String driverSettingsString = new String("Sample settings unavailable!\n\n" +
				asioJniInterface.getAsioDriverInfos().getCurrentAsioDriverList().get(driverIndex).driverName  + " supports:\n\n");
		StringBuffer driverSettingsStringBuffer = new StringBuffer();
		driverSettingsStringBuffer.append(driverSettingsString);

		AsioDriverInfo asioDriverInfo = asioJniInterface.getAsioDriverInfos().getCurrentAsioDriverList().get(driverIndex);
		for(int i=0;i<asioDriverInfo.sampleRateInfo.size();i++){

			driverSettingsStringBuffer.append(asioDriverInfo.maxChannels.get(i) + " input channels at: " + asioDriverInfo.sampleRateInfo.get(i) + " Hz\n");

		}
		driverSettingsString = driverSettingsStringBuffer.toString();

		JOptionPane.showMessageDialog(acquisitionDialog, driverSettingsString);

		//	asioJniInterface.setMaxChannelNumber(acquisitionParameters.-1);
		//	asioJniInterface.asioDriverSettings.setSampleRate((int)sampleRateRequested);

	}


	// startSystem Starts this acquisition system.
	@Override
	public boolean startSystem(AcquisitionControl daqControl) {

		// can't use targetDataLine for asio 
		// maybe use an VerifiedAsioInfo structure instead ???
		//if (targetDataLine == null) return false;
//		System.out.println("JAVA:: ASIOSoundSystem::startSystem::1");
//		System.out.flush();

		totalSamples = new long[PamConstants.MAX_CHANNELS];
		
		try {

			//targetDataLine.open(audioFormat);
			//targetDataLine.start();
			// Create a thread to capture the sound card input
			// and start it running.
			// Thread captureThread = new Thread(new CaptureThread());
			// captureThread.start();

			Thread captureThreadAsio = new Thread(new CaptureThreadAsio());
			captureThreadAsio.start();
//			asioJniInterface.setMaxChannelNumber(daqControl.acquisitionParameters.getNChannels()-1);
//			System.out.println("ASIOSoundSystem::startSystem::2");
//			System.out.flush();
			asioJniInterface.asioDriverSettings.setDriverName(asioJniInterface.asioDriverInfos.getCurrentAsioDriverList().get(soundCardParameters.deviceNumber).driverName);
//			System.out.println("ASIOSoundSystem::startSystem::3");
//			System.out.flush();
			asioJniInterface.asioDriverSettings.setSampleRate((int)daqControl.acquisitionParameters.getSampleRate());
//			System.out.println("ASIOSoundSystem::startSystem::4");
//			System.out.flush();
			
			asioJniInterface.setInputChannelList(daqControl.acquisitionParameters.getNChannelList(),
					daqControl.acquisitionParameters.nChannels); //Xiao Yan Deng
//			System.out.println("ASIOSoundSystem::startSystem::02");
//			System.out.flush();
			
			asioJniInterface.asioController.asioStart();
			
//			System.out.println("ASIOSoundSystem::startSystem::5");
//			System.out.flush();
			setStreamStatus(STREAM_RUNNING);
//			System.out.println("ASIOSoundSystem::startSystem::6");
//			System.out.flush();
			
		} catch (Exception Ex) {
			Ex.printStackTrace();
			return false;
		}
//		System.out.println("ASIOSoundSystem::startSystem::7");
//		System.out.flush();
		return true;

	}

	@Override
	public void stopSystem(AcquisitionControl daqControl) {
		stopCapture = true;
		asioJniInterface.setRecordActive(false);
		// now wait for the thread to finsih - when it does it
		// will set stopCapture back to false. Set max 2s timeout
		int count = 0;
		while (stopCapture && ++count < 100) {
			try {
				Thread.sleep(20);
			}
			catch (Exception Ex) {
				Ex.printStackTrace();
			}
			//System.out.println("Sleeping while thread exits");
		}
//		System.out.println("Sound card thread exit took " + count*20 + " ms");

		//	replace with JNI INTERFACE
		//targetDataLine.stop();
		//targetDataLine.close();
	}

	@Override
	public JPanel getDaqSpecificDialogComponent(AcquisitionDialog acquisitionDialog) {

		if (daqDialog == null) {
			daqDialog = createDaqDialogPanel();
		}

		return daqDialog;

	}

	private JPanel createDaqDialogPanel() {

		JPanel p = new JPanel();
		JButton asioControl = new JButton("Control");
		asioControl.addActionListener(new ActionListener() {
			int trials = 0;
			@Override
			public void actionPerformed(ActionEvent evt) {
			asioJniInterface.asioDriverSettings.displayAsioDriverSettings();

		}});

		p.setBorder(new TitledBorder("Select audio line"));
		p.setLayout(new BorderLayout());
		p.add(BorderLayout.CENTER, audioDevices = new JComboBox());
//		p.add(BorderLayout.SOUTH, asioControl);
		return p;
	}



	public ArrayList<AsioDriverInfo> getDevicesList() {
//		System.out.println("getDevicesList() calling getAsioDriverList()");
//		System.out.flush();
		ArrayList<AsioDriverInfo> asioDriverInfos = asioJniInterface.getAsioDriverInfos().getCurrentAsioDriverList();	
		for(int i = 0; i< asioDriverInfos.size();i++){
			//System.outprintln("ASIO drivers: " + asioDriverInfos.get(i).driverName);
		}
		return asioDriverInfos;


	}

	@Override
	public void dialogSetParams() {
		// do a quick check to see if the system type is stored in the parameters.  This field was added
		// to the SoundCardParameters class on 23/11/2020, so any psfx created before this time
		// would hold a null.  The system type is used by the getParameterSet method to decide
		// whether or not to include the parameters in the XML output
		if (soundCardParameters.systemType==null) soundCardParameters.systemType=getSystemType();

		ArrayList<AsioDriverInfo> devices = getDevicesList();

		audioDevices.removeAllItems();

		audioDevices.removeAllItems();
		for (int i = 0; i < devices.size(); i++) {
			audioDevices.addItem(devices.get(i).driverName);
		}

		if (soundCardParameters.deviceNumber < devices.size()) {
			audioDevices.setSelectedIndex(soundCardParameters.deviceNumber);
		}

	}

	@Override
	public boolean dialogGetParams() {
		soundCardParameters.deviceNumber = audioDevices.getSelectedIndex();
		return true;
	}

	@Override
	public String getSystemType() {
		return "ASIO Sound Cards ";
	}

	@Override
	public String getSystemName() {
		// return the name of the sound card.
		ArrayList<AsioDriverInfo> devices = getDevicesList();
		if (devices == null || devices.size() <= soundCardParameters.deviceNumber) {
			return new String("No card");
		}
		else {
			return devices.get(soundCardParameters.deviceNumber).driverName;
		}
	}

	@Override
	public int getMaxChannels() {
		return PARAMETER_UNKNOWN;
	}

	@Override
	public int getMaxSampleRate() {
		return PARAMETER_UNKNOWN;
	}

	/* (non-Javadoc)
	 * @see Acquisition.DaqSystem#getPeak2PeakVoltage()
	 */
	@Override
	public double getPeak2PeakVoltage(int swChannel) {
		return PARAMETER_UNKNOWN;
	}

	@Override
	public boolean isRealTime() {
		return true;
	}
	

	@Override
	public boolean canPlayBack(float sampleRate) {
		return true; // will be true once implemented
	}
	
	
	@Override
	public PlaybackSystem getPlaybackSystem(PlaybackControl playbackControl, DaqSystem daqSystem) {
		return asioPlaybackSystem;
	}


	public int getChannels() {
		return PARAMETER_UNKNOWN;
	}

//	public int getSampleRate() {
//		return PARAMETER_UNKNOWN;
//	}

	@Override
	public Serializable getSettingsReference() {
		return soundCardParameters;
	}

	@Override
	public long getSettingsVersion() {
		return SoundCardParameters.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		return acquisitionControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "ASIO Sound System";
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
//		if (PamSettingManager.getInstance().isSettingsUnit(this, pamControlledUnitSettings)) {
		try {
			soundCardParameters = ((SoundCardParameters) pamControlledUnitSettings.getSettings()).clone();
			return true;
		}
		catch (ClassCastException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	long[] totalSamples;
	int expectedChannel = 0;
//	long currentBlockTime = 0;
	public boolean newASIOData(double[] data, int channel) {
		if (stopCapture) return false;
		RawDataUnit newRawDataUnit;
//		if (channel == acquisitionControl.acquisitionParameters.getChannelList(0)) {
//			currentBlockTime = PamCalendar.getTimeInMillis();
//		}
		
		long ms = acquisitionControl.getAcquisitionProcess().absSamplesToMilliseconds(totalSamples[channel]);

		if (channelList[expectedChannel] != channel) {
			String str = String.format("Unexpected channel = %d, want %d; ms = %d, now = %d, diff = %d, total samples = %d, obj = %d",
					channel, expectedChannel, ms, PamCalendar.getTimeInMillis(),ms- PamCalendar.getTimeInMillis(), 
					totalSamples[channel], data.hashCode());
			System.out.println(str); 
		}
		if (++expectedChannel == this.daqChannels) {
			expectedChannel = 0;
		}
		
		if (ZeroIndexChannels) {
			channel = reverseChannelList[channel];
		}
		
		//		long ms = PamCalendar.getTimeInMillis();
		newRawDataUnit = new RawDataUnit(ms, 1<<channel, totalSamples[channel], data.length);
		newRawDataUnit.setRawData(data);
		newDataUnits.addNewData(newRawDataUnit, channel);
		totalSamples[channel] += data.length;
		
		return true;
	}

	class CaptureThreadAsio implements Runnable {
		@Override
		public void run() {

			asioJniInterface.createChannelBuffers();

			stopCapture = false;

//			int newSamplesPerChannel =0;
//			double[] doubleData;
			long totalSamplesInThread[] = new long[PamConstants.MAX_CHANNELS];
			long ms;
			int ichan;


			RawDataUnit newDataUnit;

//			Vector<double[]> currentChannelArrayList;
			Vector<RawDataUnit> asioDataUnits = asioJniInterface.getAsioDataUnits();
			while (!stopCapture) {

				try {
						Thread.sleep(50);
					} catch (Exception ex) {
						ex.printStackTrace();
//					}
				}

			}// end while (!stopCapture)

		}
	}

//	class CaptureThread implements Runnable {
//		public void run() {
//			stopCapture = false;
//			// move this here, since we'll only need one copy of it
//			// if we move the data to PamDataUnits immediately
//			/*
//			 * So much of this code is identical to the file reading stuff, that
//			 * the two methods should be combined
//			 */
//			//int count = 0;
//			byte tempBuffer[] = new byte[rawBufferSizeInBytes];
//			RawDataUnit newDataUnit;
//			int newSamplesPerChannel;
//			int startbyte;
//			double[] doubleData;
//			short sample;
//			long totalSamples = 0; // needs to be long not int to stop wrap around after about 12 hours
//			long ms;
//			try {// Loop until stopCapture is set
//				// by another thread that
//				// services the Stop button.
//				while (!stopCapture) {
//					//	count++;
//					// Read data from the internal
//					// buffer of the data line.
//
//					int bytesRead = targetDataLine.read(tempBuffer, 0,
//							tempBuffer.length);
//
//					// System.out.println("Read in :" + bytesRead + " bytes");
//					/*
//					 * Much better to create the PamDataUnits here, since the
//					 * block lenght and other information is required to do
//					 * this. The main thread then just has to add the references
//					 * to the PamDataBlocks to theoutput data block.
//					 */
//
//					if (bytesRead > 0) {
//						// convert byte array to set of double arrays, one per
//						// channel  e.g. framesize = 1 for 8 bit sound
//
//						// Framesize is bytecost per Sample in time. ie chanels x sampledepth in bygtes
//						newSamplesPerChannel = bytesRead / audioFormat.getFrameSize();
//						//System.out.println("frameSize: " +audioFormat.getFrameSize());
//						for (int ichan = 0; ichan < daqChannels; ichan++) {
//							/*
//							 * Make a new double array everytime for each
//							 * channel and pass it over to the datablock
//							 */
//							startbyte = ichan * (audioFormat.getSampleSizeInBits()/8); //     /8 means /bits per byte.
//							doubleData = new double[newSamplesPerChannel];
//							for (int isamp = 0; isamp < newSamplesPerChannel; isamp++) {
//								sample = getSample(tempBuffer, startbyte);
//								// TODO FIXME scale by correct (variable) resolution
//								doubleData[isamp] = sample / 32768.;
//								startbyte += audioFormat.getFrameSize(); // skips ahead channels x sample bit depth in bytes
//							}
//							// now make a PamDataUnit
//							ms = acquisitionControl.acquisitionProcess.absSamplesToMilliseconds(totalSamples);
////							newDataUnit = new PamDataUnit(totalSamples, ms,
////							newSamplesPerChannel, 1 << ichan, doubleData);
//							newDataUnit = new RawDataUnit(ms, 1 << ichan, totalSamples, newSamplesPerChannel);
//							newDataUnit.setRawData(doubleData);
//							newDataUnits.add(newDataUnit);
//						}
//						totalSamples += newSamplesPerChannel;
//					}
//					try {
//						Thread.sleep(50);
//					} catch (Exception ex) {
//						ex.printStackTrace();
//					}
//
//				}// end while
//			} catch (Exception e) {
//				System.out.println(e);
//				System.exit(0);
//			}// end catch
//
//			stopCapture = false;
//			setStreamStatus(STREAM_CLOSED);
//
//		}// end run
//	}// end inner class CaptureThread
	public static short getSample(byte[] buffer, int position) {
		return (short) (((buffer[position] & 0xff) << 8) | (buffer[position + 1] & 0xff));
	}


	@Override
	public void daqHasEnded() {
		// TODO Auto-generated method stub

	}


	@Override
	public boolean supportsChannelLists() {
		return true;
	}
	
	protected boolean playData(int outputChannel, RawDataUnit rawDataUnit, double gain) {
		return asioJniInterface.playData(outputChannel, rawDataUnit);
	}

	@Override
	public String getDeviceName() {
		return String.format("%d", soundCardParameters.deviceNumber);
	}


	/* (non-Javadoc)
	 * @see Acquisition.DaqSystem#getSampleBits()
	 */
	@Override
	public int getSampleBits() {
		return 24;
	}
}
