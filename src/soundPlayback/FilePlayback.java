package soundPlayback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import asiojni.ASIOFilePlaybackSystem;
import nidaqdev.NIFilePlayback;
import soundPlayback.fx.FilePlaybackSettingsPane;
import soundPlayback.fx.PlaybackSettingsPane;
import soundPlayback.swing.FilePlaybackDialogComponent;
import soundPlayback.swing.PlaybackDialogComponent;
import warnings.QuickWarning;
import Acquisition.DaqSystem;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamModel.SMRUEnable;
import PamUtils.PamCalendar;
import PamUtils.PlatformInfo;
import PamUtils.PlatformInfo.OSType;
import PamguardMVC.debug.Debug;

/**
 * Playback of sound from wav files. 
 * Other real time sound sources must handle their own playback so that 
 * timing of in and out is synchronised correctly. 
 * @author Doug Gillespie
 * @see DaqSystem
 *
 */
public class FilePlayback extends PlaybackSystem {

	private PlaybackControl playbackControl;

	private FilePlaybackDialogComponent filePlaybackDialogComponent;

	private ArrayList<FilePlaybackDevice> filePBDevices = new ArrayList<FilePlaybackDevice>();

	private FilePlaybackDevice currentDevice;

	private boolean realTimePlayback;

	private List<RawDataUnit[]> realTimeQueue;

	private int maxQueueLength;

	private RealTimeQueueReader realTimeQueueReader;

	private QuickWarning quickWarning;

	private Thread realTimeQueueThread;

	private int currentChannels;

	private float currentSampleRate;

	/**
	 * The FX file playback pane. Will be null in swing GUI. 
	 */
	private FilePlaybackSettingsPane filePlayBackPane;

	//	private FilePlaybackSideBar playbackSideBar;

	public FilePlayback(PlaybackControl playbackControl) {
		this.playbackControl = playbackControl;
		quickWarning = new QuickWarning("Sound file playback");
		filePBDevices.add(currentDevice = new SoundCardFilePlayback(this));
		filePBDevices.add(new NIFilePlayback(this));
		if (PlatformInfo.calculateOS() == OSType.WINDOWS) {
			filePBDevices.add(new ASIOFilePlaybackSystem(this));
		}
		filePlaybackDialogComponent = new FilePlaybackDialogComponent(this);
		playbackControl.setSidePanel(null);
		//		if (!playbackControl.isRealTimePlayback() & SMRUEnable.isEnable() ){
		//			this.playbackControl.setSidePanel(playbackSideBar = new FilePlaybackSideBar(this.playbackControl));
		//		} 
	}

	public int getMaxChannels() {
		FilePlaybackDevice device = filePBDevices.get(playbackControl.playbackParameters.deviceType);
		return device.getNumPlaybackChannels(getDeviceNumber());
	}

	public int getDeviceNumber() {
		return playbackControl.playbackParameters.deviceNumber;
	}

	@Override
	synchronized public boolean prepareSystem(PlaybackControl playbackControl,
			int nChannels, float sampleRate) {

		//		Debug.out.println("Preparing playback on " + currentDevice.getName());
		unPrepareSystem();

		currentChannels  = nChannels;
		currentSampleRate = sampleRate;

		realTimePlayback = playbackControl.isRealTimePlayback();
		if (realTimeQueueReader != null) {
			realTimeQueueReader.stopThread();
			try {
				realTimeQueueThread.join(100);
			} catch (InterruptedException e) {
				quickWarning.setWarning(e.getMessage(), 1);
			}
		}
		if (realTimePlayback) {
			realTimeQueue = Collections.synchronizedList(new LinkedList<RawDataUnit[]>());
			maxQueueLength = 10 * nChannels; // hold about a second of data. 
			realTimeQueueReader = new RealTimeQueueReader();
			realTimeQueueThread = new Thread(realTimeQueueReader);
			realTimeQueueThread.start();
		}

		currentDevice = filePBDevices.get(playbackControl.playbackParameters.deviceType);
		return currentDevice.preparePlayback(playbackControl.playbackParameters);


	}

	synchronized public boolean unPrepareSystem() {
		if (realTimePlayback) {
			synchronized (realTimeQueue) {
				realTimeQueue.clear();
			}
		}
		if (currentDevice != null) {
			return currentDevice.stopPlayback();
		}
		return false;
	}

	public boolean playData(RawDataUnit[] data, double gain) {

		if (currentDevice == null) {
			quickWarning.setWarning("currentDevice == null", 2);
			return false;
		}
		if (realTimePlayback) {
			if (realTimeQueue.size() < maxQueueLength) {
				synchronized(realTimeQueue) {
					realTimeQueue.add(data.clone());
				}
			}
			else {
				System.out.println("Dumping playback data since output running too slow for input. Queue size " + 
						realTimeQueue.size() + " " + PamCalendar.formatTime(System.currentTimeMillis(), true));
				realTimeQueue.clear();
			}
			return true;
		}
		else {
			return currentDevice.playData(data.clone());
		}

	}

	class RealTimeQueueReader implements Runnable {

		private volatile boolean keepRunning = true;

		@Override
		public void run() {
			RawDataUnit[] data;
			while (keepRunning) {
				try {
					while (realTimeQueue.size() > 0) {
						//						synchronized (realTimeQueue) {
						//							realTimeQueue.
						//						}
						data = realTimeQueue.remove(0);
						//						double gain = Math.pow(10., playbackControl.playbackParameters.playbackGain/20.);
						boolean ok = currentDevice.playData(data);
						if (ok == false) {
							keepRunning = false;
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									playbackError();
								}
							});
							break;
						}
						//						synchronized(realTimeQueue) {
						//							realTimeQueue.remove(0);
						//						}
					}
				}
				catch (IndexOutOfBoundsException e) {
					// can happen if all data were deleted at just that moment due to queue getting 
					// too large. 
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}


		public void stopThread() {
			keepRunning = false;
		}

	}

	protected void playbackError() {
		Debug.out.println("Playback error reported on " + currentDevice.getName());
		prepareSystem(playbackControl, currentChannels, currentSampleRate);
	}

	public PlaybackDialogComponent getDialogComponent() {
		return filePlaybackDialogComponent;
	}

	@Override
	public String getName() {
		if (currentDevice != null) {
			return currentDevice.getName();
		}
		else {
			return "Playback Device";
		}
	}

	@Override
	public String getDeviceName() {
		if (currentDevice == null) {
			return getName();
		}
		else {
			return currentDevice.getDeviceName();
		}
	}

	public PlaybackControl getPlaybackControl() {
		return playbackControl;
	}

	@Override
	public boolean mustSameIOSampleRate() {
		return false;
	}

	/**
	 * @return the filePBDevices
	 */
	public ArrayList<FilePlaybackDevice> getFilePBDevices() {
		return filePBDevices;
	}

	@Override
	public PlayDeviceState getDeviceState() {
		if (currentDevice == null) {
			return null;
		}
		else {
			return currentDevice.getDeviceState();
		}
	}

	@Override
	public PlaybackSettingsPane getSettingsPane() {
		if (this.filePlayBackPane==null) {
			filePlayBackPane = new FilePlaybackSettingsPane(this); 
		}
		return filePlayBackPane;
	}


}
