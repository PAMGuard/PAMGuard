package clickTrainDetector.classification.standardClassifier;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import clickTrainDetector.classification.CTClassification;
import clickTrainDetector.classification.SimpleClassifierJSONLogging;


/**
 * Standard classification JSON logging
 * 
 * @author Jamie Macaulay 
 *
 */
public class StandardClassificationJSON extends SimpleClassifierJSONLogging {	
	
	private CTClassification[] standardClassifier;

	/**
	 * Create the JSON logging for standard classifier. 
	 * @param ctClassifications - the standard classifier.
	 */
	public StandardClassificationJSON(CTClassification[] ctClassifications) {
		this.standardClassifier=ctClassifications; 
	}
	

	@Override
	public void writeJSONData(JsonGenerator jg, CTClassification ctClassification) {
		for (int i=0; i<standardClassifier.length; i++) {
			((SimpleClassifierJSONLogging) standardClassifier[i].getJSONLogging()).writeJSONData(jg, standardClassifier[i]);
		}
	}
	
	
	@Override
	public CTClassification createClassification(JsonNode jTree) {
		
		JsonNode na = jTree.findValue("SPECIES");
		int speciesID;
		if (na != null ) {
			speciesID = na.asInt(); 
		}
		else {
			System.err.println("Cannot load template classifier");
			return null; 
		}

		CTClassification[] ctClassification = new CTClassification[standardClassifier.length]; 
		for (int i=0; i<standardClassifier.length; i++) {
			ctClassification[i] = ((SimpleClassifierJSONLogging) standardClassifier[i].getJSONLogging()).createClassification(jTree);
		}
		
		StandardClassification stClassification = 
				new StandardClassification(ctClassification, speciesID); 
		
		return stClassification; 
	}


}
