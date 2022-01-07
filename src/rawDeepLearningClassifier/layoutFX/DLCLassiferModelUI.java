package rawDeepLearningClassifier.layoutFX;

import javax.swing.JPanel;

import PamController.SettingsPane;

/**
 * Model specific GUI components. 
 * @author Jamie Macaulay. 
 *
 */
public interface DLCLassiferModelUI {
	
	/**
	 * Get the settings pane for the classifier. 
	 * @return the settings pane. 
	 */
	public SettingsPane<?> getSettingsPane();

	/**
	 * Get the parameters for the classifiers
	 */
	public void getParams();

	/**
	 * Set the parameters for the settings pane. 
	 */
	public void setParams(); 
	
	/**
	 * Get a side panel specific to the classifier. 
	 * @return the side panel. 
	 */
	public JPanel getSidePanel(); 
	
	
}
