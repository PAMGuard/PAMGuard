package Array;

import java.lang.reflect.Field;
import java.util.ListIterator;

import depthReadout.DepthControl;
import depthReadout.DepthDataBlock;
import depthReadout.DepthDataUnit;
import Array.streamerOrigin.HydrophoneOriginMethod;
import GPS.GpsData;
import GPS.GpsDataUnit;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamUtils.LatLong;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamObservable;

/**
 * Straight Hydrophone Locator, uses GPS heading information from
 * GPS and assumes that the hydrophones are just sticking straight out
 * the back of the boat as though on a rigid stick. 
 * <p>
 * Suitable for use with rigidly mounted hydrophones such as on a glider also.  * 
 *  <p> 
 *  If depth readout is installed, then the depth will be taken from the depth data for
 *  the appropriate time and also, more interestingly, the y coordinate of each hydrophone
 *  will be reduced according to cable angle based on a simple (straight) model of how the 
 *  hydrophone is lying. 
 * 
 * @author Doug Gillespie
 *
 */
public class StraightHydrophoneLocator extends MovingHydrophoneLocator implements ManagedParameters {
		
	private static final long serialVersionUID = 7673685461759252089L;
	
	PamDataBlock hydrophoneDataBlock;
	
	GpsDataUnit lastGpsUnit;
	
//	protected GPSDataBlock gpsDataBlock;
	
	private EmptyLocatorSettings locatorSettings;
	
	
	public StraightHydrophoneLocator(PamArray pamArray, Streamer streamer) {
		super(pamArray, streamer);
		locatorSettings = new EmptyLocatorSettings(this.getClass());
	}


//	@Override
//	public double getPairAngle(long timeMilliseconds, int phone1, int phone2, int angleType) {
//		double angle = super.getPairAngle(timeMilliseconds, phone1, phone2, angleType);
//		if (angleType == HydrophoneLocator.ANGLE_RE_SHIP) return angle;
//		if (angleType == HydrophoneLocator.ANGLE_RE_ARRAY) return angle;
//		GpsData gpsData = findGpsData(timeMilliseconds);
//		if (gpsData != null) {
//			angle += gpsData.getHeading();
//		}
//		return PamUtils.constrainedAngle(angle);
//	}
	
	// speed up the search, since it's likely to get called for
	// the same time for each phone in turn. 
	private GpsDataUnit lastFoundUnit;
	private long lastSearchTime = 0;

	
	private GpsData findGpsData(long timeMilliseconds) {
		GpsData gpsData = streamer.getHydrophoneOrigin().getStreamerData(timeMilliseconds);
		if (gpsData == null) {
			gpsData = new GpsData();
		}
		return gpsData;
//		if (gpsDataBlock == null) return null;
//		if (PamController.getInstance().getRunMode() != PamController.RUN_NORMAL) {
////			timeMilliseconds = PamCalendar.getTimeInMillis();
//			return gpsDataBlock.getClosestUnitMillis(timeMilliseconds);
//		}
//		if (lastGpsUnit == null) return null;
//		if (lastSearchTime == timeMilliseconds) return lastFoundUnit;
//		if (timeMilliseconds >= lastGpsUnit.getTimeMilliseconds()) return lastGpsUnit;
//		return gpsDataBlock.getClosestUnitMillis(timeMilliseconds);
	}


	@Override
	protected GpsData getStreamerGpsPos(long timeMilliseconds) {
		
		GpsData gpsData = findGpsData(timeMilliseconds);
		if (gpsData == null) {
			gpsData = new GpsData();
		}
//		else {
//			gpsData = gpsData.clone();
//		}
		double gpsHead = gpsData.getHeading();
		// travel function clones the data anyway, so no need to clone above. 
		gpsData = (GpsData) gpsData.travelDistanceMeters(gpsHead, streamer.getY());
		gpsData = (GpsData) gpsData.travelDistanceMeters(gpsHead+90, streamer.getX());
		gpsData.setHeight(streamer.getZ());

		getStreamer().getSensorManager().getOrientationData(gpsData, timeMilliseconds);
		
		return gpsData;
	}

//	@Override
//	public double getArrayHeading(long timeMilliseconds, int phoneNo) {
//		
//		GpsData gpsData = findGpsData(timeMilliseconds);
//		
//		if (gpsData == null) {
//			return 0;
//		}
//		return gpsData.getHeading();
//	}
//
//	public GpsData getPhoneLatLong(long timeMilliseconds, int phoneNo) {
//		/*
//		 * in the array basic settings, x is the distance along the beam and y
//		 * is the distance ahead of the vessel - so need to use the vessel heading
//		 * to get from the gps position to the positions of individual hydrophones
//		 * If no GPS data are available, then reference to 0,0.
//		 */
//		double[] hCoords = null;
//		double[] sCoords = null;
//		HydrophoneOriginMethod hydrophoneOriginMethod = streamer.getHydrophoneOrigin();
//		StreamerDataUnit sdu = hydrophoneOriginMethod.getStreamerData(timeMilliseconds);
//		Streamer streamerData;
//		GpsData gpsData = null;
//		if (sdu == null) {
//			return null;
//		}
//		streamerData = streamer;
//
//		synchronized (hydrophoneOriginMethod.getSynchronizationObject()) {
//			if (sdu != null) {
//				gpsData = sdu.getGpsData();
//			}
//			if (gpsData == null) {
//				gpsData = new GpsData();
//			}
//		}
//		
//		sCoords = streamerData.getCoordinates();
//		hCoords = getHydrophoneCoordinates(phoneNo, timeMilliseconds);
//		if (hCoords == null) {
//			hCoords = zeroCoordinate ;
//		}
//
//		// start by seeing how far beyond the last gps position we're likely
//		// to have travelled...
//		double beyond = (timeMilliseconds - gpsData.getTimeInMillis()) / 1000.;
//		beyond *= gpsData.getSpeedMetric(); 
//		/*
//		 * N.B. beyond will generally be +ve, h.getY() should be negative for a hydrophone towed 
//		 * astern - note that the travelDistanceMeters function is in the direction of the
//		 * vessel, so we simply add these two values together. 
//		 */
////		double height = getPhoneHeight(timeMilliseconds, phoneNo);
//		LatLong latLong = gpsData.travelDistanceMeters(getArrayHeading(gpsData), 
//				beyond + hCoords[1]+sCoords[1]);
//		latLong = latLong.travelDistanceMeters(getArrayHeading(gpsData)+90, 
//				hCoords[0]+sCoords[0]);
//		latLong.setHeight(hCoords[2]+sCoords[2]);
//		gpsData = gpsData.clone();
//		gpsData.setLatitude(latLong.getLatitude());
//		gpsData.setLongitude(latLong.getLongitude());
//		gpsData.setHeight(sCoords[2] + hCoords[2]);
////		System.out.println(gpsData.toFullString());
//		return gpsData;
//	}
//
//
//	@Override
//	public double getPhoneHeight(long timeMilliseconds, int phoneNo) {
//		DepthDataUnit ddu = findDepthDataUnit(timeMilliseconds);
//		if (ddu == null) {
//			return super.getPhoneHeight(timeMilliseconds, phoneNo);
//		}
//		DepthControl depthControl = ArrayManager.getArrayManager().getDepthControl();
//		int iSensor = depthControl.getSensorForHydrophone(phoneNo);
//		double[] depthData = ddu.getDepthData();
//		if (iSensor < 0 || depthData == null || iSensor >= depthData.length) {
//			return super.getPhoneHeight(timeMilliseconds, phoneNo);
//		}
//		return depthData[iSensor];
//	}
//	
//	
//	protected DepthDataUnit findDepthDataUnit(long timeMillis) {
//		DepthDataBlock depthDataBlock = findDepthDataBlock();
//		if (depthDataBlock == null) {
//			return null;
//		}
//		return depthDataBlock.getClosestUnitMillis(timeMillis);
//	}
//	
//	private DepthDataBlock findDepthDataBlock() {
//		DepthControl depthControl = ArrayManager.getArrayManager().getDepthControl();
//		if (depthControl == null) {
//			return null;
//		}
//		return depthControl.getDepthDataBlock();
//	}
//
//	/**
//	 * Get a corrected distance astern of the vessel based on the hydrophone being at some
//	 * depth and the hydrophone cable being basically straight. Generally, this correction will
//	 * be small when towing close to the surface, but will get large when the phone sinks. 
//	 * @param timeMilliseconds time that the Y is required for
//	 * @param phoneNo hydrophone number. 
//	 * @return corrected distance astern of vessel. 
//	 */
//	public double getCorrectedYPos(long timeMilliseconds, int phoneNo) {
//		double height = getPhoneHeight(timeMilliseconds, phoneNo);
//		Hydrophone h = pamArray.getHydrophone(phoneNo);
//		if (h == null) return 0;
//		double y = h.getY();
////		depth = Math.max(0, Math.min(depth, Math.abs(y)));
//		double y2 = Math.sqrt(y * y - height*height);
//		if (y < 0) {
//			y2 *= -1;
//		}
//		return y2;
//	}
	
	public String getObserverName() {
		return "Stright HydrophonePositioner";
	}

	
	@Override
	public String toString() {
		return "Straight / rigid hydrophone";
	}

	@Override
	public String getName() {
		return StraightHydrophoneLocatorSystem.sysName;
	}

	/* (non-Javadoc)
	 * @see Array.HydrophoneLocator#getDefaultSettings()
	 */
	@Override
	public LocatorSettings getDefaultSettings() {
		return new EmptyLocatorSettings(this.getClass());
	}

	/* (non-Javadoc)
	 * @see Array.HydrophoneLocator#getLocatorSettings()
	 */
	@Override
	public LocatorSettings getLocatorSettings() {
		return locatorSettings;
	}

	/* (non-Javadoc)
	 * @see Array.HydrophoneLocator#setLocatorSettings(Array.LocatorSettings)
	 */
	@Override
	public boolean setLocatorSettings(LocatorSettings locatorSettings) {
		if (EmptyLocatorSettings.class.isAssignableFrom(locatorSettings.getClass())) {
			this.locatorSettings = (EmptyLocatorSettings) locatorSettings;
			return true;
		}
		return false;
	}
	
	/**
	 * Get the auto-generated parameter set, and then add in the fields that are not included
	 * because they are not public and do not have getters.
	 * Note: for each field, we search the current class and (if that fails) the superclass.  It's
	 * done this way because StraightHydrophoneLocator might be used directly (and thus the field would
	 * be found in the class) and it also might be used as a superclass to something else
	 * (e.g. ThreadingHydrophoneLocator) in which case the field would only be found in the superclass.
	 */
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		try {
			Field field = this.getClass().getDeclaredField("lastSearchTime");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return lastSearchTime;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			
		}
		return ps;
	}

}
