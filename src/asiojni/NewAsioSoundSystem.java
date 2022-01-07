package asiojni;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackParameters;
import soundPlayback.PlaybackSystem;
import soundPlayback.fx.PlaybackSettingsPane;
import soundPlayback.swing.PlaybackDialogComponent;

import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriver;
import com.synthbot.jasiohost.AsioDriverListener;
import com.synthbot.jasiohost.AsioException;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionDialog;
import Acquisition.DaqSystem;
import Acquisition.AudioDataQueue;
import Acquisition.SoundCardParameters;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.PamConstants;

/**
 * this uses jAsioHost for acquisition. There are a few problems with jAsioHost. 
 * In particular, the built version does not correctly unpack 24 bit data, so there 
 * is a fudged fix in the read function. The latest code on the github site (from about 2012) 
 * works correclty for the 64 bit version, but throws a version compatibility error on the 32 bit
 * version. I've gone with the cludgy fix, checking numbers are not > 1. 
 * @author dg50
 *
 */
public class NewAsioSoundSystem extends DaqSystem implements PamSettings {

	public static final String sysType = "New ASIO System";

	private AcquisitionControl acquisitionControl;

	private AsioDriver currentDriver;

	private SoundCardParameters soundCardParameters = new SoundCardParameters(sysType);

	private JPanel dialogPanel = null;

	private JComboBox<String> deviceList;

	private int[] inputChannelList;

	private AsioChannel[] inputChannels;

	private NewAsioPlayback newAsioPlayback;

	private boolean prepareOutput;

	private int[] outputChannelList;

	private AsioChannel[] outputChannels;

	private double[][] newDataBlocks;

	private int dataBlockSize;

	private int[] dataBlockPos;

	private long[] channelSamples;

	private int prefBufferSize;

	private float[] tempFloatBuffer;

	private AudioDataQueue newDataUnits;
	
	private int[] currentPlayPos = new int[PamConstants.MAX_CHANNELS];

	
	public NewAsioSoundSystem(AcquisitionControl acquisitionControl) {
		this.acquisitionControl = acquisitionControl;
		newAsioPlayback = new NewAsioPlayback();
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public boolean canPlayBack(float sampleRate) {
		return true;
	}

	@Override
	public void daqHasEnded() {	
		if (currentDriver != null) {
			currentDriver.shutdownAndUnloadDriver();
		}
	}

	@Override
	public boolean dialogGetParams() {
		soundCardParameters.deviceNumber = deviceList.getSelectedIndex();
		return true;
	}

	@Override
	public void dialogSetParams() {
		// do a quick check to see if the system type is stored in the parameters.  This field was added
		// to the SoundCardParameters class on 23/11/2020, so any psfx created before this time
		// would hold a null.  The system type is used by the getParameterSet method to decide
		// whether or not to include the parameters in the XML output
		if (soundCardParameters.systemType==null) soundCardParameters.systemType=getSystemType();

		deviceList.removeAllItems();
		try {
			List<String> driverNames = AsioDriver.getDriverNames();
			for (String aName:driverNames) {
				deviceList.addItem(aName);
			}
		}
		catch (AsioException e) {
			System.out.println(e.getLocalizedMessage());
		}
		if (soundCardParameters.deviceNumber < deviceList.getItemCount()) {
			deviceList.setSelectedIndex(soundCardParameters.deviceNumber);
		}
	}

	@Override
	public JComponent getDaqSpecificDialogComponent(
			AcquisitionDialog acquisitionDialog) {
		if (dialogPanel == null) {
			createDialogPanel();
		}
		return dialogPanel;
	}

	private void createDialogPanel() {
		dialogPanel = new JPanel(new BorderLayout());
		dialogPanel.setBorder(new TitledBorder("Select Device"));
		JButton asioControl = new JButton("Settings ...");
		asioControl.addActionListener(new AsioControl());
		dialogPanel.add(BorderLayout.EAST, asioControl);

		deviceList = new JComboBox<String>();
		dialogPanel.add(BorderLayout.CENTER, deviceList);

	}

	private class AsioControl implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			showASIOControl();
		}
	}

	public void showASIOControl() {
		try {
			List<String> driverNames = AsioDriver.getDriverNames();
			int ind = deviceList.getSelectedIndex();
			AsioDriver aDriver = AsioDriver.getDriver(driverNames.get(ind));
			if (aDriver == null) {
				return;
			}
			aDriver.openControlPanel();
		}
		catch (AsioException e) {
			System.out.println(e.getLocalizedMessage());
		}

	}

	@Override
	public int getDataUnitSamples() {
		return dataBlockSize;
	}

	@Override
	public String getDeviceName() {
		return "ASIO";
	}

	@Override
	public int getMaxChannels() {
		return PamConstants.MAX_CHANNELS;
	}

	@Override
	public int getMaxSampleRate() {
		return 1000000;
	}

	@Override
	public double getPeak2PeakVoltage(int swChannel) {
		return acquisitionControl.acquisitionParameters.voltsPeak2Peak;
	}

	@Override
	public String getSystemName() {
		try {
			List<String> driverNames = AsioDriver.getDriverNames();
			int ind = soundCardParameters.deviceNumber;
			if (ind < driverNames.size()) {
				return driverNames.get(ind);
			}
			else {
				return "ASIO Name unavailable (index out of range)";
			}
		}
		catch (AsioException e) {
			return "Unknown ASIO device";
		}
	}

	@Override
	public String getSystemType() {
		return sysType;
	}

	@Override
	public boolean isRealTime() {
		return true;
	}

	@Override
	public boolean prepareSystem(AcquisitionControl daqControl) {
//		System.out.println("Preapare ASIO input");
		inputChannelList = acquisitionControl.acquisitionParameters.getHardwareChannelList();
		channelSamples = new long[inputChannelList.length];
		newDataUnits = daqControl.getDaqProcess().getNewDataQueue();
		return true;
	}

	private boolean preparePlayback(PlaybackControl playbackControl,
			int nChannels, float sampleRate) {
		System.out.println("Prepare ASIO playback");
		prepareOutput = true;
		return true;
	}

	@Override
	public boolean startSystem(AcquisitionControl daqControl) {
//		System.out.println("Start ASIO");
		try {
			List<String> driverNames = AsioDriver.getDriverNames();
			currentDriver = AsioDriver.getDriver(driverNames.get(soundCardParameters.deviceNumber));
			if (currentDriver == null) {
				return false;
			}
			HashSet<AsioChannel> activeChannels = new HashSet<AsioChannel>();
			inputChannelList = acquisitionControl.acquisitionParameters.getHardwareChannelList();
			inputChannelList = Arrays.copyOfRange(inputChannelList, 0, acquisitionControl.acquisitionParameters.nChannels);
			inputChannels = new AsioChannel[inputChannelList.length];
			for (int i = 0; i < inputChannelList.length; i++) {
				inputChannels[i] = currentDriver.getChannelInput(inputChannelList[i]);
				activeChannels.add(inputChannels[i]);
			}
			if (prepareOutput) {
				int nOutput = Math.min(currentDriver.getNumChannelsOutput(), inputChannelList.length);
				outputChannelList = new int[nOutput];
				for (int i = 0; i < nOutput; i++) {
					outputChannelList[i] = i;
				}
				outputChannels = new AsioChannel[nOutput];
				for (int i = 0; i < nOutput; i++) {
					outputChannels[i] = currentDriver.getChannelOutput(outputChannelList[i]);
					activeChannels.add(outputChannels[i]);
				}			
			}
			currentDriver.addAsioDriverListener(new AsioCallback());
			currentDriver.setSampleRate(acquisitionControl.acquisitionParameters.sampleRate);
			currentDriver.createBuffers(activeChannels);
			prefBufferSize = currentDriver.getBufferPreferredSize();
			prepareFlowBuffers(prefBufferSize);
			currentDriver.start();
		}
		catch (AsioException e) {
			System.out.println("Cannot prepare ASIO DAQ system: " + e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Prepare the buffers which will actually transfer data out of the ASIO device 
	 * and build it up into big enough blocks to be added to RawDataUnits. 
	 * @param buffSize size of buffers in samples. 
	 */
	private void prepareFlowBuffers(int buffSize) {
		tempFloatBuffer = new float[buffSize];
		int blockSize = buffSize * 2;
		dataBlockSize = (int) (acquisitionControl.acquisitionParameters.sampleRate / 10);
		dataBlockSize /= blockSize;
		dataBlockSize *= blockSize;
		dataBlockSize = Math.max(dataBlockSize, blockSize);
		newDataBlocks = new double[inputChannelList.length][];
		dataBlockPos = new int[inputChannelList.length];
		createDataBlockArrays();
	}

	/**
	 * Create new double arrays to write new data into
	 * and reset indexes to start of each.
	 */
	private void createDataBlockArrays() {
		for (int i = 0; i < newDataBlocks.length; i++) {
			newDataBlocks[i] = new double[dataBlockSize];
			dataBlockPos[i] = 0;
		}
	}

	@Override
	public void stopSystem(AcquisitionControl daqControl) {
		if (currentDriver == null) {
			return;
		}
		try {
			currentDriver.stop();
		}
		catch (IllegalStateException e) {
			// TODO: handle exception
		}
		catch (AsioException e) {
			System.out.println("Error stopping ASIO: " + e.getMessage());
		}
	}

	/**
	 * Callback from ASIO system. 
	 * May as well use the locally stored list of channels since it's easier 
	 * to find the right one that way. 
	 * @param activeChannels
	 */
	public void switchBuffers(Set<AsioChannel> activeChannels) {
		AsioChannel aChannel;
		RawDataUnit rawDataUnit;
		float f;
		for (int i = 0; i < inputChannels.length; i++) {
			aChannel = inputChannels[i];
			aChannel.read(tempFloatBuffer);
			for (int iSamp = 0; iSamp < prefBufferSize; iSamp++) {
				f = tempFloatBuffer[iSamp];
				/**
				 * This line is required to fix a bug in the unpacking of 24 bit data
				 * from the TASCAM US16x08 multi channel sound card. Seems like
				 * the developers have messed up their signed and unsigned ints
				 * and the card is returning values between 0 and 2 instead of -1 and +1.  
				 */
				f = f > 1.0 ? f-2.f : f;
				newDataBlocks[i][dataBlockPos[i]++] = f;
			}
			if (dataBlockPos[i] == dataBlockSize) {
				long millis = acquisitionControl.getAcquisitionProcess().absSamplesToMilliseconds(channelSamples[i]);
				rawDataUnit = new RawDataUnit(millis, 1<<i, channelSamples[i], dataBlockSize);
				rawDataUnit.setRawData(newDataBlocks[i], true);
				channelSamples[i] += dataBlockSize;
				newDataBlocks[i] = new double[dataBlockSize];
				dataBlockPos[i] = 0;
				newDataUnits.addNewData(rawDataUnit, i);
//				System.out.println("New raw data at " + PamCalendar.formatTime(millis) + " Have " + newDataUnits.size());
			}
		}
		try {
		RawDataUnit[] playData = newAsioPlayback.getCurrentUnits(false);
		if (playData != null && playData[0] == null) {
			playData = newAsioPlayback.getCurrentUnits(true);
		}
		if (playData != null) {
			int nPlay = Math.min(playData.length, outputChannelList.length);
			if (nPlay > 0) {
				double[] playArray = null;
				for (int i = 0; i < nPlay; i++) {
					if (playData[i] == null) {
						continue;
					}
					playArray = playData[i].getRawData();
					for (int iSamp = 0; iSamp < prefBufferSize; iSamp++) {
						tempFloatBuffer[iSamp] = (float) playArray[currentPlayPos[i]++];
					}
					outputChannels[i].write(tempFloatBuffer);
//					System.out.println("Wrtite to sound output");
				}
				if (playArray == null || (playArray !=null && currentPlayPos[0] == playArray.length)) {
					playData = newAsioPlayback.getCurrentUnits(true);
				}
			}
		}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

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
		return "New ASIO Sound System";
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
//		if (PamSettingManager.getInstance().isSettingsUnit(this, pamControlledUnitSettings)) {
		try {
			soundCardParameters  = ((SoundCardParameters) pamControlledUnitSettings.getSettings()).clone();
			return true;
		}
		catch (ClassCastException e) {
			e.printStackTrace();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see Acquisition.DaqSystem#supportsChannelLists()
	 */
	@Override
	public boolean supportsChannelLists() {
		return true;
	}

	class AsioCallback implements AsioDriverListener {

		@Override
		public void bufferSizeChanged(int bufferSize) {
			//			System.out.println("bufferSizeChanged");
		}
		@Override
		public void bufferSwitch(long sampleTime, long samplePosition,
				Set<AsioChannel> activeChannels) {
			switchBuffers(activeChannels);
		}
		@Override
		public void latenciesChanged(int inputLatency, int outputLatency) {
			//			System.out.println("latenciesChanged");

		}
		@Override
		public void resetRequest() {
			System.out.println("ASIO resetRequest");
		}
		@Override
		public void resyncRequest() {
			System.out.println("ASIO resyncRequest");
		}
		@Override
		public void sampleRateDidChange(double sampleRate) {
			//			System.out.println("sampleRateDidChange");			
		}
	}

	/* (non-Javadoc)
	 * @see Acquisition.DaqSystem#getPlaybackSystem()
	 */
	@Override
	public PlaybackSystem getPlaybackSystem(PlaybackControl playbackControl, DaqSystem daqSystem) {
		return newAsioPlayback;
	}

	class NewAsioPlayback extends PlaybackSystem {

		private volatile RawDataUnit[] nextUnits, currentUnits;

		private int[] channelList;
		
		private Object synchObject;
		
		private synchronized RawDataUnit[] getCurrentUnits(boolean switchNext) {
			if (currentUnits == null || switchNext) {
				currentUnits = nextUnits;
				nextUnits = null;
				if (currentUnits != null) {
					for (int i = 0; i < currentUnits.length; i++) {
						currentPlayPos[i] = 0;
					}
				}
			}
//			if (currentUnits != null) System.out.println("Got non null units " + 
//					currentUnits.toString() + " / " + currentUnits[0] + " switch = " + switchNext);
			return currentUnits;
		}
		
		@Override
		public PlaybackDialogComponent getDialogComponent() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getMaxChannels() {
			// TODO Auto-generated method stub
			return PamConstants.MAX_CHANNELS;
		}

		@Override
		public String getName() {
			return "ASIO Playback";
		}

		@Override
		public synchronized boolean playData(RawDataUnit[] data, double gain) {
			if (data != null && data.length > 0 && data[0] != null) {
				nextUnits = Arrays.copyOf(data, data.length);
			}
			return true;
		}

		@Override
		public synchronized boolean prepareSystem(PlaybackControl playbackControl,
				int nChannels, float sampleRate) {
			nextUnits = null;
			currentUnits = null;
			PlaybackParameters playbackParameters = playbackControl.getPlaybackParameters();
//			channelList = new int[PamConstants.MAX_CHANNELS];
//			int nFound = 0;
//			for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
//				if ((playbackParameters.channelBitmap & 1<<i) != 0) {
//					channelList[i] = nFound++;
//				}
//				else {
//					channelList[i] = -1;
//				}
//			}
			for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
				currentPlayPos[i] = 0;
			}
			return preparePlayback(playbackControl, nChannels, sampleRate);
		}


		@Override
		public boolean unPrepareSystem() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public PlaybackSettingsPane getSettingsPane() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	/* (non-Javadoc)
	 * @see Acquisition.DaqSystem#getSampleBits()
	 */
	@Override
	public int getSampleBits() {
		return 24;
	}
}
