package clickDetector.offlineFuncs.rcImport;

import PamguardMVC.PamDataUnit;


/*
 * Dummy click class to use for copying data from the 
 * RainbowClick to PAMGuard database. 
 */
public class DummyClick extends PamDataUnit {
	int eventId;
	int clickNo;
	String pamFile;
	double amplitude;
	public DummyClick(long timeMilliseconds, int eventId, int clickNo, String pamFile, double amplitude) {
		super(timeMilliseconds);
		this.eventId = eventId;
		this.clickNo = clickNo;
		this.pamFile = pamFile;
		this.amplitude = amplitude;
	}

}
