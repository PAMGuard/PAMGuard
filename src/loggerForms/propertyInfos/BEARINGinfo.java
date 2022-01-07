package loggerForms.propertyInfos;

import loggerForms.BearingTypes;
import loggerForms.PropertyDescription;
import loggerForms.controlDescriptions.ControlDescription;

public class BEARINGinfo {
	private ControlDescription relatedControl;
	private BearingTypes type;
	private boolean plot;
	private int controlIndex;
	
	
	public BEARINGinfo(ControlDescription relatedControl, int controlIndex, BearingTypes type,boolean plot){
		this.relatedControl=relatedControl;
		this.controlIndex = controlIndex;
		this.type=type;
		this.plot=plot;
	}

	/**
	 * @return the relatedControl
	 */
	public ControlDescription getRelatedControl() {
		return relatedControl;
	}

	/**
	 * @return the type
	 */
	public BearingTypes getType() {
		return type;
	}

	/**
	 * @return the plot
	 */
	public boolean isPlot() {
		return plot;
	}

	public int getControlIndex() {
		return controlIndex;
	}
	
	
}
