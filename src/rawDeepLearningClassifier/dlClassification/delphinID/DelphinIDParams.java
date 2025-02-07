package rawDeepLearningClassifier.dlClassification.delphinID;

import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;

public class DelphinIDParams extends StandardModelParams {
	
	/**
	 * Allowed data input types for DelphinID. 
	 */
	public static enum DelphinIDDataType {CLICKS, WHISTLES}
	
	/**
	 * The allowed input data type. 
	 */
	public DelphinIDDataType dataType = DelphinIDDataType.WHISTLES; 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The minimum detection density for whisltes and minimum number of clicks for clicks before
	 * a classification is attempted. 
	 */
	public double minDetectionValue = 0.3;
	
	
	/**
	 * Get the allowed input data type for the delphinID classifier. 
	 * @return the allowed data type. 
	 */
	public DelphinIDDataType getDataType() {
		return dataType;
	}
	

}
