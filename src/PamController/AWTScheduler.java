package PamController;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Class to schedule SwingWorker tasks so that they execute one
 * at a time. SwingWorkers do of course do their work in a different 
 * thread to the AWT thread. The point of this scheduler is to ensure
 * that they execute one by one, can themselves update the GUI (for instance
 * with progress bars or text output) but that the GUI itself is locked 
 * for user input to avoid problems causes by impatient users. 
 * @author Doug Gillespie
 *
 */
public class AWTScheduler {

	private static AWTScheduler singleInstance;

	private PamController pamController;

	private List<Runnable> taskList = Collections.synchronizedList(new LinkedList<Runnable>());

	private AWTScheduler() {
		pamController = PamController.getInstance();
	}

	/**
	 * Get singleton instance. 
	 * @return singleton instance of AWTScheduler. 
	 */
	synchronized public static AWTScheduler getInstance() {
		if (singleInstance == null) {
			singleInstance = new AWTScheduler();
		}
		return singleInstance;
	}

	/**
	 * Schedules a task for execution. Starts from the 
	 * AWT thread, but SwingWorker execution is in a different thread.
	 * <p>
	 * This function will always return immediately, using invokeLAter
	 * to start the task on AWT when the function calling this has
	 * completed.  
	 * @param swingWorker Prepared swing worker to execute
	 * @return number of tasks in the queue in front of this task. 
	 */
	public int scheduleTask(Runnable swingWorker) {
		synchronized(taskList) {
			int currentTasks = taskList.size();
			taskList.add(swingWorker);
			startNextTask();
			if (currentTasks == 0) {
				/**
				 * Start the task immediately, otherwise it will be invoked
				 * when the preceeding task has finished. 
				 */
				startNextTask();
			}
			return currentTasks;
		}

	}
	
	/**
	 * Start the next task in the list. 
	 * <p>If it's a SwingWorker, then run it as a scheduled worker task which 
	 * will handle the disabling and re-enabling of the PAMGUARD GUI.  
	 * <p>If it's a plain old runnable, then just send it off to the AWT
	 * thread with invoke later and go straight on to setting up the 
	 * task after it. 
	 */
	public void startNextTask() {
		int nTasks = taskList.size();
		if (nTasks == 0) {
			pamController.enableGUIControl(true);
			return;
		}
		Runnable nextTask = taskList.get(0);		
		if (SwingWorker.class.isAssignableFrom(nextTask.getClass())) {
			SwingUtilities.invokeLater(new ScheduledWorkerTask((SwingWorker) taskList.get(0)));
		}
		else {
			SwingUtilities.invokeLater(nextTask);
			taskList.remove(0);
			startNextTask();
		}
	}

	/**
	 * USed by InvokeLater to start a SwingWorker task.  
	 * @author Doug Gillespie
	 *
	 */
	private class ScheduledWorkerTask implements Runnable {

		private SwingWorker swingWorker;

		public ScheduledWorkerTask(SwingWorker swingWorker) {
			super();
			this.swingWorker = swingWorker;
		}

		@Override
		public void run() {
			pamController.enableGUIControl(false);
//			System.out.println("Starting swing worker " + swingWorker.toString() + " " + swingWorker.hashCode());
			swingWorker.addPropertyChangeListener(new WorkerPropertyChangeListener(swingWorker));
			swingWorker.execute();
		}


	}

	private class WorkerPropertyChangeListener implements PropertyChangeListener {

		private SwingWorker swingWorker;

		public WorkerPropertyChangeListener(SwingWorker swingWorker) {
			super();
			this.swingWorker = swingWorker;
		}

		@Override
		public void propertyChange(PropertyChangeEvent pEvent) {
			
//			System.out.println("Property change event = " + pEvent.getNewValue().toString() + " for " + swingWorker + " source " +
//					pEvent.getSource() + " " + pEvent.getSource().hashCode());
			/*
			 * Get's called when the task has started and ended.
			 * When it's ended, start the next task in the queue. 
			 */
			if (pEvent.getNewValue() == SwingWorker.StateValue.DONE) {
				synchronized (taskList) {
					taskList.remove(swingWorker);
					startNextTask();
				}
			}
		}

	}
}
