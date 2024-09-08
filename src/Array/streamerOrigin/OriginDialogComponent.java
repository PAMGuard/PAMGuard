package Array.streamerOrigin;

import PamView.dialog.DialogComponent;
import javafx.scene.layout.Pane;

public abstract class OriginDialogComponent implements DialogComponent{

	/**
	 * Get a JavaFX pane for the origin method. 
	 * @return the JavaFX pane. 
	 */
	public abstract Pane getSettingsPane();
	

}
