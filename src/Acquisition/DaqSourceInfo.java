package Acquisition;

/**
 * information to pass around with raw data units giving information about the acquisition source. 
 * This will probably be most useful when reprocessing data from files, since it gives the opportunity
 * to pass around the file name and sample number / time within a file, so that these can eventually 
 * be written to output data along with detections. This goes somewhat against previous philosophy of 
 * PAMGuard of doing everything on UTC timestamps and telling the user to work out which file thier 
 * data came from, but I'm getting soft and appreicating that having a file name and a sample number in 
 * a database table will be helpful to a lot of poeple. 
 * @author dg50
 *
 */
public class DaqSourceInfo {
	
	/**
	 * Might be the name of a daq device or of a file name. 
	 */
	private String sourceName;
	
	/**
	 * Seconds relative to start of source.
	 * Seconds are used because otherwise it will get very confusing if it's been through 
	 * a decimator, this will remain the same whatever happens.  
	 */
	private double seconds;

	/**
	 * @param sourceName
	 * @param sampleNumber
	 */
	private DaqSourceInfo(String sourceName, double seconds) {
		super();
		this.sourceName = sourceName;
		this.seconds = seconds;
	}

	@Override
	public DaqSourceInfo clone() {
		try {
			return (DaqSourceInfo) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Make a copy with a new time but same source name. 
	 * @param newSampleNumber
	 * @return
	 */
	private DaqSourceInfo copy(double seconds) {
		return new DaqSourceInfo(sourceName, seconds);
	}

	@Override
	public String toString() {
		return String.format("%s - %6.4f", sourceName, seconds);
	}

	/**
	 * @return the sourceName
	 */
	public String getSourceName() {
		return sourceName;
	}

	/**
	 * @param sourceName the sourceName to set
	 */
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	/**
	 * @return the seconds
	 */
	public double getSeconds() {
		return seconds;
	}

	/**
	 * @param seconds the seconds to set
	 */
	public void setSeconds(double seconds) {
		this.seconds = seconds;
	}


}
