package targetMotionModule;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import pamMaths.PamVector;
import PamDetection.AbstractLocalisation;
import PamUtils.LatLong;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import Stats.LinFit;

/**
 * Class to rotate and transform an event so that it's track lies as close
 * as possible to the x axis. 
 * <br>
 * Step 1 is to convert all units to metres, relative to the origin coordinate
 * of the first sub detection
 * <br> 
 * Step 2 is to fit a straight line through all the origin points
 * <br>
 * Step 3 is to rotate all position and angle vectors so that they correspond to 
 * a track as close as possible to the x axis. 
 * @author Doug Gillespie
 * 
 * Deprecated. Not used and will probably fall over with system whereby sub detection info
 * is present, but not all sub detections are. 
 *
 */
@Deprecated
@SuppressWarnings("rawtypes") 
public class EventRotator extends AbstractTargetMotionInformation{


	private long lastUpdateTime;

	/**
	 * Sub detection origins, in m E and N of the first one.
	 * <br>These values have NOT been rotated.  
	 */
	private PamVector[] subDetectionOrigins;

	/**
	 * Unit vectors giving the heading of the array at each sub detection
	 * in Cartesian coordinate frame. 
	 * <br>These are NOT rotated
	 */
	private PamVector[] subDetectionHeadings;

	/**
	 * Sub detection origins, rotated to lie close to the x axis. 
	 */
	private PamVector[] rotatedOrigins;

	/**
	 * Array headings at each point in Catesian coordinate frame
	 * rotated to be close to x axis.  
	 */
	private PamVector[] rotatedHeadings;

	private PamVector[][] rotatedWorldVectors;

	private PamVector[][] worldVectors;
		
	private long[] pointTimes;

	private int nSubDetections;

	private LatLong plotOrigin;

	private double linFitA, linFitB;

	/**
	 * Reference angle is the Cartesian angle up from the x axis.  
	 */
	private double referenceAngle;

	/**
	 * Array angles for each sub detection - radians anti clock wise from x axis.
	 */
	private double[] rotatedArrayAngles;

	private int referenceHydrophones;

	private ArrayList<PamDataUnit> pamDetections;
	
	ArrayList<ArrayList<Point3f>> hydrophonePos;
	/**
	 * @param pamDetection
	 */
	
	//array infromation
	
	public EventRotator(ArrayList<PamDataUnit> pamDetections, PamDataBlock pamDataBlock) {
		super(pamDetections,pamDataBlock);
		this.pamDetections = pamDetections;
		rotatedWorldVectors = null;
		//get Array Info
		calculateMetrePoints();
	}

	/**
	 * Calculate the points on the line on a metre scale relative
	 * to the first coordinate. 
	 */
	private void calculateMetrePoints() {
		lastUpdateTime = pamDetections.get(0).getLastUpdateTime();
		nSubDetections = pamDetections.size();
		subDetectionOrigins = new PamVector[nSubDetections];
		subDetectionHeadings = new PamVector[nSubDetections];
		pointTimes = new long[nSubDetections];
		if (nSubDetections == 0) {
			return;
		}
		PamDataUnit pd = pamDetections.get(0);
		if (pd == null) {
			return;
		}
		plotOrigin = pd.getOriginLatLong(false);
		double x, y, z;
		AbstractLocalisation localisation;
		double[] xArray = new double[nSubDetections]; // temp to use in linfit
		double[] yArray = new double[nSubDetections];
		rotatedArrayAngles = new double[nSubDetections];
		LatLong detOrigin;

		for (int i = 0; i < nSubDetections; i++) {
			pd = pamDetections.get(i);
			localisation = pd.getLocalisation();
			if (localisation == null) {
				continue;
			}
			detOrigin = pd.getOriginLatLong(false);
			x = plotOrigin.distanceToMetresX(detOrigin);
			y = plotOrigin.distanceToMetresY(detOrigin);
			z = detOrigin.getHeight();
			xArray[i] = x;
			yArray[i] = y;
			subDetectionOrigins[i] = new PamVector(x, y, z);
			rotatedArrayAngles[i] = localisation.getBearingReference();
			referenceHydrophones |= localisation.getReferenceHydrophones();
			pointTimes[i] = pd.getTimeMilliseconds();
		}

		if (nSubDetections >= 2) {
			// now fit a straight line through the data.
			LinFit linFit = new LinFit(xArray, yArray, nSubDetections);
//			System.out.println(String.format("Lin fit A = %3.2f, B = %3.2f, chi = %3.2f",
//					linFit.getA(), linFit.getB(), linFit.getChi2()));
			linFitA = linFit.getA();
			linFitB = linFit.getB();
			referenceAngle = Math.atan(linFitB);
			// now work out if it's pointing in the opposite direction or not..
			if (xArray[nSubDetections-1] < xArray[0]) {
				referenceAngle += Math.PI;
			}
			while (referenceAngle > Math.PI) {
				referenceAngle -= Math.PI*2;
			}
//			System.out.println(String.format("Rotation angle = %3.1f radians,  %3.1f degrees", 
//					referenceAngle, Math.toDegrees(referenceAngle)));
		}
		rotatedOrigins = new PamVector[nSubDetections];
		rotatedHeadings = new PamVector[nSubDetections];
		for (int i = 0; i < nSubDetections; i++) {
			subDetectionHeadings[i] = new PamVector(Math.cos(Math.PI/2-rotatedArrayAngles[i]), Math.sin(Math.PI/2-rotatedArrayAngles[i]), 0);
			rotatedOrigins[i] = subDetectionOrigins[i].rotate(-referenceAngle);
			rotatedArrayAngles[i] = Math.PI/2. - rotatedArrayAngles[i];
			rotatedArrayAngles[i] += -referenceAngle;
			rotatedHeadings[i] = new PamVector(Math.cos(rotatedArrayAngles[i]), Math.sin(rotatedArrayAngles[i]), 0);
//			System.out.println(String.format("Event %d sub %d: %s rotates to %s, heading %3.2f or %s", pamDetection.getDatabaseIndex(), i,
//			subDetectionOrigins[i].toString(), rotatedOrigins[i].toString(), 
//			Math.toDegrees(rotatedArrayAngles[i]), subDetectionHeadings[i].toString()));
		}

	}
	

	private void calculateRotatedWorldVectors() {
		rotatedWorldVectors = new PamVector[nSubDetections][];
		PamVector[] v;
		PamDataUnit pd;
		AbstractLocalisation localisation;
		for (int i = 0; i < nSubDetections; i++) {
			pd = pamDetections.get(i);
			localisation = pd.getLocalisation();
			if (localisation == null) {
				continue;
			}
			v = localisation.getRealWorldVectors();
			if (v == null) {
				continue;
			}
			for (int j = 0; j < v.length; j++) {
				v[j] = v[j].rotate(-referenceAngle);
			}
			rotatedWorldVectors[i] = v;
		}

	}
	
	public PamVector latLongToMetres(LatLong ll, boolean rotate) {
		PamVector v = new PamVector();
		if (plotOrigin == null) {
			return v;
		}
		v.setElement(0, plotOrigin.distanceToMetresX(ll));
		v.setElement(1, plotOrigin.distanceToMetresY(ll));
		v.setElement(2, ll.getHeight());
		if (rotate) {
			v = v.rotate(-referenceAngle);
		}
		return v;
	}
	
	public LatLong metresToLatLong(PamVector pt, boolean isRotated) {
		if (isRotated) {
			pt = pt.clone().rotate(referenceAngle);
		}
		if (plotOrigin == null) {
			return null;
		}
		LatLong ll = plotOrigin.addDistanceMeters(pt.getElement(0), pt.getElement(1));
		ll.setHeight(pt.getElement(2));
		return ll;
	}
	
	/**
	 * From a position, work out what time that position was passed. 
	 * @param pt
	 * @param isRotated
	 * @return time in milliseconds. 
	 */
	public long metresToTime(PamVector pt, boolean isRotated) {
		if (isRotated == false) {
			pt = pt.clone().rotate(-referenceAngle);
		}
		// now only have to work on the x coordinate. 
		int iBefore=0, iAfter=0;
		double x = pt.getElement(0);
		for (int i = 1; i < nSubDetections; i++) {
			if (rotatedOrigins[i-1].getElement(0) <= x && 
					rotatedOrigins[i].getElement(0) >= x) {
				iBefore = i-1;
				iAfter = i;
			}
		}
		double d1, d2;
		d1 = x - rotatedOrigins[iBefore].getElement(0);
		d2 = rotatedOrigins[iAfter].getElement(0) - x;
		if (d1 == 0 && d2 == 0) {
			return pointTimes[iBefore];
		}
		long t = (long) ((pointTimes[iAfter]-pointTimes[iBefore])*d2/(d1+d2));
		return t+pointTimes[iBefore];
	}

	/**
	 * @return the pamDetection
	 */
	public ArrayList<PamDataUnit> getPamDetections() {
		return pamDetections;
	}

	/**
	 * @return the lastUpdateTime
	 */
	public long getLastUpdateTime() {
		return lastUpdateTime;
	}
	


	/**
	 * Sub detection origins, in m E and N of the first one.
	 * <br>These values have NOT been rotated.
	 * @return the subDetectionOrigins
	 */
	public PamVector[] getSubDetectionOrigins() {
		return subDetectionOrigins;
	}
	

	/**
	 * Unit vectors giving the heading of the array at each sub detection
	 * in Cartesian coordinate frame. 
	 * <br>These are NOT rotated
	 * @return the subDetectionHeadings
	 */
	public PamVector[] getSubDetectionHeadings() {
		return subDetectionHeadings;
	}

	/**
	 * Sub detection origins, rotated to lie close to the x axis. 
	 * @return the rotatedOrigins
	 */
	public PamVector[] getRotatedOrigins() {
		return rotatedOrigins;
	}

	/**
	 * @return the number of sub detections (i.e. the number of points 
	 * to expect in each of the vector arrays)
	 */
	public int getnSubDetections() {
		return nSubDetections;
	}

	/**
	 * The LatLong of the coordinate frame origin. This should be
	 * the LatLong of the first sub detection in the event. 
	 * @return the plotOrigin
	 */
	public LatLong getPlotOrigin() {
		return plotOrigin;
	}

	/**
	 * The 'a' parameter of a straight line fit through the coordinates
	 * before they were rotated (y = a + bx); 
	 * @return the linFitA
	 */
	public double getLinFitA() {
		return linFitA;
	}

	/**
	 * The 'b' parameter of a straight line fit through the coordinates
	 * before they were rotated (y = a + bx); 
	 * @return the linFitB
	 */
	public double getLinFitB() {
		return linFitB;
	}

	/**
	 * the reference angle for the straight line track (i.e. the 
	 * getLinfitB() turned into an angle). This angle is in 
	 * the Cartesian coordinate frame (i.e. anti clockwise from x axis).   
	 * @return the referenceAngle
	 */
	public double getReferenceAngle() {
		return referenceAngle;
	}

	/**
	 * Get an array of rotated array angles at each point in 
	 * the Cartesian coordinate frame (i.e. anti clockwise from x axis).  
	 * @return the arrayAngles
	 */
	public double[] getRotatedArrayAngles() {
		return rotatedArrayAngles;
	}

	/**
	 * Array headings at each point in Cartesian coordinate frame
	 * rotated to be close to x axis.  
	 * @return the rotatedHeadings
	 */
	public PamVector[] getRotatedHeadings() {
		return rotatedHeadings;
	}

	/**
	 * Get rotated world vectors - vectors rotated with other data to lie along 
	 * the x axis. 
	 * @return
	 */
	public PamVector[][] getRotatedWorldVectors() {
		if (rotatedWorldVectors == null) {
			calculateRotatedWorldVectors();
		}
		return rotatedWorldVectors;
	}

	/**
	 * Get vectors transformed into real world Co-Ordinates, i.e. relative to N and E rather than
	 * the array axis. 
	 * @return
	 */
	public PamVector[][] getWorldVectors() {
		if (worldVectors == null) {
			calculateWorldVectors();
		}
		return worldVectors;
	}

	/**
	 * 
	 * @return bitmap of hydrophones used in the event rotator
	 */
	public int getReferenceHydrophones() {
		return referenceHydrophones;
	}

	


	

}
