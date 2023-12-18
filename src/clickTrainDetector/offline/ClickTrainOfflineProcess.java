package clickTrainDetector.offline;

import java.awt.Frame;

import clickTrainDetector.ClickTrainControl;
import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;

/**
 * 
 * Offline processes for the click detector. 
 * <p>
 * Used in viewer mode to run the click train algorithm 
 * @author Jamie Macaulay
 *
 */
public class ClickTrainOfflineProcess {
	
	/**
	 * Reference to the click train control
	 */
	private ClickTrainControl clickTrainControl;
	
	/**
	 * Click train offline task group. 
	 */
	private OfflineTaskGroup clickTrainOfflineGroup;

	/**
	 * The AWT dialog. 
	 */
	private OLProcessDialog clickTrainDialog;

	/**
	 * The click train offline task. This processes clicks and creates click trains
	 */
	private ClickTrainOfflineTask clickTrain;

	/**
	 * Click classification offline task 
	 */
	private CTClassifierOfflineTask clickTrainClassification;

	/**
	 * Constructor for click train offline process. 
	 * @param clickTrainControl - the click train control. 
	 */
	public ClickTrainOfflineProcess(ClickTrainControl clickTrainControl) {
		this.clickTrainControl=clickTrainControl;
		
		//create the offline task group. 
		clickTrainOfflineGroup = new OfflineTaskGroup(clickTrainControl, "Click Train Detection");
		
		//create the click train offline task
		clickTrain = new ClickTrainOfflineTask(clickTrainControl); 
		//create the click train offline task
		clickTrainClassification = new 	CTClassifierOfflineTask(clickTrainControl); 
		
		//add the task
		clickTrainOfflineGroup.addTask(clickTrain);
		//TODO - the click train offline group
		clickTrainOfflineGroup.addTask(clickTrainClassification);
		
		//always delete old database entries for the click train detector.
		//This is to prevent multiple parents fro clicks and the ensuing confusion
		//and processing mess that would cause. 
		clickTrainOfflineGroup.getTaskGroupParams().deleteOld = true;

	}
	
	/***********GUI***********/
	
	/**
	 * Show the offline dialog for the offline click train. 
	 * @param parentFrame 
	 */
	public void showOfflineDialog(Frame parentFrame) {

		//set the data block in the offline task. 
//		System.out.println("Click train offline data block " + clickTrainControl.getParentDataBlock());
		
		clickTrainOfflineGroup.setPrimaryDataBlock(clickTrainControl.getParentDataBlock());
		clickTrain.setParentDataBlock(clickTrainControl.getParentDataBlock());
		
		//if null open the dialog- also create a new offlineTask group if the datablock has changed. 
		if (clickTrainDialog == null) {
			clickTrainDialog = new CTProcessDialog(this.clickTrainControl.getGuiFrame(), 
					clickTrainOfflineGroup, "Click Train Detection");
			//batchLocaliseDialog.setModalityType(Dialog.ModalityType.MODELESS);
		}
		
		clickTrainDialog.setVisible(true);
	}
	
	/************************/

}
