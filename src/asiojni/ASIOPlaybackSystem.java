package asiojni;

import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackParameters;
import soundPlayback.PlaybackSystem;
import soundPlayback.fx.PlaybackSettingsPane;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamguardMVC.PamConstants;


public class ASIOPlaybackSystem extends PlaybackSystem {

	ASIOSoundSystem asioSoundSystem;
	
	private int channelList[];
	
	public ASIOPlaybackSystem(ASIOSoundSystem asioSoundSystem) {
		super();
		this.asioSoundSystem = asioSoundSystem;
	}

	@Override
	public soundPlayback.swing.PlaybackDialogComponent getDialogComponent() {
		return null;
//		return new ASIOPlaybackDialogComponent(asioSoundSystem, this);
	}

	@Override
	public int getMaxChannels() {
		return 2;
	}

	@Override
	public boolean playData(RawDataUnit[] data, double gain) {
		/*
		 * If this gets one RDU at a time, then we'll need to stack them 
		 * up so that multiple channels are available for output to 
		 * ASIO card at the same time. 
		 */
//		System.out.println("Data hitting playData");
		int outputChannel;
		for (int i = 0; i < data.length; i++) {
			outputChannel = channelList[PamUtils.getSingleChannel(data[i].getChannelBitmap())];
			if (outputChannel >= 0 && outputChannel <= getMaxChannels()) {
				asioSoundSystem.playData(outputChannel, data[i], gain);
			}
		}
		return true;
	}

	@Override
	public boolean prepareSystem(PlaybackControl playbackControl, int channels, float sampleRate) {
//		System.out.println("ASIOPlaybackSystem prepareSystem");
		if (channels > getMaxChannels()) {
			return false;
		}
		/* 
		 * Make a LUT to convert from the received channel to 
		 * 0 or 1 for the output. 
		 */
		PlaybackParameters playbackParameters = playbackControl.getPlaybackParameters();
		channelList = new int[PamConstants.MAX_CHANNELS];
		int nFound = 0;
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if ((playbackParameters.channelBitmap & 1<<i) != 0) {
				channelList[i] = nFound++;
			}
			else {
				channelList[i] = -1;
			}
		}
		
		return asioSoundSystem.preparePlayback(channels, sampleRate);
	}

	@Override
	public boolean unPrepareSystem() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		return asioSoundSystem.getSystemName();
	}

	@Override
	public PlaybackSettingsPane getSettingsPane() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
