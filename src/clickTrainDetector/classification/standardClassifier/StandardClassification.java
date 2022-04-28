package clickTrainDetector.classification.standardClassifier;

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
	 * The current species ID.
	 */
	private int speciesID;

	/**
	 * All the classifications. 
	 */
	private CTClassification[] ctClassifications;
	
	
	public CTClassification[] getCtClassifications() {
		return ctClassifications;
	}

	/**
	 * Standard classifier JSON logging. 
	 */
	private StandardClassificationJSON standardClassifierJSONLogging; 
	
	
	public StandardClassification(CTClassification[] ctClassifications, int speciesID) {
		this.ctClassifications=ctClassifications; 
		standardClassifierJSONLogging = new StandardClassificationJSON(ctClassifications); 
		this.speciesID=speciesID; 

	}

	/**
	 * Create a classification unit from a JSON string. 
	 * @param jsonstring
	 */
	public StandardClassification(String jsonstring) {
		standardClassifierJSONLogging = new StandardClassificationJSON(); 
		
		CTClassification classification  = standardClassifierJSONLogging.createClassification(jsonstring); 
		
		this.ctClassifications = ((StandardClassification) classification).getCtClassifications();
		this.speciesID			=classification.getSpeciesID(); 

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
