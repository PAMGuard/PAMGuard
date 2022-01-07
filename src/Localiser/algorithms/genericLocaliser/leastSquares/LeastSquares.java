package Localiser.algorithms.genericLocaliser.leastSquares;

import Localiser.LocaliserPane;
import Localiser.algorithms.genericLocaliser.Chi2Bearings;
import Localiser.algorithms.genericLocaliser.MinimisationAlgorithm;
import Localiser.algorithms.genericLocaliser.MinimisationFunction;
import Localiser.algorithms.locErrors.LocaliserError;
import Localiser.algorithms.locErrors.SimpleError;
import Localiser.detectionGroupLocaliser.DetectionGroupLocaliser;
import Localiser.detectionGroupLocaliser.GroupDetection;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import PamDetection.AbstractLocalisation;
import PamUtils.LatLong;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import Stats.LinFit;
import pamMaths.PamQuaternion;
import pamMaths.PamVector;

/**
 * A least squares algorithm for solving particular localisation problems. Less generic than MCMC and Simplex. 
 * @author Jamie Macaulay
 *
 */
public class LeastSquares implements MinimisationAlgorithm {
	
	/**
	 * Least squares algorithm. 
	 */
	private Chi2Bearings leastSquaresData;
	
	private Object synvObject=new Object();

	/**
	 * The loclisation error. 
	 */
	private LocaliserError[] errors = new LocaliserError[1];
	
	/**
	 * Thge loclaisation result 
	 */
	private double[][] result=new double[1][3];
	
	
	private double[] chi2=new double[1];
	
	
	/**
	 * Set the least squares algorithm. 
	 */
	public void setMinimisationFunction(MinimisationFunction leastSquaresData) {
		//least squares can only handle bearings for now. 
		if (leastSquaresData instanceof Chi2Bearings){
			this.leastSquaresData=(Chi2Bearings) leastSquaresData;
		}
		else System.err.println("The least squares algorithm can only accept a bearing based minimisation algorithm");
		
		
	}
	
	/**
	 * Calcuates the position of a detection assuming that sub detections all 
	 * contain bearings, which are probably ambiguous. If groupSize > 0, then 
	 * atempt to group clicks into fewer categories to speed up fit. (Not implemented).
	 * @param bearingInfo
	 * @param side -1 or 1 for left and right
	 * @param groupSize
	 * @return true of localisation calculated successfully
	 */
	public boolean localiseDetectionGroup(Chi2Bearings bearingInfo) {
		//System.out.println("Least Squares Loclaisation: ");
		PamVector[] usedWorldVectors = bearingInfo.getUsedWorldVectors();
		if (usedWorldVectors == null || usedWorldVectors.length == 0) {
			return false;
		}
		
		/*
		 * Basic information we need for each detection is
		 * 1. position relative to given reference
		 * 2. reference bearing for each sub detection (bearing will probably be ambiguous)
		 * 3. Whether bearings are ambigous or not. 
		 */
		PamDataUnit subDetection;
		PamVector subLatLong;

		
		/*
		 * Plan to 1. find the detection with a bearing closest to 90 degrees, then use the array heading at
		 * that detection as a reference for everything. So that will be x = y = 0. For all 
		 * other detections, work out an x, y in m from 0,0. then rotate them all onto an axis 
		 * which is the array heading at 0,0. After that, it's all easy !
		 */
		double bestAngle = 9999999;
		int bestSubDetection = -1;
		double angle = 0;
		double angles;
		int usefulSubDetections = 0;
		double[] planarAngles;
		synchronized (synvObject){
			for (int i = 0; i < usedWorldVectors.length; i++) {
				usefulSubDetections++;
				
				angle=Math.abs(PamUtils.constrainedAngleR(PamVector.vectorToSurfaceBearing(bearingInfo.getUsedWorldVectors()[i])-bearingInfo.getRotationVectors()[i].toHeading(), Math.PI)); 
				
				
				//System.out.println("LeastSquares new: "+Math.toDegrees(angle)); 
				if (Math.abs(angle - Math.PI / 2) < Math.abs(bestAngle - Math.PI / 2)) {
					bestSubDetection = i;
					bestAngle = angle;				
				}
			}
			
			if (bestSubDetection < 0) return false;
		}
		
//		System.out.println("Least Squares Loclaisation: bestSubDetection: " +bestSubDetection + 
//				" best angle: "+Math.toDegrees(bestAngle)+ " nSubdetections: "+usefulSubDetections);

		
		
		PamQuaternion rotation = bearingInfo.getRotationVectors()[bestSubDetection]; // this is the heading from the berst angle. 
		double referenceHeading = rotation.toHeading(); 
		
//		System.out.println("Least Squares Loclaisation: reference angle: " +Math.toDegrees(referenceHeading)); 

		// referenceHeading is from North, referenceAngle is up from x axis. 
		double referenceAngle = Math.PI / 2 - referenceHeading;
		
		
		
		PamVector originLatLong = bearingInfo.getSubDetectionOrigins()[bestSubDetection]; 
		double x[] = new double[usefulSubDetections];
		double y[] = new double[usefulSubDetections];
		double xt, yt;
		double sig[] = new double[usefulSubDetections];
		double arrayAngle[] = new double[usefulSubDetections];
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

		for (int i = 0; i < usefulSubDetections; i++) {
			subLatLong = bearingInfo.getSubDetectionOrigins()[i];
			
			xt= subLatLong.getVector()[0]-originLatLong.getVector()[0];
			yt= subLatLong.getVector()[1]-originLatLong.getVector()[1];
			x[i] = a * xt + b * yt;
			y[i] = c * xt + d * yt; 
			
			/**
			 * Cartesian angle. i.e. clockwise is negative, anti clockwise is positive. 
			 */
			arrayAngle[i] = (Math.PI/2-bearingInfo.getRotationVectors()[i].toHeading()) - referenceAngle;

			/*
			 * Angle relative to the primary heading. First side is to the left and gives +ve ngles, the second side 
			 * is to the right of the track and gives negative angles. 
			 */
			angle = -(PamVector.vectorToSurfaceBearing(bearingInfo.getUsedWorldVectors()[i]) - (Math.PI/2-referenceAngle));
			angle = PamUtils.constrainedAngleR(angle, Math.PI);
			//System.out.println(Math.toDegrees(PamVector.vectorToSurfaceBearing(bearingInfo.getUsedWorldVectors()[i])) + " ref "+Math.toDegrees(referenceAngle));
			detectionAngle[i] = angle;//-(arrayAngle[i] - angle);
			
//			System.out.println("Least squares new: xt: "+ xt+ " yt:"+yt+
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
		if (Double.isNaN(x0) || Double.isNaN(y0) ) {
			return false;
		}

		/*
		 * If angles do not converge, then the fitted position will come out on the wrong side of the
		 * line. Positive 'side' will be below the axis, so y0 < 0 and side = -1 will give a position
		 * above the axis so y0 > 0. If side and y0 have the same sign, then the bearings didn't
		 * converge and the position is the wrong side of the line. x0 can be anything since 
		 * the fit may be ahead or astern of the reference position. 
		 */
//		System.out.println(String.format("Angle %3.1f, y0 %3.1f, x0 %3.1f", Math.toDegrees(angle), y0, x0));
//		if ((angle-Math.PI) * y0  < 0) {
		if (angle * y0  < 0) { // note that y0 - -1/linfitb so check it's on the +ve side of the lin. 
//			System.out.printf("Least Squares cannot return y0 = %3.1f, angle = %3.1f degrees\n", y0, angle*180./Math.PI);
			return false;
		}



		// set the output data, which can get picked up by the calling process using the 
		// getters below.
		//		originLatLong is done
		double range = Math.sqrt(y0 * y0 + x0 * x0);
		double bearing = Math.atan2(y0, x0);
		double trueBearing = bearing + referenceAngle; // Cartesian reference. 
		trueBearing = Math.PI / 2 - trueBearing;
		trueBearing *= 180. / Math.PI; //convert to radians
		
//		System.out.println(String.format("Least Squares new: True bearing:  %3.1f, range %3.1f, ", trueBearing, range));

		//now work out where this is is on the map so can give a cartesian position back.
		PamVector pos=originLatLong.add(PamVector.fromHeadAndSlant(trueBearing, 0).times(range));
		result[0][0]=pos.getVector()[0]; 
		result[0][1]=pos.getVector()[1]; 
		result[0][2]=pos.getVector()[2]; 

		//detectionLatLong = originLatLong.travelDistanceMeters(trueBearing, range);
		
		/**Calculate Errors**/
		chi2[0]=linFit.getChi2(); 

		Double perpendicularError = Math.abs(linFit.getSigb() / Math.pow(linFit.getB(),2));
		Double parallelError = Math.sqrt(Math.pow(y0*linFit.getSiga(), 2) + Math.pow(linFit.getA()*perpendicularError,2));
		Double[] error={perpendicularError, parallelError, null}; 
		
		//System.out.println("Reference angle: "+referenceAngle + "  "+ Math.toDegrees(referenceHeading) + " trueBearing: "+ trueBearing);
		SimpleError simpleLocError= new SimpleError(error, referenceHeading+Math.PI/2); 
		errors[0]=simpleLocError; 
		
	
		return true;
	}



	@Override
	public synchronized boolean runAlgorithm() {
		boolean success=false; 
		if (leastSquaresData!=null) {
			success=localiseDetectionGroup( leastSquaresData);
		}
		else {
			System.err.println("LeastSquares: The least square algorithm has now set minimisation function");
		}
		// failure only likely to occur if angle change not enough for lines to converge. 
//		if (!success) System.out.println("LeastSquares: The least square algorithm failed");
		return success;
	}

	@Override
	public double[][] getResult() {
		return this.result;
	}

	@Override
	public double[] getChi2() {
		// the least squares algorithm has no chi2 value...
		return chi2;
	}

	@Override
	public LocaliserError[] getErrors() {
		return errors; 
	}

	@Override
	public void notifyStatus(int status, double progress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasParams() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LocaliserPane<?> getSettingsPane() {
		// TODO Auto-generated method stub
		return null;
	}

}
