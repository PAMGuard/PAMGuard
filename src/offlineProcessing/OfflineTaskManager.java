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
	
	public static final String commandFlag = "-offlinetask";
	
	public ArrayList<String> commandLineTasks = new ArrayList();
	
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
	@SuppressWarnings("rawtypes")
	public OfflineTask findOfflineTask(OfflineTask offlineTask) {
		return findOfflineTask(offlineTask.getLongName());
	}
	
	/**
	 * Find a registered task based on it's module type, module name and task name. This should 
	 * be enough to uniquely identify every task. 
	 * @param unitType
	 * @param unitName
	 * @param taskName
	 * @return matching task or null. 
	 */
	@SuppressWarnings("rawtypes")
	public OfflineTask findOfflineTask(String unitType, String unitName, String taskName) {
		// could possibly also do a check on class type ????
		for (OfflineTask aTask : globalTaskList) {
			if (!aTask.getUnitType().equals(unitType)) {
				continue;
			}
			if (!aTask.getUnitName().equals(unitName)) {
				continue;
			}
			if (aTask.getName().equals(taskName)) {
				return aTask;
			}
		}
		return null;
	}
	
	/**
	 * Another way of finding offline tasks based on their long name. This is basically
	 * the three names unitType, unitName and taskName concatenated together. Get's used
	 * for some task management such as passing batch processing instructions. 
	 * @param taskLongName 
	 * @return matching task or null. 
	 */
	@SuppressWarnings("rawtypes")
	public OfflineTask findOfflineTask(String taskLongName) {
		for (OfflineTask aTask : globalTaskList) {
			if (aTask.getLongName().equals(taskLongName)) {
				return aTask;
			}
		}
		return null;
	}

	/**
	 * Add a task listed in the command line when PAMGuard was started. 
	 * @param taskLongName
	 */
	public void addCommandLineTask(String taskLongName) {
		commandLineTasks.add(taskLongName);
	}

	/**
	 * The list of tasks from the command line. 
	 * @return the commandLineTasks
	 */
	public ArrayList<String> getCommandLineTasks() {
		return commandLineTasks;
	}

	/**
	 * Get the status of jobs to pass back to the batch process controller. 
	 * @return
	 */
	public String getBatchStatus() {
		/**
		 * this needs to largely follow the format of the data in folderinputsystem:
		 * String bs = String.format("%d,%d,%d,%s", nFiles,currentFile,generalStatus,currFile);
		 */
		int generalStatus = PamController.getInstance().getRealStatus();
		String bs = String.format("%d,%d,%d,%s", commandLineTasks.size(), 0, generalStatus, "Processing");
		return bs;
	}
}
