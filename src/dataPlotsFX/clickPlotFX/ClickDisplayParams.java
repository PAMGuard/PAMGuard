package dataPlotsFX.clickPlotFX;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamController.PamController;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import dataPlotsFX.rawClipDataPlot.FFTPlotSettings;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;

/**
 * Parameters for displaying clicks in the TDDisplayFX. 
 * 
 * @author Jamie Macaulay
 *
 */
public class ClickDisplayParams extends FFTPlotSettings implements Serializable, Cloneable, ManagedParameters {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3L;

	
//	@Deprecated //- no longer used. 
//	public static enum ClickColour {
//		COLOUR_BY_TRAIN, COLOUR_BY_SPECIES, COLOUR_BY_TRAINANDSPECIES, COLOUR_BY_HYDROPHONE
//	}
//	
//
//	/**
//	 * How to colour clicks in the display.
//	 */
//	@Deprecated //- no longer used. 
//	public ClickColour colourBy=ClickColour.COLOUR_BY_SPECIES;
//
//	@Deprecated //- no longer used. 
//	public static final String[] colourNames = {"Click Train", "Click Type", "Train then Type", "Hydrophone"};
	
	
	/**
	 * Plot FFT by threshold or by spectrogram if thge clicks are display on a frequency time axis. 
	 */
	public boolean thresholdFFT = false; 
	
	/**
	 * The colour limits when plotting click FFT 
	 */
	//public double[] fftColorLims = new double[]{-90, -60};
	
	
	/**
	 * The cut off in energy at which a click FFT is not displayed. Represented as a % value 0-1;
	 */
	public double fftCutOf=0.3; 
	
	/**
	 * Color of clicks on FFT
	 */
	@Deprecated //- no longer used. 
	public String fftColor="red";

	/**
	 * Max height of click in pixels.
	 */
	public int maxClickHeight=12;

	/**
	 * Min height of click in pixels.
	 */
	public int minClickHeight=4;

	/**
	 * Max length of click in pixels.
	 */
	public int maxClickLength=12;
	
	/**
	 * Min length of click in pixels.
	 */
	public int minClickLength=4;
	
	/**
	 * Channel bitmap of channels to display. 
	 */
	public int displayChannels=0;

	/**
	 * The amplitude range 
	 */
	public double[] amplitudeRange=new double[]{80,  180}; //dB re 1uPa pp

	/**
	 * The bearing range. 
	 */
	public double[] bearingRange= new double[] {0, 180}; //degrees
	

	/**
	 * The inter-click interval range
	 */
	public double[] iciRange= new double[] {0, 2}; //seconds

	/**
	 * The slant angle
	 */
	public double[] slantRange = new double[] {-90, 90}; 

	/**
	 * The current colour scheme 
	 */
	public int colourScheme = 0;


	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ClickDisplayParams clone() {
		try {
			return (ClickDisplayParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}


	public double[] getDefaultFreqAmpLimits() {
		return PamController.getInstance().getGlobalMediumManager().getDefaultdBHzScales();
	}


	public double[] getDefaultFreqAmpRange() {
		return PamController.getInstance().getGlobalMediumManager().getDefaultdBHzScales();
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		try {
			Field field = this.getClass().getDeclaredField("thresholdFFT");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return thresholdFFT;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
