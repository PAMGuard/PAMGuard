package group3dlocaliser.algorithm.toadbase;

import java.awt.Window;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionProcess;
import Array.ArrayManager;
import Array.SnapshotGeometry;
import Localiser.DelayMeasurementParams;
import Localiser.algorithms.Correlations;
import Localiser.algorithms.locErrors.EllipticalError;
import Localiser.algorithms.locErrors.SimpleError;
import Localiser.detectionGroupLocaliser.DetectionGroupOptions;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.SettingsNameProvider;
import PamController.SettingsPane;
import PamDetection.AbstractLocalisation;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.FFTDataHolder;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.superdet.SuperDetection;
import PamguardMVC.toad.GenericTOADCalculator;
import PamguardMVC.toad.TOADCalculator;
import fftManager.FFTDataUnit;
import group3dlocaliser.Group3DLocaliserControl;
import group3dlocaliser.Group3DParams;
import group3dlocaliser.ToadManagedSettingsPane;
import group3dlocaliser.algorithm.Chi2Data;
import group3dlocaliser.algorithm.FitTestValue;
import group3dlocaliser.algorithm.LocaliserAlgorithm3D;
import group3dlocaliser.algorithm.LogLikelihoodData;
import pamMaths.PamVector;
import pamViewFX.fxNodes.pamDialogFX.ManagedSettingsPane;

abstract public class TOADBaseAlgorithm extends LocaliserAlgorithm3D {

	private double sampleRate;

	private Correlations correlations = new Correlations();

	private PamDataBlock sourceDataBlock;

	private TOADCalculator toadCalculator;

	private GenericTOADCalculator genericTOADCalculator;

	private TOADBaseParams toadBaseParams = new TOADBaseParams();

	private Group3DLocaliserControl group3dLocaliser;
	
	private static double halflog2pi = Math.log(2.*Math.PI)/2.;

	public TOADBaseAlgorithm(Group3DLocaliserControl group3dLocaliser) {
		this.group3dLocaliser = group3dLocaliser;
		genericTOADCalculator = new GenericTOADCalculator(group3dLocaliser);
		PamSettingManager.getInstance().registerSettings(new TOADSettingManager());
	}

	@Override	
	public boolean prepare(PamDataBlock sourceBlock) {
		this.sourceDataBlock = sourceBlock;
		toadCalculator = sourceBlock.getTOADCalculator();
		if (toadCalculator == null) {
			toadCalculator = genericTOADCalculator;
			genericTOADCalculator.setDetectorDataBlock(sourceBlock);
		}
		this.sampleRate = sourceBlock.getSampleRate();
		return true;
	}

	@Override
	public AbstractLocalisation runModel(PamDataUnit groupDataUnit, DetectionGroupOptions detectionGroupOptions,
			boolean addLoc) {
		if (groupDataUnit instanceof SuperDetection == false) {
			return null;
		}
		SuperDetection superDetection = (SuperDetection) groupDataUnit;
		int allChannels = 0;
		ArrayList<PamDataUnit> subDetections = superDetection.getSubDetections();
		if (subDetections == null || subDetections.size() < 1) {
			return null;
		}
		double sampleRate = 0;
		for (PamDataUnit aDataUnit:subDetections) {
			allChannels |= aDataUnit.getChannelBitmap();
			sampleRate = aDataUnit.getParentDataBlock().getSampleRate();
		}

		/**
		 * Only use channels that are selected in the algorithm options. 
		 * This gives an opportunity to ditch any noise or corrupt channels. 
		 */
		allChannels &= toadBaseParams.getChannelBitmap();
		PamProcess sourceProcess = subDetections.get(0).getParentDataBlock().getSourceProcess();
		AcquisitionControl acquisitionControl;
		int hydrophoneMap = allChannels;
		if (sourceProcess instanceof AcquisitionProcess) {
			AcquisitionProcess ap = (AcquisitionProcess) sourceProcess;
			acquisitionControl = ap.getAcquisitionControl();
			hydrophoneMap = acquisitionControl.ChannelsToHydrophones(allChannels);
		}

		SnapshotGeometry geometry = ArrayManager.getArrayManager().getSnapshotGeometry(hydrophoneMap, 
				groupDataUnit.getTimeMilliseconds());

		//		if (groupDataUnit.getSubDetection(0).getUID() == 9035003222L) {
//		if (superDetection.getSubDetection(0).getUID() == 9035004477L) {
//			System.out.println("Found it");
//		}
		TOADInformation toadInformation = toadCalculator.getTOADInformation(superDetection.getSubDetections(), sampleRate, allChannels, geometry);

		boolean toadOK = checkTOADInformation(toadInformation);
		if (!toadOK) {
			return null;
		}
		if (toadInformation == null) {
			return null;
		}
		if (toadInformation.getToadSeconds() == null || toadInformation.getToadSeconds().length == 0) {
			return null;
		}

		return processTOADs(groupDataUnit, geometry, toadInformation);
	}

	/**
	 * Check the TOAD information is 'OK', i.e. cross correlations are reasonable, etc. 
	 * @param toadInformation
	 * @return true if enough channels have a reasonable TOAD measurement. 
	 */
	public boolean checkTOADInformation(TOADInformation toadInformation) {
		if (toadInformation == null) {
			return false;
		}
		return countUsableTOADS(toadInformation) >= toadBaseParams.getMinTimeDelays();
	}

	/**
	 * Count the number of TOAD values which have a correlation coefficient or 'score' 
	 * >= the minimum.
	 * @param toadInformation TOAD information
	 * @return number of scores >= the minimum. 
	 */
	public int countUsableTOADS(TOADInformation toadInformation) {
		return countUsableTOADS(toadInformation, toadBaseParams.getMinCorrelation());
	}
	
	/**
	 * Count the number of TOAD values which have a correlation coefficient or 'score' 
	 * >= the minimum.
	 * @param toadInformation TOAD information
	 * @param minScore Minimum correlation score. 
	 * @return number of scores >= the minimum. 
	 */
	public int countUsableTOADS(TOADInformation toadInformation, double minScore) {
		/**
		 * Count the number of pairs with OK cross correlations
		 */
		int[] chanList = toadInformation.getChannelList();
		int nChan = chanList.length;
		double[][] xc = toadInformation.getToadScores();
		if (xc == null) {
			return 0;
		}
		int goodDelays = 0;
		for (int i = 0; i < nChan; i++) {
			for (int j = i+1; j < nChan; j++) {
				if (xc[i][j] >= toadBaseParams.getMinCorrelation()) {
					goodDelays++;
				}
			}
		}
		return goodDelays;
	}

	/**
	 * Get all the FFT data, note that we'll try with the default length, but that
	 * that may not be long enough or they may not all be the same (in the case of cicks)
	 * so be prepared to ask for more ...
	 * @param groupDataUnit
	 * @param fftLength
	 * @return
	 */
	private GroupedFFTDataLump getAllFFTData(SuperDetection groupDataUnit, Integer fftLength) {
		int nUnits = groupDataUnit.getSubDetectionsCount();
		int totalChannels = 0;
		for (int i = 0; i < nUnits; i++) {
			PamDataUnit dataUnit = groupDataUnit.getSubDetection(i);
			totalChannels += PamUtils.getNumChannels(dataUnit.getChannelBitmap());
		}
		GroupedFFTDataLump fftLump = new GroupedFFTDataLump(totalChannels);
		int iChan = 0;
		for (int i = 0; i < nUnits; i++) {
			PamDataUnit dataUnit = groupDataUnit.getSubDetection(i);
			FFTDataHolder fftDataHolder = (FFTDataHolder) dataUnit;
			int chMap = dataUnit.getChannelBitmap();
			int nChan = PamUtils.getNumChannels(chMap);
			long startSamp = dataUnit.getStartSample();
			List<FFTDataUnit> fftDataUnits = fftDataHolder.getFFTDataUnits(fftLength);
			if (fftDataUnits == null) {
				return null;
			}
			int nPChan = fftDataUnits.size() / nChan;
			int rem = fftDataUnits.size()%nChan; 
			if (rem != 0) {
				// panick !
				return null;
			}
			fftLump.minPerChannel = Math.min(fftLump.minPerChannel, nPChan);
			fftLump.maxPerChannel = Math.max(fftLump.maxPerChannel, nPChan);
			for (FFTDataUnit u:fftDataUnits) {
				int fftLen = u.getFftData().length()*2;
				fftLump.minFFTLength = Math.min(fftLump.minFFTLength, fftLen);
				fftLump.maxFFTLength = Math.max(fftLump.maxFFTLength, fftLen);
			}
			int lind = 0;
			for (int ic = 0; ic < nChan; ic++, iChan++) {
				fftLump.channelFFTData[iChan] = new FFTDataUnit[nPChan];
				fftLump.channelFFTCount[iChan] = nPChan;
				fftLump.channelGroup[iChan] = i;
				for (int id = 0; id <nPChan; id++) {
					fftLump.channelFFTData[iChan][id] = fftDataUnits.get(id*nChan+ic);
				}
			}
		}
		return fftLump;
	}

	private class GroupedFFTDataLump {
		public GroupedFFTDataLump(int nChan) {
			super();
			channelFFTData = new FFTDataUnit[nChan][];
			channelFFTCount = new int[nChan];
			channelGroup = new int[nChan];
			minPerChannel = Integer.MAX_VALUE;
			minFFTLength = Integer.MAX_VALUE;
			maxPerChannel = 0;
			maxFFTLength = 0;
		}
		FFTDataUnit[][] channelFFTData;
		int[] channelGroup;
		int[] channelFFTCount;
		int minPerChannel;
		int maxPerChannel;
		int minFFTLength;
		int maxFFTLength;
	}

	private DelayMeasurementParams getDelayMeasurementParams() {
		return null;
	}

	/**
	 * Calculate a Log Likelihood value for the given geometry and set of delays. 
	 * @param geometry
	 * @param delays
	 * @return
	 */
	public LogLikelihoodData calcLogLikelihood(SnapshotGeometry geometry, TOADInformation toadInformation, double[] position) {
		/**
		 * There is an awful lot of repeated code in this and the Chi2 function. Would be good
		 * to sort out some time !
		 */
//		if (1>0) {
//			Chi2Data chiRes = calcChi2(geometry, toadInformation, position);
//			LogLikelihoodData lld = new LogLikelihoodData(-chiRes.getChi2(), chiRes.getDegreesOfFreedom());
//		}

		if (position == null || position.length == 0) {
			return null;
		}
		double[][] delays = toadInformation.getToadSeconds();
		double[][] delayErrors = toadInformation.getToadErrorsSeconds();

		double minCorrelationValue = toadBaseParams.getMinCorrelation();

		int nDim = position.length;
		int[] hydrophones = toadInformation.getHydrophoneList();
		int[] channels = toadInformation.getChannelList();
		int nChan = channels.length;
		double[] expectedDelays = new double[nChan];
		double c = geometry.getCurrentArray().getSpeedOfSound();
		double cError = geometry.getCurrentArray().getSpeedOfSoundError();
		PamVector centre = geometry.getGeometricCentre();
		double[] channelErrors = new double[nChan];
		double[] streamerErrors = new double[nChan];
		int[] streamerId = new int[nChan];
		double[][] correlationScores = toadInformation.getToadScores();
		PamVector positionVec = new PamVector(position).add(centre);
		// calculate the absolute distance (then time) to each hydrophone used in the delay matrix.  
		for (int i = 0; i < nChan; i++) {
			double r = 0;
			/*
			 *  use the hydrophone LUT from TOAD information to make sure we get the right phone
			 *  since channel groups may not be in a sensible order. 
			 */
			PamVector hydrophoneGeom = geometry.getGeometry()[hydrophones[i]];
			PamVector rv = positionVec.sub(hydrophoneGeom);
			expectedDelays[i] = rv.norm(nDim) / c;
			/**
			 * Now work out the expected range error along the unit vector position-hydrophone+centre 
			 */
			PamVector rvu = rv.getUnitVector();
			PamVector hErr = geometry.getHydrophoneErrors()[hydrophones[i]]; //NB. this is not really a vector !
			channelErrors[i] = Math.pow(rvu.sumComponentsSquared(hErr)/c, 2); // square and convert to time now
			PamVector sErr = geometry.getStreamerErrors()[hydrophones[i]]; // this isn't a vector either
			streamerId[i] = geometry.getCurrentArray().getStreamerForPhone(hydrophones[i]);
			if (sErr != null) {
				streamerErrors[i] = Math.pow(rvu.sumComponentsSquared(sErr)/c, 2);
			}
		}
		double llVal = 0.;
		int nGood = 0;
		for (int i = 0; i < nChan; i++) {
			for (int j = i+1; j < nChan; j++) {
				double val = (delays[i][j]);
				if (Double.isNaN(val)) {
					continue;
				}
				if (correlationScores == null || correlationScores[i][j] < minCorrelationValue) {
					continue;
				}
				double exp = expectedDelays[j]-expectedDelays[i];
				// now work out the squared error...
				double errSq = Math.pow(delayErrors[i][j], 2)+channelErrors[i]+channelErrors[j];

				// add in the streamer errors if they are in different streamers. 
				if (streamerId[i] != streamerId[j]) {
					errSq += streamerErrors[i]+streamerErrors[j];
				}
				/*
				 * Finally there is an error due to uncertainty in c, which is 
				 * obviously proportional to the expected delay. i.e. if you're 
				 * perpendicular to a hydrophone pair, then there will be zero 
				 * delay error, but more if you're end on.  
				 */
				errSq += Math.pow(exp*cError/c, 2.);
				/*
				 * Do the full calc -ln(2pi)/2 - .5ln(sig) - .5(diff^2/errSq). 
				 * So note the 0.5 instead of the which is to take the sqrt(errSq) !
				 */
				llVal -= (halflog2pi + 0.5*Math.log(errSq) + Math.pow(val-exp, 2)/errSq/2);
//				llVal -= Math.pow(val-exp, 2)/errSq/2.;
				nGood++;
			}
		}
		return new LogLikelihoodData(llVal, nGood-nDim);
	}
		
	/**
	 * Calculate a chi2 value for the given geometry and set of delays. 
	 * @param geometry array geometry. 
	 * @param delays time delays
	 * @param position Position relative to the array centre
	 * @return
	 */
	public Chi2Data calcChi2(SnapshotGeometry geometry, TOADInformation toadInformation, double[] position) {
		if (position == null || position.length == 0) {
			return null;
		}
		double[][] delays = toadInformation.getToadSeconds();
		double[][] delayErrors = toadInformation.getToadErrorsSeconds();

		double minCorrelationValue = toadBaseParams.getMinCorrelation();

		int nDim = position.length;
		int[] hydrophones = toadInformation.getHydrophoneList();
		int[] channels = toadInformation.getChannelList();
		int nChan = channels.length;
		double[] expectedDelays = new double[nChan];
		double c = geometry.getCurrentArray().getSpeedOfSound();
		double cError = geometry.getCurrentArray().getSpeedOfSoundError();
		PamVector centre = geometry.getGeometricCentre();
		double[] channelErrors = new double[nChan];
		double[] streamerErrors = new double[nChan];
		int[] streamerId = new int[nChan];
		double[][] correlationScores = toadInformation.getToadScores();
		PamVector positionVec = new PamVector(position).add(centre);
		// calculate the absolute distance (then time) to each hydrophone used in the delay matrix.  
		for (int i = 0; i < nChan; i++) {
			double r = 0;
			/*
			 *  use the hydrophone LUT from TOAD information to make sure we get the right phone
			 *  since channel groups may not be in a sensible order. 
			 */
			PamVector hydrophoneGeom = geometry.getGeometry()[hydrophones[i]];
			PamVector rv = positionVec.sub(hydrophoneGeom);
			expectedDelays[i] = rv.norm() / c;
			/**
			 * Now work out the expected range error along the unit vector position-hydrophone+centre 
			 * 
			 * These calculations were incorrect since they look at dot of the 
			 * error against the direction to the source, which is wrong ! 
			 * e.g. if error was (1,1,1) it appears as a vector along that bearing, if this
			 * were dotted with an angle perpendicular to this, then it would give a very 
			 * different answer. 
			 * The correct way to calculate the inter pair error is to add each component of
			 * the error along the vector joining the two hydrophones. This error should be stored
			 * as a pair error and then dot producted with the unit vector towards the source.
			 * Need to rewrite and produce a matrix of channel pair errors!
			 */
			PamVector rvu = rv.getUnitVector();
			PamVector hErr = geometry.getHydrophoneErrors()[hydrophones[i]];
			//			if (hErr != null) {
			channelErrors[i] = Math.pow(rvu.sumComponentsSquared(hErr)/c, 2); // square and convert to time now
			//			}
			PamVector sErr = geometry.getStreamerErrors()[hydrophones[i]];
			streamerId[i] = geometry.getCurrentArray().getStreamerForPhone(hydrophones[i]);
			if (sErr != null) {
				streamerErrors[i] = Math.pow(rvu.sumComponentsSquared(sErr)/c, 2);
			}
		}
		double chiVal = 0.;
		int nGood = 0;
		for (int i = 0; i < nChan; i++) {
			for (int j = i+1; j < nChan; j++) {
				double val = (delays[i][j]);
				if (Double.isNaN(val)) {
					continue;
				}
				if (correlationScores == null || correlationScores[i][j] < minCorrelationValue) {
					continue;
				}
				double exp = expectedDelays[j]-expectedDelays[i];
				// now work out the squared error...
				double errSq = Math.pow(delayErrors[i][j], 2)+channelErrors[i]+channelErrors[j];

				// add in the streamer errors if they are in different streamers. 
				if (streamerId[i] != streamerId[j]) {
//					errSq += streamerErrors[i]+streamerErrors[j];
					//					continue; simulate unsynchronised streamers.
				}
				/*
				 * Finally there is an error due to uncertainty in c, which is 
				 * obviously proportional to the expected delay. i.e. if you're 
				 * perpendicular to a hydrophone pair, then there will be zero 
				 * delay error, but more if you're end on.  
				 */
				errSq += Math.pow(exp*cError/c, 2.);
				chiVal += Math.pow(val-exp, 2)/errSq;
				nGood++;
			}
		}
		return new Chi2Data(chiVal, nGood-nDim);
	}
	/**
	 * Estimate an elliptical error with axes aligned with largest error coordinate based on the curvature of the Chi2 / likelihood surface. 
	 * @param geometry Array geometry
	 * @param toadInformation Current TOAD information
	 * @param position Array geometry
	 * @return Elliptical error
	 */
	public EllipticalError estimateEllipticalError(SnapshotGeometry geometry, TOADInformation toadInformation, double[] position) {
//		Chi2Data centralChi2 = calcChi2(geometry, toadInformation, position);
		/**
		 * Will take the principle axis as a direct line to the detection from the central point of the array 
		 * geometry. 
		 */
//		PamVector arrayCentre = geometry.getGeometricCentre();
		// position is already relative to the geometric centre of the array. 
		PamVector posVec = new PamVector(position);
		double[] errors = new double[6];
		PamVector[] errVecs = new PamVector[3];
		// get a unit vector along the direction from the array centre to the calculated coordinate. 
		errVecs[0] = posVec.getUnitVector();
//		if (posVec.getCoordinate(0) > 6 && posVec.getCoordinate(0) < 8 && Math.abs(posVec.getCoordinate(1)) < 5) {
//			System.out.println(posVec);
//		}
		
		/*
		 * to get secondary axis we can rotate by 90 degrees in any direction apart from
		 * the principle direction of the vector. 
		 * Ideally, we want a horizontal bearing, so cross product the man pointing
		 * vector with a vertical one which should give a vec in the horizontal plane. 
		 */
		if (errVecs[0].getCoordinate(2) > 0.999) {
			// i main vector is vertical, just use the and y axes
			errVecs[1] = PamVector.xAxis;
			errVecs[2] = PamVector.yAxis;
		}
		else {
			errVecs[1] = (errVecs[0].vecProd(PamVector.zAxis)).getUnitVector();
			errVecs[2] = errVecs[1].vecProd(errVecs[0]);
		}
		
		/*
		 * Error along each principle axis. 
		 */
		for (int i = 0; i < 3; i++) {
			double[] splitErr = getSplitError(geometry, toadInformation, posVec, errVecs[i]);
			errors[i] = splitErr[0];
			errors[i+3] = splitErr[1];
		}
		// now need to convert the error vectors to heading, pitch and roll.
		double[] angles = PamVector.getHeadingPitchRoll(errVecs);
		
		EllipticalError elError = new EllipticalError(angles, errors);
		return elError;
	}
	
	/**
	 * Estimate the error in Cartesian coordinates based on the curvature of the Chi2 / likelihood surface. 
	 * @param geometry Array geometry
	 * @param toadInformation Current TOAD information
	 * @param position Array geometry
	 * @return Cartesian error
	 */
	public SimpleError estimateCartesianError(SnapshotGeometry geometry, TOADInformation toadInformation, double[] position) {
		Chi2Data centralChi2 = calcChi2(geometry, toadInformation, position);
		/*
		 * If we assume that the error is normally distributed, then just need to work out the chi2 curvature. 
		 * Move the point by 1% in each dimension and see how much chi changes. 
		 */
		double nDF = Math.max(centralChi2.getDegreesOfFreedom(), 1);
		double chiCent = centralChi2.getChi2();
		if (chiCent == 0) {
			chiCent = 1;
		}
		int nDim = position.length;
		double step;
		double[] sig = new double[nDim];
		double[] shiftedChi = new double[2];
		for (int i = 0; i<nDim; i++) {
			step = Math.max(position[i] * .01, .1);
			for (int s = 0; s < 2; s++) {
				double[] newPos = position.clone();
				newPos[i] += step * (2.*s-1);
				Chi2Data newChi2 = calcChi2(geometry, toadInformation, newPos);
				shiftedChi[s] = newChi2.getChi2()/chiCent;
			}
			double curve = (shiftedChi[0] + shiftedChi[1] - 2)/(2*Math.pow(step, 2));
			sig[i] = 1./Math.sqrt(curve);
		}
		//		System.out.printf("\n");
		SimpleError simpleError = new SimpleError(sig[0], sig[1], sig[2], Math.PI/2);

		return simpleError;
	}
	
	/**
	 * Get the error along the principle direction 
	 * @param geometry current array geometry
	 * @param toadInformation Timing information
	 * @param position best position
	 * @param direction direction to measure error in
	 * @return error in metres
	 */
	private double getError(SnapshotGeometry geometry, TOADInformation toadInformation, PamVector position, PamVector direction) {
		Chi2Data centralChi2 = calcChi2(geometry, toadInformation, position.getVector());
		/*
		 * If we assume that the error is normally distributed, then just need to work out the chi2 curvature. 
		 * Move the point by 1% in each dimension and see how much chi changes. 
		 */
		double nDF = Math.max(centralChi2.getDegreesOfFreedom(), 1);
		double chiCent = centralChi2.getChi2();
		if (chiCent == 0) {
			chiCent = 1;
		}
		double step;
		double sig;
		double[] shiftedChi = new double[2];
		step = Math.max(position.norm() * .01, .1);
		for (int s = 0; s < 2; s++) {
			PamVector newPos = position.add(direction.times(step));
			Chi2Data newChi2 = calcChi2(geometry, toadInformation, newPos.getVector());
			shiftedChi[s] = newChi2.getChi2()/chiCent;
			step = -step;
		}
		double curve = (shiftedChi[0]+shiftedChi[1]-2)/(2*Math.pow(step, 2));
		sig = 1./Math.sqrt(curve);

		return sig;
	}

	private double[] getSplitError(SnapshotGeometry geometry, TOADInformation toadInformation, PamVector position, PamVector direction) {
		FitTestValue centralChi2 = calcLogLikelihood(geometry, toadInformation, position.getVector());
		/*
		 * If we assume that the error is normally distributed, then just need to work out the chi2 curvature. 
		 * Move the point by 1% in each dimension and see how much chi changes. 
		 */
		double nDF = Math.max(centralChi2.getDegreesOfFreedom(), 1);
		double chiCent = centralChi2.getTestScore();
//		if (chiCent == 0) {
//			chiCent = 1;
//		}
		double step;
		double[] sig = new double[2];
		double[] shiftedChi = new double[2];
		for (int s = 0; s < 2; s++) {
			step = Math.max(position.norm() * .1, 1.1);
			if (s == 1) step = -step;
			for (int i = 0; i < 5; i++) {
				PamVector newPos = position.add(direction.times(step));
				FitTestValue newChi2 = calcLogLikelihood(geometry, toadInformation, newPos.getVector());
				shiftedChi[s] = newChi2.getTestScore();
				if (chiCent-shiftedChi[s] > 2.) {
					break;
				}
				step*=Math.pow(2, i);
			}
			double curve = (2*chiCent-shiftedChi[s]*2)/(2*Math.pow(step, 2));
			if (curve < 0) {
				// should never happen but does if it's a crap Chi2 function without a decent peak:
				sig[s] = Double.POSITIVE_INFINITY;
			}
			else {
				sig[s] = 1./Math.sqrt(curve);
			}
		}

		return sig;
	}

	/**
	 * Process the list of delays, which are in seconds
	 * @param groupDataUnit group data unit. 
	 * @param geometry 
	 * @param toadInformation all information on delays, errors, channels, etc.  
	 * @return
	 */
	abstract public AbstractLocalisation processTOADs(PamDataUnit groupDataUnit, SnapshotGeometry geometry, TOADInformation toadInformation);

	/* (non-Javadoc)
	 * @see group3dlocaliser.algorithm.LocaliserAlgorithm3D#getSourceSettingsPane(java.awt.Window)
	 */
	@Override
	public ToadManagedSettingsPane<?> getSourceSettingsPane(Window parent, PamDataBlock<?> detectionSource) {
		//		return new TOADSourcePane(parent);
		//		return toadCalculator.getSettingsPane();
		if (toadCalculator != null) {
			/*
			 *  this gets the algorithm specific settings pane. We want to tab
			 *  this with a channel panel. 
			 */
			ManagedSettingsPane<?> toadPane = toadCalculator.getSettingsPane(parent, detectionSource);
			TOADSettingsPaneWithChannels<?> tspwc = new TOADSettingsPaneWithChannels<>(parent, this, toadPane);
			tspwc.getChannelPanel().setMultiColumn(true);
			tspwc.getChannelPanel().setAvailableChannels(detectionSource.getChannelMap());
			return tspwc;
		}
		else {
			return null;
		}
	}

	@Override
	public boolean canLocalise(PamDataBlock pamDataBlock) {
		/*
		 * TOAD always returns true since it's always possible to 
		 * select a raw or FFT data source from which time delays 
		 * can be extracted. Can override this in the concrete classes
		 * if necessary.
		 */
		return true;
	}

	/**
	 * @return the toadBaseParams
	 */
	public TOADBaseParams getToadBaseParams() {
		return toadBaseParams;
	}

	private class TOADSettingManager implements PamSettings {

		@Override
		public String getUnitName() {
			return group3dLocaliser.getUnitName();
		}

		@Override
		public String getUnitType() {
			return "TDOA Base Algorithm";
		}

		@Override
		public Serializable getSettingsReference() {
			return toadBaseParams;
		}

		@Override
		public long getSettingsVersion() {
			return TOADBaseParams.serialVersionUID;
		}

		@Override
		public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
			toadBaseParams = ((TOADBaseParams) pamControlledUnitSettings.getSettings()).clone();
			return true;
		}

	}

}
