package videoRangePanel.externalSensors;

import videoRangePanel.VRControl;
import IMU.IMUDataBlock;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import angleMeasurement.AngleDataUnit;

public class IMUListener extends PamProcess {

	@Override
	public String getProcessName() {
		return "IMU monitor";
	}

	VRControl vrControl;
	private IMUDataBlock imuDataBlock;
	
	public IMUListener(VRControl vrControl) {
		super(vrControl, null);
		this.vrControl = vrControl;
	}
	
	@Override
	public void pamStart() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void noteNewSettings(){
//		System.out.println("VR.IMUListener: sETTINGS UPDATE");
		super.noteNewSettings();
		vrControl.update(VRControl.SETTINGS_CHANGE);
	}
	
	@SuppressWarnings("rawtypes")
	public void sortIMUMeasurement () {
		if (imuDataBlock != null) {
			setParentDataBlock(null);
			imuDataBlock = null;
		}
		if (vrControl.getVRParams().measureAngles) {
			PamDataBlock potentialDataBlock=PamController.getInstance().getDataBlock(AngleDataUnit.class, vrControl.getVRParams().angleDataBlock);
			if (potentialDataBlock instanceof IMUDataBlock)  imuDataBlock = (IMUDataBlock) potentialDataBlock;
			if (imuDataBlock != null) {
				setParentDataBlock(imuDataBlock);
			}
		}
	}
	
	
	
//	@Override
//	public void newData(PamObservable o, PamDataUnit arg) {
//		if (o == imuDataBlock) {
//			newAngles((AngleDataUnit) arg);
//		}
//	}
	
//	private void newAngles(AngleDataUnit angleDataUnit) {
//		if (angleDataUnit.getHeld()) {
//			//TODO
////			setImageHeading(angleDataUnit.correctedAngle);
//		}
//	}
	
	public IMUDataBlock getIMUDataBlock(){
		return imuDataBlock;
	}
}