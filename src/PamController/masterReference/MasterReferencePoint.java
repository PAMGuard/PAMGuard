package PamController.masterReference;

import GPS.GPSControl;
import GPS.GpsDataUnit;
import PamController.PamController;
import PamUtils.LatLong;

/**
 * Master reference point for PAMGAURD. This is 
 * a bit of a stopgap to get some bearing and range
 * info from Landmarks, but may get used later on
 * to contain a list of possible reference marks that
 * can be selected. 
 * <br>
 * Currently gets data from the map whenever GPS updates, or whenever 
 * static hydrophone locations update. 
 * <br>
 * Everything static.
 * 
 * @author Douglas Gillespie
 *
 */
public class MasterReferencePoint {


	private static MasterReferenceSystem currentSystem = null;
	private static boolean initialisationComplete = false;
	
	private static ArrayReferenceSystem arrayReferenceSystem = new ArrayReferenceSystem();

	public static void notifyModelChanged(int changeType) {
		switch(changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			initialisationComplete = true;
			findDefaultReference();
			break;
		case PamController.ADD_CONTROLLEDUNIT:
		case PamController.REMOVE_CONTROLLEDUNIT:
			if (initialisationComplete) {
				findDefaultReference();
			}
		}
	}

	/**
	 * Find the default reference position for the system
	 * <p>Order of search is
	 * <br> Ships GPS
	 * <br> Static hydrophone position
	 * <br> landmarks of any kind. 
	 */
	private static void findDefaultReference() {
		/**
		 * This is a bit more complicated for Decimus receive systems. 
		 * Even if the hydrophones are on static moorings there may still be 
		 * 
		 */
		GPSControl gpsControl = GPSControl.getGpsControl();
		if (gpsControl != null) {
			currentSystem = new GPSDataSystem(gpsControl);
		}
		else {
			currentSystem = arrayReferenceSystem;
		}

		PamController.getInstance().notifyModelChanged(PamController.MASTER_REFERENCE_CHANGED);
	}

	private GpsDataUnit getLastGPSDataUnit() {
		GPSControl gpsControl = GPSControl.getGpsControl();
		if (gpsControl != null) {
			return gpsControl.getGpsDataBlock().getLastUnit();
		}
		else {
			return null;
		}
	}

	/**
	 * 
	 * @return the current reference lat long
	 */
	public static LatLong getLatLong() {
		if (currentSystem == null) {
			return null;
		}
		LatLong latLong = currentSystem.getLatLong();
		if (latLong == null) {
			latLong = arrayReferenceSystem.getLatLong();
		}
		return latLong;
	}

	/**
	 * 
	 * @return the time at which the current reference was last updated
	 */
	public static Long getFixTime() {
		if (currentSystem == null) {
			return null;
		}
		return currentSystem.getFixTime();
	}

	/**
	 * 
	 * @return any course information associated with the current reference
	 */
	public static Double getCourse() {
		if (currentSystem == null) {
			return null;
		}
		return currentSystem.getCourse();
	}
	/**
	 * 
	 * @return any heading information associated with the current reference
	 */
	public static Double getHeading() {
		if (currentSystem == null) {
			return null;
		}
		return currentSystem.getHeading();
	}
	
	/**
	 * 
	 * @return the speed of the reference system. 
	 */
	public static Double getSpeed() {
		if (currentSystem == null) {
			return null;
		}
		return currentSystem.getSpeed();
	}
	
	/**
	 * @return that origin system is Ok. 
	 */
	public static String getError() {
		if (currentSystem == null) {
			return "No reference system set";
		}
		return currentSystem.getError();
	}

	/**
	 * 
	 * @return the name of the current reference system. 
	 */
	public static String getName() {
		if (currentSystem == null) {
			return null;
		}
		return currentSystem.getName();
	}

	/**
	 * Used in the viewer if something is scrolling and wants to change the default time.
	 */
	public static void setDisplayTime(long displayTime) {
		if (currentSystem == null) {
			return;
		}
		currentSystem.setDisplayTime(displayTime);
	}
}
