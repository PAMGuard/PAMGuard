package rawDeepLearningClassifier;

/**
 * Status reporting for the deep learning module.
 */
public enum DLStatus {

	FILE_NULL("The input file is null",
			"The loaded file was null. If the file was download it may not have downloaded properly.", ErrorLevel.ERROR),

	MODEL_LOAD_FAILED("The model failed to load",
			" The model failed to load - this could be because it is incompatible with PAMGuard or an uknown file format.",
			ErrorLevel.ERROR),

	MODEL_LOAD_SUCCESS("The model loaded", " The model successfully load", ErrorLevel.NO_ERROR),

	DOWNLOAD_STARTING("Download starting", "The model is downloading", ErrorLevel.NO_ERROR),

	DOWNLOAD_FINISHED("Download finished", " The model successfully downloaded", ErrorLevel.NO_ERROR),

	DOWNLOADING("Downloading", "The model is currently downloading", ErrorLevel.NO_ERROR),

	NO_CONNECTION_TO_URL("Could not connect to the URL",
			" Could connect to the URL - the URL may be wrong or there may be no internet connection", ErrorLevel.ERROR),

	CONNECTION_TO_URL("Connected to URL", "The connection to the URL was successful", ErrorLevel.NO_ERROR),

	MODEL_DOWNLOAD_FAILED("The model was not downloaded",
			" The model download failed. Check internet connection and try again", ErrorLevel.ERROR),

	NO_MODEL_LOADED("There is no loaded model",
			"There is no loaded model. A model my have failed to load or the path/URL to the model is incorrect", ErrorLevel.WARNING),

	//this is not a show stopper because predictions are saved there's just no binary classification. 
	NO_BINARY_CLASSIFICATION("No binary classification",
			" There are no prediction classes selected for classification. Predicitons for each segment will be saved but there will be no detections generated",
			ErrorLevel.WARNING), 
	
	DECOMPRESSING_MODEL("Decompressing model", "Decompressing the model file", ErrorLevel.NO_ERROR), 

	INCOMPATIBLE_ZIP("Incorrect Zip format", "The zip format is incorrect. The zip file should have a *.pgdl file in the parent directory along with either a Tensorflow or PyTorch model.", ErrorLevel.ERROR), ;


	/**
	 * True of the message is an error message.
	 */
	private int isError = ErrorLevel.NO_ERROR;

	/**
	 * Check whether the message is an error message.
	 * 
	 * @return true if an error message.
	 */
	public boolean isError() {
		return isError==ErrorLevel.ERROR;
	}

	/**
	 * The name of the status
	 */
	private String string;

	/**
	 * Description of the status
	 */
	private String expanded;

	/**
	 * Status report for the leep learning module.
	 * 
	 * @param string   - status
	 * @param expanded - description of the status.
	 */
	DLStatus(String string, String expande, int isError) {
		this.string = string;
		this.expanded = expande;
		this.isError = isError;
	}

	/**
	 * Get a brief description of the status.
	 * 
	 * @return a brief description of the status.
	 */
	public String getName() {
		return string;
	}

	/**
	 * Longer description of the status.
	 * 
	 * @return longer descrption of the status.
	 */
	public String getDescription() {
		return expanded;
	}

	private static class ErrorLevel {
		public static final int NO_ERROR = 0;

		public static final int WARNING = 1;

		public static final int ERROR = 2;
	}

	/**
	 * Get the error flag. e.g. ErrorLevel.NO_ERROR. The error flag describes 
	 * whether the status is an error, warning or not an error. 
	 * @return the error flag.
	 */
	int getErrorFlag() {
		return isError;
	}

	public boolean isWarning() {
		return isError==ErrorLevel.WARNING;
	}

}
