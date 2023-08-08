package group3dlocaliser;

import javafx.scene.control.TabPane;
import pamViewFX.fxNodes.pamDialogFX.ManagedSettingsPane;

public abstract class ToadManagedSettingsPane<S> extends ManagedSettingsPane<S>{
	
	/**
	 * Get  the main tab pane. 
	 * @return the main tab pane. 
	 */
	public abstract TabPane getTabPane();
}
