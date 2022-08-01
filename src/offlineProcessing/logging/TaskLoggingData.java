package offlineProcessing.logging;

import PamguardMVC.PamDataUnit;
import offlineProcessing.OfflineTask;
import offlineProcessing.TaskMonitorData;

/**
 * Wee class for holding the latest data on each task, most importantly the 
 * database index which is needed for updates. 
 * @author dg50
 *
 */
public class TaskLoggingData extends PamDataUnit {
	
	protected OfflineTask task;
	protected int databaseIndex; 
	protected TaskMonitorData monitorData;
	
	public TaskLoggingData(OfflineTask task, int databaseIndex, TaskMonitorData monitorData) {
		super(System.currentTimeMillis());
		this.task = task;
		this.databaseIndex = databaseIndex;
		this.monitorData = monitorData;
	}

}
