package loggerForms.cameragrabber.source;

import javax.swing.JComponent;

import loggerForms.cameragrabber.CameraParams;

public interface CameraSourcePanel {
	/**
	 * Get the awt component which will be incorporated into a 
	 * larger display panel / component. 
	 * @return an awt component. 
	 */
	abstract JComponent getDialogComponent();
	
	/**
	 * Set the initial parameter values in the panel.
	 * Since this interface is purely abstract, the type 
	 * of parameter is unknown, so concrete implementations will 
	 * somehow have to pass the class containing the parameter information 
	 * in some other way.   
	 */
	abstract void setParams(CameraParams cameraParams);
	
	/**
	 * 
	 * @return true if parameters all have acceptable values and the dialog can 
	 * close. 
	 */
	abstract CameraParams getParams();
}
