package videoRangePanel.vrmethods.landMarkMethod;

import java.io.Serializable;

import PamUtils.LatLong;

/**
 * Class for storing Landmark information. A landmark may be known exactly using GPS information, 
 * however a landmark may only have a bearing and pitch from a specified location if not possible 
 * to fix and exact location. 
 * @author Jamie Macaulay
 *
 */
public class LandMark extends Object implements Serializable, Cloneable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Name of the Landmark	
	 */
	private String name;
	
	/**
	 * Position of theLandMark including height.
	 */
	private LatLong position;
	
	/**
	 * Bearing to a LandMark
	 */
	private Double bearing;
	
	/**
	 * Pitch to LandMark
	 */
	private Double pitch;
	
	/**
	 * LatLong and height from which bearing measurment was taken
	 */
	private LatLong latLongOrigin; 
	
	public LandMark(){
		
	}

	public LandMark(String name, LatLong position, Double height){
		this.name=name;
		this.position=position; 
		position.setHeight(height);
	}
	
	
	public LandMark(String name, double bearing, double pitch , LatLong latLongOrigin) {
		this.bearing=bearing;
		this.pitch=pitch;
		this.latLongOrigin=latLongOrigin;
	}
	
	public void update(LandMark newData) {
		bearing=newData.getBearing();
		pitch=newData.getPitch();
		latLongOrigin=newData.getLatLongOrigin();
		position=newData.getPosition();
		name = new String(newData.name);
	}
	
	/**
	 * Get the name of the Landmark
	 * @return the name of the landmark 
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the latitude and longitude position of the landmark. 
	 * Note that this can be null if bearings are used instead. 
	 * @return the lat lon position of the landmark 
	 */
	public LatLong getPosition() {
		return position;
	}
	
	/**
	 * Get the height of the landmark  
	 * @return the height of the landmark in meters
	 */
	public double getHeight() {
		return position.getHeight();
	}

	/**
	 * Set the name of the landmark. 
	 * @param name - the nmae of the landmark
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set the latiide and longitude of the landmark. 
	 * @param position
	 */
	public void setPosition(LatLong position) {
		this.position = position;
	}
	
	
	/**
	 * Get the horizontal bearing to the landmark from the image location. 
	 * This has limits of -pi to pi and is in RADIANS. 
	 * @return the horizontal bearing to the landmark in RADIANS. 
	 */
	public Double getBearing() {
		return bearing;
	}

	/**
	 * The pitch of landmark from the image location limits of -pi/2 to pi/2 in RADIANS
	 * @return the image pitch in RADIANS. 
	 */
	public Double getPitch() {
		return pitch;
	}

	/**
	 * Get the height of the image. 
	 * @return the height of the landmark 
	 */
	public Double getHeightOrigin() {
		if (latLongOrigin==null) return null; 
		return latLongOrigin.getHeight();
	}

	/**
	 * Get the location the bearings to the landmark are from 
	 * @return the location the horizontal and vertical bearings are from. 
	 */
	public LatLong getLatLongOrigin() {
		return latLongOrigin;
	}

	/**
	 * Set the horizontal bearing in RADIANS
	 * @param bearing - the horizontal bearings in RADIANS. 
	 */
	public void setBearing(Double bearing) {
		this.bearing = bearing;
	}

	/**
	 * Set the pitch of the landmark form the image origin. 
	 * @param pitch - the pitch in RADIANS.
	 */
	public void setPitch(Double pitch) {
		this.pitch = pitch;
	}

	/**
	 * Set the height of the location the horizontal and vertical bearings to the landmark 
	 * are measured from. 
	 * @param heightOrigin - the height the landmark pitch and horizontal angles are measured from. 
	 */
	public void setHeightOrigin(Double heightOrigin) {
		this.latLongOrigin.setHeight(heightOrigin);
	}

	/**
	 * Set the location the horizontal and vertcial angles of the landmark are measured from
	 * @param latLongOrigin - the location the landmark horizontal bearings are measured from. 
	 */
	public void setLatLongOrigin(LatLong latLongOrigin) {
		this.latLongOrigin = latLongOrigin;
	}

	
	@Override
	public LandMark clone() {
		try {
			return (LandMark) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	

}
