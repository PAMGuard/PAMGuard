package Localiser.algorithms.locErrors.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import Localiser.algorithms.locErrors.LocaliserError;

public class LocaliserErrorFactory {

	static {
		registerErrorClasses();
	}

	private static void registerErrorClasses() {
		errorTypes = new ArrayList<>();
		errorTypes.add( new EllipseJsonConverter());
		errorTypes.add( new SimpleErrorJsonConverter());
	}
	
	/**
	 * Take a JSON error string, identify teh correct type and unpack
	 * it into a LocaliserError object. 
	 * @param errorString JSON error string
	 * @return LocaliserError of the appropriate type or null
	 */
	public static LocaliserError getErrorFromJsonString(String errorString) {
		ErrorJsonConverter errorPacker = findJSONConverter(errorString);
		if (errorPacker == null) {
			return null;
		}
		else {
			return errorPacker.createError(errorString);
		}
	}
	
	/**
	 * Find an XML converter for an error string. 
	 * @param errorString
	 * @return XML converter. 
	 */
	private static ErrorJsonConverter findJSONConverter(String errorString) {
		ObjectMapper om = new ObjectMapper();
		JsonNode jTree;
		try {
			jTree = om.readTree(new ByteArrayInputStream(errorString.getBytes()));
			JsonNode nv = jTree.findValue("NAME");
			String errorName = nv.asText();
			return findConverterbyName(errorName);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			return null;
		}
	}

	private static ErrorJsonConverter findConverterbyName(String errorName) {
		 Iterator<ErrorJsonConverter> it = errorTypes.iterator();
		while (it.hasNext()) {
			ErrorJsonConverter eClass = it.next();
			if (eClass.getErrorName().equals(errorName)) {
				return eClass;
			}
		}
		return null;
	}


	private static ArrayList<ErrorJsonConverter> errorTypes;
	
}
