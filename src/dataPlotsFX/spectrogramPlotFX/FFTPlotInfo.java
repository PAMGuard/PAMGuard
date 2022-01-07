package dataPlotsFX.spectrogramPlotFX;

import java.io.Serializable;

import PamController.PamController;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.DataBlock2D;
import PamguardMVC.PamDataBlock;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDSettingsPane;
import dataPlotsFX.scrollingPlot2D.PlotParams2D;
import dataPlotsFX.scrollingPlot2D.Scrolling2DPlotInfo;
import dataPlotsFX.scrollingPlot2D.Scrolling2DScaleInfo;
import dataPlotsFX.scrollingPlot2D.StandardPlot2DColours;
import fftManager.PamFFTControl;
import pamViewFX.fxNodes.utilsFX.ColourArray;

/**
 * Plots spectrogram data. 
 * 
 * @author Jamie Macaulay
 *
 */
public class FFTPlotInfo extends Scrolling2DPlotInfo {

	
	private Scrolling2DScaleInfo freqScaleInfo; // same as ref to yScaleinfo in super class Scrolling2DPlotinfo

	private TDSpectrogramControlPane spectrogramControlPane;

	private SpectrogramParamsFX specParams;
	
	public FFTPlotInfo(TDDataProviderFX tdDataProvider, TDGraphFX tdGraph, PamFFTControl fftControl,
			DataBlock2D pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);
		spectrogramControlPane = new TDSpectrogramControlPane(tdGraph, this);
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.scrollingPlot2D.Scrolling2DPlotInfo#createPlotParams()
	 */
	@Override
	public PlotParams2D createPlotParams() {
		specParams = new SpectrogramParamsFX();
		return specParams;
	}

	@Override
	public Scrolling2DScaleInfo createTDScaleInfo(Scrolling2DPlotInfo scrollingPlotInfo, double minVal, double maxVal) {
		freqScaleInfo = new Scrolling2DScaleInfo(scrollingPlotInfo, minVal, maxVal, ParameterType.FREQUENCY, ParameterUnits.HZ);
		return freqScaleInfo;
	}

	@Override
	public TDSettingsPane getGraphSettingsPane(){
		return spectrogramControlPane;
	}

	boolean stopLoop=false; 
	public void bindPlotParams(){
		if (freqScaleInfo == null) {
			return;
		}
		
		//don;t use binding here to allow params to be changed for zooming etc. 
		freqScaleInfo.minValProperty().addListener((obsVal, oldVal, newVal)->{
			stopLoop=true; 
			if (spectrogramControlPane!=null) spectrogramControlPane.setMinFrequency(newVal.doubleValue());
			stopLoop=false; 
		}); 
		
		freqScaleInfo.maxValProperty().addListener((obsVal, oldVal, newVal)->{
			stopLoop=true;
			if (spectrogramControlPane!=null)  spectrogramControlPane.setMaxFrequency(newVal.doubleValue());
			stopLoop=false; 
		}); 

		
		specParams.getFrequencyLimit(0).addListener((obsVal, oldVal, newVal)->{
			if (!stopLoop) freqScaleInfo.minValProperty().setValue(newVal.doubleValue());
		});
		
		specParams.getFrequencyLimit(1).addListener((obsVal, oldVal, newVal)->{
			if (!stopLoop) freqScaleInfo.maxValProperty().setValue(newVal.doubleValue());
		});
		
//		freqScaleInfo.minValProperty().bind(specParams.getFrequencyLimit(0));
//		freqScaleInfo.maxValProperty().bind(specParams.getFrequencyLimit(1));

		PamDataBlock dataBlock = this.getDataBlock();
		//set the max frequency to Nyquist if zero or really low 
		if (dataBlock != null) {
			if (specParams.getFrequencyLimit(1).get()==0 || specParams.getFrequencyLimit(1).get()<dataBlock.getSampleRate()/100){
				specParams.getFrequencyLimit(1).setValue(dataBlock.getSampleRate()/2);
			}
		}
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.scrollingPlot2D.Scrolling2DPlotInfo#setStoredSettings(java.io.Serializable)
	 */
	@Override
	public boolean setStoredSettings(Serializable storedSettings) {
		boolean ans = super.setStoredSettings(storedSettings);

		if (SpectrogramParamsFX.class.isAssignableFrom(storedSettings.getClass()) == false) {
			return false;
		}
		specParams = (SpectrogramParamsFX) storedSettings;
		
		//setup bindings and listeners. 
		spectrogramControlPane.setFrequencyProperties(specParams.getFrequencyLimits());
		spectrogramControlPane.setAmplitudeProperties(specParams.getAmplitudeLimits(), specParams.getMaxAmplitudeLimits());

		setSpectrogramColours(new StandardPlot2DColours(specParams)); 

		//set correct colours
		spectrogramControlPane.getColorBox().setValue(ColourArray.getName(specParams.getColourMap()));

		
		return ans;
	}

	@Override
	public Serializable getStoredSettings() {
		Serializable ans = super.getStoredSettings();

		specParams.setFrequencyLimitsSerial();
//		specParams.frequencyLimitsSerial[0] = specParams.frequencyLimits[0].getValue();
//		specParams.frequencyLimitsSerial[1] = specParams.frequencyLimits[1].getValue();
		
		return ans;
	}
	
	/**
	 * Set the frequency limits, the maximum and minimum (usually 0) allowed frequencies. . 
	 * @param min
	 */
	private void setFrequencyRange(float sampleRate){
		if (spectrogramControlPane == null) {
			return;
		}
		// will set the max frequency to niquist only if it was at the old niquist or above the new one. 
		double currentMax = specParams.getFrequencyLimit(1).doubleValue();
		double currentRange = spectrogramControlPane.getMaxFrequencyRange();
		boolean setNiquist = (currentMax == currentRange || currentMax > sampleRate/2.); 
//		DoubleProperty[] frequencyLimits = new doublep
		spectrogramControlPane.setMinFrequency(0);
		if (setNiquist) {
			spectrogramControlPane.setMaxFrequency(sampleRate/2.);
			//change so that the high limit covers the whole display. 
			specParams.getFrequencyLimit(1).unbind(); 
			specParams.getFrequencyLimit(1).set(sampleRate/2);
		}

		//now change the control pane.
		if (spectrogramControlPane!=null){
			spectrogramControlPane.setFrequencyRange(specParams.getFrequencyLimits());
			spectrogramControlPane.setFrequencyProperties(specParams.getFrequencyLimits());
		}
	}
	
	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		super.setSampleRate(sampleRate, notify);
		setFrequencyRange(sampleRate);
	}
	/**
	 * Checks the frequency range range to be displayed. 
	 * This is a direct copy of similar fun in the old spec display
	 */
	public void calcFrequencyRangeDisplay(){
		// check the frequency bins ...
//		DataBlock2D sourceFFTDataBlock = getFftDataBlock();
		DataBlock2D dataBlock2D = (DataBlock2D) getDataBlock();
		if (specParams == null || dataBlock2D == null) {
			return;
		}
//		double bin1 = Math.floor(specParams.getFrequencyLimit(0).get()
//						* dataBlock2D.getDataWidth() / dataBlock2D.getMaxDataValue());
//		double bin2 = Math.ceil(specParams.getFrequencyLimit(1).get()
//						* dataBlock2D.getDataWidth() / dataBlock2D.getMaxDataValue());
		/*
		 * Now keep to physical units until the last possible moment
		 */
		setDisplayedDataRange(specParams.getFrequencyLimit(0).get(), specParams.getFrequencyLimit(1).get());
//		float sampleRate = sourceFFTDataBlock.getSampleRate();
//		freqBinRange  [0].setValue( Math
//				.floor(specParams.frequencyLimits[0].get()
//						* dataBlock2D.getDataWidth() / dataBlock2D.getMaxDataValue()));
//		freqBinRange[1].setValue (Math
//				.ceil(specParams.frequencyLimits[1].get()
//						* dataBlock2D.getDataWidth() / dataBlock2D.getMaxDataValue()));

		//System.out.println(String.format("Freq bins range: "+freqBinRange[0]+" "+ freqBinRange[1]));
	}
	
	@Override
	public void finalConfigurationTasks() {
		calcFrequencyRangeDisplay();
		super.finalConfigurationTasks();
	}
	
	/**
	 * Notify of changes from PamController. 
	 * @param changeType - the chnage type. 
	 */
	@Override
	public void notifyChange(int changeType){
		switch (changeType) {
		case PamController.GLOBAL_MEDIUM_UPDATE:
			
			//set new default limits. 
			specParams.maxAmplitudeLimits = PamController.getInstance().getGlobalMediumManager().getDefaultdBHzScales(); 
			spectrogramControlPane.setAmplitudeProperties(specParams.getAmplitudeLimits(), specParams.getMaxAmplitudeLimits());
			
			//System.out.println("FFTPlotInfo: notifyChange:  FFT colour limits: " + specParams.maxAmplitudeLimits[0] +  "  "  +specParams.maxAmplitudeLimits[1]); 

			this.getTDGraph().repaint(0);
			break;
		}
	}
	
	

	
	/**
	 * @return the spectrogramControlPane
	 */
	public TDSpectrogramControlPane getSpectrogramControlPane() {
		return spectrogramControlPane;
	}
	
	@Override
	public long getMasterClockOverride() {
		//be careful here. Use this clock update to make a nicer display end but only if there is a 
		//spectrogram
		if ( this.getDataBlock().getLastUnit()!=null) {
			return this.getDataBlock().getLastUnit().getTimeMilliseconds();
		}
		else return -1; 
	}
}
