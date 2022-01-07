package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * Parameters class for a simple chi2 variable. Contains the expected error in the 
 * variable, the minimum the erro can be (to stop near infintie chi^2 values) and 
 * the scaling factor for the error for creating user friendly controls.  
 * 
 * @author Jamie Macaulay 
 *
 */
public class SimpleChi2VarParams implements Serializable, Cloneable, ManagedParameters {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 10L;
	
	/**
	 * Default scale factor for ICI in seconds. 
	 */
	public static final double SCALE_FACTOR_ICI = 0.2; 
	
	/**
	 * Default scale factor for Bearing in radians. 
	 */
	public static final double SCALE_FACTOR_BEARING = Math.toRadians(2); 

	/**
	 * Default scale factor for amplitude in dB. 
	 */
	public static final double SCALE_FACTOR_AMPLITUDE  = 10;
	
	/**
	 * Default scale factor for peak frequency in Hertz. 
	 */
	public static final double SCALE_FACTOR_PFREQ  = 100000;
	
	/**
	 * Default scale factor for ICI. 
	 */
	public static final double SCALE_FACTOR_CORRELATION  = 1;
	
	/**
	 * Default scale factor for ICI. 
	 */
	public static final double SCALE_FACTOR_TIMEDELAYS  = 0.00001;


	
	/**
	 * Units string.
	 */
	private String unitString="";
	
	/**
	 * The name of the MHT variable. 
	 */
	public String name;


	/**
	 * The error value. This changes the relevance of the particular chi2 val. 
	 */
	public double error =0.2;
	

	/**
	 * The minimum error value allowed. This is used if error*ICI < minError. 
	 */
	public double minError=0.001;
	
	
	/**
	 * Get the scaling value for the MHT variable. The scaling value is used to set
	 * the scale for input controls so that users can set relative rather than
	 * absolute errors. It's essentially a guess at the standard error. So for
	 * example it might be 10 for amplitude because it's on the dB scale and perhaps
	 * 0.01 for the ICI value because the units are in seconds.
	 */
	public double errorScaleValue = 1; 
	
//	/**
//	 * Default results converter for converting error values to human readable values.. 
//	 * Note: Have to serialize this because it being null if transient causes all sort of
//	 * annoying issues...
//	 */
//	public ResultConverter resultConverter = new ResultConverter();
	
	
	/**
	 *The limits in which a user can change the slider. Only used with GUI for slider
	 */
	@Deprecated
	public double[] errLimits = new double[] {0.0000, 5}; //we want these defaults to stay the same very time PG opens. 


	/**
	 * Constructor for the simple MHT variable parameters. 
	 * @param name - the name of the variable
	 */
	public SimpleChi2VarParams(String name) {
		this.name=name;
	}
	
	/**
	 * Constructor for the simple MHT variable parameters. 
	 * @param name - the name of the variable
	 * @param unitString - the units of the variable. 
	 */
	public SimpleChi2VarParams(String name, String unitString ) {
		this.name=name; 
		this.unitString=unitString; 
	}
	
	/**
	 * Constructor for the simple MHT variable parameters.
	 * 
	 * @param name       - the name of the variable
	 * @param unitString -the units of the variable.
	 * @param error      - the standard error in the variable
	 * @param minError  - the absolute minimum error (after error is multiplied by
	 *                   IDI)
	 */
	public SimpleChi2VarParams(String name, String unitString, double error, double minError) {
		this.name=name; 
		this.unitString=unitString; 
		this.error=error;
		this.minError=minError;
	}
	
	/**
	 * Constructor for the simple MHT variable parameters.
	 * 
	 * @param name            - the name of the variable
	 * @param unitString      -the units of the variable.
	 * @param error           - the standard error in the variable
	 * @param minError       - the absolute minimum error (after error is
	 *                        multiplied by IDI)
	 * @param errorScaleValue - the error scale value. Should be close to the mean
	 *                        or median error you would expect for a species. Used
	 *                        for controls. Not used in the algorithm. 
	 */
	public SimpleChi2VarParams(String name, String unitString, double error, double minError, double errorScaleValue) {
		this.name=name; 
		this.unitString=unitString; 
		this.error=error;
		this.minError=minError;
		this.errorScaleValue=errorScaleValue; 
	}
	
	
	@Override
	public SimpleChi2VarParams clone() {
		try {
			return (SimpleChi2VarParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	

	/**
	 * Get the units string for the SimpleChi2Var. 
	 * @return the units string e.g. dB, degrees, s, etc.
	 */
	public String getUnits() {
		return unitString;
	}
	
	/**
	 * This method was added just so that the unitString field would be
	 * automatically included in the XML output in the getParameterSet method.
	 * @return
	 */
	public String getUnitString() {
		return getUnits();
	}
	
	
	
	@Override
	public String toString() {
		return name + String.format(" Error: %.7f Min Error: %.7f", 
				error, minError); 
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
