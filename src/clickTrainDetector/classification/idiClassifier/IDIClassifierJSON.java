package clickTrainDetector.classification.idiClassifier;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import clickTrainDetector.classification.CTClassification;
import clickTrainDetector.classification.SimpleClassifierJSONLogging;

public class IDIClassifierJSON extends SimpleClassifierJSONLogging {	
	
	public static final String MEDIAN_IDI_STRING = "MED_IDI_DELTA"; 
	public static final String MEAN_IDI_STRING = "MEAN_IDI_DELTA"; 
	public static final String STD_IDI_STRING = "STD_IDI_DELTA"; 

	/**
	 * Write JSON data after default TYPE and SPECIESID values have been added. 
	 * @param the JSON data generator. 
	 * @throws IOException 
	 * @throws JsonGenerationException 
	 */
	public void writeJSONData(JsonGenerator jg, CTClassification ctClassification) {
		try {
			jg.writeNumberField(MEDIAN_IDI_STRING, ((IDIClassification) ctClassification).getMedianIDI());
			jg.writeNumberField(MEAN_IDI_STRING, ((IDIClassification) ctClassification).getMeanIDI());
			jg.writeNumberField(STD_IDI_STRING, ((IDIClassification) ctClassification).getStdIDI());

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
		double medianIDIDelta;
		double meanIDIDelta;
		double stdIDIDelta; 
		if (na != null ) {
			speciesID = na.asInt(); 
		}
		else {
			System.err.println("Cannot load IDI classifier");
			return null; 
		}

		na = jTree.findValue(MEDIAN_IDI_STRING);
		if (na != null ) {
			medianIDIDelta = na.asDouble(); 
		}
		else {
			System.err.println("Cannot load IDI classifier: no median IDI");
			return null; 
		}
		
		na = jTree.findValue(MEAN_IDI_STRING);
		if (na != null ) {
			meanIDIDelta = na.asDouble(); 
		}
		else {
			System.err.println("Cannot load IDI classifier: no mean IDI");
			return null; 
		}
		
		na = jTree.findValue(STD_IDI_STRING);
		if (na != null ) {
			stdIDIDelta = na.asDouble(); 
		}
		else {
			System.err.println("Cannot load IDI classifier: no std IDI");
			return null; 
		}

		IDIClassification idiClassification = 
				new IDIClassification(speciesID, meanIDIDelta, medianIDIDelta, stdIDIDelta); 
		
		return idiClassification; 
	}
	
}

