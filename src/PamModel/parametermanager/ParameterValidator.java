package PamModel.parametermanager;

/**
 * A parameter validator for checking whether an input value for a field is valid. 
 * @author Jamie Macaulay 
 *
 */
public interface ParameterValidator {
	
	public static final int PARAMOK=1;
	
	/**
	 * Check whether the parameter is OK. This is overridden to perform tests. 
	 * @return 1 if parameter is OK, other values are error codes.  
	 */
	public int isParameterOK(Number value);

}
