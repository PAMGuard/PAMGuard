package cpod.fx;

import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.SettingsPane;
import cpod.CPODControl2;
import cpod.CPODParams;
import pamViewFX.PamControlledGUIFX;

public class CPODGUIFX extends PamControlledGUIFX {
	
	/**
	 * Reference to the CPOD control. 
	 */
	private CPODControl2 cpodControl;

	public CPODGUIFX(CPODControl2 cpodControl2) {
		cpodControl = cpodControl2; 
	}

	
	/**
	 * The settings pane contains ALL possible setting for the module in one placed.
	 * Settings panes can be divided into tabs for more space etc.
	 * 
	 * @param <T>
	 * @return a Pane containing controls to change settings for module.
	 */
	public SettingsPane<?> getSettingsPane(){
		cpodControl.getSettingsPane().setParams(cpodControl.getCPODParam());
		return cpodControl.getSettingsPane();
	}
	
	
	@Override
	public void updateParams() {
		CPODParams newParams=cpodControl.getSettingsPane().getParams(cpodControl.getCPODParam()); 
		if (newParams!=null) {
			cpodControl.setCPODParams(newParams);
		}
		//setup the controlled unit. 
		cpodControl.setupControlledUnit(); 
	}
	
	@Override
	public void notifyGUIChange(int changeType) {
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			break;
		case PamControllerInterface.CHANGED_PROCESS_SETTINGS:
		}
	}
	
}
