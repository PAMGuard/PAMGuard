package Localiser.algorithms.locErrors;

import java.text.DecimalFormat;

public abstract class ErrorXMLData {


	protected static final DecimalFormat errorDigitFormat = new DecimalFormat("0.###E0");
	
	/**
	 * Create a standard xml like string of the error data which can 
	 * be written to the relational database. 
	 * @return Error information in an XML like format. 
	 */
	abstract public String getXMLString();

	public String getJSONElement(String name, double[] data) {
		return getJSONElement(name, data, errorDigitFormat);
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
			x += errorDigitFormat.format(data[i]);
			if (i < data.length-1) {
				x += ",";
			}
		}
		x += "]";
		return x;
	}

}