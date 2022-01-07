package dataPlotsFX.layout;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * A wrapper for the hiding panes which allow users to change display settings for each different data block 
 * Associated with a TDGraphFX. 
 * @author Jamie Macaulay
 *
 */
 public interface TDSettingsPane {

	/**
	 * Get an icon for the settings pane when hiding. 
	 * @return an icon which shows when this settings pane is hidden
	 */
	public abstract Node getHidingIcon(); 
		
	/**
	 * Get the name which shows on the setting tab when showing. 
	 * @return name to show on tab when not hiding. 
	 */
	public abstract String getShowingName();
	
	/**
	 * Icon to show on settings tab, along with name, when settings tab is showing. 
	 * @return Icon to show on settings tab when showing. Can be null for no Icon. 
	 */
	public  Node getShowingIcon();
	
	/**
	 * Get the settings pane
	 * @return pane with controls to change display settings. 
	 */
	public Pane getPane(); 

}
