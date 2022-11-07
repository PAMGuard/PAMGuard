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
//
//	static public final int TASK_IDLE = 0;
//	static public final int TASK_STARTING = 1; // before it started. 
//	static public final int TASK_RUNNING = 2;
//	static public final int TASK_INTERRRUPTED = 3;
//	static public final int TASK_COMPLETE = 4;
//	static public final int TASK_CRASHED = 5;
	
//	public static final int ACTIVITY_PROCESSING = 10;
//	public static final int ACTIVITY_LOADING = 11;
//	public static final int ACTIVITY_LINKING = 12;
//	public static final int ACTIVITY_SAVING = 13;
	
	public void setTaskStatus(TaskMonitorData taskMonitorData);
//	/**
//	 * Set the task status. 
//	 * @param taskStatus
//	 */
//	public void setStatus(int taskStatus);
//	
//	/**
//	 * Set the total number of files to process
//	 * (will be one if only loaded data are being processed).
//	 * @param nFiles
//	 */
//	public void setNumFiles(int nFiles);
//	/**
//	 * Set the overall task progress
//	 * <p>
//	 * @param global - global progress, i.e. number of files completed (0 - nFiles) 
//	 * @param loaded - progress though data currently loaded (0 - 1.)
//	 */
//	public void setProgress(int global, double loaded);
//	
//	/**
//	 * Set the current file name. 
//	 * @param fileName
//	 */
//	public void setFileName(String fileName);
}
