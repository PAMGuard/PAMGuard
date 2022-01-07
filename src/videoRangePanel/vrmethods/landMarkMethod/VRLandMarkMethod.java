package videoRangePanel.vrmethods.landMarkMethod;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;

import videoRangePanel.VRControl;
import videoRangePanel.VRHeightData;
import videoRangePanel.VRMeasurement;
import videoRangePanel.layoutAWT.AcceptMeasurementDialog;
import videoRangePanel.vrmethods.AbstractVRMethod;
import videoRangePanel.vrmethods.VROverlayAWT;
import videoRangePanel.vrmethods.VROverlayFX;
import PamUtils.LatLong;
import PamUtils.PamUtils;
import PamUtils.PamArrayUtils;
import PamUtils.PamCalendar;
import PamView.panel.PamPanel;
import PamguardMVC.debug.Debug;
import net.sf.geographiclib.PolygonResult;

/**
 * The LandMark uses landmarks at known positions or at known angles from the
 * position the image was taken to calculate an animal's position. Two
 * Landmark's essentially give you the four out of the five vital bits of
 * information required to get a true location (on the sea surface). Image
 * bearing, pitch and tilt and degrees per pixel. The fifth bit of information ,
 * the height from which the image was taken, is defined by the user.
 * <p>
 * Angular convention for this module and method.
 * <p>
 * Bearing- 0==north, 90==east 180=south, 270==west
 * <p>
 * Pitch- 90=-g, 0=0g, -90=g
 * <p>
 * Tilt 0->180 -camera turning towards left to upside down 0->-180 camera
 * turning right to upside down
 * <p>
 * 
 * @author Jamie Macaulay
 */
public class VRLandMarkMethod extends AbstractVRMethod {

	public final static int SET_LANDMARK = -100;

	public final static int SET_LANDMARK_READY = -101;

	/**
	 * New image orientation measurement.
	 */
	public static final int NEW_IMAGE_BEARING_CAL = -102;

	/**
	 * New measurement has been made.
	 */
	public static final int NEW_MESURMENT = -103;

	private VRControl vrControl;

	private int currentStatus = SET_LANDMARK;

	private ArrayList<LandMark> setLandMarks;

	/**
	 * Corresponds to ArrayList<LandMark> and contains with each point the location
	 * of the landmark on the image
	 */
	private ArrayList<Point> landMarkPoints;

	// orientation values
	/**
	 * The mean tilt from landmark measurements.
	 */
	private Double tilt;
	/**
	 * The tilt values from all combinations of landmarks
	 */
	private ArrayList<Double> tiltVals;

	/**
	 * Bearing values for animal based on calculations from each landmark in
	 * radians. 0= north, pi=south, pi/2=east. Ideally all values in array should be
	 * the same.
	 */
	private ArrayList<Double> animalBearingVals;

	/**
	 * Pitch values for animal based on calculations from each landmark in radians.
	 * Ideally should be the same. -pi degrees =downwards, +pi degrees. Ideally all
	 * values in array should be the same.
	 */
	private ArrayList<Double> animalPitchVals;

	// calibration values;
	/**
	 * mean calibration value
	 */
	private Double calMean;
	/**
	 * Calibration values (pixels per degree) for each combination of landmarks
	 */
	private ArrayList<Double> calValues;

	/**
	 * The landmark GUI wrtten in AWT.
	 */
	private LandMarkGUIAWT landMarkGUIAWT;

	/**
	 * The landmark GUI in JavaFX
	 */
	private LandMarkGUIFX landMarkGUIFX;

	/**
	 * VRLandMark method constructor
	 * 
	 * @param vrControl
	 *            - reference to the vrControl.
	 */
	public VRLandMarkMethod(VRControl vrControl) {
		super(vrControl);
		this.vrControl = vrControl;
		// this.sidePanel=createSidePanel();
		// this.landMarkUI=new LandMarkMethodUI(vrControl);
	}

	/**
	 * Create the correct GUI. Either the newer JavaFX GUI or the older Swing based
	 * GUI.
	 */
	private void createGUI() {
		// create the GUI. No need to create both as FX will only be
		// called with an FX GUI and vice versa.
		if (vrControl.isFX() && landMarkGUIFX == null) {
			this.landMarkGUIFX = new LandMarkGUIFX(this);
		} else if (!vrControl.isFX() && landMarkGUIAWT == null) {
			this.landMarkGUIAWT = new LandMarkGUIAWT(this);
		}
	}

	@Override
	public String getName() {
		return "Landmark Method";

	}

	/**
	 * Get the currently selected landmark group. This contains a list of the
	 * landmarks which can be set on the image
	 * 
	 * @return the current landmark group.
	 */
	public LandMarkGroup getSelectedLMGroup() {
		if (vrControl.getVRParams().getLandMarkDatas() == null)
			return null;
		if (vrControl.getVRParams().getLandMarkDatas().size() == 0)
			return null;
		return vrControl.getVRParams().getLandMarkDatas().get(vrControl.getVRParams().getSelectedLandMarkGroup());
	}

	/**
	 * Calculate the image bearing, pitch and tilt.
	 * 
	 * @param measurement
	 *            - the measurement data unit.
	 * @return the updated measurment data unit.
	 */
	public VRMeasurement setImagePosVals(VRMeasurement measurement) {
		
		if (tiltVals==null) return null;
		
		//calculate the middle of the image
		Debug.out.println("The current Image: " +vrControl.getCurrentImage());
		Point imageMiddle=new Point(vrControl.getVRPanel().getImageWidth()/2
			,vrControl.getVRPanel().getImageHeight()/2);
		//work out image tilt
		double averageTilt=PamArrayUtils.mean(tiltVals, 0);
		double stdTilt=PamArrayUtils.std(tiltVals, 0);
		//work out image bearing
		ArrayList<Double> vals=calcBearingLoc(imageMiddle, averageTilt);
		double averageBearing=PamArrayUtils.mean(vals, 0);
		double stdBearing=PamArrayUtils.std(vals, 0);
		//work out image pitch
		vals=calcPitchLoc(imageMiddle, averageTilt);
		double averagePitch=PamArrayUtils.mean(vals, 0);
		double stdPitch=PamArrayUtils.std(vals, 0);
	
		//TODO-units?
		measurement.imageBearing=Math.toDegrees(averageBearing);
		measurement.imageBearingErr=Math.toDegrees(stdBearing);
		measurement.imagePitch=Math.toDegrees(averagePitch);
		measurement.imagePitchErr=Math.toDegrees(stdPitch);
		measurement.imageTilt=Math.toDegrees(averageTilt);
		measurement.imageTiltErr=Math.toDegrees(stdTilt);

		return measurement;

	}

	// /**
	// * JLayer overlay panel for the land mark method. Handles most mouse events.
	// * @author Jamie Macaulay
	// *
	// */
	// @SuppressWarnings("serial")
	// private class LandMarkMethodUI extends VRAbstractLayerUI {
	//
	// private VRControl vrControl;
	// JPopupMenu menu;
	//
	// public LandMarkMethodUI(VRControl vRControl){
	// super(vRControl);
	// this.vrControl=vRControl;
	// }
	//
	// @Override
	// public void paint(Graphics g, JComponent c) {
	// super.paint(g, c);
	// addMeasurementMarks(g);
	// addAnimals(g);
	// }
	//
	// @SuppressWarnings("rawtypes")
	// @Override
	// protected void processMouseEvent(MouseEvent e, JLayer l) {
	//
	// boolean vis=isMenuVisible();
	//
	// if (e.isPopupTrigger()){
	// menu=showlmPopUp(e.getPoint());
	// menu.show(l, e.getPoint().x, e.getPoint().y);
	// }
	// //if there are enough landmark points and popup menu is not visible then a
	// double click will measure animal location.
	// else if (e.getClickCount()>1 && e.getButton()==MouseEvent.BUTTON1 && !vis){
	// if (isCalcReady()) measureAnimal(e.getPoint());
	// }
	// vrControl.getVRPanel().repaint();
	// }
	//
	// private boolean isMenuVisible(){
	// if (menu==null) return false;
	// return menu.isVisible();
	// }
	//
	// }

	/**
	 * Make sure an angle falls between zero and 2pi(360 degrees)
	 * 
	 * @params t- angle in radians;
	 * @return angle between 0 and 360;
	 */
	public static double wrap360(double t) {
		if (t < 0)
			t = (2 * 1000 * Math.PI) + t; // if negative to get into correct form. Make big incase we have a huge
											// negative angle.
		t = t % (2 * Math.PI);
		return t;
	}
	
	/**
	 * Convert points to a double array of x and y. 
	 * @return array of equal length to points with each elements a two element double[]. 
	 */
	private double[][] points2Array(Point[] points) {
		double[][] dblpoints=new double[points.length][2]; 
		for (int i=0; i<points.length; i++) {
			dblpoints[i]=new double[] {points[i].getX(), points[i].getY()};
		}
		return dblpoints; 
	}
	
	/**
	 * Measure polygon points instead of a single animal location
	 * @param polygonPoints - list of polygon points. 
	 */
	protected void measurePolygon(Point[] polygonPoints) {
		
		//first do the basics. Measure the x and y co-ordinates. 

		
		//the animal measurment is the center of the polygon.
		double[] point= PamUtils.centroid(points2Array(polygonPoints)); 
		Point imPoint=new Point((int) point[0],(int) point[1]);
		
		//create a new animal measurements
		VRMeasurement possibleMeasurement = new VRMeasurement(imPoint);
		
		//might be able to geo-reference the polygon. 
		if (this.isCalcReady()) {
			//calculate where all the landmarks are and therefore image tilt
			calcVals();
			//apply image bearing, pitch and tilt measurements, 
			possibleMeasurement=setImagePosVals(possibleMeasurement); 

			//calculate the 
			animalBearingVals = calcBearingLoc(imPoint, tilt);
			animalPitchVals = calcPitchLoc(imPoint, tilt);
			possibleMeasurement=newAnimalMeasurement_LandMark(imPoint, possibleMeasurement);
			
			//get measurements for the polygon
			VRMeasurement polyMeasurement;
			LatLong[] latLongPoly=new LatLong[polygonPoints.length];
			for (int i=0; i<polygonPoints.length; i++) {
				imPoint=new Point(polygonPoints[i]); 
				polyMeasurement=  new VRMeasurement(imPoint);
				//apply image bearing, pitch and tilt measurments.
				//slightly inefficient but have basically recalc everything. 
				polyMeasurement=setImagePosVals(polyMeasurement); 
				animalBearingVals = calcBearingLoc(imPoint, tilt);
				animalPitchVals = calcPitchLoc(imPoint, tilt);
				polyMeasurement=newAnimalMeasurement_LandMark(imPoint, polyMeasurement);
				
//				System.out.println("XXXXXXX");
//				System.out.println("Polymeasurment: X: " + polygonPoints[i].getX() +"  Y: "+ polygonPoints[i].getY() + 
//						" lat: " + polyMeasurement.locLatLong.getLatitude()+ " long: " +  polyMeasurement.locLatLong.getLongitude());
//				System.out.println("XXXXXXX");

				latLongPoly[i]=new LatLong(polyMeasurement.locLatLong); 
			}
			possibleMeasurement.polygonLatLon=latLongPoly;
			PolygonResult polyLatLong=LatLong.latLongArea(latLongPoly);
			possibleMeasurement.polyAreaM=polyLatLong.area;
			possibleMeasurement.polyPerimM=polyLatLong.perimeter;
		}
		else {
			//if no info still should have image time and name. 
			possibleMeasurement.imageTime = vrControl.getImageTime();
			Debug.out.println("VRLandMarkMethod:  time: " +  PamCalendar.formatDateTime(possibleMeasurement.imageTime));
			possibleMeasurement.imageName = new String(vrControl.getImageName());
		}
		
		//add pixel measurments. 
		possibleMeasurement.polygonArea=Arrays.copyOf(polygonPoints, polygonPoints.length); //need to copy this. 
		
		double[][] pointsA=points2Array(polygonPoints); 
		possibleMeasurement.polyAreaPix=PamUtils.area(pointsA); 
		
		//now show confirm dialog. 
		candidateMeasurement=possibleMeasurement; 
		
		VRMeasurement newMeasurement = null;
		newMeasurement = AcceptMeasurementDialog.showDialog(null, vrControl, candidateMeasurement);
		
		if (newMeasurement != null) {
			if (vrControl.getMeasuredAnimals()==null){
				vrControl.setMeasuredAnimals(new ArrayList<VRMeasurement>());
			}
			vrControl.getMeasuredAnimals().add(newMeasurement);
			vrControl.getVRProcess().newVRLoc(newMeasurement);
		} else {
			System.err.println("VRLandMarkMethod: the measurment was null");
			candidateMeasurement = null;
			return;
		}
	}

	/**
	 * Measure the location of an animal for a certain location in the image.
	 * 
	 * @param point
	 *            - the location of the animal in pixels on the screen
	 */
	public void measureAnimal(Point point) {
		currentStatus = MEASURE_ANIMAL;
		calcVals();

		Point imPoint = vrControl.getVRPanel().screenToImage(point);
		//calculate a list of possible bearing and pictch values
		//based on all combination of landmarks. 
		animalBearingVals = calcBearingLoc(imPoint, tilt);
		animalPitchVals = calcPitchLoc(imPoint, tilt);

		VRMeasurement possibleMeasurement = new VRMeasurement(imPoint);

		// add image position labels
		setImagePosVals(possibleMeasurement);

		// paint the photo with possible meaurements
		candidateMeasurement = possibleMeasurement;
		vrControl.getVRPanel().repaint();

		// calculate the loc values and show the measurement dialog.
		candidateMeasurement=newAnimalMeasurement_LandMark(imPoint, possibleMeasurement);
		
		// show a dialog with the new measurment
		VRMeasurement newMeasurement = AcceptMeasurementDialog.showDialog(null, vrControl, candidateMeasurement);

		if (newMeasurement != null) {
			vrControl.getMeasuredAnimals().add(newMeasurement);
			vrControl.getVRProcess().newVRLoc(newMeasurement);
			// add animal labels
			if (vrControl.getMeasuredAnimals() != null) {
				if (vrControl.getMeasuredAnimals().size() > 0)
					update(NEW_MESURMENT);
				// this.landMarkGUIAWT.setAnimalLabels(vrControl.getMeasuredAnimals()
				// .get(vrControl.getMeasuredAnimals().size()-1));
			}
		}
		candidateMeasurement = null;
		vrControl.getVRPanel().repaint();
	}

	/**
	 * Calculate the mean pixels per degree and the tilt of the mean tilt of the
	 * image based on the set landmarks. Note there must be at least two landmarks
	 * but more is preferable.
	 */
	public void calcVals() {

		ArrayList<Integer> indexM1 = PamUtils.indexM1(landMarkPoints.size());
		ArrayList<Integer> indexM2 = PamUtils.indexM2(landMarkPoints.size());

		// tilt values
		tiltVals = new ArrayList<Double>();
		double tiltMean = 0;
		double tiltVal;
		// calibration value
		double bearingDiff;
		double pitchDiff;
		double pixelPerDegree;
		double pPDMean = 0;
		calValues = new ArrayList<Double>();

		// don't include tide data here.
		for (int i = 0; i < indexM1.size(); i++) {

			tiltVal = calcTilt(landMarkPoints.get(indexM1.get(i)), landMarkPoints.get(indexM2.get(i)),
					setLandMarks.get(indexM1.get(i)), setLandMarks.get(indexM2.get(i)), getImagePos(false));
			tiltVals.add(tiltVal);
			tiltMean += tiltVal;

			bearingDiff = calcBearingDiff(setLandMarks.get(indexM1.get(i)), setLandMarks.get(indexM2.get(i)),
					getImagePos(false));
			pitchDiff = calcPitchDiff(setLandMarks.get(indexM1.get(i)), setLandMarks.get(indexM2.get(i)),
					getImagePos(false));
			pixelPerDegree = calcPixelsperDegree(
					landMarkPoints.get(indexM1.get(i)).distance(landMarkPoints.get(indexM2.get(i))), bearingDiff,
					pitchDiff);
			calValues.add(pixelPerDegree);
			pPDMean += pixelPerDegree;

		}

		this.calMean = pPDMean / (double) indexM1.size();
		this.tilt = tiltMean / (double) indexM1.size();

	}

	/**
	 * Calculate the bearing of the animal from the tilt and calibration information
	 * from currently set landmarks.
	 * 
	 * @param animalPoint-point
	 *            on full sized image
	 * @param tilt-
	 *            tilt of image in radians
	 * @return ArrayList of bearing values calculated form each currently set
	 *         landmark
	 */
	private ArrayList<Double> calcBearingLoc(Point anP, Double tilt) {
		ArrayList<Double> bearingVals = new ArrayList<Double>();
		double bearingVal;
		for (int i = 0; i < setLandMarks.size(); i++) {
			bearingVal = wrap360(calcBearing(setLandMarks.get(i), getImagePos(true))
					+ calcAnimalBearing(tilt, landMarkPoints.get(i), anP, calMean));
			// System.out.println("landmark bearing: original: "
			// +setLandMarks.get(i).getName() +" "+
			// Math.toDegrees(calcBearing(setLandMarks.get(i), getImagePos())) + " animal
			// from landmark: "+Math.toDegrees(calcAnimalBearing(tilt,landMarkPoints.get(i),
			// anP, calMean)) +" wrap360 bearingVal: "+Math.toDegrees(bearingVal));
			bearingVals.add(bearingVal);
		}
		return bearingVals;
	}

	/**
	 * Calculate the pitch of the animal from the tilt and calibration information
	 * from currently set landmarks.
	 * 
	 * @param anP-
	 *            point of the animal
	 * @param tilt-
	 *            tilt of image in radians
	 * @return ArrayList of pitch values calculated form each currently set landmark
	 */
	private ArrayList<Double> calcPitchLoc(Point anP, Double tilt) {
		ArrayList<Double> pitchVals = new ArrayList<Double>();
		for (int i = 0; i < setLandMarks.size(); i++) {
			pitchVals.add(calcPitch(setLandMarks.get(i), getImagePos(true))
					- calcAnimalPitch(tilt, landMarkPoints.get(i), anP, calMean));
		}
		return pitchVals;
	}

	/**
	 * Check whether a landmark is in the current list of landmarks whihc have been
	 * set.
	 * 
	 * @param landMark
	 *            - the landmakr to check
	 * @return true if in the list.
	 */
	public boolean isInList(LandMark landMark) {
		if (setLandMarks == null)
			return false;
		return setLandMarks.contains(landMark);
	}

	/**
	 * Set a landmark in the set landmarks list.
	 * 
	 * @param landMark
	 *            - the landmark on the screen.
	 * @param point
	 *            - the location of the landmark on the image.
	 */
	public void setLandMark(LandMark landMark, Point point) {
		// System.out.println("SET LANDMARK");
		if (landMarkPoints == null || setLandMarks == null) {
			landMarkPoints = new ArrayList<Point>();
			setLandMarks = new ArrayList<LandMark>();
		}

		Point landMarkPoint = vrControl.getVRPanel().screenToImage(point);
		Point pointclone = new Point();
		pointclone.setLocation(landMarkPoint.getX(), landMarkPoint.getY());
		// System.out.println(pointclone);
		landMarkPoints.add(landMarkPoint);
		setLandMarks.add(landMark);
		if (setLandMarks.size() >= 2)
			currentStatus = SET_LANDMARK_READY;
	}

	/**
	 * Remove a landmark from the landmark list.
	 * 
	 * @param landMark
	 *            - the landmark to remove.
	 */
	public void removeLandMark(LandMark landMark) {
		// System.out.println("SET LANDMARK");
		if (landMarkPoints == null || setLandMarks == null) {
			landMarkPoints = new ArrayList<Point>();
			setLandMarks = new ArrayList<LandMark>();
		}

		landMarkPoints.remove(setLandMarks.indexOf(landMark));
		setLandMarks.remove(landMark);
	}

	/**
	 * Check whether a landmark based calculation is ready. If there are two
	 * landmarks then the method is ready for an animal measurement.
	 * 
	 * @return true if ready for measurement;
	 */
	public boolean isCalcReady() {
		if (setLandMarks == null)
			return false;
		if (setLandMarks.size() >= 2)
			return true;
		return false;
	}

	public PamPanel getRibbonPanel() {
		return this.landMarkGUIAWT.getRibbonPanel();
	}

	public PamPanel getSettingsPanel() {
		return null;
	}

	@Override
	public void clearOverlay() {
		clearLandMarks();
		this.landMarkGUIAWT.clearOverlay();
	}

	public void clearLandMarks() {
		// clear animal measurements in abstract class
		super.clearPoints();
		// clear landmark measurements
		setLandMarks = null;
		landMarkPoints = null;
		currentStatus = SET_LANDMARK;
	}

	@Override
	public void update(int updateType) {
		super.update(updateType);
		if (landMarkGUIAWT != null)
			landMarkGUIAWT.update(updateType);
		if (landMarkGUIFX != null)
			landMarkGUIFX.update(updateType);

	}

	/**
	 * Print the current values.
	 */
	public void printValues() {
		System.out.println("-----------------------------------------");
		System.out.println("The data used in landmark measurments is:");
		double tideOffset = candidateMeasurement.heightData.height - getImageHeight(false).height;
		System.out.println("Observer height: " + candidateMeasurement.heightData + " tide offset: "
				+ String.format("%.2f", vrControl.getTideManager().getHeightOffset()) + "m");
		System.out.println("Observer position: " + candidateMeasurement.imageOrigin.toString());
		System.out.println("Landmarks used: ");
		for (int i = 0; i < setLandMarks.size(); i++) {
			System.out.println("");
			System.out.print(setLandMarks.get(i).getName());
			if (setLandMarks.get(i).getPosition() != null) {
				System.out.print("  Position: " + setLandMarks.get(i).getPosition().toString());
				System.out.print(" " + setLandMarks.get(i).getHeight() + " (no tide offset is used)");
				System.out.println("The distance from the observer to the landmark is "
						+ setLandMarks.get(i).getPosition().distanceToMetres(candidateMeasurement.imageOrigin));

			} else {
				System.out.print(" Bearing to: " + String.format("%.4f", setLandMarks.get(i).getBearing())
						+ " Pitch to: " + String.format("%.4f", setLandMarks.get(i).getPitch()));
				System.out.print(" Origin height " + setLandMarks.get(i).getHeightOrigin());
			}
		}
	}
	

	/**
	 * Calculate the measurment and errors for a new LandMark based animal
	 * measurment. The distance and bearing calculations are perfromed here.
	 * 
	 * @param animalPoint
	 *            - the animal point on the screen
	 * @param possibleMeasurement
	 *            - the measurement class to populate with values.
	 * @return  the VRMeasurment. 
	 */
	protected VRMeasurement newAnimalMeasurement_LandMark(Point animalPoint, VRMeasurement possibleMeasurement) {

		if (animalPoint==null) return null;
		
		//calculate pitch and bearing values to animal
		double averageBearing=PamArrayUtils.mean(animalBearingVals, 0);
		double averagePitch=PamArrayUtils.mean(animalPitchVals, 0);
		double stdBearing=PamArrayUtils.std(animalBearingVals, 0);
		double stdPitch=PamArrayUtils.std(animalPitchVals, 0);
			
		candidateMeasurement=possibleMeasurement;
		
		if (vrControl.getMeasuredAnimals()==null){
			vrControl.setMeasuredAnimals(new ArrayList<VRMeasurement>());
		}
		candidateMeasurement.vrMethod = this;

		candidateMeasurement.imageTime = vrControl.getImageTime();
		candidateMeasurement.imageName = new String(vrControl.getImageName());
		candidateMeasurement.imageAnimal = vrControl.getMeasuredAnimals().size();
		candidateMeasurement.heightData = getImageHeight(true);
		candidateMeasurement.rangeMethod = vrControl.getRangeMethods().getCurrentMethod();

		/************************/
		System.out.println("-----------------------------------");
		System.out.println("Measurements:");
		System.out.println(String.format("The average pitch to animal is: %.3f degrees",Math.toDegrees(averagePitch)));
		System.out.println(String.format("The average bearing to animal is: %.3f degrees", Math.toDegrees(averageBearing)));
		System.out.println(String.format("The image height is: %.3f m (including tide offset)", getImageHeight(true).height));
		System.out.println("The range method is: " + vrControl.getRangeMethods().getCurrentMethod().toString());
		System.out.println("-----------------------------------");
		/***********************/

		// set location info. The
		candidateMeasurement.locDistance = vrControl.getRangeMethods().getCurrentMethod()
				.getRange(getImageHeight(true).height, -averagePitch);
		// candidateMeasurement.locDistance=vrControl.getRangeMethods().getCurrentMethod().rangeFromPsi(getImageHeight(true).height,
		// averagePitch+Math.PI/2);
		
		System.out.println("Average bearing: " + averageBearing +  "  " +candidateMeasurement.imageBearing);

		candidateMeasurement.locPitch = Math.toDegrees(averagePitch);
		candidateMeasurement.angleCorrection = Math.toDegrees(averageBearing) - candidateMeasurement.imageBearing;
		candidateMeasurement.locBearing = candidateMeasurement.imageBearing + candidateMeasurement.angleCorrection;

		// calc location errors
		double range1Er = candidateMeasurement.locDistance - Math.abs(vrControl.getRangeMethods().getCurrentMethod()
				.getRange(getImageHeight(true).height, -(averagePitch + stdPitch)));
		double range2Er = candidateMeasurement.locDistance - Math.abs(vrControl.getRangeMethods().getCurrentMethod()
				.getRange(getImageHeight(true).height, -(averagePitch - stdPitch)));
		candidateMeasurement.locDistanceError = Math.abs(range1Er - range2Er) / 2;

		// calc pixel errors
		double range1PxLEr = vrControl.getRangeMethods().getCurrentMethod().getRange(getImageHeight(true).height,
				-(averagePitch + (Math.PI / 180) / calMean));
		double range2PxLEr = vrControl.getRangeMethods().getCurrentMethod().getRange(getImageHeight(true).height,
				-(averagePitch - (Math.PI / 180) / calMean));
		double pxlError = Math.abs(range1PxLEr - range2PxLEr) / 2;
		// add pixel error and pitch error
		candidateMeasurement.pixelAccuracy = pxlError;
		candidateMeasurement.locDistanceError = Math
				.sqrt(Math.pow(pxlError, 2) + Math.pow(candidateMeasurement.locDistanceError, 2));
		// add in bearing errors
		candidateMeasurement.locBearingError = Math.toDegrees(stdBearing);
		candidateMeasurement.locPitchError = Math.toDegrees(stdPitch);

		// work out location of animal (LatLong)
		candidateMeasurement.imageOrigin = getImagePos(true);
		calcLocLatLong(candidateMeasurement);

		// print values
		printValues();

		return candidateMeasurement;
	}

	////////////// Calculations/////////////
	/**
	 * Calculates the bearing of the target animal based on a landmark point, the
	 * animal location and a calibration value;
	 * 
	 * @param tilt-
	 *            tilt of the image in radians.
	 * @param landmrkp1-landmark
	 *            point.
	 * @param animal-animal
	 *            point.
	 * @param calValue-
	 *            calibration value in pixels per degree
	 * @return bearing relative to the landmark - radians.
	 */
	public static double calcAnimalBearing(double tilt, Point lndmrkPoint, Point animalPoint, double calValue) {
		double bearing = calcBearingDiffAnimal(tilt, lndmrkPoint, animalPoint);
		System.out.println("Bearing pixels: " + bearing + " Bearing degrees: " + (bearing / calValue));
		bearing = Math.toRadians(bearing / calValue);
		return bearing;
	}

	/**
	 * Calculates the pitch of the target animal based on a landmark point, the
	 * animal location and a calibration value;
	 * 
	 * @param tilt-
	 *            tilt of the image in radians.
	 * @param landmrkp1-a
	 *            point on the image for which we know the pitch and bearing.
	 * @param animal-animal
	 *            point.
	 * @param calValue-
	 *            calibration value in pixels per degree.
	 * @return pitch relative to the landmark .radians.
	 */
	public static double calcAnimalPitch(double tilt, Point lndmrkPoint, Point animalPoint, double calValue) {
		double pitch = calcPitchDiffAnimal(tilt, lndmrkPoint, animalPoint);
		pitch = Math.toRadians(pitch / calValue);
		return pitch;
	}

	/**
	 * Calculates the bearing of the animal in pixels
	 * 
	 * @param tilt-
	 *            tilt of the image in radians.
	 * @param landmrkp1-a
	 *            point on the image for which we know the pitch and bearing.
	 * @param animal-animal
	 *            point.
	 * @return bearing in pixels.
	 */
	public static double calcBearingDiffAnimal(double tilt, Point landmrkp1, Point animal) {

		double dist = landmrkp1.distance(animal);

		double angr = Math.atan2((animal.y - landmrkp1.y), (animal.x - landmrkp1.x));

		double adj = Math.cos(angr + tilt) * dist;
		// System.out.println("Animal bearing diff: "+adj);
		return adj;

	}

	/**
	 * Calculates the pitch of the animal in pixels
	 * 
	 * @param tilt-
	 *            tilt of the image in radians.
	 * @param landmrkp1-a
	 *            point on the image for which we know the pitch and bearing.
	 * @param animal-animal
	 *            point.
	 * @return bearing in pixels.
	 */
	public static double calcPitchDiffAnimal(double tilt, Point landmrkp1, Point animal) {

		double dist = landmrkp1.distance(animal);

		double angr = Math.atan2((animal.y - landmrkp1.y), (animal.x - landmrkp1.x));

		double opp = Math.sin(angr + tilt) * dist;
		// System.out.println("Animal pitch diff: "+opp);
		return opp;

	}

	/**
	 * Calculates location of the perpendicular vertex of the triangle defined by
	 * landmarks 1 and 2, with the side of the triangle following the tilt of the
	 * picture.
	 * 
	 * @param tilt-tilt
	 *            of image
	 * @param landmrkP1-landmark
	 *            1 point on image
	 * @param landmrkP2-landmark
	 *            2 point on image
	 * @param landmrk1-landmark
	 *            1
	 * @param landmrk2-landmark
	 *            2
	 * @param imagePos-the
	 *            from which the image was taken from. Note that this can be null if
	 *            the landmarks are defined by bearings rathar than GPS
	 *            co-ordinates.
	 * @return the perpindicular vertex of a tringle where the other two vertex have
	 *         been defined by landmark points.
	 */
	public Point calcPerpPoint(double tilt, Point landmrkP1, Point landmrkP2, LandMark landmrk1, LandMark landmrk2,
			LatLong imagePos) {
		double bearingDiff = calcBearingDiff(landmrk1, landmrk2, getImagePos(true));
		double pitchDiff = calcPitchDiff(landmrk1, landmrk2, getImagePos(true));
		double pixelPerDegree = calcPixelsperDegree(landmrkP1.distance(landmrkP2), bearingDiff, pitchDiff);
		Point perpPoint = calcPerpPoint(tilt, pixelPerDegree * Math.toDegrees(bearingDiff), landmrkP2);
		return perpPoint;
	}

	/**
	 * Calculates the point at which the perpendicular corner of the pitch/bearing
	 * triangle resides.
	 * 
	 * @param tilt-tilt
	 *            in radians
	 * @param bearingDiff-difference
	 *            in bearing angle, in pixels.
	 */
	public static Point calcPerpPoint(double tilt, double bearingDiff, Point landMark2) {

		int x = (int) (Math.cos(tilt) * bearingDiff);
		int y = (int) (Math.sin(tilt) * bearingDiff);

		Point perpindicularLoc = new Point();
		perpindicularLoc.x = landMark2.x - x;
		perpindicularLoc.y = landMark2.y + y;

		return perpindicularLoc;

	}

	public static double calcTilt(Point landmrkP1, Point landmrkP2, LandMark landmrk1, LandMark landmrk2,
			LatLong imagePos) {
		double tilt = calcTilt(landmrkP1, landmrkP2, calcBearingDiff(landmrk1, landmrk2, imagePos),
				calcPitchDiff(landmrk1, landmrk2, imagePos));
		return tilt;
	}

	/**
	 * Calculate the pixels per degree.
	 * 
	 * @param distPix-distance
	 *            between two points in pixels
	 * @param bearingDiff-bearing
	 *            difference-radians
	 * @param pitchdiff-pitch
	 *            difference-radians
	 * @return pixels per degree.
	 */
	private static double calcPixelsperDegree(double distPix, double bearingDiff, double pitchdiff) {
		double sizeDistRad = Math.sqrt(Math.pow(bearingDiff, 2) + Math.pow(pitchdiff, 2));
		double sizeDistDeg = Math.toDegrees(sizeDistRad);
		double pixelsPerDeg = distPix / sizeDistDeg;
		return pixelsPerDeg;
	}

	/**
	 * Calculate the pitch difference between two landmarks for an image taken at a
	 * certain location
	 * 
	 * @param landmrk1-
	 *            Landmark 1
	 * @param landmrk2-
	 *            Landmark 2
	 * @param imagePos-
	 *            position of the image, including lat, long, height.
	 * @return the bearing difference in radians.
	 */
	private static double calcBearingDiff(LandMark landmrk1, LandMark landmrk2, LatLong imagePos) {
		double bearing1 = calcBearing(landmrk1, imagePos);
		double bearing2 = calcBearing(landmrk2, imagePos);
		// System.out.println("Bearing1: "+bearing1+" Bearing2: "+bearing2+ "
		// bearingDiff: "+Math.toDegrees(calcBearingDiff(bearing1, bearing2)));
		return calcBearingDiff(bearing1, bearing2);
	}

	/**
	 * We want the bearing difference between two points on a screen. That's the
	 * minimum difference between points either way round the circle.
	 * 
	 * @param bearing1-
	 *            bearing 1 in radians
	 * @param bearing2-brearing
	 *            2 in radians
	 */
	private static double calcBearingDiff(double bearing1, double bearing2) {
		double bearingdiff = bearing2 - bearing1;
		if (Math.abs(bearingdiff) > Math.PI) {
			if (bearingdiff < 0)
				return Math.PI * 2 + bearingdiff;
			else
				return bearingdiff - Math.PI * 2;
		} else
			return bearingdiff;
	}

	/**
	 * Calculate the pitch difference between two landmarks for an image taken at a
	 * certain location
	 * 
	 * @param landmrk1-
	 *            Landmark 1
	 * @param landmrk2-
	 *            Landmark 2
	 * @param imagePos-
	 *            position of the image, including lat, long, height.
	 * @return the pitch difference in radians.
	 */
	private static double calcPitchDiff(LandMark landmrk1, LandMark landmrk2, LatLong imagePos) {
		double pitch1 = calcPitch(landmrk1, imagePos);
		double pitch2 = calcPitch(landmrk2, imagePos);
		// System.out.println("Pitch1: "+Math.toDegrees(pitch1)+" pitch2:
		// "+Math.toDegrees(pitch2));
		return -(pitch1 - pitch2);
	}

	/**
	 * Calculate the bearing re. North between a landmark and image location.
	 * 
	 * @param landMrk
	 * @param imagePos
	 * @return the bearing of the landmark in radians.
	 */
	private static double calcBearing(LandMark landMrk, LatLong imagePos) {
		double bearing;
		// if the landMarks have a true GPS location then work out bearing, otherwise
		// the landMark should have a pre-defined bearing from image;
		if (landMrk.getPosition() != null) {
			bearing = Math.toRadians(imagePos.bearingTo(landMrk.getPosition()));
		} else {
			bearing = Math.toRadians(landMrk.getBearing());
		}
		// System.out.println("VRLandMarkMethod: Landmark bearing: "
		// +Math.toDegrees(bearing));
		return bearing;
	}

	/**
	 * Calculate the pitch between a landMark and image location.
	 * 
	 * @param landMrk-current
	 *            landmark
	 * @param imagePos-image
	 *            location
	 * @return
	 */
	public static double calcPitch(LandMark landMrk, LatLong imagePos) {
		double pitch;
		// if the landMarks have a true GPS location then work out pitch, otherwise the
		// landMark should have a pre-defined pitch from image;
		if (landMrk.getPosition() != null) {
			double distance = landMrk.getPosition().distanceToMetres(imagePos);
			// System.out.println("LndMark Height: "+landMrk.getHeight() + "imagePos Height:
			// "+ imagePos.getHeight() +"Height diff:
			// "+(landMrk.getHeight()-imagePos.getHeight()) + "distance: "+distance);
			pitch = Math.atan((landMrk.getHeight() - imagePos.getHeight()) / distance);
		} else {
			pitch = Math.toRadians(landMrk.getPitch());
		}
		return pitch;
	}

	/**
	 * Calculates the tilt of the image based on two landmarks selected on that
	 * image and the known angles between those landmarks.
	 * 
	 * @param landmrkP1-
	 *            the location on the image of landmark1-pixels
	 * @param landmrkP2-
	 *            the location on the image of landmark2-pixels
	 * @param bearingDiff-
	 *            the difference in heading between landMark1 and landMark2
	 * @param pitchDiff-
	 *            the difference in pitch between landMark1 and landMark2
	 * @return tilt in radians
	 */
	public static double calcTilt(Point landmrkP1, Point landmrkP2, double bearingDiff, double pitchDiff) {

		// System.out.println(" landmrkP2.y: "+landmrkP2.y+" landmrkP1.y "+ landmrkP1.y
		// +" landmrkP2.x "+landmrkP2.x+" landmrkP1.x "+landmrkP1.x);
		// work out angle from horizontal
		int opp = landmrkP2.y - landmrkP1.y;
		int adj = landmrkP2.x - landmrkP1.x;

		// tilt angle in radians between the landmark in the image
		double angHorz = Math.atan2((double) opp, (double) adj);

		// work out actual true angle between landmarks
		double angActual = Math.atan2(pitchDiff, bearingDiff);
		// System.out.println("pitchDiff "+ Math.toDegrees(pitchDiff) + "bearing diff:
		// "+Math.toDegrees(bearingDiff) + "Actual Angle: "+Math.toDegrees(angActual));

		// work out tilt
		double tilt = PamUtils.constrainedAngleR(-angActual - angHorz, Math.PI);

		return tilt;
	}

	/**
	 * Must have a special function here to get the height as the a landmark group
	 * may have a predefined height if a theodolite or other instrument was used to
	 * get bearings to landmarks
	 * 
	 * @param tideHeight
	 *            - True to use the tide height fofset in the image height
	 *            calculation.
	 * @return the height of the image in meters
	 */
	private VRHeightData getImageHeight(boolean tideHeight) {
		LandMarkGroup landmrkGroup = vrControl.getVRParams().getLandMarkDatas()
				.get(vrControl.getVRParams().getSelectedLandMarkGroup());

		Double height = landmrkGroup.getOriginHeight();

		VRHeightData heightDatas = new VRHeightData();

		double tideVal = tideHeight ? vrControl.getTideManager().getHeightOffset() : 0;

		if (height == null) {
			heightDatas = vrControl.getVRParams().getCurrentheightData().clone();
			heightDatas.height = heightDatas.height - tideVal;
		} else {
			heightDatas.name = "forced landmark height ";
			heightDatas.height = height - tideVal;
		}

		// System.out.println("The image height is "+ heightDatas + " tide value: " +
		// tideVal + " "+tideHeight);

		return heightDatas;
	}

	/**
	 * Return the position the image was taken from. This is either the location
	 * from which bearings were determined for the current land mark group or uses
	 * the location manager to determine a position. Note that in the event any
	 * landmark is defined by bearings rather than a latlong then the position of
	 * the image has to be the location from which those bearings were calculated.
	 * 
	 * @return the position of the image.
	 */
	public LatLong getImagePos(boolean tideHeight) {
		LandMarkGroup lndMrksGroup = getSelectedLMGroup();
		if (lndMrksGroup == null)
			return null;
		// if the landmark group contains any landmarks which are define by bearings
		// rather than a lat long and height then we must use the position these
		// bearings were taken from.
		if (lndMrksGroup.getOriginLatLong() != null)
			return lndMrksGroup.getOriginLatLong();
		// otherwise defualt to the position manager.
		LatLong imagePos = getGPSinfo(); // get image position from location manager.
		imagePos.setHeight(getImageHeight(tideHeight).height);// important to set correct height as used in some calc's
																// here;
		return imagePos;
	}

	/**
	 * Get the current tilt
	 * 
	 * @return the tilt
	 */
	public Double getTilt() {
		return this.tilt;
	}

	/**
	 * Get the controller for the module.
	 * 
	 * @return the control
	 */
	public VRControl getVRControl() {
		return this.vrControl;
	}

	/***
	 * Get the current status flag.
	 * 
	 * @return the status flag.
	 */
	public int getCurrentStatus() {
		return this.currentStatus;
	}

	/**
	 * Set the current status flag.
	 * 
	 * @param flag
	 *            - the current status flag to set
	 */
	public void setCurrentStatus(int flag) {
		this.currentStatus = flag;
	}

	// public Pane getSidePaneFX() {
	// createGUI();
	// return landMarkGUIFX.getSidePaneFX();
	// }

	/**
	 * Get the current landmark points.
	 * 
	 * @return the land mark points.
	 */
	public ArrayList<Point> getLandMarkPoints() {
		return landMarkPoints;
	}

	/**
	 * Tilt measurment for all combinations of landmarks.
	 * 
	 * @return array list of points.
	 */
	public ArrayList<Double> getTiltVals() {
		return tiltVals;
	}

	/**
	 * Get the currently set landmarks.
	 * 
	 * @return the set landmarks
	 */
	public ArrayList<LandMark> getSetLandMarks() {
		return this.setLandMarks;
	}

	@Override
	public VROverlayAWT getOverlayAWT() {
		createGUI();
		return this.landMarkGUIAWT;
	}

	@Override
	public VROverlayFX getOverlayFX() {
		createGUI();
		return this.landMarkGUIFX;
	}


}
