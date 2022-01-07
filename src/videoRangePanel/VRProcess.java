package videoRangePanel;

import PamguardMVC.PamProcess;

public class VRProcess extends PamProcess {

	private VRControl vrControl;
	
	private VRDataBlock vrDataBlock;
	
	public VRProcess(VRControl vrControl) {
		super(vrControl, null);
		this.vrControl = vrControl;
		addOutputDataBlock(vrDataBlock = new VRDataBlock(vrControl.getUnitName(), this));
		vrDataBlock.SetLogging(new VRSQLLogging(vrControl, this));
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub

	}
	
	public void newVRLoc(VRMeasurement vrMeasurement) {
		long time;
		if (vrMeasurement.imageTime!=null) time=vrMeasurement.imageTime;
		else time=System.currentTimeMillis();
		VRDataUnit vrDataUnit = new VRDataUnit(time, vrMeasurement);
		vrDataBlock.addPamData(vrDataUnit);
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}

	public VRDataBlock getVrDataBlock() {
		return vrDataBlock;
	}

}
