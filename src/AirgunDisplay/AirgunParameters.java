package AirgunDisplay;

import java.awt.Color;
import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamUtils.LatLong;

public class AirgunParameters implements Serializable, Cloneable, ManagedParameters {

	public static final int GUNS_THIS_VESSEL = 0;
	public static final int GUNS_AIS_VESSEL = 1;
	public static final int GUNS_FIXEDPOSITION = 2;

	static public final long serialVersionUID = 1;
	
	/**
	 * True if guns are on this vessel
	 */
	private boolean gunsThisVessel = true;
	
	public int gunsReferencePosition = GUNS_THIS_VESSEL;
	private boolean hasGunsReferencePosition = false;
	
	/**
	 * mmsi number of vessel if guns are on another vessel
	 */
	public int gunsMMSIVessel = 0;

	/**
	 * distance in m towards the stern from the vessels GPS receiver
	 */
	double dimE = 20;
	
	/**
	 * distance in m towards the starboard side from the vessels GPS receiver
	 */
	double dimF = 0;
	
	/**
	 * Gun depth in metres. 
	 */
	double gunDepth = 0;
	
	/**
	 * Show exclusion zone on the map
	 */
	boolean showExclusionZone = true;
	
	/**
	 * radius of exclusion xone in m
	 */
	int exclusionRadius = 500;
	
	/**
	 * Colour for exclusion zone on map.
	 */
	Color exclusionColor = Color.RED;
	
	/**
	 * predict where we'll be in a certain time
	 */
	boolean predictAhead = false;
	
	/**
	 *  prediction time in seconds
	 */
	int secondsAhead = 600;
	
	public LatLong fixedPosition;
	

	@Override
	public AirgunParameters clone() {
		try {
			AirgunParameters np =  (AirgunParameters) super.clone();
			if (np.hasGunsReferencePosition == false) {
				// sort out what happens if old boolean ref was used
				// instead of new int type. 
				np.hasGunsReferencePosition = true;
				if (np.gunsThisVessel == false) {
					np.gunsReferencePosition = GUNS_AIS_VESSEL;
				}
			}
			return np;
		}
		catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("dimE");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return dimE;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("dimF");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return dimF;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("exclusionColor");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return exclusionColor;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("exclusionRadius");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return exclusionRadius;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("gunDepth");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return gunDepth;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("gunsThisVessel");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return gunsThisVessel;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("hasGunsReferencePosition");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return hasGunsReferencePosition;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("predictAhead");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return predictAhead;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("secondsAhead");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return secondsAhead;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("showExclusionZone");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return showExclusionZone;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}


}
