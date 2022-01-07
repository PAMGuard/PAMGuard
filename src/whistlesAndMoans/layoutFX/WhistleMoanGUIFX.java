package whistlesAndMoans.layoutFX;

import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.SettingsPane;
import pamViewFX.PamControlledGUIFX;
import whistlesAndMoans.WhistleMoanControl;
import whistlesAndMoans.WhistleToneParameters;

public class WhistleMoanGUIFX extends PamControlledGUIFX {

	private WhistleMoanSettingsPaneFX whistleMoanSettingsPane;


	private WhistleMoanControl whistleMoanControl;

	public WhistleMoanGUIFX(WhistleMoanControl whistleMoanControl){
		this.whistleMoanControl=whistleMoanControl; 
	}


	public SettingsPane<?> getSettingsPane(){
		if (whistleMoanSettingsPane==null){
			whistleMoanSettingsPane=new WhistleMoanSettingsPaneFX(whistleMoanControl);
		}
		whistleMoanSettingsPane.setParams(whistleMoanControl.getWhistleToneParameters());
		//		System.out.println("Whistle and Moan Control: The parent of the whistle and moan module is "
		//		+whistleMoanControl.getDataSource());
		return whistleMoanSettingsPane;	
	}

	@Override
	public void updateParams() {
		WhistleToneParameters newParams=whistleMoanSettingsPane.getParams(null); 
		if (newParams!=null) whistleMoanControl.setWhistleMoanControl(newParams);
		whistleMoanControl.setupControlledUnit(); 
	}

	@Override
	public void notifyGUIChange(int changeType) {
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			break;
		case PamControllerInterface.CHANGED_PROCESS_SETTINGS:
			//data source may have potentially changed. e.g. by a datamodelfx Need to set in params.
			//System.out.println("FFTControl: CHANGED_PROCESS_SETTINGS : " +fftProcess.getParentDataBlock());
			if (whistleMoanControl.getSpectrogramNoiseProcess().getParentDataBlock()!=null){
				whistleMoanControl.getWhistleToneParameters().setDataSource(
						whistleMoanControl.getSpectrogramNoiseProcess().getParentDataBlock().getDataName()); 
			}
			else whistleMoanControl.getWhistleToneParameters().setDataSource("");
			break;
		}
	}


}
