package clickTrainDetector.offline;

import java.awt.Window;

import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTask;
import offlineProcessing.OfflineTaskGroup;
import offlineProcessing.TaskMonitor;

/**
 * A click train offline dialog which ensure that the click train offline
 *  tasks cannot be run at the same time because they have different parent data blocks.. 
 * @author Jamie Macaulay 
 *
 */
public class CTProcessDialog extends OLProcessDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public CTProcessDialog(Window parentFrame, OfflineTaskGroup taskGroup, String title) {
		super(parentFrame, taskGroup, title);
		
		super.getDeleteOldDataBox().setText("Delete old click trains/classifications");
	}
	
	
	/**
	 * Enable controls in the dialog. 
	 * @param task - the task group in whihc enable controls has been called from
	 */
	public void enableControls(OfflineTask task) {
		boolean nr = getCurrentStatus() != TaskMonitor.TASK_RUNNING;		
		int nTasks = getTaskGroup().getNTasks();
		OfflineTask aTask;
		int selectedTasks = 0;
		for (int i = 0; i < nTasks; i++) {
			aTask = getTaskGroup().getTask(i);
			//System.out.println("Task: " + i + " can run?? " + aTask.canRun());
			getTaskCheckBoxs()[i].setEnabled(aTask.canRun() && nr);
			//added extra but here so that only one tasks can be run at a time- may change
			
			System.out.println("A task can run: !!" + aTask.canRun() + "  " + aTask.getDataBlock());
			//if more tasks are added to the click train detector. 
			if (aTask.canRun() == false || (aTask!=task && task!=null)) {
				getTaskCheckBoxs()[i].setSelected(false);
			}
			if (getSettingsButtons()[i] != null) {
				getSettingsButtons()[i].setEnabled(nr);
			}
			if (getTaskCheckBoxs()[i].isSelected()) {
				selectedTasks++;
			}
		}		
		
		//maybe should set not visible but perhaps warns the user they;re about to delete their 
		//previous click trains...
		super.getDeleteOldDataBox().setEnabled(false);
		
		getOkButton().setEnabled(selectedTasks > 0 && nr);
	}
	
	





}
