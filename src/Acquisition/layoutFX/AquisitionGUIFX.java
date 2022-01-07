package Acquisition.layoutFX;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionParameters;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.SettingsPane;
import pamViewFX.PamControlledGUIFX;

/**
 * FX GUI for the SoundAquisition module. 
 * @author Jamie Macaulay
 *
 */
public class AquisitionGUIFX extends PamControlledGUIFX {
	
	/**
	 * The main settings pane for the aquisition control. 
	 */
	private AcquisitionPaneFX acquisitionPane;
	
	/**
	 * Reference to the Sound Aquisition control. 
	 */
	private AcquisitionControl aquisitionControl;

	public AquisitionGUIFX(AcquisitionControl aquisitionControl) {
		this.aquisitionControl=aquisitionControl; 
	}

	@Override
	public SettingsPane<AcquisitionParameters> getSettingsPane(){
		if (acquisitionPane==null){
			acquisitionPane=new AcquisitionPaneFX(aquisitionControl);
		}
		acquisitionPane.setParams(aquisitionControl.getAcquisitionParameters());
		return acquisitionPane;
	}
	

	/**
	 * This is called whenever a settings pane is closed. If a pamControlledUnit has
	 * settings pane then this should be used to update settings based on info input
	 * into settings pane.
	 */
	public void updateParams() {
		AcquisitionParameters newParameters = acquisitionPane.getParams(); 
		if (newParameters != null) {
			aquisitionControl.setAquisitionParams(newParameters.clone());
			aquisitionControl.setSelectedSystem();
			boolean arraychannelOK = aquisitionControl.checkArrayChannels();
			if (!arraychannelOK) {
				//TODO- open dialog. 
			}
			aquisitionControl.getAcquisitionProcess().setupDataBlock();
			PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);
			if (aquisitionControl.isViewer()) {
//				System.out.println("New Aquisition Offline Data: " + aquisitionControl.getOfflineFileServer().getOfflineFileParameters().folderName);
				aquisitionControl.getOfflineFileServer().createOfflineDataMap();
			}
		}
	}
	
	

}
