package offlineProcessing;

import PamguardMVC.PamDataBlock;

public class RequiredDataBlockInfo {

	/**
	 * Data block required for processing data within the main 
	 * data block (e.g. click events, may need to load clicks and GPS data
	 * for target motion analysis
	 */
	private PamDataBlock pamDataBlock;
	
	/**
	 * how many milliseconds of data in the secondary data block 
	 * to load before the start of data in the main data block.  
	 */
	private long preLoadTime;

	/**
	 * how many milliseconds of data in the secondary data block 
	 * to load after the end of data in the main data block.  
	 */
	private long postLoadTime;

	/**
	 * @param pamDataBlock
	 * @param preLoadTime
	 * @param postLoadTime
	 */
	public RequiredDataBlockInfo(PamDataBlock pamDataBlock, long preLoadTime, long postLoadTime) {
		super();
		this.pamDataBlock = pamDataBlock;
		this.preLoadTime = preLoadTime;
		this.postLoadTime = postLoadTime;
	}

	/**
	 * @return the preLoadTime
	 */
	public long getPreLoadTime() {
		return preLoadTime;
	}

	/**
	 * @param preLoadTime the preLoadTime to set
	 */
	public void setPreLoadTime(long preLoadTime) {
		this.preLoadTime = preLoadTime;
	}

	/**
	 * @return the postLoadTime
	 */
	public long getPostLoadTime() {
		return postLoadTime;
	}

	/**
	 * @param postLoadTime the postLoadTime to set
	 */
	public void setPostLoadTime(long postLoadTime) {
		this.postLoadTime = postLoadTime;
	}

	/**
	 * @return the pamDataBlock
	 */
	public PamDataBlock getPamDataBlock() {
		return pamDataBlock;
	}
	
	

}
