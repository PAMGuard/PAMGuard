package Localiser.algorithms.locErrors.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import Localiser.algorithms.locErrors.EllipticalError;
import Localiser.algorithms.locErrors.LocaliserError;

public class EllipseJsonConverter extends ErrorJsonConverter {

	protected static final String errorName = "Ellipse";

	public EllipseJsonConverter() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see Localiser.algorithms.locErrors.ErrorXMLData#getXMLString()
	 */
	@Override
	public String getJsonString(LocaliserError localiserError) {
		EllipticalError ellipticalError = (EllipticalError) localiserError;
		double[] angles = ellipticalError.getAngles();
		double[] errors = ellipticalError.getEllipseDim();
		for (int i = 0; i < errors.length; i++) {
			if (!Double.isFinite(errors[i])) {
				errors[i] = 1.e8;
			}
		}

		JsonFactory jf = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			JsonGenerator jg = jf.createJsonGenerator(os, JsonEncoding.UTF8);
			jg.writeStartObject();
			jg.writeStringField("NAME", getErrorName());
			writeJsonArray(jg, "ANGLES", angles);
			writeJsonArray(jg, "ERRORS", errors);
//			jg.writeArrayFieldStart("ANGLES");
//			for (int i = 0; i < angles.length; i++) {
//				jg.writeRawValue(errorDigitFormat.format(angles[i]));
//			}
//			jg.writeEndArray();
//			jg.writeArrayFieldStart("ERRORS");
//			for (int i = 0; i < errors.length; i++) {
//				jg.writeRawValue(errorDigitFormat.format(errors[i]));
//			}
//			jg.writeEndArray();
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
		// now try to parse it back into numbers.... 
		double[] angles = null;
		double[] errors = null;
		try {
			ObjectMapper om = new ObjectMapper();
			JsonNode jTree = om.readTree(new ByteArrayInputStream(jsonString.getBytes()));
//			JsonNode nv = jTree.findValue("NAME");
			
			JsonNode na = jTree.findValue("ANGLES");
			if (na != null && ArrayNode.class.isAssignableFrom(na.getClass())) {
				angles = unpackJsonArray((ArrayNode) na);
			}

			JsonNode ne = jTree.findValue("ERRORS");
			if (ne != null && ArrayNode.class.isAssignableFrom(ne.getClass())) {
				errors = unpackJsonArray((ArrayNode) ne);
			}
		} catch (IOException e) {
			System.err.println("Ellipse Error interpreting " + jsonString);
			return null;
		}
		return new EllipticalError(angles, errors);
	}

	@Override
	public String getErrorName() {
		return errorName;
	}

}
