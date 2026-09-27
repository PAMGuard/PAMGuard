package Localiser.algorithms.locErrors.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import Localiser.algorithms.locErrors.BearingError;
import Localiser.algorithms.locErrors.LocaliserError;

public class BearingErrorJsonConverter extends ErrorJsonConverter {

	protected static final String errorName = "Bearing";
	
	@Override
	public String getJsonString(LocaliserError localiserError) {
		BearingError bearErr = (BearingError) localiserError;
		JsonFactory jf = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			JsonGenerator jg = jf.createJsonGenerator(os, JsonEncoding.UTF8);
			jg.writeStartObject();
			jg.writeStringField("NAME", getErrorName());
			jg.writeNumberField("BEARING", (float) bearErr.getMeanBearing()); 
			jg.writeNumberField("ERROR", (float) bearErr.getBearingError());
			jg.writeEndObject();
			jg.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String jsonString = os.toString();
		return jsonString;
	}

	@Override
	public LocaliserError createError(String jsonString) {
		double bear = 0;
		double err = 0;
	
		try {
			ObjectMapper om = new ObjectMapper();
			JsonNode jTree = om.readTree(new ByteArrayInputStream(jsonString.getBytes()));
//			JsonNode nv = jTree.findValue("NAME");
			JsonNode na = jTree.findValue("BEARING");
			if (na != null){
				bear = na.doubleValue();
			}
			na = jTree.findValue("ERROR");
			if (na != null) {
				err = na.doubleValue();
			}
		}catch (IOException e) {
			System.err.println("Bearing Error interpreting " + jsonString);
			return null;		
		}
		return new BearingError(bear, err);
	}

	@Override
	public String getErrorName() {
		return errorName;
	}

}
