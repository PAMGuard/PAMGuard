package angleMeasurement;

import java.util.Random;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;

public class AngleDataBlock extends PamDataBlock<AngleDataUnit> {


	private AngleDataUnit heldAngle = null;
	
	private AngleControl angleControl;
	
	Random r = new Random();
	
	public AngleDataBlock(AngleControl angleControl, String dataName, PamProcess parentProcess) {
		super(AngleDataUnit.class, dataName, parentProcess, 0);
		this.angleControl = angleControl;
	}

	public AngleDataUnit getHeldAngle() {
		return heldAngle;
	}

	public void setHeldAngle(AngleDataUnit heldAngle) {
		this.heldAngle = heldAngle;
	}

//	private int lastType = 0;
	@Override
	public boolean getShouldLog(PamDataUnit pamDataUnit) {
		AngleDataUnit adu = (AngleDataUnit) pamDataUnit;
		AngleLoggingParameters alp = angleControl.angleMeasurement.getAngleParameters().getAngleLoggingParameters(); 
		if (alp == null) {
			return false;
		}
//		if (alp.logAngles != lastType) {
//			System.out.println("Angle Log type now = " + alp.logAngles);
//			lastType = alp.logAngles;
//		}
		
		switch (alp.logAngles) {
		case AngleLoggingParameters.LOG_NONE:
			return false;
		case AngleLoggingParameters.LOG_ALL:
			return true;
		case AngleLoggingParameters.LOG_HELD:
			return adu.getHeld();
		case AngleLoggingParameters.LOG_TIMED:
			if (adu.getHeld()) {
				return true;
			}
			else {
				if (pastNextLogTime(adu.getTimeMilliseconds())) {
					setNextLogTime(alp, adu.getTimeMilliseconds());
					return true;
				}
			}
		}
		
		return false;
	}
	
	private long nextLogTime;
	private boolean pastNextLogTime(long thisTime) {
		return (thisTime >= nextLogTime); 
	}
	
	protected void setNextLogTime(AngleLoggingParameters angleLoggingParameters, long thisTime) {
		if (!angleLoggingParameters.timedRandom) {
			nextLogTime = thisTime + (long) (1000 * angleLoggingParameters.logInterval);
		}
		else {
			nextLogTime = thisTime + (long) (r.nextDouble() * 2 * 1000 * angleLoggingParameters.logInterval);
		}
	}
	
}
