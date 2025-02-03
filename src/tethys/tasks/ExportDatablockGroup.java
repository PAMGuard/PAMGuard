package tethys.tasks;

import PamView.dialog.warn.WarnOnce;
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

	@Override
	public void runBackgroundTasks(TaskGroupWorker taskGroupWorker) {
		try {
			if (exportDataBlockTask.canRun() == false) {
				String whyNot = exportDataBlockTask.whyNot;
				if (whyNot == null) {
					whyNot = "Unknown reason";
				}
				String msg = String.format("Task %s cannot run because : %s", exportDataBlockTask.getLongName(), whyNot);
				WarnOnce.showWarning("Task cannot run", msg, WarnOnce.WARNING_MESSAGE);
			}
			else {
				prepareTasks();
				exportDataBlockTask.runEntireTask(taskGroupWorker, this);
				completeTasks();
			}
			setCompletionStatus(TaskStatus.COMPLETE);
		}
		catch (Exception e) {
			e.printStackTrace();
			setCompletionStatus(TaskStatus.CRASHED);
		}
	}
}
