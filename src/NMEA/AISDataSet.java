package NMEA;

/**
 * A set of AIS data strings which can combined with simulated NMEA data for
 * testing purposes. 
 */
public interface AISDataSet {


	/**
	 * Get the NEXT AIS String from a simulator or list
	 * @return an AIS String
	 */
	public abstract String getNext();

	/**
	 * Rest the simulator or list. 
	 */
	public abstract void reset();

}