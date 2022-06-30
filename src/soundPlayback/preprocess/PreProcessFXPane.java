package soundPlayback.preprocess;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 * A node for a pre-process pane. 
 * @author Jamie Macaualay
 *
 */
public interface PreProcessFXPane {
	
	/**
	 * Get the FX component to add to the side pane.
	 * @return the side pane. 
	 */
	public Pane getPane(); 
	
	/**
	 * Update the component, e.g. if the value has been 
	 * changed somewhere else. 
	 */
	public void update();
	
	/**
	 * Pane which shows the name of the pre-process. 
	 */
	public Node getLabel();
	
	/**
	 * Pane which allows the user to switch to a default. 
	 */
	public Node getDefaultButton();
	
}
