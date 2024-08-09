package loggerForms.effort;

import effort.EffortDataUnit;
import loggerForms.FormsDataUnit;

public class FormsEffortUnit extends EffortDataUnit {

	private FormsDataUnit parentFormUnit;

	public FormsEffortUnit(FormsDataUnit parentFormUnit, long formEndTime) {
		super(parentFormUnit, parentFormUnit.getTimeMilliseconds(), formEndTime);
		this.parentFormUnit = parentFormUnit;
	}

}
