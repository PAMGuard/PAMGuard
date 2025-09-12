package binaryFileStorage.checker;

import PamguardMVC.PamDataBlock;

public class UpdateWorkProgress {

	private PamDataBlock pamDataBlock;
	private int nBlock;
	private int iBlock;
	private int nFile;
	private int iFile;
	private String fileName;

	public UpdateWorkProgress(PamDataBlock pamDataBlock, String fileName, int nBlock, int iBlock, int nFile, int iFile) {
		this.pamDataBlock = pamDataBlock;
		this.fileName = fileName;
		this.nBlock = nBlock;
		this.iBlock = iBlock;
		this.nFile = nFile;
		this.iFile = iFile;
	}

	/**
	 * @return the pamDataBlock
	 */
	public PamDataBlock getPamDataBlock() {
		return pamDataBlock;
	}

	/**
	 * @return the nBlock
	 */
	public int getnBlock() {
		return nBlock;
	}

	/**
	 * @return the iBlock
	 */
	public int getiBlock() {
		return iBlock;
	}

	/**
	 * @return the nFile
	 */
	public int getnFile() {
		return nFile;
	}

	/**
	 * @return the iFile
	 */
	public int getiFile() {
		return iFile;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

}
