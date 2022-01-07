package PamguardMVC.background;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import binaryFileStorage.BinaryDataSource;

public class BackgroundDataBlock<Tunit extends BackgroundDataUnit> extends PamDataBlock<Tunit> {

	private BackgroundManager<Tunit> backgroundManager;

	public BackgroundDataBlock(Class unitClass, BackgroundManager<Tunit> backgroundManager) {
		super(unitClass, backgroundManager.getDetectorDataBlock().getDataName()+" Noise", 
				backgroundManager.getDetectorDataBlock().getParentProcess(), 
				backgroundManager.getDetectorDataBlock().getChannelMap());
		this.backgroundManager = backgroundManager;
	}

	/**
	 * @return the backgroundManager
	 */
	public BackgroundManager<Tunit> getBackgroundManager() {
		return backgroundManager;
	}

	/**
	 * Gets the binary data source for the detection datablock that this is sharing with. 
	 */
	@Override
	public BinaryDataSource getBinaryDataSource() {
		return backgroundManager.getDetectorDataBlock().getBinaryDataSource();
	}
	
	


}
