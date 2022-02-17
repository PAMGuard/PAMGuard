package clickTrainDetector.classification.standardClassifier;

import clickTrainDetector.classification.CTClassifierParams;
import clickTrainDetector.classification.CTClassifierType;



/**
 * Standard classifier parameters.  
 */
public class StandardClassifierParams extends CTClassifierParams {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * List of the classifier parameters used. 
	 */
	public CTClassifierParams[] ctClassifierParams; 
	
	/**
	 * List of which classifiers are enabled. 
	 */
	public boolean[] enable; 

	public StandardClassifierParams(){
		///very important to set this or else the clasifier manager does not
		//know which classifier to create. 
		type = CTClassifierType.STANDARDCLASSIFIER; 
	}

//	
//	/**
//	 * Standard classifier parameters.
//	 */
//	public Chi2ThresholdParams standClassifierParams = new Chi2ThresholdParams(); 
//	
//	
//	/**
//	 * The parameters for bearing classification.
//	 */
//	public BearingClassifierParams bearingClassifierParams = new BearingClassifierParams(); 
//	
//	
//	/**
//	 * The IDI parameters.
//	 */
//	public IDIClassifierParams idClassifierParams = new IDIClassifierParams();
//	
//	
//	/**
//	 * Template classifier parameters.
//	 */
//	public TemplateClassifierParams templateClassifierParams = new TemplateClassifierParams(); 
	
	
	public CTClassifierParams clone() {
		StandardClassifierParams clonedParams =(StandardClassifierParams) super.clone();
		
		//make sure to hard clone the settings. 
		for (int i=0; i<clonedParams.ctClassifierParams.length; i++) {
			clonedParams.ctClassifierParams[i] = clonedParams.ctClassifierParams[i].clone(); 
		}
		
		return clonedParams;
	}
	
	
	
	
	
	
	
	

}
