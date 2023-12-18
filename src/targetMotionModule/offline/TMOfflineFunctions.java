package targetMotionModule.offline;

import java.awt.Window;

import PamguardMVC.PamDataBlock;
import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;
import targetMotionModule.TargetMotionControl;


/**
 * This class deals with processes in the target Motion localiser which ONLY occur in offline mode. The major difference between the TM localiser on offline and real time mode is the 'Batch run' function. In real time the TM localiser will attempt 
 * to localise every annotated groups of detections- e.g click trains, however in offline mode the user can batch localise, either a whole dataabase or just loaded data. 
 * <p>
 * Note that any types of offline task group can be created. Hence the batch process functionality is not just restricted to localising target motion data. For example summary stats of Target motion events could be exported or perhaps a file format created for another program to do the localising...
 * 
 *  
 *
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public class TMOfflineFunctions {
	
	TargetMotionControl targetMotionControl;
	
	OfflineTaskGroup tMOfflineTaskGroup;
	
	BatchTMLocalisation batchTMLocalisation;

	private TMOLProcessDialog batchLocaliseDialog;

	private PamDataBlock currentDataBlock=null;
	
	
	public TMOfflineFunctions(TargetMotionControl targetMotionControl){
		this.targetMotionControl=targetMotionControl;
	}
	
	
	private OfflineTaskGroup getOfflineTaskGroup() {
		//keep a record of the datablock used to create the oiffline task groups. 
		currentDataBlock=targetMotionControl.getCurrentDataBlock();
		
		tMOfflineTaskGroup=new OfflineTaskGroup(targetMotionControl, "TM Batch Process");
		batchTMLocalisation=new BatchTMLocalisation(targetMotionControl); 
		batchTMLocalisation.setParentDataBlock(targetMotionControl.getCurrentDataBlock());
		tMOfflineTaskGroup.addTask(batchTMLocalisation);
		return tMOfflineTaskGroup;
	}
	
	public void openBatchRunDialog() {
		//TODO
		//when we change datablock the taks group is going to stay the same- need to make sure it changes
		
		if (batchLocaliseDialog == null || currentDataBlock!=targetMotionControl.getCurrentDataBlock()) {
			batchLocaliseDialog = new TMOLProcessDialog(targetMotionControl.getGuiFrame(), 
					getOfflineTaskGroup(), "Batch Localise");
			//batchLocaliseDialog.setModalityType(Dialog.ModalityType.MODELESS);
		}
		batchLocaliseDialog.setVisible(true);
		
	}
	
	public class TMOLProcessDialog extends OLProcessDialog{

		private static final long serialVersionUID = 1L;

		public TMOLProcessDialog(Window parentFrame,
				OfflineTaskGroup taskGroup, String title) {
			super(parentFrame, taskGroup, title);
		}
		
		@Override()
		public void cancelButtonPressed(){
			super.cancelButtonPressed();
		}
		
	}


}
