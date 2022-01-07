package clickTrainDetector.clickTrainAlgorithms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import generalDatabase.JsonConverter;

/**
 * Logging for CTAlgorithmInfo. Saves and retrieves algorithm info from
 * the database. 
 * @author Jamie Macaulay 
 *
 */
public abstract class CTAlgorithmInfoLogging extends JsonConverter {
	
	
	public static final String ALGORITHMFIELD = "ALGORITHM";

	/**
	 * Create a standard xml like string of the algorithm inof data which can 
	 * be written to the relational database. 
	 * @return algorithm info information in an XML like format. 
	 */
	public String getJsonString(CTAlgorithmInfo algorithmInfo) {
		JsonFactory jf = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			JsonGenerator jg = jf.createJsonGenerator(os, JsonEncoding.UTF8);
			jg.writeStartObject();
			jg.writeStringField(ALGORITHMFIELD, algorithmInfo.getAlgorithmType());
			
			//write extra data
			writeJSONData(jg, algorithmInfo); 

			jg.writeEndObject();
			jg.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String jsonString = os.toString();
		return jsonString;
	}

	/**
	 * Write the algorithm info data - note that the algorithm type has already been added
	 * @param jg - the JSON string generator. 
	 * @param algorithmInfo - the algorithm info. 
	 */
	public abstract void writeJSONData(JsonGenerator jg, CTAlgorithmInfo algorithmInfo); 

	/**
	 * Get the algorithmInfo class from a string
	 * @param jsonString - the input string. 
	 * @return the algorithm info class. 
	 */
	public CTAlgorithmInfo createCTAlgorithmInfo(String jsonString) {
		// now try to parse it back into numbers.... 
		String algorithmType; 
		try {
			ObjectMapper om = new ObjectMapper();
			JsonNode jTree = om.readTree(new ByteArrayInputStream(jsonString.getBytes()));
			//					JsonNode nv = jTree.findValue("NAME");

			algorithmType = CTAlgorithmInfoLogging.getAlgorithmType(jTree); 

			//					JsonNode ne = jTree.findValue("ERRORS");
			//					if (ne != null && ArrayNode.class.isAssignableFrom(ne.getClass())) {
			//						errors = unpackJsonArray((ArrayNode) ne);
			//					}
			return createCTAlgorithmInfo(algorithmType, jTree);
		} catch (IOException e) {
			System.err.println("CT Algorithm " + jsonString);
			return null;
		}
	}
	
	/**
	 * Create the CTAlgorithmInfo from a JSON string. 
	 * @param algorithmType - the algorithm type from JSON string
	 * @param jTree - the jTree. 
	 * @return the CTAlgorithmInfo. 
	 */
	public abstract CTAlgorithmInfo createCTAlgorithmInfo(String algorithmType, JsonNode jTree);

	/**
	 * Get algorithm type. 
	 * @param jTree - the JSON tree
	 * @return the algorithm type. 
	 */
	public static String getAlgorithmType(JsonNode jTree) {
		JsonNode na = jTree.findValue(ALGORITHMFIELD);
		
		String algorithmType = null; 
		if (na != null ) {
			algorithmType = na.asText(); 
		}
		return algorithmType; 
	}
	
	
}
