package clickTrainDetector.classification.idiClassifier;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import clickTrainDetector.classification.CTClassifierParams;
import clickTrainDetector.classification.CTClassifierType;

public class IDIClassifierParams extends CTClassifierParams implements Serializable, Cloneable, ManagedParameters {
	
	public IDIClassifierParams(){
		super();
		type = CTClassifierType.IDICLASSIFIER; 
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Use the median ICI measurements
	 */
	public boolean useMedianIDI = true;

	/**
	 * The minimim median ICI. 
	 */
	public Double minMedianIDI = 0.; // seconds

	/**
	 * The maximum median ICI
	 */
	public Double maxMedianIDI = 2.; // seconds

	/**
	 * Use the mean ICI measurements. 
	 */
	public boolean useMeanIDI = false;


	/**
	 * The minimum median ICI. 
	 */
	public Double minMeanIDI = 0.; // seconds

	/**
	 * The maximum median ICI
	 */
	public Double maxMeanIDI = 2.; // seconds


	/**
	 * Use the mean ICI measurements
	 */
	public boolean useStdIDI = false; 

	/**
	 * The minimum standard deviation in ICI. 
	 */
	public Double minStdIDI = 0.; // seconds

	/**
	 * The maximum standard deviation in ICI
	 */
	public Double maxStdIDI = 100.; // seconds

}
