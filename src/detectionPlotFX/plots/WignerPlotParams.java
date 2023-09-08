package detectionPlotFX.plots;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;


public class WignerPlotParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	public boolean limitLength = true;

	public int manualLength = 128;
	
	
	/**
	 * The colour array of the wigner plot
	 */
	public ColourArrayType colorArray= ColourArrayType.RED;
	
	/**
	 * The min colour value...i.e. the value of the wigner plot which be the extreme gradient of the 
	 * colour aray. 
	 */
	public double minColourVal=0; 
	
	/**
	 * The max colour value...i.e. the value of the wigner plot which be the extreme gradient of the 
	 * colour aray. 
	 */
	public double maxColourVal=1; 
	
	public Integer chan=0;
	
	@Override
	protected WignerPlotParams clone() {
		try {
			return (WignerPlotParams) super.clone();
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



