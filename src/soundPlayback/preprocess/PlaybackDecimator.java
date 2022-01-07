package soundPlayback.preprocess;

import javax.swing.JComponent;

import Filters.FilterBand;
import Filters.FilterParams;
import Filters.FilterType;
import PamDetection.RawDataUnit;
import decimator.DecimatorWorker;
import soundPlayback.PBSampleRateData;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackParameters;
import soundPlayback.PlaybackProcess;
import soundPlayback.swing.DecimatorSideBar;

public class PlaybackDecimator implements PlaybackPreprocess {
	
	private DecimatorWorker decimatorWorker;
	
	private PBSampleRateData sampleRateData;

	private PlaybackControl playbackControl;
	
	private DecimatorSideBar decimatorSideBar;
	
	public PlaybackDecimator(PlaybackControl playbackControl) {
		this.playbackControl = playbackControl;
		decimatorSideBar = new DecimatorSideBar(this);
	}
	
	@Override
	public void reset(double inputSampleRate, int channelMap) {
		synchronized(this) {
			sampleRateData = playbackControl.getPlaybackProcess().getSampleRateData();
			if (sampleRateData != null) {
				decimatorWorker = new DecimatorWorker(4, channelMap, inputSampleRate, inputSampleRate / sampleRateData.getDecimationFactor());
			}
		}
	}

	@Override
	public RawDataUnit processDataUnit(RawDataUnit inputDataUnit, boolean mustCopy) {
		synchronized(this) {
			return decimatorWorker.process(inputDataUnit);
		}
	}

	@Override
	public PreprocessSwingComponent getSideParComponent() {
//		decimatorSideBar.getComponent().setVisible(playbackControl.getPlaybackParameters().isSideBarShow(PlaybackParameters.SIDEBAR_SHOW_SPEED));
		return decimatorSideBar;
	}

	@Override
	public boolean isActive() {
		return (sampleRateData != null && sampleRateData.getDecimationFactor() != 1.);
	}

	public boolean makeVisible() {
		return (playbackControl.isRealTimePlayback() == false && 
				playbackControl.getPlaybackParameters().isSideBarShow(PlaybackParameters.SIDEBAR_SHOW_SPEED));
	}

	public double getPlaySpeed() {
		return playbackControl.getPlaybackParameters().getPlaybackSpeed();
	}
	
	public void setPlaySpeed(double speed) {
		playbackControl.getPlaybackParameters().setPlaybackSpeed(speed);
		playbackControl.getPlaybackProcess().resetDecimator();
	}

	@Override
	public PreProcessFXPane getSideParPane() {
		// TODO Auto-generated method stub
		return null;
	}
}
