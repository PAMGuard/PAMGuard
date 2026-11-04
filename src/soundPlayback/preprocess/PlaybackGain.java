package soundPlayback.preprocess;

import PamDetection.RawDataUnit;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackParameters;
import soundPlayback.fx.PlayGainSidePane;
import soundPlayback.swing.PlayGainSideBar;

public class PlaybackGain implements PlaybackPreprocess {
	
	private PlaybackControl playbackControl;
	
	private PlayGainSideBar playGainSideBar;
	
	private double gainFactor;

	/**
	 * Silence the output without changing the gain. Deliberately not
	 * stored in the playback parameters so playback is never muted at startup.
	 */
	private boolean muted;

	/**
	 * The play gain side pane. 
	 */
	private PlayGainSidePane playGainSidePane;

	public PlaybackGain(PlaybackControl playbackControl) {
		this.playbackControl = playbackControl;
		playGainSideBar = new PlayGainSideBar(this);
	}

	@Override
	public void reset(double inputSampleRate, int channelMap) {
		// TODO Auto-generated method stub
	}

	@Override
	public RawDataUnit processDataUnit(RawDataUnit inputDataUnit, boolean mustCopy) {
		if (inputDataUnit == null) {
			return null;
		}
		double[] scaleData;
		RawDataUnit retUnit;
		if (mustCopy) {
			retUnit = new RawDataUnit(inputDataUnit.getTimeMilliseconds(), inputDataUnit.getChannelBitmap(), inputDataUnit.getStartSample(), inputDataUnit.getSampleDuration());
			scaleData = inputDataUnit.getRawData().clone();
			retUnit.setRawData(scaleData);
		}
		else {
			retUnit = inputDataUnit;
			scaleData = inputDataUnit.getRawData();
		}
		double scaleFactor = muted ? 0. : gainFactor;
		for (int i = 0; i < scaleData.length; i++) {
			scaleData[i] *= scaleFactor;
		}
		retUnit.setRawData(scaleData, true);
		return retUnit;
	}

	/**
	 * Set the gain in decibels. 
	 * @param gaindB gain in decibels.
	 */
	public void setGaindB(double gaindB) {
		gainFactor = Math.pow(10., gaindB/20.);
		playbackControl.getPlaybackParameters().playbackGain = (int) gaindB;
	}
	
	/**
	 * Get the gain in decibels. 
	 * @return gain in decibels.
	 */
	public double getGaindB() {
		return playbackControl.getPlaybackParameters().playbackGain;
	}

	/**
	 * Mute or unmute the output. The gain setting is left unchanged.
	 * @param muted true to silence the output.
	 */
	public void setMuted(boolean muted) {
		this.muted = muted;
	}

	/**
	 * @return true if the output is muted.
	 */
	public boolean isMuted() {
		return muted;
	}

	@Override
	public PreprocessSwingComponent getSideParComponent() {
		playGainSideBar.getComponent().setVisible(playbackControl.getPlaybackParameters().isSideBarShow(PlaybackParameters.SIDEBAR_SHOW_GAIN));
		return playGainSideBar;
	}

	@Override
	public boolean isActive() {
		return muted || gainFactor != 1.;
	}

	public String getTextValue() {
		int g = playbackControl.getPlaybackParameters().playbackGain;
		return String.format("Gain %d dB", g);
		
	}

	@Override
	public PreProcessFXPane getSideParPane() {
		if (playGainSidePane == null) {
			playGainSidePane = new PlayGainSidePane(this);
		}
		return playGainSidePane;
	}

}
