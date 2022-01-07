package Localiser.algorithms.timeDelayLocalisers.bearingLoc;

import java.util.Arrays;

import Array.ArrayManager;
import Array.PamArray;
import Jama.Matrix;
import Jama.QRDecomposition;
import PamUtils.PamUtils;
import pamMaths.PamVector;

/**
 * Really simple BearingLocaliser which works with two element closely
 * spaced arrays. 
 * @author Doug Gillespie
 *
 */
public class PairBearingLocaliser implements BearingLocaliser {

	public class LSQBearingLocaliser implements BearingLocaliser {
	
		private int hydrophoneBitMap;
		private long timeMillis;
		private double timingError;
		
		private Matrix hydrophoneVectors, hydrophoneUnitVectors;
		private double[] hydrophoneSpacing;
		private PamArray currentArray;
		private int arrayType;
		private PamVector[] arrayAxis;
	//	private LUDecomposition luHydrophoneUnitMatrix;
		private QRDecomposition qrHydrophones;
	
		public LSQBearingLocaliser(int hydrophoneBitMap, long timeMillis, double timingError) {
			this.hydrophoneBitMap = hydrophoneBitMap;
			this.timeMillis = timeMillis;
			this.timingError = timingError;
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
			
			int nHyd = arrayElements.length;
			int nDelay = (nHyd*(nHyd-1))/2;
			hydrophoneVectors = new Matrix(nDelay, 3);
			hydrophoneUnitVectors = new Matrix(nDelay, 3);
			hydrophoneSpacing = new double[nDelay];
			int iRow = 0;
			for (int i = 0; i < nHyd; i++) {
				PamVector vi = currentArray.getAbsHydrophoneVector(i, timeMillis);
				for (int j = i+1; j <nHyd; j++) {
					PamVector vj = currentArray.getAbsHydrophoneVector(j, timeMillis);
					PamVector v = vj.sub(vi);
					hydrophoneSpacing[iRow] = v.norm();
					PamVector uv = v.getUnitVector();
					for (int e = 0; e < 3; e++) {
						hydrophoneVectors.set(iRow, e, v.getElement(e));
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
			Matrix normDelays = new Matrix(delays.length, 1);
			double c = currentArray.getSpeedOfSound();
			for (int i = 0; i < delays.length; i++) {
				normDelays.set(i, 0, -delays[i]*c/hydrophoneSpacing[i]);
			}
	//		Matrix soln = luHydrophoneUnitMatrix.solve(normDelays);
			Matrix soln2 = qrHydrophones.solve(normDelays);
			double[][] angs = new double[2][2];
			PamVector v = new PamVector(soln2.get(0, 0), soln2.get(1,0), soln2.get(2, 0));
			double m = v.normalise();
			angs[0][0] = Math.PI/2. - Math.atan2(v.getElement(0),v.getElement(1));
			angs[0][1] = Math.asin(v.getElement(2));
			return angs;
		}
	
	}

	private int[] phoneNumbers;
	
	private double spacing, spacingError;
	
	private double speedOfSound, speedOfSoundError;
	
	private PamArray currentArray;
	
	private int[] arrayElements;

	private PamVector[] arrayAxis;

	private int arrayType;
	
	private double timingError;

	private double wobbleRadians;

	private long timeMillis;

	private int hydrophoneMap;

	
	public PairBearingLocaliser(int hydrophoneBitMap, long timeMillis, double timingError) {
		prepare(PamUtils.getChannelArray(hydrophoneBitMap),timeMillis, timingError);
	}
	
	public PairBearingLocaliser(int[] hydrophoneList,long  timeMillis, double timingError) {
		prepare(hydrophoneList,timeMillis, timingError);
	}
	
	
	@Override
	public void prepare(int[] arrayElements, long timeMillis, double timingError) {
		
		this.arrayElements=arrayElements;
		this.timingError = timingError;
		this.timeMillis =timeMillis; 
		
		if (arrayElements.length != 2) {
			System.out.println("Attempt to use PairBearingLocaliser with less than or greater than two elements");
		}
		phoneNumbers = Arrays.copyOf(arrayElements, 2);
		hydrophoneMap = PamUtils.makeChannelMap(phoneNumbers); 
		ArrayManager arrayManager = ArrayManager.getArrayManager();
		currentArray = arrayManager.getCurrentArray();
		arrayType = arrayManager.getArrayShape(currentArray, hydrophoneMap);
		if (arrayType != ArrayManager.ARRAY_TYPE_LINE) {
			System.out.println("The Hydrophones to not lie in a line, but are a " 
					+ ArrayManager.getArrayTypeString(arrayType));
		}
		spacing = currentArray.getSeparation(phoneNumbers[0], phoneNumbers[1], timeMillis);
		spacingError = currentArray.getSeparationError(phoneNumbers[0], phoneNumbers[1],timeMillis);
		wobbleRadians = currentArray.getWobbleRadians(phoneNumbers[0], phoneNumbers[1],timeMillis);
		speedOfSound = currentArray.getSpeedOfSound();
		speedOfSoundError = currentArray.getSpeedOfSoundError();
		arrayAxis = arrayManager.getArrayDirections(currentArray, hydrophoneMap); 
		if (arrayAxis == null) {
			arrayAxis = new PamVector[1];
			arrayAxis[0] = new PamVector(0,1,0);
		}
		/*
		 * May need to set spacing to it's negative if the principle array axis is in the 
		 * opposite direction to the pair axis !
		 */
		PamVector v1, v0;
		v1 = currentArray.getAbsHydrophoneVector(arrayElements[1],timeMillis);
		v0 = currentArray.getAbsHydrophoneVector(arrayElements[0],timeMillis);
		if (v0 == null || v1 == null) {
			spacing = 0;
		}
		else {
			PamVector pairVector = v1.sub(v0);
			if (pairVector.dotProd(arrayAxis[0]) > 0) {
				spacing = -spacing;
			}
		}
//		PamVector pairVector = currentArray.getAbsHydrophoneVector(arrayElements[1]).
//		sub(currentArray.getAbsHydrophoneVector(arrayElements[0]));
		
	}
	
	
	/**
	 * Some hydrophone locators have arrays which change with time. In this case the ML grid localiser will need to recalculate the look up table for localised ppositions
	 * 
	 * @return true if a new grid needs to be created
	 */
	private boolean resetArray(long timeMillis){
		
		if (this.timeMillis!=timeMillis && currentArray.getHydrophoneLocator().isChangeable()){
			System.out.println("Reset PairBearingLocaliser");
			prepare(this.arrayElements,  timeMillis, this.timingError);
			return true;
		}
			
		return false; 
	}

	@Override
	public PamVector[] getArrayAxis() {
		return arrayAxis;
	}

	@Override
	public int getArrayType() {
		return arrayType;
	}

	@Override
	public double[][] localise(double[] delays, long timeMillis) {
		
		resetArray(timeMillis);

		double[][] ans = new double[2][1];
		if (delays == null || delays.length == 0) {
			return null;
		}
		/** 
		 * Need a total bodge at this point to look at Vancouver data. 
		 */
		if (delays.length == 3) {
			delays = Arrays.copyOfRange(delays, 1, 2);
		}
		
		double ct = speedOfSound * delays[0] / spacing;
		ct = Math.max(-1., Math.min(1., ct));
		
		double angle = Math.acos(ct);
		
		double e1 = speedOfSound * timingError;
		double e2 = speedOfSound * delays[0] / spacing * spacingError;
		double e3 = delays[0] * speedOfSoundError;
		double error = (e1*e1 + e2*e2 + e3*e3) / spacing / Math.sin(angle);
		error += wobbleRadians;
		error = Math.sqrt(error);
		
		ans[0][0] = angle;
		ans[1][0] = error;
		
//		System.out.println(String.format("Pair angle %3.1f +- %3.1f",
//				Math.toDegrees(angle), Math.toRadians(error)));
		
		return ans;
	}

	@Override
	public int getHydrophoneMap() {
		return hydrophoneMap;
	}

	/**
	 * @return the spacing
	 */
	public double getSpacing() {
		return spacing;
	}

	/**
	 * @return the speedOfSound
	 */
	public double getSpeedOfSound() {
		return speedOfSound;
	}

}
