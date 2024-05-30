package pamViewFX.fxNodes.utilityPanes;

import javafx.stage.Stage;
import javafx.stage.StageStyle;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxStyles.PamStylesManagerFX;
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
	

		Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
		stage.toFront();
		
		this.getDialogPane().getStylesheets().addAll(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getDialogCSS());
		
//		System.out.println("SettingsPane: DIALOG " + 	this.getDialogPane().getStylesheets().size());
//		for (int i=0;i<this.getDialogPane().getStylesheets().size();i++) {
//			System.out.println(this.getDialogPane().getStylesheets().get(i));
//		}
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