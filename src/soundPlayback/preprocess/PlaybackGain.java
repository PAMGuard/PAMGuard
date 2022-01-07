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
		for (int i = 0; i < scaleData.length; i++) {
			scaleData[i] *= gainFactor;
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

	@Override
	public PreprocessSwingComponent getSideParComponent() {
		playGainSideBar.getComponent().setVisible(playbackControl.getPlaybackParameters().isSideBarShow(PlaybackParameters.SIDEBAR_SHOW_GAIN));
		return playGainSideBar;
	}

	@Override
	public boolean isActive() {
		return gainFactor != 1.;
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
