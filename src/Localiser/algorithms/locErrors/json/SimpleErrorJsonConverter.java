package Localiser.algorithms.locErrors.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import Localiser.algorithms.locErrors.EllipticalError;
import Localiser.algorithms.locErrors.LocaliserError;
import Localiser.algorithms.locErrors.SimpleError;

public class SimpleErrorJsonConverter extends ErrorJsonConverter {

	public static final String errorName = "SimpleError";
	private String errorType;
	private double[] errorData;
	
	
	/* (non-Javadoc)
	 * @see Localiser.algorithms.locErrors.ErrorXMLData#getXMLString()
	 */
	@Override
	public String getJsonString(LocaliserError localiserError) {
		SimpleError simpleError = (SimpleError) localiserError;
		JsonFactory jf = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		try {
			JsonGenerator jg = jf.createJsonGenerator(os, JsonEncoding.UTF8);
			jg.writeStartObject();
			jg.writeStringField("NAME", getErrorName());
			if (simpleError.getPerpError() != null) {
				writeJsonValue(jg, "PERP", simpleError.getPerpError());
			}
			if (simpleError.getParallelError() != null) {
				writeJsonValue(jg, "PARR", simpleError.getParallelError());
			}
			if (simpleError.getDepthError() != null) {
				writeJsonValue(jg, "DEPTH", simpleError.getDepthError());
			}
			if (simpleError.getPerpAngle() != null) {
				writeJsonValue(jg, "ANGLE", simpleError.getPerpAngle());
			}
			jg.writeEndObject();
			jg.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		System.out.println("Error string from Jackson: " + os.toString());
		String jsonString = os.toString();
		return jsonString;
	}

	@Override
	public LocaliserError createError(String jsonString) {
		
		Double perpVal =null; 
		Double parrVal =null;
		Double zVal =null; 
		Double angleVal =null; 
		try {
			ObjectMapper om = new ObjectMapper();
			JsonNode jTree = om.readTree(new ByteArrayInputStream(jsonString.getBytes()));
			JsonNode name = jTree.findValue("NAME");
			JsonNode perp = jTree.findValue("PERP");
			JsonNode parr = jTree.findValue("PARR");
			JsonNode dpth = jTree.findValue("DEPTH");
			JsonNode angl = jTree.findValue("ANGLE");
			
			
			if (perp!=null) {
				perpVal=perp.doubleValue();
			}
			if (parr!=null ) {
				parrVal=parr.doubleValue();
			}
			if (dpth!=null) {
				zVal=dpth.doubleValue();
			}	
			if (angl!=null) {
				angleVal=angl.doubleValue();
			}

			//			JsonParser jp = jf.createJsonParser(jsonString);
			//			JsonNode jn = jp.readValueAsTree();
			//			Iterator<JsonNode> elements = jn.getElements();
			//			JsonToken jt = jp.nextToken();
			//			while (jt != null) {
			////				jt.
			//				jt = jp.nextToken();
			//			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Ellipse Error interpreting " + jsonString);
			return null;
		}
		return new SimpleError(perpVal, parrVal, zVal, angleVal);
	}

	@Override
	public String getErrorName() {
		return errorName;
	}

}
