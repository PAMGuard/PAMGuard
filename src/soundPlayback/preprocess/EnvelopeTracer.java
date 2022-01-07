package soundPlayback.preprocess;

import java.awt.event.ActionEvent;
import java.io.Serializable;

import Filters.Filter;
import Filters.FilterDialog;
import Filters.FilterMethod;
import Filters.FilterParams;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackParameters;
import soundPlayback.swing.EnvelopeSideBar;

public class EnvelopeTracer implements PlaybackPreprocess, PamSettings {

	private PlaybackControl playbackControl;
	
	private EnvelopeParams envelopeParams = new EnvelopeParams();
	
	private EnvelopeSideBar envelopeSideBar;
	
	private Filter[] preFilters;
	
	private Filter[] postFilters;
	
	private double[][] filterData;

	public EnvelopeTracer(PlaybackControl playbackControl) {
		this.playbackControl = playbackControl;
		
		envelopeSideBar = new EnvelopeSideBar(this);
		
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public synchronized void reset(double inputSampleRate, int channelMap) {
		prepareFilters();
	}

	private synchronized void prepareFilters() {
		int channelMap = playbackControl.getPlaybackParameters().channelBitmap;
		int highestChan = PamUtils.getHighestChannel(channelMap);
		preFilters = new Filter[highestChan+1];
		postFilters = new Filter[highestChan+1];
		filterData = new double[highestChan+1][];
		double fs = playbackControl.getSourceSampleRate();
		FilterMethod preMethod = FilterMethod.createFilterMethod(fs, envelopeParams.getFirstFilter());
		FilterMethod postMethod = FilterMethod.createFilterMethod(fs, envelopeParams.getSecondFilter());
		for (int i = 0; i <= highestChan; i++) {
			preFilters[i] = preMethod.createFilter(i);
			postFilters[i] = postMethod.createFilter(i);
		}
	}

	@Override
	public synchronized RawDataUnit processDataUnit(RawDataUnit inputDataUnit, boolean mustCopy) {
		double[] rawData = inputDataUnit.getRawData();
		if (mustCopy) {
			inputDataUnit = new RawDataUnit(inputDataUnit.getTimeMilliseconds(), inputDataUnit.getChannelBitmap(), inputDataUnit.getStartSample(), inputDataUnit.getSampleDuration());
			inputDataUnit.setRawData(rawData = rawData.clone());
		}
		if (isActive() == false) {
			return inputDataUnit;
		}
		int chan = PamUtils.getSingleChannel(inputDataUnit.getChannelBitmap());
		if (filterData[chan] == null || filterData[chan].length != rawData.length) {
			filterData[chan] = new double[rawData.length];
		}
		preFilters[chan].runFilter(rawData, filterData[chan]);
		for (int i = 0; i < rawData.length; i++) {
			filterData[chan][i] = Math.abs(filterData[chan][i]);
		}
		postFilters[chan].runFilter(filterData[chan]); // second filter can be done in place. 
		double r1 = Math.pow(1.-envelopeParams.getMixRatio(), .5);
		double r2 = Math.pow(envelopeParams.getMixRatio(), .5);
		for (int i = 0; i < rawData.length; i++) {
			rawData[i] = rawData[i]*r1 + filterData[chan][i]*r2;
		}
		return inputDataUnit;
	}

	@Override
	public PreprocessSwingComponent getSideParComponent() {
		envelopeSideBar.getComponent().setVisible(playbackControl.getPlaybackParameters().isSideBarShow(PlaybackParameters.SIDEBAR_SHOW_ENVMIX));
		return envelopeSideBar;
	}

	@Override
	public boolean isActive() {
		return envelopeParams.getMixRatio() > 0;
	}

	@Override
	public String getUnitName() {
		return playbackControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Envelope Tracer";
	}

	@Override
	public Serializable getSettingsReference() {
		return envelopeParams;
	}

	@Override
	public long getSettingsVersion() {
		return EnvelopeParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		envelopeParams = (EnvelopeParams) pamControlledUnitSettings.getSettings();
		return true;
	}

	public void setMixRatio(double mixRatio) {
		envelopeParams.setMixRatio(mixRatio);
	}

	public double getMixRatio() {
		return envelopeParams.getMixRatio();
	}

	public void preFilterMenu(ActionEvent e) {
		double fs = playbackControl.getSourceSampleRate();
		FilterParams newParams = FilterDialog.showDialog(playbackControl.getGuiFrame(), envelopeParams.getFirstFilter(), (float) fs);
		if (newParams != null) {
			envelopeParams.setFirstFilter(newParams);
		}
		prepareFilters();
	}

	public void postFilterMenu(ActionEvent e) {
		double fs = playbackControl.getSourceSampleRate();
		FilterParams newParams = FilterDialog.showDialog(playbackControl.getGuiFrame(), envelopeParams.getSecondFilter(), (float) fs);
		if (newParams != null) {
			envelopeParams.setSecondFilter(newParams);
		}
		prepareFilters();
	}
	
	public String getStateText() {
		double r = envelopeParams.getMixRatio();
		if (r == 0) {
			return "Waveform only";
		}
		else if (r == 1) {
			return "Envelope only";
		}
		else {
			int percWave = (int) Math.round((1.-r)*100.);
			return String.format("%d%%Wav+%d%%Env'", percWave, 100-percWave);
		}
	}

	@Override
	public PreProcessFXPane getSideParPane() {
		// TODO Auto-generated method stub
		return null;
	}

}
