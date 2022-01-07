package videoRangePanel;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;

public class VRDataBlock extends PamDataBlock<VRDataUnit> {

	public VRDataBlock(String dataName, PamProcess parentProcess) {
		super(VRDataUnit.class, dataName, parentProcess, 0);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean getShouldLog(PamDataUnit pamDataUnit) {
		return true;
	}

}
