package IshmaelDetector;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * Ishamel display parameters for the Spectrogram plug in. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class IshDisplayParams implements Serializable, Cloneable, ManagedParameters {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The veretical scaling factor 
	 */
	public double verticalScaleFactor = 1.0;

	/**
	 * Indicates whether the Ishmael spectrogram plugin autoscales. 
	 */
	public boolean autoScale = true; 
	
	
	@Override 
	public IshDisplayParams clone() {
		try {
			return (IshDisplayParams) super.clone();
		}
		catch (CloneNotSupportedException Ex) {
			return null;
		}
	}


	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}
	

}
