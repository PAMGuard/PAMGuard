package pamViewFX.fxNodes.utilityPanes;

import javafx.stage.Stage;
import javafx.stage.StageStyle;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import PamController.PamController;
import PamController.SettingsPane;

/**
 * Create a dialog to show module settings pane. Use this version for dialogs which should look similar to the
 * standard PAMGuard Swing dialogs. 
 * 
 * @author Jamie Macaulay
 * 
 * @param <T> - settings class for the module. 
 */
public class SettingsDialog<T> extends PamDialogFX<T>{

	private SettingsPane<T> settingsPane;

	public SettingsDialog(SettingsPane<T> settingsPane){
		super(PamGuiManagerFX.getInstance().getPrimaryStage() /*TODO - add stage*/, settingsPane.getName(), StageStyle.DECORATED);
		this.setResizable(true);
		this.settingsPane=settingsPane;
		this.setContent(settingsPane.getContentNode());
//		this.getDialogPane().getStylesheets().add(PamController.getInstance().getGuiManagerFX().getPamSettingsCSS());
		this.getDialogPane().getStylesheets().add(PamController.getInstance().getGuiManagerFX().getPamDialogCSS());

		Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
		stage.toFront();
		
//		//set results converter
//		this.setResultConverter(dialogButton -> {
//		    if (dialogButton == ButtonType.OK) {
//		        T params = getParams();
//		        if (params!=null) pamControlledUnit.setParams(params); 
//		    }
//		    return null;
//		});
	}
	
	@Override
	public void setParams(T input) {
		settingsPane.setParams(input);
	}

	@Override
	public T getParams() {
		return settingsPane.getParams(null);
	}
	
}