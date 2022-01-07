package loggerForms.dataselect;

import PamguardMVC.dataSelector.DataSelectParams;

public class FormDataSelParams extends DataSelectParams {

	/**
	 * Form params currently only allow data selection from a single control. 
	 * For finding controls, this will be based on control titles (.getTitle())
	 */
	
	protected String controlName;
	
	protected ControlDataSelParams controlParams;
	
	public FormDataSelParams() {
	}

}
