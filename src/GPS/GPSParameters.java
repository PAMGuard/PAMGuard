package GPS;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;

public class GPSParameters implements Serializable, Cloneable, ManagedParameters {

	static public final long serialVersionUID = 3;

	public boolean setClockOnStartup = false;
	
	protected String nmeaSource;
	
	// ship dimensions relative to GPS receiver
	/**
	 * distance from bow to antenna
	 */
	public double dimA; 
	/**
	 * distance from antenna to stern
	 */
	public double dimB; 
	/**
	 * distance from port side to antenna
	 */
	public double dimC;
	/**
	 * distance from antenna to starboard side
	 */
	public double dimD;
	
	/**
	 * On the map, work out where the ship will be and draw an arrow to that point
	 */
	public boolean plotPredictedPosition = false;
	
	/**
	 * time for prediction arrow (seconds)
	 */
	public int predictionTime = 600;
	
	public boolean readHeading;
	
	public String headingNMEASource;
	
	public static final int READ_GGA = 1;
	public static final int READ_RMC = 0;
	/**
	 * Which strings to read
	 */
	public int mainString = READ_RMC;
	/**
	 * String initials
	 */
	public String rmcInitials = "GP";
	public String ggaInitials = "GP";
	
	/**
	 * Attempt to read true heading information
	 */
	public boolean readTrueHeading;
	/**
	 * String for true heading
	 */
	public String headingString = "SDVHW";
	
	/**
	 * position of data in headingString
	 */
	public int headingStringPos = 1;
	
	/**
	 * GPS HEading Smoothing
	 */
	public boolean headingSmoothing = false;
	
	/**
	 * GPS smoothing time in seconds. 
	 */
	private float smoothingTime = 10;
	
	/**
	 * Flag indicating whether or not to smooth the GPS data
	 */
	private boolean smoothingFlag = false;
	
	/**
	 * storage and read options: Can either read everything that comes in
	 * or just read every n seconds, or be a bit more clever and read every
	 * n seconds OR whenever course or speed have changed by more than than
	 * some set amount. 
	 */
	public int readType = READ_ALL;
	
	/**
	 * Read everything
	 */
	static public final int READ_ALL = 0;
	
	/**
	 * Read on a fixed time interval
	 */
	static public final int READ_TIMER = 1;
	
	/** 
	 * Read on a fixed time interval or when course or speed change
	 */
	static public final int READ_DYNAMIC = 2;
	
	/** 
	 * Interval between reads in seconds
	 */
	public int readInterval = 10;
	
	/**
	 * Minimum course change for a dynamic read
	 */
	public double courseInterval = 2; 
	
	/**
	 * Minimum speed change for a dynamic read
	 */
	public double speedInterval = 0.1;
	
	

	@Override
	public GPSParameters clone() {
		try {
			return (GPSParameters) super.clone();
		}
		catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}



	public float getSmoothingTime() {
		return smoothingTime;
	}



	public void setSmoothingTime(float smoothingTime) {
		this.smoothingTime = smoothingTime;
	}


	/**
	 * Whether or not to smooth the GPS signal.  True=smooth, False=don't
	 * 
	 * @return true=smooth signal, false=don't smooth signal
	 */
	public boolean weShouldSmooth() {
		return smoothingFlag;
	}


	/**
	 * Set whether to smooth GPS signal or not.  True = smooth, False=don't
	 * 
	 * @param smoothingFlag
	 */
	public void setSmoothingFlag(boolean smoothingFlag) {
		this.smoothingFlag = smoothingFlag;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("nmeaSource");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return nmeaSource;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("smoothingFlag");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return smoothingFlag;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("smoothingTime");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return smoothingTime;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}


}
