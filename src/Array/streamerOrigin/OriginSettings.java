package Array.streamerOrigin;

import PamController.SettingsObject;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * Class to hold settings for the different origin methods. 
 * Some of these such as GPS probably won't have any, but others
 * such as static ones or other vessels AIS ones, will definitely
 * need to hold a variety of variables.
 * <p>
 * One thing they do all hold is the class type of the actual method
 * that does the work - only the settings rather than the entire method 
 * are serialised into the .psf files though.  
 * @author Doug Gillespie
 *
 */
public abstract class OriginSettings implements SettingsObject, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public Class originMethodClass;

	/**
	 * Constructor can generally be overridden using a 
	 * simple constructor with no input arguments. 
	 * @param originMethodClass
	 */
	public OriginSettings(Class originMethodClass) {
		super();
		this.originMethodClass = originMethodClass;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public OriginSettings clone()  {
		try {
			return (OriginSettings) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
