package nmeaEmulator;

/**
 * Interface to assist a datablock in setting up emulated NMEA data. 
 * For something like the GPs, this may be very simple, in that it just
 * has to convert the data units into String. <p>
 * however for AIS, it will need to set up a database query to extract 
 * the original AIVDM string data since these are not stored in the actual 
 * data blocks.  
 * @author Doug Gillespie
 *
 */
public interface NMEAEmulator {


	/**
	 * Prepare the data to be read into the emulator. 
	 * <p>The task may be as simple as setting up an interator
	 * to go through the data already read into the datablock 
	 * or it may involve setting up a new database query to get 
	 * back to raw data. 	
	 * @param timeLimits time limits for the emulation
	 * @param timeOffset time offset - between when the data 
	 * were collected and the time they should be emulated at. 
	 * @return true if OK
	 */
	public boolean prepareDataSource(long[] timeLimits, long timeOffset);

	/**
	 * 
	 * @return the next emulated data, or null if there are no more. 
	 */
	public EmulatedData getNextData();

}
