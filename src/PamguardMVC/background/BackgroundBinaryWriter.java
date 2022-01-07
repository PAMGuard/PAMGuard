package PamguardMVC.background;

import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;

public abstract class BackgroundBinaryWriter<Tunit extends BackgroundDataUnit> {

	private BackgroundManager<Tunit> backgroundManager;

	public BackgroundBinaryWriter(BackgroundManager<Tunit> backgroundManager) {
		this.backgroundManager = backgroundManager;
	}

	/**
	 * Get packed binary data to write to 
	 * @param backgroundUnit
	 * @return packed array to write to binary store
	 */
	public abstract BinaryObjectData packBackgroundData(Tunit backgroundUnit);
	
	/**
	 * Unpack data from the binary file
	 * @param binaryObjectData
	 * @param bh
	 * @param moduleVersion
	 * @return
	 */
	public abstract Tunit unpackBackgroundData(BinaryObjectData binaryObjectData, BinaryHeader bh, int moduleVersion);
	
	/**
	 * Version number for the background data has to match that of the 
	 * main data. Could cause problems moving forwards!
	 * @return
	 */
	public int getModuleVersion() {
		BinaryDataSource binDataSource = backgroundManager.getDetectorDataBlock().getBinaryDataSource();
		if (binDataSource == null) {
			return 0;
		}
		return binDataSource.getModuleVersion();
	}
	
}
