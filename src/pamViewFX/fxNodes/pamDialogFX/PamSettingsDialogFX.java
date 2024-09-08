package pamViewFX.fxNodes.pamDialogFX;

import java.util.Optional;

import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import pamViewFX.fxStyles.PamStylesManagerFX;
import PamController.PamController;
import PamController.SettingsPane;

/**
 * Create a dialog to show module settings pane. Use this version for sliding dialogs and others which are unique to
 * JavaFX and don't need to match the look of the Pamguard Swing dialogs 
 * 
 * @author Jamie Macaulay
 * @param <T> - settings class for the module. 
 */
public class PamSettingsDialogFX<T> extends PamDialogFX<T> {
	
	private SettingsPane<T> settingsPane;

	public PamSettingsDialogFX(SettingsPane<T> settingsPane){
		super(null, settingsPane.getName(), StageStyle.DECORATED);
		this.setResizable(true);
		this.settingsPane=settingsPane;
		this.setTitle(settingsPane.getName());
		this.setContent(settingsPane.getContentNode());
		

//		if (PamController.getInstance().getGuiManagerFX()!=null){
//			this.getDialogPane().getStylesheets().add(PamController.getInstance().getGuiManagerFX().getPamSettingsCSS());
//		}
		PamStylesManagerFX stylesManager = PamStylesManagerFX.getPamStylesManagerFX();
		this.getDialogPane().getStylesheets().addAll(stylesManager.getCurStyle().getDialogCSS());
		this.setOnShown((value)->{
			settingsPane.paneInitialized();
		});
	}

	@Override
	public void setParams(T input) {
//		System.out.println("PamSettingsDialogFX: setParams()");
		settingsPane.setParams(input);
		
	}

	@Override
	public T getParams() {
		return settingsPane.getParams(null);
	}
	
	/**
	 * Show a settings dialog
	 * @param settingsPane
	 * @return
	 */
	public static Optional<?> showDialog(SettingsPane<?> settingsPane){
		PamSettingsDialogFX<?> settingsDialog=new PamSettingsDialogFX(settingsPane); 
		Optional<?> result=settingsDialog.showAndWait(); 
		if (result != null) {
			PamController.getInstance().dialogOKButtonPressed();
		}
		return result;
	}

}
