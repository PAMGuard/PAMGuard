package Acquisition;

import java.awt.BorderLayout;
import java.io.Serializable;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import Acquisition.layoutFX.AcquisitionPaneFX;
import Acquisition.layoutFX.DAQSettingsPane;
import Acquisition.layoutFX.SoundCardDAQPane;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackSystem;
import soundPlayback.SoundCardPlayback;
import wavFiles.ByteConverter;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import PamguardMVC.debug.Debug;

/**
 * Everything and everything to do with controlling and reading sound cards.
 * 
 * @author Doug Gillespie
 * @see Acquisition.DaqSystem
 * @see Acquisition.AcquisitionProcess
 *
 */
public class SoundCardSystem extends DaqSystem implements PamSettings {

	public static final String sysType = "Sound Card";

	JPanel daqDialog;
	
	JComboBox audioDevices;
	
	SoundCardParameters soundCardParameters = new SoundCardParameters(sysType);
	
	private AudioFormat audioFormat;

	private AudioDataQueue newDataList;
	
	private volatile boolean stopCapture;
	
	private AcquisitionControl acquisitionControl;
	
	private int rawBufferSizeInBytes;
	// Might need this to be more flexible
	private int sampleSizeInBytes = 2;
	private double sampleMax = 32768;
	
	private int daqChannels;
	
	private TargetDataLine targetDataLine;

	private PlaybackSystem soundCardPlayback;

	private int dataUnitSamples;

	private ByteConverter byteConverter;
	
	/**
	 * JavaFX settings pane for sound card settings
	 */
	private SoundCardDAQPane soundCardSettingsPane;
	
	public SoundCardSystem (AcquisitionControl daqControl) {
		this.acquisitionControl = daqControl;
		PamSettingManager.getInstance().registerSettings(this);
		soundCardPlayback = new SoundCardPlayback(this);
	}
	
	@Override
	public boolean prepareSystem(AcquisitionControl daqControl) {

		this.acquisitionControl = daqControl;
		
		// keep a reference to where data will be put.
		this.newDataList = daqControl.getDaqProcess().getNewDataQueue();
		if (this.newDataList == null) return false;
		daqChannels = daqControl.acquisitionParameters.nChannels;
		float sampleRate = daqControl.acquisitionParameters.sampleRate;
		
		audioFormat = new AudioFormat(sampleRate, 16, daqChannels, true, true);

		dataUnitSamples = (int) (acquisitionControl.acquisitionParameters.sampleRate / 10);
		dataUnitSamples = Math.max(dataUnitSamples, 1000);
		
		ArrayList<Mixer.Info> mixerinfos = getInputMixerList();
		//System.out.println("soundCardParameters.deviceNumber:"+soundCardParameters.deviceNumber);
		if (soundCardParameters.deviceNumber < 0 || soundCardParameters.deviceNumber >= mixerinfos.size()) {
			System.out.println("Invalid sound card number: " + soundCardParameters.deviceNumber);
			return false;
		}
		Mixer.Info thisMixerInfo = mixerinfos.get(soundCardParameters.deviceNumber);
		Mixer thisMixer = AudioSystem.getMixer(thisMixerInfo);
		if (thisMixer.getTargetLineInfo().length <= 0){
			thisMixer.getLineInfo();
			return false;
		}
		
		try {
			// try to get the device of choice ...
			targetDataLine = (TargetDataLine) thisMixer.getLine(thisMixer.getTargetLineInfo()[0]);
		} catch (Exception Ex) {
			Ex.printStackTrace();
			return false;
		}
		if (targetDataLine == null) return false;

		boolean lineOk = false;
//		audioFormat = new AudioFormat(sampleRate, 24, daqChannels, true, true);
//		// first try to open input for 24 bit recording, 
//		try {
//			targetDataLine.open(audioFormat);
//			sampleSizeInBytes = 3;
//			sampleMax = 1<<23;
//			lineOk = true;
//			System.out.println("Sound card 24 bit recording available");
//		} catch (LineUnavailableException Ex) {
//			lineOk = false;
//		}
		// if the 24 bit recording failed, then try to open for 16 bit recording. 
		if (lineOk == false) {
			audioFormat = new AudioFormat(sampleRate, 16, daqChannels, true, true);
			try {
				targetDataLine.open(audioFormat);
				sampleMax = 1<<15;
				lineOk = true;
			} catch (LineUnavailableException Ex) {
				System.out.println("Sound card 16 bit recording unavailable");
				return false;
			}
		}

		byteConverter = ByteConverter.createByteConverter(audioFormat);
		
		// Buffer size to hold 1/10th of a second
		rawBufferSizeInBytes = dataUnitSamples  * daqChannels * sampleSizeInBytes;
		
		
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
		
		if (targetDataLine == null) return false;

		// Create a thread to capture the sound card input
		// and start it running.
		Thread captureThread = new Thread(new CaptureThread());
		captureThread.start();
		
		// then start the device
		targetDataLine.start();
		
		setStreamStatus(STREAM_RUNNING);
		return true;
		
	}

	@Override
	public void stopSystem(AcquisitionControl daqControl) {
		stopCapture = true;
		// now wait for the thread to finsih - when it does it
		// will set stopCapture back to false. Set max 2s timeout
		int count = 0;
		while (stopCapture && captureRunning && ++count < 100) {
			try {
			 Thread.sleep(20);
			}
			catch (Exception Ex) {
				Ex.printStackTrace();
			}
			//System.out.println("Sleeping while thread exits");
		}
//		System.out.println("Sound card thread exit took " + count*20 + " ms");
		if (targetDataLine == null) {
			return;
		}
		targetDataLine.stop();
		targetDataLine.close();
		setStreamStatus(STREAM_CLOSED);
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
		
		p.setBorder(new TitledBorder("Select audio line"));
		p.setLayout(new BorderLayout());
		p.add(BorderLayout.CENTER, audioDevices = new JComboBox());
		
		return p;
	}


	public static ArrayList<Mixer.Info> getInputMixerList() {
		ArrayList<Mixer.Info> mixers = new ArrayList<Mixer.Info>();
		AudioSystem.getMixerInfo();
		Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
		Mixer mixer;
		for (int i = 0; i < mixerInfo.length; i++) {
			mixer = AudioSystem.getMixer(mixerInfo[i]);
			//System.out.println("mixer:"+mixer);
			//System.out.println(mixer.getClass().getName());
			//System.out.println("mixer info:"+mixerInfo[i]);
			//extra bit for Mac's where "Line In" is under mixer "Built-in Line Input" 
			//which is com.sun.media.sound.SimpleInputDevice
			//cjb 2009-01-05
			if (mixer.getTargetLineInfo().length > 0){
				if ( (mixer.getClass().getName().equals("com.sun.media.sound.DirectAudioDevice")) ||
						(mixer.getClass().getName().equals("com.sun.media.sound.SimpleInputDevice"))) {
					//System.out.println("Adding to input mixer list:"+mixer.getClass().getName());
					mixers.add(mixerInfo[i]);
				}
			}
		}
		return mixers;
	}
	public static ArrayList<Mixer.Info> getOutputMixerList() {
		ArrayList<Mixer.Info> mixers = new ArrayList<Mixer.Info>();
		AudioSystem.getMixerInfo();
		Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
		Mixer mixer;
		for (int i = 0; i < mixerInfo.length; i++) {
			try {
			mixer = AudioSystem.getMixer(mixerInfo[i]);
			}
			catch (Exception e) {
				continue;
			}
			//System.out.println("Output mixer list:" + mixer);
			//System.out.println("Output mixer list:" + mixer.getClass().getName());
			//extra bit for Mac's where "Line Out" is under mixer "Built-in Line Input" 
			//which gives 0 length array for getSourceLineInfo() 
			//cjb 2009-01-05
			if (mixer.getSourceLineInfo().length > 0){
				if ((mixer.getClass().getName().equals("com.sun.media.sound.DirectAudioDevice")) ||
				(mixer.getClass().getName().equals("com.sun.media.sound.HeadspaceMixer"))) {
					//System.out.println(mixer.getClass().getName());
					mixers.add(mixerInfo[i]);
//					Debug.out.printf("Use mixer %d, %s\n", i,  mixerInfo[i].getName());
				}			
//				else {
//					Debug.out.printf("Don't Use mixer %d, %s\n", i,  mixerInfo[i].getName());
//				}
			}
//			else {
//				Debug.out.printf("Really don't use mixer %d, %s\n", i,  mixerInfo[i].getName());
//			}
		}
		return mixers;
	}
	
	public static ArrayList<String> getDevicesList() {
		ArrayList<String> devices = new ArrayList<String>();
		ArrayList<Mixer.Info> mixers = getInputMixerList();
		for (int i = 0; i < mixers.size(); i++) {
			//System.out.println("Adding Device:"+mixers.get(i).getName());
			devices.add(mixers.get(i).getName());
		}
		return devices;

			
	}
	
	@Override
	public void dialogSetParams() {
		// do a quick check to see if the system type is stored in the parameters.  This field was added
		// to the SoundCardParameters class on 23/11/2020, so any psfx created before this time
		// would hold a null.  The system type is used by the getParameterSet method to decide
		// whether or not to include the parameters in the XML output
		if (soundCardParameters.systemType==null) soundCardParameters.systemType=getSystemType();


		ArrayList<String> devices = getDevicesList();
		
		audioDevices.removeAllItems();
		for (int i = 0; i < devices.size(); i++) {
			//System.out.println("Adding to audio device:"+devices.get(i));
			audioDevices.addItem(devices.get(i));
		}
		
		soundCardParameters.deviceNumber = Math.max(Math.min(devices.size()-1, soundCardParameters.deviceNumber), 0);
		if (devices.size() > 0) {
			audioDevices.setSelectedIndex(soundCardParameters.deviceNumber);
		}
		
	}
	
	@Override
	public boolean dialogGetParams() {
		//System.out.println("soundCardParameters: " + soundCardParameters + "audioDevices: " + audioDevices);
		if (audioDevices!=null) soundCardParameters.deviceNumber = audioDevices.getSelectedIndex();
		return true;
	}
	
	@Override
	public String getSystemType() {
		return sysType;
	}
	
	@Override
	public String getSystemName() {
		// return the name of the sound card.
		ArrayList<String> devices = getDevicesList();
		//System.out.println("soundCardParameters.deviceNumber:"+soundCardParameters.deviceNumber);
		//System.out.println("devices.size():"+devices.size());
		if (devices == null || devices.size() <= soundCardParameters.deviceNumber) {
			return new String("No card");
		}
		else {
			//device number was sometimes ending up as -1 probably as getting set from
			//as selected item in dialog when nothing selected? cjb
			try {
				return devices.get(java.lang.Math.max(soundCardParameters.deviceNumber,0));
			}
			catch (Exception e) {
				return "No card";
			}
		}
	}

	@Override
	public int getMaxChannels() {
		//javax.sound only seems to support upto 2 channels
		//i.e. option of 1 or 2 - mono or stereo
		//even though API reading API can make one think
		//could have more... cjb 2010-04-28 
		return 2;
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
		return true;
	}

	@Override
	public PlaybackSystem getPlaybackSystem(PlaybackControl playbackControl, DaqSystem daqSystem) {
		return soundCardPlayback;
	}

	public int getChannels() {
		return PARAMETER_UNKNOWN;
	}

	public int getSampleRate() {
		return PARAMETER_UNKNOWN;
	}

	public Serializable getSettingsReference() {
		return soundCardParameters;
	}

	public long getSettingsVersion() {
		return SoundCardParameters.serialVersionUID;
	}

	public String getUnitName() {
//		return "Sound Card System";
		return acquisitionControl.getUnitName();
	}

	public String getUnitType() {
//		return "Acquisition System";
		return "Sound Card System";
	}
	
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
//		if (PamSettingManager.getInstance().isSettingsUnit(this, pamControlledUnitSettings)) {
		try {
			soundCardParameters = ((SoundCardParameters) pamControlledUnitSettings.getSettings()).clone();
			return soundCardParameters != null;
		}
		catch (ClassCastException e) {
			e.printStackTrace();
		}
		return false;
	}

	private volatile boolean captureRunning;

	
//	/**
//	 * Unpack the byte data into a sensible number.
//	 * @param buffer
//	 * @param position
//	 * @return
//	 */
//	public int getSample(byte[] buffer, int position) {
//			return (int)(((buffer[position] & 0xff) << 8) + (buffer[position + 1] & 0xff));
////			if (sampleSizeInBytes == 3) {
////				return buffer[position] << 16 + buffer[position+1] << 8 + buffer[position+2];
////			}
////			else{
////				return buffer[position] << 8 + buffer[position+1];
////			}
//		}

	class CaptureThread implements Runnable {
		
		public void run() {
			stopCapture = false;
			captureRunning = true;
			// move this here, sinc we'll only need one copy of it
			// if we move the data to PamDataUnits immediatly
			/*
			 * So much of this code is identical to the file reading stuff, that
			 * the two methods should be combined
			 */
			int count = 0;
			byte tempBuffer[] = new byte[rawBufferSizeInBytes];
			RawDataUnit newDataUnit;
			int newSamplesPerChannel;
			int startbyte;
//			double[] doubleData;
			int sample;
			long totalSamples = 0;
			long ms;
			try {// Loop until stopCapture is set
				// by another thread that
				// services the Stop button.
				while (!stopCapture) {
					count++;
					// Read data from the internal
					// buffer of the data line.
					
					int bytesRead = targetDataLine.read(tempBuffer, 0,
							tempBuffer.length);
					
					// System.out.println("Read in :" + bytesRead + " bytes");
					/*
					 * Much better to create the PamDataUnits here, since the
					 * block lenght and other information is required to do
					 * this. The main thread then just has to add the references
					 * to the PamDataBlocks to theoutput data block.
					 */
					
					if (bytesRead > 0) {
						// convert byte array to set of double arrays, one per
						// channel  e.g. framesize = 1 for 8 bit sound
						
						// Framesize is bytecost per Sample in time. ie chanels x sampledepth in bygtes
						newSamplesPerChannel = bytesRead / audioFormat.getFrameSize();
						double[][] doubleData = new double[daqChannels][newSamplesPerChannel];
						byteConverter.bytesToDouble(tempBuffer, doubleData, bytesRead);
						//System.out.println("frameSize: " +audioFormat.getFrameSize());
						for (int ichan = 0; ichan < daqChannels; ichan++) {
							/*
							 * Make a new double array everytime for each
							 * channel and pass it over to the datablock
							 */
//							startbyte = ichan * (audioFormat.getSampleSizeInBits()/8); //     /8 means /bits per byte.
//							doubleData = new double[newSamplesPerChannel];
//							for (int isamp = 0; isamp < newSamplesPerChannel; isamp++) {
//								sample = getSample(tempBuffer, startbyte);
//								// TODO FIXME scale by correct (variable) resolution
//								doubleData[isamp] = (double)sample / sampleMax;
//								startbyte += audioFormat.getFrameSize(); // skips ahead channels x sample bit depth in bytes
//							}
							// now make a PamDataUnit
							ms = acquisitionControl.getAcquisitionProcess().absSamplesToMilliseconds(totalSamples);
							newDataUnit = new RawDataUnit(ms, 1 << ichan, totalSamples, newSamplesPerChannel);
							newDataUnit.setRawData(doubleData[ichan]);
							newDataList.addNewData(newDataUnit, ichan);
						}
						totalSamples += newSamplesPerChannel;
					}
					try {
						Thread.sleep(50);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					
				}// end while
			} catch (Exception e) {
				System.out.println(e);
				System.exit(0);
			}// end catch

			stopCapture = false;
			captureRunning = false;
			setStreamStatus(STREAM_ENDED);
			
		}// end run
	}// end inner class CaptureThread
	@Override
	public void daqHasEnded() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDeviceName() {
		return String.format("%d", soundCardParameters.deviceNumber);
	}
	
	@Override
	public DAQSettingsPane getDAQSpecificPane(AcquisitionPaneFX acquisitionPaneFX) {
		if (soundCardSettingsPane==null) {
			soundCardSettingsPane = new SoundCardDAQPane(this); 
		}
		return soundCardSettingsPane;
	}

	/**
	 * Get the parameters for the sound card system. 
	 * @return the parameters for the sound card system
	 */
	public SoundCardParameters getSoundCardParameters() {
		return this.soundCardParameters;
	}
}
