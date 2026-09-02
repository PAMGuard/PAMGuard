package pamViewFX.fxNodes.pamDialogFX;

import java.util.Optional;

import helpFX.HelpManager;
import helpFX.HelpPoint;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.StageStyle;
import pamViewFX.fxGlyphs.PamGlyphDude;
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

		// Add a help button if the settings pane declares a help point
		addHelpButton(settingsPane.getHelpPointFX());
	}

	/**
	 * Add a help "?" button to the dialog pane if a non-null {@link HelpPoint} is provided.
	 * The button is appended to the existing OK/Cancel button row.
	 *
	 * @param helpPoint the help target, or {@code null} to skip adding the button
	 */
	private void addHelpButton(HelpPoint helpPoint) {
		if (helpPoint == null) return;
		// Use a custom ButtonType so JavaFX doesn't close the dialog on click
		ButtonType helpButtonType = new ButtonType("?");
		this.getDialogPane().getButtonTypes().add(helpButtonType);
		Button helpBtn = (Button) this.getDialogPane().lookupButton(helpButtonType);
		if (helpBtn != null) {
			helpBtn.setGraphic(PamGlyphDude.createPamIcon("mdi2h-help-circle-outline", 14));
			helpBtn.setTooltip(new javafx.scene.control.Tooltip("Open help for this module"));
			// Prevent the dialog from closing when the help button is clicked
			helpBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
				event.consume();
				HelpManager.getInstance().openHelp(helpPoint);
			});
		}
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
