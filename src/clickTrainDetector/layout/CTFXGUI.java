package clickTrainDetector.layout;

import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.SettingsPane;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.ClickTrainParams;
import pamViewFX.PamControlledGUIFX;

/**
 * The JavaFX GUI for the click train detector
 * 
 * @author Jamie Macaulay
 *
 */
public class CTFXGUI  extends PamControlledGUIFX {
	
	
	/**
	 * CTFXGUI. 
	 * @param clickTrainControl
	 */
	public CTFXGUI(ClickTrainControl clickTrainControl) {
		this.clickTrainControl=clickTrainControl; 
	}

	/**
	 * Reference to click train control. 
	 */
	private ClickTrainControl clickTrainControl;

	/**
	 * The FFT Pane.
	 */
	private ClickTrainAlgorithmPaneFX fftPane;


	public SettingsPane<ClickTrainParams> getSettingsPane(){
		if (fftPane==null) fftPane= new ClickTrainAlgorithmPaneFX(clickTrainControl); 
		fftPane.setParams(clickTrainControl.getClickTrainParams());
		return fftPane;
	}

	@Override
	public void updateParams() {
		//System.out.println("FFT Params input: " + fftControl.getFftParameters());
		ClickTrainParams newParams=fftPane.getParams(clickTrainControl.getClickTrainParams()); 
		//System.out.println("NEW PARAMS: " + newParams);
		//System.out.println("FFT Length: " + newParams.fftLength);
		if (newParams!=null) clickTrainControl.updateParams(newParams); 
		
//		ctSidePanel.update(clickTrainControl.getCurrentCTAlgorithm()); 
	}

	@Override
	public void notifyGUIChange(int changeType) {
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			break;
		case PamControllerInterface.CHANGED_PROCESS_SETTINGS:
			//data source may have potentially changed. e.g. by a datamodelfx Need to set in params.
			//System.out.println("FFTControl: CHANGED_PROCESS_SETTINGS : " +fftProcess.getParentDataBlock());
					if (clickTrainControl.getClickTrainProcess().getParentDataBlock()!=null){
						clickTrainControl.getClickTrainParams().dataSourceName=clickTrainControl.getClickTrainProcess().getParentDataBlock().getLongDataName();
					}
					else clickTrainControl.getClickTrainParams().dataSourceName="";
			break;
		}
	}
	

}
