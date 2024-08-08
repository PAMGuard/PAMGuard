package Localiser.algorithms.timeDelayLocalisers.bearingLoc;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealConvergenceChecker;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.direct.NelderMead;

import Array.ArrayManager;
import Array.PamArray;
import Jama.Matrix;
import Jama.QRDecomposition;
import Localiser.LocalisationAlgorithmInfo;
import PamDetection.LocContents;
import PamUtils.PamUtils;
import pamMaths.PamVector;

@Deprecated
public class SimplexBearingLocaliser implements BearingLocaliser {
	private int arrayType;
	private Matrix hydrophoneVectors;
	private Matrix hydrophoneErrorVectors;
	private Matrix hydrophoneUnitVectors;
	private double[] hydrophoneSpacing;
	private PamArray currentArray;
	private int hydrophoneBitMap;
	private long timeMillis;
	private double timingError;
	private PamVector[] arrayAxis;
	private QRDecomposition qrHydrophones;
	private double[] startValue = {0, Math.PI/2};
	private double[] firstStep;
	private NelderMead optimiser;

	public SimplexBearingLocaliser(int hydrophoneBitMap, long timeMillis, double timingError) {
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

		hydrophoneBitMap = PamUtils.makeChannelMap(arrayElements); 
		ArrayManager arrayManager = ArrayManager.getArrayManager();
		currentArray = arrayManager.getCurrentArray();
		arrayType = arrayManager.getArrayShape(currentArray, hydrophoneBitMap);

		arrayAxis = arrayManager.getArrayDirections(currentArray, hydrophoneBitMap);
		/*  This will actually rotate everything so that x is the principle axis even 
		 *  though y will nearly always the be principle axis for forward towing hydrophones, 
		 *  but this doesn't matter. When it comes to the theta and phi calculations, make
		 *  sure that they are calculated relative to the x axis and the xy plane.  
		 */
		PamVector[] rotVectors = Arrays.copyOf(arrayAxis, 3);
		// may need to work out the third vector.
		if (arrayType == ArrayManager.ARRAY_TYPE_PLANE) {
			rotVectors[2] = rotVectors[0].vecProd(rotVectors[1]);
		}
		Matrix rotMatrix = PamVector.arrayToMatrix(rotVectors);
		
		int nHyd = arrayElements.length;
		int nDelay = (nHyd*(nHyd-1))/2;
		hydrophoneVectors = new Matrix(nDelay, 3);
		hydrophoneErrorVectors = new Matrix(nDelay, 3);
		hydrophoneUnitVectors = new Matrix(nDelay, 3);
		hydrophoneSpacing = new double[nDelay];
		int iRow = 0;
		double c = currentArray.getSpeedOfSound();
		for (int i = 0; i < nHyd; i++) {
			PamVector vi = currentArray.getAbsHydrophoneVector(i, timeMillis);
			for (int j = i+1; j <nHyd; j++) {
				PamVector vj = currentArray.getAbsHydrophoneVector(j, timeMillis);
				PamVector v = vj.sub(vi).rotate(rotMatrix);
				PamVector errorVec = currentArray.getSeparationErrorVector(i, j, timeMillis).rotate(rotMatrix);
				hydrophoneSpacing[iRow] = v.norm();
				PamVector uv = v.getUnitVector();
				for (int e = 0; e < 3; e++) {
					hydrophoneVectors.set(iRow, e, v.getElement(e)/c);
					hydrophoneErrorVectors.set(iRow, e, errorVec.getElement(e)/c);
					hydrophoneUnitVectors.set(iRow, e, uv.getElement(e));
				}
				iRow++;
			}
		}
//		luHydrophoneUnitMatrix = new LUDecomposition(hydrophoneUnitVectors);
		qrHydrophones = new QRDecomposition(hydrophoneUnitVectors);
		

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
		
		BearingChi2 bearingChi2 = new BearingChi2(delays);
		if (optimiser == null) {
			optimiser = new NelderMead();
			
//			optimiser.setMaxIterations(20);
		}
		double[] start = startValue;
		if (start == null) {
			start = new double[2];
			start[1] = Math.PI/2;
		}
		RealPointValuePair result = null;
		if (firstStep == null) {
			firstStep = new double[2];
			firstStep[0] = firstStep[1] = .1;
		}
//		optimiser.setStartConfiguration(firstStep);
//		optimiser.setConvergenceChecker(new BearingConvergence());
//		optimiser.
		try {
			 result = optimiser.optimize(bearingChi2, GoalType.MINIMIZE, start);
		} catch (OptimizationException | FunctionEvaluationException
				| IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		}
		double[][] angles = new double[2][2];
		angles[0] = result.getPoint();
//		System.out.printf("Optimiser took %d steps to reach chi2 = %3.1f angles %3.1f,%3.1f\n", 
//				optimiser.getIterations(), result.getValue(),
//				angles[0][0]*180./Math.PI, angles[0][1]*180./Math.PI);
		return angles;
	}

	/**
	 * Chis2 calculation takes an angle pair and converts to a vector 
	 * before estimating the chi2.
	 * @author Doug
	 *
	 */
	private class BearingChi2 implements MultivariateRealFunction {

		private Matrix whaleVec;
		
		private double[] delays;

		public BearingChi2(double[] delays) {
			this.delays = delays;
			whaleVec = new Matrix(3,1);
		}

		@Override
		public double value(double[] angles) throws FunctionEvaluationException,
				IllegalArgumentException {
			whaleVec.set(0, 0, Math.cos(angles[1])*Math.cos(angles[0]));
			whaleVec.set(1, 0, Math.cos(angles[1])*Math.sin(angles[0]));
			whaleVec.set(2, 0, Math.sin(angles[1]));
			Matrix times = hydrophoneVectors.times(whaleVec);
			Matrix timeErrors = hydrophoneErrorVectors.times(whaleVec);
			double c = currentArray.getSpeedOfSound();
			double dc = currentArray.getSpeedOfSoundError();
			Matrix timeErrors2 = times.times(dc/c/c);
			double chi = 0;
			for (int i = 0; i < times.getRowDimension(); i++) {
				double expectedVariance = Math.pow(timeErrors.get(i, 0),2) + Math.pow(timeErrors2.get(i, 0),2) + Math.pow(timingError, 2); 
				chi += Math.pow((times.get(i, 0)+delays[i]), 2)/expectedVariance;
			}
			return chi;
		}
		
	}
	
	public double getChiVal(double[] delays, double[] angles) {
		BearingChi2 c = new BearingChi2(delays);
		try {
			return c.value(angles);
		} catch (FunctionEvaluationException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * @param startValue the startValue to set
	 */
	public void setStartValue(double[] startValue) {
		this.startValue = startValue;
	}

	/**
	 * @return the firstStep
	 */
	public double[] getFirstStep() {
		return firstStep;
	}

	/**
	 * @param firstStep the firstStep to set
	 */
	public void setFirstStep(double[] firstStep) {
		this.firstStep = firstStep;
	}

	@Override
	public String getAlgorithmName() {
		return "Simplex bearing localiser";
	}

	@Override
	public LocalisationAlgorithmInfo getAlgorithmInfo() {
		return this;
	}

	@Override
	public Serializable getParameters() {
		return null;
	}
//	private class BearingConvergence implements RealConvergenceChecker {
//
//		@Override
//		public boolean converged(int arg0, RealPointValuePair point0,
//				RealPointValuePair point1) {
//			double maxDiff = 0;
//			for (int i = 0; i < 2; i++) {
//				maxDiff = Math.max(maxDiff, Math.abs(point0.getPoint()[i]-point1.getPoint()[i]));
//			}
//			return maxDiff < Math.PI / 2 /1000;
//		}
//		
//	}
}
