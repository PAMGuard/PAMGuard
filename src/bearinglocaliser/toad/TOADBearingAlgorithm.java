package bearinglocaliser.toad;

import java.util.ArrayList;
import Array.ArrayManager;
import Array.PamArray;
import Array.SnapshotGeometry;
import Localiser.algorithms.Correlations;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliser;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliserSelector;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.MLGridBearingLocaliser2;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.MLLineBearingLocaliser2;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.PairBearingLocaliser;
import PamDetection.LocContents;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamView.GroupedSourceParameters;
import PamguardMVC.PamDataUnit;
import PamguardMVC.TFContourData;
import PamguardMVC.TFContourProvider;
import PamguardMVC.debug.Debug;
import bearinglocaliser.BearingAlgorithmGroup;
import bearinglocaliser.BearingLocalisation;
import bearinglocaliser.BearingLocaliserControl;
import bearinglocaliser.BearingLocaliserParams;
import bearinglocaliser.BearingProcess;
import bearinglocaliser.algorithms.BaseFFTBearingAlgorithm;
import bearinglocaliser.algorithms.BearingAlgorithmParams;
import bearinglocaliser.display.BearingDataDisplay;
import bearinglocaliser.toad.display.TOAD2DPlot;
import bearinglocaliser.toad.display.TOADPairPlot;
import bearinglocaliser.toad.display.TOADPlot;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import fftManager.fftorganiser.FFTDataList;
import pamMaths.PamVector;


public class TOADBearingAlgorithm extends BaseFFTBearingAlgorithm {

	private BearingLocaliserControl bearingLocaliserControl;
	private int groupChannels, groupHydrophones;
	private int arrayShape;
	private int nChannels;
	private int nPairs;
	private ComplexArray[] pairedConjugates;
	private Correlations correlations = new Correlations();
	private BearingLocaliser bearingLocaliser;
	private String algoName;
	private Object maxDelaySeconds;
	private TOADBearingProvider toadBearingProvider;
	private TOADPlot toadBearingPlot;
	private double[][] pairedConjugateScales;

	public TOADBearingAlgorithm(TOADBearingProvider toadBearingProvider, BearingProcess bearingProcess, BearingAlgorithmParams algorithmParams, int groupIndex) {
		super(bearingProcess, algorithmParams, groupIndex);
		this.toadBearingProvider = toadBearingProvider;
		bearingLocaliserControl = bearingProcess.getBearingLocaliserControl();
		if (algorithmParams == null) {
			setAlgorithmParams(algorithmParams = new TOADBearingParams(groupIndex, groupChannels));
		}
		BearingLocaliserParams params = bearingLocaliserControl.getBearingLocaliserParams();
		algoName = toadBearingProvider.getStaticProperties().getShortName();
		GroupedSourceParameters groupParams = params.getRawOrFFTSourceParameters();
		if (groupParams.countChannelGroups() <= groupIndex) {
			System.out.println("Error in TOAD Localiser. not enought channel groups inconfiguratin");
			return;
		}
		
		groupChannels = groupParams.getGroupChannels(groupIndex);
		ArrayManager arrayManager = ArrayManager.getArrayManager();
		PamArray currentArray = arrayManager.getCurrentArray();
		groupHydrophones = groupChannels;
		FFTDataBlock fftSource = getFftSourceData();
		if (fftSource != null) {
			groupHydrophones = getFftSourceData().getChannelListManager().channelIndexesToPhones(groupHydrophones);
		}
		arrayShape = arrayManager.getArrayShape(currentArray, groupHydrophones);
		nChannels = PamUtils.getNumChannels(groupChannels);
		nPairs = (nChannels * (nChannels-1)) / 2;

		double timingError = 0;
		if (fftSource != null) {
			timingError = Correlations.defaultTimingError(fftSource.getSampleRate());
		}
		bearingLocaliser = BearingLocaliserSelector.createBearingLocaliser(groupHydrophones, timingError);
		if (bearingLocaliser instanceof MLLineBearingLocaliser2) {
			TOADBearingParams tbp = (TOADBearingParams) algorithmParams;
			int[] thetaDeg = tbp.getBearingHeadings();
			int[] phiDeg = tbp.getBearingSlants();
			double[] theta = {Math.toRadians(thetaDeg[0]), Math.toRadians(thetaDeg[1]), Math.toRadians(thetaDeg[2])};
			double[] phi = {0., 0., 0.};
			((MLLineBearingLocaliser2) bearingLocaliser).setAnalysisAngles(theta, phi);
		}
		else if (bearingLocaliser instanceof MLGridBearingLocaliser2) {
			TOADBearingParams tbp = (TOADBearingParams) algorithmParams;
			int[] thetaDeg = tbp.getBearingHeadings();
			int[] phiDeg = tbp.getBearingSlants();
			double[] theta = {Math.toRadians(thetaDeg[0]), Math.toRadians(thetaDeg[1]), Math.toRadians(thetaDeg[2])};
			double[] phi = {Math.toRadians(phiDeg[0]), Math.toRadians(phiDeg[1]), Math.toRadians(phiDeg[2])};
			((MLGridBearingLocaliser2) bearingLocaliser).setAnalysisAngles(theta, phi);
		}
		maxDelaySeconds = ArrayManager.getArrayManager().getCurrentArray().getMaxPhoneSeparation(groupHydrophones, PamCalendar.getTimeInMillis()) / 
				ArrayManager.getArrayManager().getCurrentArray().getSpeedOfSound();
	}

	@Override
	public BearingLocalisation processFFTData(PamDataUnit pamDataUnit, BearingAlgorithmGroup beamGroup,
			FFTDataList fftDataList) {
		ArrayList<FFTDataUnit> fftDataUnits = fftDataList.getFftDataUnits();
		if (fftDataUnits.size() == 0) {
			return null;
		}
		int fftLen = getFftSourceData().getFftLength(); 
		checkFFTAllocation(fftLen);
		zeroComplexData(fftLen/2); // only need to zero the first half
		ComplexArray[] fftGroup = new ComplexArray[nChannels];
		/**
		 * Set the frequency range for analysis
		 */
		int bin1 = 0, bin2 = fftLen/2;
		double[] fRange = pamDataUnit.getFrequency();
		if (fRange != null && fRange.length >= 2) {
			int[] binRange = frequencyToBin(fRange);
			bin1 = binRange[0];
			bin2 = binRange[1];
		}
		int nChannelGroups = fftDataUnits.size() / nChannels;
		/*
		 * Now see if the data unit has more detailed contour information that
		 * we might use ...
		 */
		TFContourData contourData = null;
		int nContourCont = 0;
		if (pamDataUnit instanceof TFContourProvider) {
			TFContourProvider cp = (TFContourProvider) pamDataUnit;
			contourData = cp.getTFContourData();
			if (contourData != null) {
				nContourCont = contourData.getContourTimes().length;
				// check if it's simple to match the contour info with the FFT data:
				//				int nFFT = fftDataList.size() / PamUtils.getNumChannels(beamGroup.channelMap);
				//				System.out.printf("Contour with %d slices for %d FFT datas dt1 = %d\n", 
				//						contourData.getContourTimes().length, nFFT, fftDataList.get(0).getTimeMilliseconds()-contourData.getContourTimes()[0]);
			}
		}
		
		int lastChannel = PamUtils.getHighestChannel(groupChannels);
		int iGroupCount = 0;
		double[] a1, a2, ap;
		int minBin1 = fftLen;
		int maxBin2 = 0;
		for (FFTDataUnit fftDataUnit:fftDataUnits) {
			int iPos = PamUtils.getChannelPos(PamUtils.getSingleChannel(fftDataUnit.getChannelBitmap()), groupChannels);
			fftGroup[iPos] = fftDataUnit.getFftData();
			if (iPos == fftGroup.length-1) {
				// time to process...
				// get the sum o fcontent of each fft in the group for scaling...
				double[] fftScale = new double[nChannels];
				for (int i = 0; i < nChannels; i++) {
					fftScale[i] = ComplexArray.sumSquared(fftGroup[i].getData());
				}
				
				if (iGroupCount < nContourCont) {
					bin1 = frequencyToBin(contourData.getLowFrequency()[iGroupCount])-1;
					bin2 = frequencyToBin(contourData.getHighFrequecy()[iGroupCount])+2;
					bin1 = Math.max(bin1, 0);
					bin2 = Math.min(bin2, fftDataUnit.getFftData().length());
					iGroupCount++;
				}
				else if (fftDataUnit.getUsefulBinRange() != null) { 
					/*
					 *  new system - might still be null for many ...
					 *  but should make this work for classified clicks. 
					 */
					bin1 = fftDataUnit.getUsefulBinRange()[0];
					bin2 = fftDataUnit.getUsefulBinRange()[1];
				}
				minBin1 = Math.min(bin1, minBin1);
				maxBin2 = Math.max(bin2, maxBin2);
				// calculate one by comp conj (time reversal) of the other
				int iPair = 0;
				for (int i = 0; i < nChannels; i++) {
					a1 = fftGroup[i].getData();
					for (int j = i+1; j < nChannels; j++) {
						a2 = fftGroup[j].getData();
						double scale = Math.sqrt(fftScale[i]*fftScale[j])*2*nChannelGroups;
						double[] pairedScales = pairedConjugateScales[iPair];
						ap = pairedConjugates[iPair++].getData();
						for (int ir = bin1*2, ii = ir+1; ir < bin2*2; ir+=2, ii+=2) {
							pairedScales[0] += a1[ir]*a1[ir]+a1[ii]*a1[ii];
							pairedScales[1] += a2[ir]*a2[ir]+a2[ii]*a2[ii];
							ap[ir] += (a1[ir]*a2[ir]+a1[ii]*a2[ii]);///scale;
							ap[ii] += (-a1[ir]*a2[ii]+a1[ii]*a2[ir]);///scale;
						}
					}
				}
			}
		}
		// now fill in all the conjugate pairs in the back half of the arrays and invert. 
		double[] delays = new double[nPairs];
		float sampleRate = getFftSourceData().getSampleRate();
		double[] maxDelays = correlations.getMaxDelays(sampleRate, groupHydrophones, pamDataUnit.getTimeMilliseconds());
//		System.out.printf("Group delays: ");
		double scale1 = 0, scale2 = 0;
		for (int i = 0; i < nPairs; i++) {
			ap = pairedConjugates[i].getData();
			double normScale = Math.sqrt(pairedConjugateScales[i][0]*pairedConjugateScales[i][1])*2;
			for (int ir1 = minBin1*2, im1 = ir1+1, ir2 = ap.length-minBin1*2-2, im2 = ir2+1; ir1 < maxBin2*2; ir1+=2, im1+=2, ir2-=2, im2-=2) {
				ap[ir1]/=normScale;
				ap[im1]/=normScale;
				ap[ir2] = ap[ir1];
				ap[im2] = -ap[im1];
			}
			correlations.getFastFFT().ifft(pairedConjugates[i], fftLen);
			double[] peakPos = correlations.getInterpolatedPeak(pairedConjugates[i], 1, maxDelays[i]);
			delays[i] = (peakPos[0]) / sampleRate;
//			System.out.printf(", %3.3f", peakPos[0]);
		}
//		System.out.printf("\n");
		double[][] locBearings = bearingLocaliser.localise(delays, pamDataUnit.getTimeMilliseconds());

//		if (pamDataUnit.getDurationInMilliseconds() > 300) {
//			Debug.out.printf("TOAD Primary angle for UID %d = %3.1f\n", pamDataUnit.getUID(), Math.toDegrees(locBearings[0][0]));
//		}
		/*
		 * this is a pain. The localiser is referenced to xyz axis, not the array axis (y), so bearings are in 
		 * a different frame to expected. This is easily rectified by the following line. Ideally we should be using 
		 * the arrayAxis, which works in RT, but the array axis is not saved when the localisation gets saved in binary
		 * files, so it's easier to not have to use the array axis by rotating these results to the array axis now ...
		 * Wrong - this line does NOT seem to be needed in any scenarios I can simulate. 
		 */
//		locBearings[0][0] = Math.PI/2-locBearings[0][0];
		
//		if (arrayShape == ArrayManager.ARRAY_TYPE_PLANE) {
//			locBearings[0][0] = Math.PI/2-locBearings[0][0];
//		}

		PamVector[] arrayAxis = bearingLocaliser.getArrayAxis();
		double[] arrayAngles = PamVector.getMinimalHeadingPitchRoll(arrayAxis);
		BearingLocalisation bl = new BearingLocalisation(pamDataUnit, algoName, 
				LocContents.HAS_BEARING, groupHydrophones, locBearings[0], locBearings[1], arrayAngles);
		bl.setSubArrayType(arrayShape);
		bl.setArrayAxis(arrayAxis);
		pamDataUnit.setLocalisation(bl);
		
		if (toadBearingPlot != null) {
			toadBearingPlot.plotData(pamDataUnit, bearingLocaliser, locBearings);
		}
		
		return bl;
	}

	private void checkFFTAllocation(int fftLength) {
		if (nPairs == 0) return;
		if (pairedConjugates == null || pairedConjugates.length != nPairs || pairedConjugates[0].length() != fftLength) {
			pairedConjugates = new ComplexArray[nPairs];
			for (int i = 0; i < nPairs; i++) {
				pairedConjugates[i] = new ComplexArray(fftLength);
			}
			pairedConjugateScales = new double[nPairs][2];
		}
	}

	/**
	 * Zero the first half of all the complex arrays.
	 * @param len Length to zero
	 */
	private void zeroComplexData(int len) {
		for (int i = 0; i < nPairs; i++) {
			pairedConjugates[i].setZero();
			pairedConjugateScales[i][0] = pairedConjugateScales[i][1] = 0.; 
		}
	}
	
	@Override
	public BearingDataDisplay createDataDisplay() {
		String plotName;
		if (bearingLocaliser instanceof MLGridBearingLocaliser2) {
			plotName = "TOA Max Likelihood Ch " + PamUtils.getChannelList(groupChannels);
			toadBearingPlot = new TOAD2DPlot(this, plotName, bearingLocaliser, (TOADBearingParams) getAlgorithmParams());
		}
		else if (bearingLocaliser instanceof PairBearingLocaliser) {
			plotName = "TOA Pair Localiser Ch " + PamUtils.getChannelList(groupChannels);
			toadBearingPlot = new TOADPairPlot(this, plotName, bearingLocaliser, (TOADBearingParams) getAlgorithmParams());
		}
		return toadBearingPlot;
	}

	/**
	 * @return the correlations
	 */
	public Correlations getCorrelations() {
		return correlations;
	}


}
