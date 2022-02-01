package matchedTemplateClassifer.offline;

import java.awt.Frame;

import matchedTemplateClassifer.MTClassifierControl;
import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;

/**
 * 
 * Offline processes for the matched click classifier. 
 * <p>
 * Used in viewer mode to run the click train algorithm 
 * 
 * @author Jamie Macaulay
 *
 */
public class MTOfflineProcess {
	
	/**
	 * Reference to the click train control
	 */
	private MTClassifierControl mtContorl;
	
	/**
	 * Click train offline task group. 
	 */
	private OfflineTaskGroup mtOfflineGroup;

	/**
	 * The AWT dialog. 
	 */
	private OLProcessDialog mtOfflineDialog;

	private MTClassifierOfflineTask mtOfflineTask;


	/**
	 * Constructor for matched template classifier offline process. 
	 * @param mtControl - the mt classifier control
	 */
	public MTOfflineProcess(MTClassifierControl mtControl) {
		this.mtContorl=mtControl;
		
		//create the offline task group. 
		mtOfflineGroup = new OfflineTaskGroup(mtControl, "Match Template Classifier");
		
		//create the click train offline task
		mtOfflineTask = new MTClassifierOfflineTask(mtControl); 
		
		//add the task
		mtOfflineGroup.addTask(mtOfflineTask);

	}
		
	/***********GUI***********/
	
	/**
	 * Show the offline dialog for the matched click classifier
	 * @param parentFrame 
	 */
	public void showOfflineDialog(Frame parentFrame) {
		
		//set the data block in the offline task. 
		//System.out.println("Click train offline data block " + clickTrainControl.getParentDataBlock());
		mtOfflineGroup.setPrimaryDataBlock(mtContorl.getParentDataBlock());
		mtOfflineTask.setParentDataBlock(mtContorl.getParentDataBlock());
		mtOfflineTask.addAffectedDataBlock(mtContorl.getParentDataBlock());
		
		//if null open the dialog- also create a new offlineTask group if the datablock has changed. 
		if (mtOfflineDialog == null) {
			mtOfflineDialog = new OLProcessDialog(this.mtContorl.getPamView().getGuiFrame(), 
					mtOfflineGroup, "Match Template Classifier");
			//batchLocaliseDialog.setModalityType(Dialog.ModalityType.MODELESS);
		}
		mtOfflineDialog.enableControls();
		mtOfflineDialog.setVisible(true);
	}
	
	
	/************************/
	
}

