package soundPlayback.preprocess;

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
	
}
