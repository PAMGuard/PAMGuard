package tethys.output;

import PamguardMVC.PamDataBlock;
import tethys.TethysControl;

/**
 * Data about a PAMDataBlock. <br>
 * All the information needed to populate a table row in the synchronisation table. 
 * some will need to be set as rarely as possible since it may
 * be slow to update. <br>
 * This needs to sit alongside the StreamExportParams objects since those others are serialisable wheras
 * there is a lot of stuff in here which isn't. 
 * @author dg50
 *
 */
public class DatablockSynchInfo {

	private PamDataBlock dataBlock;
	public PamDataBlock getDataBlock() {
		return dataBlock;
	}

	private TethysControl tethysControl;
	private int setDataCount;

	public DatablockSynchInfo(TethysControl tethysControl, PamDataBlock dataBlock) {
		super();
		this.tethysControl = tethysControl;
		this.dataBlock = dataBlock;
	}
	
	/**
	 * Get the stored export params for this data block
	 * @return
	 */
	public StreamExportParams getExportParams() {
		return tethysControl.getTethysExportParams().getStreamParams(dataBlock);
	}

	public void setDataCount(int n) {
		this.setDataCount = n;
	}
	
	public int getDataCount() {
		return setDataCount;
	}
	
}
