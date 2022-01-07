package soundPlayback.preprocess;

import javax.swing.JComponent;

public interface PreprocessSwingComponent {

	/**
	 * Get a swing component to add to the side panel. 
	 * @return swing component
	 */
	public JComponent getComponent();
	
	/**
	 * Update the component, e.g. if the value has been 
	 * changed somewhere else. 
	 */
	public void update();
}
