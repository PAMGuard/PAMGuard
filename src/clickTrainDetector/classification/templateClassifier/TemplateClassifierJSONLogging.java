package clickTrainDetector.classification.templateClassifier;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
//import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import clickTrainDetector.classification.CTClassification;
import clickTrainDetector.classification.SimpleClassifierJSONLogging;

public class TemplateClassifierJSONLogging extends SimpleClassifierJSONLogging {
	
	public static final String CORRELATIONFIELD = "CORRVALUE";

	/**
	 * Write JSON data after default TYPE and SPECIESID values have been added. 
	 * @param the JSON data generator. 
	 * @throws IOException 
	 * @throws JsonGenerationException 
	 */
	public void writeJSONData(JsonGenerator jg, CTClassification ctClassification) {
		try {
			jg.writeNumberField(CORRELATIONFIELD, ((TemplateClassification) ctClassification).getCorrelationValue());
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get classifier data after default TYPE and SPECIESID values has been 
	 * @param the JSON node which contains data. 
	 */
	public CTClassification createClassification(JsonNode jTree) {
		JsonNode na = jTree.findValue("SPECIES");
		int speciesID;
		double corrValue; 
		if (na != null ) {
			speciesID = na.asInt(); 
		}
		else {
			System.err.println("Cannot load template classifier");
			return null; 
		}

		na = jTree.findValue(CORRELATIONFIELD);
		if (na != null ) {
			corrValue = na.asDouble(); 
		}
		else {
			System.err.println("Cannot load template classifier");
			return null; 
		}

		TemplateClassification templateClassification = new TemplateClassification(speciesID, corrValue); 
		
		return templateClassification; 
	}
}
