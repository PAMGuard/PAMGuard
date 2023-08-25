package group3dlocaliser.algorithm.hyperbolic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import Array.ArrayManager;
import Array.SnapshotGeometry;
import GPS.GpsData;
import Jama.Matrix;
import Localiser.LocaliserPane;
import Localiser.algorithms.locErrors.SimpleError;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.LatLong;
import PamUtils.PamArrayUtils;
import PamguardMVC.PamDataUnit;
import generalDatabase.SQLLoggingAddon;
import group3dlocaliser.Group3DLocaliserControl;
import group3dlocaliser.algorithm.Chi2Data;
import group3dlocaliser.algorithm.toadbase.TOADBaseAlgorithm;
import group3dlocaliser.algorithm.toadbase.TOADInformation;
import group3dlocaliser.localisation.LinearLocalisation;
import pamMaths.PamVector;

/**
 * Hyperbolic localisation using methods described in Gillette, M. D., and
 * Silverman, H. F. (2008). �A linear closed-form algorithm for source
 * localization from time-differences of arrival,� IEEE Signal Processing
 * Letters, 15, 1�4.
 * <p>
 * 
 * Also worth reading Spiesberger, J. L. (2001). �Hyperbolic location errors due
 * to insufficient numbers of receivers,� The Journal of the Acoustical Society
 * of America, 109, 3076�3079. which gives a clearer explanation of why at least
 * 4 recievers are needed for 2D localisation and 5 for 3D localisation.
 * <p>
 * Worth noting that the equations derived in Gillette 2008 are functionally
 * identical to those in Spiesberger 2001 and an earlier work by Speisberger and
 * Fristrup:<br>
 * Spiesberger, J. L., and Fristrup, K. M. (1990). �Passive localization of
 * calling animals and sensing of their acoustic environment using acoustic
 * tomography,� The american naturalist, 135, 107�153.
 * 
 * @author Doug Gillespie
 *
 */
public class HyperbolicLocaliser extends TOADBaseAlgorithm {

	private double[] lastPosVector;
	private LocContents locContents = new LocContents(0);
	
	private HyperbolicParams params = new HyperbolicParams();
	
	private HyperbolicSettingsPane hyperbolicLocaliserPane;
	

	public HyperbolicLocaliser(Group3DLocaliserControl group3dLocaliser) {
		super(group3dLocaliser);
	}

	@Override
	public AbstractLocalisation processTOADs(PamDataUnit groupDataUnit, SnapshotGeometry geometry, TOADInformation toadInformation) {

		//System.out.println("Process TOADS: Hyperbolic: " + toadInformation.getChannelMap()); 
		
		/**
		 * This module is a little odd in that it stores paramters for each algorithm in it's own has table without acc
		 */
		params = (HyperbolicParams) group3dLocaliser.getLocaliserAlgorithmParams(this).getAlgorithmParameters();

		
		int shape = geometry.getShape();
		GroupLocalisation loclaisation = null; 
		switch (shape) {
		case ArrayManager.ARRAY_TYPE_LINE:
			//System.out.println("HyperbolicLocaliser: Line");

			return processTOADs2D(groupDataUnit, geometry, toadInformation);
		case ArrayManager.ARRAY_TYPE_PLANE:
			//System.out.println("HyperbolicLocaliser: Plane");

			loclaisation = processTOADsPlane(groupDataUnit, geometry, toadInformation);
			calcErrors(loclaisation.getGroupLocaResult(0),  groupDataUnit,  geometry,  toadInformation,    params);
			return loclaisation;

		case ArrayManager.ARRAY_TYPE_VOLUME:
			//System.out.println("HyperbolicLocaliser: Volume");

			loclaisation =  processTOADs3D(groupDataUnit, geometry, toadInformation);
			calcErrors(loclaisation.getGroupLocaResult(0),  groupDataUnit,  geometry,  toadInformation,    params);
			return loclaisation;
		}
		System.out.println("HyperbolicLocaliser: Shape is null?");
		return loclaisation; //will be null if no array geometry found. 
	}

	private HyperbolicGroupLocalisation processTOADsPlane(PamDataUnit groupDataUnit, SnapshotGeometry geometry,
			TOADInformation toadInformation) {
		PamVector[] arrayAxes = geometry.getArrayAxes();
		if (arrayAxes.length < 2) {
			return null;
		}
//			arrayAxes = Arrays.copyOf(arrayAxes, 3);
//			arrayAxes[2] = arrayAxes[0].vecProd(arrayAxes[1]);
//		}
		/*
		 * Following code should flatten the array onto the x,y plane. 
		 */
		PamVector[] rotationVectors = new PamVector[3];
		rotationVectors[2] = arrayAxes[0].vecProd(arrayAxes[1]);
		rotationVectors[0] = arrayAxes[0];
		rotationVectors[1] = arrayAxes[1];
		
		Matrix axesMatrix = PamVector.arrayToMatrix(rotationVectors);
		PamVector[] geom3d = geometry.getGeometry();
		Matrix geomMatrix = PamVector.arrayToMatrix(geom3d);
		// now rotate the geometry by the axes matrix ...
		Matrix flatGeom = geomMatrix.times(axesMatrix.inverse());
		/*
		 *  check that the z dimension is zero ...
		 *  Should be able to comment this out after debugging. 
		 */
		double[] cSums = new double[3];
		for (int i = 0; i < flatGeom.getRowDimension(); i++) {
			for (int j = 0; j < 3; j++) {
				cSums[j] += Math.abs(flatGeom.get(i, j));
			}
		}
		/**
		 *  Code above was to flatten geometry into a x,y plane, so all z coordinates will be 
		 *  zero. Code below is now the same as the 3D code, but reduced to ignore everything using z
		 */
		double c = geometry.getCurrentArray().getSpeedOfSound();
		double cErr = geometry.getCurrentArray().getSpeedOfSoundError();
		
//		PamVector[] geom = geometry.getGeometry();
		PamVector[] geom = new PamVector[flatGeom.getRowDimension()];
		double[][] geomData = flatGeom.getArray();
		double xt = 0, yt = 0; 
		for (int i = 0; i < geom.length; i++) {
			geom[i] = new PamVector(geomData[i]);
			xt += geomData[i][0];
			yt += geomData[i][1];
		}
		PamVector centre = new PamVector(xt/geom.length, yt/geom.length, 0);
		// note that this is a maximum - we may have to reduce the number of rows if 
		// some are badly populated. 
		double[][] delays = toadInformation.getToadSeconds();
		double[][] delayErrors = toadInformation.getToadErrorsSeconds();
		int[] hydrophones = toadInformation.getHydrophoneList();
		int nPhones = delays.length;
		int nPairs = nPhones*(nPhones-1)/2;
		boolean[] goodRow = new boolean[nPairs];
		int nCols = 2+nPhones-1;
		double[][] left = new double[nPairs][nCols];
		double[][] right = new double[nPairs][1];
		double[][] rightPlusErr = new double[nPairs][1];
		double[] scale = new double[nPairs];
		int iPair = 0;
		int nGoodRows = 0;
		double delayErr;
		for (int i = 0; i < nPhones; i++) {
			for (int j = i+1; j < nPhones; j++, iPair++) {
				double delay = -delays[i][j];
				PamVector geomi = geom[hydrophones[i]];
				PamVector geomj = geom[hydrophones[j]];
				if (Double.isNaN(delay) || geomi == null || geomj == null) {
					continue;
				}
				delay *= c;
				right[iPair][0] = Math.pow(delay, 2);
				if (delayErrors != null) {
					delayErr = delayErrors[i][j]*c;
				}
				else {
					delayErr = 0;
				}
				rightPlusErr[iPair][0] = Math.pow(delay+delayErr, 2);
				double l = 0;
				for (int d = 0; d < 2; d++) {
					l += Math.abs(geomi.getCoordinate(d)-geomj.getCoordinate(d)); // total distance between phones, to check not zero
					left[iPair][d] = geomi.getCoordinate(d)-geomj.getCoordinate(d); // first two cols of left matrix
					right[iPair][0] += (Math.pow(geomi.getCoordinate(d)-centre.getCoordinate(d), 2) - Math.pow(geomj.getCoordinate(d)-centre.getCoordinate(d), 2));
					rightPlusErr[iPair][0] += (Math.pow(geomi.getCoordinate(d)-centre.getCoordinate(d), 2) - Math.pow(geomj.getCoordinate(d)-centre.getCoordinate(d), 2));
					scale[iPair] += Math.pow(geomi.getCoordinate(d)-geomj.getCoordinate(d), 2);
				}
				if (l == 0) {
					// check that the two hydrophones are not at the same point in space. 
					continue;
				}
				goodRow[iPair] = true;
				nGoodRows ++;
				left[iPair][2+i] = delay;
				right[iPair][0] *= 0.5;
				rightPlusErr[iPair][0] *= 0.5;
			}
//			break;
		}
		// reduce rows if necessary. 
		double[][] goodLeft = left;
		double[][] goodRight = right;
		double[][] goodRightError = rightPlusErr;
		double[] goodScale = scale;
		if (nGoodRows < nPairs) {
			goodLeft = new double[nGoodRows][];
			goodRight = new double[nGoodRows][];
			goodRightError = new double[nGoodRows][];
			goodScale = new double[nGoodRows];
			int iG = 0;
			for (int i = 0; i < nPairs; i++) {
				if (goodRow[i] == false) {
					continue;
				}
				goodLeft[iG] = left[i];
				goodRight[iG] = right[i];
				goodRightError[iG] = rightPlusErr[i];
				goodScale[iG] = scale[i];
				iG++;
			}
		}
		boolean weight = true;
		if (weight) {
			for (int i = 0; i < goodLeft.length; i++) {
				double w = goodScale[i];
				if (w <= 0) continue;
				w = 1./Math.sqrt(w);
				w = 1./w;
//				w = 1000.;
				for (int j = 0; j < goodLeft[i].length; j++) {
					goodLeft[i][j] *= w;
				}
				goodRightError[i][0] *= w;
				goodRight[i][0] *= w;
			}
		}
		// now check that every column has something in it ... ?? zero is a valid number !
		
		Matrix leftMatrix = new Matrix(goodLeft);
		Matrix rightMatrix = new Matrix(goodRight);
		Matrix rightPlusErrorMatrix = new Matrix(goodRightError);
//		Debug.out.println("right rows = " +  rightMatrix.getRowDimension() + ", cols " + rightMatrix.getColumnDimension() +
//				" left rows " + leftMatrix.getRowDimension() + ", cols " + leftMatrix.getColumnDimension());
		
//		Matrix answer = leftMatrix.solve(rightMatrix);
		Matrix answer = null, leftInverse = null, answer2 = null;
		try {
			leftInverse = leftMatrix.inverse();
//			Debug.out.println("Left inverse rows " + leftInverse.getRowDimension() + ", columns " + leftInverse.getColumnDimension());
			answer = leftInverse.times(rightMatrix);
//			answer2 = rightMatrix.times(leftInverse.transpose());
		}
		catch (Exception e) {
			System.out.println("Error in HyperbolicLocaliser.processTOADsPlane: " + e.getMessage());
//			Matrix m = leftMatrix.transpose();
//			m = m.inverse();
			return null;
		}
//		Debug.out.println("answer rows = " +  answer.getRowDimension());
		// get a chi2 value for this location. 
		double[] posVec = answer.getColumnPackedCopy();
		posVec = Arrays.copyOf(posVec, 3);
		lastPosVector = posVec;
		double[][][] dE = {delays, delayErrors};
		Chi2Data chiData = calcChi2(geometry, toadInformation, posVec);
		if (chiData.getDegreesOfFreedom() <= 0) {
			return null;
		}
		ChiSquaredDistribution chiDist = new ChiSquaredDistribution(chiData.getDegreesOfFreedom());
		double cumProb = chiDist.cumulativeProbability(chiData.getChi2());
//		for (int i = 0; i < 4; i++) {
//			System.out.printf("Res %d = %3.5f, ", i, answer.get(i,  0));
//		}
//		System.out.printf(", Chi2 = %3.1f, p=%3.1f, ndf = %d\n", chiData.getChi2(), cumProb, chiData.getNdf());
				
		// error estimation
		try {
			/*
			 * Error estimation based on 
			 * https://www.math.umd.edu/~petersd/460/linsysterrn.pdf
			 * 
			 */
			double cond1 = leftMatrix.norm1()*leftInverse.norm1();
			double errorSscale = (rightPlusErrorMatrix.minus(rightMatrix)).norm1();
			errorSscale *= cond1;
			errorSscale /= rightMatrix.norm1();
//			System.out.printf("Error scale = %3.3f\n", errorSscale);
			
		}
		catch (Exception e) {
			System.out.println("Error2 in HyperbolicLocaliser.processTOADsPlane: " + e.getMessage());
		}
		
		// now need to rotate back from the plane that we're in to get back to the original position. 

		double[] pos2d = {centre.getCoordinate(0)+answer.get(0,0), 
				centre.getCoordinate(1)+answer.get(1, 0), 
				0};
		Matrix posMatrix = new Matrix(pos2d, 1);
		PamVector posVector = new PamVector(pos2d);
		/*
		 * The final answer consists of x and y, followed by distances to each of the first n-1 hydrophones, so can 
		 * also calculate the height above the plane from these data. 
		 */
		posVec = answer.getColumnPackedCopy();
		double[] heightData = new double[posVec.length-2];
		double totH = 0;
		double msH = 0;
		int nH = 0;
//		Debug.out.printf("Height for pos %s: ", posVector.toString());
		for (int i = 0; i < heightData.length; i++) {
			double D = posVec[i+2];
			double xy = posVector.sub(geom[i]).norm();// posvec already corrected for centre, so don't need here.  
			double z = Math.pow(D,2)-Math.pow(xy, 2);
			msH += z;
			if (z >= 0) {
				heightData[i] = Math.sqrt(z);
				totH += heightData[i];
				nH++;
			}
			else {
				heightData[i] = Double.NaN;
			}
			heightData[i] = z;
//			Debug.out.printf("%d:%3.1f->%3.1f,    ", i, D, xy);
		}
//		Debug.out.printf(", meanheight %3.1fm from %d or rms %3.1f\n", totH/nH, nH, Math.sqrt(msH/heightData.length));
		boolean haveHeight = false;
		if (nH > 0) {
			haveHeight = true;
			/**
			 * Have to decide what to do here. In principle we have two locations at +/- the height from the 
			 * plane. In some circumstances this means an ambiguity, in others it may not, e.g. if bottom or surface 
			 * array, so only one solution possible. Also for a very big array, this may be so small that it's not worth
			 * bothering with, and why have two solutions when you can have one !
			 * however, if an array was in a daft plane, there are genuinely two solutions so somehow we should reflect that.
			 * Might need to add options to Hyperbolic localiser asking what to do in case of vertical ambiguity.  
			 */
		}
		
		// rotate all back by axis coordinates to earth frame. 
		Matrix finalMatrix = posMatrix.times(axesMatrix);
		/*
		 * Above works, but I suspect I've got the maths wrong and it may not generalise for a non - horizontal plane array. 
		 */		
//		if (nH > 0) {
//		}
		
		
//		LatLong pos = geometry.getReferenceGPS().addDistanceMeters(centre.getCoordinate(0)+answer.get(0,0), 
//				centre.getCoordinate(1)+answer.get(1, 0), 
//				centre.getCoordinate(2));
		LatLong pos = geometry.getReferenceGPS().addDistanceMeters(finalMatrix.get(0,0), 
				finalMatrix.get(0, 1), 	finalMatrix.get(0, 2));
//		TargetMotionResult tmResult = new TargetMotionResult(geometry.getTimeMilliseconds(), null, pos, 0, 0);
		GroupLocResult glr = new GroupLocResult(pos, 0, 0);
		glr.setPerpendicularDistance(0.);
//		glr.setModel(model);
		HyperbolicGroupLocalisation groupLocalisation = new HyperbolicGroupLocalisation(groupDataUnit, glr);
		groupLocalisation.setPosVec(posVec);

		return groupLocalisation;
		
	}

	/**
	 * Called for a linear array, which will calculate an XY position which has rotational symetry around the 
	 * line of the array, returning a pair of coordinates. 
	 * @param groupDataUnit
	 * @param geometry
	 * @param toadInformation
	 * @return
	 */
	private AbstractLocalisation processTOADs2D(PamDataUnit groupDataUnit, SnapshotGeometry geometry, TOADInformation toadInformation) {
		/*
		 * In the linear case, the problem is reduced to an x.y two dimensional plane. However, our problem is that the array may 
		 * not (probably isn't) aligned with any axis, so we'll need to rotate absolutely everything into a line and then to do everything
		 * relative to that line. This system will deal with alignment in any direction. 
		 */
		double c = geometry.getCurrentArray().getSpeedOfSound();
		double cErr = geometry.getCurrentArray().getSpeedOfSoundError();
		PamVector arrayAxes = geometry.getArrayAxes()[0];
		PamVector[] geom = geometry.getGeometry();
		PamVector centre = geometry.getGeometricCentre();
		int[] hydrophones = toadInformation.getHydrophoneList();
		double xArray[] = new double[geom.length];
		for (int i = 0; i < geom.length; i++) {
			xArray[i] = geom[hydrophones[i]].sub(centre).dotProd(arrayAxes);
		}

		double[][] delays = toadInformation.getToadSeconds();
		double[][] delayErrors = toadInformation.getToadErrorsSeconds();
		int nPhones = delays.length;
		int nPairs = nPhones*(nPhones-1)/2;
		boolean[] goodRow = new boolean[nPairs];
		int nCol = 1+nPhones-1;
		double[][] left = new double[nPairs][nCol];
		boolean[] goodCol = new boolean[nCol];
		double[][] right = new double[nPairs][1];
		double[][] rightPlusErr = new double[nPairs][1];
		double[] scale = new double[nPairs];
		int iPair = 0;
		int nGoodRows = 0;
		double delayErr;
		for (int i = 0; i < nPhones; i++) {
			for (int j = i+1; j < nPhones; j++, iPair++) {
				double delay = delays[i][j];
				PamVector geomi = geom[hydrophones[i]];
				PamVector geomj = geom[hydrophones[j]];
				if (Double.isNaN(delay) || geomi == null || geomj == null) {
					continue;
				}
				delay *= c;
				right[iPair][0] = Math.pow(delay, 2);
				if (delayErrors != null) {
					delayErr = delayErrors[i][j]*c;
				}
				else {
					delayErr = 0;
				}
				rightPlusErr[iPair][0] = Math.pow(delay+delayErr, 2);
				double l = 0;
//				for (int d = 0; d < 3; d++) {
					l = xArray[i]-xArray[j];
					left[iPair][0] = xArray[i]-xArray[j];
					right[iPair][0] += xArray[i]*xArray[i] - xArray[j]*xArray[j];
					rightPlusErr[iPair][0] += xArray[i]*xArray[i] - xArray[j]*xArray[j];
					scale[iPair] += Math.pow(l, 2);
//				}
				if (l == 0) {
					// check that the two hydrophones are not at the same point in space. 
					continue;
				}
				goodRow[iPair] = true;//iPair < 3;
				nGoodRows ++;
				left[iPair][1+i] = delay;
				right[iPair][0] *= 0.5;
				rightPlusErr[iPair][0] *= 0.5;
			}
//			break;
		}
//		nGoodRows = 3;

		// reduce rows if necessary. 
		double[][] goodLeft = left;
		double[][] goodRight = right;
		double[][] goodRightError = rightPlusErr;
		double[] goodScale = scale;
		if (nGoodRows < nPairs) {
			goodLeft = new double[nGoodRows][];
			goodRight = new double[nGoodRows][];
			goodRightError = new double[nGoodRows][];
			goodScale = new double[nGoodRows];
			int iG = 0;
			for (int i = 0; i < nPairs; i++) {
				if (goodRow[i] == false) {
					continue;
				}
				goodLeft[iG] = left[i];
				goodRight[iG] = right[i];
				goodRightError[iG] = rightPlusErr[i];
				goodScale[iG] = scale[i];
				iG++;
			}
		}
		boolean weight = false;
		if (weight) {
			for (int i = 0; i < goodLeft.length; i++) {
				double w = goodScale[i];
				if (w <= 0) continue;
				w = 1./Math.sqrt(w);
				w = 1./w;
//				w = 1000.;
				for (int j = 0; j < goodLeft[i].length; j++) {
					goodLeft[i][j] *= w;
				}
				goodRightError[i][0] *= w;
				goodRight[i][0] *= w;
			}
		}
		Matrix leftMatrix = new Matrix(goodLeft);
		Matrix rightMatrix = new Matrix(goodRight);
		Matrix rightPlusErrorMatrix = new Matrix(goodRightError);

//		Matrix answer = leftMatrix.solve(rightMatrix);
		Matrix answer = null, leftInverse = null;
		try {
//			answer = leftMatrix.solve(rightMatrix);
			leftInverse = leftMatrix.inverse();
			answer = leftInverse.times(rightMatrix);
		}
		catch (Exception e) {
//			System.out.println(e.getMessage());
			return null;
		}
		// get a chi2 value for this location. 
		double[] posVec = answer.getColumnPackedCopy();
		int nY = posVec.length-1;
		double[] ys = new double[nY];
		double y = 0;
		boolean haveY = false;
		for (int i = 0; i < 1; i++) {
			y = Math.pow(posVec[1+i],2)-Math.pow(posVec[0]-xArray[i], 2);
			if (y < 0) {
				System.out.println("Impossible result (complex y coordinate) for 2D hyperbolic localiser");
				//							return null;
			}
			else {
				y = Math.sqrt(y);
				haveY = true;
				break;
			}
			ys[i] = y;
		}
//		y = ys[0];
		if (!haveY) {
			return null;
		}
//		posVec = Arrays.copyOf(posVec, 3);
		lastPosVector = posVec;
		double[][][] dE = {delays, delayErrors};
		Chi2Data chiData = calcChi22D(xArray, toadInformation, c, posVec[0], y);
		
		// now try to solve for the error. 
//		ChiSquaredDistribution chiDist = new ChiSquaredDistribution(chiData.getNdf());
//		double cumProb = chiDist.cumulativeProbability(chiData.getChi2());
//		for (int i = 0; i < posVec.length; i++) {
//			System.out.printf("Res %d = %3.5f, ", i, posVec[i]);
//		}
//		System.out.printf(", y=%3.1fm, Chi2 = %3.1f, p=%3.1f, ndf = %d\n", y, chiData.getChi2(), cumProb, chiData.getNdf());
				
		// error estimation
		double errorSscale = 0;
		try {
			/*
			 * Error estimation based on 
			 * https://www.math.umd.edu/~petersd/460/linsysterrn.pdf
			 * 
			 */
			double cond1 = leftMatrix.norm1()*leftInverse.norm1();
			errorSscale = (rightPlusErrorMatrix.minus(rightMatrix)).norm1();
			errorSscale *= cond1;
			errorSscale /= rightMatrix.norm1();
//			System.out.printf("Error scale = %3.3f\n", errorSscale);
			
		}
		catch (Exception e) {
//			System.out.println(e.getMessage());
			return null;
		}
		// now based on the x and y and on the angle of the vector, work out 
		// two possible locations...
		// probably nicer just to make this an angle and range since it's ambiguous !
		// that means that this type of localiser needs to get a lot more abstract in what 
		// it returns !
		// for now see if we can get the maths right to just add a location with a latlong 
		// using existing framework. 
		LatLong l = geometry.getReferenceGPS();
		double x = posVec[0];// - xArray[hydrophones[0]];
		double angle = Math.atan2(y, x);
//		PamVector v = geom[geom.length-1].sub(geom[0]);
//		double arrayHead = Math.atan2(v.getCoordinate(1), v.getCoordinate(0));
		double r = Math.sqrt(y*y+x*x);
//		double xErr = 
		
		
//		double totAng = Math.toDegrees(arrayHead + angle);
//		LatLong p = l.travelDistanceMeters(90-totAng, r);
//		GroupLocResult glr = new GroupLocResult(p, 0, chiData.getChi2());
//		glr.setPerpendicularDistance(y);
//		GroupLocalisation groupLocalisation = new GroupLocalisation(groupDataUnit, glr);
//		totAng = Math.toDegrees(arrayHead - angle);
//		p = l.travelDistanceMeters(90-totAng, r);
//		glr = new GroupLocResult(p, 0, chiData.getChi2());
//		glr.setPerpendicularDistance(y);
//		groupLocalisation.addGroupLocaResult(glr);
////		glr.setModel(model);
//		return groupLocalisation;
		LinearLocalisation linLoc = new LinearLocalisation(groupDataUnit, toadInformation.getHydrophoneMap(), geometry.getArrayAxes(), angle, r);
		GpsData refPos = new GpsData(geometry.getReferenceGPS().addDistanceMeters(geometry.getGeometricCentre()));
		linLoc.setReferencePosition(refPos);
		return linLoc;
	}

	/**
	 * 2D chi2 calculation for the linear array hyperbolic localiser. 
	 * @param x
	 * @param toadInformation
	 * @param y2 
	 * @param posVec
	 * @return
	 */
	private Chi2Data calcChi22D(double[] phonePos, TOADInformation toadInformation, double c, double x, double y) {
		double[] dists = new double[phonePos.length];
		for (int i = 0; i < dists.length; i++) {
			dists[i] = Math.sqrt(Math.pow(x-phonePos[i],2) + y*y) / c;
		}
		double[][] toads = toadInformation.getToadSeconds();
		double[][] toadErrors = toadInformation.getToadErrorsSeconds();
		int nGood = 0;
		int n = toads.length;
		double chiTot = 0.;
		for (int i = 0; i < n; i++) {
			for (int j = i+1; j < n; j++) {
				double exp = dists[j]-dists[i];
				double ch = Math.pow((toads[i][j]-exp)/toadErrors[i][j], 2);
				if (Double.isNaN(c)) {
					continue;
				}
				chiTot += ch;
				nGood++;
			}
		}
		
		
		return new Chi2Data(chiTot, nGood-2);
	}

	private HyperbolicGroupLocalisation processTOADs3D(PamDataUnit groupDataUnit, SnapshotGeometry geometry, TOADInformation toadInformation) {

		// follow formulation in http://ieeexplore.ieee.org/document/4418389/#full-text-section

//		for (int i = 0; i < delays.length; i++) {
//			for (int j = 0; j < delays.length; j++) {
//				System.out.printf("%6.3f\t", delays[i][j]);
//			}
//			System.out.printf("\n");
//		}
		lastPosVector = null;

		double c = geometry.getCurrentArray().getSpeedOfSound();
		double cErr = geometry.getCurrentArray().getSpeedOfSoundError();
		
		PamVector[] geom = geometry.getGeometry();
		PamVector centre = geometry.getGeometricCentre();
		// note that this is a maximum - we may have to reduce the number of rows if 
		// some are badly populated. 
		double[][] delays = toadInformation.getToadSeconds();
		double[][] delayErrors = toadInformation.getToadErrorsSeconds();
		int[] hydrophones = toadInformation.getHydrophoneList();
		int nPhones = delays.length;
		int nPairs = nPhones*(nPhones-1)/2;
		boolean[] goodRow = new boolean[nPairs];
		int nCols = 3+nPhones-1;
		double[][] left = new double[nPairs][nCols];
		double[][] right = new double[nPairs][1];
		double[][] rightPlusErr = new double[nPairs][1];
		double[] scale = new double[nPairs];
		int iPair = 0;
		int nGoodRows = 0;
		double delayErr;
		for (int i = 0; i < nPhones; i++) {
			for (int j = i+1; j < nPhones; j++, iPair++) {
				double delay = -delays[i][j];
				PamVector geomi = geom[hydrophones[i]];
				PamVector geomj = geom[hydrophones[j]];
				if (Double.isNaN(delay) || geomi == null || geomj == null) {
					continue;
				}
				delay *= c;
				right[iPair][0] = Math.pow(delay, 2);
				if (delayErrors != null) {
					delayErr = delayErrors[i][j]*c;
				}
				else {
					delayErr = 0;
				}
				rightPlusErr[iPair][0] = Math.pow(delay+delayErr, 2);
				double l = 0;
				for (int d = 0; d < 3; d++) {
					l += Math.abs(geomi.getCoordinate(d)-geomj.getCoordinate(d));
					left[iPair][d] = geomi.getCoordinate(d)-geomj.getCoordinate(d);
					right[iPair][0] += (Math.pow(geomi.getCoordinate(d)-centre.getCoordinate(d), 2) - Math.pow(geomj.getCoordinate(d)-centre.getCoordinate(d), 2));
					rightPlusErr[iPair][0] += (Math.pow(geomi.getCoordinate(d)-centre.getCoordinate(d), 2) - Math.pow(geomj.getCoordinate(d)-centre.getCoordinate(d), 2));
					scale[iPair] += Math.pow(geomi.getCoordinate(d)-geomj.getCoordinate(d), 2);
				}
				if (l == 0) {
					// check that the two hydrophones are not at the same point in space. 
					continue;
				}
				goodRow[iPair] = true;
				nGoodRows ++;
				left[iPair][3+i] = delay;
				right[iPair][0] *= 0.5;
				rightPlusErr[iPair][0] *= 0.5;
			}
//			break;
		}
		// reduce rows if necessary. 
		double[][] goodLeft = left;
		double[][] goodRight = right;
		double[][] goodRightError = rightPlusErr;
		double[] goodScale = scale;
		if (nGoodRows < nPairs) {
			goodLeft = new double[nGoodRows][];
			goodRight = new double[nGoodRows][];
			goodRightError = new double[nGoodRows][];
			goodScale = new double[nGoodRows];
			int iG = 0;
			for (int i = 0; i < nPairs; i++) {
				if (goodRow[i] == false) {
					continue;
				}
				goodLeft[iG] = left[i];
				goodRight[iG] = right[i];
				goodRightError[iG] = rightPlusErr[i];
				goodScale[iG] = scale[i];
				iG++;
			}
		}
		boolean weight = true;
		if (weight) {
			for (int i = 0; i < goodLeft.length; i++) {
				double w = goodScale[i];
				if (w <= 0) continue;
				w = 1./Math.sqrt(w);
				w = 1./w;
//				w = 1000.;
				for (int j = 0; j < goodLeft[i].length; j++) {
					goodLeft[i][j] *= w;
				}
				goodRightError[i][0] *= w;
				goodRight[i][0] *= w;
			}
		}
		// now check that every column has something in it ... ?? zero is a valid number !
		
		Matrix leftMatrix = new Matrix(goodLeft);
		Matrix rightMatrix = new Matrix(goodRight);
		Matrix rightPlusErrorMatrix = new Matrix(goodRightError);
		
//		Matrix answer = leftMatrix.solve(rightMatrix);
		Matrix answer = null, leftInverse = null;
		try {
			leftInverse = leftMatrix.inverse();
			answer = leftInverse.times(rightMatrix);
		}
		catch (Exception e) {
//			System.out.println(e.getMessage());
//			Matrix m = leftMatrix.transpose();
//			m = m.inverse();
			System.out.println("HyperbolicLoclaiser: localisation failed");
			return null;
		}
		// get a chi2 value for this location. 
		double[] posVec = answer.getColumnPackedCopy();
		posVec = Arrays.copyOf(posVec, 3);
		lastPosVector = posVec;
		double[][][] dE = {delays, delayErrors};
		Chi2Data chiData = calcChi2(geometry, toadInformation, posVec);
		if (chiData.getDegreesOfFreedom() <= 0) {
			return null;
		}
		ChiSquaredDistribution chiDist = new ChiSquaredDistribution(chiData.getDegreesOfFreedom());
		double cumProb = chiDist.cumulativeProbability(chiData.getChi2());
//		for (int i = 0; i < 4; i++) {
//			System.out.printf("Res %d = %3.5f, ", i, answer.get(i,  0));
//		}
//		System.out.printf(", Chi2 = %3.1f, p=%3.1f, ndf = %d\n", chiData.getChi2(), cumProb, chiData.getNdf());
		
				
//		// error estimation
//		try {
//			/*
//			 * Error estimation based on 
//			 * https://www.math.umd.edu/~petersd/460/linsysterrn.pdf
//			 * 
//			 */
//			double cond1 = leftMatrix.norm1()*leftInverse.norm1();
//			double errorSscale = (rightPlusErrorMatrix.minus(rightMatrix)).norm1();
//			errorSscale *= cond1;
//			errorSscale /= rightMatrix.norm1();
//			//System.out.printf("Error scale = %3.3f\n", errorSscale);
//			
//		}
//		catch (Exception e) {
//			System.out.println("Error in HyperbolicLocaliser.processTOADs3D: " + e.getMessage());
//		}
		
		LatLong pos = geometry.getReferenceGPS().addDistanceMeters(centre.getCoordinate(0)+answer.get(0,0), 
				centre.getCoordinate(1)+answer.get(1, 0), 
				centre.getCoordinate(2)+answer.get(2, 0));
//		TargetMotionResult tmResult = new TargetMotionResult(geometry.getTimeMilliseconds(), null, pos, 0, 0);
		GroupLocResult glr = new GroupLocResult(pos, 0, 0);
		glr.setPerpendicularDistance(0.);
		glr.setModel(this);
		int nToads = countUsableTOADS(toadInformation);
		int nDF = nToads-3;
		glr.setnDegreesFreedom(nDF);
		glr.setDim(3);
		glr.setBeamLatLong(geometry.getReferenceGPS());
		glr.setBeamTime(groupDataUnit.getTimeMilliseconds());
		glr.setAic(chiData.getChi2()-6);
//		glr.setModel(model);
		HyperbolicGroupLocalisation groupLocalisation = new HyperbolicGroupLocalisation(groupDataUnit, glr);
		groupLocalisation.setPosVec(posVec);
		
		//System.out.println("HyperbolicLoclaiser: Lat long: " + groupLocalisation.getLatLong(0) + "  " + groupLocalisation.hasLocContent(LocContents.HAS_LATLONG) );

		
		return groupLocalisation;
	}
	
	/**
	 * Calculate and add the errors in source position. This is achieved by sampling a random number from the time delay error distributions, localising and looking at the distribution 
	 * in position of localisation results.
	 * @param loclaisation - the localisation result.  
	 * @param timeDelays - object containing time delay data. 
	 * @param hydrophonePos - the hydrophone positions. 
	 * @param params - hyperbolic params object with settings for error calculations.  
	 * @return the loclaisation object iwth errors added. 
	 */
	public GroupLocResult calcErrors(GroupLocResult loclaisation, PamDataUnit groupDataUnit, SnapshotGeometry geometry, TOADInformation toadInformation,   HyperbolicParams params){
		 double[] errors = calcErrors( groupDataUnit,  geometry,  toadInformation ,  params);
		 loclaisation.setError(new SimpleError(errors[0], errors[1], errors[2],geometry.getArrayAngles()[0])); 
		 return loclaisation; 
	}

	
	
	/**
	 * Calculate the errors in source position. This is achieved by sampling a random number from the time delay error distributions, localising and looking at the distribution 
	 * in position of localisation results. 
	 * @param timeDelays - object containing time delay data. 
	 * @param hydrophonePos - the hydrophone positions. 
	 * @param params - hyperbolic params object with settings for error calculations.  
	 * @return the errors in meters
	 */
	public double[] calcErrors(PamDataUnit groupDataUnit, SnapshotGeometry geometry, TOADInformation toadInformation , HyperbolicParams params){
		
		Random r = new Random(); 
		double[][] sourcePositions=new double[params.bootStrapN][3];
		
		TOADInformation errToadInformation;
		HyperbolicGroupLocalisation errLoc;
		for (int i=0; i<params.bootStrapN; i++){
			//create new time delays from errors 
			ArrayList<Double> timeDelaysJitter=new ArrayList<Double>();
			double error; 
			
			errToadInformation = (TOADInformation) toadInformation.clone();

			//generate the new time delay errors. 
			for (int j=0; j<toadInformation.getToadErrorsSeconds().length ; j++){
				for (int jj=0; jj<toadInformation.getToadErrorsSeconds()[j].length ; jj++){
					//generate random Gaussina 
					error=r.nextGaussian()*toadInformation.getToadErrorsSeconds()[j][jj]; 
					//				//add cross correlation error
					//				error=Math.sqrt(Math.pow(error,2)+Math.pow((r.nextGaussian()*this.hyperbolicParams.crossCorrError)/sampleRate,2));

					//now add that error to the time delay
					errToadInformation.getToadSeconds()[j][jj] = toadInformation.getToadSeconds()[j][jj]+error;
				}
			}
			
			double[] posVec = null; 
			switch (geometry.getShape()) {
			case ArrayManager.ARRAY_TYPE_LINE:
				//System.out.println("HyperbolicLocaliser: Line");
				//TODO
			case ArrayManager.ARRAY_TYPE_PLANE:
				//System.out.println("HyperbolicLocaliser: Plane");
				errLoc = processTOADsPlane(groupDataUnit, geometry, toadInformation);
				posVec = errLoc.getPosVec(); 

			case ArrayManager.ARRAY_TYPE_VOLUME:
				//System.out.println("HyperbolicLocaliser: Volume");

				errLoc=  processTOADs3D(groupDataUnit, geometry, toadInformation);
				posVec = errLoc.getPosVec(); 

			}
			
			sourcePositions[i]= posVec;
		}
		
		//now take the standard deviation of all the measurements
		double[] errors=PamArrayUtils.std(sourcePositions); 
		
		return errors; 
	}

	@Override
	public String getName() {
		return "Hyperbolic Localiser";
	}

	@Override
	public SQLLoggingAddon getSQLLoggingAddon(int arrayType) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the lastPosVector
	 */
	public double[] getLastPosVector() {
		return lastPosVector;
	}

	/* (non-Javadoc)
	 * @see Localiser.LocaliserModel#getToolTipText()
	 */
	@Override
	public String getToolTipText() {
		// TODO Auto-generated method stub
		return "Hyperbolic lest squares localisation";
	}

	/* (non-Javadoc)
	 * @see Localiser.LocaliserModel#getLocContents()
	 */
	@Override
	public LocContents getLocContents() {
		return locContents ;
	}

	/* (non-Javadoc)
	 * @see Localiser.LocaliserModel#getSettingsPane()
	 */
	@Override
	public LocaliserPane getAlgorithmSettingsPane() {
		if (hyperbolicLocaliserPane == null) {
			hyperbolicLocaliserPane= new HyperbolicSettingsPane(); 
		}
		return hyperbolicLocaliserPane;
	}

	/* (non-Javadoc)
	 * @see Localiser.LocaliserModel#hasParams()
	 */
	@Override
	public boolean hasParams() {
		return true;
	}

	/* (non-Javadoc)
	 * @see Localiser.LocaliserModel#notifyModelProgress(double)
	 */
	@Override
	public void notifyModelProgress(double progress) {
		// TODO Auto-generated method stub
		
	}
	

	

}
