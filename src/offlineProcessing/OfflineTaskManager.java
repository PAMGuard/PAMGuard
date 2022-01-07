package offlineProcessing;

import java.util.ArrayList;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamguardMVC.PamDataBlock;

/**
 * Some global management of offline tasks / task groups. 
 * @author dg50
 *
 */
public class OfflineTaskManager {
	
	/**
	 * Get a list of ALL offline tasks registered with all modules 
	 * in all of PAMGUard. <p>
	 * N.B. Many tasks won't be registered. 
	 * @return list of all offline tasks
	 */
	public static ArrayList<OfflineTask> getAllOfflineTasks() {
		ArrayList<OfflineTask> allTasks = new ArrayList<>();
		PamController pamController = PamController.getInstance();
		int n = pamController.getNumControlledUnits();
		for (int i = 0; i < n; i++) {
			PamControlledUnit pcu = pamController.getControlledUnit(i);
			ArrayList<OfflineTask> unitTasks = pcu.getOfflineTasks();
			if (unitTasks != null) {
				allTasks.addAll(unitTasks);
			}
		}
		return allTasks;
	}
	
	/**
	 * Get a list of all tasks in the system which have the given parent data block
	 * @param taskParentDataBlock parent data block for tasks
	 * @return list of available tasks. 
	 */
	public static ArrayList<OfflineTask> getOfflineTasks(PamDataBlock taskParentDataBlock) {
		ArrayList<OfflineTask> allTasks = getAllOfflineTasks();
		ArrayList<OfflineTask> wantedTasks = new ArrayList<>();
		for (OfflineTask task : allTasks) {
			if (task == null) continue;
			if (task.getDataBlock() == taskParentDataBlock) {
				wantedTasks.add(task);
			}
		}
		return wantedTasks;
	}
	
	/**
	 * Add all available tasks from the system which use the given datablock 
	 * as input. Note that tasks already in the goup will NOT be added a second time. 
	 * @param taskGroup Task group to add tasks to
	 * @param taskParentDataBlock parent data block
	 * @return number of tasks added
	 */
	public static int addAvailableTasks(OfflineTaskGroup taskGroup, PamDataBlock taskParentDataBlock) {
		ArrayList<OfflineTask> tasks = getOfflineTasks(taskParentDataBlock);
		return taskGroup.addTasks(tasks);
	}

}
