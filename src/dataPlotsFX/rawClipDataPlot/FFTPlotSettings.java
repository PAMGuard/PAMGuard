package dataPlotsFX.rawClipDataPlot;

import java.io.Serializable;

import PamController.PamController;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;


/**
 * Settings for displaying raw data clips on a frequency time display. 
 * 
 * @author Jamie Macaulay
 *
 */
public class FFTPlotSettings implements ManagedParameters {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * The minimum and maximum frequency amplitude limits that can be set using the colour bar
	 */
	public double[] freqAmplitudeLimits = PamController.getInstance().getGlobalMediumManager().getDefaultdBHzScales();

	/**
	 * The range of amplitudes between which there is a colour scale
	 */
	public double[] freqAmplitudeRange  = PamController.getInstance().getGlobalMediumManager().getDefaultdBHzScales();
	
	/**
	 * The FFT length for clicks 
	 */
	public int fftHop = 256;

	/**
	 * The FFT hop for clicks. 
	 */
	public int fftLength = 512;

	/**
	 * The colour array type. 
	 */
	public ColourArrayType colourMap = ColourArrayType.INFERNO; 
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}

}
