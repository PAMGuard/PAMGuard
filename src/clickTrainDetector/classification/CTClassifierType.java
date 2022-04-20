package clickTrainDetector.classification;

import java.io.Serializable;

/**
 * Types of click train classifier that the click train detector can use. 
 * @author Jamie Macaulay
 *
 */
public enum CTClassifierType implements Serializable {

	CHI2THRESHOLD("X\\u00b2 threshold classifier", false), //very simple chi^2 threshold classifier. 
	TEMPLATECLASSIFIER("Template correlation", false),//template based classifier
	BEARINGCLASSIFIER("Bearing classifier",false), // bearing based classification
	IDICLASSIFIER("IDI classifier", false), //IDI. 
	STANDARDCLASSIFIER("Standard Classifier", true), //COMBINES THE IDI, BEARINGS, TEMPLATE AND CHI2THRESHOLD into one classifier 
	MATCHEDCLICK("Matched click classifier", false); //external matched click classifier which uses average waveforms of click trains. 
	
	
	
	/**
	 * True to enable the classifier.
	 */
	private boolean enable;
	
	/**
	 * The name to use for the classifier. 
	 */
	private String name;
	
	/**
	 * The name of the classifier type. 
	 * @return the name. 
	 */
	public String getName() {
		return name;
	}

	/**
	 * Check whether the classifier should be used or not. 
	 * @return true of the classifier is enabled. 
	 */
	public boolean isEnable() {
		return enable;
	}


	CTClassifierType(String name, boolean enable){
		this.name = name; 
		this.enable = enable;
	}
	
//	@Override
//	public String toString() {
//		return name;
//	}
	

}
