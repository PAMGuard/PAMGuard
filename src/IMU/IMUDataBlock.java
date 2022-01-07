package IMU;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import angleMeasurement.AngleDataUnit;

public class IMUDataBlock extends PamDataBlock<AngleDataUnit>  {

	private IMULogging imuLogging;
	private IMUControl imuControl;
	
	public IMUDataBlock(IMUControl imuControl, String dataName,
			PamProcess parentProcess) {
		super(AngleDataUnit.class, dataName, parentProcess, 0);
		this.imuControl=imuControl;
		imuLogging= new IMULogging(imuControl, this);
		super.SetLogging(imuLogging);
	}
	
	@Override
	public boolean shouldNotify() {
		return false;
	}
	
	@Override
	public boolean getShouldLog(PamDataUnit pamDataUnit) {
		return true;
	}
	
//	@Override
//	public long getRequiredHistory() {
//		return 100000000;
//	}
	
	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#addPamData(PamguardMVC.PamDataUnit)
	 */
	
	@Override
	public void addPamData(AngleDataUnit pamDataUnit) {
		super.addPamData(pamDataUnit);
	}
	
	/**
	 * Returns the values to add onto the imu data units
	 * @return double fo calibration values (heading, pitch, tilt) in RADIANS. Note these are offset values were true value= measured value + offset. 
	 */
	public double[] getCalibrationVals(){
		double [] imuCalVals={ imuControl.getParams().headingCal, imuControl.getParams().pitchCal, imuControl.getParams().tiltCal};
		return imuCalVals;
	}

	
}
