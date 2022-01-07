package clickTrainDetector.classification;

import java.io.Serializable;

/**
 * Types of click train classifier that the click train detector can use. 
 * @author Jamie Macaulay
 *
 */
public enum CTClassifierType implements Serializable {

	CHI2THRESHOLD, //very simple chi^2 threshold classifier. 
	TEMPLATECLASSIFIER,//template based classifier
	BEARINGCLASSIFIER, // bearing based classification
	MATCHEDCLICK //external matched click classifier which uses average waveforms of click trains. 

}
