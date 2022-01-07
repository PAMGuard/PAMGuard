package GPS;

import PamguardMVC.PamDataUnit;

public class GpsDataUnit extends PamDataUnit {

	/** 
	 * We could just include all the members of GpsData here, 
	 * but since the GpsData
	 * class works OK already, just leave it alone. 
	 */
	private GpsData gpsData;
	
	public GpsDataUnit(long timeMilliseconds, GpsData gpsData) {
		super(timeMilliseconds);
		this.gpsData = gpsData;
	}

	/**
	 * @return Returns the gpsData.
	 */
	public GpsData getGpsData() {
		return gpsData;
	}
	
	public void setGpsData(GpsData gpsData) {
		this.gpsData = gpsData;
		setTimeMilliseconds(gpsData.getTimeInMillis());
	}

	/**
	 * Get the average of two gps data units. If one unit is null, then 
	 * it simply returns the other one. If both are null, null is returned. 
	 * @param unit1 first data unit
	 * @param unit2 second data unit
	 * @return average position, heading, etc. 
	 */
	public static GpsDataUnit getAverage(GpsDataUnit unit1,
			GpsDataUnit unit2) {
		if (unit1 == null) {
			return unit2;
		}
		else if (unit2 == null) {
			return unit1;
		}
		GpsData newGps = GpsData.getAverage(unit1.gpsData, unit2.gpsData);
		return new GpsDataUnit((unit1.getTimeMilliseconds()+unit2.getTimeMilliseconds())/2, newGps);
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataUnit#getSummaryString()
	 */
	@Override
	public String getSummaryString() {
		// TODO Auto-generated method stub
		String str = super.getSummaryString();
		if (gpsData != null) {
			str +=  gpsData.summaryString();
		}
		return str;
	}

}
