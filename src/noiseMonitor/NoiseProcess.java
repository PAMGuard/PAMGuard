package noiseMonitor;

import java.util.Arrays;
import java.util.Random;
import pamMaths.Mean;

import fftManager.Complex;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import Acquisition.AcquisitionProcess;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import beamformer.continuous.BeamFormerDataBlock;

public class NoiseProcess extends PamProcess {

	private NoiseControl noiseControl;

	private FFTDataBlock fftDataSource;

	private NoiseDataBlock noiseDataBlock;

	private NoiseLogging noiseLogging;

//	private String[] measureNames = {"mean", "median", "low95", "high95", "Min", "Max"};

	/**
	 * Index of the next measurement to make
	 */
	private int iMeasurement;

	private long[] measurementTimes;

	private long nextBlockStartSample;

	private Random r = new Random();

	private double[][][] measurementData;
	
	private AcquisitionProcess daqProcess;
	
	private NoiseBinaryDataSource noiseBinaryDataSource;
	
	/**
	 * Total gain of all upstream processes which will
	 * be divided off all noise measurements. 
	 */
	private double[] processGains;

	public NoiseProcess(NoiseControl noiseControl) {
		super(noiseControl, null);
		this.noiseControl = noiseControl;
		noiseDataBlock = new NoiseDataBlock(noiseControl.getUnitName(),this,0);
		noiseLogging = new NoiseLogging(noiseDataBlock);
		noiseDataBlock.SetLogging(noiseLogging);
		noiseBinaryDataSource = new NoiseBinaryDataSource(noiseControl, noiseDataBlock);
		noiseDataBlock.setBinaryDataSource(noiseBinaryDataSource);
		noiseDataBlock.setStatisticTypes(NoiseDataBlock.NOISE_MEAN | NoiseDataBlock.NOISE_MEDIAN |
				NoiseDataBlock.NOISE_LO95 | NoiseDataBlock.NOISE_HI95 |
				NoiseDataBlock.NOISE_MIN | NoiseDataBlock.NOISE_MAX);
		addOutputDataBlock(noiseDataBlock);
	}

	/**
	 * Remember the last settings
	 */
	private NoiseSettings lastCheckSet = null;

	/**
	 * Call when settings dialog has been used or after initialisation
	 */
	protected void newSettings() {
		/**
		 * Check the database table, but only if the settings have changed
		 * (in which case there will be a new reference since they would have
		 * been cloned in the dialog)./ 
		 */
		if (lastCheckSet != noiseControl.noiseSettings) {
			noiseControl.sortBandEdges();
			noiseLogging.createAndCheckTable();
			lastCheckSet = noiseControl.noiseSettings;
		}
//		noiseDataBlock.setChannelMap(noiseControl.noiseSettings.channelBitmap);
		findDataSource();
		if (fftDataSource!=null) {
			noiseDataBlock.sortOutputMaps(fftDataSource.getChannelMap(), fftDataSource.getSequenceMapObject(), noiseControl.noiseSettings.channelBitmap);
		}
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamControllerInterface.INITIALIZATION_COMPLETE ||
				changeType == PamControllerInterface.CHANGED_PROCESS_SETTINGS) {
			findDataSource();
		}

	}

	/**
	 * Work out a new set of measurement times. 
	 * <p>
	 * These are initially set as evenly spread between now and the 
	 * measurement interval. Then add some jitter at random to each. 
	 * @param currentTime current sample number. 
	 */
	private void setMeasurementTimes(long currentTime) {
		int nMeasures = noiseControl.noiseSettings.nMeasures;
		if (measurementTimes == null || measurementTimes.length != nMeasures) {
			measurementTimes = new long[nMeasures];
		}
		long measureInterval = noiseControl.noiseSettings.measurementIntervalSeconds * 
		(long) getSampleRate() / (nMeasures+1);
		for (int i = 0; i < nMeasures; i++) {
			measurementTimes[i] = currentTime + i*measureInterval + r.nextInt((int) measureInterval);
		}
		iMeasurement = 0;
		int nChan = PamUtils.getNumChannels(noiseControl.noiseSettings.channelBitmap);
		measurementData = new double[nChan][noiseControl.noiseSettings.getNumMeasurementBands()][nMeasures];
		nextBlockStartSample = currentTime + noiseControl.noiseSettings.measurementIntervalSeconds * 
		(long) getSampleRate();
	}

	private void findDataSource() {
		PamDataBlock source = noiseControl.getPamConfiguration().getDataBlock(FFTDataUnit.class, 
				noiseControl.noiseSettings.dataSource);
		daqProcess = null;
		
		if (source == null) {
			setParentDataBlock(null);
			return;
		}
		setParentDataBlock(source);
		fftDataSource = (FFTDataBlock) source;

		PamProcess ppp = source.getSourceProcess();
		if (ppp == null) {
			daqProcess = null;
			return;
		}
		if (AcquisitionProcess.class.isAssignableFrom(ppp.getClass())) {
			daqProcess = (AcquisitionProcess) source.getSourceProcess();
		}
		else {
			daqProcess = null;
			return;
		}
		
		// note - we need to use channels (not sequences) to calculate gains
		int chanMap = source.getChannelMap();
		processGains = new double[PamConstants.MAX_CHANNELS];
		int n = PamUtils.getNumChannels(source.getChannelMap());
		int iChan;
		for (int i = 0; i < n; i++) {
			iChan = PamUtils.getNthChannel(i, chanMap);
			processGains[i] = 20.*Math.log10(Math.abs(getChannelGains(iChan)));
		}
	}

	/**
	 * Work back through the data model and work out the accumulated gain
	 * of all upstream processes. 
	 * @return total gain of upstream processes as a factor (not dB). 
	 */
	private double getChannelGains(int iChan) {
		double totalGain = 1;
		PamDataBlock dataBlock = getParentDataBlock();
		PamProcess pamProcess;
		while (dataBlock != null) {
			
			// don't try to add in a gain if this is beamformer output; just continue on and try and find the FFT behind it
			if (!(dataBlock instanceof BeamFormerDataBlock)) {
				totalGain *= dataBlock.getDataGain(iChan);
			}
			pamProcess = dataBlock.getParentProcess();
			if (pamProcess == null) {
				break;
			}
			dataBlock = pamProcess.getParentDataBlock();
		}
		
		return totalGain;
	}

	@Override
	public void pamStart() {
		setMeasurementTimes(0);
	}

	@Override
	public void pamStop() {

	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {

		if (o == fftDataSource) {
			newFFTData((FFTDataUnit) arg);
		}
	}

	private void newFFTData(FFTDataUnit fftDataUnit) {
//		if ((fftDataUnit.getChannelBitmap() & noiseControl.noiseSettings.chanOrSeqBitmap) == 0) {
		if ((fftDataUnit.getSequenceBitmap() & noiseControl.noiseSettings.channelBitmap) == 0) {
			return; // this channel not being used, so get out. 
		}
//		int chanNum = PamUtils.getSingleChannel(fftDataUnit.getChannelBitmap());
		int chanNum = PamUtils.getSingleChannel(fftDataUnit.getSequenceBitmap());
		int highestChan = PamUtils.getHighestChannel(noiseControl.noiseSettings.channelBitmap);

		if (iMeasurement < measurementTimes.length && 
				(fftDataUnit.getStartSample() > measurementTimes[iMeasurement] || noiseControl.noiseSettings.useAll)) {
			makeMeasurments(chanNum, iMeasurement, fftDataUnit);
			if (chanNum == highestChan) {
				iMeasurement ++;
			}
		}
		if (iMeasurement >= measurementTimes.length) {
			createStats();
			setMeasurementTimes(nextBlockStartSample);
		}
	}

	//	private class ChannelProcess {
	//		int channelNumber;
	//		ChannelProcess(int channelNumber) {
	//			this.channelNumber = channelNumber;
	//		}
	//	}

	private void makeMeasurments(int channelNumber, int iMeasurement, FFTDataUnit fftDataUnit) {

		int nBands = noiseControl.noiseSettings.getNumMeasurementBands();
//		int chanIndex = fftDataSource.getChannelListManager().channelNumberToIndex(chanNum);
		for (int i = 0; i < nBands; i++) {
			makeMeasurement(channelNumber, i, iMeasurement, fftDataUnit);
		}

	}

	/**
	 * Make a single measurement for a single energy band and store it. 
	 * @param iBand
	 * @param iMeasurement
	 * @param fftDataUnit
	 */
	private void makeMeasurement(int channelNumber, int iBand, int iMeasurement, FFTDataUnit fftDataUnit) {
		double freqToBin = fftDataSource.getFftLength() / getSampleRate();
		NoiseMeasurementBand nmb = noiseControl.noiseSettings.getMeasurementBand(iBand);
		double bin1 = nmb.f1 * freqToBin;
		double bin2 = nmb.f2 * freqToBin;
		int floorBin1 = (int) Math.floor(bin1);
		int ceilBin2 = (int) Math.ceil(bin2);
		ComplexArray fftData = fftDataUnit.getFftData();
		
		// if we're outside of the freq band by 1 bin, it's just because the floating point math rounded a decimal place too low/high
		if (floorBin1==-1) floorBin1=0;
		if (ceilBin2==fftData.length()+1) ceilBin2=fftData.length();
		
		int chanIndex = PamUtils.getChannelPos(channelNumber, noiseControl.noiseSettings.channelBitmap);
		if (floorBin1 < 0 || ceilBin2 > fftData.length()) {
			measurementData[chanIndex][iBand][iMeasurement] = Double.NaN;
			return;
		}
		double a = 0;
		for (int i = floorBin1; i < ceilBin2; i++) {
			a += fftData.magsq(i);
		}
		double frac = bin1-floorBin1;
		a -= fftData.magsq(floorBin1)*frac;
		frac = ceilBin2 - bin2;
		a -= fftData.magsq(ceilBin2-1)*frac;

		measurementData[chanIndex][iBand][iMeasurement] = a;

	}

	private void createStats() {
		int nChan = PamUtils.getNumChannels(noiseControl.noiseSettings.channelBitmap);
		for (int i = 0; i < nChan; i++) {
			createStats(i, measurementData[i]);
		}
	}
	private void createStats(int channelIndex, double[][] measurementData) {

		//		private String[] measureNames = {"mean", "median", "low95", "high95"};
		
		// get the channel number.  If the data source is using sequence numbers, just use the lowest channel
		int chanNum = PamUtils.getNthChannel(channelIndex, noiseControl.noiseSettings.channelBitmap);
		int gainChannel = channelIndex;
		if (fftDataSource.getSequenceMapObject()!=null) {
			chanNum = PamUtils.getLowestChannel(fftDataSource.getChannelMap());
			gainChannel=0;
		}
		
		int nBands = noiseControl.noiseSettings.getNumMeasurementBands();
		double[][] measurementStats = new double[nBands][noiseDataBlock.getNumUsedStats()];
		int nP = iMeasurement;
		int medPoint = nP/2-1;
		int low5 = nP/20-1;
		low5 = Math.max(0, low5);
		int high5 = nP-1-low5;
		high5 = Math.min(nP-1, high5);
		for (int iB = 0; iB < nBands; iB++) {
			measurementStats[iB][0] = Mean.getMean(measurementData[iB]);
			// need to sort the data for the next three measurements
			Arrays.sort(measurementData[iB]);
			measurementStats[iB][1] = measurementData[iB][medPoint];
			measurementStats[iB][2] = measurementData[iB][low5];
			measurementStats[iB][3] = measurementData[iB][high5];
			measurementStats[iB][4] = measurementData[iB][0];
			measurementStats[iB][5] = measurementData[iB][nP-1];
		}
		// now need to convert all those to dB levels re 1 muPa. 
		daqProcess.prepareFastAmplitudeCalculation(chanNum);
		for (int iB = 0; iB < nBands; iB++) {
			for (int iM = 0; iM < 6; iM++) {
				measurementStats[iB][iM] = daqProcess.fftBandAmplitude2dB(measurementStats[iB][iM], chanNum, 
						fftDataSource.getFftLength(), true, true) - processGains[gainChannel];
			}
		}

		long timeMillis = this.absSamplesToMilliseconds(nextBlockStartSample);
		NoiseDataUnit ndu = new NoiseDataUnit(timeMillis, 1<<chanNum, nextBlockStartSample, 0);
		ndu.sortOutputMaps(fftDataSource.getChannelMap(),
				fftDataSource.getSequenceMapObject(), 
				1<<PamUtils.getNthChannel(channelIndex, noiseControl.noiseSettings.channelBitmap));
		ndu.setNoiseBandData(measurementStats);
		noiseDataBlock.addPamData(ndu);

	}

	public String[] getDBColNames() {
		int nTypes = noiseControl.noiseSettings.getNumMeasurementBands();
		String[] measureNames = noiseDataBlock.getUsedMeasureNames();
		int nNames = measureNames.length;
//		int nChan = PamUtils.getNumChannels(noiseControl.noiseSettings.channelBitmap);
		String[] names = new String[nTypes*nNames];
		int iName = 0;
		int chan;
		for (int iT = 0; iT < nTypes; iT++) {
			for (int iM = 0; iM < measureNames.length; iM++) {
				names[iName] = noiseControl.createDBColumnName(iT, iM);
				iName++;
			}

		}
		return names;
	}

	public NoiseControl getNoiseControl() {
		return noiseControl;
	}

	/**
	 * @return the noiseDataBlock
	 */
	public NoiseDataBlock getNoiseDataBlock() {
		return noiseDataBlock;
	}



}
