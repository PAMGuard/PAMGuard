package whistleClassifier.training;

public class ClassifierTrainingProgress {


	public static final int IDLE = 0;
	public static final int START_ALL = 1;
	public static final int COMPLETE_ALL = 2;
	public static final int ABORT = 3;
	public static final int START_ONE = 4;
	public static final int COMPLETE_ONE = 5;
	
	int status;
	int totalBootstraps;
	int completedBootstraps;
	
	String message;
	
	/**
	 * @param status
	 * @param totalBootstraps
	 * @param completedBootstraps
	 */
	public ClassifierTrainingProgress(int status, int totalBootstraps,
			int completedBootstraps) {
		super();
		this.status = status;
		this.totalBootstraps = totalBootstraps;
		this.completedBootstraps = completedBootstraps;
	}

	/**
	 * @param message
	 */
	public ClassifierTrainingProgress(String message) {
		super();
		this.message = message;
	}
	
	
}
