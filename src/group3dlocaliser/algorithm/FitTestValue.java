package group3dlocaliser.algorithm;

/**
 * Common interface for the results of fit tests, e.g. Chi2 or Log Likelihood
 * @author dg50
 *
 */
public interface FitTestValue {
	
	/**
	 * The value of the test in it's normal units, e.g. a Chi2 test will return
	 * something positive and a Log Likelihood test will return something negative. 
	 * @return test result value
	 */
	public double getTestValue();
	
	/**
	 * 
	 * @return the number of degrees of freedom in the test
	 */
	public int getDegreesOfFreedom();
	
	/**
	 * the test score. This is the same as the value returned by getTestValue, except 
	 * that it may be flipped so that a larger value is always better, i.e. for a Chi2
	 * test it will return -getTestValue, whereas for a Log Likelihood test it will return +testValue
	 * @return score - where more positive (or less negative) results are always better
	 */
	public double getTestScore();

}
