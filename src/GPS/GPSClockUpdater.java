package GPS;

import java.util.ArrayList;

import NMEA.NMEADataUnit;
import PamUtils.clock.SystemClock;
import net.engio.mbassy.listener.Synchronized;

/**
 * Functions for PC clock updates from GPS. Separated out from UpdateClockDialog so 
 * that's it's easier to run stand alone in -nogui operation, and just to make neater
 * and cleaner code. 
 */
public class GPSClockUpdater {
	
	private GPSControl gpsControl;
	
	private ProcessNmeaData gpsProcess;

	private ArrayList<ClockUpdateObserver> updateObservers = new ArrayList();;

	private boolean updateASAP;

	public GPSClockUpdater(GPSControl gpsControl, ProcessNmeaData gpsProcess) {
		super();
		this.gpsControl = gpsControl;
		this.gpsProcess = gpsProcess;
	}
	
	/**
	 * Receive GPS data. Unpack it and see if there is a reasonable time. 
	 * @param nmeaDataUnit
	 */
	public long newGPSData(GpsDataUnit gpsDataUnit) {
		GpsData gpsData = gpsDataUnit.getGpsData();
		if (gpsData == null || gpsData.isDataOk() == false) {
			return -1;
		}
		long millis = gpsData.getTimeInMillis();
		newGPSTime(millis);
		
		if (updateASAP == false) {
			return millis;
		}
		
		if (millis > 0) {
			clockUpdated(millis);
			updateASAP = false;
		}
		
		return millis;
	}
	
	private void newGPSTime(long millis) {
		synchronized (updateObservers) {
			for (ClockUpdateObserver updateObserver : updateObservers) {
				updateObserver.newTime(millis);
			}
		}
	}

	private void clockUpdated(long millis) {
		boolean ok = SystemClock.getSystemClock().setSystemTime(millis);
		synchronized (updateObservers) {
			for (ClockUpdateObserver updateObserver : updateObservers) {
				updateObserver.clockUpdated(ok, millis, null);
			}
		}
	}

	/**
	 * Add an observer of clock updates
	 * @param updateObserver
	 */
	public void addObserver(ClockUpdateObserver updateObserver) {
		if (updateObservers.contains(updateObserver) == false) {
			updateObservers.add(updateObserver);
		}
	}

	/**
	 * Remove a clock update observer. 
	 * @param updateObserver
	 * @return
	 */
	public boolean removeObserver(ClockUpdateObserver updateObserver) {
		return updateObservers.remove(updateObserver);
	}
	
	/**
	 * Tell it to update on the next reasonable NMEA time string. 
	 */
	public void updateOnNext() {
		updateASAP = true;
	}

}
