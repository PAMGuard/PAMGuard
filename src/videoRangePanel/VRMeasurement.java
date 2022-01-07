package videoRangePanel;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.Serializable;

import videoRangePanel.vrmethods.VRMethod;

import PamUtils.LatLong;

/**
 * Class to store information on loclaisations using an image. All angles are stored in degrees. 
 * @author Doug Gillespie, modified by Jamie Macaulay. 
 *
 */
public class VRMeasurement implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//horizon and shore methods specific
	public Point horizonPoint;
	
	public double distancePixels;
		
	//animal info
	
	/**
	 * The integer number of the animal being measured from a single image. 
	 */
	public int imageAnimal;

	/**
	 * The estimated group size
	 */
	public int groupSize;

	//location information
	
	/**
	 * Animal point on the image
	 */
	public Point animalPoint;
	
	//polygon shape stuff.
	
	/**
	 * A list of points describing a shape on the image. Minimum must be three 
	 * elements. This can be null. 
	 */
	public Point[] polygonArea;
	
	/**
	 * List of latitude and longitude corresponding to areaVertex. 
	 */
	public LatLong[] polygonLatLon; 
	
	/**
	 * The area of the polygon in pixels. Null if no shape defined.
	 */
	public Double polyAreaPix;
	
	
	/**
	 * The area of the polygon in meters squared. Null if no shape defined.
	 */
	public Double polyAreaM;
	
	
	/**
	 * The perimeter of the polygon in pixels. Null if no shape defined.
	 */
	public Double polyPerimPix;
	
	
	/**
	 * The perimeter of the polygon in meters. Null if no shape defined.
	 */
	public Double polyPerimM;
	
	//single animal measurements. 
	
	/**
	 * Distance to object (animal)- flat along surface, not from camera.
	 */
	public Double locDistance = 0.0;
	
	/**
	 * The estimated distance error.
	 */
	public Double locDistanceError = null;
		
	/**
	 * Bearing of object (animal) degrees.
	 */
	public Double locBearing = null; // should be sum of imageBearing and angleCorrection. 
	
	/**
	 * The error in the bearing to the animal in degrees.
	 */
	public Double locBearingError = null;
	
	/**
	 * Pitch of object (animal) degrees.
	 */
	public Double locPitch = null;
	
	/**
	 * The error in the pitch angle in degrees.
	 */
	public Double locPitchError = null;
	
	/**
	 * Location of the animal. This requires a heading, distance and image origin.
	 */
	public LatLong locLatLong;

	

	////Image Info/////
	/**
	 * Time the image was taken (note this can be null, e.g. for a pasted image).
	 */
	public Long imageTime=null; 
	
	/**
	 * Bearing of image in degrees.
	 */
	public Double imageBearing= null;
	
	/**
	 * image bearing error in degrees.
	 */
	public Double imageBearingErr=0.0;
	
	/**
	 * Image Pitch in degrees.
	 */
	public Double imagePitch= null;
	
	/**
	 * Image pitch error in degrees.
	 */
	public Double imagePitchErr=0.0;
	
	/**
	 * Image tilt in degrees.
	 */
	public Double imageTilt= null;
	/**
	 * Image tilt error in degrees.
	 */
	public Double imageTiltErr= 0.0;
	
	/**
	 * Location the image was taken from.
	 */
	public LatLong imageOrigin;

	 /**
	  * The range difference that one pixel of error makes in meters.
	  */
	public double pixelAccuracy = 0;
	
	//image info
	public String imageName;
	
	//external info
	/**
	 * difference between image bearing and bearing to animal.
	 */
	public double angleCorrection = 0;
	
	
	//other data
	
	/**
	 * User comment on the picture. 
	 */
	public String comment;
	
	/**
	 * Camera calibration data. Used in horizona and IMU methods. 
	 */
	public VRCalibrationData calibrationData;
	
	/**
	 * The height of the camera
	 */
	public VRHeightData heightData;
	
	/**
	 * The method used to calculated range from pitch and height.
	 */
	public VRHorzCalcMethod rangeMethod;

	/**
	 * The overall method used.
	 */
	public VRMethod vrMethod;
	
//	public int measurementType;
	
	/**
	 * Constructor for measurements from the horizon.
	 * @param horizonPoint1 first horizon point
	 * @param horizonPoint2 second horizon point
	 * @param animalPoint clicked animal point. 
	 */
	public VRMeasurement(Point horizonPoint1, Point horizonPoint2, Point animalPoint) {
		super();
		this.animalPoint = animalPoint;
		
		/* 
		 * work out the perpendicular intercept point on the horizon.
		 */
		horizonPoint = new Point();
		
		if (horizonPoint1.x == horizonPoint2.x) {
			// vertical horizon
			horizonPoint.y = animalPoint.y;
			horizonPoint.x = horizonPoint1.x;
		}
		else if (horizonPoint1.y == horizonPoint2.y) {
			// horizontal horizon
			horizonPoint.y = horizonPoint1.y;
			horizonPoint.x = animalPoint.x;
		} 
		else {
			double x1  = horizonPoint1.x - animalPoint.x;
			double y1  = horizonPoint1.y - animalPoint.y;
			double x2  = horizonPoint2.x - animalPoint.x;
			double y2  = horizonPoint2.y - animalPoint.y;
			// slanted horizon
			double T = (y2 - y1) / (x2 - x1);
			horizonPoint.y = (int) ((y1 - x1 * T) / 
			(1 + T * T));
			horizonPoint.x =(int) ((y1 - T * x1) / 
			(-1/T - T));
			horizonPoint.y += animalPoint.y;
			horizonPoint.x += animalPoint.x;
		}
		
		distancePixels = horizonPoint.distance(animalPoint);
	}
	
	/**
	 * Constructor for shore based measurements.
	 * @param shorePoint clicked shore point on image.
	 * @param animalPoint clicked animal point on image.
	 */
	public VRMeasurement(Point shorePoint, Point animalPoint) {
		super();
		this.animalPoint = animalPoint;
		distancePixels = shorePoint.distance(animalPoint);
	}
	
	/**
	 * Constructor for measurments which know the animals bearing, pitch and the roll of the image. 
	 * @param animalPoint
	 * @param pitchPixels
	 * @param bearingPixels
	 */
	public VRMeasurement(Point animalPoint) {
		super();
		this.animalPoint = animalPoint;
	}
	
	public VRMeasurement() {
		super();
	}

	/**
	 * Returns the horizontal angle of the animal in degrees. 
	 * @param vrCalibrationData: calibration data.
	 * @return angle in degrees based on measured distance in pixels and .
	 * given calibration data. 
	 */
	public double getAnimalAngleDegrees(VRCalibrationData vrCalibrationData) {
		double angle = distancePixels * vrCalibrationData.degreesPerUnit;
		return angle;
	}
	
	/**
	 * Returns the horizontal angle of the animal in radians. 
	 * @param vrCalibrationData: calibration data.
	 * @return angle in degrees based on measured distance in pixels and 
	 * given calibration data. 
	 */
	public double getAnimalAngleRadians(VRCalibrationData vrCalibrationData) {
		return getAnimalAngleDegrees(vrCalibrationData) * Math.PI / 180.;
	}
	
	public String getHoverText() {
		String str = String.format("<html>Image %s (%d)<br>Range: %.1f m<br>Bearing: %.1f\u00B0",
				imageName, imageAnimal, locDistance, locBearing);
		if (comment != null) {
			str += "<br>" + comment;
		}
		str += "</html>";
		return str;
	}

	@Override
	public VRMeasurement clone() {
		try {
			return (VRMeasurement) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
