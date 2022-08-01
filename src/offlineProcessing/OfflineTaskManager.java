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
	
	private static OfflineTaskManager singleInstance = null;
	
	private ArrayList<OfflineTask> globalTaskList = new ArrayList();
	
	public static OfflineTaskManager getManager() {
		if (singleInstance == null) {
			singleInstance = new OfflineTaskManager();
		}
		return singleInstance;
	}
	
	/**
	 * Get a list of ALL offline tasks registered with all modules 
	 * in all of PAMGUard. <p>
	 * N.B. Many tasks won't be registered. 
	 * @return list of all offline tasks
	 */
	public ArrayList<OfflineTask> getAllOfflineTasks() {
//		ArrayList<OfflineTask> allTasks = new ArrayList<>();
//		PamController pamController = PamController.getInstance();
//		int n = pamController.getNumControlledUnits();
//		for (int i = 0; i < n; i++) {
//			PamControlledUnit pcu = pamController.getControlledUnit(i);
//			ArrayList<OfflineTask> unitTasks = pcu.getOfflineTasks();
//			if (unitTasks != null) {
//				allTasks.addAll(unitTasks);
//			}
//		}
//		return allTasks;
		return globalTaskList;
	}
	
	/**
	 * Get a list of all tasks in the system which have the given parent data block
	 * @param taskParentDataBlock parent data block for tasks
	 * @return list of available tasks. 
	 */
	public ArrayList<OfflineTask> getOfflineTasks(PamDataBlock taskParentDataBlock) {
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
	 * Get a list of all tasks in the system which associate with the given PAMGuard module
	 * @param pamControlledUnit parent PAMGuard module
	 * @return list of available tasks. 
	 */
	public ArrayList<OfflineTask> getOfflineTasks(PamControlledUnit pamControlledUnit) {
		ArrayList<OfflineTask> allTasks = getAllOfflineTasks();
		ArrayList<OfflineTask> wantedTasks = new ArrayList<>();
		for (OfflineTask task : allTasks) {
			if (task == null) continue;
			if (task.getTaskControlledUnit() == pamControlledUnit) {
				wantedTasks.add(task);
			}
		}
		return wantedTasks;
	}
	
	/**
	 * Add all available tasks from the system which use the given datablock 
	 * as primary input. 
	 * @param taskGroup Task group to add tasks to
	 * @param taskParentDataBlock parent data block
	 * @return number of tasks added
	 */
	public int addAvailableTasks(OfflineTaskGroup taskGroup, PamDataBlock taskParentDataBlock) {
		ArrayList<OfflineTask> tasks = getOfflineTasks(taskParentDataBlock);
		return taskGroup.addTasks(tasks);
	}

	/**
	 * Register a task in the global list. It's possible some tasks might get 
	 * recreated, in which case when registered they will replace the previous one
	 * This will cause trouble if two separate tasks have the same name, but that should 
	 * not be possible. 
	 * @param offlineTask
	 */
	public void registerTask(OfflineTask offlineTask) {
		// if it exists, replace it. 
		OfflineTask existingTask = findOfflineTask(offlineTask);
		if (existingTask != null) {
			int ind = globalTaskList.indexOf(existingTask);
			globalTaskList.set(ind, offlineTask);
		}
		else {
			globalTaskList.add(offlineTask);
		}
	}

	/**
	 * find a task with the same module type, module name and task name. This should 
	 * be enough to uniquely identify every task. 
	 * @param offlineTask
	 * @return matching task or null. 
	 */
	public OfflineTask findOfflineTask(OfflineTask offlineTask) {
		return findOfflineTask(offlineTask.getUnitType(), offlineTask.getUnitName(), offlineTask.getName());
	}
	
	/**
	 * Find a registered task based on it's module type, module name and task name. This should 
	 * be enough to uniquely identify every task. 
	 * @param unitType
	 * @param unitName
	 * @param taskName
	 * @return matching task or null. 
	 */
	public OfflineTask findOfflineTask(String unitType, String unitName, String taskName) {
		// could possibly also do a check on class type ????
		for (OfflineTask aTask : globalTaskList) {
			if (aTask.getUnitType().equals(unitType) == false) {
				continue;
			}
			if (aTask.getUnitName().equals(unitName) == false) {
				continue;
			}
			if (aTask.getName().equals(taskName)) {
				return aTask;
			}
		}
		return null;
	}
}
