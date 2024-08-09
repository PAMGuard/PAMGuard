package effort;

import PamguardMVC.PamDataUnit;

public class EffortDataUnit extends PamDataUnit implements EffortDataThing {


	public EffortDataUnit(long effortStart, long effortEnd) {
		super(effortStart);
		getBasicData().setEndTime(effortEnd);
	}

	@Override
	public long getEffortStart() {
		return getTimeMilliseconds();
	}

	@Override
	public long getEffortEnd() {
		return getEndTimeInMilliseconds();
	}

	public void setEffortEnd(long endTime) {
		getBasicData().setEndTime(endTime);
	}

}
