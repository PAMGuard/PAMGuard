package videoRangePanel.externalSensors;

import videoRangePanel.VRControl;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import angleMeasurement.AngleDataBlock;
import angleMeasurement.AngleDataUnit;

public class AngleListener extends PamProcess {

		@Override
		public String getProcessName() {
			// TODO Auto-generated method stub
			return "Angle monitor";
		}

		VRControl vrControl;
		private AngleDataBlock angleDataBlock;
		
		public AngleListener(VRControl vrControl) {
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
		@SuppressWarnings("rawtypes")
		public void sortAngleMeasurement () {
			if (angleDataBlock != null) {
				setParentDataBlock(null);
				angleDataBlock = null;
			}
			if (vrControl.getVRParams().measureAngles) {
//				System.out.println("DataBlock: "+vrControl.getVRParams().angleDataBlock);
				if (vrControl.getVRParams().measureAngles) {
					PamDataBlock potentialDataBlock=PamController.getInstance().getDataBlock(AngleDataUnit.class, vrControl.getVRParams().angleDataBlock);
					if (potentialDataBlock instanceof AngleDataBlock)  angleDataBlock = (AngleDataBlock) potentialDataBlock;
					if (angleDataBlock != null) {
						setParentDataBlock(angleDataBlock);
					}
				}
			}
		}

		@Override
		public void newData(PamObservable o, PamDataUnit arg) {
			if (o == angleDataBlock) {
				newAngles((AngleDataUnit) arg);
			}
		}
		
		private void newAngles(AngleDataUnit angleDataUnit) {
			if (angleDataUnit.getHeld()) {
				//TODO
//				setImageHeading(angleDataUnit.correctedAngle);
			}
		}
		
		public AngleDataBlock getAngleDataBlock(){
			return angleDataBlock;
		}
	}