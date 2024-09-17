package Array;

import java.io.Serializable;
import java.lang.reflect.Field;

import GPS.GpsData;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;
import PamUtils.LatLong;
import pamMaths.PamQuaternion;
import pamMaths.PamVector;

/**
 * HydrophoneLocator is an interface for objects that can work out the true positions of 
 * hydrophones based on any or all of the following information
 * <p>
 * The set positions for each hydrophone from the array dialog
 * <p>
 * The movement of the vessel (e.g. the hydrophone may thread behind the vessel
 * <p>
 * Any sensor information available, e.g. depth, Terella, etc. 
 * 
 * @author Doug Gillespie
 *
 */
abstract public class HydrophoneLocator implements Serializable, Cloneable, ManagedParameters {

	private static final long serialVersionUID = 3820622344712667731L;

//	static public final int ANGLE_RE_NORTH = 1;
//
//	static public final int ANGLE_RE_SHIP = 2;
//
//	static public final int ANGLE_RE_ARRAY = 3;

	abstract public LocatorSettings getDefaultSettings();

	abstract public String getName();

	protected PamArray pamArray;

	protected Streamer streamer;

	public HydrophoneLocator(PamArray pamArray, Streamer streamer) {
		super();
		this.pamArray = pamArray;
		this.streamer = streamer;
	}

	/**
	 * Gets a reference LatLong for the streamer for the given time including any 
	 * orientation data available to the system. 
	 * @param timeMilliseconds time reference needed for 
	 * @return position, including heading pitch and roll data.
	 */
	public final GpsData getStreamerLatLong(long timeMilliseconds) {
		GpsData gpsData = getStreamerGpsPos(timeMilliseconds);
		if (gpsData == null) {
			return null;
		}
		getStreamer().getSensorManager().getOrientationData(gpsData, timeMilliseconds);
		return gpsData;
	}
	
	/**
	 * Get streamer GPS data. This is only used internally and most calls should be to 
	 * getStreamerLatLong, which returns an object that also has the orientation data. 
	 * @param timeMilliseconds
	 * @return Gps data (without orientation sensors)
	 */
	protected abstract GpsData getStreamerGpsPos(long timeMilliseconds);


	/**
	 * Get's the LatLong of a specific hydrophone at a given time. 
	 * @param timeMilliseconds time position needed for
	 * @param phoneNo Hydrophone number
	 * @return Hydrophone LatLong
	 */
	public GpsData getPhoneLatLong(long timeMilliseconds, int phoneNo) {
		
		// start with the hydrophone position and orientation. 
		GpsData streamerPos = getStreamerLatLong(timeMilliseconds);
		if (phoneNo == 0) {
			return streamerPos;
		}
		
		LatLong hydPos = streamerPos;
		Hydrophone hyd = pamArray.getHydrophone(phoneNo, timeMilliseconds);

		double streamerHead = streamerPos.getHeading();
		double streamerPitch = streamerPos.getPitch();
		double streamerRoll = streamerPos.getRoll();
//		System.out.printf("HPR = %5.1f, %5.1f, %5.1f\n", streamerHead, streamerPitch, streamerRoll);
		PamQuaternion streamerQuaternion = new PamQuaternion(Math.toRadians(streamerHead), Math.toRadians(streamerPitch), Math.toRadians(streamerRoll));
		PamVector hydrophoneVec = new PamVector(hyd.getCoordinates());

		hydrophoneVec = PamVector.rotateVector(hydrophoneVec, streamerQuaternion);
		hydPos = hydPos.addDistanceMeters(hydrophoneVec);
		
//		if (hyd.getX() != 0) {
//			hydPos = hydPos.travelDistanceMeters(streamerPos.getHeading()+90, hyd.getX());
//		}
//		if (hyd.getY() != 0) {
//			hydPos = hydPos.travelDistanceMeters(streamerPos.getHeading(), hyd.getY());
//		}
//		if (hyd.getZ() != 0) {
//			hydPos.setHeight(hydPos.getHeight()+hyd.getZ());
//		}
		
		// now set the xyz values back into a clone of the streamerpos. 
		streamerPos = streamerPos.clone();
		streamerPos.setLatitude(hydPos.getLatitude());
		streamerPos.setLongitude(hydPos.getLongitude());
		streamerPos.setHeight(hydPos.getHeight());
		
		return streamerPos;
	}

	//	/**
	//	 * Get's the depth of a specific hydrophone at a given time. 
	//	 * @param timeMilliseconds time depth needed for
	//	 * @param phoneNo Hydrophone number
	//	 * @return Hydrophone depth
	//	 */
	//	abstract public double getPhoneHeight(long timeMilliseconds, int phoneNo);
	//
	//	/**
	//	 * Get's the tilt of a specific hydrophone at a given time. 
	//	 * @param timeMilliseconds time tilt needed for
	//	 * @param phoneNo Hydrophone number
	//	 * @return Hydrophone tilt
	//	 */
	//	abstract public double getPhoneTilt(long timeMilliseconds, int phoneNo);
	//	
	//	/**
	//	 * Get's the angle between a pair of hydrophones
	//	 * @param timeMilliseconds time angle is needed for
	//	 * @param phone1 First hydrophone
	//	 * @param phone2 Second Hydrophone
	//	 * @param angleType Type of angle - ANGLE_RE_NORTH; ANGLE_RE_SHIP; ANGLE_RE_ARRAY;
	//	 * @return angle in radians
	//	 */
	//	abstract public double getPairAngle(long timeMilliseconds, int phone1, int phone2, int angleType);
	//	
	//	/**
	//	 * Gets the distance between a pair of hydrophones in metres. 
	//	 * @param timeMilliseconds time angle is needed for
	//	 * @param phone1 First hydrophone
	//	 * @param phone2 Second Hydrophone
	//	 * @return distance in metres.
	//	 */
	//	abstract public double getPairSeparation(long timeMilliseconds, int phone1, int phone2);

	abstract public void notifyModelChanged(int changeType, boolean initComplete);

	//	/**
	//	 * Get the hydrophone coordinates as a three element (x,y,z) vector
	//	 * referenced to the nominal array position (GPS for towed systems, a fixed point, etc.)
	//	 * <p> Hydrophone locators which want to vary location as a function of 
	//	 * time should override this function and use the timeMilliseconds 
	//	 * parameter to get the true location of the hydrophones in their own way. 
	//	 * @param iHydrophone hydrophone number
	//	 * @param timeMilliseconds time of location
	//	 * @return three element position vector. 
	//	 */
	//	abstract public double[] getHydrophoneCoordinates(int iHydrophone, long timeMilliseconds);


	//	/**
	//	 * Get the hydrophone coordinate errors as a three element (x,y,z) vector. 
	//	 * <p> Hydrophone locators which want to vary this error as a function of 
	//	 * time should override this function and use the timeMilliseconds 
	//	 * parameter to get the true errors in their own way. 
	//	 * @param iHydrophone hydrophone number
	//	 * @param timeMilliseconds time of location
	//	 * @return three element error vector. 
	//	 */
	//	abstract public double[] getHydrophoneCoordinateErrors(int iHydrophone, long timeMilliseconds);

	//	/**
	//	 * Return the heading in degrees of the hydrophone array
	//	 * @param gpsData
	//	 * @return heading in DEGREES. Can return null;
	//	 */
	//	abstract public Double getArrayHeading(GpsData gpsData);
	//	
	//	/**
	//	 * Get the heading of the array at the given time. 
	//	 * @param timeMillieseconds time in milliseconds. 
	//	 * @param phoneNo Hydrophone number (the array may be heading in different directions at different places !)
	//	 * @return Heading in standard navigation units of degrees clockwise
	//	 * from North. 
	//	 */
	//	abstract public double getArrayHeading(long timeMilliseconds, int phoneNo);


	/**
	 * 
	 * @return true if it's some sort of static array (not using GPS data in any way). 
	 */
	abstract public boolean isStatic();

	/**
	 * Note- changeable refers to the array positions changing relative to each other. i.e. the array doesn't change with heading or gps position, it's only changeable if the distances between hydrophones
	 * can change with time. 
	 * @return true if the hydrophone positions within the array can change with time
	 */
	abstract public boolean isChangeable();

	abstract public LocatorSettings getLocatorSettings();

	abstract public boolean setLocatorSettings(LocatorSettings locatorSettings);

	/**
	 * Get the auto-generated parameter set, and then add in the fields that are not included
	 * because they are not public and do not have getters.
	 * Note: for each field, we search the current class and (if that fails) the superclass.  It's
	 * done this way in case HydrophoneLocator is used directly (and thus the field would
	 * be found in the class), or else it might be used as a superclass to something else
	 * (e.g. MasterLocator) in which case the field would only be found in the superclass.
	 */
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("pamArray");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return pamArray;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			try {
				Field field = this.getClass().getSuperclass().getDeclaredField("pamArray");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						return pamArray;
					}
				});
			} catch (NoSuchFieldException | SecurityException e2) {
				e2.printStackTrace();
			}
		}

		try {
			Field field = this.getClass().getDeclaredField("streamer");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return streamer;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			try {
				Field field = this.getClass().getSuperclass().getDeclaredField("streamer");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						return streamer;
					}
				});
			} catch (NoSuchFieldException | SecurityException e2) {
				e2.printStackTrace();
			}
		}

		return ps;
	}

	/**
	 * @return the streamer
	 */
	public Streamer getStreamer() {
		return streamer;
	}

}
