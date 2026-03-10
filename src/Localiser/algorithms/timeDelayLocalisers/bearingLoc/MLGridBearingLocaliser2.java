package Localiser.algorithms.timeDelayLocalisers.bearingLoc;

import java.awt.Window;
import java.io.Serializable;
import java.util.Arrays;

import Array.ArrayManager;
import Array.PamArray;
import Jama.Matrix;
import Localiser.LocalisationAlgorithmInfo;
import Localiser.algorithms.Correlations;
import Localiser.algorithms.PeakPosition2D;
import Localiser.algorithms.PeakSearch;
import PamDetection.LocContents;
import PamUtils.ArrayDump;
import PamUtils.PamUtils;
import PamUtils.SystemTiming;
import pamMaths.PamVector;
import tethys.localization.LocalizationBuilder;
import tethys.localization.LocalizationCreator;
import tethys.pamdata.AutoTethysProvider;
import tethys.swing.export.LocalizationOptionsPanel;

/**
 * Revamp of the earlier MLGridBearingLocaliser but with a more sensible 
 * coordinate frame. <br>
 * Now oriented the 'globe' differently, so that the first angle can be between -Pi and +Pi or 
 * between 0 and 2Pi. The second angle is the elevation angle which is either up (+ve angles)
 * or down (-ve angles) having a range of -Pi/2 to +Pi/2. This change will only affect 
 * data from volumetrica arrays when the animal is at significant depth.
 * @author Doug Gillespie
 *
 */
public class MLGridBearingLocaliser2 implements BearingLocaliser {
	private int[] arrayElements;
	private int nPhones, nPairs;
	private int[] phoneNumbers;
	private int arrayType;
	private PamVector[] arrayAxis;
	private PamArray currentArray;
	private double speedOfSound, speedOfSoundError;
	
	private double[][][] delayGrid;
	private double[][][] delayErrorGrid;
	
	private int[][] phonePairs;
	private double[] thetaRange;
	private double[] phiRange;
	private int nThetaBins, nPhiBins;
	private double thetaStep, phiStep;
	
	private PairBearingLocaliser[] pairBearingLocalisers;
	private int[] pairBearingLocaliserPairs;
	private double timingError;

	private PeakSearch peakSearch = new PeakSearch(true);
	
	private long timeMillis;
	
	private Correlations correlations = new Correlations();
	
	/**
	 * whether or not to use the default theta and phi angles 
	 */
	private boolean useDefaults=true;

	public MLGridBearingLocaliser2(int hydrophoneBitMap, long timMillis, double timingError) {
		prepare(PamUtils.getChannelArray(hydrophoneBitMap), timMillis, timingError);
	}
	
	public MLGridBearingLocaliser2(int[] hydrophoneList, long timMillis, double timingError) {
		prepare(hydrophoneList, timMillis, timingError);
	}
	
	@Override
	/**
	 * Initialise the grid search using the step values passed and setting the angle ranges to their defaults
	 */
	public void prepare(int[] arrayElements, long timMillis, double timingError) {
		this.useDefaults=true;
		prepare(arrayElements, timMillis, timingError, Math.toRadians(3), Math.toRadians(3));
	}

	@Override
	public int getLocalisationContents() {
		return LocContents.HAS_BEARING | LocContents.HAS_AMBIGUITY;
	}
	
	synchronized private void prepare(int[] arrayElements, long timeMillis, double timingError, double thetaStep, double phiStep) {
		this.timingError = timingError;
		this.thetaStep = thetaStep;
		this.phiStep = phiStep;
		this.timeMillis=timeMillis;
		this.arrayElements=arrayElements;
		
		nPhones = arrayElements.length;
		nPairs = nPhones * (nPhones-1) / 2;
		phoneNumbers = Arrays.copyOf(arrayElements, nPhones);
		hydrophoneMap = PamUtils.makeChannelMap(phoneNumbers); 
		ArrayManager arrayManager = ArrayManager.getArrayManager();
		currentArray = arrayManager.getCurrentArray();
		arrayType = arrayManager.getArrayShape(currentArray, hydrophoneMap);
//		if (arrayType != ArrayManager.ARRAY_TYPE_PLANE && arrayType != ArrayManager.ARRAY_TYPE_VOLUME) {
//			System.out.println("Ths MLGridBEaringLocaliser should only be used with planer " +
//					" and volumetric sub arrays, not this one which is " + 
//					ArrayManager.getArrayTypeString(arrayType));
//		}
		arrayAxis = arrayManager.getArrayDirections(currentArray, hydrophoneMap);
		
		speedOfSound = currentArray.getSpeedOfSound();
		speedOfSoundError = currentArray.getSpeedOfSoundError(); 
		
		/**
		 * Prepare the grid, if we are using the defaults
		 */
		if (useDefaults) {
			thetaRange = new double[2];
			thetaRange[0] = -Math.PI;
			thetaRange[1] = Math.PI;
			phiRange = new double[2];
			switch (arrayType) {
			case ArrayManager.ARRAY_TYPE_LINE:
				thetaRange[0] = 0;
				phiRange[0] = phiRange[1] = 0;
				peakSearch.setWrapDim0(false);
				peakSearch.setWrapStep0(1);
				break;
			case ArrayManager.ARRAY_TYPE_PLANE:
				phiRange[0] = 0;//-Math.PI/2;
				phiRange[1] = Math.PI/2.;
				peakSearch.setWrapDim0(true);
				peakSearch.setWrapStep0(2);
				break;
			case ArrayManager.ARRAY_TYPE_VOLUME:
				phiRange[0] = -Math.PI/2.;
				phiRange[1] = Math.PI/2.;
				peakSearch.setWrapDim0(true);
				peakSearch.setWrapStep0(2);
				break;
			}
		}
		
		nThetaBins = (int) Math.floor((thetaRange[1] - thetaRange[0])/thetaStep)+1;
		if (phiRange[1] == phiRange[0]) {
			nPhiBins = 1;
		}
		else {
			nPhiBins = (int) Math.floor((phiRange[1] - phiRange[0])/phiStep)+1;
		}
		delayGrid = new double[nThetaBins][nPhiBins][nPairs];
		delayErrorGrid = new double[nThetaBins][nPhiBins][nPairs];
		/*
		 * Make a quick access list of phone pairs. 
		 */
		int iPair = 0;
		phonePairs = new int[nPairs][2];
		for (int i = 0; i < nPhones; i++) {
			for (int j = i+1; j < nPhones; j++) {
				phonePairs[iPair][0] = phoneNumbers[i];
				phonePairs[iPair][1] = phoneNumbers[j];
				iPair ++;
			}
		}
		/*
		 * Try to find a pair which is close to the alignment of each
		 * principle axis. If one can be found, then create a pairBearingLocaliser
		 * with that pair, and use to do a super rapid calculation of the angles
		 * to get in the right starting place. 
		 */
		pairBearingLocalisers = new PairBearingLocaliser[3];
		pairBearingLocaliserPairs = new int[3];
		int[] map = new int[2];
		for (int i = 0; i < arrayAxis.length; i++) {
			pairBearingLocaliserPairs[i] = findPairLocaliser(arrayAxis[i], timeMillis);
			if (pairBearingLocaliserPairs[i] >= 0) {
				map[0] = phonePairs[pairBearingLocaliserPairs[i]][0];
				map[1] = phonePairs[pairBearingLocaliserPairs[i]][1];
				pairBearingLocalisers[i] = new PairBearingLocaliser(map, timeMillis, timingError);
			}
		}
		
		/**
		 * now work out all the delays and delay errors
		 * loop over pairs first
		 * 
		 * The pair vector will be in the standard x,y,z coordinate frame. however, 
		 * the theta and phi coordinates are being calculated in the arrayAxis frame, which 
		 * may be lined up on different axis. We therefore need to rotate the 
		 * pairVector and pairErrorVector so that they are relative to the current theta / phi frame
		 * and then base theta / phi angles on the rotated vectors.
		 * 
		 *  This will actually rotate everything so that x is the principle axis even 
		 *  though y will nearly always the be principle axis for forward towing hydrophones, 
		 *  but this doesn't matter. When it comes to the theta and phi calculations, make
		 *  sure that they are calculated relative to the x axis and the xy plane.  
		 */
		PamVector[] rotVectors = Arrays.copyOf(arrayAxis, 3);
		// may need to work out the third vector. Bit of a bodge to handle linear array. 
		if (rotVectors[1] == null) {
			/*
			 * get any perpendicular to the first vector. This can be achieved by 
			 * taking the vector product of this and any other vector then normalising. 
			 */
			// start by changing any non zero elements.
			rotVectors[1] = rotVectors[0].getPerpendicularVector();
		}
//		if (arrayType == ArrayManager.ARRAY_TYPE_PLANE) {
		if (rotVectors[2] == null) {
			rotVectors[2] = rotVectors[0].vecProd(rotVectors[1]);
		}
		Matrix rotMatrix = PamVector.arrayToMatrix(rotVectors);
		try {
			rotMatrix = rotMatrix.inverse();
		}
		catch (Exception e) {
			return;
		}
//		long nanosStart = SystemTiming.getProcessCPUTime();
		PamVector pairVector, pairErrorVector, bearingVector, pV0, pV1;
		bearingVector = new PamVector();
		double theta, phi;
		double e1, e2, e3, cosAng, pairSeparationError;
		double sosErrorFactor = speedOfSoundError / speedOfSound / speedOfSound;
		for (int iP = 0; iP < nPairs; iP++) {
			pV0 = currentArray.getAbsHydrophoneVector(phonePairs[iP][0],timeMillis);
			pV1 = currentArray.getAbsHydrophoneVector(phonePairs[iP][1],timeMillis);
			if (pV1 == null || pV0 == null) {
				continue;
			}
			pairVector = pV1.sub(pV0);
			pairErrorVector = currentArray.getSeparationErrorVector(phonePairs[iP][1], phonePairs[iP][0],timeMillis);
			pairVector = pairVector.rotate(rotMatrix);
			pairErrorVector = pairErrorVector.rotate(rotMatrix);

			for (int iT = 0; iT < nThetaBins; iT++) {
				for (int iPhi = 0; iPhi < nPhiBins; iPhi++) {
					// create a unit vector along theta / phi
					theta = thetaBinToAngle(iT);
					phi = phiBintoAngle(iPhi);
					bearingVector = PamVector.fromHeadAndSlantR(Math.PI/2.-theta, phi);
//					bearingVector.setElement(0, Math.cos(theta));
//					bearingVector.setElement(1, Math.sin(theta)*Math.cos(phi));
//					bearingVector.setElement(2, Math.sin(theta)*Math.sin(phi));
					delayGrid[iT][iPhi][iP] = -bearingVector.dotProd(pairVector)/speedOfSound;
					/*
					 * Now the error on the delay. 
					 */
					cosAng = bearingVector.dotProd(pairVector.getUnitVector());
					e1 = pairErrorVector.sumComponentsSquared(bearingVector) / speedOfSound;
					e2 = pairVector.dotProd(bearingVector) * sosErrorFactor;
					e3 = timingError;
					delayErrorGrid[iT][iPhi][iP] = Math.sqrt(e1*e1 + e2*e2 + e3*e3);
//					delayErrorGrid[iT][iPhi][iP] = e1;
				}
			}
		}
//		long nanoEnd = SystemTiming.getProcessCPUTime();
//		System.out.println(String.format("LUT Creation time %3.2f microseconds", (nanoEnd-nanosStart)/1000.));
		
		initialiseLLLut();
	}
	
	/**
	 * Find a pair of phones which line up well with pamVector and
	 * return a pair localiser made from them .
	 * @param pamVector
	 * @return
	 */
	private int findPairLocaliser(PamVector pamVector, long timeMillis) {
		double angle;
		PamVector pairVector;
		double maxAngle = Math.toRadians(5);
		for (int i = 0; i < nPairs; i++) {
			pairVector = currentArray.getAbsHydrophoneVector(phonePairs[i][0],timeMillis)
			.sub(currentArray.getAbsHydrophoneVector(phonePairs[i][1],timeMillis));
			angle = pairVector.absAngle(pamVector);
			if (angle < maxAngle) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Convert a bin into an angle. 
	 * @param bin bin index
	 * @return angle
	 */
	protected double thetaBinToAngle(double bin) {
		return Math.PI/2. - (thetaRange[0] + bin*thetaStep);
	}
	
	/**
	 * Convert an angle into a bin, constrained to 0 <= bin < nBins
	 * @param theta angle
	 * @return bin
	 */
	protected int thetaAngleToBin(double theta) {
		theta = Math.PI/2. - theta;
		int bin = (int) ((theta-thetaRange[0]) / thetaStep);
		bin = Math.max(0, Math.min(nThetaBins-1, bin));
		return bin;
	}

	/**
	 * Convert a bin into an angle. 
	 * @param bin bin index
	 * @return angle
	 */
	private double phiBintoAngle(double bin) {
		return phiRange[0] + bin*phiStep;
	}

	/**
	 * Convert an angle into a bin, constrained to 0 <= bin < nBins
	 * @param phi angle
	 * @return bin
	 */
	private int phiAngleToBin(double phi) {
		int bin = (int) ((phi-phiRange[0]) / phiStep);
		bin = Math.max(0, Math.min(nPhiBins-1, bin));
		return bin;
	}

	@Override
	public PamVector[] getArrayAxis() {
		return arrayAxis;
	}

	@Override
	public int getArrayType() {
		return arrayType;
	}
	
	/**
	 * Some hydrophone locators have arrays which change with time. In this case the ML grid localiser will need to recalculate the look up table for localised ppositions
	 * 
	 * @return true if a new grid needs to be created
	 */
	private boolean resetArray(long timeMillis){
		
		if (this.timeMillis!=timeMillis && currentArray.getHydrophoneLocator().isChangeable()){
			prepare(this.arrayElements,  timeMillis, this.timingError,  this.thetaStep, this. phiStep);
			return true;
		}
			
		return false; 
	}

	@Override
	synchronized public double[][] localise(double[] delays,long timeMillis) {
		
		resetArray(timeMillis);
		this.timeMillis=timeMillis;
//		initialiseLLLut();
//		//		return localiseBySimplex(delays);
//		//		return localiseByBisection(delays, delayErrors);
//		long t1 = System.nanoTime();
//		long t2 = System.nanoTime();
		//			initialiseLLLut();

		if (delays == null || delays.length != nPairs) {
			return null;
		}
//		long t3 = System.nanoTime();
//		t3 = System.
		/**
		 * Use the full grid search. It takes < .5ms and gives the 
		 * correct answer every time, whereas the crawl can easily and often 
		 * does end up in the wrong corner. Grid has been reduced to 2 degrees, which gives
		 * high accuracy with angle interpolation. Could probably be reduced further. 
		 */
//		initialiseLLLut();
		double[][] angles = fullGridSearch(delays);
//		double[][] angles =  localiseByCrawl(delays,timeMillis);
//		long t4 = System.nanoTime();
//		System.out.printf("Localisation time = %3.3fms\n", (t4-t3)/1.e6);
		 
//		 double i1 = (t2-t1) / 1e6;
//		 double i2 = (t4-t3) / 1e6;
//		 System.out.println(String.format("Grid search time = %3.6fms, crawl search time = %3.6fms", i1, i2));
//			System.out.println(String.format("Theta = %3.1f +- %3.2f, Phi = %3.1f +- %3.2f, in %3.2f milliseconds",
//					Math.toDegrees(angles[0][0]), Math.toDegrees(angles[1][0]), 
//					Math.toDegrees(angles[0][1]), Math.toDegrees(angles[1][1]), i2));
		 
		 return angles;
	}
	
	int[][] simplex;
	private double[][] localiseBySimplex(double[] delays) {
		simplex = new int[3][2];
		// initialise somewhere !
		simplex[1][0] = nThetaBins/4;
		simplex[2][1] = nPhiBins/4;
		while (true) {
			break;
		}
		return null;
	}

	private double[][] likelihoodLUT;

	private void initialiseLLLut() {
		likelihoodLUT = new double[nThetaBins][nPhiBins];
		for (int i = 0; i < nThetaBins; i++) {		
			Arrays.fill(likelihoodLUT[i], 1);
		}
		
	}

	private double getLLValue(int[] bins, double[] delays) {
		return getLLValue(bins[0], bins[1], delays);
	}
	
	private double getLLValue(int thetaBin, int phiBin, double[] delays) {
//		thetaBin = wrapIndex(thetaBin, nThetaBins);
//		if (arrayType == ArrayManager.ARRAY_TYPE_VOLUME) {
//			phiBin = bounceIndex(phiBin, nPhiBins);
//		}
//		else {
//			phiBin = bounceIndex(phiBin, nPhiBins);
//		}
//		thetaBin = Math.min(Math.max(0, thetaBin), nThetaBins-1);
//		phiBin = Math.min(Math.max(0, phiBin), nPhiBins-1);
//		double val = likelihoodLUT[thetaBin][phiBin];
//		if (val <= 0) return val;
		// need to calculate that value and store it in case it's needed again.
//		double val = 0;
//		for (int i = 0; i < nPairs; i++) {
//			val -= 0.5*Math.pow((delays[i]-delayGrid[thetaBin][phiBin][i])/
//					delayErrorGrid[thetaBin][phiBin][i],2);
////			val -= 0.5*Math.pow((delays[i]-delayGrid[thetaBin][phiBin][i]),2);
//		}
		double val = calcLValue(delays, thetaBin, phiBin);
		likelihoodLUT[thetaBin][phiBin] = val;
		return val;
	}
	
	/**
	 * Calculate a likelihood value for a specific bin. 
	 * @param thetaBin
	 * @param phiBin
	 */
	private double calcLValue(double[] delays, int thetaBin, int phiBin) {
		double val = 0;
		for (int i = 0; i < nPairs; i++) {
			val -= 0.5*Math.pow((delays[i]-delayGrid[thetaBin][phiBin][i])/
					delayErrorGrid[thetaBin][phiBin][i],2);
		}
		return val;
	}
	
	/**
	 * For 0 - 2Pi measurements, the values can just loop round and round, 
	 * @param index
	 * @return
	 */
	private int wrapIndex(int index, int nBins) {
		while (index < 0) index += nBins;
		while (index >= nBins) index -= nBins;
		return index;
	}
	
	/**
	 * For 0 - pi measurements, the values should bounce off the start end the end. 
	 * @param index
	 * @param maxBins
	 * @return
	 */
	private int bounceIndex(int index, int nBins) {
		if (index >= nBins) {
			int bounce = index - nBins;
			index = nBins - 1 - bounce;
		}
		if (index < 0) {
			index = -index-1;
		}
		return index;
	}
	
	/**
	 * Calculate the errors on theta and phi based on the curvature of the
	 * likelihood surface around the central bins
	 * @param thetaBin central theta bin
	 * @param phiBin central phi bin
	 * @return errors on theta and phi in radians. 
	 */
	private double[] getErrors(int thetaBin, int phiBin, double[] delays) {
		double[] errors = new double[2];
		if (nThetaBins >= 3) {
			try {
			thetaBin = Math.min(Math.max(1, thetaBin), nThetaBins-2);
			errors[0] = getCurvature(getLLValue(thetaBin-1, phiBin, delays), getLLValue(thetaBin, phiBin, delays), 
					getLLValue(thetaBin+1, phiBin, delays)) * thetaStep;
			}
			catch (Exception e) {
				System.out.printf("Theta and Phi bins %d and %d\n", thetaBin, phiBin);
			}
		}
		if (nPhiBins >= 3) {
			phiBin = Math.min(Math.max(1, phiBin), nPhiBins-2);
			errors[1] = getCurvature(getLLValue(thetaBin, phiBin-1, delays), getLLValue(thetaBin, phiBin, delays), 
					getLLValue(thetaBin, phiBin+1, delays)) * phiStep;
		}
		return errors;
	}
	
	private double getCurvature(double v1, double v2, double v3) {
		double a2 = 1./(2*v2-v1-v3);
		if (a2 >= 0) {
			return Math.sqrt(a2);
		}
		else {
			return 0;
		}
	}

	private int[][] crawlPairs = {{0,0},{-1,1},{0,1},{1,1},{-1,0},{1,0},{-1,-1},{0,-1},{1,-1}};
	public double[][] localiseByCrawl(double[] delays, long timeMillis) {
//		long startNanos = System.nanoTime();
//		long endNanos;
		int nCrawlSteps = 0;
		
		int thetaBin = nThetaBins/2;
		int phiBin = nPhiBins/2;
		double[][] angData;
		double thetaAngle = 0, phiAngle;
		double[] aDelay = new double[1];
		for (int i = 0; i < 2; i++) {
			if (pairBearingLocalisers[i] == null) {
				continue;
			}
			aDelay[0] = delays[pairBearingLocaliserPairs[i]];
			angData = pairBearingLocalisers[i].localise(aDelay,timeMillis);
			if (angData != null) {
				if (i == 0) {
					thetaBin = thetaAngleToBin(thetaAngle = angData[0][0]);
				}
				else {
					phiAngle = angData[0][0];
					/*
					 * that isn't really a phi angle though, so transofrm it !
					 */
//					double a, b, c;
//					a = Math.cos(thetaAngle);
//					b = Math.cos(phiAngle);
//					// check a^2 + b^2 <= 1;
//					if ((a*a+b*b) > 1) {
//						b = Math.sqrt(1-a*a);
//					}
//					c = Math.sqrt(1-a*a-b*b);
//					phiAngle = Math.atan2(c, b);
					phiBin = phiAngleToBin(phiAngle);
				}
			}
		}
		/*
		 * the theta bin, which is a colatitude does not suffer from LR ambiguity, 
		 * however, the phi bin does since from the single pair used to get the first guess
		 * of phi, we don't know which side it is. In fact, I think it may be anywhere along 
		 * the line defined by theta - so start the search by ignoring phibin and search along
		 * theta bin 
		 */
		double aVal, bestVal;
		bestVal = getLLValue(thetaBin, 0, delays);
		for (int i = 1; i < nPhiBins; i++) {
			aVal = getLLValue(thetaBin, i, delays);
			if (aVal > bestVal) {
				phiBin = i;
				bestVal = aVal;
			}
		}
		
		int bestPair = -1;
		bestVal = getLLValue(thetaBin, phiBin, delays);
		while (bestPair != 0) {
			bestPair = 0;
			for (int i = 1; i < 9; i++) {
				aVal = getLLValue(thetaBin + crawlPairs[i][0], phiBin + crawlPairs[i][1], delays);
				if (aVal > bestVal) {
					bestVal = aVal;
					bestPair = i;
				}
			}
			if (bestPair == 0) {
				break;
			}
			thetaBin += crawlPairs[bestPair][0];
			phiBin += crawlPairs[bestPair][1];
			nCrawlSteps++;
		}
		double[] errors = getErrors(thetaBin, phiBin, delays);
//		endNanos = System.nanoTime();
		double theta = thetaBin;
		double phi = phiBin;
		if (thetaBin > 0 && thetaBin < nThetaBins-1) {
			theta += correlations.parabolicCorrection(getLLValue(thetaBin-1, phiBin, delays), 
					getLLValue(thetaBin, phiBin, delays),
					getLLValue(thetaBin+1, phiBin, delays));
		}
		if (phiBin > 0 && phiBin < nPhiBins-1) {
			phi += correlations.parabolicCorrection(getLLValue(thetaBin, phiBin-1, delays), 
					getLLValue(thetaBin, phiBin, delays),
					getLLValue(thetaBin, phiBin+1, delays));
		}
		theta = thetaBinToAngle(theta);
		phi = phiBintoAngle(phi);
		double[][] output = new double[2][2];
		output[0][0] = theta;
		output[1][0] = errors[0];
		output[0][1] = phi;
		output[1][1] = errors[1];
//		System.out.print(String.format(", %3.2f", bestVal));
//		double micros = (endNanos-startNanos)/1000;
//		System.out.println(String.format("Crawl Search: Theta = %3.1f +- %3.2f, Phi = %3.1f +- %3.2f, %d crawl steps in %3.2f microseconds",
//				Math.toDegrees(theta), Math.toDegrees(errors[0]), Math.toDegrees(phi), 
//				Math.toDegrees(errors[1]), nCrawlSteps, 0.0));
	
		return output;
	}

	private boolean firstGrid = false;
	private int hydrophoneMap;
	private double[][] fullGridSearch(double[] delays) {
		long startNanos = System.nanoTime();
		long endNanos;
		int thetaBin, phiBin;
		double bestVal = getLLValue(0, 0, delays);
		double aVal;
		if (likelihoodLUT == null) {
			likelihoodLUT = new double[nThetaBins][nPhiBins];
		}
		for (int i = 0; i < nThetaBins; i++) {
			for (int j = 0; j < nPhiBins; j++) {
				likelihoodLUT[i][j] = calcLValue(delays, i, j);
//				aVal = getLLValue(i, j, delays);
//				if (aVal > bestVal) {
//					thetaBin = i;
//					phiBin = j;
//					bestVal = aVal;
//				}
			}
		}
		PeakPosition2D peakResult = peakSearch.interpolatedPeakSearch(likelihoodLUT);
		
		double[] errors = getErrors((int) Math.round(peakResult.getBin0()), (int) Math.round(peakResult.getBin1()), delays);
		endNanos = System.nanoTime();
//		double theta = Math.toDegrees(thetaBinToAngle(thetaBin));
//		double phi = Math.toDegrees(phiBintoAngle(phiBin));
		double theta = thetaBinToAngle(peakResult.getBin0());
		double phi = phiBintoAngle(peakResult.getBin1());
		double micros = (endNanos-startNanos)/1000.;
//		System.out.println(String.format("Grid Search Theta = bin %d  %3.1f +- %3.2f, Phi = bin %d %3.1f +- %3.2f, in %3.2f microseconds",
//				thetaBin, theta, Math.toDegrees(errors[0]), phiBin, phi, Math.toDegrees(errors[1]), micros));
	
		
		double[][] output;
		if (arrayType == ArrayManager.ARRAY_TYPE_LINE) {
			output = new double[2][1];
			output[0][0] = theta;
			output[1][0] = errors[0];
		}
		else {
			output = new double[2][2];
			output[0][0] = theta;
			output[1][0] = errors[0];
			output[0][1] = phi;
			output[1][1] = errors[1];
		}

		return output;
	}
	

	/*
	 * Try localising using a simple bisection method
	 * has advantage over crawl in that same points are often reevaluated
	 */
//	private int[] A, B, C;
	public double[][] localiseByBisection(double[] delays) {
		/**
		 * box defines the extremities and the centre in each dimension. 
		 */
		int[][] box = new int[2][3];
//		B = new int[2];
//		C = new int[2];
		double[] vals = new double[3];
		/**
		 * Start searching the whole thing - may change this to start with a smaller
		 * region with known start point. 
		 */
		box[0][0] = box[1][0] = 0;
		box[0][2] = nThetaBins-1;
		box[1][2] = nPhiBins-1;
		box[0][1] = box[0][2]/2;
		box[1][1] = box[1][2]/2;
		
		while(true) {
			for (int d = 0; d < 2; d++) {
				for (int i = 0; i < 3; i++) {
					if (d == 0) {
						vals[i] = getLLValue(box[d][i], box[1][1], delays);
					}
					else break;
				}
			}
			break;
		}
		return null;
	}
	

	@Override
	public int getHydrophoneMap() {
		return hydrophoneMap;
	}
	
	/**
	 * Set the angles to analyse and call the prepare method to set everything up.  Use the array elements, time,
	 * and timing error to the values that have already been specified
	 * 
	 * @param thetaRange the range for theta angles, specified in radians as [min max step]
	 * @param phiRange the range for phi angles, specified in radians as [min max step]
	 */
	public void setAnalysisAngles(double[] thetaRange, double[] phiRange) {
		this.thetaRange[0] = thetaRange[0];
		this.thetaRange[1] = thetaRange[1];
		this.thetaStep = thetaRange[2];
		this.phiRange[0] = phiRange[0];
		this.phiRange[1] = phiRange[1];
		this.phiStep = phiRange[2];
		this.useDefaults = false;
		
		// if the angles start/end at the same spot (e.g. -180 to 180 or 0 to 360) set the wrapping here
		if (((int) (thetaRange[0]+thetaRange[1])==0) || 
				(thetaRange[0]==0 && thetaRange[1]==2*Math.PI) ||
				(thetaRange[0]==-2*Math.PI && thetaRange[1]==0)) {
			peakSearch.setWrapDim0(true); 
			peakSearch.setWrapStep0(2);
		} else {
			peakSearch.setWrapDim0(false);
			peakSearch.setWrapStep0(1);
		}
		prepare(this.arrayElements, this.timeMillis, this.timingError, this.thetaStep, this.phiStep);
	}

	/**
	 * @return the likelihoodLUT
	 */
	public double[][] getLikelihoodLUT() {
		return likelihoodLUT;
	}

	@Override
	public String getAlgorithmName() {
		return "Maximum likelyhood grid bearing localiser V2";
	}

	@Override
	public Serializable getParameters() {
		return new LocaliserParams(this.thetaStep, this.phiStep);
	}
	
	@Override
	public LocalisationAlgorithmInfo getAlgorithmInfo() {
		return this;
	}

	@Override
	public LocalizationCreator getTethysCreator() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * For passing to Tethys output. 
	 * @author dg50
	 *
	 */
	public class LocaliserParams implements Serializable {
		private static final long serialVersionUID = 1L;
		public double thetaStep, phiStep;
		public LocaliserParams(double thetaStep, double phiStep) {
			super();
			this.thetaStep = Math.toDegrees(thetaStep);
			this.phiStep = Math.toDegrees(phiStep);
			this.thetaStep = AutoTethysProvider.roundDecimalPlaces(this.thetaStep, 2);
			this.phiStep = AutoTethysProvider.roundDecimalPlaces(this.phiStep, 2);
		}
	}
	
	@Override
	public LocalizationOptionsPanel getLocalizationOptionsPanel(Window parent, LocalizationBuilder locBuilder) {
		return null;
	}
}
