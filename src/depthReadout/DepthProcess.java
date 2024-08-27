package depthReadout;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class DepthProcess extends PamProcess {
	
	private DepthControl depthControl;

	private DepthDataBlock depthDataBlock;
	
	public DepthProcess(DepthControl depthControl) {
		super(depthControl, null, "Hydrophone Depth Readout");
		this.depthControl = depthControl;

		addOutputDataBlock(depthDataBlock = new DepthDataBlock("Hydrophone Depth Data", depthControl, this));
		getDepthDataBlock().SetLogging(new DepthSQLLogging(depthControl, getDepthDataBlock()));
		getDepthDataBlock().setMixedDirection(PamDataBlock.MIX_OUTOFDATABASE);
		getDepthDataBlock().setNaturalLifetime(10000);
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}

	protected boolean readDepthData() {
		DepthSystem depthSystem = depthControl.getDepthSystem();
		if (depthSystem == null) {
			return false;
		}
		
		double[] rawData = new double[depthControl.depthParameters.nSensors];
		double[] depth = new double[depthControl.depthParameters.nSensors];
		for (int i = 0; i < depthControl.depthParameters.nSensors; i++) {
			if (!depthSystem.readSensor(i)) {
				return false;
			}
			rawData[i] = depthSystem.getDepthRawData(i);
			depth[i] = depthSystem.getDepth(i);
		}
		DepthDataUnit depthDataUnit = new DepthDataUnit(depthControl, PamCalendar.getTimeInMillis());
		depthDataUnit.setRawDepthData(rawData);
		depthDataUnit.setDepthData(depth);
		
		getDepthDataBlock().addPamData(depthDataUnit);
		
		return true;
	}

	/**
	 * @return the depthDataBlock
	 */
	public DepthDataBlock getDepthDataBlock() {
		return depthDataBlock;
	}
}
