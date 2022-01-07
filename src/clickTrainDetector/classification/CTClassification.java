package clickTrainDetector.classification;


/**
 * A classification results for a single click train.
 * 
 * @author Jamie Macaulay 
 *
 */
public interface CTClassification {
	
	
	/**
	 * The classifier type
	 * @return the classifier type
	 */
	public CTClassifierType getClassifierType(); 
	
	/**
	 * Get the species ID for the classifier. 0 is unassigned and will results in
	 * the click train being deleted . >0 means the click train has been classified.
	 * <0 means that the click train has been <i>detected</i> but not classified.
	 * 
	 * @return the species ID for the classifier.
	 */
	public int getSpeciesID();

	/**
	 * Get summary string for classifiers. 
	 * @return a string summary of classifiers. 
	 */
	public String getSummaryString();
	
	
	/**
	 * Get string logging for the classifier. 
	 * @return the JSON logging
	 */
	public ClassifierJSONLogging getJSONLogging();


}
