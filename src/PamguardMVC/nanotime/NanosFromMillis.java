package PamguardMVC.nanotime;

import PamguardMVC.PamDataUnit;

public class NanosFromMillis implements NanoTimeCalculator {

	@Override
	public long getNanoTime(PamDataUnit pamDataUnit) {
		return pamDataUnit.getTimeMilliseconds() * NANOSTOMILLS;
	}

}
