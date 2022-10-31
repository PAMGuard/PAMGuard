package matchedTemplateClassifer;

import PamController.PamControlledUnitGUI;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamController.SettingsPane;
import PamView.WrapperControlledGUISwing;
import pamViewFX.PamControlledGUIFX;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.DLControlGUI;
import rawDeepLearningClassifier.RawDLParams;

/**
 * The JavaFX GUI for the Matched click classifier control...
 * @author Jamie Macaulay 
 *
 */
public class MTControlGUI extends PamControlledGUIFX {
	
	/**
	 * The dl control. 
	 */
	private MTClassifierControl mtControl;
	

	public MTControlGUI(MTClassifierControl dlcontrol) {
		this.mtControl = dlcontrol; 
	}
	
	/**
	 * The settings pane contains ALL possible setting for the module in one placed.
	 * Settings panes can be divided into tabs for more space etc.
	 * 
	 * @param <T>
	 * @return a Pane containing controls to change settings for module.
	 */
	public SettingsPane<?> getSettingsPane(){
		mtControl.getSettingsPane().setParams(mtControl.getMTParams());
		return mtControl.getSettingsPane();
	}
	
	
	@Override
	public void updateParams() {
		MatchedTemplateParams newParams=mtControl.getSettingsPane().getParams(mtControl.getMTParams()); 
		if (newParams!=null) {
			mtControl.setMTParams(newParams);
		}
		//setup the controlled unit. 
		mtControl.setupControlledUnit(); 
	}
	
	@Override
	public void notifyGUIChange(int changeType) {
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			break;
		case PamControllerInterface.CHANGED_PROCESS_SETTINGS:
			//data source may have potentially changed. e.g. by a datamodelfx Need to set in params.
			//System.out.println("FFTControl: CHANGED_PROCESS_SETTINGS : " +fftProcess.getParentDataBlock());
			if (mtControl.getParentDataBlock()!=null){
				mtControl.getMTParams().dataSourceName = mtControl.getParentDataBlock().getDataName();
		}
		else 	mtControl.getMTParams().dataSourceName = "";
			break;
		}
	}
	

	
	
}

