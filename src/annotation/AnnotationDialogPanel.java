package annotation;

import javax.swing.JComponent;

import PamguardMVC.PamDataUnit;
/**
 * General interface for dialog panels which get incorporated into other
 * dialog panels for editing one or more annotations of a data unit. 
 * @author Doug Gillespie
 *
 */
public interface AnnotationDialogPanel {

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
	 * @param pamDataUnit Data unit to update annotation on 
	 */
	abstract void setParams(PamDataUnit pamDataUnit);
	
	/**
	 * 
	 * @param pamDataUnit Data unit to update annotation on 
	 * @return true if parameters all have acceptable values and the dialog can 
	 * close. 
	 */
	abstract boolean getParams(PamDataUnit pamDataUnit);
	
}
