package tethys.tasks;

import offlineProcessing.TaskStatus;

/**
 * Detections handler already has a very  system for looping through 
 * data which is a bit different to the standard one in offline task, 
 * so an going to override the main background function and do it our
 * own way! 
 * @author dg50
 *
 */
public class ExportDatablockGroup extends TethysTaskGroup {

	private ExportDataBlockTask exportDataBlockTask;

	public ExportDatablockGroup(ExportDataBlockTask tethysTask) {
		super(tethysTask);
		this.exportDataBlockTask = tethysTask;
	}

	public void runBackgroundTasks(TaskGroupWorker taskGroupWorker) {
		try {
			prepareTasks();
			exportDataBlockTask.runEntireTask(taskGroupWorker, this);
			completeTasks();
			setCompletionStatus(TaskStatus.COMPLETE);
		}
		catch (Exception e) {
			e.printStackTrace();
			setCompletionStatus(TaskStatus.CRASHED);
		}
	}
}
