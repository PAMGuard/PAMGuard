package PamguardMVC.background;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class SpecBackgroundManager extends BackgroundManager<SpecBackgroundDataUnit> {
	
	private SpecBackgroundDataBlock dataBlock;
	
	private SpecBackgroundWriter backgroundWriter;

	public SpecBackgroundManager(PamProcess pamProcess, PamDataBlock detectorDataBlock) {
		super(pamProcess, detectorDataBlock);
		dataBlock = new SpecBackgroundDataBlock(this);
		backgroundWriter = new SpecBackgroundWriter(this);
	}

	@Override
	public SpecBackgroundDataBlock getBackgroundDataBlock() {
		return dataBlock;
	}

	@Override
	public SpecBackgroundWriter getBackgroundBinaryWriter() {
		return backgroundWriter;
	}

	
}
