package Localiser.algorithms.locErrors.json;

import Localiser.algorithms.locErrors.LocaliserError;
import generalDatabase.JsonConverter;

public abstract class ErrorJsonConverter extends JsonConverter{


	/**
	 * Create a standard xml like string of the error data which can 
	 * be written to the relational database. 
	 * @return Error information in an XML like format. 
	 */
	abstract public String getJsonString(LocaliserError localiserError);

	abstract public LocaliserError createError(String jsonString);

	abstract public String getErrorName();

}