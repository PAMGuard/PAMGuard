package noiseBandMonitor;

import java.util.ArrayList;
import java.util.Arrays;

import noiseMonitor.NoiseBinaryDataSource;
import noiseMonitor.NoiseDataBlock;
import noiseMonitor.NoiseDataUnit;
import noiseMonitor.NoiseLogging;

import Acquisition.AcquisitionProcess;
import Filters.Filter;
import Filters.FilterMethod;
import Filters.FilterParams;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.debug.Debug;

public class NoiseBandProcess extends PamProcess {

	private NoiseBandControl noiseBandControl;
	private ArrayList<FilterMethod> decimationFilterMethods;
	private ArrayList<FilterMethod> bandFilterMethods;
	private int[] decimatorIndexes;
	private ChannelProcess[] channelProcesses;
	private NoiseDataBlock noiseDataBlock;
	private AcquisitionProcess daqProcess;
	private NoiseBinaryDataSource noiseBinaryDataSource;
	private NoiseLogging noiseLogging;

	public NoiseBandProcess(NoiseBandControl noiseBandControl) {
		super(noiseBandControl, null);
		this.noiseBandControl = noiseBandControl;
		noiseDataBlock = new NoiseDataBlock(noiseBandControl.getUnitName(), this, 0);
		noiseDataBlock.setStatisticTypes(NoiseDataBlock.NOISE_MEAN | NoiseDataBlock.NOISE_PEAK);
		noiseBinaryDataSource = new NoiseBinaryDataSource(noiseBandControl, noiseDataBlock);
		noiseDataBlock.setBinaryDataSource(noiseBinaryDataSource);
		noiseDataBlock.SetLogging(noiseLogging = new NoiseLogging(noiseDataBlock));
		noiseDataBlock.setDatagramProvider(new NoiseBandDatagramProvider(noiseBandControl, this));
		addOutputDataBlock(noiseDataBlock);
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}

	public void setupProcess() {
		super.setupProcess();
		PamDataBlock sourceData = noiseBandControl.getPamConfiguration().getDataBlock(RawDataUnit.class, noiseBandControl.noiseBandSettings.rawDataSource);
		
//		System.out.println("********************************************************");
//		System.out.println("NOISE BAND PROCESS: " + sourceData + "  " + noiseBandControl.noiseBandSettings.rawDataSource);
//		System.out.println("********************************************************");
		if (sourceData == null) {
			return;
		}
		setParentDataBlock(sourceData);
		setSampleRate(sourceData.getSampleRate(), true);
		noiseDataBlock.setChannelMap(noiseBandControl.noiseBandSettings.channelMap);
		PamProcess ppp = sourceData.getSourceProcess();
		if (ppp == null) {
			daqProcess = null;
			return;
		}
		if (AcquisitionProcess.class.isAssignableFrom(ppp.getClass())) {
			daqProcess = (AcquisitionProcess) sourceData.getSourceProcess();
		}
		else {
			daqProcess = null;
		}
		prepareProcess();
	}

	@Override
	public void prepareProcess() {
		super.prepareProcess();
		if (getSampleRate() == 0) return;
		decimationFilterMethods = noiseBandControl.makeDecimatorFilters(noiseBandControl.noiseBandSettings, 
				getSampleRate());
		bandFilterMethods = noiseBandControl.makeBandFilters(noiseBandControl.noiseBandSettings, 
				decimationFilterMethods, getSampleRate());
		decimatorIndexes = noiseBandControl.getDecimatorIndexes();
		channelProcesses = new ChannelProcess[PamConstants.MAX_CHANNELS];
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if ((1<<i & noiseBandControl.noiseBandSettings.channelMap) != 0) {
				channelProcesses[i] = new ChannelProcess(i);
				channelProcesses[i].setupFilters();
			}
		}
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		RawDataUnit rawDataUnit = (RawDataUnit) arg;
		int iChan = PamUtils.getSingleChannel(rawDataUnit.getChannelBitmap());
		if ((1<<iChan & noiseBandControl.noiseBandSettings.channelMap) != 0) {
			channelProcesses[iChan].newData(rawDataUnit);
		}
	}

	class ChannelProcess {
		private int iChan;
//		private Filter[] decimationFilters;
//		private Filter[] bandFilters;
		private BandOutput[] bandOutputs;
		DecimationGroup[] decimationGroups;
		long lastOutputTime = 0;

		ChannelProcess(int iChan) {
			this.iChan = iChan;
		}

		public void setupFilters() {
			decimationGroups = new DecimationGroup[decimationFilterMethods.size()+1];
			decimationGroups[0] = new DecimationGroup(this, null);
			if (bandFilterMethods == null) {
				return;
			}
			bandOutputs = new BandOutput[bandFilterMethods.size()];
			for (int i = 0; i < decimationFilterMethods.size(); i++) {
				decimationGroups[i+1] = new DecimationGroup(this, decimationFilterMethods.get(i));
			}

			for (int i = 0; i < bandFilterMethods.size(); i++) {
				bandOutputs[i] = new BandOutput();
				decimationGroups[decimatorIndexes[i]+1].addBand(i, bandFilterMethods.get(i), bandOutputs[i]);
			}
			
//			if (2>1) {
//				Debug.out.printf("Noise band channel %d with %d decimator groups\n", iChan, decimationGroups.length);
//				for (int i = 0; i < decimationGroups.length; i++) {
//					Debug.out.printf("Decimator %2d:\n%s", i, decimationGroups[i].toString());
//				}
//			}
		}
		
		protected void newData(RawDataUnit rawDataUnit) {
			long timeMillis = rawDataUnit.getTimeMilliseconds();
			double[] lastOutput = rawDataUnit.getRawData();
			int lastOutputLength = lastOutput.length;
			for (int i = 0; i < decimationGroups.length; i++) {
				lastOutputLength = decimationGroups[i].processData(lastOutput, lastOutputLength);
				lastOutput = decimationGroups[i].decimatedData;
			}
			if (bandOutputs[0].getNSamples() == 48000) {
				return;
			}
			if (lastOutputTime == 0) {
				lastOutputTime = timeMillis;
			}
			else if (timeMillis - lastOutputTime >= noiseBandControl.noiseBandSettings.outputIntervalSeconds*1000) {
				double ampTerm = daqProcess.prepareFastAmplitudeCalculation(iChan);
//				System.out.printf("Amplitude term cahnnel %d is %3.1f\n", iChan, ampTerm);
				double[][] measurementStats = new double[bandOutputs.length][2];
				for (int i = 0; i < bandOutputs.length; i++) {
					measurementStats[bandOutputs.length-i-1][0] = daqProcess.rawAmplitude2dB(bandOutputs[i].getRMS(), iChan, true);
					measurementStats[bandOutputs.length-i-1][1] = daqProcess.rawAmplitude2dB(bandOutputs[i].getMaxValue(), iChan, true);
					bandOutputs[i].clear();
				}
				NoiseDataUnit ndu = new NoiseDataUnit(timeMillis, 1<<iChan, rawDataUnit.getLastSample(), 0);
				ndu.setNoiseBandData(measurementStats);
				noiseDataBlock.addPamData(ndu);
				
				lastOutputTime = timeMillis;
			}
		}
	}

	/**
	 * Each decimation group does a single channel and a single
	 * decimator. The first in the list will not actually have a 
	 * decimator, but will just process filter bands on the raw input
	 * data.
	 * @author Doug Gillespie
	 *
	 */
	private class DecimationGroup {
		private FilterMethod decimatorMethod;
		private ArrayList<FilterMethod> bandFilterMethodss = new ArrayList<FilterMethod>();
		private ArrayList<Filter> bandFilters = new ArrayList<Filter>();
		private ArrayList<BandOutput> bandOutputs = new ArrayList<BandOutput>();
		private Filter decimationFilter;
		private ChannelProcess channelProcess;
//		double inputSampleRate;
//		private double outputSampleRate;
		double[] decimatedData;
		double[] preDecimatedData;
		int decimatorOffset = 0;

		DecimationGroup(ChannelProcess channelProcess, FilterMethod decimatorMethod) {
			this.channelProcess = channelProcess;
			this.decimatorMethod = decimatorMethod;
			if (decimatorMethod != null) {
				decimationFilter = decimatorMethod.createFilter(channelProcess.iChan);
				decimationFilter.prepareFilter();
//				inputSampleRate = decimatorMethod.getSampleRate();
//				outputSampleRate = inputSampleRate / 2;
			}
			else {
				// no decimator on first group. 
//				inputSampleRate = 1;
//				outputSampleRate = inputSampleRate;
			}

		}

		protected void addBand(int iBand, FilterMethod aBand, BandOutput bandOutput) {
			bandFilterMethodss.add(aBand);
			bandOutputs.add(bandOutput);
			Filter filter;
			bandFilters.add(filter = aBand.createFilter(channelProcess.iChan));
			filter.prepareFilter();
		}

		/**
		 * Once the length of the decimated data array gets odd, then the length
		 * of the decimated data will vary from call to call and may even be 0 for 
		 * several consecutive calls at very low sample rates. To avoid continuously 
		 * have to reallocate the length of the decimated data arrays,include 
		 * the data length in function calls and process data up to that number
		 * of samples only.  
		 * 
		 * @param inputData input data array
		 * @param nSamples number of used samples (may be less than inputData.length)
		 * @return number of output samples from the decimator. May vary when inputsamples is odd. 
		 */
		protected int processData(double[] inputData, int nSamples) {
			int decSamples = decimateData(inputData, nSamples);
			/*
			 * Now process the decimated data with as many band filters as are present. 
			 */
			Filter bandFilter;
			BandOutput bandOutput;
			double aValue;
//			double[] testArr = new double[200];
//			double dum;
			for (int i = 0; i < bandFilters.size(); i++) {
				bandFilter = bandFilters.get(i);
				bandOutput = bandOutputs.get(i);
				for (int s = 0; s < decSamples; s++) {
					bandOutput.addSample(bandFilter.runFilter(decimatedData[s]));
//					if (s < 200) {
//						testArr[s] = dum;
//					}
				}
			}
			
			return decSamples;
		}

		/**
		 * Filter and decimate the data by a factor of two. 
		 * Note that the number of input samples my vary by +/-1
		 * after many decimations, so it's not always going to 
		 * output the same number of datas. 
		 * <p>
		 * Note that the first one of these probably won't decimate the data
		 * but will just operate on the raw data values. 
		 * 
		 * @param inputData input data array
		 * @param nSamples number of used samples in input data. 
		 * @return number of samples in decimated data. 
		 */
		private int decimateData(double[] inputData, int nSamples) {
			if (decimationFilter == null) {
				decimatedData = inputData;
				return inputData.length;
			}
			int deciLength = (nSamples+1)/2;
			if (decimatedData == null) {
				decimatedData = new double[deciLength];
			}
			else if (decimatedData.length < deciLength) {
				decimatedData = Arrays.copyOf(decimatedData, deciLength);
			}
			if (preDecimatedData == null || preDecimatedData.length < nSamples) {
				preDecimatedData = new double[nSamples];
			}
			for (int i = 0; i < nSamples; i++) {
				preDecimatedData[i] = decimationFilter.runFilter(inputData[i]);
			}
			int newSamples = 0;
			int sample;
			for (sample = decimatorOffset; sample < nSamples; sample+=2) {
				decimatedData[newSamples++] = preDecimatedData[sample];
			}
			decimatorOffset = sample - nSamples;
			return newSamples;
		}

		@Override
		public String toString() {
			String str = "";
			if (decimationFilter != null) {
				str += String.format("Input fs %3.1fHz, LP filter %3.1fHz\n", decimatorMethod.getSampleRate(), decimatorMethod.getFilterParams().lowPassFreq);
			}
			for (int i = 0; i < bandFilterMethodss.size(); i++) {
			FilterMethod aFilt = bandFilterMethodss.get(i);
				FilterParams fPs = aFilt.getFilterParams();
				str += String.format(" band %d filter %3.1f to %3.1f Hz\n", i, fPs.highPassFreq, fPs.lowPassFreq);
			}
			
			return str;
		}
		
	}
	class BandOutput {
		private int nSamples;
		private double maxValue;
		private double sumSquared;
		
		void addSample(double sample) {
			nSamples++;
			maxValue = Math.max(Math.abs(sample), maxValue);
			sumSquared += sample*sample;
		}
		
		double getMaxValue() {
			return maxValue;
		}
		
		double getRMS() {
			return(Math.sqrt(sumSquared/nSamples));
		}
		
		int getNSamples() {
			return nSamples;
		}
		
		void clear() {
			nSamples = 0;
			maxValue = 0;
			sumSquared = 0;
		}
	}
	/**
	 * @return the noiseDataBlock
	 */
	public NoiseDataBlock getNoiseDataBlock() {
		return noiseDataBlock;
	}

	/**
	 * @return the decimationFilterMethods
	 */
	public ArrayList<FilterMethod> getDecimationFilterMethods() {
		return decimationFilterMethods;
	}

	/**
	 * @return the bandFilterMethods
	 */
	public ArrayList<FilterMethod> getBandFilterMethods() {
		return bandFilterMethods;
	}

	/**
	 * @return the decimatorIndexes
	 */
	public int[] getDecimatorIndexes() {
		return decimatorIndexes;
	}

	/**
	 * @return the noiseLogging
	 */
	public NoiseLogging getNoiseLogging() {
		return noiseLogging;
	}
}
