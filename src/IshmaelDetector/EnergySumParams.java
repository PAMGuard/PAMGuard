/**
 * 
 */
package IshmaelDetector;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;

/**
 * 
 * Parameters for the energy sum detectors.
 *  
 * @author Dave Mellinger, Hisham Qayum and Jamie Macaulay
 */
public class EnergySumParams extends IshDetParams implements Serializable, Cloneable, ManagedParameters {

	static public final long serialVersionUID = 0;
	
	public double f0 = 0, f1 = 1000;		//frequency range to sum over in Hz
	
	public double ratiof0 = 1000, ratiof1 = 2000;		//frequency range to sum over in Hz flor the ratio band	
	
	public boolean useRatio = false; 
	
	/**
	 * Use an averaging window adaptive noise floor. 
	 */
	public boolean adaptiveThreshold = false;
	
	/**
	 * The averaging value/damping factor for the adaptive noise floor. 
	 */
	public double longFilter = 0.0001; 
	
	/**
	 * Use a dB scale to calculate peaks
	 */
	public boolean useLog = false;

	/**
	 * If there is a loud short sound then the adaptive noise filter will take  along time to decay 
	 * back to normal. If the result reaches spikeDecay*the current energy then a noise exponentially decays
	 */
	public double spikeDecay=100;

	/**
	 * Smooth the detector output
	 */
	public boolean outPutSmoothing = false;

	/**
	 * The averaging value/damping factor for detector output. . 
	 */
	public Double shortFilter = 0.1; 
	
	/**
	 * Upgrade flag - use to test whether the deserialized settings object has the new fields.  Set
	 * it to true now.  If the object is from an old psf, then the field will be created with a default
	 * value of false.
	 */
	private boolean dontUpgrade = true;

	@Override
	protected EnergySumParams clone() {
		EnergySumParams newParams = (EnergySumParams) super.clone();
		
		// test if these settings are from an old settings file - if so, default the new fields correctly
		// Note that this really should be done in EnergySumControl.restoreSettings, but that
		// method calls clone() and it's nicer in this class because if the defaults ever change above we can copy
		// the changes easily here
		if (!newParams.dontUpgrade) {
			newParams.adaptiveThreshold = false;
			newParams.longFilter = 0.0001; 
			newParams.spikeDecay=100;
			newParams.outPutSmoothing = false;
			newParams.shortFilter = 0.1; 
			newParams.dontUpgrade = true;
		}
		return newParams;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		try {
			Field field = this.getClass().getDeclaredField("dontUpgrade");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return dontUpgrade;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
