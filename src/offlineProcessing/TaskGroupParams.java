package offlineProcessing;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;

/**
 * Parameter control for offline task groups. 
 * @author Doug Gillespie
 *
 */
public class TaskGroupParams implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	/**
	 * process only the data currently loaded in memory.
	 */
	static public final int PROCESS_LOADED = 0;
	/**
	 * Process all data in the datastore (binary, database, or wherever). 
	 */
	static public final int PROCESS_ALL = 1;
	
	/**
	 * Process new data only - i.e. data which arrived after the lastDataTime field. 
	 */
	static public final int PROCESS_NEW = 2;
	
	/**
	 * Time of the last bit of data to be processed. 
	 */
	public long lastDataTime;
	
	/**
	 * 
	 * Process a specific period of data
	 */
	static public final int PROCESS_SPECIFICPERIOD = 3;

	/**
	 * Process imported time chunks. 
	 */
	public static final int PROCESS_TME_CHUNKS = 4;

	
	/**
	 * used in PROCESS_SPECIFICPERIOD mode-will not affect PROCESS_NEW and lastDataTime
	 * boolean could be used to govern this in future
	 * <p>
	 * Start time used for one off  re-processing jobs 
	 */
	public long startRedoDataTime = 1;
	
	/**
	 * used in PROCESS_SPECIFICPERIOD mode-will not affect PROCESS_NEW and lastDataTime
	 * boolean could be used to govern this in future
	 * <p>
	 * End time used for one off  re-processing jobs 
	 */
	public long endRedoDataTime = Long.MAX_VALUE;
	
	/**
	 * The data to process flag e.g. only data loaded in memory or the entire dataset.
	 */
	public int dataChoice = PROCESS_LOADED;
	
	
	/**
	 * Delete old database entries. 
	 */
	public boolean deleteOld = false;
	
	private boolean[] taskSelection;
	
	/**
	 * Each time processData is called we can either load
	 * ALL of the secondary data block data for that block 
	 * or can only call loading secondary data for the times
	 * we're interested in. IF this is true, we'll load everything. 
	 */
	public boolean loadBlockedSecondaryData = false;

	/**
	 * An array of time chunks in millis. 
	 */
	public ArrayList<long[]> timeChunks;
	
	/**
	 * Set the selection state of a particular task. 
	 * @param iTask task number (counting from 0)
	 * @param selState selection state
	 */
	public void setTaskSelection(int iTask, boolean selState) {
		if (taskSelection == null) {
			taskSelection = new boolean[iTask+1];
		}
		else if (iTask >= taskSelection.length) {
			taskSelection = Arrays.copyOf(taskSelection, iTask+1);
		}
		taskSelection[iTask] = selState;
	}
	
	/**
	 * Get the selection state of a task
	 * @param iTask task number (counting from 0)
	 * @return state (default is false). 
	 */
	public boolean getTaskSelection(int iTask) {
		if (taskSelection == null || iTask >= taskSelection.length) {
			return false;
		}
		return taskSelection[iTask];
	}


	@Override
	protected TaskGroupParams clone() {
		// TODO Auto-generated method stub
		try {
			return (TaskGroupParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("taskSelection");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return taskSelection;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
