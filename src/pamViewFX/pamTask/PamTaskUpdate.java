package pamViewFX.pamTask;

/**
 * Class for passing information from loading tasks to the FX GUI. 
 * @author Jamie Macaulay
 *
 */
public abstract class PamTaskUpdate {
	
	//reserve positive numbers for other stuff. 

	/**
	 * The loading of data is idle
	 */
	static public final int STATUS_IDLE = 0;
	
	/**
	 * Currently counting files
	 */
	static public final int STATUS_COUNTING_FILES = -1;
	
	/**
	 * Analysing files
	 */
	static public final int STATUS_ANALYSING_FILES = -2;
	
	/**
	 * Sorting something, e.g. file list
	 */
	static public final int STATUS_SORTING = -3;
	
	/*
	 *Serializing data 
	 */
	static public final int STATUS_SERIALIZING = -4;
	
	/*
	 *Deserializing data 
	 */
	static public final int STATUS_DESERIALIZING = -5;
	
	/**
	 * The task has completed successfully
	 */
	static public final int STATUS_DONE = -1000;
	
	/**
	 * The task has completed but an error was flagged. It is possible that the task did not complete successfully. 
	 */
	static public final int STATUS_DONE_ERROR = -999
			;


	/**
	 * Indicator that this task update has two progress updates- one larger overall progress and a fine scale progress. 
	 * Used if there are two progress bars 
	 * Developer Note: Made this a boolean instead of enum as you only ever get one or two progress bars in a program. 
	 */
	boolean twoProgBars=false; 

	/**
	 * The current status
	 */
	private int status=STATUS_IDLE;
		
	
	/**
	 * Get the name of the load thread. Note the name is also the unique identifier for the
	 * running thread so cannot be the same between two different threads.
	 * @return the name of the thread.
	 */
	public abstract String getName();
	
	/**
	 * Get the progress of the load thread. This is a value between 0 and 1 were 1 is equivalent to 100% or the thread having finished.
	 * <p>
	 * Note that ProgressIndicator.INTERMEDIATE can be used as an update value to set progress to intermediate. 
	 * @return the progress of the thread, between 0 and 1. 
	 */
	public abstract double getProgress();
	
	
	/**
	 * There may be a need for two progress bars for long task- one showing overall progress and the other showing more fine scale loading tasks.
	 * If this is the case then 
	 * <p>
	 * Note that ProgressIndicator.INTERMEDIATE can be used as an update value to set progress to intermediate. 
	 * @return the progress of the thread, between 0 and 1. 
	 */
	public double getProgress2(){
		return -1;
	}
		
	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Set the status of the thread. This can either a generic status from the abstract class or a custom status. 
	 * @param status flag to set. 
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	
	/**
	 * There may be two progress updates, one overall progress and then a finer scale progress. 
	 * @return true if two progress updates. 
	 */
	public boolean isDualProgressUpdate() {
		return twoProgBars;
	}

	/**
	 * Set if there should be two progress updates, one overall progress and then a finer scale progress. 
	 * @param true if two progress updates. 
	 */
	public void setDualProgressUpdate(boolean twoProgUpdate) {
		this.twoProgBars = twoProgUpdate;
	}
	
	/**
	 * Returns a message indicating progress. Can be overriden to supply other messages. 
	 * @return
	 */
	public String getProgressString(){
		String progMessage=null; 
		switch (status){
		case STATUS_IDLE:
		break;
		
		}
		return progMessage;
	}
	
	/**
	 * If two updates are available then this is used to return the fine progress update message.
	 * @return
	 */
	public String getProgressString2(){
		return "";
	}
	
	
}
