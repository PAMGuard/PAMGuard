package generalDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Converts arrays of numbers into string representations which are a little
 * bit, but not entirely json like for writing to text fields in the database. 
 * @author Doug
 *
 */
public class JsonConverter {

	protected static final String NUMBERFORMAT = "0.###E0";
	protected static final DecimalFormat digitFormat = new DecimalFormat(NUMBERFORMAT);

	public String getJSONElement(String name, double[] data) {
		return getJSONElement(name, data, digitFormat);
	}
	
	/**
	 * Make a standard JSON like element
	 * @param name name of element
	 * @param data data for it
	 * @param format number format to use
	 * @return String of formatted data. 
	 */
	public String getJSONElement(String name, double[] data, DecimalFormat format) {
		String x = String.format("\"%s\":[", name);
		for (int i = 0; i < data.length; i++) {
			x += digitFormat.format(data[i]);
			if (i < data.length-1) {
				x += ",";
			}
		}
		x += "]";
		return x;
	}

	public double[] unpackJsonArray(ArrayNode jsonArrayNode) {
		if (jsonArrayNode == null) {
			return null;
		}
		int n = jsonArrayNode.size();
		double[] data = new double[n];
		for (int i = 0; i < n; i++) {
			JsonNode node = jsonArrayNode.get(i);
			if (node == null) {
				data[i] = Double.NaN;
				continue;
			}
			data[i] = node.asDouble();
		}
		return data;
	}
	
	/**
	 * Function to create a complete json like string including start and end 
	 * characters using a standard number format. 
	 * @param name name of item
	 * @param data array of data
	 * @return json string. 
	 */
	public String quickJsonString(String name, double[] data) {
		return quickJsonString(name, data, NUMBERFORMAT);
	}

	/**
	 * Function to create a complete json like string including start and end 
	 * characters. Can only be used for a single json element. 
	 * @param name name of item
	 * @param data array of data
	 * @param numberformat2 number format. 
	 * @return json string. 
	 */
	private String quickJsonString(String name, double[] data, String numberformat2) {
		JsonFactory jf = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			JsonGenerator jg = jf.createJsonGenerator(os, JsonEncoding.UTF8);
			jg.writeStartObject();
			writeJsonArray(jg, name, data);
			jg.writeEndObject();
			jg.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String jsonString = os.toString();
		return jsonString;
		
	}

	/**
	 * Write an array of numbers into a JSON array in a predetermined number format.  
	 * Can only be used for a single json element. 
	 * @param jasonGenerator
	 * @param name
	 * @param data
	 * @throws JsonGenerationException
	 * @throws IOException
	 */
	public void writeJsonArray(JsonGenerator jasonGenerator, String name, double[] data) throws JsonGenerationException, IOException {
		jasonGenerator.writeArrayFieldStart(name);
		for (int i = 0; i < data.length; i++) {
			jasonGenerator.writeRawValue(digitFormat.format(data[i]));
		}
		jasonGenerator.writeEndArray();
	}

	/**
	 * Write a single number into a JSON array in a predetermined number format.  
	 * @param jasonGenerator
	 * @param name
	 * @param data
	 * @throws JsonGenerationException
	 * @throws IOException
	 */
	public void writeJsonValue(JsonGenerator jasonGenerator, String name, double data) throws JsonGenerationException, IOException {
		jasonGenerator.writeFieldName(name);
		jasonGenerator.writeRawValue(digitFormat.format(data));
	}


}
