package meygenturbine;

import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;

public class MeygenDataUnit extends PamDataUnit {

	private double bearing;
	private double magnitude;
	private double depth;

	/**
	 * @param timeMilliseconds
	 */
	public MeygenDataUnit(long timeMilliseconds, double bearing, double magnitude, double depth) {
		super(timeMilliseconds);
		this.bearing = bearing;
		this.magnitude = magnitude;
		this.depth = depth;
	}

	/**
	 * @return the bearing
	 */
	public double getBearing() {
		return bearing;
	}

	/**
	 * @return the magnitude
	 */
	public double getMagnitude() {
		return magnitude;
	}

	/**
	 * @return the depth
	 */
	public double getDepth() {
		return depth;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataUnit#getSummaryString()
	 */
	@Override
	public String getSummaryString() {
		return String.format("<html>Trubine 4 hub<br>%s<br>Flow %3.1f%s, %3.1fm/s", 
				PamCalendar.formatDateTime(getTimeMilliseconds()), bearing, LatLong.deg, magnitude);
	}


}
