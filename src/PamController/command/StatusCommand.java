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
	public String execute(String command) {
		return getReturnString();
	}

	public String getReturnString() {
		/*
		 * Need to work out if it's stalled or not by looking at the Daq systems. 
		 */
		return String.format("status %d", getRealStatus());
	}
	
	private int getRealStatus() {
		PamController pamController = PamController.getInstance();
		return pamController.getRealStatus();
	}


	@Override
	public String getHint() {
		return "Get the current PAMGuard status. 0=idle, 1=running";
	}
}
