package clickTrainDetector.classification.simplechi2classifier;

import com.fasterxml.jackson.databind.JsonNode;

import clickTrainDetector.classification.CTClassification;
import clickTrainDetector.classification.SimpleClassifierJSONLogging;

public class Chi2JsonLogging extends SimpleClassifierJSONLogging {
	
	@Override
	public CTClassification createClassification(JsonNode jTree) {
		CTClassification simplClassification =super.createClassification(jTree);
		return new Chi2CTClassification(simplClassification.getSpeciesID());	
	}


}
