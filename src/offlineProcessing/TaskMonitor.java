package offlineProcessing;

/**
 * 
 * Interface to receive notifications about the 
 * task progress (which will be running in a 
 * different SwingWorker thread). 
 * @author Doug Gillespie
 *
 */
public interface TaskMonitor {

	static public final int TASK_IDLE = 0;
	static public final int TASK_RUNNING = 1;
	static public final int TASK_INTERRRUPTED = 2;
	static public final int TASK_COMPLETE = 3;
	static public final int TASK_CRASHED = 4;
	
	/**
	 * Set the task status. 
	 * @param taskStatus
	 */
	public void setStatus(int taskStatus);
	
	/**
	 * Set the total number of files to process
	 * (will be one if only loaded data are being processed).
	 * @param nFiles
	 */
	public void setNumFiles(int nFiles);
	/**
	 * Set the overall task progress
	 * <p>
	 * @param global - global progress, i.e. number of files completed (0 - nFiles) 
	 * @param loaded - progress though data currently loaded (0 - 1.)
	 */
	public void setProgress(int global, double loaded);
	
	/**
	 * Set the current file name. 
	 * @param fileName
	 */
	public void setFileName(String fileName);
}
