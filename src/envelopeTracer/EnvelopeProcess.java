package envelopeTracer;

import Filters.Filter;
import Filters.FilterMethod;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;

public class EnvelopeProcess extends PamProcess {

	private EnvelopeControl envelopeControl;
	
	private PamRawDataBlock inputData, outputData;
	
	private ChannelProcess[] channelProcesses = new ChannelProcess[PamConstants.MAX_CHANNELS];
	
	private double pickStep;

	private FilterMethod preFilterMethod;

	private FilterMethod postFilterMethod;
	
	public EnvelopeProcess(EnvelopeControl envelopeControl) {
		super(envelopeControl, null);
		this.envelopeControl = envelopeControl;
		
		outputData = new PamRawDataBlock("Envelope Waveform", this, envelopeControl.envelopeParams.channelMap, 
				envelopeControl.envelopeParams.outputSampleRate);
		addOutputDataBlock(outputData);
	}
	
	public void newSettings() {
		if (envelopeControl.envelopeParams.dataSourceName != null) {
			inputData = PamController.getInstance().getRawDataBlock(envelopeControl.envelopeParams.dataSourceName);
		}
		else {
			inputData = PamController.getInstance().getRawDataBlock(0);
		}
		setParentDataBlock(inputData);
		if (inputData == null) {
			return;
		}
		outputData.setChannelMap(envelopeControl.envelopeParams.channelMap);
		this.setSampleRate(envelopeControl.envelopeParams.outputSampleRate, true);
		outputData.setSampleRate(envelopeControl.envelopeParams.outputSampleRate, true);
		
		preFilterMethod = FilterMethod.createFilterMethod(inputData.getSampleRate(), envelopeControl.envelopeParams.filterSelect);
		postFilterMethod = FilterMethod.createFilterMethod(inputData.getSampleRate(), envelopeControl.envelopeParams.postFilterParams);
		
		// create the filters ...
		int chans = envelopeControl.envelopeParams.channelMap;
		int nChans = PamUtils.getNumChannels(chans);
		int iChan;
		for (int i = 0; i < nChans; i++) {
			iChan = PamUtils.getNthChannel(i, chans);
			channelProcesses[iChan] = new ChannelProcess(iChan);
		}
		pickStep = inputData.getSampleRate() / envelopeControl.envelopeParams.outputSampleRate;
//		System.out.println("Pick step = "  + new Double(pickStep).toString());
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		RawDataUnit rawDataUnit = (RawDataUnit) arg;
		if ((rawDataUnit.getChannelBitmap() & envelopeControl.envelopeParams.channelMap) != 0) {
			int singleChan = PamUtils.getSingleChannel(rawDataUnit.getChannelBitmap());
			channelProcesses[singleChan].newData(rawDataUnit);
		}
	}

	private class ChannelProcess {
		
		int iChan;
		
		int channelMap;

		private Filter firstFilter, secondFilter;
		
		private double pickPoint;
		
		private int totalSamples;
		
		private double[] firstFilterOutput;
		
		private double minVal = 1;
		private double logMinVal = 1;
		/**
		 * @param iChan
		 */
		public ChannelProcess(int iChan) {
			super();
			this.iChan = iChan;
			channelMap = 1<<iChan;
			firstFilter = preFilterMethod.createFilter(iChan);
			secondFilter = postFilterMethod.createFilter(iChan);
//			firstFilter = new IirfFilter(iChan, envelopeControl.envelopeParams.filterSelect, inputData.getSampleRate());
//			secondFilter = new IirfFilter(iChan, envelopeControl.envelopeParams.postFilterParams, inputData.getSampleRate());
			minVal = 1./Math.pow(2,15);
			logMinVal = Math.log(minVal);
			prepare();
		}
		
		public void prepare() {
			pickPoint = 0;
			totalSamples = 0;
		}
		
		public void newData(RawDataUnit rawDataUnit) {
			double[] inputData = rawDataUnit.getRawData();
			int nSamples = inputData.length;
			
			/**
			 * Run the first filter to get the band we're interested in. 
			 */
			if (firstFilterOutput == null || firstFilterOutput.length != nSamples) {
				firstFilterOutput = new double[nSamples];
			}
			firstFilter.runFilter(inputData, firstFilterOutput);
			/**
			 * Now to the rectification ...
			 */
			for (int i = 0; i < nSamples; i++) {
				firstFilterOutput[i] = Math.abs(firstFilterOutput[i]);
			}
			if (envelopeControl.envelopeParams.logScale) {
				for (int i = 0; i < nSamples; i++) {
					if (firstFilterOutput[i] < minVal) {
						firstFilterOutput[i] = -.5;
					}
					else {
						firstFilterOutput[i] = Math.log(firstFilterOutput[i])/logMinVal + 0.5;
					}
				}
			}
			/**
			 * Run the second filter in place. 
			 */
			secondFilter.runFilter(firstFilterOutput);
			
			/**
			 * Now decimate the data. 
			 */
			int outputSamples = (int) Math.round((nSamples-pickPoint) / pickStep);
			double[] outputRawData = new double[outputSamples];
			for (int i = 0; i < outputSamples; i++) {
				outputRawData[i] = firstFilterOutput[(int)(pickPoint+.5)];
				pickPoint += pickStep;
			}
			pickPoint -= nSamples;
			pickPoint = Math.max(0, pickPoint);
			
			RawDataUnit newDataUnit;			
			newDataUnit = new RawDataUnit(rawDataUnit.getTimeMilliseconds(), channelMap, totalSamples, outputSamples);
			newDataUnit.setRawData(outputRawData, true);
			outputData.addPamData(newDataUnit);
			
			totalSamples += outputSamples;
			
		}
		
	}
	
	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		// TODO Auto-generated method stub
		super.setSampleRate(envelopeControl.envelopeParams.outputSampleRate, notify);
//		this.sampleRate = sampleRate;
	}

	@Override
	public void pamStart() {
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if (channelProcesses[i] != null) {
				channelProcesses[i].prepare();
			}
		}
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#prepareProcess()
	 */
	@Override
	public void prepareProcess() {
		/*
		 * Override this to stop it setting the sample rate when it starts up !
		 */
	}

}
