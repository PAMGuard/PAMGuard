package dataPlotsFX.scroller;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import dataPlotsFX.scrollingPlot2D.StandardPlot2DColours;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;

public class TDAcousticScrollerParams implements Cloneable, Serializable, ManagedParameters {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Type of colour scheme for spectrogram. 
	 */
	public ColourArrayType colourMap = ColourArrayType.HOT; 
	
	/**
	 * The amplitude limits of the color scale. 
	 */
	public double[] amplitudeLimits= {100, 160};

	/**
	 * The min and max allowed amplitude for the colour scale
	 */
	public double[] amplitudeMinMax= {60, 180};
	
	/**
	 * Disables the spectrogram preview if true. Used to prevent  long clunky load times, especially for high frequency data. 
	 * 
	 */
	public boolean disableSpectrumPreview= false; 
	
	@Override
	public TDAcousticScrollerParams clone()  {
		try {
			TDAcousticScrollerParams newParams = (TDAcousticScrollerParams) super.clone();
			return newParams;
		}
		catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}

	public void setParams(StandardPlot2DColours colors) {
		amplitudeLimits[0]=colors.getAmplitudeLimits()[0].get(); 
		amplitudeLimits[1]=colors.getAmplitudeLimits()[1].get(); 
		colourMap=colors.getColourMap();
	} 

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}


}
