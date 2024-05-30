package pamViewFX.fxSettingsPanes;

import java.util.Optional;

import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.pamDialogFX.PamSettingsDialogFX;
import pamViewFX.fxStyles.PamStylesManagerFX;
import PamController.PamController;
import PamController.SettingsFileData;
import javafx.stage.Stage;

/**
 * Convenience class for opening a settings file dialog. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class SettingsFileDialogFX  {

	private SettingsFilePane settingsFilePane;

	private static SettingsFileDialogFX singleInstance;

	private PamSettingsDialogFX<?> settingsDialog;

	public SettingsFileDialogFX(){
		this.settingsFilePane=new SettingsFilePane();

	}

	/**
	 * Open the settings dialog for a .psf file for PAMGuard in real time mode. 
	 * @param settingsFileData - the settings file data. 
	 * @param startup true if the dialog is the start tup dialog. 
	 * @return
	 */
	public static SettingsFileData showDialog(SettingsFileData settingsFileData, boolean startup){
		if (singleInstance == null ) {
			singleInstance = new SettingsFileDialogFX();
			singleInstance.settingsDialog=new PamSettingsDialogFX(singleInstance.settingsFilePane); 
			//			singleInstance.settingsDialog.getDialogPane().getStylesheets().add(singleInstance.getClass().getResource("/Resources/css/pamSettingsCSS.css").toExternalForm());
			singleInstance.settingsDialog.getDialogPane().getStylesheets().addAll(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getDialogCSS());
		}

		singleInstance.settingsFilePane.setParams(settingsFileData);
		singleInstance.settingsDialog.setOnShown((value)->{
			
//			singleInstance.settingsFilePane.getProgressBar().setVisible(startup);
//			singleInstance.settingsFilePane.getProgressBar().setProgress(-1);;

			singleInstance.settingsFilePane.paneInitialized();
			//fix to make sure the dialog appearsa in pre PG GUI FX insitialisation i.e. when selecting viewer database 
			//on PG start up/. 
			((Stage) singleInstance.settingsDialog.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
		});
		
		


		Optional<?> result=singleInstance.settingsDialog.showAndWait();	
		
		//System.out.println("Hello FX result: ");

		if (result==null || !result.isPresent()) {
			if (startup) {
				PamDialogFX.showWarning("No .psfx was selected. PAMGaurd will now exit.");
				System.exit(0);
			}
			else return null;
		}
		
		SettingsFileData settings=(SettingsFileData) result.get();

		return settings;
	}
}
