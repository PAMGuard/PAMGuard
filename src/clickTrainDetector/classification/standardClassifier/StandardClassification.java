package clickTrainDetector.classification.standardClassifier;


import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.classification.CTClassification;
import clickTrainDetector.classification.CTClassifierType;
import clickTrainDetector.classification.ClassifierJSONLogging;
import clickTrainDetector.classification.bearingClassifier.BearingClassifierJSON;

/**
 * A classification object for a standard classification 
 * 
 * @author Jamie Macaulay
 *
 */
public class StandardClassification implements CTClassification {
	
	/**
	 * Reference to the click control. 
	 */
	private ClickTrainControl clickTrainControl;
	

	/**
	 * The current species ID.
	 */
	private int speciesID;

	/**
	 * All the classifications. 
	 */
	private CTClassification[] ctClassifications;

	/**
	 * Standard classifier JSON logging. 
	 */
	private StandardClassificationJSON standardClassifierJSONLogging; 
	

	public StandardClassification(ClickTrainControl clickTrainControl, CTClassification[] ctClassifications, int speciesID) {
		this.clickTrainControl=clickTrainControl; 
		this.ctClassifications=ctClassifications; 
		standardClassifierJSONLogging = new StandardClassificationJSON();
	}

	/**
	 * Create a classification unit from a JSON string. 
	 * @param jsonstring
	 */
	public StandardClassification(String jsonstring) {
		
		
	}

	@Override
	public CTClassifierType getClassifierType() {
		return CTClassifierType.STANDARDCLASSIFIER;
	}

	@Override
	public int getSpeciesID() {
		return speciesID;
	}

	@Override
	public String getSummaryString() {
		String summaryString =""; 
		for (CTClassification ctClassification : ctClassifications) {
			summaryString+= ctClassification.getSummaryString() + "\n"; 
		}
		return summaryString;
	}

	@Override
	public ClassifierJSONLogging getJSONLogging() {
		return standardClassifierJSONLogging;
	}

}
