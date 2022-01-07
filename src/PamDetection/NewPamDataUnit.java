package PamDetection;

import PamguardMVC.PamDataBlock;

/**
 * Class to eventually replace existing PamDataUnits. IS abstract 
 * @author Doug Gillespie
 *
 */
@Deprecated
abstract public class NewPamDataUnit {
	
	/**
	 * time the NewPamDataUnit was created based using standard Java time
	 */
	protected long timeMilliseconds;
	
	/**
	 * Absolute block index, needed for searches once 
	 * NPDU's start getting deleted off the front of the storage
	 */
	protected long absBlockIndex;
	
	/**
	 * Reference to parent data block
	 */
	private PamDataBlock parentDataBlock;


	/**
	 * Reference to nearest Gps data block
	 * (I like to cross reference everything to GPS data)
	 * (Class GpsDataUnit not actually defined yet)
	 */
//	GpsDataUnit gpsData;

	public NewPamDataUnit(long timeMilliseconds, PamDataBlock parentDataBlock) {
		super();
		this.timeMilliseconds = timeMilliseconds;
		this.parentDataBlock = parentDataBlock;
	}


	public long getAbsBlockIndex() {
		return absBlockIndex;
	}


	public PamDataBlock getParentDataBlock() {
		return parentDataBlock;
	}


	public long getTimeMilliseconds() {
		return timeMilliseconds;
	}
}
