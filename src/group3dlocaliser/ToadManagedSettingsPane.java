package group3dlocaliser;

import javafx.scene.control.TabPane;
import pamViewFX.fxNodes.pamDialogFX.ManagedSettingsPane;

public abstract class ToadManagedSettingsPane<S> extends ManagedSettingsPane<S>{
	
	/**
	 * Get  the main tab pane. 
	 * @return the main tab pane. 
	 */
	public abstract TabPane getTabPane();
	
	/**
	 * Set whether the pane should show errors. Because there are multiple algorithms that
	 * will use this pane we do not want to warn for every algorithm only the user selected one. 
	 * @param warn = true to warn errors e.g. show an dialog - false and there are no error warnings. 
	 */
	public abstract void setErrorWarn(boolean warn);
	
	

}
