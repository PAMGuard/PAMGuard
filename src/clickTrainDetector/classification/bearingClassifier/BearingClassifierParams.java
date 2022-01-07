package clickTrainDetector.classification.bearingClassifier;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import clickTrainDetector.classification.CTClassifierParams;
import clickTrainDetector.classification.CTClassifierType;

/**
 * The bearing classifier parameters. 
 * 
 * @author Jamie Macualay 
 *
 */
public class BearingClassifierParams extends CTClassifierParams implements ManagedParameters {
	
	
	public BearingClassifierParams(){
		type = CTClassifierType.BEARINGCLASSIFIER; 
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;


	/**
	 * The default species flag. 
	 */
	public int speciesFlag = -1; 

	
	/**
	 * The minimum bearing limits in which the classifier works
	 */
	public double bearingLimMin = Math.toRadians(85);
	
	/**
	 * The maximum limit in which the classifier works. 
	 */
	public double bearingLimMax = Math.toRadians(95);
	

	
	/***Bearing derivatives (in radians per second)****/
	
	//MEAN
	
	public boolean useMean = false; 

	/**
	 * The minimum mean bearing derivative (radian/second)
	 */
	public double minMeanBearingD = Math.toRadians(-0.005);	
	
	/**
	 * The minimum mean bearing derivative (radian/second)
	 */
	public double maxMeanBearingD = Math.toRadians(0.005);
	
	
	//MEDIAN
	
	public boolean useMedian = true; 
	
	/**
	 * The minimum mean bearing derivative (radian/second)
	 */
	public double minMedianBearingD = Math.toRadians(-0.005);	
	
	
	/**
	 * The minimum mean bearing derivative (radian/second)
	 */
	public double maxMedianBearingD = Math.toRadians(0.005);	
	
	//STANDARD DEVIATION 
	
	/**
	 * True to consider 
	 */
	public boolean useStD = true;
	
	/**
	 * The minimum standard deviation in bearing derivative (radian/second) 
	 */
	public double minStdBearingD = Math.toRadians(0);	
	
	/**
	 * The minimum standard deviation in bearing derivative (radian/second)
	 */
	public double maxStdBearingD = Math.toRadians(1.5);
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}
	
}
