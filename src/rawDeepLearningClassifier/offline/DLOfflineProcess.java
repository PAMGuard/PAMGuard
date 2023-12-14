package rawDeepLearningClassifier.offline;

import java.awt.Frame;

import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;
import rawDeepLearningClassifier.DLControl;


/**
 * The offline process.
 * @author Jamie Macaulay 
 *
 */
public class DLOfflineProcess {

	
	/**
	 * Reference to the DL control. 
	 */
	private DLControl dlControl;
	
	/**
	 * The offline task group
	 */
	private OfflineTaskGroup dlOfflineGroup;


	private DLOfflineTask dlOfflineTask;


	private OLProcessDialog mtOfflineDialog;

	public DLOfflineProcess(DLControl dlControl) {
		this.dlControl=dlControl;
		
		//create the offline task group. 
		dlOfflineGroup = new OfflineTaskGroup(dlControl, "Raw Deep Learning Classifier");
		
		//create the click train offline task
		dlOfflineTask = new DLOfflineTask(dlControl); 
		
		//add the task
		dlOfflineGroup.addTask(dlOfflineTask);
	}
	
	
	////---Swing stuff----/// should not be here but this is how PG works. 

	public void showOfflineDialog(Frame parentFrame) {
	
		//set the data block in the offline task. 
		//System.out.println("Click train offline data block " + clickTrainControl.getParentDataBlock());
		dlOfflineGroup.setPrimaryDataBlock(dlControl.getParentDataBlock());
		dlOfflineTask.setParentDataBlock(dlControl.getParentDataBlock());
		
		//if null open the dialog- also create a new offlineTask group if the datablock has changed. 
		if (mtOfflineDialog == null) {
			mtOfflineDialog = new OLProcessDialog(this.dlControl.getGuiFrame(), 
					dlOfflineGroup, "Deep Learning Classifier");
			//batchLocaliseDialog.setModalityType(Dialog.ModalityType.MODELESS);
		}
		mtOfflineDialog.enableControls();
		mtOfflineDialog.setVisible(true);
		
	}


}
