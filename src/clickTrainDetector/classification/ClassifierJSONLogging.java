package clickTrainDetector.classification;

import generalDatabase.JsonConverter;

/**
 * String logging for classifier for the click train detector. 
 * 
 * @author Jamie Macaulay
 *
 */
public abstract class ClassifierJSONLogging extends JsonConverter{


	/**
	 * Create a standard xml like string of the error data which can 
	 * be written to the relational database. 
	 * @return Error information in an XML like format. 
	 */
	abstract public String getJsonString(CTClassification classification);

	/**
	 * Get the CT classification from a string
	 * @param jsonString - the input string. 
	 * @return
	 */
	abstract public CTClassification createClassification(String jsonString);

}