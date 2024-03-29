package PamController.command;

import java.util.ArrayList;

import Acquisition.AcquisitionControl;
import PamController.DataInputStore;
import PamController.PamControlledUnit;
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
		/**
		 * find a controlled unit thats a DataInputSource which should either be a sound daq or a Tritech daq module. 
		 */
		ArrayList<PamControlledUnit> inputSources = PamController.getInstance().findControlledUnits(DataInputStore.class, true);
		if (inputSources.size() == 0) {
			return null;
		}
//		DataInputStore daqControl = (DataInputStore) PamController.getInstance().findControlledUnit(DataInputStore.class, null);
		DataInputStore daqControl = (DataInputStore) inputSources.get(0);
//		System.out.println("Getting batch status from : " + daqControl);
		if (daqControl == null) {
			return null;
		}
		return daqControl.getBatchStatus();
	}

}
