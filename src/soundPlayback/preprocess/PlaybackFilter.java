package soundPlayback.preprocess;

import javax.swing.JComponent;

import Filters.Filter;
import Filters.FilterBand;
import Filters.FilterMethod;
import Filters.FilterParams;
import Filters.FilterType;
import PamDetection.RawDataUnit;
import PamUtils.FrequencyFormat;
import PamUtils.PamUtils;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackParameters;
import soundPlayback.swing.PlayFilterSideBar;

/**
 * High pass filters for playback system. 
 * @author dg50
 *
 */
public class PlaybackFilter implements PlaybackPreprocess {

	private PlaybackControl playbackControl;
	
	private FilterParams filterParams;
	
	private Object synchObj = new Object();
	
	private Filter[] filters;
	
	private PlayFilterSideBar playFilterSideBar;

	public PlaybackFilter(PlaybackControl playbackControl) {
		this.playbackControl = playbackControl;
		playFilterSideBar = new PlayFilterSideBar(this);
	}

	@Override
	public void reset(double inputSampleRate, int channelMap) {
		filterParams = new FilterParams();
		filterParams.filterType = FilterType.BUTTERWORTH;
		filterParams.filterBand = FilterBand.HIGHPASS;
		filterParams.filterOrder = 2;
		float filterFreq = (float) playbackControl.getPlaybackParameters().getHpFilter();
		filterParams.highPassFreq = filterFreq;
		if (filterFreq <= 0) {
			filterParams.filterType = FilterType.NONE;
		}
		FilterMethod filterMethod = FilterMethod.createFilterMethod(1., filterParams);
		synchronized (synchObj) {
			int hChan = PamUtils.getHighestChannel(channelMap);
			filters = new Filter[hChan+1];
			for (int i = 0; i <= hChan; i++) {
				if ((1<<i & channelMap) == 0) {
					continue;
				}
				filters[i] = filterMethod.createFilter(i);
			}
		}
	}
	
	public void setValue(double filterValue) {
		playbackControl.getPlaybackParameters().setHpFilter(filterValue);
		reset(playbackControl.getSourceSampleRate(), playbackControl.getPlaybackParameters().channelBitmap);
	}
	
	public double getValue() {
		return playbackControl.getPlaybackParameters().getHpFilter();
	}
	
	public String getTextValue() {
		double f = getValue() * playbackControl.getSourceSampleRate();
		if (f == 0) {
			return "High pass filter off";
		}
		else {
			return "High pass " + FrequencyFormat.formatFrequency(f, true);
		}
	}

	@Override
	public RawDataUnit processDataUnit(RawDataUnit inputDataUnit, boolean mustCopy) {
		RawDataUnit newData;
		double[] scaleData;
		if (mustCopy == false) {
			newData = inputDataUnit;
			scaleData = newData.getRawData();
		}
		else {
			newData = new RawDataUnit(inputDataUnit.getTimeMilliseconds(), inputDataUnit.getChannelBitmap(), inputDataUnit.getStartSample(), inputDataUnit.getSampleDuration());
			scaleData = inputDataUnit.getRawData().clone();
		}
		int chan = PamUtils.getSingleChannel(inputDataUnit.getChannelBitmap());
		synchronized (synchObj) {
			if (filters[chan] != null) {
				filters[chan].runFilter(scaleData);
			}
		}
		newData.setRawData(scaleData, true);
		return newData;
	}

	@Override
	public PreprocessSwingComponent getSideParComponent() {
		playFilterSideBar.getComponent().setVisible(playbackControl.getPlaybackParameters().isSideBarShow(PlaybackParameters.SIDEBAR_SHOW_FILTER));
		return playFilterSideBar;
	}

	@Override
	public boolean isActive() {
		return playbackControl.getPlaybackParameters().getHpFilter() > 0;
	}

	@Override
	public PreProcessFXPane getSideParPane() {
		// TODO Auto-generated method stub
		return null;
	}

}
