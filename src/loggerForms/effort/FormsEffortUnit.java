package loggerForms.effort;

import effort.EffortDataUnit;
import loggerForms.FormsDataUnit;

public class FormsEffortUnit extends EffortDataUnit {

	private FormsDataUnit parentFormUnit;

	public FormsEffortUnit(LoggerEffortProvider loggerEffortProvider, FormsDataUnit parentFormUnit, long formEndTime) {
		super(loggerEffortProvider, parentFormUnit, parentFormUnit.getTimeMilliseconds(), formEndTime);
		this.parentFormUnit = parentFormUnit;
	}

}
