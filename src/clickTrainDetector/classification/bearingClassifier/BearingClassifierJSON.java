package clickTrainDetector.classification.bearingClassifier;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import clickTrainDetector.classification.CTClassification;
import clickTrainDetector.classification.SimpleClassifierJSONLogging;

/**
 * JSON string logging for the bearing classifier. 
 * 
 * @author Jamie Macaulay
 *
 */
public class BearingClassifierJSON extends SimpleClassifierJSONLogging {	
	
	public static final String MEDIAN_BEARING_STRING = "MED_BEARING_DELTA"; 
	public static final String MEAN_BEARING_STRING = "MEAN_BEARING_DELTA"; 
	public static final String STD_BEARING_STRING = "STD_BEARING_DELTA"; 

	/**
	 * Write JSON data after default TYPE and SPECIESID values have been added. 
	 * @param the JSON data generator. 
	 * @throws IOException 
	 * @throws JsonGenerationException 
	 */
	public void writeJSONData(JsonGenerator jg, CTClassification ctClassification) {
		try {
			jg.writeNumberField(MEDIAN_BEARING_STRING, ((BearingClassification) ctClassification).getMedianDelta());
			jg.writeNumberField(MEAN_BEARING_STRING, ((BearingClassification) ctClassification).getMeanDelta());
			jg.writeNumberField(STD_BEARING_STRING, ((BearingClassification) ctClassification).getStdDelta());

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
		double medianBearingDelta;
		double meanBearingDleta;
		double stdBearingDelta; 
		if (na != null ) {
			speciesID = na.asInt(); 
		}
		else {
			System.err.println("Cannot load template classifier");
			return null; 
		}

		na = jTree.findValue(MEDIAN_BEARING_STRING);
		if (na != null ) {
			medianBearingDelta = na.asDouble(); 
		}
		else {
			System.err.println("Cannot load template classifier: no median bearing delta");
			return null; 
		}
		
		na = jTree.findValue(MEAN_BEARING_STRING);
		if (na != null ) {
			meanBearingDleta = na.asDouble(); 
		}
		else {
			System.err.println("Cannot load template classifier: no mean bearing delta");
			return null; 
		}
		
		na = jTree.findValue(STD_BEARING_STRING);
		if (na != null ) {
			stdBearingDelta = na.asDouble(); 
		}
		else {
			System.err.println("Cannot load bearing classifier: no std bearing delta");
			return null; 
		}

		BearingClassification bearingClassification = 
				new BearingClassification(speciesID, meanBearingDleta, medianBearingDelta, stdBearingDelta); 
		
		return bearingClassification; 
	}
	
}
