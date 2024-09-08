package angleMeasurement;

import PamUtils.PamCalendar;
import PamguardMVC.PamProcess;

public class AngleProcess extends PamProcess implements AngleMeasurementListener{

	AngleControl angleControl;
	
	private AngleDataBlock angleDataBlock;
	
	private AngleDataUnit lastDataUnit;
	
	protected AngleLogging angleLogging;
	
	public AngleProcess(AngleControl angleControl) {
		super(angleControl, null);
		this.angleControl = angleControl;
		
		addOutputDataBlock(angleDataBlock = new AngleDataBlock(angleControl, this.getProcessName(), this));
		angleDataBlock.SetLogging(angleLogging = new AngleLogging(angleControl, angleDataBlock));
		
		angleControl.angleMeasurement.addMeasurementListener(this);
	}

	@Override
	public void newAngle(Double rawAngle, Double calibratedAngle, Double correctedAngle) {

		lastDataUnit = new AngleDataUnit(PamCalendar.getTimeInMillis(), rawAngle, calibratedAngle, correctedAngle);

		angleControl.newAngle(lastDataUnit);
		
		angleDataBlock.addPamData(lastDataUnit);
		
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
		
	}
	
	protected AngleDataUnit holdAngle() {
		if (lastDataUnit != null) {
			lastDataUnit.setHeld(true);
			angleDataBlock.updatePamData(lastDataUnit, lastDataUnit.getTimeMilliseconds());
			angleDataBlock.setHeldAngle(lastDataUnit);
		}
		return lastDataUnit;
	}

}
