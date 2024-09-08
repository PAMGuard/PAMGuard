package GPS;

import java.util.ListIterator;

import PamguardMVC.dataOffline.OfflineDataLoadInfo;

/**
 * Class of utilities to help match times to GPS locations. PArticulalry used offline
 * to match large amounts of data to GPS data so that Lat Longs can be written out
 * with other database data. Will load GPS Data in reasonable chunks and match detections
 * into them. 
 * 
 * Is accessed from GPS Control (not a singleton since future versions of PAMGuard will 
 * probably support multiple GPS sources). 
 * 
 * @author Doug Gillespie
 *
 */
public class GPSDataMatcher {

	private GPSControl gpsControl;
	
	private long meanGpsInterval = 60000;
	
	private long typicalLoadinterval = 3600000L;

	public GPSDataMatcher(GPSControl gpsControl) {
		super();
		this.gpsControl = gpsControl;
	}
	
	public GpsData matchData(long dataTimeMillis) {
		if (!canMatch(dataTimeMillis)) {
			loadGpsData(dataTimeMillis);
		}
		return findMatch(dataTimeMillis);
	}

	/**
	 * Check that loaded data bracket the time we're interested in. 
	 * @param dataTimeMillis the tiem we're interested in. 
	 * @return true if loaded data bracket the time we're interested in. 
	 */
	private boolean canMatch(long dataTimeMillis) {
		GPSDataBlock gpsDataBlock = gpsControl.getGpsDataBlock();
		GpsDataUnit firstUnit = gpsDataBlock.getFirstUnit();
		GpsDataUnit lastUnit = gpsDataBlock.getLastUnit();
		if (firstUnit == null || lastUnit == null) {
			return false;
		}
		if (firstUnit.getTimeMilliseconds() > dataTimeMillis || lastUnit.getTimeMilliseconds() < dataTimeMillis) {
			return false;
		}
		return true;
	}

	private GpsData findMatch(long dataTimeMillis) {
		GPSDataBlock gpsDataBlock = gpsControl.getGpsDataBlock();
		GpsDataUnit firstUnit = gpsDataBlock.getFirstUnit();
		GpsDataUnit lastUnit = gpsDataBlock.getLastUnit();
		if (firstUnit == null || lastUnit == null) {
			return null;
		}
		// scroll forwards until out unit is just after a gpsUnit
		GpsDataUnit prevGps = firstUnit;
		GpsDataUnit nextGps = null;
		synchronized (gpsDataBlock.getSynchLock()) {
			ListIterator<GpsDataUnit> it = gpsDataBlock.getListIterator(0);
			while (it.hasNext()) {
				nextGps = it.next();
				if (nextGps.getTimeMilliseconds() >= dataTimeMillis) {
					break;
				}
				prevGps = nextGps;
			}
			// now have units bracketing the detection we want. 
		}
		if (prevGps == null || nextGps == null) {
			return null;
		}
		
		if (!(nextGps.getTimeMilliseconds() >= dataTimeMillis & prevGps.getTimeMilliseconds()<= dataTimeMillis)) {
			return null;
		}
		double tDiff = nextGps.getTimeMilliseconds() - prevGps.getTimeMilliseconds();
		double rPrev = (double) (nextGps.getTimeMilliseconds() - dataTimeMillis) / tDiff;
		double rNext = (double) (dataTimeMillis-prevGps.getTimeMilliseconds()) / tDiff;
		
		GpsData gpsData = prevGps.getGpsData().clone();
		GpsData prev = prevGps.getGpsData();
		GpsData next = nextGps.getGpsData();
		// now do some weighted averaging. 
		gpsData.setTimeInMillis(dataTimeMillis);
		gpsData.setLatitude(prev.getLatitude()*rPrev + next.getLatitude()*rNext);
		gpsData.setLongitude(prev.getLongitude()*rPrev + next.getLongitude()*rNext);
		gpsData.setSpeed(prev.getSpeed()*rPrev + next.getSpeed()*rNext);
		// should average everything else, but currently don't need them, so do later. 
		return gpsData;
	}

	private boolean loadGpsData(long dataTimeMillis) {
		GPSDataBlock gpsDataBlock = gpsControl.getGpsDataBlock();
		long currentLoadPeriod = gpsDataBlock.getCurrentViewDataEnd() - gpsDataBlock.getCurrentViewDataStart();
		if (currentLoadPeriod <= typicalLoadinterval) {
			currentLoadPeriod = typicalLoadinterval;
		}
		long loadStart = dataTimeMillis - meanGpsInterval * 2;
		long loadEnd = loadStart + currentLoadPeriod;
//		System.out.println(String.format("Loading GPS Data From %s to %s", PamCalendar.formatDateTime(loadStart), PamCalendar.formatDateTime(loadEnd)));
		gpsDataBlock.loadViewerData(new OfflineDataLoadInfo(loadStart, loadEnd), null);
		
		return true;
	}
}
