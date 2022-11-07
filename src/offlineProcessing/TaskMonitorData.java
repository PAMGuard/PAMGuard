package offlineProcessing;

/**
 * Class used to transfer status and progress
 * information out of the OfflienTaskGroup worker thread. 
 * @author Doug Gillespie
 *
 */
public class TaskMonitorData {

//	static public final int SET_NFILES = 0x1;
//	static public final int SET_FILENAME = 0x2;
//	static public final int SET_STATUS = 0x4;
//	static public final int SET_PROGRESS = 0x8;
//	static public final int LOADING_DATA = 0x10;
//	static public final int LINKING_DATA = 0x20;
	
	public int progMaximum; // used for both files and units. 
	
	public int progValue;
	
	public String fileOrStatus; // will be a file name or the words "Current data" or null
	
	public TaskStatus taskStatus;
	
	public TaskActivity taskActivity;
	
	public long lastDataDate;
	
	

	public TaskMonitorData(TaskStatus taskStatus, TaskActivity taskActivity, int totalFiles, int currentFile, 
			String fileOrStatus, long lastDataDate) {
		super();
		this.progMaximum = totalFiles;
		this.progValue = currentFile;
		this.fileOrStatus = fileOrStatus;
		this.taskStatus = taskStatus;
		this.taskActivity = taskActivity;
		this.lastDataDate = lastDataDate;
	}

//	int dataType;
//	
//	int globalProgress;
//	
//	int nFiles;
//	
//	/**
//	 * Progress through loaded data
//	 * Values < 0 represent unknown (i.e. currently loading data). 
//	 * so progress bars should be set to indeterminate. 
//	 */
//	double loadedProgress;
//	
//	int status;
//	
//	String fileName;
//	
//
//	/**
//	 * Constructor used to set the status
//	 * @param status status
//	 */
//	TaskMonitorData(int status) {
//		this.status = status;
//		dataType = SET_STATUS;
//	}
//	
//	/**
//	 * Constructor used to set both the status and 
//	 * the total number of files. 
//	 * @param status status
//	 * @param nFiles number of files
//	 */
//	TaskMonitorData(int status, int nFiles) {
//		this.status = status;
//		this.nFiles = nFiles;
//		dataType = SET_STATUS | SET_NFILES;
//	}
//	
//	/**
//	 * Constructor used to set the current file name
//	 * @param fileName file Name
//	 */
//	TaskMonitorData(String fileName) {
//		this.fileName = fileName;
//		dataType = SET_FILENAME;
//	}
//	
//	/**
//	 * Constructor used to set the analysis progress. 
//	 * @param globalProgress
//	 * @param loadedProgress
//	 */
//	TaskMonitorData(int globalProgress, double loadedProgress) {
//		this.globalProgress = globalProgress;
//		this.loadedProgress = loadedProgress;
//		dataType = SET_PROGRESS;
//	}
	 
//	/**
//	 * 
//	 * @param status Task status
//	 * @return some string or other to represent the status. 
//	 */
//	public static String getStatusString(int status) {
//		switch(status) {
//		case TaskMonitor.TASK_COMPLETE:
//			return "Done";
//		case TaskMonitor.TASK_INTERRRUPTED:
//			return "Interrupted";
//		case TaskMonitor.TASK_RUNNING:
//			return "Running";
//		case TaskMonitor.TASK_CRASHED:
//			return "Crashed";
//		}
//		return "Unknown";
//	}
	
//	/**
//	 * Turn a status string back into a code. 
//	 * @param statusString
//	 * @return
//	 */
//	public static int getStatusCode(String statusString) {
//		for (int i = 0; i < 5; i++) {
//			if (statusString.equals(getStatusString(i))) {
//				return i;
//			}
//		}
//		return -1;
//	}
	
}
