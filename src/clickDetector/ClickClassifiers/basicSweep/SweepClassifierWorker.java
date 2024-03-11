package clickDetector.ClickClassifiers.basicSweep;

import java.util.Arrays;

import Filters.SmoothingFilter;
import Localiser.algorithms.Correlations;
import PamModel.SMRUEnable;
import PamUtils.PamArrayUtils;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import Spectrogram.WindowFunction;
import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import clickDetector.ClickClassifiers.ClickIdInformation;
import fftFilter.FFTFilterParams;
import fftManager.FastFFT;

/**
 * Do the actual work of the click seep classifier
 * Separated into a separate class for clarity and to keep 
 * Separate from all the control functions. 
 * @author Doug Gillespie
 *
 */
public class SweepClassifierWorker {

	/**
	 * Reference to the sweep classifier. 
	 */
	private SweepClassifier sweepClassifier;

	/**
	 * Reference to the click control.
	 */
	private ClickControl clickControl;

	/**
	 * Class for making correlations
	 */
	private Correlations correlations = new Correlations(); 

	int nChannels;
	double sampleRate;
	int[][] lengthData;
	double[][] specData;
	double[][] smoothSpecData;
	ZeroCrossingStats[] zeroCrossingStats;

	public SweepClassifierWorker(ClickControl clickControl,
			SweepClassifier sweepClassifier) {
		super();
		this.clickControl = clickControl;
		this.sweepClassifier = sweepClassifier;

	}

	/**
	 * Classifiy the click based on a sequential list of clicks classifiers. The
	 * click is identified as the first sweep classifier it passes in the list. If
	 * checkAllClassifiers is enabled in the sweep params then the click is tested
	 * by all classiifers and a list of passed classifiers is saved in the
	 * ClickIdInformation.
	 * 
	 * @param click
	 *            - the click to test
	 * @return classification informaiton.
	 */
	public synchronized ClickIdInformation identify(ClickDetection click) {
		clearExtractedParams();
		int usedChannels = SMRUEnable.getGoodChannels(click.getChannelBitmap());
		nChannels = PamUtils.getNumChannels(usedChannels);
		sampleRate = clickControl.getClickDetector().getSampleRate();

		int n = sweepClassifier.sweepClassifierParameters.getNumSets();
		SweepClassifierSet scs;
		boolean passed; 
		ClickIdInformation clickInfo = null;
		int[] classificationPass = new int[n]; 
		int passn=0; 
		for (int i = 0; i < n; i++) {
			scs = sweepClassifier.sweepClassifierParameters.getSet(i);
			if (scs.enable) {
				passed = classify(click, scs); 
				if (passed && sweepClassifier.sweepClassifierParameters.checkAllClassifiers) {
					if (clickInfo==null) clickInfo = new ClickIdInformation(scs.getSpeciesCode(), scs.getDiscard());
					classificationPass[passn]=scs.getSpeciesCode(); 
					passn++; 
				}
				else if (passed) {
					return new ClickIdInformation(scs.getSpeciesCode(), scs.getDiscard());
				}
			}
		}

		//return the first clickInfo which passed or null.
		if (clickInfo==null) return new ClickIdInformation(0);
		else {
			//trim the classifiation array and save
			clickInfo.classifiersPassed=Arrays.copyOfRange(classificationPass,0,passn);
			return clickInfo;
		}
	}

	/**
	 * Binary classification of a clicks.
	 * @param click - the click to test. 
	 * @param scs - the sweep classifier parameters. 
	 * @return true if the click passes the classification. 
	 */
	private boolean classify(ClickDetection click, SweepClassifierSet scs) {

		if (scs.enableLength) {
			if (testLength(click, scs) == false) {
				return false;
			}
		}
		if (scs.testAmplitude) {
			if (testAmplitude(click, scs) == false) {
				return false;
			}
		}
		if (scs.enableEnergyBands) {
			if (testEnergyBands(click, scs) == false) {
				return false;
			}
		}
		if (scs.enablePeak) {
			if (testPeakFreq(click, scs) == false) {
				return false;
			}
		}
		if (scs.enableWidth) {
			if (testPeakWidth(click, scs) == false) {
				return false;
			}
		}
		if (scs.enableMean) {
			if (testMeanFreq(click, scs) == false) {
				return false;
			}
		}
		if (scs.enableZeroCrossings || scs.enableSweep) {
			if (testZeroCrossings(click, scs) == false) {
				return false;
			}
		}
		if (scs.enableMinXCrossCorr || scs.enablePeakXCorr) {
			if (testXCorr(click, scs) == false) {
				return false;
			}
		}

		if (scs.enableBearingLims) {
			if (testBearings(click, scs) == false) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Test the click amplitude range. 
	 * @param click - the click detector
	 * @param scs - the sweep classifier parameters
	 * @return true if test passed
	 */
	private boolean testAmplitude(ClickDetection click, SweepClassifierSet scs) {
		double meanAmp = 0;
		double[] r = scs.amplitudeRange;
		if (r == null) {
			return false;
		}
		for (int i = 0; i < nChannels; i++) {
			double amp = click.linAmplitudeToDB(click.getAmplitude(i));
			switch (scs.channelChoices) {
			case SweepClassifierSet.CHANNELS_REQUIRE_ALL:
				if (amp < r[0] || amp > r[1]) {
					return false;
				}
				break;
			case SweepClassifierSet.CHANNELS_REQUIRE_ONE:
				if (amp >= r[0] && amp <= r[1]) {
					return true;
				}
				break;
			case SweepClassifierSet.CHANNELS_USE_MEANS:
				meanAmp += amp;
				break;
			}
		}
		switch (scs.channelChoices) {
		case SweepClassifierSet.CHANNELS_REQUIRE_ALL:
			return true;
		case SweepClassifierSet.CHANNELS_REQUIRE_ONE:
			return false;
		case SweepClassifierSet.CHANNELS_USE_MEANS:
			meanAmp /= nChannels;
			return (meanAmp >= r[0] && meanAmp <= r[1]);
		}

		return false;
	}

	private boolean testLength(ClickDetection click, SweepClassifierSet scs) {
		int[][] lData = getLengthData(click, scs);
		if (lData == null) {
			return false;
		}
		double[] realLengthData = new double[nChannels];
		for (int i = 0; i < nChannels; i++) {
			realLengthData[i] = (lData[i][1]-lData[i][0]) / sampleRate * 1000;
		}
		switch (scs.channelChoices) {
		case SweepClassifierSet.CHANNELS_REQUIRE_ALL:
			for (int i = 0; i < nChannels; i++) {
				if (realLengthData[i] < scs.minLength || realLengthData[i] > scs.maxLength) {
					return false;
				}
			}
			return true;
		case SweepClassifierSet.CHANNELS_REQUIRE_ONE:
			for (int i = 0; i < nChannels; i++) {
				if (realLengthData[i] >= scs.minLength && realLengthData[i] <= scs.maxLength) {
					return true;
				}
			}
			return false;
		case SweepClassifierSet.CHANNELS_USE_MEANS:
			double m = realLengthData[0];
			for (int i = 1; i < nChannels; i++) {
				m += realLengthData[i];
			}
			m /= nChannels;
			return (m >= scs.minLength && m <= scs.maxLength);
		}
		return false;
	}

	private boolean testEnergyBands(ClickDetection click, SweepClassifierSet scs) {
		double[][] specData = getSpecData(click, scs);
		double[] testEnergy = new double[nChannels];
		double[][] controlEnergy = new double[nChannels][SweepClassifierSet.nControlBands];
		int nSpecs = specData.length;
		for (int i = 0; i < nSpecs; i++) {
			testEnergy[i] = 10*Math.log10(pickSpecEnergy(specData[i], scs.testEnergyBand));
			for (int b = 0; b < SweepClassifierSet.nControlBands; b++) {
				controlEnergy[i][b] = 10*Math.log10(pickSpecEnergy(specData[i], scs.controlEnergyBand[b]));
			}
		}
		switch (scs.channelChoices) {
		case SweepClassifierSet.CHANNELS_REQUIRE_ALL:
			for (int i = 0; i < nChannels; i++) {
				for (int b = 0; b < SweepClassifierSet.nControlBands; b++) {
					if (testEnergy[i] - controlEnergy[i][b] < scs.energyThresholds[b]) {
						return false;
					}
				}
			}
			return true;
		case SweepClassifierSet.CHANNELS_REQUIRE_ONE:
			int okBands;
			for (int i = 0; i < nChannels; i++) {
				okBands = 0;
				for (int b = 0; b < SweepClassifierSet.nControlBands; b++) {
					if (testEnergy[i] - controlEnergy[i][b] >= scs.energyThresholds[b]) {
						okBands ++;
					}
				}
				if (okBands == SweepClassifierSet.nControlBands) {
					return true;
				}
			}
			return false;
		case SweepClassifierSet.CHANNELS_USE_MEANS:
			// sum all data onto the first channel bin.
			for (int b = 0; b < SweepClassifierSet.nControlBands; b++) {
				if (testEnergy[0] - controlEnergy[0][b] < scs.energyThresholds[b]) {
					return false;
				}
			}
			return true;

		}

		return true;
	}

	/**
	 * Get some energy measurement from some spectral data. 
	 * @param specData single channel of power spectrum data
	 * @param frequency limits (Hz)
	 * @return summed energy (allowing for non integer bins)
	 */
	private double pickSpecEnergy(double[] specData, double[] freqLims) {
		double binsPerHz = specData.length * 2 / sampleRate;
		double rBin1 = freqLims[0] * binsPerHz;
		double rBin2 = freqLims[1] * binsPerHz;
		rBin1 = Math.max(rBin1, 0);
		rBin1 = Math.min(rBin1, specData.length);
		rBin2 = Math.max(rBin2, 0);
		rBin2 = Math.min(rBin2, specData.length);
		if (rBin2 <= rBin1) return 0;
		int bin1 = (int) Math.floor(rBin1);
		int bin2 = (int) Math.ceil(rBin2);
		bin2 = Math.min(bin2, specData.length-1);
		if (bin1 < 0 || bin2 >= specData.length) {
			return Double.NaN;
		}
		double e = 0;
		for (int i = bin1; i < bin2; i++) {
			e += specData[i];
		}
		e -= specData[bin1] * (rBin1-bin1);
		e -= specData[bin2-1] * (bin2-rBin2);
		return e;
	} 

	int[] peakBins;
	private int[] getPeakBins(ClickDetection click, SweepClassifierSet scs) {
		if (peakBins != null && peakBins.length == PamUtils.getNumChannels(click.getChannelBitmap())) {
			return peakBins;
		}
		double[][] specData = getSmoothSpecData(click, scs);
		double binsPerHz = specData.length * 2 / sampleRate;
		if (specData == null) {
			return null;
		}
		int nSpecs = specData.length;
		peakBins = new int[nSpecs];
		for (int i = 0; i < nSpecs; i++) {
			peakBins[i] = getPeakBin(specData[i], scs.peakSearchRange);
		}
		return peakBins;
	}
	private boolean testPeakFreq(ClickDetection click, SweepClassifierSet scs) {
		int[] peakBin = getPeakBins(click, scs);
		if (peakBin == null) {
			return false;
		}
		double binsPerHz = specData[0].length * 2 / sampleRate;
		double[] peakFreq = new double[peakBin.length];
		int nSpecs = specData.length;
		//		if (peakBin.length < nSpecs || specData.length < nSpecs) {
		//			System.err.println("About to crash");
		//			peakBin = getPeakBins(click, scs);
		//		}
		for (int i = 0; i < nSpecs; i++) {
			peakBin[i] = getPeakBin(specData[i], scs.peakSearchRange);
			peakFreq[i] = peakBin[i] / binsPerHz;
		}

		switch (scs.channelChoices) {
		case SweepClassifierSet.CHANNELS_REQUIRE_ALL:
			for (int i = 0; i < nSpecs; i++) {
				if (peakFreq[i] < scs.peakRange[0] || peakFreq[i] > scs.peakRange[1]) {
					return false;
				}
			}
			return true;
		case SweepClassifierSet.CHANNELS_REQUIRE_ONE:
			for (int i = 0; i < nSpecs; i++) {
				if (peakFreq[i] >= scs.peakRange[0] && peakFreq[i] <= scs.peakRange[1]) {
					return true;
				}
			}
			return false;
		case SweepClassifierSet.CHANNELS_USE_MEANS:
			if (peakFreq[0] >= scs.peakRange[0] && peakFreq[0] <= scs.peakRange[1]) {
				return true;
			}
			return false;
		}

		return true;
	}

	private int getPeakBin(double[] specData, double[] peakSearchRange) {
		double binsPerHz = getBinsPerHz();
		double rBin1 = peakSearchRange[0] * binsPerHz;
		double rBin2 = peakSearchRange[1] * binsPerHz;
		rBin1 = Math.max(rBin1, 0);
		rBin2 = Math.min(rBin2, specData.length);
		int bin1 = (int) Math.floor(rBin1);
		int bin2 = (int) Math.ceil(rBin2);
		double maxVal = specData[bin1];
		int maxInd = bin1;
		for (int i = bin1; i < bin2; i++) {
			if (specData[i] > maxVal) {
				maxVal = specData[i];
				maxInd = i;
			}
		}
		return maxInd;
	}

	private boolean testPeakWidth(ClickDetection click, SweepClassifierSet scs) {
		int[] peakBin = getPeakBins(click, scs);
		double[][] specData = getSmoothSpecData(click, scs);
		int nSpec = peakBin.length;
		int[] peakWidth = new int[nSpec];
		for (int i = 0; i < nSpec; i++) {
			peakWidth[i] = getPeakWidth(specData[i], peakBin[i], scs.peakWidthThreshold);
		}
		int f1, f2;
		double binsPerHz = getBinsPerHz();
		f1 = (int) (scs.peakWidthRange[0] * binsPerHz);
		f2 = (int) (scs.peakWidthRange[1] * binsPerHz);
		switch(scs.channelChoices) {
		case SweepClassifierSet.CHANNELS_REQUIRE_ALL:
			for (int i = 0; i < nSpec; i++) {
				if (peakWidth[i] < f1 || peakWidth[i] > f2) {
					return false;
				}
			}
			return true;
		case SweepClassifierSet.CHANNELS_REQUIRE_ONE:
			for (int i = 0; i < nSpec; i++) {
				if (peakWidth[i] >= f1 && peakWidth[i] <= f2) {
					return true;
				}
			}
			return false;
		case SweepClassifierSet.CHANNELS_USE_MEANS:
			if (peakWidth[0] >= f1 && peakWidth[0] <= f2) {
				return true;
			}
			return false;
		}
		return true;
	}

	private int getPeakWidth(double[] specData, int peakBin, double peakWidthThreshold) {

		double thresh = specData[peakBin];
		thresh /= Math.pow(10, Math.abs(peakWidthThreshold)/10);
		int lData = specData.length;
		int bin1, bin2;
		bin1 = bin2 = peakBin;
		for (int i = peakBin-1; i >= 0; i--) {
			if (specData[i] >= thresh) {
				bin1 = i;
			}
			else {
				break;
			}
		}
		for (int i = peakBin+1; i < lData; i++) {
			if (specData[i] >= thresh) {
				bin2 = i;
			}
			else {
				break;
			}
		}

		return bin2-bin1+1;
	}

	private boolean testMeanFreq(ClickDetection click, SweepClassifierSet scs) {
		double[][] specData = getSpecData(click, scs);
		double binsPerHz = getBinsPerHz();
		double rBin1 = scs.peakSearchRange[0] * binsPerHz;
		double rBin2 = scs.peakSearchRange[1] * binsPerHz;
		rBin1 = Math.max(rBin1, 0);
		rBin2 = Math.min(rBin2, specData[0].length);
		int bin1 = (int) Math.floor(rBin1);
		int bin2 = (int) Math.ceil(rBin2);
		int nSpec = specData.length;
		double[] meanFreq = new double[nSpec];
		double a, b;
		for (int c = 0; c < nSpec; c++) {
			a = b = 0;
			for (int i = bin1; i < bin2; i++) {
				a += (i*specData[c][i]);
				b += specData[c][i];
			}
			meanFreq[c] = a / b / binsPerHz;
		}


		switch (scs.channelChoices) {
		case SweepClassifierSet.CHANNELS_REQUIRE_ALL:
			for (int i = 0; i < nChannels; i++) {
				if (meanFreq[i] < scs.meanRange[0] || meanFreq[i] > scs.meanRange[1]) {
					return false;
				}
			}
			return true;
		case SweepClassifierSet.CHANNELS_REQUIRE_ONE:
			for (int i = 0; i < nChannels; i++) {
				if (meanFreq[i] >= scs.meanRange[0] && meanFreq[i] <= scs.meanRange[1]) {
					return true;
				}
			}
			return false;
		case SweepClassifierSet.CHANNELS_USE_MEANS:
			if (meanFreq[0] >= scs.meanRange[0] && meanFreq[0] <= scs.meanRange[1]) {
				return true;
			}
			return false;
		}

		return true;
	}

	private boolean testZeroCrossings(ClickDetection click,
			SweepClassifierSet scs) {
		double[][] zeroCrossings = getZeroCrossings(click, scs);
		if (zeroCrossingStats == null) {
			return false;
		}
		switch (scs.channelChoices) {
		case SweepClassifierSet.CHANNELS_REQUIRE_ALL:
			for (int i = 0; i < nChannels; i++) {
				if (testZeroCrossingStat(zeroCrossingStats[i], scs) == false) {
					return false;
				}
			}
			return true;
		case SweepClassifierSet.CHANNELS_REQUIRE_ONE:
			for (int i = 0; i < nChannels; i++) {
				if (testZeroCrossingStat(zeroCrossingStats[i], scs) == true) {
					return true;
				}
			}
			return false;
		case SweepClassifierSet.CHANNELS_USE_MEANS:
			ZeroCrossingStats meanStats = new ZeroCrossingStats();
			for (int i = 0; i < nChannels; i++) {
				meanStats.nCrossings += zeroCrossingStats[i].nCrossings;
				meanStats.startFreq += zeroCrossingStats[i].startFreq;
				meanStats.endFreq += zeroCrossingStats[i].endFreq;
				meanStats.sweepRate += zeroCrossingStats[i].sweepRate;
			}
			meanStats.nCrossings /= nChannels;
			meanStats.startFreq /= nChannels;
			meanStats.endFreq /= nChannels;
			meanStats.sweepRate /= nChannels;
			return testZeroCrossingStat(meanStats, scs);
		}

		return false;
	}

	/**
	 * Test whether the zero crossing statistics pass sweep classifier tests
	 * @param zcStat - the zero crossings statistics. 
	 * @param scs - sweep classifier parameters. 
	 * @return true if passed. 
	 */
	public synchronized boolean testZeroCrossingStat(ZeroCrossingStats zcStat, SweepClassifierSet scs) {
		if (scs.enableZeroCrossings) {
			if (zcStat.nCrossings < scs.nCrossings[0] || zcStat.nCrossings > scs.nCrossings[1]) {
				return false;
			}
		}
		if (scs.enableSweep) {
			double sweep = zcStat.sweepRate / 1e6; // convert to kHz per millisecond.
			if (sweep < scs.zcSweep[0] || sweep > scs.zcSweep[1]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Test the cross correlation values for multi-channel detections. Note that this is 
	 * 
	 * @param click - the click	detection to select. 
	 * @param scs - sweep classifier parameters.
	 * @return true if correlation conditions are met. 
	 */
	private boolean testXCorr(ClickDetection click, SweepClassifierSet scs) {
		/**
		 * Note that calculating the correlation value again is not particularly processor intensive however
		 * 1) This is unlikely to be used very often
		 * 2) The framework to save correlation values is quite substantial including alteration to binary files. 
		 * 
		 * We also keep this simple correlating the waveform without using time delay parameters. 
		 */
		int nChan = click.getNChan(); 
		int iOut = 0; 

		double[] minXCorrVals = new double[click.getDelaysInSamples().length];
		double[] maxXCorrVals = new double[click.getDelaysInSamples().length];
		boolean[] minMaxPeak = new boolean[click.getDelaysInSamples().length]; //whether the trough is higher than the maxpeak*corrFactor

		double[][] wave = click.getWaveData(scs.enableFFTFilter, scs.fftFilterParams);

		if (click.getNChan()>1) {
			double[] correlationFunc;
			for (int i = 0; i < nChan; i++) {
				for (int j = i+1; j < nChan; j++, iOut++) {		
					correlationFunc = correlations.getCorrelation(wave[i], wave[j], false); 

					minXCorrVals[i] = PamArrayUtils.min(correlationFunc); 
					maxXCorrVals[i] = PamArrayUtils.max(correlationFunc); 
					//check whether the the maximum value of the correlation function is greater than the absolutle value 
					//of the trough. 
					minMaxPeak[i] =   maxXCorrVals[i] > scs.corrFactor*Math.abs(minXCorrVals[i]); 

					//Debug.out.println("wave: " + i + " wave: " + j + " max val: " + maxXCorrVals[i]); 

				}
			}
			//now perform the classifier tests
			double maxCorr = PamArrayUtils.max(maxXCorrVals);

			//Debug.out.println("maxCorr: " +  maxCorr + " minCorr: " + PamArrayUtils.max(minXCorrVals) + " corrFactor: " + scs.corrFactor); 

			//do any of maximum correlation value pass the minimum correlation value
			//is there at least instance of the m
			if (scs.enableMinXCrossCorr && maxCorr<scs.minCorr) return false;
			if (scs.enablePeakXCorr && !PamArrayUtils.isATrue(minMaxPeak)) return false; 

			return true; 
		}
		else {
			//should not be used as should be disabled...
			return true; 
		}
	}

	/**
	 * Test the bearings
	 * @param click - the click detection 
	 * @param scs - the sweep classifier set
	 * @return true if bearing limits are passed
	 */
	private boolean testBearings(ClickDetection click, SweepClassifierSet scs) {
		if (click.getLocalisation()==null) return true;
		
		if (click.getLocalisation().getAngles()==null) return true; //passes the test if there is no bearing info. 

		double bearing = click.getLocalisation().getAngles()[0]; 

		//test if the bearings is within limits then return whether that means a classification or no classification
		if (bearing>scs.bearingLims[0] & bearing<scs.bearingLims[1]) {
			return !scs.excludeBearingLims; 
		}
		else return scs.excludeBearingLims;
	}



	double[][] zeroCrossings;
	private synchronized double[][] getZeroCrossings(ClickDetection click,
			SweepClassifierSet scs) {
		if (zeroCrossings == null) {
			zeroCrossings = createZeroCrossings(click, scs);
		}

		return zeroCrossings;
	}

	private synchronized double[][] createZeroCrossings(ClickDetection click,
			SweepClassifierSet scs) {
		double[][] waveData = click.getWaveData(scs.enableFFTFilter, scs.fftFilterParams);
		if (waveData == null) {
			return null;
		}

		// 2014/11/12 Rocca serialVersionUID = 15; added because when in ViewerMode, sampleRate=0
		if (sampleRate==0) {
			sampleRate=click.getClickDetector().getSampleRate();
		}

		int[][] lengthData = getLengthData(click, scs);
		if (lengthData == null) {
			return null;
		}
		double[][] zeroCrossings = new double[nChannels][];
		zeroCrossingStats = new ZeroCrossingStats[nChannels];
		int[] chLUT = SMRUEnable.makeUsedChannelLUT(click.getChannelBitmap());
		for (int i = 0; i < nChannels; i++) {
			zeroCrossings[i] = createZeroCrossings(waveData[chLUT[i]], lengthData[i]);
			zeroCrossingStats[i] = new ZeroCrossingStats(zeroCrossings[i], sampleRate);
		}

		// save the zeroCrossingStats information to the click detection object for use
		// by Rocca during classification
		// 2014/08/06 MO
		click.setZeroCrossingStats(zeroCrossingStats);

		return zeroCrossings;
	}

	/**
	 * Work out zero crossings for one channel between given limits. 
	 * @param waveData
	 * @param lengthData
	 * @return array of zero crossing times. 
	 */
	private double[] createZeroCrossings(double[] waveData, int[] lengthData) {
		/*
		 * Make an array that must be longer than needed, then 
		 * cut it down to size at the end - will be quicker than continually
		 * growing an array inside a loop. 
		 */
		double[] zc = new double[lengthData[1]-lengthData[0]]; // longer than needed. 
		double lastPos = -1;
		double exactPos;
		int nZC = 0;
		for (int i = lengthData[0]; i < lengthData[1]-1; i++) {
			if (waveData[i] * waveData[i+1] > 0) {
				continue; // no zero crossing between these samples
			}
			exactPos = i + waveData[i] / (waveData[i] - waveData[i+1]);
			/*
			 * Something funny happens if a value is right on zero since the 
			 * same point will get selected twice, so ensure ignore ! 
			 */
			if (exactPos > lastPos) {
				lastPos = zc[nZC++] = exactPos;
			}
		}
		return Arrays.copyOf(zc, nZC);
	}

	// remember the last length threshold and smoothing term 
	// re-make the wave if they have changed. 
	double lastLengthdB = 0;
	int lastLengthSmooth = 0;

	/**
	 * 
	 * Creates a 2D array of length data[channels][start/end]
	 * <p>
	 * Will only call getLengthData if it really has to. 
	 * really has to.<p> 
	 * 
	 * 2017/11/24 added synchronized keyword
	 * 
	 * @param click click
	 * @param scs classifier settings
	 */
	public synchronized int[][] getLengthData(ClickDetection click, SweepClassifierSet scs) {
		if (lengthData == null || Math.abs(scs.lengthdB) != lastLengthdB || 
				scs.lengthSmoothing != lastLengthSmooth) {
			createLengthData(click, scs);
			lastLengthdB = Math.abs(scs.lengthdB);
			lastLengthSmooth = scs.lengthSmoothing;
		}
		return lengthData;
	}

	/**
	 * Creates a 2D array of length data[channels][start/end]
	 * <p>
	 * Better to call getLengthData which will only call this if it
	 * really has to. 
	 * @param click click
	 * @param scs classifier settings
	 */
	private synchronized void createLengthData(ClickDetection click, SweepClassifierSet scs) {
		// 2014/11/12 Rocca serialVersionUID = 15; added because when in ViewerMode, nChannels=0 and sampleRate=0
		if (nChannels==0) {
			nChannels = click.getWaveData().length;		
		}

		int[][] tempLengthData = createLengthData(click,  nChannels, scs.lengthdB, 
				scs.lengthSmoothing, scs.enableFFTFilter, scs.fftFilterParams);

		// Rocca serialVersionUID = 22 changed from lengthData to local variable tempLengthData
		// copy new length data to the variable
		lengthData = tempLengthData;
	}

	/**
	 * Creates a 2D array of length data[channels][start/end]
	 * @param click - the click detection 
	 * @param nChannels - the number of channels to process
	 * @param lengthdB - the dB drop for peak finding
	 * @param lengthSmoothing - the number of bins to smooth waveform for length calculation 
	 * @return 2D array of length compesnated click bin positions to use for modified measurements. 
	 */
	public static int[][] createLengthData(ClickDetection click, int nChannels, double lengthdB, 
			int lengthSmoothing) {
		return createLengthData( click,  nChannels,  lengthdB, 
				lengthSmoothing, false, null);
	}

	/**
	 * Creates a 2D array of length data[channels][start/end]
	 * @param click - the click detection 
	 * @param nChannels - the number of channels to process
	 * @param lengthdB - the dB drop for peak finding
	 * @param lengthSmoothing - the number of bins to smooth waveform for length calculation 
	 * @param enableFFTFilter - true to use filter
	 * @param fftFilterParams - the filter params- this is null if no filter is used. 
	 * @return 2d array of length compensated click bin positions to use for modified measurements. 
	 */
	public static int[][] createLengthData(ClickDetection click, int nChannels, double lengthdB, 
			int lengthSmoothing, boolean enableFFTFilter, FFTFilterParams fftFilterParams) {
		int channelMap = click.getChannelBitmap();
		int usedChannelMap = SMRUEnable.getGoodChannels(channelMap);

		int[][] tempLengthData = new int[nChannels][2];		// 2015/09/13 Rocca serialVersionUID = 22 changed from lengthData to local variable tempLengthData
		double[] aWave;
		double maxVal;
		int maxIndex;
		double threshold;
		double threshRatio = Math.pow(10., Math.abs(lengthdB)/20);
		int waveLen;
		int p;
		int[] chLUT = SMRUEnable.makeUsedChannelLUT(channelMap);
		for (int i = 0; i < nChannels; i++) {
			aWave = click.getAnalyticWaveform(chLUT[i], enableFFTFilter, fftFilterParams);
			if (aWave == null) {
				return null;
			}
			aWave = SmoothingFilter.smoothData(aWave, lengthSmoothing);
			waveLen = aWave.length;
			maxVal = aWave[0];
			maxIndex = 0;
			for (int s = 1; s < waveLen; s++) {
				if (aWave[s] > maxVal) {
					maxVal = aWave[s];
					maxIndex = s;
				}
			}
			threshold = maxVal / threshRatio;
			p = maxIndex-1;
			//			try {
			tempLengthData[i][0] = 0;
			//			} catch (NullPointerException e) {
			//				System.out.println("NullPointerException i=" + i);
			//				System.out.println("NullPointerException lengthData[0][0]=" + lengthData[0][0]);
			//				System.out.println("NullPointerException lengthData[i][0]=" + lengthData[i][0]);
			//				System.out.println("waiting point");
			//			}
			for (; p >= 0; p--) {
				if (aWave[p] < threshold) {
					//					try {
					tempLengthData[i][0] = p+1;
					//					} catch (NullPointerException e) {
					//						System.out.println("NullPointerException i=" + i);
					//						System.out.println("NullPointerException lengthData[0][0]=" + lengthData[0][0]);
					//						System.out.println("waiting point");						
					//					}
					break;
				}
			}
			p = maxIndex+1;
			try {
				tempLengthData[i][1] = waveLen;
			} catch (NullPointerException e) {

			}
			for (; p < waveLen; p++) {
				if (aWave[p] < threshold) {
					tempLengthData[i][1] = p-1;
					break;
				}
			}
		}
		return tempLengthData; 
	}


	private double[][] getSmoothSpecData(ClickDetection click, SweepClassifierSet scs) {
		double[][] specData = getSpecData(click, scs);
		int nSpec = specData.length;
		smoothSpecData = new double[nSpec][];
		for (int i = 0; i < nSpec; i++) {
			smoothSpecData[i] = SmoothingFilter.smoothData(specData[i], scs.peakSmoothing);
		}

		return smoothSpecData;
	}

	private int lastPeakSmooth = 0;
	private double[][] getSpecData(ClickDetection click, SweepClassifierSet scs) {
		if (specData == null || lastPeakSmooth  != scs.peakSmoothing) {
			createSpecData(click, scs);
		}
		return specData;
	}

	private void createSpecData(ClickDetection click, SweepClassifierSet scs) {
		/** 
		 * have to decide whether to use the whole click, in which case can 
		 * get the power spectrum from the click, or if we've been told to 
		 * just get the data around the peak maximum
		 */
		double[][] tempData;
		if (scs.restrictLength) {
			tempData = createRestrictedLengthSpec(click, scs);
		}
		else {
			tempData = createSpec(click, scs);
		}
		if (tempData != null && tempData.length > 0 &&
				scs.channelChoices == SweepClassifierSet.CHANNELS_USE_MEANS) {
			specData = new double[1][];
			specData[0] = tempData[0];
			for (int i = 0; i < nChannels; i++) {
				for (int s = 1; s < tempData[0].length; s++) {
					specData[0][s] += tempData[i][s];
				}
			}
		}
		else {
			specData = tempData;
		}
	}
	private double getBinsPerHz() {
		if (specData == null) {
			return 0;
		}
		return specData[0].length * 2 / sampleRate;
	}
	private double[][] createRestrictedLengthSpec(ClickDetection click,
			SweepClassifierSet scs) {
		lengthData = getLengthData(click, scs);
		double[][] newSpecData = new double[nChannels][];
		int[] chLUT = SMRUEnable.makeUsedChannelLUT(click.getChannelBitmap());
		for (int iC = 0; iC < nChannels; iC++) {
			newSpecData[iC] = createRestrictedLenghtSpec(click, chLUT[iC], lengthData[iC], scs);
		}
		return newSpecData;
	}

	private FastFFT fastFFT = new FastFFT();
	private double[] createRestrictedLenghtSpec(ClickDetection click, int chan, int[] lengthPoints,
			SweepClassifierSet scs) {
		
		int startBin;
		if (scs.restrictedBinstype==SweepClassifierSet.CLICK_CENTER) {
			startBin = (lengthPoints[0] + lengthPoints[1] - scs.restrictedBins)/2;
		}
		else {
			startBin= 0; 
		}
		
		startBin = Math.max(0, startBin);
		int endBin = startBin + scs.restrictedBins;
		double[] waveData = click.getWaveData(chan, scs.enableFFTFilter, scs.fftFilterParams);
		endBin = Math.min(endBin, waveData.length);
		waveData = Arrays.copyOfRange(waveData, startBin, endBin);
		if (waveData.length < scs.restrictedBins) {
			waveData = Arrays.copyOf(waveData, scs.restrictedBins);
		}
		double[] win = getWindow(scs.restrictedBins);
		for (int i = 0; i < scs.restrictedBins; i++) {
			waveData[i]*=win[i];
		}
		//		if (scs.restrictedBins)
		int fftLen = FastFFT.nextBinaryExp(scs.restrictedBins);
		ComplexArray fftData = fastFFT.rfft(waveData, fftLen);
		double[] specData = new double[scs.restrictedBins/2];
		for (int i = 0; i < scs.restrictedBins/2; i++) {
			specData[i] = fftData.magsq(i);
		}
		return specData;
	}

	private double[] window;
	private double[] getWindow(int len) {
		if (window == null ||window.length != len) {
			window = WindowFunction.hann(len);
		}
		return window;
	}

	/**
	 * Get a copy of the ordinary power spectrum data for the click. 
	 * @param click click
	 * @param scs sweep param settings
	 */
	private double[][] createSpec(ClickDetection click, SweepClassifierSet scs) {
		double[][] newSpecData = new double[nChannels][];
		int[] chLUT = SMRUEnable.makeUsedChannelLUT(click.getChannelBitmap());
		for (int i = 0; i < nChannels; i++) {
			newSpecData[i] = click.getPowerSpectrum(chLUT[i]);
		}
		return newSpecData;
	}

	private synchronized void clearExtractedParams() {
		lengthData = null;
		specData = null;
		peakBins = null;
		smoothSpecData = null;
		zeroCrossings = null;
		zeroCrossingStats = null;
	}

	/**
	 * Returns the zeroCrossingStats variable, used as an identifier in the Rocca interface.
	 * 2014/07/25 MO
	 * 
	 * @param click the Click Detection
	 * @return 
	 */
	public synchronized ZeroCrossingStats[] getZeroCrossingStats(ClickDetection click) {
		// clear the parameters so that we force the code to recalculate the data
		clearExtractedParams();

		// get the sweep classifier set of parameters that matches the click type of the passed click
		SweepClassifierSet scs = null;
		for (int i=0; i<sweepClassifier.sweepClassifierParameters.getNumSets();i++) {
			if (sweepClassifier.sweepClassifierParameters.getSet(i).getSpeciesCode()== ((int) click.getClickType())) {
				scs = sweepClassifier.sweepClassifierParameters.getSet(0);
				break;
			}
		}


		// if we've found the correct sweep classifier set, call createZeroCrossings to generate the data
		if (scs != null) {
			zeroCrossings = createZeroCrossings(click, scs);
		}

		return zeroCrossingStats;
	}

	/**
	 * Clear the lengthData parameter.  Required when Rocca runs multiple analysis on the same clicks.  Once the length data is calculated,
	 *  it's no longer recalculated even when a different click is being analyzed. 
	 *  added by MO 2015/06/04
	 *  
	 *  2017/11/24 added synchronized keyword
	 */
	public synchronized void resetLengthData() {
		lengthData=null;
	}
}
