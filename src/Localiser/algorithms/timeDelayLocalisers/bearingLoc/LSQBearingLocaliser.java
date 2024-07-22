package Localiser.algorithms.timeDelayLocalisers.bearingLoc;

import Array.ArrayManager;
import Array.PamArray;
import Jama.LUDecomposition;
import Jama.Matrix;
import Jama.QRDecomposition;
import PamDetection.LocContents;
import PamUtils.PamUtils;
import pamMaths.PamVector;

public class LSQBearingLocaliser implements BearingLocaliser {

	private int hydrophoneBitMap;
	private long timeMillis;
	private double timingError;
	
	private Matrix weightedHydrophoneVectors;
	private Matrix hydrophoneVectors;
	private Matrix hydrophoneErrorVectors;
	private double[] hydrophoneSpacing;
	private PamArray currentArray;
	private int arrayType;
	private PamVector[] arrayAxis;
	private QRDecomposition qrHydrophones;
	private double[] fitWeights;

	public LSQBearingLocaliser(int hydrophoneBitMap, long timeMillis, double timingError) {
		this.hydrophoneBitMap = hydrophoneBitMap;
		this.timeMillis = timeMillis;
		this.timingError = timingError;
	}

	@Override
	public int getLocalisationContents() {
		return LocContents.HAS_BEARING | LocContents.HAS_AMBIGUITY;
	}
	
	@Override
	public void prepare(int[] arrayElements, long timeMillis, double timingError) {
		/*
		 * Set up the matrixes of inter hydrophone vectors. 
		 */
		this.timingError = timingError;

		hydrophoneBitMap = PamUtils.makeChannelMap(arrayElements); 
		ArrayManager arrayManager = ArrayManager.getArrayManager();
		currentArray = arrayManager.getCurrentArray();
		arrayType = arrayManager.getArrayShape(currentArray, hydrophoneBitMap);

		arrayAxis = arrayManager.getArrayDirections(currentArray, hydrophoneBitMap);
		
		int nHyd = arrayElements.length;
		int nDelay = (nHyd*(nHyd-1))/2;
		weightedHydrophoneVectors = new Matrix(nDelay, 3);
		hydrophoneVectors = new Matrix(nDelay, 3);
		hydrophoneErrorVectors = new Matrix(nDelay, 3);
		hydrophoneSpacing = new double[nDelay];
		fitWeights = new double[nDelay];
		double c = currentArray.getSpeedOfSound();
		int iRow = 0;
		for (int i = 0; i < nHyd; i++) {
			PamVector vi = currentArray.getAbsHydrophoneVector(i, timeMillis);
			for (int j = i+1; j <nHyd; j++) {
				PamVector vj = currentArray.getAbsHydrophoneVector(j, timeMillis);
				PamVector v = vj.sub(vi);
				hydrophoneSpacing[iRow] = v.norm();
				PamVector uv = v.getUnitVector();
				PamVector errorVec = currentArray.getSeparationErrorVector(i, j, timeMillis);
				/*
				 * Get the error component along the line of the pair and then calculate
				 * the weight as 1/the variance of the hydrophone separation. 
				 */
				double errorComponent = uv.dotProd(errorVec);
				fitWeights[iRow] = Math.pow(v.norm()/errorComponent, 2);
				for (int e = 0; e < 3; e++) {
					weightedHydrophoneVectors.set(iRow, e, v.getElement(e)/c*fitWeights[iRow]);
					hydrophoneVectors.set(iRow, e, v.getElement(e)/c);
					hydrophoneErrorVectors.set(iRow, e, errorVec.getElement(e)/c);
//					hydrophoneUnitVectors.set(iRow, e, uv.getElement(e));
				}
				iRow++;
			}
		}
//		luHydrophoneUnitMatrix = new LUDecomposition(hydrophoneUnitVectors);
		qrHydrophones = new QRDecomposition(weightedHydrophoneVectors);
	}

	@Override
	public int getArrayType() {
		return arrayType;
	}

	@Override
	public int getHydrophoneMap() {
		return hydrophoneBitMap;
	}

	@Override
	public PamVector[] getArrayAxis() {
		return arrayAxis;
	}	 
	
	/* 
	 * @return true if a new grid needs to be created
	 */
	private boolean resetArray(long timeMillis){
		
		if (currentArray == null || (this.timeMillis!=timeMillis && currentArray.getHydrophoneLocator().isChangeable())){
			prepare(PamUtils.getChannelArray(hydrophoneBitMap), timeMillis, 1e-6);
			this.timeMillis = timeMillis;
			return true;
		}
			
		return false; 
	}

	@Override
	public double[][] localise(double[] delays, long timeMillis) {
		resetArray(timeMillis);
//		qrHydrophones = new QRDecomposition(hydrophoneVectors);
		Matrix normDelays = new Matrix(delays.length, 1);
		for (int i = 0; i < delays.length; i++) {
			normDelays.set(i, 0, -delays[i]*fitWeights[i]);
		}
//		Matrix soln = luHydrophoneUnitMatrix.solve(normDelays);
		Matrix soln2 = qrHydrophones.solve(normDelays);
		double[][] angs = new double[2][2];
		PamVector v = new PamVector(soln2.get(0, 0), soln2.get(1,0), soln2.get(2, 0));
//		System.out.printf("Vector Norm = %4.3f: ", v.norm());
		double m = v.normalise();
		angs[0][0] = Math.PI/2. - Math.atan2(v.getElement(0),v.getElement(1));
		angs[0][1] = Math.asin(v.getElement(2));
		
//		timingError = 1e-5;
		// now take a look at angle errors
		double oneDeg = Math.PI/180.;
		
		double testDeg = 5;
		double[][] er = new double[2][20];
		for (int i = 0; i < 20; i++) {
			testDeg = 1+i;
		// pick points testDeg degrees either side of angs and change one at a time
		double aDiff = testDeg * oneDeg;
		double a;
		double l1, l2, l3, l1a, l3a;
		l1 = logLikelihood(delays, angs[0][0] - aDiff, angs[0][1]);
		l2 = logLikelihood(delays, angs[0][0], angs[0][1]);
		l3 = logLikelihood(delays, angs[0][0] + aDiff, angs[0][1]);
		er[0][i] = angs[1][0] = Math.sqrt(1./(l1+l3-2*l2))*aDiff;
		l1a = logLikelihood(delays, angs[0][0], angs[0][1] - aDiff);
//		l2 = logLikelihood(delays, angs[0][0], angs[0][1]);
		l3a = logLikelihood(delays, angs[0][0], angs[0][1] + aDiff);
		er[1][i] = angs[1][1] = Math.sqrt(1./(l1a+l3a-2*l2))*aDiff;
		}
		
		
		
		
//		double ll[] = new double[21];
//		double a[] = new double[2];
//		timingError = 1.e-4;
//		a[1] = angs[0][1];
//		for (int i = 0; i < ll.length; i++) {
//			a[0] = angs[0][0] + (-10 + i)*oneDeg;
//			ll[i] = logLikelihood(delays, a);
//		}
		return angs;
	}
	/**
	 * Log likelihood function
	 * @param delays time delays
	 * @param angle0 horizontal angle
	 * @param angle1 vertical angle
	 * @return log likelihood based on an estimate of errors. 
	 */
	public double logLikelihood(double[] delays, double angle0, double angle1) {
		Matrix whaleVec = new Matrix(3,1);
		whaleVec.set(0, 0, Math.cos(angle1)*Math.cos(angle0));
		whaleVec.set(1, 0, Math.cos(angle1)*Math.sin(angle0));
		whaleVec.set(2, 0, Math.sin(angle1));
		return logLikelihood(delays, whaleVec);
	}
	
	/**
	 * Calculate a log likelihood for a given pair of angles
	 * @param delays actual delays
	 * @param angles angles, horizontal and vertical. 
	 * @return
	 */
	public double logLikelihood(double[] delays, double[] angles) {
		return logLikelihood(delays, angles[0], angles[1]);
//		Matrix whaleVec = new Matrix(3,1);
//		whaleVec.set(0, 0, Math.cos(angles[1])*Math.cos(angles[0]));
//		whaleVec.set(1, 0, Math.cos(angles[1])*Math.sin(angles[0]));
//		whaleVec.set(2, 0, Math.sin(angles[1]));
//		return logLikelihood(delays, whaleVec);
	}
	/**
	 * Calculate a log likelihood for a given whale vector. 
	 * @param delays actual delays
	 * @param whaleVector estimated whale vector. 
	 * @return
	 */
	public double logLikelihood(double[] delays, Matrix whaleVector) {
		Matrix times = hydrophoneVectors.times(whaleVector); // expected times for this whale position (note vecs are already divided by c)
		Matrix timeErrors = hydrophoneErrorVectors.times(whaleVector);
		double c = currentArray.getSpeedOfSound();
		double dc = currentArray.getSpeedOfSoundError();
		Matrix timeErrors2 = times.times(dc/c/c);
		double chi = 0;
		for (int i = 0; i < times.getRowDimension(); i++) {
			double expectedVariance = Math.pow(timeErrors.get(i, 0),2) + Math.pow(timeErrors2.get(i, 0),2) + Math.pow(timingError, 2); 
			chi = Math.pow((times.get(i, 0)+delays[i]), 2)/expectedVariance;
		}
		return chi/2;
	}

}
