package rawDeepLearningClassifier;

import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.SettingsPane;
import pamViewFX.PamControlledGUIFX;

/**
 * The JavaFX GUI for the DLControl...
 * @author Jamie Macaulay 
 *
 */
public class DLControlGUI extends PamControlledGUIFX {
	
	/**
	 * The dl control. 
	 */
	private DLControl dlControl;

	public DLControlGUI(DLControl dlcontrol) {
		this.dlControl = dlcontrol; 
	}
	
	/**
	 * The settings pane contains ALL possible setting for the module in one placed.
	 * Settings panes can be divided into tabs for more space etc.
	 * 
	 * @param <T>
	 * @return a Pane containing controls to change settings for module.
	 */
	public SettingsPane<?> getSettingsPane(){
		dlControl.getSettingsPane().setParams(dlControl.getDLParams());
		return dlControl.getSettingsPane();
	}
	
	
	@Override
	public void updateParams() {
		RawDLParams newParams=dlControl.getSettingsPane().getParams(dlControl.getDLParams()); 
		if (newParams!=null) {
			dlControl.setParams(newParams);
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
			//data source may have potentially changed. e.g. by a datamodelfx Need to set in params.
			//System.out.println("FFTControl: CHANGED_PROCESS_SETTINGS : " +fftProcess.getParentDataBlock());
			if (dlControl.getParentDataBlock()!=null){
				dlControl.getDLParams().groupedSourceParams.setDataSource(dlControl.getParentDataBlock().getDataName());
		}
		else dlControl.getDLParams().groupedSourceParams.setDataSource("");
			break;
		}
	}
	
	
}
