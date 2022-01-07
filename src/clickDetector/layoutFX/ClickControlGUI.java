package clickDetector.layoutFX;

import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.SettingsPane;
import clickDetector.ClickControl;
import clickDetector.ClickDetector;
import clickDetector.ClickParameters;
import pamViewFX.PamControlledGUIFX;

/**
 * Get the FX GUI for the click control 
 * @author Jamie Macaulay 
 *
 */
public class ClickControlGUI extends PamControlledGUIFX {
	
	/**
	 * Reference to the click control
	 */
	private ClickControl clickControl;
	

	/**
	 * The main click settings pane. 
	 */
	private ClickSettingsPane clickSettingsPane;

	/**
	 * The click detector (click process)
	 */
	private ClickDetector clickDetector;


	public ClickControlGUI(ClickControl clickControl) {
		this.clickControl=clickControl; 
		this.clickDetector = clickControl.getClickDetector(); 
	}
	
	
	private void newClassifySettings() {
		clickControl.setClickIdentifier(clickControl.getClassifierManager().
				getClassifier(clickControl.getClickParameters().clickClassifierType));
//		if (offlineToolbar != null) {
//			offlineToolbar.setupToolBar();
//		}
	}
	
	@Override
	public SettingsPane<ClickParameters> getSettingsPane(){
		if (clickSettingsPane==null){
			clickSettingsPane=new ClickSettingsPane(clickControl);
		}
		clickSettingsPane.setParams(clickControl.getClickParameters());
		return clickSettingsPane;
	}
	
	/**
	 * Convenience function to get the ClickSettingsPane (rather than generic pane)
	 * @return the click settings pane for the click control. 
	 */
	public ClickSettingsPane getClickSettingsPane(){
		//NOTE- do not put setParams() here as this causes issues 
		//with the click classifer pane. Controls in the click settings pane
		//call this function. 
		if (clickSettingsPane==null){
			clickSettingsPane=new ClickSettingsPane(clickControl);
		}
		return  clickSettingsPane; 
	}
	
	@Override
	public void updateParams() {
		ClickParameters newParams=clickSettingsPane.getParams(clickControl.getClickParameters()); 
		if (newParams!=null) {
			clickControl.setClickParameters(newParams);
			newClassifySettings();
		}
		//setup the controlled unit. 
		clickControl.setupControlledUnit(); 
	}
	
	@Override
	public void notifyGUIChange(int changeType) {
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			break;
		case PamControllerInterface.CHANGED_PROCESS_SETTINGS:
			//data source may have potentially changed. e.g. by a datamodelfx Need to set in params.
			//System.out.println("FFTControl: CHANGED_PROCESS_SETTINGS : " +fftProcess.getParentDataBlock());
			if (clickDetector.getParentDataBlock()!=null){
			clickControl.getClickParameters().setRawDataSource(clickDetector.getParentDataBlock().getDataName());
		}
		else clickControl.getClickParameters().setRawDataSource("");
			break;
		}
	}


}
