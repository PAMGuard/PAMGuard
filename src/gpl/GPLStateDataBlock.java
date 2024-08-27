package gpl;

import PamguardMVC.PamDataBlock;

public class GPLStateDataBlock extends PamDataBlock<GPLStateDataUnit> {

	private GPLProcess gplProcess;

	public GPLStateDataBlock(GPLProcess gplProcess, int channelMap) {
		super(GPLStateDataUnit.class, gplProcess.getGplControlledUnit().getUnitName() + " Level", gplProcess, channelMap);
		this.gplProcess = gplProcess;
	}

}
