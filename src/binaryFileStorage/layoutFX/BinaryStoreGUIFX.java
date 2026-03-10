package binaryFileStorage.layoutFX;

import java.util.Optional;

import PamController.SettingsPane;
import binaryFileStorage.BinaryStore;
import binaryFileStorage.BinaryStoreSettings;
import pamViewFX.PamControlledGUIFX;
import pamViewFX.fxNodes.pamDialogFX.PamSettingsDialogFX;

/**
 * The JavaFX GUI for the binary store module. 
 * @author Jamie Macaulay
 *
 */
public class BinaryStoreGUIFX extends PamControlledGUIFX {

	private BinaryStoreSettingsPaneFX binaryStoreSettingsPane;

	/**
	 * Reference to the binary store control. 
	 */
	private BinaryStore binaryStoreControl;

	/**
	 * The Binary store control. 
	 */
	public BinaryStoreGUIFX(BinaryStore binaryStoreControl) {
		this.binaryStoreControl=binaryStoreControl;
	}


	@Override
	public SettingsPane<?> getSettingsPane(){
		if (binaryStoreSettingsPane==null){
			binaryStoreSettingsPane=new BinaryStoreSettingsPaneFX(binaryStoreControl);
		}
		binaryStoreSettingsPane.setParams(binaryStoreControl.getBinaryStoreSettings());
		return binaryStoreSettingsPane;
	}


	@Override
	public void updateParams() {
		BinaryStoreSettings newParams=binaryStoreSettingsPane.getParams(binaryStoreControl.getBinaryStoreSettings()); 
		if (newParams!=null) binaryStoreControl.setBinaryStoreSettings(newParams);
		//setup the controlled unit. 
		binaryStoreControl.setupControlledUnit(); 
	}


	/**
	 * Show the binary store dialog. Used on start up/ 
	 * @param binaryStoreSettings
	 * @return
	 */
	public BinaryStoreSettings showDialog(BinaryStoreSettings binaryStoreSettings) {
		PamSettingsDialogFX<BinaryStoreSettings> dBsettingsDialog=new PamSettingsDialogFX<BinaryStoreSettings>((SettingsPane<BinaryStoreSettings>) getSettingsPane());
		dBsettingsDialog.setParams(binaryStoreSettings);
		dBsettingsDialog.setOnShown((value)->{
			getSettingsPane().paneInitialized();
			//fix to make sure the dialog appearsa in pre PG GUI FX insitialisation i.e. when selecting viewer database 
			//on PG start up/. 
			//((Stage) dBsettingsDialog.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);;
		});


		Optional<BinaryStoreSettings> dBParams=dBsettingsDialog.showAndWait();
		if (dBParams!=null) {
			return dBParams.get();
		}
		else {
			//the cancel button has been pressed...
			return null; 
		}
	}



}
