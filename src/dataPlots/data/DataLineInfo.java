package dataPlots.data;


/**
 * Info about a specific data line within a TDDataInfo. 
 * This is needed since the DataInfos generally represent a
 * whole data block, but the data lines represent a single 
 * way of displaying the data within that block. 
 * @author Doug Gillespie
 *
 */
public class DataLineInfo {

	/**
	 * Name of the data, e.g. accelerometer x; bearing; slant angle, etc. 
	 */
	public String name;
	
	/**
	 * Units of the data, e.g. m/s2, deg., deg.C, etc. 
	 */
	public String unit;

	public DataLineInfo(String name, String unit) {
		super();
		this.name = name;
		this.unit = unit;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s (%s)", name, unit);
	}
}
