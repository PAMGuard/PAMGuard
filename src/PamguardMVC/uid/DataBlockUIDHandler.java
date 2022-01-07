package PamguardMVC.uid;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickDetection;

public class DataBlockUIDHandler {

	/**
	 * Rounding up factor for new file starts. 
	 * May make this as a user definable option one day. 
	 */
	public static final int ROUNDINGFACTOR = 1000000;

	private PamDataBlock dataBlock;
	
	/**
	 * Current UID value. 
	 */
	private long uid = 0;

	private int runMode;

	/**
	 * Construct a UID handler for a datablock. 
	 * @param dataBlock Datablock owning the UID handler. 
	 */
	public DataBlockUIDHandler(PamDataBlock dataBlock) {
		this.dataBlock = dataBlock;
		if (PamController.getInstance()!=null) {
			runMode = PamController.getInstance().getRunMode();
		}
		else runMode=PamController.RUN_PAMVIEW; 
	}

	/**
	 * Get the next available UID. If the dataUnit already has a 
	 * UID, then return the same number, otherwise increment 
	 * uid by 1, then return the new value.
	 * @param dataUnit Dataunit which may or may not have an existing UID
	 * @return old UID if it existed, otherwise a new one. 
	 */
	public synchronized long getNextUID(PamDataUnit dataUnit) {
//		if (dataUnit.getClass() == ClickDetection.class) {
//			System.out.printf("Click current UID = %d, next = %d+1 in %s at %s\n", dataUnit.getUID(), uid, dataBlock.getDataName(), 
//					PamCalendar.formatTime(dataUnit.getTimeMilliseconds()));
//		}
		if (dataUnit.getUID() > 0) {
			return dataUnit.getUID();
		}
		return ++uid;
	}
	
	/**
	 * Set the current UID value. 
	 * @param uid UID value. 
	 */
	public synchronized void setCurrentUID(long uid) {
		this.uid = uid;
	}

	/**
	 * Round up the UID value so that the next file will start on 
	 * a nice round number. 
	 * @param rounding factor
	 */
	public synchronized void roundUpUID(int rounding) {
		long rem = uid%rounding;
		uid = uid - rem + rounding;
	}

	/**
	 * Get the current UID without incrementing it
	 * @return The current UID. 
	 */
	public synchronized long getCurrentUID() {
		return uid;
	}


}
