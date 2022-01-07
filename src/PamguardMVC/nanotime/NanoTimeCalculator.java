package PamguardMVC.nanotime;

import PamguardMVC.PamDataUnit;

public interface NanoTimeCalculator {

	static public final long NANOSTOMILLS = 1000000;
	
	static public final long NANOSTOSECONDS = 1000000000;
	
	static public final double NANOSTOSECONDS_F = 1.e9;
	
	public long getNanoTime(PamDataUnit pamDataUnit);
	
}
