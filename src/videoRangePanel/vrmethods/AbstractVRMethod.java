package videoRangePanel.vrmethods;

import java.awt.Point;
import java.util.ArrayList;

import PamUtils.LatLong;
import PamUtils.PamUtils;
import videoRangePanel.VRCalibrationData;
import videoRangePanel.VRControl;
import videoRangePanel.VRHeightData;
import videoRangePanel.VRHorzCalcMethod;
import videoRangePanel.VRMeasurement;
import videoRangePanel.layoutAWT.AcceptMeasurementDialog;

/**
 * AbstractVRMethod provides a significant amount of the basic functionality required to determine the range of an animal from an image. It also contains generic components, such as calibration lists, generic instructions and image location indicators for making a vrMethod
 * @author Jamie Macaulay
 *
 */
public abstract class AbstractVRMethod implements VRMethod {
	
	/**
	 * Flag to indicate that all information is present to measure an animal 
	 */
	public static final int MEASURE_ANIMAL = 2;
	
	/**
	 * Flag to show that measurment is done. 
	 */
	public static final int MEASURE_DONE = 3;

	
	//Horizon tilt in radians. 
	protected double horizonTilt=0;
	
	//Heading
	protected Double imageHeading;

	//measurements
	protected Point horizonPoint1, horizonPoint2;
	//possible animal measurment
	protected VRMeasurement candidateMeasurement;

	public VRControl vrControl;

	public AbstractVRMethod(VRControl vrControl){
		this.vrControl=vrControl;
	}

	
	/**
	 * Get the lat long from the location mnanager for a given time. 
	 * @param timeMillis
	 * @return the co-ordinates of the camera location at timeMillis
	 */
	public LatLong getGPSinfo(long timeMillis){
		return vrControl.getLocationManager().getLocation(timeMillis);
	}
	
	/**
	 * Get the current image co-ordinates based on the image time stamp. 
	 * @return the co-ordinates the current image was taken from. 
	 */
	public LatLong getGPSinfo(){
		//System.out.println(vrControl.getLocationManager().getLocation(vrControl.getImageTime()));
		return vrControl.getLocationManager().getLocation(vrControl.getImageTime());
	}
	
	@Override
	public void update(int updateType){
		switch (updateType){
		case VRControl.SETTINGS_CHANGE:
		break;
		case VRControl.IMAGE_CHANGE:
		break;
		case VRControl.METHOD_CHANGED:
			break;
		}
		
	}
	



	///Calculations////
	/**
	 * Gets the horizon pixel number when horizon points have beenmarked. 
	 * <p>
	 * Only this one needed, since VRControl immediately works out the horizon
	 * position whenever the shore point is set or the angle changes. 
	 * @param x c coordinate on screeen
	 * @return horizon pixel number
	 */
	protected Double getHorizonPixel_H(int x) {
		Point p1 = horizonPoint1;
		Point p2 = horizonPoint2;
		if (p1 == null) {
			return null;
		}
		if (p2 == null) {
			return (double) p1.y;
		}
		// extrapolate / interpolate to allow for slanty horizon. 
		return p1.y + (double) (p2.y-p1.y) / (double) (p2.x-p1.x) * (x - p1.x);
	}
	
	/**
	 * Get's the Y coordinate of the horizon at a given X.
	 * @param x
	 * @return
	 */
	public Double getHorizonPixel(int x) {
		Double y = null;
		y = getHorizonPixel_H(x);
		if (y == null) {
			/*
			 * Work something out based on the tilt so that the 
			 * shore line gets shown at the top of the image in any case. 
			 */
			double dx = x - (vrControl.getVRPanel().getImageWidth()/2);
			y = -dx * Math.tan(getHorizonTilt() * Math.PI / 180);
		}
		return y;
	}
	
	
	/**
	 * Gets called when a second horizon point is added and then 
	 * calculates the horizon tilt, based on the two points. 
	 *
	 */
	public void calculateHorizonTilt() {
		if (horizonPoint1 == null ||horizonPoint2 == null) {
			return;
		}
		Point l, r;
		if (horizonPoint1.x < horizonPoint2.x) {
			l = horizonPoint1;
			r = horizonPoint2;
		}
		else {
			l = horizonPoint2;
			r = horizonPoint1;
		}
		double tilt = Math.atan2(-(r.y-l.y), r.x-l.x) * 180 / Math.PI;
		setHorizonTilt(tilt);
	}
	
	protected int bearingTox(double centreAngle, double objectBearing) {
		/*
		 * centreAngle should have been taken from the little top panel
		 * and is the bearing at the centre of the image. Work out the
		 * x pixel from the bearing difference. 
		 */
		VRCalibrationData calData = vrControl.getVRParams().getCurrentCalibrationData();
		if (calData == null) return 0;
		double ad = PamUtils.constrainedAngle(objectBearing - centreAngle, 180);
		return vrControl.getVRPanel().getImageWidth() / 2 + (int) (ad / calData.degreesPerUnit); 
	}
	
	/**
	 * Measure an animnal based on two points defining the horizon in a picture. Note this method is not just used in the direct horizon method but any method which can reconstruct the horizon, such as using shore points. 
	 * @param animalPoint
	 * @return
	 */
	public boolean newAnimalMeasurement_Horizon(Point animalPoint) {
		
		if (horizonPoint1 == null || horizonPoint2 == null) {
			return false;
		}
		VRCalibrationData calData = vrControl.getVRParams().getCurrentCalibrationData();
		if (calData == null) {
			return false;
		}
		VRHeightData heightData = vrControl.getVRParams().getCurrentheightData();
		if (heightData == null) {
			return false;
		}
		
		VRHorzCalcMethod vrMethod = vrControl.getRangeMethods().getCurrentMethod();
		if (vrMethod == null) {
			return false;
		}
		
		if (vrControl.getMeasuredAnimals()==null){
			vrControl.setMeasuredAnimals(new ArrayList<VRMeasurement>());
		}
		

		candidateMeasurement = new VRMeasurement(horizonPoint1, horizonPoint2, animalPoint);
		
		candidateMeasurement.vrMethod=vrControl.getCurrentMethod(); 
		candidateMeasurement.imageTime = vrControl.getImageTime();
		candidateMeasurement.imageName = new String(vrControl.getImageName());
		candidateMeasurement.imageAnimal = vrControl.getMeasuredAnimals().size();
		candidateMeasurement.calibrationData = calData.clone();
		candidateMeasurement.heightData = heightData.clone();
		candidateMeasurement.rangeMethod = vrMethod;
		
		// try to get an angle
//		AngleDataUnit heldAngle = null;
//		if (angleDataBlock != null) {
//			heldAngle = angleDataBlock.getHeldAngle();
//		}
//		if (heldAngle != null) {
//			candidateMeasurement.cameraAngle = heldAngle.correctedAngle;
//		}
		/*
		 * Held angles would have been put straight into the imageAngle field. 
		 * Even if they haven't, it's possible that someone will have entered a
		 * value manually into the field, so try using it. 
		 */
		candidateMeasurement.imageBearing = getImageHeading();
		
		double angle = candidateMeasurement.getAnimalAngleRadians(calData);
		double range = vrMethod.getRange(vrControl.getCurrentHeight(), angle);
		candidateMeasurement.locDistance = range;
		// see how accurate the measurement might be based on a single pixel error
		double range1 = vrMethod.getRange(vrControl.getCurrentHeight(), angle + Math.PI/180*calData.degreesPerUnit);
		double range2 = vrMethod.getRange(vrControl.getCurrentHeight(), angle - Math.PI/180*calData.degreesPerUnit);
		double error = Math.abs(range1-range2)/2;
		candidateMeasurement.pixelAccuracy = error;
		
		// calculate the angle correction
		int imageWidth = vrControl.getVRPanel().getImageWidth();
		double angCorr = (animalPoint.x - imageWidth/2) * calData.degreesPerUnit;
		candidateMeasurement.angleCorrection = angCorr;
				
		VRMeasurement newMeasurement = AcceptMeasurementDialog.showDialog(null, vrControl, candidateMeasurement);
		if (newMeasurement != null) {
//			System.out.println("new measurment!");
			vrControl.getMeasuredAnimals().add(newMeasurement);
			vrControl.getVRProcess().newVRLoc(newMeasurement);
		}
		candidateMeasurement = null;
				
		return true;
	}
	

	
	protected void clearPoints() {
		horizonPoint1 = horizonPoint2 =null;
		vrControl.setMeasuredAnimals(null);
		candidateMeasurement = null;
	}
	
	public VRMeasurement getCandidateMeasurement() {
		return candidateMeasurement;
	}
	
	public double getHorizonTilt() {
		return horizonTilt;
	}

	public void setHorizonTilt(double horizonTilt) {
		this.horizonTilt = horizonTilt;
	}
	
	public Double getImageHeading() {
		return imageHeading;
	}
	
	public Double getImagePitch() {
		return imageHeading;
	}
	
	
	/**
	 * Calculates the lat long of an animal based on heading, range and the image origin. 
	 * @param vrMeasurment- the lat long is added to a the imageLatLong field in the measurment. 
	 */
	public void calcLocLatLong(VRMeasurement vrMeasurment){
		//need three components to calculate a latLong for the animal
		if (vrMeasurment.locBearing==null || vrMeasurment.locDistance==null || vrMeasurment.imageOrigin==null) return;
//		System.out.println(String.format("Calculating the lat long location: Range: %.3f Bearing: %.3f from ", 
//				vrMeasurment.locDistance, vrMeasurment.locBearing) + vrMeasurment.imageOrigin.toString());
		vrMeasurment.locLatLong=vrMeasurment.imageOrigin.travelDistanceMeters(vrMeasurment.locBearing, vrMeasurment.locDistance);
	}
	
}
