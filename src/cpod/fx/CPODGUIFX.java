package cpod.fx;

import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.SettingsPane;
import cpod.CPODControl2;
import cpod.CPODParams;
import pamViewFX.PamControlledGUIFX;

public class CPODGUIFX extends PamControlledGUIFX {
	
	/**
	 * The dl control. 
	 */
	private CPODControl2 dlControl;

	public CPODGUIFX(CPODControl2 cpodControl2) {
		// TODO Auto-generated constructor stub
	}

	
	/**
	 * The settings pane contains ALL possible setting for the module in one placed.
	 * Settings panes can be divided into tabs for more space etc.
	 * 
	 * @param <T>
	 * @return a Pane containing controls to change settings for module.
	 */
	public SettingsPane<?> getSettingsPane(){
		dlControl.getSettingsPane().setParams(dlControl.getCPODParam());
		return dlControl.getSettingsPane();
	}
	
	
	@Override
	public void updateParams() {
		CPODParams newParams=dlControl.getSettingsPane().getParams(dlControl.getCPODParam()); 
		if (newParams!=null) {
			dlControl.setCPODParams(newParams);
		}
		//setup the controlled unit. 
		dlControl.setupControlledUnit(); 
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
