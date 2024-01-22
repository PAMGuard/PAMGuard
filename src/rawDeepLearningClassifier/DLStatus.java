package rawDeepLearningClassifier;

/**
 * Status reporting for the deep learning module. 
 */
public enum DLStatus {

	FILE_NULL("The input file is null", "The loaded file was null. If the file was download it may not have downloaded properly.", true), 

	MODEL_LOAD_FAILED("The model failed to load", " The model failed to load - this could be because it is incompatible with PAMGuard or an uknown file format.", true),

	MODEL_LOAD_SUCCESS("The model loaded", " The model successfully load", false),

	DOWNLOAD_STARTING("The model loaded", " The model successfully load", false),

	DOWNLOAD_FINISHED("The model loaded", " The model successfully load", false),

	DOWNLOADING("The model loaded", " The model successfully load", false),

	NO_CONNECTION_TO_URL ("Could not connect to the URL", " Could connect to the URL - the URL may be wrong or there may be no internet connection", true),

	CONNECTION_TO_URL("The model loaded", " The model successfully load", false), 

	MODEL_DOWNLOAD_FAILED("The model was not downloaded", " The model download failed. Check internet connection and try again", true);


	private boolean isError = true;

	public boolean isError() {
		return isError;
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
	 * @param string - status
	 * @param expanded - description of the status. 
	 */
	DLStatus(String string, String expande, boolean isError) {
		this.string = string;
		this.expanded = expande;
		this.isError= isError;
	}

	public String getName() {
		return string;
	}

	public String getDescription() {
		return expanded;
	}

}
