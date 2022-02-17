package clickTrainDetector.layout.classification;

import clickTrainDetector.classification.CTClassifierParams;
import javafx.scene.layout.Pane;

/**
 * Handles GUI for the classifier algorithms. 
 * 
 * @author Jamie Macaulay
 *
 */
public interface CTClassifierGraphics {
	
	/**
	 * Pane with settings specific to the click train classifier 
	 * @return - the pane for click train algorithm settings. 
	 */
	public Pane getCTClassifierPane();

	/**
	 * Called to save the parameters from the settings pane. 
	 */
	public CTClassifierParams getParams();

	/**
	 * Set the parameters. 
	 * @param params - the parameters for a classifier. 
	 */
	public void setParams(CTClassifierParams params);

}
