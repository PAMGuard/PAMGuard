package detectionview;

public class LoadProgress {
	
	public static final int LOAD_RUNNING = 1; 
	public static final int LOAD_DONE = 2; 
	public static final int LOAD_FAILED = 3; 

	int loadState; 
	
	int nTotalData;
	
	int nCreated;
	
	int nFails; 
	
	String message;

	/**
	 * @param nTotalData
	 * @param nCreated
	 * @param nFails
	 * @param message
	 */
	public LoadProgress(int loadState, int nTotalData, int nCreated, int nFails, String message) {
		super();
		this.loadState = loadState;
		this.nTotalData = nTotalData;
		this.nCreated = nCreated;
		this.nFails = nFails;
		this.message = message;
	}

	/**
	 * @param nTotalData
	 * @param nCreated
	 * @param nFails
	 */
	public LoadProgress(int loadState, int nTotalData, int nCreated, int nFails) {
		super(); 
		this.loadState = loadState;
		this.nTotalData = nTotalData;
		this.nCreated = nCreated;
		this.nFails = nFails;
	}

	/**
	 * @return the loadState
	 */
	public int getLoadState() {
		return loadState;
	}

	/**
	 * @return the nTotalData
	 */
	public int getnTotalData() {
		return nTotalData;
	}

	/**
	 * @return the nCreated
	 */
	public int getnCreated() {
		return nCreated;
	}

	/**
	 * @return the nFails
	 */
	public int getnFails() {
		return nFails;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
}
