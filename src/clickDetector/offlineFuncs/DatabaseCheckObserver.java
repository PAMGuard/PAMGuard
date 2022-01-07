package clickDetector.offlineFuncs;

public interface DatabaseCheckObserver {

	public void checkProgress(String text, int totalTasks, int taskNumber, int percent);
	
	public void checkOutputText(String text, int warnLevel);
	
	public boolean stopChecks();
	
}
