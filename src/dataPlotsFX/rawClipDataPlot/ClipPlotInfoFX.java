package dataPlotsFX.rawClipDataPlot;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clipgenerator.ClipControl;
import clipgenerator.ClipDataUnit;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.layout.TDGraphFX;

/**
 * The clip data info. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class ClipPlotInfoFX extends RawClipDataInfo {

	/**
	 * Reference to the clip control. 
	 */
	private ClipControl clipControl;

	public ClipPlotInfoFX(TDDataProviderFX tdDataProvider, ClipControl clipControl, TDGraphFX tdGraph, PamDataBlock pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);
		this.clipControl = clipControl;
	}

	@Override
	public double[][] getSpectrogram(PamDataUnit pamDataUnit, int chanClick) {
		ClipDataUnit clipDataUnit = (ClipDataUnit) pamDataUnit; 
		return clipDataUnit.getSpectrogramData(chanClick, this.getRawClipParams().fftLength, this.getRawClipParams().fftHop); 
	}

}
