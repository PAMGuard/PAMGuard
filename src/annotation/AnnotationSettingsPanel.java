package annotation;

import javax.swing.JComponent;

import annotation.handler.AnnotationOptions;

/**
 * Settings panel for configuring annotation types
 * @author dg50
 *
 */
public interface AnnotationSettingsPanel {

	/**
	 * 
	 * @return a swing panel to include in a dialog
	 */
	public JComponent getSwingPanel();
	
	/**
	 * Set params in the dialog panel
	 * @param annotationOptions
	 */
	public void setSettings(AnnotationOptions annotationOptions);
	
	/**
	 * 
	 * @return parameters from the dialog panel.
	 */
	public AnnotationOptions getSettings();
	
}
