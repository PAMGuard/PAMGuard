package soundPlayback;

import java.util.ArrayList;

import javax.sound.sampled.Mixer.Info;

import Acquisition.SoundCardSystem;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamView.dialog.PamDialogPanel;

public class SoundCardFilePlayback implements FilePlaybackDevice {

	private FilePlayback filePlayback;

	private String[] soundCardNames;
	
	private PlaybackControl playbackControl;
	
	private SoundCardPlaybackBase soundCardPlaybackBase;

	public SoundCardFilePlayback(FilePlayback filePlayback) {
		super();
		this.filePlayback = filePlayback;
		playbackControl = filePlayback.getPlaybackControl();
		
		soundCardPlaybackBase = new SoundCardPlaybackBase();

		ArrayList<Info> mixers = SoundCardSystem.getOutputMixerList();
		soundCardNames = new String[mixers.size()];
		for (int i = 0; i < mixers.size(); i++) {
			soundCardNames[i] = mixers.get(i).getName();
		}
	}

	@Override
	public String getName() {
		return soundCardPlaybackBase.getName();
	}

	@Override
	public String getDeviceName() {
		return soundCardPlaybackBase.getDeviceName();
	}
	
	@Override
	public String[] getDeviceNames() {
		return soundCardNames;
	}

	@Override
	public int getNumPlaybackChannels(int devNum) {
		return 2;
	}

	@Override
	public synchronized boolean playData(RawDataUnit[] data) {
		return soundCardPlaybackBase.playData(data);
	}
	

	@Override
	public synchronized boolean preparePlayback(PlaybackParameters playbackParameters) {
		if (playbackParameters.channelBitmap == 0) {
			return false;
		}
		int nChan = PamUtils.getNumChannels(playbackParameters.channelBitmap);
		return soundCardPlaybackBase.preparePlayback(playbackParameters.deviceNumber, 
				nChan, playbackParameters.getPlaybackRate(), playbackControl.isRealTimePlayback());
	}

	@Override
	public synchronized boolean stopPlayback() {
		return soundCardPlaybackBase.stopPlayback();
	}
	

	@Override
	public PlayDeviceState getDeviceState() {
		return soundCardPlaybackBase.getDeviceState();
	}

	@Override
	public PamDialogPanel getSettingsPanel() {
		// TODO Auto-generated method stub
		return null;
	}

}
