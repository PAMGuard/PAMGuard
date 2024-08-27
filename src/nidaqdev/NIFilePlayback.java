package nidaqdev;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamView.dialog.PamDialogPanel;
import soundPlayback.FilePlayback;
import soundPlayback.FilePlaybackDevice;
import soundPlayback.PlayDeviceState;
import soundPlayback.PlaybackParameters;

/**
 * PArt of the system for playback from wav files. The wav file playback system
 * has a number of subsystems, currently including sound card and NI card outputs. 
 * <br>This is NOT the class used to play back data actually acquired using NI cards. 
 * @author Doug Gillespie
 *
 */
public class NIFilePlayback implements FilePlaybackDevice, PamSettings {

	
	private volatile boolean prepared;
	
	private FilePlayback filePlayback;
	
	/**
	 * converts usable NI devices (ones with playback channels)
	 * to numbers used by the NI interface to identify the different cards
	 * on the system .
	 */
	private int[] niDeviceLUT = new int[0];
	
	private String[] niDeviceNames = new String[0];
	
	private Nidaq niDaq;
	
	private ArrayList<NIDeviceInfo> deviceInfo;

	private NIDeviceInfo currentDeviceInfo;
	
	private NIPlaybackSettingsPanel playSettingsPanel;
	
	private NIFilePlaybackParams niFilePlaybackParams = new NIFilePlaybackParams();
	
	public NIFilePlayback(FilePlayback filePlayback) {
		super();
		this.filePlayback = filePlayback;
		PamSettingManager.getInstance().registerSettings(this);
		niDaq = new Nidaq();
		getNIDevices();
	}
	
	private void getNIDevices() {
		deviceInfo = niDaq.getDevicesList();
		int nDevs = 0;
		for (int i = 0; i < deviceInfo.size(); i++) {
			if (deviceInfo.get(i).getOutputChannels() > 0) {
				niDeviceNames = Arrays.copyOf(niDeviceNames, niDeviceNames.length+1);
				niDeviceLUT = Arrays.copyOf(niDeviceLUT, niDeviceLUT.length+1);
				niDeviceNames[nDevs] = deviceInfo.get(i).getName();
				if (deviceInfo.get(i).isSimulated()) {
					niDeviceNames[nDevs] += " (simulated)";
				}
				if (!deviceInfo.get(i).isExists()) {
					niDeviceNames[nDevs] += " (not present)";
				}
				niDeviceLUT[nDevs] = i;
				nDevs++;
			}
		}
		
	}

	@Override
	public String[] getDeviceNames() {
		return niDeviceNames;
	}

	@Override
	public String getName() {
		return "National Instruments Devices";
	}

	@Override
	public int getNumPlaybackChannels(int devNum) {
		if (devNum < 0) return 0;
		if (niDeviceLUT == null || devNum >= niDeviceLUT.length) {
			return 0;
		}
		return deviceInfo.get(niDeviceLUT[devNum]).getOutputChannels();
	}

	@Override
	synchronized public boolean playData(RawDataUnit[] data) {
		if (!prepared) {
			return false;
		}
		double[] dataBuffer;
		int buffLen = (int) (data.length * data[0].getSampleDuration());
		dataBuffer = new double[buffLen];
		int n = 0;
		int samps;
		RawDataUnit aDataUnit;
		double[] unitData;
		for (int iB = 0; iB < data.length; iB++) {
			aDataUnit = data[iB];
			samps = aDataUnit.getSampleDuration().intValue();
			unitData = aDataUnit.getRawData();
			for (int i = 0; i < samps; i++) {
				dataBuffer[n++] = unitData[i];
			}
		}
		
//		long startNanos = System.nanoTime();
		int nSamps =  niDaq.javaPlaybackData(dataBuffer);
//		long endNanos = System.nanoTime() - startNanos;
//		System.out.println(String.format("%d NI Samples written for playback in %dus",
//				nSamps, endNanos/1000));
		return true;
	}

	@Override
	public boolean preparePlayback(PlaybackParameters playbackParameters) {
		if (niDeviceLUT == null || niDeviceLUT.length <= playbackParameters.deviceNumber
				|| playbackParameters.deviceNumber < 0) {
			return false;
		}
		int bn = niDeviceLUT[playbackParameters.deviceNumber];
		currentDeviceInfo = deviceInfo.get(bn);
		bn = deviceInfo.get(bn).getDevNumber();

		int nChans = PamUtils.getNumChannels(playbackParameters.channelBitmap);
		int[] outchans = new int[nChans];
		for (int i = 0; i < nChans; i++) {
			outchans[i] = i;
		}

		int playRate = (int) playbackParameters.getPlaybackRate();
		int ans = niDaq.javaPreparePlayback(bn, playRate, playRate, outchans, (float) niFilePlaybackParams.outputRange);
//		System.out.println("NI Answer = " + ans);
		prepared = (ans == 0);
		return prepared;
	}

	@Override
	synchronized public boolean stopPlayback() {
		boolean ok = prepared;
		if (prepared) {
			ok = niDaq.javaStopPlayback();
		}
		prepared = false;
		return ok;
	}

	@Override
	public PlayDeviceState getDeviceState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDeviceName() {
		if (currentDeviceInfo == null) {
			return getName();
		}
		else {
			return currentDeviceInfo.getName();
		}
	}

	@Override
	public PamDialogPanel getSettingsPanel() {
		if (playSettingsPanel == null) {
			playSettingsPanel = new NIPlaybackSettingsPanel(this);
		}
		return playSettingsPanel;
	}

	/**
	 * @return the niFilePlaybackParams
	 */
	public NIFilePlaybackParams getNiFilePlaybackParams() {
		return niFilePlaybackParams;
	}

	/**
	 * @param niFilePlaybackParams the niFilePlaybackParams to set
	 */
	public void setNiFilePlaybackParams(NIFilePlaybackParams niFilePlaybackParams) {
		this.niFilePlaybackParams = niFilePlaybackParams;
	}

	@Override
	public String getUnitName() {
		return "NIFilePalybackSettings";
	}

	@Override
	public String getUnitType() {
		return "NIFilePalybackSettings";
	}

	@Override
	public Serializable getSettingsReference() {
		return niFilePlaybackParams;
	}

	@Override
	public long getSettingsVersion() {
		return NIFilePlaybackParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		niFilePlaybackParams = (NIFilePlaybackParams) pamControlledUnitSettings.getSettings();
		return true;
	}

	/**
	 * @return the currentDeviceInfo
	 */
	public NIDeviceInfo getCurrentDeviceInfo() {
		return currentDeviceInfo;
	}

}
