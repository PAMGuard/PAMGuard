package PamController.command;

import java.util.ArrayList;

import Acquisition.AcquisitionControl;
import PamController.PamControlledUnit;
import PamController.PamController;

/**
 * Get the status of PAMGuard 0 = idle, 1 = running
 * @author Doug Gillespie
 *
 */
public class StatusCommand extends ExtCommand {

	public StatusCommand() {
		super("Status", true);
	}

	@Override
	public boolean execute() {
		return true;
	}

	@Override
	public String getReturnString() {
		/*
		 * Need to work out if it's stalled or not by looking at the Daq systems. 
		 */
		return String.format("status %d", getRealStatus());
	}
	
	private int getRealStatus() {
		PamController pamController = PamController.getInstance();
		if (pamController.isInitializationComplete() == false) {
			return PamController.PAM_INITIALISING;
		}
		int runMode = PamController.getInstance().getRunMode();
		if (runMode == PamController.RUN_NETWORKRECEIVER) {
			return PamController.PAM_RUNNING;
		}
		int status = pamController.getPamStatus();
		if (status == PamController.PAM_IDLE) {
			status = PamController.PAM_IDLE;
		}
		else {
			ArrayList<PamControlledUnit> daqs = PamController.getInstance().findControlledUnits(AcquisitionControl.unitType);
			if (daqs != null) for (int i = 0; i < daqs.size(); i++) {
				try {
					AcquisitionControl daq = (AcquisitionControl) daqs.get(i);
					if (daq.isStalled()) {
						status = PamController.PAM_STALLED;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		WatchdogComms watchdogComms = PamController.getInstance().getWatchdogComms();
		return watchdogComms.getModifiedWatchdogState(status);
	}

}
