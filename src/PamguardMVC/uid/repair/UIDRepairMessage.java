package PamguardMVC.uid.repair;

import PamguardMVC.PamDataBlock;

public class UIDRepairMessage {

	public PamDataBlock currentDataBlock;
	public int binFileIndex;
	public int nBinFiles;
	public int infoType;
	public int percentComplete;
	
	public static final int TYPE_BLOCKPROGRESS = 1;
	public static final int TYPE_FILEPROGRESS = 2;
	public static final int TYPE_TOTALPROGRESS = 3;
	/**
	 * @param currentDataBlock
	 * @param nBinFiles
	 * @param binFileIndex
	 */
	public UIDRepairMessage(int infoType, PamDataBlock currentDataBlock, int nBinFiles, int binFileIndex) {
		super();
		this.infoType = infoType;
		this.currentDataBlock = currentDataBlock;
		this.nBinFiles = nBinFiles;
		this.binFileIndex = binFileIndex;
	}
	
	public UIDRepairMessage(int infoType, int percentComplete) {
		this.infoType = infoType;
		this.percentComplete = percentComplete;
	}

}
