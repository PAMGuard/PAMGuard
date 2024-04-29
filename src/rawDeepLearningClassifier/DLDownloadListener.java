package rawDeepLearningClassifier;

/**
 * Listener for downloading files. 
 */
public interface DLDownloadListener {


	/**
	 * Updates  the number of bytes downloaded 
	 * @param bytesDownlaoded - the number of bytes downloaded  so far. 
	 */
	public void update(DLStatus status, long bytesDownlaoded);

}
