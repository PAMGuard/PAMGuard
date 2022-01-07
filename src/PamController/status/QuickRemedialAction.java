package PamController.status;

import java.awt.event.ActionListener;

import PamController.PamControlledUnit;

/**
 * Quick remedial action. Can generally be used to wrap an existing menu item handler
 * 
 * @author dg50
 *
 */
public class QuickRemedialAction implements RemedialAction {

	private PamControlledUnit pamControlledUnit;
	private String info;
	private ActionListener action;
	public QuickRemedialAction(PamControlledUnit pamControlledUnit, String info, ActionListener action) {
		this.pamControlledUnit = pamControlledUnit;
		this.info = info;
		this.action = action;
	}

	@Override
	public String getInfo() {
		return info;
	}

	@Override
	public ModuleStatus takeAction(ModuleStatus currentStatus) {
		if (action != null) {
			action.actionPerformed(null);
		}
		if (pamControlledUnit != null) {
			return pamControlledUnit.getModuleStatus();
		}
		else {
			return null;
		}
	}

}
