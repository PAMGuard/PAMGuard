package PamController.command;

import Acquisition.AcquisitionControl;
import PamController.PamController;
import offlineProcessing.OfflineTaskManager;
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
		if (PamController.getInstance().getRunMode() == PamController.RUN_NORMAL) {
			return getNormalModeStatus(command);
		}
		else {
			return getViewerModeStatus(command);
		}
	}

	private String getViewerModeStatus(String command) {
		return OfflineTaskManager.getManager().getBatchStatus();
	}

	private String getNormalModeStatus(String command) {
		AcquisitionControl daqControl = (AcquisitionControl) PamController.getInstance().findControlledUnit(AcquisitionControl.class, null);
		if (daqControl == null) {
			return null;
		}
		return daqControl.getBatchStatus();
	}

}
