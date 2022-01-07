package PamUtils.time;

import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;

public class GlobalTimeDataUnit extends PamDataUnit {

	private TimeCorrection timeCorrection;
	private PCTimeCorrector timeCorrector;

	public GlobalTimeDataUnit(long timeMilliseconds, PCTimeCorrector timeCorrector, TimeCorrection timeCorrection) {
		super(timeMilliseconds);
		this.timeCorrector = timeCorrector;
		this.timeCorrection = timeCorrection;
	}

	/**
	 * @return the timecorrection
	 */
	public TimeCorrection getTimeCorrection() {
		return timeCorrection;
	}

	/**
	 * @return the timeCorrector
	 */
	public PCTimeCorrector getTimeCorrector() {
		return timeCorrector;
	}


}
