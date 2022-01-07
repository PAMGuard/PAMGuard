package whistleClassifier;

/**
 * interface for classes which are able to parameterise a whistle fragment
 * @author Doug Gillespie
 *
 */
public interface FragmentParameteriser {

	/**
	 * Get the number of parameters returned by this parameteriser
	 * @return number of parameters
	 */
	public int getNumParameters();
	
	/**
	 * Extract the parameters for a given fragment
	 * @param whistleContour
	 * @return array of parameters. 
	 */
	public double[] getParameters(WhistleContour whistleContour);
	
}
