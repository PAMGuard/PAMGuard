package asiojni;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriver;
import com.synthbot.jasiohost.AsioDriverListener;
import com.synthbot.jasiohost.AsioDriverState;
import com.synthbot.jasiohost.AsioException;
import com.synthbot.jasiohost.AsioSampleType;

import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamguardMVC.PamConstants;
import soundPlayback.FilePlayback;
import soundPlayback.FilePlaybackDevice;
import soundPlayback.PlayDeviceState;
import soundPlayback.PlaybackParameters;

public class ASIOFilePlaybackSystem implements FilePlaybackDevice {

	private FilePlayback filePlayback;

	String[] deviceNames = null;

	private AsioDriver currentDriver = null;
	
	private Set<AsioChannel> activeChannels = null;
	
	private AsioChannel[] asioChannels;
	
	private Object dataSynchObject = new Object();
	
	private volatile RawDataUnit[] currentData, nextData;
	
	private int[] currentDataPos;
	
	private float[][] floatBuffer;

//	private double currentGain = 1.;

	public ASIOFilePlaybackSystem(FilePlayback filePlayback) {
		this.filePlayback = filePlayback;
//		SummariseDevices();
	}

	@Override
	public String[] getDeviceNames() {
		if (deviceNames == null) {
			try {
				List<String> driverNameList = AsioDriver.getDriverNames();
				deviceNames = new String[driverNameList.size()];
				for (int i = 0; i < driverNameList.size(); i++) {
					deviceNames[i] = driverNameList.get(i);
				}
			}
			catch (AsioException asioException) {
				System.out.println("ASIO PlaybackError: " + asioException.getMessage());
				return null;
			}
			catch (NullPointerException npException) {
				System.out.println("ASIO PlaybackError: " + npException.getMessage());
				return null;
			}
		}
		return deviceNames;
	}

	@Override
	public int getNumPlaybackChannels(int devNum) {
		return PamConstants.MAX_CHANNELS;
//		String[] devNames = getDeviceNames();
//		if (devNames == null) {
//			return 0;
//		}
//		AsioDriver asioDriver = AsioDriver.getDriver(devNames[devNum]);
//		return asioDriver.getNumChannelsOutput();
	}

	@Override
	public boolean preparePlayback(PlaybackParameters playbackParameters) {
		String[] devNames = getDeviceNames();
		if (devNames == null) {
			return false;
		}
		if (devNames.length <= playbackParameters.deviceNumber) {
			System.out.println("ASIO Playback error: Invalid device number: " + playbackParameters.deviceNumber);
			return false;
		}
		currentData = nextData = null;
		int nChan = PamUtils.getNumChannels(playbackParameters.channelBitmap);
		currentDataPos = new int[nChan];
		floatBuffer = new float[nChan][];
		try {
			currentDriver = AsioDriver.getDriver(devNames[playbackParameters.deviceNumber]); // load the names ASIO driver
			currentDriver.shutdownAndUnloadDriver();
			currentDriver = AsioDriver.getDriver(devNames[playbackParameters.deviceNumber]);
//			currentDriver.returnToState(AsioDriverState.LOADED);
			activeChannels = new HashSet<AsioChannel>(); // create a Set of AsioChannels, defining which input and output channels will be used
			asioChannels = new AsioChannel[nChan];
			AsioSampleType sampleType;
			for (int i = 0; i < nChan; i++) {
				activeChannels.add(asioChannels[i] = currentDriver.getChannelOutput(i)); // configure the ASIO driver to use the given channels
				sampleType = asioChannels[i].getSampleType();
				System.out.println("Channel sample type = " + sampleType);
				floatBuffer[i] = new float[currentDriver.getBufferPreferredSize()];
			}
//			currentDriver.
			currentDriver.addAsioDriverListener(new ASIOCallback());
			currentDriver.setSampleRate(playbackParameters.getPlaybackRate());
			int buffSize = currentDriver.getBufferPreferredSize();
			System.out.println("ASIO Buffer preferred size = " + buffSize);
//			currentDriver.disposeBuffers();
			currentDriver.createBuffers(activeChannels); // create the audio buffers and prepare the driver to run
			currentDriver.start(); // start the driver
		}
		catch (AsioException asioException) {
			System.out.println("ASIO PlaybackError: " + asioException.getMessage());
			return false;
		}
		catch (NullPointerException npException) {
			System.out.println("ASIO PlaybackError: " + npException.getMessage());
			return false;
		}
		catch (IndexOutOfBoundsException oobEx) {
			System.out.println("ASIO PlaybackError: " + oobEx.getMessage());
			return false;
		}
		return true;
	}
	

	public void SummariseDevices() {
		String[] devNames = getDeviceNames();
		for (int i = 0; i < devNames.length; i++) {
			summariseDevice(devNames[i]);
		}
	}
	
	public void summariseDevice(String deviceName) {
		AsioDriver asioDriver = AsioDriver.getDriver(deviceName); // load the names ASIO driver
		int nChanIn = asioDriver.getNumChannelsInput();
		int nChanOut = asioDriver.getNumChannelsOutput();
		String name = asioDriver.getName();
		int buffPref = asioDriver.getBufferPreferredSize();
		int buffMin = asioDriver.getBufferMinSize();
		int buffMax = asioDriver.getBufferMaxSize();
		String str = String.format("ASIO Drvier %s/%s, in: %d; out: %d, buff min: %d, pref: %d, max %d", 
				name, deviceName, nChanIn, nChanOut, buffMin, buffPref, buffMax);
		System.out.println(str);
	}

	class ASIOCallback implements AsioDriverListener {

		int callCount = 0;
		@Override
		public void bufferSizeChanged(int bufferSize) {
//			System.out.println("bufferSizeChanged");
		}

		@Override
		public void bufferSwitch(long sampleTime, long samplePosition,
				Set<AsioChannel> activeChannels) {
			/*
			 * This is coming in in a different thread to the main calls from 
			 * playData, so not a problem to read the data queue directly here
			 * and pack the data into the buffers. 
			 * This function should return the buffers immediately. If there is data
			 * available, then fill them, if not just return them empty or half full.  
			 */
//			if (++callCount < 20) {
//			System.out.println(String.format("Buffer switch for %d channels in thread %d", 
//					activeChannels.size(), Thread.currentThread().getId()));
//			}
			switchBuffers(activeChannels);
		}

		@Override
		public void latenciesChanged(int inputLatency, int outputLatency) {
//			System.out.println("latenciesChanged");
			
		}

		@Override
		public void resetRequest() {
//			System.out.println("resetRequest");
		}

		@Override
		public void resyncRequest() {
//			System.out.println("resyncRequest");
		}

		@Override
		public void sampleRateDidChange(double sampleRate) {
//			System.out.println("sampleRateDidChange");			
		}
		
	}
	
	@Override
	public boolean stopPlayback() {
		if (currentDriver == null) {
			return false;
		}
		try { 
			currentDriver.stop();
			currentDriver.shutdownAndUnloadDriver();
		}
		catch (IllegalStateException e) {
			
		}
		catch (AsioException ae) {
			
		}
		currentDriver = null;
		return true;
	}

	@Override
	public String getName() {
		return "ASIO Devices";
	}
	
	@Override
	public String getDeviceName() {
		return getName();
	}

	@Override
	public boolean playData(RawDataUnit[] data) {
		/*
		 * Byte buffer capacity seems to be four times the currentDriver.getBufferPreferredSize()
		 * and the channels are claiming they want 32 bit signed data - so that all makes sense. 
		 * All we have to do now is to set up a blocking data queue here, so that the arrival of 
		 * data to playback will hold execution. Have a different thread emptying the queue and 
		 * filling the buffers returned 
		 */
//		System.out.println("Play data in thread " + Thread.currentThread().getId());
		
//		int nChan = data.length;
//		int iChan;
//		ByteBuffer byteBuffer;
//		for (int i = 0; i < nChan; i++) {
//			iChan = PamUtils.getSingleChannel(data[i].getChannelBitmap());
//			 byteBuffer = asioChannels[i].getByteBuffer();
//			 if (byteBuffer == null) {
//				 System.out.println("Null byte buffer for channel " + iChan);
//			 }
//			 else {
//				 int cap = byteBuffer.capacity();
//				 System.out.println("Byte buffer for channel capacity = " + cap);
//			 }
//		}
//		activeChannels.
		// this function needs to block, so go into a wait state until nextData is null
		while (currentDriver != null && nextData != null && PamController.getInstance().getPamStatus() != PamController.PAM_IDLE) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		nextData = data;
		return true;
	}

	/**
	 * Called from the ASIOCallback.bufferSwitch();
	 * @param activeChannels2
	 */
	public void switchBuffers(Set<AsioChannel> activeChannels) {
		int nChan = activeChannels.size();
		RawDataUnit[] data = getCurrentData(false);
		if (data == null) {
			return;
		}
		int dataChans = Math.min(floatBuffer.length, data.length);
		int availSamps = 0;
		double[] rawData;
		for (int i = 0; i < dataChans; i++) {
			if (data[i] == null) {
				return;
			}
			rawData = data[i].getRawData();
			availSamps = rawData.length - currentDataPos[i];
			availSamps = Math.min(availSamps, floatBuffer[i].length);
			for (int iSamp = 0; iSamp < availSamps; iSamp++) {
				floatBuffer[i][iSamp] = (float) (rawData[currentDataPos[i]++]);
			}
		}
		int neededSamps = floatBuffer[0].length - availSamps;
		if (neededSamps > 0) {
			data = getCurrentData(true);
			if (data != null) {
				for (int i = 0; i < dataChans; i++) {
					rawData = data[i].getRawData();
					neededSamps = Math.min(neededSamps, rawData.length);
					for (int iSamp = 0; iSamp < neededSamps; iSamp++) {
						floatBuffer[i][iSamp+availSamps] = (float) rawData[currentDataPos[i]++];
					}
				}
			}
		}
		int iChan;
		// channels not guaranteed to return in right order, so check their
		// index. 
		for (AsioChannel aChannel:activeChannels) {
			iChan = aChannel.getChannelIndex();
			aChannel.write(floatBuffer[iChan]);	
		}
		
	}

	private RawDataUnit[] getCurrentData(boolean forceNext) {
		if (currentData == null || forceNext) {
			currentData = nextData;
			nextData = null;
			for (int i = 0; i < currentDataPos.length; i++) {
				currentDataPos[i] = 0;
			}
		}
		return currentData;
	}
	
	@Override
	public PlayDeviceState getDeviceState() {
		// TODO Auto-generated method stub
		return null;
	}

}
