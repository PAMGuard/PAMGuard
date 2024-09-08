package loggerForms;

import generalDatabase.PamTableItem;
import loggerForms.controlDescriptions.ControlDescription;

public class FormsTableItem extends PamTableItem {
	
	private ControlDescription controlDescription;

	public FormsTableItem(ControlDescription controlDescription, String name, int sqlType, int length, boolean required) {
		super(name, sqlType, length, required);
		this.controlDescription = controlDescription;
	}

	public FormsTableItem(ControlDescription controlDescription, String name, int sqlType, int length) {
		super(name, sqlType, length);
		this.controlDescription = controlDescription;
	}

	public FormsTableItem(ControlDescription controlDescription, String name, int sqlType) {
		super(name, sqlType);
		this.controlDescription = controlDescription;
	}

	/**
	 * @return the controlDescription
	 */
	public ControlDescription getControlDescription() {
		return controlDescription;
	}

}
