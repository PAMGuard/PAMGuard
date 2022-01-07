package dataPlotsFX.rawClipDataPlot;

import PamguardMVC.PamDataUnit;
import dataPlotsFX.data.FFTPlotManager;
import dataPlotsFX.data.TDScaleInfo;

/**
 * 
 * The plot manager for data units that contains raw clips of data. 
 * 
 * @author Jamie Macaulay
 *
 */
public class RawClipFFTPlotManager extends FFTPlotManager {

	/**
	 * The raw clip plot info. 
	 */
	private RawClipDataInfo rawClipPlotInfo;

	public RawClipFFTPlotManager(RawClipDataInfo rawClipPlotInfo) {
		super(rawClipPlotInfo);
		this.rawClipPlotInfo=rawClipPlotInfo; 
	}


	@Override
	public FFTPlotSettings getFFTPlotParams() {
		return this.rawClipPlotInfo.getRawClipParams();
	}

	@Override
	public TDScaleInfo getFrequencyScaleInfo() {
		return rawClipPlotInfo.getFrequencyScaleInfo();
	}

	@Override
	public double[][] getSpectrogram(PamDataUnit pamDataUnit, int chanClick) {
		return rawClipPlotInfo.getSpectrogram( pamDataUnit, chanClick); 
	}




}
