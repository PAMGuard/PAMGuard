package offlineProcessing;

import java.util.ArrayList;

import PamController.PamConfiguration;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;

/**
 * Some global management of offline tasks / task groups. 
 * @author dg50
 *
 */
public class OfflineTaskManager implements TaskMonitor {
	
	private static OfflineTaskManager singleInstance = null;
	
//	private ArrayList<OfflineTask> globalTaskList = new ArrayList();
	
	public static final String commandFlag = "-offlinetask";
	
	public ArrayList<String> commandLineTasks = new ArrayList();

	private BatchWorker worker;
	
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
		PamConfiguration mainConfig = PamController.getInstance().getPamConfiguration();
		ArrayList<OfflineTask> tasks = mainConfig.getAllOfflineTasks();
		return tasks;
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
		// check the tasks are not already in the list. 
		return taskGroup.addTasks(tasks);
	}

//	/**
//	 * Register a task in the global list. It's possible some tasks might get 
//	 * recreated, in which case when registered they will replace the previous one
//	 * This will cause trouble if two separate tasks have the same name, but that should 
//	 * not be possible. 
//	 * @param offlineTask
//	 */
//	public void registerTask(OfflineTask offlineTask) {
//		// if it exists, replace it. 
//		OfflineTask existingTask = findOfflineTask(offlineTask);
//		if (existingTask != null) {
//			int ind = globalTaskList.indexOf(existingTask);
//			globalTaskList.set(ind, offlineTask);
//		}
//		else {
//			globalTaskList.add(offlineTask);
//		}
//	}

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
		ArrayList<OfflineTask> globalTaskList = getAllOfflineTasks();
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
		ArrayList<OfflineTask> globalTaskList = getAllOfflineTasks();
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
		 * status string is nFiles, currentFile, generalStatus from PamController and current file as string. 
		 * String bs = String.format("%d,%d,%d,%s", nFiles,currentFile,generalStatus,currFile);
		 */
		// copy ref in case it's changed while doing this. This is enough to make it thread sfe
		TaskMonitorData tmd = latestTaskData;
		String bs;
		if (tmd == null) {
			bs = String.format("%d,%d,%d,%s", commandLineTasks.size(), 0, PamController.PAM_INITIALISING, "Initialising");
		}
		else {
			int nGroup = worker.usedGroups.size();
			int doneGroup = worker.currentGroup;
			if (doneGroup == nGroup && tmd.taskStatus == TaskStatus.COMPLETE) {
				bs = String.format("%d,%d,%d,%s", nGroup, nGroup, PamController.PAM_IDLE, "Jobs complete");
			}
			else {
				// work it out as a percentage of total work. 
				int maxV = Math.max(1, tmd.progMaximum);
				int curr = tmd.progValue;
				curr += doneGroup*maxV;
				maxV *= nGroup;
				bs = String.format("%d,%d,%d,%s", maxV, curr, PamController.PAM_RUNNING, "Running offline tasks");				
			}
		}
//		int generalStatus = PamController.getInstance().getRealStatus();
//		String bs = String.format("%d,%d,%d,%s", commandLineTasks.size(), 0, generalStatus, "Processing");
		return bs;
	}

	/**
	 * Organise and start processing offline tasks created for batck processing. 
	 */
	public void startBatchTasks() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Called from controller at end of first data load when PAMGuard is in viewer batch processing
	 * mode. Will launch a new thread to work it's way through all batch jobs. 
	 */
	public void launchOfflineBatchTasks() {
//		if (true) {
//			return;
//		}
		/*
		 * Start by making a list of task groups that have one or more tasks. 
		 */
		ArrayList<OfflineTaskGroup> usedGroups = new ArrayList<>();
		for (String taskCmd: commandLineTasks) {
			OfflineTask task = findOfflineTask(taskCmd);
			if (task == null) {
				System.out.println("Unable to find task " + taskCmd);
				continue;
			}
			System.out.println("Prepare offlinetask " + taskCmd + " " + task);
			OfflineTaskGroup taskGroup = task.getOfflineTaskGroup();
			if (usedGroups.contains(taskGroup) == false) {
				usedGroups.add(taskGroup);
			}
		}
		/*
		 *  then need to go through all tasks in each group and based on their index
		 *  eithr enable them or disable them. Must actively disable unused ones. 
		 */
		for (int g = 0; g < usedGroups.size(); g++) {
			OfflineTaskGroup taskGroup = usedGroups.get(g);
			TaskGroupParams groupParams = taskGroup.getTaskGroupParams();
			groupParams.dataChoice = TaskGroupParams.PROCESS_ALL;
			groupParams.deleteOld = true;
			groupParams.taskNote = "Batch process at " + PamCalendar.formatDBDateTime(System.currentTimeMillis());
			int nT = taskGroup.getNTasks();
			for (int i = 0; i < nT; i++) {
				OfflineTask task = taskGroup.getTask(i);
				String taskName = task.getLongName();
				boolean enable =  commandLineTasks.contains(taskName);
				groupParams.setTaskSelection(i, enable);
				System.out.printf("Set task eneable state for %s : %s in %s\n", taskName, enable, taskGroup.getUnitType());
			}
			System.out.println("Tasks in group is now " + taskGroup.getNTasks());
		}
		/*
		 *  can possibly now just get on with this using the standard dialog  with a few
		 *  mods. Disable every control on it and also set up a secondary monitor that passes
		 *  messages from the tasks back to here or somewhere where they can be packaged up 
		 *  and given to the batch processor as status messages. Batch status messages are requested
		 *  with a multicast broadcast of batchstatuscommand from the main controller, to which the running PAMGuards
		 *  reply with a String for a packet as at line 197 above. So 
		 *  a) set up a thread to launch the dialog and run everyting in it
		 *  b) have a secondary Taskmonitor back in this class. 
		 *  c) use the data received by that monitor to update the getBatchStatus string. 
		 *  d) when status is changed from processing to closed, then Batch Control should automatically 
		 *  close this instance and start next job. Easy !
		 *  What's in what thread ? dialog starts a swing worker. This is in AWT, so probably want out of it. 
		 *  Need to monitor state of dialog jobs since there may be more than one. 
		 */
		worker = new BatchWorker(usedGroups);
		Thread t = new Thread(worker);
		t.start();
		
	}
	
	private class BatchWorker implements Runnable {

		private ArrayList<OfflineTaskGroup> usedGroups;
		private volatile int currentGroup;
		private OLProcessDialog olDialog;

		public BatchWorker(ArrayList<OfflineTaskGroup> usedGroups) {
			this.usedGroups = usedGroups;
		}

		@Override
		public void run() {
			for (int i = 0; i < usedGroups.size(); i++) {
				olDialog = new OLProcessDialog(PamController.getMainFrame(), usedGroups.get(i), "Offline batch processing", 
						true, OfflineTaskManager.this);
				olDialog.setVisible(true);
				currentGroup++;
			}
		}
		
	}
	
	private volatile TaskMonitorData latestTaskData;

	@Override
	public void setTaskStatus(TaskMonitorData taskMonitorData) {
		latestTaskData = taskMonitorData;
//		System.out.println("Task monitor message " + taskMonitorData.taskStatus);
		if (taskMonitorData.taskStatus == TaskStatus.COMPLETE) {
			worker.olDialog.setVisible(false);
			if (worker.currentGroup >= worker.usedGroups.size()) {
				System.out.println(" all batch tasks complete.");
			}
		}
//		System.out.println("Status string " + getBatchStatus());
	}
}
