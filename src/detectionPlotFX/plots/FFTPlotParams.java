package detectionPlotFX.plots;

import java.io.Serializable;

import PamController.PamController;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;

public class FFTPlotParams  implements Serializable, Cloneable, ManagedParameters  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4L;

	/**
	 * The fft hop
	 */
	public int fftHop=512; //bins
	
	/**
	 * The fft length
	 */
	public int fftLength=1024; //bins

	/**
	 * The window function index. 
	 */
	public int windowFunction=1; 
	
	/**
	 * The padding to either side of the detection
	 */
	public long detPadding=500; //millis 

	/**
	 * The colour array for the plot. 
	 */
	ColourArrayType colorArray = ColourArrayType.HOT;
	
	
	/**
	 * The minimum and maximum frequency amplitude limits that can be set using the colour bar
	 */
	public double[] freqAmplitudeLimits = PamController.getInstance().getGlobalMediumManager().getDefaultdBHzScales();

	/**
	 * The range of amplitudes between which there is a colour scale
	 */
	public double[] freqAmplitudeRange  = PamController.getInstance().getGlobalMediumManager().getDefaultdBHzScales();

//	/**
//	 * The upper colour value
//	 */
//	public double upperColourValue = 160; 
//	
//	/**
//	 * The lower colour value. 
//	 */
//	public double lowerColourValue = 20; 
//	
//	/**
//	 * The maximum possible colour value
//	 */
//	public double maxColourValue = 160; 
//	
//	/**
//	 * The minimum possible colour value. 
//	 */
//	public double minColourValue = 20;

	/**
	 * The plot channel 
	 */
	public int plotChannel=0; 
	
	/**
	 * Normalise the FFFT's
	 */
	public boolean normalise = false;


	
	@Override
	protected FFTPlotParams clone() {
		try {
			return (FFTPlotParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setColourMap(ColourArrayType colourArrayType) {
		colorArray=colourArrayType;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}
	

}
