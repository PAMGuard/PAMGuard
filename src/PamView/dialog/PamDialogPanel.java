package PamView.dialog;

import javax.swing.JComponent;

/**
 * General class for dialog panels which will be incorporated into one or 
 * more actual dialogs. 
 * Can be quickly opened with GenericSwingDialog
 * @author Doug Gillespie
 *
 */
public interface PamDialogPanel {

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
	abstract void setParams();
	
	/**
	 * 
	 * @return true if parameters all have acceptable values and the dialog can 
	 * close. 
	 */
	abstract boolean getParams();
	
}
