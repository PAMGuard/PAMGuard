package Localiser.detectionGroupLocaliser;

import GPS.GpsData;
import PamDetection.AbstractLocalisation;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import Stats.LinFit;

/**
 * Class to provide localisation functions for PamDetections which contain > 1 sub detections. 
 * The first iteration of this is being written for the purposes of working out where bearing lines
 * from multiple manually tracked clicks cross and it's likely it will all need to be re-written
 * one day !
 * @author Doug
 *
 */
@Deprecated
public class DetectionGroupLocaliser {

	private PamProcess parentProcess;

	private LatLong localisationLatLong;

	private GpsData originLatLong; 

	private LatLong detectionLatLong;

	private double range;

	private double bearing;

	private double perpendicularError;

	private double parallelError;

	private double referenceHeading;

	public DetectionGroupLocaliser(PamProcess parentProcess) {
		this.parentProcess = parentProcess;
	}

	/**
	 * calculate the position of detecion assuming that sub detections all contain
	 * bearings, which are probably ambiguous, so will calculate for one side or
	 * another - +1 = right of track, -1 = left of track. No other values are
	 * allowed !
	 * @param groupDetection
	 * @param side -1 or 1 for left and right
	 * @return true of localisation calculated successfully
	 */
	public boolean localiseDetectionGroup(GroupDetection groupDetection, int side) {

		return localiseDetectionGroup(groupDetection, side, 0);

	}

	/**
	 * Calcuates the position of a detection assuming that sub detections all 
	 * contain bearings, which are probably ambiguous. If groupSize > 0, then 
	 * atempt to group clicks into fewer categories to speed up fit. (Not implemented).
	 * @param groupDetection
	 * @param side -1 or 1 for left and right
	 * @param groupSize
	 * @return true of localisation calculated successfully
	 */
	public boolean localiseDetectionGroup(GroupDetection groupDetection, int side, double groupSize) {
		// 29/11/2016 MO: converted PamDetection references to PamDataUnit, since we've moved
		// the generics to there now
		
		/*
		 * Basic information we need for each detection is
		 * 1. position relative to given reference
		 * 2. reference bearing for each sub detection (bearing will probably be ambiguous)
		 * 3. Whether bearings are ambigous or not. 
		 */
//		PamDetection subDetection;
		PamDataUnit subDetection;
		LatLong subLatLong;

		if (side != 1) {
			side = -1;
		}

		// side is 1 or -1, these must go into localisaions 0 and 1 !
		int sideIndex = 0;
		if (side == 1) {
			sideIndex = 0;
		}
		else if (side == -1) {
			sideIndex = 1;
		}
		else return false;
		
		
		/*
		 * Plan to 1. find the detection with a bearing closest to 90 degrees, then use the array heading at
		 * that detection as a reference for everything. So that will be x = y = 0. For all 
		 * other detections, work out an x, y in m from 0,0. then rotate them all onto an axis 
		 * which is the array heading at 0,0. After that, it's all easy !
		 */
		double bestAngle = 9999999;
		int bestSubDetection = -1;
		AbstractLocalisation loc;
		double angle = 0;
		double angles;
		int usefulSubDetections = 0;
//		PamDetection[] subDetections = null;
		PamDataUnit[] subDetections = null;
		double[] planarAngles;
		synchronized (groupDetection.getSubDetectionSyncronisation()){
			for (int i = 0; i < groupDetection.getSubDetectionsCount(); i++) {
				subDetection = groupDetection.getSubDetection(i);
				loc = subDetection.getLocalisation();
				if (useDetection(subDetection, sideIndex) == false) continue;
				usefulSubDetections++;
				planarAngles = loc.getPlanarAngles();
				if (sideIndex >= planarAngles.length) {
					continue;
				}
				angle = planarAngles[sideIndex];
				//System.out.println("LeastSquares old: "+Math.toDegrees(angle)); 
				if (Math.abs(angle - Math.PI / 2) < Math.abs(bestAngle - Math.PI / 2)) {
					bestSubDetection = i;
					bestAngle = angle;				
				}
			}
			
//			System.out.println("Least Squares old: bestSubDetection: " +bestSubDetection + 
//					" best angle: "+Math.toDegrees(bestAngle)+ " nSubdetections: "+usefulSubDetections);

			// now pull out the useful ones and store their references in a new array
			// seems a bit inneficient, but will make 
			// subsequent loops easier. 
//			subDetections = new PamDetection[usefulSubDetections];
			subDetections = new PamDataUnit[usefulSubDetections];
			int j = 0;
			for (int i = 0; i < groupDetection.getSubDetectionsCount(); i++) {
				subDetection = groupDetection.getSubDetection(i);
				if (useDetection(subDetection, side) == false) continue;
				subDetections[j++] = subDetection;
			}

			if (bestSubDetection < 0) return false;
			int referenceChannels = groupDetection.getSubDetection(bestSubDetection).getChannelBitmap();
			loc = groupDetection.getSubDetection(bestSubDetection).getLocalisation();
		}
		
		
//		referenceHeading = loc.getBearingReference();
//		System.out.println("Leasts squares old reference angle: "+ Math.toDegrees(referenceHeading));
		
		double referenceAngle = Math.PI / 2 - referenceHeading;
		
		
		originLatLong = loc.getParentDetection().getOriginLatLong(false);
		double x[] = new double[usefulSubDetections];
		double y[] = new double[usefulSubDetections];
		double xt, yt;
		double sig[] = new double[usefulSubDetections];
		double arrayHeading[] = new double[usefulSubDetections];
		double detectionAngle[] = new double[usefulSubDetections];
		double fitX[] = new double[usefulSubDetections];
		double fitY[] = new double[usefulSubDetections];

		/** 
		 * all x,y coordinates will have to be rotated by -referenceAngle;
		 * do this using a 2 by 2 rotation matrix
		 *    a   b
		 *    c   d
		 *    
		 */
		double a = Math.cos(referenceAngle);
		double b = -Math.sin(-referenceAngle);
		double c = -b;
		double d = a;		

		for (int i = 0; i < subDetections.length; i++) {
			subDetection = subDetections[i];
			loc = subDetection.getLocalisation();
			subLatLong = subDetection.getOriginLatLong(false);
			xt= originLatLong.distanceToMetresX(subLatLong);
			yt= originLatLong.distanceToMetresY(subLatLong);
			x[i] = a * xt + b * yt;
			y[i] = c * xt + d * yt; 
			arrayHeading[i] = (Math.PI/2-loc.getBearingReference()) - referenceAngle;
			

			if (sideIndex >= loc.getPlanarAngles().length) {
				return false;
			}
			angle = loc.getPlanarAngles()[sideIndex];
			detectionAngle[i] = arrayHeading[i] - angle;
			
//			System.out.println("Least squares old: xt: "+ xt+ " yt:"+yt+
//					" array heading: "+arrayHeading[i]+ " detectionAngle: "
//							+ ""+Math.toDegrees(angle)+ " a: "+a+" b: "+b);

			// now project that bearing down onto the x axis, and update the x
			// coordinate (after this, we will no longer need y).
			x[i] -= (y[i] / Math.tan(detectionAngle[i])); 

			fitX[i] = x[i];
			fitY[i] = 1 / Math.tan(detectionAngle[i]);
			//			sig[i] = 10; // should do something more clever with angle error !)
			sig[i] = 3 * Math.PI / 180 / Math.pow(Math.sin(angle), 2);
			// really need to turn whole fit around so that we fit angle onto position since position is
			// well known and angle isn't. 
		}

		// useful sets of numbers for the fit are now x and detectionAngle

		LinFit linFit = new LinFit(fitX, fitY, usefulSubDetections, sig);
		// if 
		//		fitY[i] = x[i];
		//		fitX[i] = -1.0 / Math.tan(detectionAngle[i]);
		//		double y0 = linFit.getB(); // distance off track line. 
		//		double x0 = linFit.getA();

		// but we're using 
		//		fitX[i] = x[i];
		//		fitY[i] = 1 / Math.tan(detectionAngle[i]);
		// so
		double y0 = -1/linFit.getB();
		double x0 = y0 * linFit.getA();

		/**
		 * The below is no longer true since it's rotated in some arbitrary way
		 * so probably best to look at A and B to see if we're the right side. 
//		 */
//		System.out.printf("Fit side %d, angle %3.1f, A=%3.1f, B=%3.1f, allsig = %3.1f\n", side, angle, Math.signum(linFit.getA()), Math.signum(linFit.getB()),
//				side*Math.signum(linFit.getA())*Math.signum(linFit.getB()));
		/**
		 * No simple rule that I can see - in any case this is deprecated. Need to switch 
		 * whistle and moan paired localiser over to a new system in any case. 
		 */
		/*
		 * If angles do not converge, then the fitted position will come out on the wrong side of the
		 * line. Positive 'side' will be below the axis, so y0 < 0 and side = -1 will give a position
		 * above the axis so y0 > 0. If side and y0 have the same sign, then the bearings didn't
		 * converge and the position is the wrong side of the line. x0 can be anything since 
		 * the fit may be ahead or astern of the reference position. 
		 */
//		System.out.println(String.format("Angle %3.1f, y0 %3.1f, x0 %3.1f", angle, y0, x0));
//		if (y0 * side  < 0) {
//			return false; // removed July 2017.
//		}

		/*
		 * work out why example is chosing rear hydrophones ....
		 * 
		 */
		//		int ccc = groupDetection.getSubDetection(bestSubDetection).getChannelBitmap();
		//		if (ccc == 12) {
		//			System.out.println("channels 12");
		//		}

		// set the output data, which can get picked up by the calling process using the 
		// getters below.
		//		originLatLong is done
		range = Math.sqrt(y0 * y0 + x0 * x0);
		bearing = Math.atan2(y0, x0);
		double trueBearing = bearing + referenceAngle;
		trueBearing = Math.PI / 2 - trueBearing;
		trueBearing *= 180. / Math.PI;
		
//		System.out.println(String.format("Least Squares old: True bearing:  %3.1f, range %3.1f, ", trueBearing, range));

		detectionLatLong = originLatLong.travelDistanceMeters(trueBearing, range);
		perpendicularError = Math.abs(linFit.getSigb() / Math.pow(linFit.getB(),2));
		parallelError = Math.sqrt(Math.pow(y0*linFit.getSiga(), 2) + Math.pow(linFit.getA()*perpendicularError,2));
		//		double xError = linFit.getSiga();
		// bearing error is combination of x error and y error. 
		//		bearingError = 
		//		bearingError

		//		int referenceHydrophones = 
		int iSide = side;
		if (iSide < 0) iSide = 0;
		groupDetection.addFitData(iSide, getOriginLatLong(), getDetectionLatLong(), getBearing(), getRange(), getReferenceHeading(), getPerpendicularError(), 
				getParallelError(), getReferenceHeading());


		return true;
	}

//	boolean useDetection(PamDetection subDetection, int sideIndex) {
	boolean useDetection(PamDataUnit subDetection, int sideIndex) {

		AbstractLocalisation loc = subDetection.getLocalisation();
		if (loc == null) return false;
		double[] angles = loc.getAngles();
		if (angles == null) {
			return false;
		}
//		double angle = loc.getBearing(sideIndex);
		double angle = Double.NaN;
		if (sideIndex >= 0 && sideIndex < angles.length) {
			angle = angles[sideIndex];
		}
		else if (angles.length > 0){
			angle = -angles[0];
		}
		if (Double.isNaN(angle)) return false;
		//		if (side == -1 && loc.bearingAmbiguity() == false) return false;

		return true;
	}

	public LatLong getLocalisationLatLong() {
		return localisationLatLong;
	}

	public GpsData getOriginLatLong() {
		return originLatLong;
	}

	public PamProcess getParentProcess() {
		return parentProcess;
	}

	public double getRange() {
		return range;
	}

	public double getBearing() {
		return bearing;
	}

	public LatLong getDetectionLatLong() {
		return detectionLatLong;
	}

	public double getParallelError() {
		return parallelError;
	}

	public double getPerpendicularError() {
		return perpendicularError;
	}

	public double getReferenceHeading() {
		return referenceHeading;
	}

}
