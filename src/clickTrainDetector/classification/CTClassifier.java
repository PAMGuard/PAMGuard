package clickTrainDetector.classification;

import clickTrainDetector.CTDataUnit;
import clickTrainDetector.layout.classification.CTClassifierGraphics;

/**
 * A classifier which is used to classify groups of detections
 * <p>
 * In keep with PAMGuard convention the classification has a species flag. In
 * general positive values indicate a species whilst negative values indicate
 * noise or some unwanted classification. A 0 value means a click train is
 * totally unclassified and should be deleted. Generally an ID of 0 is only used
 * by the pre-classifier when running through the detection process for the
 * first time.
 * 
 * @author Jamie Macaulay
 *
 */
public interface CTClassifier {
	
	/**
	 * Flag which indicates that the classification has passed the pre-classifier stage. 
	 */
	public final static int PRECLASSIFIERFLAG = -1; 
	
	/**
	 * Flag which indicates the click train is completely unclassified and should be deleted. 
	 */
	public final static int NOSPECIES = 0; 

	
	/**
	 * Classify a click train detection. 
	 * @return the classified data block. 
	 */
	public CTClassification classifyClickTrain(CTDataUnit clickTrain);
	
	/**
	 * Get the name of the classifier. 
	 * @return the name of the classifier 
	 */
	public String getName(); 
	
	/**
	 * Get the unique species ID for the classifier. 
	 * @return the name of the classifier 
	 */
	public int getSpeciesID(); 
	
	/*
	 * Get the GUI components of the click train classifier.
	 */
	public CTClassifierGraphics getCTClassifierGraphics();

	/**
	 * Set the classifier parameters. 
	 * @param ctClassifierParams - the ct classifier paratmers
	 */
	public void setParams(CTClassifierParams ctClassifierParams);

	
	/**
	 * Get the CT classifier params. 
	 * @return the CT classifier params. 
	 */
	public CTClassifierParams getParams(); 
	
}
