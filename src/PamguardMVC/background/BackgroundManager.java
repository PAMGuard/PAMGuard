package PamguardMVC.background;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

/**
 * Trying to standardise measures of background noise for detectors. The measures will be written to the 
 * standard pgdf files, interleaving with detection data. Will have slightly varying formats so some abstract
 * classes at the start. 
 * @author dg50
 *
 */
abstract public class BackgroundManager<Tunit extends BackgroundDataUnit> {

	private PamDataBlock detectorDataBlock;
	
	private PamProcess detectorProcess;

	public BackgroundManager(PamProcess detectorProcess, PamDataBlock detectorDataBlock) {
		this.detectorProcess = detectorProcess;
		this.detectorDataBlock = detectorDataBlock;
		detectorDataBlock.setBackgroundManager(this);
	}

	/**
	 * @return the detectorDataBlock
	 */
	public PamDataBlock getDetectorDataBlock() {
		return detectorDataBlock;
	}

	/**
	 * @return the detectorProcess
	 */
	public PamProcess getDetectorProcess() {
		return detectorProcess;
	}
	
	/**
	 * Get the datablock that holds background data. 
	 * @return datablock for background data
	 */
	abstract public BackgroundDataBlock<Tunit> getBackgroundDataBlock();	
	
	/**
	 * Get a writer and reader for binary background data
	 * @return Writer for binary data
	 */
	abstract public BackgroundBinaryWriter<Tunit> getBackgroundBinaryWriter();
	
	/**
	 * Add a background data unit (calls through the addPamData in the underlying
	 * datablock). 
	 * @param backgroundDataUnit
	 */
	public void addData(Tunit backgroundDataUnit) {
		getBackgroundDataBlock().addPamData(backgroundDataUnit);
	}

}
