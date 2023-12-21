package Array;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

import Array.sensors.ArrayParameterType;
import Array.sensors.ArraySensorFieldType;
import Array.sensors.StreamerSensorManager;
import Array.streamerOrigin.GPSOriginMethod;
import Array.streamerOrigin.GPSOriginSettings;
import Array.streamerOrigin.HydrophoneOriginMethod;
import Array.streamerOrigin.HydrophoneOriginMethods;
import Array.streamerOrigin.OriginSettings;
import PamController.PamController;
import PamController.masterReference.MasterReferencePoint;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import geoMag.MagneticVariation;
import pamMaths.PamVector;

/**
 * Contains information on an individual streamer -a Streamer is loosely defined
 * as a group of hydrophones who's positions are fixed relative to the streamer. Streamers
 * can have positions, headings, pitch and roll and an offset from their referene position, which 
 * will generally be 0,0,-depth for a static system and 0,-hydLengh,-depth for a towing system. 
 * Each hydrophone element will belong to one streamer. 
 * @author Doug Gillespie
 */
public class Streamer implements Serializable, Cloneable, ManagedParameters {


	public static final long serialVersionUID = 1L;

	/**
	 * Streamer Id;
	 */
//	private int Id;
	private transient HydrophoneLocator hydrophoneLocator;
	
	private transient HydrophoneOriginMethod hydrophoneOrigin;
	
	private transient StreamerSensorManager sensorManager;
	
	private ArrayList<LocatorSettings> allLocatorSettings;
	
	private ArrayList<OriginSettings> allOriginSettings;
	
	private Class locatorClass = ThreadingHydrophoneLocator.class;
	
	private Class originClass = GPSOriginMethod.class;
	
	// just used to store the info about the locator.
	// during psf serialisation
//	private LocatorSettings locatorSettings = new LocatorSettings();
	
		
	/**
	 * Streamer coordinates. 
	 */
	private double[] coordinate = new double[3];
	
	/**
	 * Errors on streamer location in x,y,z
	 */
	private double[] coordinateError = new double[3];
	
	/**
	 * Identifier to use with PAMbuoy. 
	 */
//	private int unusedbuoyId1;
	
//	private transient BuoyStatusDataUnit buoyStats;
	
	private String streamerName;
	
//	private boolean enableOrientation;
	
	private ArrayParameterType[] orientationTypes;
	
	private String[] sensorDataBlocks;
	
	/**
	 * Euler angles in degrees.
	 */
	private Double heading, pitch, roll;

	/**
	 * The streamer doesn't need to hold the reference to the locator though
	 * That's held within the MasterLocator - still need to work out 
	 * how to serialise that with the array settings. 
	 */
	public static final int REF_SHIP_GPS = 0;
	public static final int REF_BUOY = 1;
	
//	private int positionReference = REF_SHIP_GPS;

	private PamArray pamArray;

	private int streamerIndex;

	/**
	 * 
	 * @param x    x coordinate of streamer (m)
	 * @param dx   estimated error on x location of streamer (m)
	 * @param dy   estimated error on y location of streamer (m)
	 * @param dz   estimated error on z location of streamer (m)
	 */
	public Streamer(int streamerIndex, double x, double y, double z, double dx, double dy, double dz) {
		super();
		checkAllocations();
		this.streamerIndex = streamerIndex;
		coordinate[0] = x;
		coordinate[1] = y;
		coordinate[2] = z;
		coordinateError[0] = dx;
		coordinateError[1] = dy;
		coordinateError[2] = dz;
	}

	private void checkAllocations() {
		if (coordinate == null) {
			coordinate = new double[3];
		}
		if (coordinateError == null) {
			coordinateError = new double[3];
		}
	}

	public Streamer(int streamerIndex) {
		super();
		this.streamerIndex=streamerIndex;
	}

	// copy constructor 
	public Streamer(Streamer firstStreamer, int streamerIndex, String streamerName) {
		this.streamerIndex = streamerIndex;
		this.coordinate = firstStreamer.coordinate.clone();
		this.coordinateError = firstStreamer.coordinateError.clone();
//		this.unusedbuoyId1 = buoyId;
		this.streamerName = streamerName;
	}

	@Override
	public Streamer clone()  {
		try {
			Streamer newStreamer =  (Streamer) super.clone();
			newStreamer.allLocatorSettings = new ArrayList<>(allLocatorSettings);
			newStreamer.allOriginSettings = new ArrayList<>(allOriginSettings);
			newStreamer.checkAllocations();
			OriginSettings os = this.getOriginSettings();
			if (os != null) {
				newStreamer.setOriginSettings(os.clone());
			}
			else {
				this.allOriginSettings.add(new GPSOriginSettings());
			}
			LocatorSettings ls = this.getLocatorSettings();
			if (ls != null) {
				newStreamer.setLocatorSettings(ls.clone());
			}
			newStreamer.setupLocator(pamArray);
			return newStreamer;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

//	public int getId() {
//		return Id;
//	}
	
	public void setCoordinates(double[] coordinates) {
		this.coordinate = coordinates;
	}
	
	public void setCoordinate(int iCoordinate, double coordinate) {
		checkAllocations();
		this.coordinate[iCoordinate] = coordinate;
	}
	
	public double[] getCoordinates() {
		checkAllocations();
		return coordinate;
	}
	
	public double getCoordinate(int iCoordinate) {
		checkAllocations();
		return coordinate[iCoordinate];
	}
	
	public double[] getCoordinateError() {
		checkAllocations();
		return coordinateError;
	}
	
	public double getCoordinateError(int iCoordinate) {
		checkAllocations();
		return coordinateError[iCoordinate];
	}
	
	public void setCoordinateErrors(double[] coordinateErrors) {
		this.coordinateError = coordinateErrors;
	}
	
	public void setCoordinateError(int iCoordinate, double error) {
		checkAllocations();
		this.coordinateError[iCoordinate] = error;
	}
	
	public PamVector getCoordinateVector() {
		return new PamVector(coordinate);
	}
	
	public PamVector getErrorVector() {
		return new PamVector(coordinateError);
	}

	public void setX(double x) {
		checkAllocations();
		coordinate[0] = x;
	}
	
	public void setY(double x) {
		checkAllocations();
		coordinate[1] = x;
	}
	
	public void setZ(double x) {
		checkAllocations();
		coordinate[2] = x;
	}

	public void setDx(double dx) {
		checkAllocations();
		coordinateError[0] = dx;
	}

	public void setDy(double dy) {
		checkAllocations();
		coordinateError[1] = dy;
	}

	public void setDz(double dz) {
		checkAllocations();
		coordinateError[2] = dz;
	}

	public double getX() {
		checkAllocations();
		return coordinate[0];
	}
	
	public double getY() {
		checkAllocations();
		return coordinate[1];
	}
	
	public double getZ() {
		checkAllocations();
		return coordinate[2];
	}

	public double getDx() {
		checkAllocations();
		return coordinateError[0];
	}

	public double getDy() {
		checkAllocations();
		return coordinateError[1];
	}

	public double getDz() {
		checkAllocations();
		return coordinateError[2];
	}

//	/**
//	 * @param buoyId1 the buoyId1 to set
//	 */
//	public void setBuoyId1(Integer buoyId1) {
//		this.unusedbuoyId1 = buoyId1;
//	}
//
//	/**
//	 * @return the buoyId1
//	 */
//	public Integer getBuoyId1() {
//		return unusedbuoyId1;
//	}
//
//	/**
//	 * @param buoyStats the buoyStats to set
//	 */
//	public void setBuoyStats(BuoyStatusDataUnit buoyStats) {
//		this.buoyStats = buoyStats;
//		this.unusedbuoyId1 = buoyStats.getBuoyId1();
//	}

//	/**
//	 * @return the buoyStats
//	 */
//	public BuoyStatusDataUnit getBuoyStats() {
//		return buoyStats;
//	}

	public String getStreamerName() {
		if (streamerName == null) {
			streamerName = "Unknown Streamer";
		}
		return streamerName;
	}

	public void setStreamerName(String streamerName) {
		this.streamerName = streamerName;
	}

//	/**
//	 * 
//	 * @return the streamers default location, heading, speed, etc. 
//	 */
//	public GpsData getStreamerDefaultLocation() {
//		return streamerDefaultLocation;
//	}
//
//	/**
//	 * Set the streamers default location, heading, speed, etc.
//	 * @param streamerDefaultLocation
//	 */
//	public void setStreamerDefaultLocation(GpsData streamerDefaultLocation) {
//		this.streamerDefaultLocation = streamerDefaultLocation;
//	}

//	/**
//	 * Set the streamers default position. 
//	 * @param latLong
//	 */
//	public void setStreamerDefaultLocation(LatLong latLong) {
//		streamerDefaultLocation = new GpsData();
//		if (latLong != null) {
//			streamerDefaultLocation.setLatitude(latLong.getLatitude());
//			streamerDefaultLocation.setLongitude(latLong.getLongitude());
//			streamerDefaultLocation.setHeight(latLong.getHeight());
//		}
//	}

	/**
	 * @return the hydrophoneLocator
	 */
	public HydrophoneLocator getHydrophoneLocator() {
		return hydrophoneLocator;
	}

	/**
	 * @param hydrophoneLocator the hydrophoneLocator to set
	 */
	public void setHydrophoneLocator(HydrophoneLocator hydrophoneLocator) {
		this.hydrophoneLocator = hydrophoneLocator;
		this.locatorClass = hydrophoneLocator.getClass();
	}

	/*
	 * Called just before psf serialisation so that the streamer can 
	 * get the locator settings out of the locator. 
	 */
//	public LocatorSettings getLocatorSettings() {
//		locatorSettings = hydrophoneLocator.getFinalLocatorSettings();
//		locatorSettings.locatorClass = hydrophoneLocator.getClass();
//		locatorSettings.originSettings = hydrophoneLocator.getHydrophoneOriginMethod().getOriginSettings();
//	}
	
	/**
	 * Get the settings for the currently set locator method. 
	 * @return settings for the current locator method. 
	 */
	public LocatorSettings getLocatorSettings() {
		return getLocatorSettings(locatorClass);
	}
	
	/**
	 * Get the locator settings for the currently selected hydrophone locator
	 * method
	 * @param locatorClass class for the current locator method
	 * @return settings for the specified locator method. 
	 */
	public LocatorSettings getLocatorSettings(Class locatorClass) {
		if (allLocatorSettings == null) {
			allLocatorSettings = new ArrayList<LocatorSettings>();
			return null;
		}
		for (LocatorSettings ls:allLocatorSettings) {
			if (ls.locatorClass == locatorClass) {
				return ls;
			}
		}
		return null;
	}
	
	/**
	 * Set the locator settings. Streamer actually holds a list of locator
	 * settings for all the different available locators, so will check that 
	 * list and remove any ones that have the same locator class before 
	 * adding this to the list. 
	 * @param locatorSettings new locator settings. 
	 */
	public void setLocatorSettings(LocatorSettings locatorSettings) {
		// remove any locator settings that have that locator class first. 
		LocatorSettings exSettings = getLocatorSettings(locatorSettings.locatorClass);
		if (exSettings != null) {
			allLocatorSettings.remove(exSettings);
		}
		allLocatorSettings.add(locatorSettings);
		locatorClass = locatorSettings.locatorClass;
	}
	
	/**
	 * Set the origin settings. Streamer actually holds a list of 
	 * origin settings for all the different origin types, so will check there
	 * aren't any for that type and remove them from the master list before
	 * adding these. 
	 * @param originSettings new origin settings to store. 
	 */
	public void setOriginSettings(OriginSettings originSettings) {
		// remove any settings that have that origin method class
		if (allOriginSettings == null) {
			allOriginSettings = new ArrayList<OriginSettings>();
		}
		Iterator<OriginSettings> li = allOriginSettings.iterator();
		while (li.hasNext()) {
			if (li.next().originMethodClass == originSettings.originMethodClass) {
				li.remove();
			}
		}
		
		OriginSettings exSettings = getOriginSettings(originSettings.originMethodClass);
		if (exSettings != null) {
			allOriginSettings.remove(exSettings);
		}
		allOriginSettings.add(originSettings);
		originClass = originSettings.originMethodClass;
	}

	/**
	 * Get the hydrophone origin settings for the currently selected origin method.
	 * @return origin settings for current method
	 */
	public OriginSettings getOriginSettings() {
		return getOriginSettings(originClass);
	}
	
	/**
	 * Get the hydrophone origin settings for a specific origin method class. 
	 * @param originClass origin method class. 
	 * @return origin settings for that class (or null if there are none). 
	 */
	public OriginSettings getOriginSettings(Class originClass) {
		if (allOriginSettings == null) {
			allOriginSettings = new ArrayList<OriginSettings>();
			return null;
		}
		for (OriginSettings ho:allOriginSettings) {
			if (ho.originMethodClass == originClass) {
				return ho;
			}
		}
		return null;
	}

	/**
	 * Set up the hydrophone locator. Note that only the 
	 * locatorSettings are serialised in psf files, to it's
	 * necessary to construct the actual locator and it's 
	 * corresponding hydrophone origin system. <p>
	 * Some locators (such as static, are also no longer
	 * used, since they are superseded by a fixed hydrophone origin and
	 * a rigid locator. 
	 * @param pamArray
	 */
	public void setupLocator(PamArray pamArray) {
		this.pamArray = pamArray; // may not be set in old serialised versions. 
		// first make sure that there is a locator for the streamer. 
		if (locatorClass == null) {
//			if (pamArray.getArrayLocatorClass() == StaticHydrophoneLocator.class || 
//					pamArray.getArrayLocatorClass()  == NetworkHydrophoneLocatorSystem.class) {
//				locatorClass = StraightHydrophoneLocator.class;
//				originClass = StaticOriginMethod.class;
//				HydrophoneLocator locator = HydrophoneLocators.getInstance().getLocatorSystem(locatorClass).getLocator(pamArray, this);
//				setLocatorSettings(locator.getDefaultSettings());
////				HydrophoneOriginMethod origin = HydrophoneOriginMethods.getInstance().getMethod(originClass);
//			}
//			else 
			if (pamArray.getArrayLocatorClass() == StraightHydrophoneLocator.class) {
				locatorClass = StraightHydrophoneLocator.class;
				originClass = GPSOriginMethod.class;
				HydrophoneLocator locator = HydrophoneLocators.getInstance().getLocatorSystem(locatorClass).getLocator(pamArray, this);
				setLocatorSettings(locator.getDefaultSettings());
			}
			else {
				locatorClass = ThreadingHydrophoneLocator.class;
				originClass = GPSOriginMethod.class;
				HydrophoneLocator locator = HydrophoneLocators.getInstance().getLocatorSystem(locatorClass).getLocator(pamArray, this);
				setLocatorSettings(locator.getDefaultSettings());
			}
		}
		// now make the actual locator we want ...
		hydrophoneLocator = HydrophoneLocators.getInstance().getLocator(pamArray, this);
		LocatorSettings ls = getLocatorSettings(locatorClass);
		if (ls != null) {
			hydrophoneLocator.setLocatorSettings(ls);
		}
		hydrophoneOrigin = HydrophoneOriginMethods.getInstance().getMethod(originClass, pamArray, this);
		OriginSettings os = getOriginSettings(originClass);
		if (os != null) {
			hydrophoneOrigin.setOriginSettings(os);
		}
		
		sensorManager = new StreamerSensorManager(this); 

	}

	/**
	 * @return the hydrophoneOrigin
	 */
	public HydrophoneOriginMethod getHydrophoneOrigin() {
		return hydrophoneOrigin;
	}

	/**
	 * @param hydrophoneOrigin the hydrophoneOrigin to set
	 */
	public void setHydrophoneOrigin(HydrophoneOriginMethod hydrophoneOrigin) {
		this.hydrophoneOrigin = hydrophoneOrigin;
		this.originClass = hydrophoneOrigin.getClass();
	}

	/**
	 * @return the locatorClass
	 */
	protected Class getLocatorClass() {
		return locatorClass;
	}

	/**
	 * @return the originClass
	 */
	protected Class getOriginClass() {
		return originClass;
	}

	public void notifyModelChanged(int changeType, boolean initComplete) {
		if (initComplete) {
			hydrophoneOrigin.prepare();
		}
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
//			if (PamController.getInstance().getRunMode() == PamController.RUN_NORMAL) {
//				makeStreamerDataUnit();
//			}
			break;
		}
	}
	
	/**
	 * Make a streamer data unit and add it to the data block. 
	 */
	public void makeStreamerDataUnit() {
		StreamerDataUnit sdu = new StreamerDataUnit(PamCalendar.getTimeInMillis(), this);
		ArrayManager.getArrayManager().getStreamerDatabBlock().addPamData(sdu);
	}

	/**
	 * @return the heading
	 */
	public Double getHeading() {
		return heading;
	}

	/**
	 * @param heading the heading to set
	 */
	public void setHeading(Double heading) {
		this.heading = heading;
	}

	/**
	 * Set magnetic heading. Will automatically correct
	 * to true using geomag stuff. 
	 * @param heading the magnetic heading to set
	 */
	public void setMagneticHeading(Double heading) {
		if (heading != null) {
			LatLong masterLL = MasterReferencePoint.getLatLong();
			if (masterLL != null) {
				double var = MagneticVariation.getInstance().getVariation(PamCalendar.getTimeInMillis(), masterLL.getLatitude(), masterLL.getLongitude());
				heading += var;
			}
		}
		this.heading = heading;
	}

	/**
	 * @return the pitch in degrees
	 */
	public Double getPitch() {
		return pitch;
	}

	/**
	 * @param pitch the pitch to set in degrees
	 */
	public void setPitch(Double pitch) {
		this.pitch = pitch;
	}

	/**
	 * @return the roll in degrees
	 */
	public Double getRoll() {
		return roll;
	}

	/**
	 * @param roll the roll to set (degrees)
	 */
	public void setRoll(Double roll) {
		this.roll = roll;
	}

//	/**
//	 * @return the enableOrientation
//	 */
//	public boolean isEnableOrientation() {
//		return enableOrientation;
//	}
//
//	/**
//	 * @param enableOrientation the enableOrientation to set
//	 */
//	public void setEnableOrientation(boolean enableOrientation) {
//		this.enableOrientation = enableOrientation;
//	}

	/**
	 * @return the streamerIndex
	 */
	public int getStreamerIndex() {
		return streamerIndex;
	}

	/**
	 * @param streamerIndex the streamerIndex to set
	 */
	protected void setStreamerIndex(int streamerIndex) {
		this.streamerIndex = streamerIndex;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + "; OriginSettings: " + getOriginSettings()==null ? "null" : getOriginSettings().toString() + "," + getHydrophoneOrigin().getOriginSettings().toString() + 
				"; Locator " + getLocatorSettings()==null ? "null" : getLocatorSettings().toString();
	}

	public static Streamer getAverage(Streamer sd1,
			Streamer sd2) {
		if (sd1 == null) {
			return sd2;
		}
		if (sd2 == null) {
			return sd1;
		}
		Streamer s = sd1.clone();
		for (int i = 0; i < 3; i++) {
			s.coordinate[i] = (sd1.coordinate[i]+sd2.coordinate[i])/2;
			s.coordinateError[i] = (sd1.coordinateError[i]+sd2.coordinateError[i])/2;
			// TODO need to work out a way of averaging the position data - most important of all !
		}
		s.heading = PamUtils.angleAverageDegrees(sd1.heading, sd2.heading);
		s.pitch = PamUtils.angleAverageDegrees(sd1.pitch, sd2.pitch);
		s.roll = PamUtils.angleAverageDegrees(sd1.roll, sd2.roll);
		
		return s;
	}
	
	/**
	 * get a weighted average of two streamer datas. Used when interpolating. 
	 * @param sd1
	 * @param sd2
	 * @param w1
	 * @param w2
	 * @return
	 */
	public static Streamer weightedAverage(Streamer sd1, Streamer sd2, double w1, double w2) {
		if (sd1 == null) {
			return sd2;
		}
		if (sd2 == null) {
			return sd1;
		}
		
		double wTot = w1+w2;
		Streamer s = sd1.clone();
		for (int i = 0; i < 3; i++) {
			s.coordinate[i] = (sd1.coordinate[i]*w1 + sd2.coordinate[i]*w2)/wTot;
			s.coordinateError[i] = (sd1.coordinateError[i]*w1 + sd2.coordinateError[i]*w2)/wTot;
		}

		s.heading = PamUtils.angleAverageDegrees(sd1.heading, sd2.heading, w1, w2);
		s.pitch = PamUtils.angleAverageDegrees(sd1.pitch, sd2.pitch, w1, w2);
		s.roll = PamUtils.angleAverageDegrees(sd1.roll, sd2.roll, w1, w2);
		
		return s;
	}

	/**
	 * Get a list of hydrophones that go with this streamer. 
	 * @return
	 */
	public int getStreamerHydrophones() {
		int phones = 0;
		if (pamArray == null) {
			return 0;
		}
		int nP = pamArray.getHydrophoneCount();
		for (int i = 0; i < nP; i++) {
			Hydrophone h = pamArray.getHydrophone(i);
			if (h.getStreamerId() == this.streamerIndex) {
				phones |= 1<<i;
			}
		}
		return phones;
	}
	/**
	 * Check if two streamers have identical values for all parameters
	 * @param s2
	 * @return
	 */
	public boolean equals(Streamer s2){
		
		if (this == null || s2 == null) return false;
		
		//Check the coordinates are the same
		for (int i=0; i<3; i++) {
			if (this.coordinate[i] != s2.coordinate[i] ||
					this.coordinateError[i] != s2.coordinate[i])
				return false;
		}
		
		// Check that all the other parametes are the same
		return (this.heading			== s2.heading && 
				this.pitch   			== s2.pitch &&
				this.roll   			== s2.roll &&
				this.streamerName		== s2.streamerName &&
				this.hydrophoneOrigin 	== s2.hydrophoneOrigin &&
				this.hydrophoneLocator	== s2.hydrophoneLocator &&
				this.streamerIndex 		== s2.streamerIndex);
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("coordinate");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return coordinate;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		// don't include pamArray field, because that puts us in a recursive loop (e.g. pamArray contains streamer, and streamer contains pamArray)
//		try {
//			Field field = this.getClass().getDeclaredField("pamArray");
//			ps.put(new PrivatePamParameterData(this, field) {
//				@Override
//				public Object getData() throws IllegalArgumentException, IllegalAccessException {
//					return pamArray;
//				}
//			});
//		} catch (NoSuchFieldException | SecurityException e) {
//			e.printStackTrace();
//		}
		return ps;
	}

	/**
	 * 
	 * @param sensorType type of sensor: HEAD, PITCH, ROLL, HEIGHT
	 * @return the type of data, which is one of FIXED, DEFAULT or SENSOR
	 */
	public ArrayParameterType getOrientationTypes(ArraySensorFieldType sensorType) {
		if (orientationTypes == null) {
			orientationTypes = new ArrayParameterType[ArraySensorFieldType.values().length];
		}
		// make sure we always return something valid. 
		if (orientationTypes[sensorType.ordinal()] == null) {
			orientationTypes[sensorType.ordinal()] = defaultParameterType(sensorType);
		}
		return orientationTypes[sensorType.ordinal()];
	}
	
	/**
	 * Get defalt values for the four orientation fields. Fixed for height, default for the 
	 * other three, which means zero for tilt and roll, but whatever was in the GPS data for heading. 
	 * @param fieldType
	 * @return
	 */
	private ArrayParameterType defaultParameterType(ArraySensorFieldType fieldType) {
		switch (fieldType) {
		case HEADING:
			return ArrayParameterType.DEFAULT;
		case HEIGHT:
			return ArrayParameterType.FIXED;
		case PITCH:
			return ArrayParameterType.DEFAULT;
		case ROLL:
			return ArrayParameterType.DEFAULT;
		default:
			return null;		
		}
	}


	/**
	 * 
	 * @param sensorType type of sensor: HEAD, PITCH, ROLL, HEIGHT
	 * @param orientationType  the type of data, which is one of FIXED, DEFAULT or SENSOR
	 */
	public void setOrientationTypes(ArraySensorFieldType sensorType, ArrayParameterType orientationType) {
		if (orientationTypes == null) {
			orientationTypes = new ArrayParameterType[ArraySensorFieldType.values().length];
		}
		this.orientationTypes[sensorType.ordinal()] = orientationType;
	}

	/**
	 * @return the sensorDataBlocks
	 */
	public String getSensorDataBlocks(ArraySensorFieldType sensorType) {
		if (sensorDataBlocks == null) {
			sensorDataBlocks = new String[ArraySensorFieldType.values().length];
		}
		return sensorDataBlocks[sensorType.ordinal()];
	}

	/**
	 * Get a value for one of the four orientation fields. 
	 * @param sensorType
	 * @return
	 */
	public Double getSensorField(ArraySensorFieldType sensorType) {
		switch (sensorType) {
		case HEADING:
			return getHeading();
		case HEIGHT:
			return getZ();
		case PITCH:
			return getPitch();
		case ROLL:
			return getRoll();
		default:
			return null;
		}
	}
	/**
	 * Set a value for one of the four orientation fields. 
	 * @param sensorType
	 * @return
	 */
	public void setSensorField(ArraySensorFieldType sensorType, Double value) {
		switch (sensorType) {
		case HEADING:
			setHeading(value);
			break;
		case HEIGHT:
			if (value != null) {
				setZ(value);;
			}
			break;
		case PITCH:
			setPitch(value);
			break;
		case ROLL:
			setRoll(value);
			break;
		}
	}
	/**
	 * @param sensorDataBlocks the sensorDataBlocks to set
	 */
	public void setSensorDataBlocks(ArraySensorFieldType sensorType, String sensorDataBlock) {
		if (sensorDataBlocks == null) {
			sensorDataBlocks = new String[ArraySensorFieldType.values().length];
		}
		this.sensorDataBlocks[sensorType.ordinal()] = sensorDataBlock;
	}

	/**
	 * Get the streamer sensor manager
	 * @return
	 */
	public StreamerSensorManager getSensorManager() {
		if (sensorManager == null) {
			sensorManager = new StreamerSensorManager(this);
		}
		return sensorManager;
	}


}
