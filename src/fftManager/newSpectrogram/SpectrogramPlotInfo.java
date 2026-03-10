package fftManager.newSpectrogram;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.Serializable;

import Layout.PamAxis;
import PamController.PamControlledUnit;
import PamUtils.PamUtils;
import PamView.ColourArray;
import PamView.ColourArray.ColourArrayType;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.hidingpanel.HidingDialogChangeListener;
import PamView.hidingpanel.HidingDialogComponent;
import PamguardMVC.LoadObserver;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.dataOffline.OfflineDataLoading;
import Spectrogram.SpectrogramHidingPanel;
import Spectrogram.SpectrogramParameters;
import Spectrogram.SpectrogramParametersUser;
import Spectrogram.SpectrogramParamsDialog;
import dataPlots.TDControl;
import dataPlots.data.DataLineInfo;
import dataPlots.data.TDDataInfo;
import dataPlots.data.TDDataProvider;
import dataPlots.data.TDSymbolChooser;
import dataPlots.layout.TDGraph;
import dataPlotsFX.data.TDScaleInfo;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;

public class SpectrogramPlotInfo extends TDDataInfo implements SpectrogramParametersUser, HidingDialogChangeListener {

	private PamControlledUnit fftControl;
	
	private DataLineInfo fftDataLine;
	
	private TDScaleInfo fftScaleInfo;
	
	private SpectrogramParameters specParams = new SpectrogramParameters();

	private TDControl tdControl;

	private FFTObserver fftObserver;
	
	private FFTDataBlock fftDataBlock;
	
	private SpectrogramChannelData[] specChannelData = new SpectrogramChannelData[PamConstants.MAX_CHANNELS];

	private ColourArray colourArray;

	private double[][] colorValues;	
	
	private SpectrogramHidingPanel spectrogramHidingPanel;

	protected int[] freqBinRange = new int[2];
	
	public SpectrogramPlotInfo(TDDataProvider tdDataProvider, PamControlledUnit fftControl, TDGraph tdGraph,
			PamDataBlock pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);
		this.fftControl = fftControl;
		this.tdControl = tdGraph.getTdControl();
		this.fftDataBlock = (FFTDataBlock) pamDataBlock;
		createColours();
		fftDataLine = new DataLineInfo("Spectrogram", "Hz");
		addDataUnits(fftDataLine);
		fftScaleInfo = new TDScaleInfo(0, 1, ParameterType.FREQUENCY, ParameterUnits.HZ);
		pamDataBlock.addObserver(fftObserver = new FFTObserver());
		//set the max frequency to nyquist if zero. m
		if (specParams.frequencyLimits[1]==0) specParams.frequencyLimits[1]=fftObserver.fftSampleRate/2;
		spectrogramHidingPanel = new SpectrogramHidingPanel(this);
		spectrogramHidingPanel.addChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#getHidingDialogComponent()
	 */
	@Override
	public HidingDialogComponent getHidingDialogComponent() {
		return spectrogramHidingPanel;
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#removeData()
	 */
	@Override
	public void removeData() {
		super.removeData();
		getDataBlock().deleteObserver(fftObserver);
	}

	/**
	 * Called when new FFT data arrive. 
	 * @param fftDataUnit
	 */
	public void newFFTData(FFTDataUnit fftDataUnit) {
//		int chan = PamUtils.getSingleChannel(fftDataUnit.getChannelBitmap());
		int chan = PamUtils.getSingleChannel(fftDataUnit.getSequenceBitmap());
		if (specChannelData[chan] != null) {
			specChannelData[chan].newFFTData(fftDataUnit);
		}
	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		return null;
	}

	@Override
	public TDSymbolChooser getSymbolChooser() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#drawData(int, java.awt.Graphics, int, long, double, Layout.PamAxis)
	 */
	@Override
	public void drawData(int plotNumber, Graphics g, Rectangle windowRect, int orientation,
			PamAxis timeAxis, long scrollStart, PamAxis graphAxis) {
		int chan = specParams.channelList[plotNumber];
		if (chan >= 0 && specChannelData[chan] != null) {
			specChannelData[chan].drawSpectrogram(g, windowRect, orientation, timeAxis, scrollStart);
		}
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#hasOptions()
	 */
	@Override
	public boolean hasOptions() {
		return true;
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#editOptions()
	 */
	@Override
	public boolean editOptions(Window frame) {
		SpectrogramParameters newParams = SpectrogramParamsDialog.showDialog(frame, null, specParams);
		if (newParams == null) {
			return false;
		}
		specParams = newParams.clone();
		return configureSpectrogram();
	}

	private boolean configureSpectrogram() {
		
//		getTdGraph().setNumberOfPlots(specParams.nPanels);
		
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			int nUsers = specParams.channelUsers(i);
			if (nUsers == 0) {
				specChannelData[i] = null;
				continue;
			}
			if (specChannelData[i] == null) {
				specChannelData[i] = new SpectrogramChannelData(this, i);
			}
			specChannelData[i].checkConfig();
		}
		
		calcFrequencyRangeDisplay();
		
		//important to set the axis properly on the graph. 
		getTdGraph().checkAxis();
		
		createColours();
		return true;
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#getScaleInformation(int, boolean)
	 */
	@Override
	public TDScaleInfo getScaleInformation(int orientation,
			boolean autoScale) {
		fftScaleInfo.setMinVal(specParams.frequencyLimits[0]);
		fftScaleInfo.setMaxVal(specParams.frequencyLimits[1]);
		fftScaleInfo.setnPlots(specParams.nPanels);
		return fftScaleInfo;
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#getFixedScaleInformation(int)
	 */
	@Override
	public TDScaleInfo getFixedScaleInformation(int orientation) {
		return getScaleInformation(0, false);
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#getStoredSettings()
	 */
	@Override
	public Serializable getStoredSettings() {
		return specParams;
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#setStoredSettings(java.io.Serializable)
	 */
	@Override
	public boolean setStoredSettings(Serializable storedSettings) {
		if (storedSettings != null) {
			specParams = (SpectrogramParameters) storedSettings;
			return configureSpectrogram();
		}
		return false;
	}
	/**
	 * @return the tdControl
	 */
	public TDControl getTdControl() {
		return tdControl;
	}

	/**
	 * @return the fftDataBlock
	 */
	public FFTDataBlock getFftDataBlock() {
		return fftDataBlock;
	}
	
	private ColourArrayType previousColour;
	/**
	 * Create the colour arrays to use in the plots. 
	 */
	protected void createColours() {
		//		colourArray = ColourArray.createHotArray(256);
		if (specParams.getColourMap() == previousColour) {
			return;
		}
		colourArray = ColourArray.createStandardColourArray(256, previousColour=specParams.getColourMap());
		colorValues = new double[256][3];
		for (int i = 0; i < 256; i++) {
			colorValues[i][0] = colourArray.getColours()[i].getRed();
			colorValues[i][1] = colourArray.getColours()[i].getGreen();
			colorValues[i][2] = colourArray.getColours()[i].getBlue();
		}
	}

	/**
	 * Get the index of a colour value. 
	 * @param dBLevel signal level in dB. 
	 * @return colour index (0 - 255)
	 */
	private int getColourIndex(double dBLevel) {
		// fftMag is << 1
		double  p;
		p = 256	* (dBLevel - specParams.amplitudeLimits[0])
				/ (specParams.amplitudeLimits[1] - specParams.amplitudeLimits[0]);
		return (int) Math.max(Math.min(p, 255), 0);
	}
	/**
	 * Get the colour triplet for a particular db value. 
	 * @param dBLevel
	 * @return colour triplet. 
	 */
	public double[] getColours(double dBLevel) {
		return colorValues[getColourIndex(dBLevel)];
	}
	
	private class FFTObserver extends PamObserverAdapter {

		private float fftSampleRate;

		@Override
		public void addData(PamObservable o, PamDataUnit dataUnit) {
			newFFTData((FFTDataUnit) dataUnit);
		}

		@Override
		public void setSampleRate(float sampleRate, boolean notify) {
			this.fftSampleRate = sampleRate;
		}

		@Override
		public String getObserverName() {
			return getDataName() + "Spectrogram";
		}
	}
	
	private class FFTLoadObserver implements LoadObserver {

		@Override
		public void setLoadStatus(int loadState) {
			getTdGraph().repaint(100);
		}
		
	}

	@Override
	public SpectrogramParameters getSpectrogramParameters() {
		return specParams;
	}

	@Override
	public void setSpectrogramParameters(SpectrogramParameters spectrogramParameters) {
		this.specParams = spectrogramParameters;
//		System.out.println("FrequencyLimits: lower: "+spectrogramParameters.frequencyLimits);
		configureSpectrogram();
//		calcFrequencyRangeDisplay();
//		createColours();
//		getTdGraph().checkAxis();
//		getTdControl().getComponent().repaint();
	}

	@Override
	public FFTDataBlock getFFTDataBlock() {
		return fftDataBlock;
	}

	/**
	 * Checks the frequency range range to be displayed. 
	 * This is a direct copy of similar fun in spec display
	 */
	private void calcFrequencyRangeDisplay(){
		// check the frequency bins ...
		SpectrogramParameters spectrogramParameters = getSpectrogramParameters();
		FFTDataBlock sourceFFTDataBlock = getFftDataBlock();
		if (spectrogramParameters == null || sourceFFTDataBlock == null) {
			return;
		}
		float sampleRate = sourceFFTDataBlock.getSampleRate();
		freqBinRange  [0] = (int) Math
				.floor(spectrogramParameters.frequencyLimits[0]
						* sourceFFTDataBlock.getFftLength() / sampleRate);
		freqBinRange[1] = (int) Math
				.ceil(spectrogramParameters.frequencyLimits[1]
						* sourceFFTDataBlock.getFftLength() / sampleRate);
		for (int i = 0; i < 2; i++) {
			freqBinRange[i] = sourceFFTDataBlock.getFftLength()/2 - freqBinRange[i];
			freqBinRange[i] = Math.min(Math.max(freqBinRange[i], 0),
					sourceFFTDataBlock.getFftLength() / 2 - 1);
		}
//		System.out.println(String.format("Freq bins range = %d to %d", freqBinRange[0], freqBinRange[1]));
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#timeScrollValueChanged(long)
	 */
	@Override
	public void timeScrollValueChanged(long valueMillis) {
		/*
		 *  called in viewer mode - need to request FFT data in order to 
		 *  rebuild the  spectrogram image
		 */
		if (isViewer()) {
			orderOfflineData();
		}
	}
	
	
	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#timeScrollRangeChanged(long, long)
	 */
	@Override
	public void timeScrollRangeChanged(long minimumMillis, long maximumMillis) {
		// TODO Auto-generated method stub
		super.timeScrollRangeChanged(minimumMillis, maximumMillis);
//		System.out.println(String.format("Spec time range change from %s to %s", PamCalendar.formatDateTime(minimumMillis),
//				PamCalendar.formatTime(maximumMillis)));
	}
	
	@Override
	public void timeRangeSpinnerChange(double oldValue, double newValue) {	
		if (isViewer()) {
			orderOfflineData();
		}
	}
	
	private void orderOfflineData() {

		if (fftDataBlock == null) {
			return;
		}
		if (specChannelData == null) {
			return;
		}
		/**
		 * First cancel the last order and reset pointers in the output images. 
		 */
		fftDataBlock.cancelDataOrder();
		
		for (int i = 0; i < specChannelData.length; i++) {
			if (specChannelData[i] != null) {
				specChannelData[i].resetForLoad();
			}
		}
		
		long dataStart = getTdControl().getTimeScroller().getValueMillis();
		long dataEnd = dataStart + (long) (getTdControl().getTimeRangeSpinner().getSpinnerValue() * 1000);
//		System.out.println(String.format("Loading data from %s to %s", PamCalendar.formatDateTime(dataStart),
//				PamCalendar.formatTime(dataEnd)));
		fftDataBlock.orderOfflineData(fftObserver, new FFTLoadObserver(), dataStart, dataEnd, 1, OfflineDataLoading.OFFLINE_DATA_INTERRUPT);

	}

	@Override
	public void dialogChanged(int changeLevel, Object object) {
		if (changeLevel < 1 && object != null && SpectrogramParameters.class.isAssignableFrom(object.getClass())) {
			setSpectrogramParameters((SpectrogramParameters) object);
			configureSpectrogram();
			getTdControl().getComponent().repaint();
		}
		
	}

}
