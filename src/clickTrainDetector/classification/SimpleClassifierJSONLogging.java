package clickTrainDetector.classification;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import PamguardMVC.debug.Debug;
import clickTrainDetector.classification.simplechi2classifier.Chi2CTClassification;

public class SimpleClassifierJSONLogging extends ClassifierJSONLogging {
	
	
	public static final String TYPEFIELD = "TYPE";

	public static final String SPECIESFIELD = "SPECIES";


	@Override
	public String getJsonString(CTClassification ctClassification) {
		
		JsonFactory jf = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			JsonGenerator jg = jf.createJsonGenerator(os, JsonEncoding.UTF8);
			jg.writeStartObject();
			jg.writeStringField(TYPEFIELD, ctClassification.getClassifierType().toString());
			jg.writeNumberField(SPECIESFIELD, ctClassification.getSpeciesID());
			
			//write extra data
			writeJSONData(jg, ctClassification); 

			jg.writeEndObject();
			jg.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String jsonString = os.toString();
		return jsonString;
	}
	
	/**
	 * Write JSON data after default TYPE and SPECIESID values have been added. 
	 * @param the JSON data generator. 
	 */
	public void writeJSONData(JsonGenerator jg, CTClassification ctClassification) {
		
	}
	
	/**
	 * Get classifier data after default TYPE and SPECIESID values has been 
	 * @param the JSON node which contains data. 
	 */
	public CTClassification createClassification(JsonNode jTree) {
		int speciesType = 0; 
		CTClassifierType clssfrType = null;
		JsonNode na = jTree.findValue(SPECIESFIELD);
		
		if (na != null ) {
			speciesType = na.asInt(); 
		}
		else {
			Debug.out.println("SimpleClassifierJSONLogging:  Could not find SPECIESFIELD");
			return null;
		}
		
		na = jTree.findValue(TYPEFIELD);
		if (na != null ) {
			String type = na.textValue();
			clssfrType  =CTClassifierType.valueOf(type);
			//Debug.out.println("SimpleClassifierJSONLogging:  speciesType: " + speciesType + " clssfrType: " + type);
		}
		else {
			Debug.out.println("SimpleClassifierJSONLogging:  Could not find TYPEFIELD");
			return null;
		}
		

		SimpleCTClassification smpl = new SimpleCTClassification(speciesType, clssfrType); 

		return smpl; 
	}




	@Override
	public CTClassification createClassification(String jsonString) {
		// now try to parse it back into numbers.... 
		try {
			ObjectMapper om = new ObjectMapper();
			JsonNode jTree = om.readTree(new ByteArrayInputStream(jsonString.getBytes()));
//			JsonNode nv = jTree.findValue("NAME");
			
			return createClassification(jTree); 

//			JsonNode ne = jTree.findValue("ERRORS");
//			if (ne != null && ArrayNode.class.isAssignableFrom(ne.getClass())) {
//				errors = unpackJsonArray((ArrayNode) ne);
//			}
		} catch (IOException e) {
			System.err.println("Classification interpreting " + jsonString);
			e.printStackTrace();
			return null;
		}
	}

}
