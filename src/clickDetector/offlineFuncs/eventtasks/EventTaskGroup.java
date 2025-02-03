package clickDetector.offlineFuncs.eventtasks;

import java.util.ArrayList;

import clickDetector.ClickControl;
import clickDetector.offlineFuncs.OfflineEventDataBlock;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import offlineProcessing.OfflineTask;
import offlineProcessing.OfflineTaskGroup;
import offlineProcessing.TaskActivity;
import offlineProcessing.TaskMonitorData;
import offlineProcessing.TaskStatus;

public class EventTaskGroup extends OfflineTaskGroup {

	private ClickControl clickControl;

	public EventTaskGroup(ClickControl clickControl, String settingsName) {
		super(clickControl, settingsName);
		this.clickControl = clickControl;
	}

	@Override
	public void runBackgroundTasks(TaskGroupWorker taskGroupWorker) {
		// do our own loops since we don't want the normal mapped data loading.
		try {
			prepareTasks();
			OfflineEventDataBlock dataBlock = clickControl.getClickDetector().getOfflineEventDataBlock();
			ArrayList<OfflineEventDataUnit> dataCopy = dataBlock.getDataCopy();
			int n = getNTasks();
			int nEvent = dataCopy.size();
			for (int i = 0; i < dataCopy.size(); i++) {
				for (int t = 0; t < n; t++) {
					OfflineTask aTask = getTask(t);
					if (aTask.isDoRun()) {
						aTask.processDataUnit(dataCopy.get(i));
					}
					TaskMonitorData tmd = new TaskMonitorData(TaskStatus.RUNNING, TaskActivity.PROCESSING, nEvent, i+1, "Processing " + i, dataCopy.get(i).getTimeMilliseconds());
					taskGroupWorker.publish(tmd);
				}
				if (isTaskCancelled()) {
					break;
				}
			}
			completeTasks();
			TaskMonitorData tmd = new TaskMonitorData(TaskStatus.COMPLETE, TaskActivity.IDLE, nEvent, nEvent, "Complete", 0);
			setCompletionStatus(TaskStatus.COMPLETE);
		}
		catch (Exception e) {
			e.printStackTrace();
			setCompletionStatus(TaskStatus.CRASHED);
		}

	}
}
