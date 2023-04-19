package PamController.command;

import Acquisition.AcquisitionControl;
import PamController.PamController;
import pamViewFX.PamControlledGUIFX;

/**
 * Command to get PAMGuard to send back the batch processing status. 
 * 
 * @author dg50
 *
 */
public class BatchStatusCommand extends ExtCommand {

	public static final String commandId = "batchstatus";
	
	public BatchStatusCommand() {
		super(commandId, true);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String execute(String command) {
		AcquisitionControl daqControl = (AcquisitionControl) PamController.getInstance().findControlledUnit(AcquisitionControl.class, null);
		if (daqControl == null) {
			return null;
		}
		return daqControl.getBatchStatus();
	}

}
