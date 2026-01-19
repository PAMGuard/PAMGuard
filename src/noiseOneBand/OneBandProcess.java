package noiseOneBand;

import Acquisition.AcquisitionProcess;
import Filters.Filter;
import Filters.FilterMethod;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamguardMVC.ChannelIterator;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import networkTransfer.receive.BuoyStatusDataUnit;
import noiseOneBand.offline.OneBandDatagramProvider;

public class OneBandProcess extends PamProcess {
	
	/**
	 * value to be considered Double.Nan as an int
	 */
	public static final int nanValue = -327;
	
	protected OneBandControl oneBandControl;
	private DbHtChannelProcess[] channelProcesses = new DbHtChannelProcess[PamConstants.MAX_CHANNELS];
	private PamRawDataBlock rawSourceDataBlock;
	private PamRawDataBlock waveOutDataBlock;
	private OneBandDataBlock measureDataBlock;

	public OneBandProcess(OneBandControl oneBandControl) {
		super(oneBandControl, null, "Noise Measurement");
		this.oneBandControl = oneBandControl;
		waveOutDataBlock = new PamRawDataBlock(oneBandControl.getUnitName() + " audio", this, 0, 1);
		addOutputDataBlock(waveOutDataBlock);
		measureDataBlock = new OneBandDataBlock(oneBandControl.getUnitName(), oneBandControl, this, 1);
		measureDataBlock.setBinaryDataSource(new OneBandDataSource(oneBandControl, measureDataBlock, "Noise"));
		measureDataBlock.SetLogging(new OneBandLogging(oneBandControl, measureDataBlock));
		measureDataBlock.setDatagramProvider(new OneBandDatagramProvider(oneBandControl));
		measureDataBlock.addObserver(new SelfObserver());
		addOutputDataBlock(measureDataBlock);
	}

	@Override
	public void newData(PamObservable o, PamDataUnit pamDataUnit) {
		RawDataUnit rawDataUnit = (RawDataUnit) pamDataUnit;
		int singleChan = PamUtils.getSingleChannel(rawDataUnit.getChannelBitmap());
		if (channelProcesses[singleChan] != null) {
			channelProcesses[singleChan].newData(rawDataUnit);
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#getRequiredDataHistory(PamguardMVC.PamObservable, java.lang.Object)
	 */
	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		return super.getRequiredDataHistory(o, arg);
	}

	@Override
	public void setupProcess() {
		super.setupProcess();
		OneBandParameters params = oneBandControl.oneBandParameters;
		if (params.dataSource == null) {
			return;
		}
		rawSourceDataBlock = (PamRawDataBlock) PamController.getInstance().getDataBlock(RawDataUnit.class, params.dataSource);
		setParentDataBlock(rawSourceDataBlock);		
		if (rawSourceDataBlock == null) {
			return;
		}
		int outputChannels = rawSourceDataBlock.getChannelMap() & params.channelMap;
		waveOutDataBlock.setChannelMap(outputChannels);
		measureDataBlock.setChannelMap(outputChannels);
		waveOutDataBlock.setSampleRate(rawSourceDataBlock.getSampleRate(), true);
		
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if ((1<<i & outputChannels) != 0) {
				channelProcesses[i] = new DbHtChannelProcess(i);
			}
			else {
				channelProcesses[i] = null;
			}
		}
		prepareProcess();
		
	}

	@Override
	public void prepareProcess() {
		super.prepareProcess();
		if (rawSourceDataBlock == null) {
			return;
		}
		OneBandParameters params = oneBandControl.oneBandParameters;
		double sr = rawSourceDataBlock.getSampleRate();
//		try {
//			params.calculateFilterThings(sr);
//		} catch (DbHtException e) {
//			e.printStackTrace();
//			return;
//		}
//		FIRArbitraryFilter faf = new FIRArbitraryFilter(sr, null);
//		faf.setResponse(params.getFilterFrequencies(sampleRate), params.getFilterGains(sr), 
//				params.filterLogOrder, params.chebyGamma);
//		faf.calculateFilter();
		int chanMap = rawSourceDataBlock.getChannelMap();
		measureDataBlock.setChannelMap(chanMap);
		FilterMethod filterMethod = FilterMethod.createFilterMethod(getSampleRate(), params.getFilterParams());
		Filter noiseFilter;
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if ((1<<i & chanMap) == 0) {
				continue;
			}
			if (channelProcesses[i] != null) {
				noiseFilter = filterMethod.createFilter(i);
				channelProcesses[i].prepareProcess(noiseFilter);
			}
		}
		
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
		
	}	
	
	/**
	 * @return the sourceDataBlock
	 */
	@Override
	public PamRawDataBlock getRawSourceDataBlock() {
		return rawSourceDataBlock;
	}

	/**
	 * @return the waveOutDataBlock
	 */
	public PamRawDataBlock getWaveOutDataBlock() {
		return waveOutDataBlock;
	}

	/**
	 * @return the measureDataBlock
	 */
	public OneBandDataBlock getMeasureDataBlock() {
		return measureDataBlock;
	}
	
	/**
	 * Observer it's own output data so that it can set the keep time
	 * for SEL measurement. 
	 * @author Doug Gillespie
	 *
	 */
	private class SelfObserver extends PamObserverAdapter {

		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return (oneBandControl.getParameters().selIntegrationTime+2)*1000;
		}

		@Override
		public String getObserverName() {
			return oneBandControl.getUnitName();
		}

		@Override
		public PamObserver getObserverObject() {
			return this;
		}
		
	}

	private class DbHtChannelProcess {
		private int chan;
		private int chanMap;
		private Filter firFilter;
		private long outputStepSize; // step size for output
		private long nextOutputSample; // sample number for next output. 
		private long processedSamples;
		private double dataSum, dataSum2;
		private double maxVal, minVal;
//		lastMax, lastMin, maxZP, maxPP;
		private double dataGain;
		private AcquisitionProcess daqProcess;
//		private double htMinVal;
		
		/**
		 * @param chan
		 */
		public DbHtChannelProcess(int chan) {
			super();
			this.chan = chan;
			chanMap = 1<<chan;
		}

		public void newData(RawDataUnit oldDataUnit) {
			double[] data = oldDataUnit.getRawData();
			RawDataUnit newDataUnit = waveOutDataBlock.getRecycledUnit();
			long duration = oldDataUnit.getSampleDuration();
			if (newDataUnit == null) {
				newDataUnit = new RawDataUnit(oldDataUnit.getTimeMilliseconds(), oldDataUnit.getChannelBitmap(),
						oldDataUnit.getStartSample(), oldDataUnit.getSampleDuration());
			}
			else {
				newDataUnit.setTimeMilliseconds(oldDataUnit.getTimeMilliseconds());
				newDataUnit.setChannelBitmap(oldDataUnit.getChannelBitmap());
				newDataUnit.setStartSample(oldDataUnit.getStartSample());
				newDataUnit.setSampleDuration(oldDataUnit.getSampleDuration());
			}
			double[] newData = newDataUnit.getRawData();
			if (newData == null || newData.length != duration) {
				newData = new double[(int)duration];
			}
			if (firFilter == null) return;
			firFilter.runFilter(data, newData);
			newDataUnit.setRawData(newData, true);
			waveOutDataBlock.addPamData(newDataUnit);
			
			makeMeasures(newData, newDataUnit.getStartSample());
		}
		
		private void makeMeasures(double[] filteredData, long startSample) {
			int n = filteredData.length;
//			boolean isMin, isMax;
			for (int i = 0; i < n; i++) {
				dataSum += filteredData[i];
				dataSum2 += (filteredData[i]*filteredData[i]);
				maxVal = Math.max(maxVal, filteredData[i]);
				minVal = Math.min(minVal, filteredData[i]);
				/*
				 * Over complicated, so get rid of it. 
				 */
//				if (i > 0 && i < n-1) {
//					isMax = (filteredData[i] > filteredData[i-1] & filteredData[i] > filteredData[i+1]);
//					isMin = (filteredData[i] < filteredData[i-1] & filteredData[i] < filteredData[i+1]);
//					if (isMax) {
//						lastMax = filteredData[i];
//						maxPP = Math.max(maxPP, lastMax-lastMin);
//						maxZP = Math.max(maxZP, lastMax);
//					}
//					if (isMin) {
//						lastMin = filteredData[i];
//						maxPP = Math.max(maxPP, lastMax-lastMin);
//						maxZP = Math.max(maxZP, -lastMin);
//					}
//				}
				if (processedSamples == nextOutputSample) {
					double rms = Math.sqrt((dataSum2-dataSum/outputStepSize)/(outputStepSize-1));
					long timeMills = absSamplesToMilliseconds(startSample+n);
					OneBandDataUnit du = new OneBandDataUnit(timeMills, chanMap, startSample+n, outputStepSize);
					du.setRms(convertDBHT(rms));
					du.setZeroPeak(convertDBHT(maxVal));
					du.setPeakPeak(convertDBHT(maxVal-minVal));
					calculateSEL(du, oneBandControl.getParameters().selIntegrationTime);
					measureDataBlock.addPamData(du);
					zeroStuff();
					nextOutputSample += outputStepSize;
				}
				processedSamples++;
			}
		}
		
		/**
		 * Convert a value measured in raw counts (-1 to +1 on ADC scale) to dBHt
		 * @param rawValue raw value (-1 to +1 scale)
		 * @return value in dBHt
		 */
		private double convertDBHT(double rawValue) {
			// first convert it to dB re 1 micropascal using standard PAMGUARD
			// functions
			if (daqProcess == null) {
				return OneBandProcess.nanValue;
			}
			if (Double.isNaN(rawValue)) {
				return OneBandProcess.nanValue;
			}
			double dbVal = daqProcess.rawAmplitude2dB(rawValue, chan, false);
			// now take off the most sensitive part of the ht response. 
			return dbVal;
		}

		public void prepareProcess(Filter firFilter) {
			this.firFilter = firFilter;
			outputStepSize = oneBandControl.oneBandParameters.measurementInterval;
			if (outputStepSize == 0) {
				outputStepSize = 1;
			}
			outputStepSize *= getSampleRate();
			nextOutputSample = outputStepSize-1;
			processedSamples = 0;
			zeroStuff();
//			htMinVal = dbHtControl.dbHtParameters.getLowestThreshold();
			dataGain = measureDataBlock.getDataGain(chan);
			daqProcess = (AcquisitionProcess) rawSourceDataBlock.getSourceProcess();
			if (daqProcess != null) {
				daqProcess.prepareFastAmplitudeCalculation(chan);
			}
		}
		
		private void zeroStuff() {
//			PamguardMVC.debug.Debug.out.printf("Zero everything at sample %d\n", processedSamples);
			maxVal = minVal = 0;
//			lastMax = lastMin = maxZP = maxPP = 0;
			dataSum = dataSum2 = 0;
		}
		
	}

	@Override
	public void processNewBuoyData(BuoyStatusDataUnit buoyStatus, PamDataUnit dataUnit) {
		OneBandDataUnit du = (OneBandDataUnit) dataUnit;
		calculateSEL(du, oneBandControl.getParameters().selIntegrationTime);
	}
	/**
	 * Calculate SEL and add it to a data unit. 
	 * @param du most recent data unit
	 * @param selIntegrationTime SEL integration time in seconds. 
	 */
	public void calculateSEL(OneBandDataUnit du, int selIntegrationTime) {
		long earliestTime = du.getTimeMilliseconds() - selIntegrationTime*1000;
		double sel = Math.pow(10., du.getRms()/10) * oneBandControl.getParameters().measurementInterval;
		OneBandDataUnit nextU, prevU = du;
		synchronized (measureDataBlock.getSynchLock()) {
			ChannelIterator<OneBandDataUnit> chanIt = measureDataBlock.getChannelIterator(du.getChannelBitmap(), PamDataBlock.ITERATOR_END);
			long dt;
			while (chanIt.hasPrevious()) {
				nextU = chanIt.previous();
				if (nextU.getTimeMilliseconds() <= earliestTime) {
					break;
				}
				dt = prevU.getTimeMilliseconds()-nextU.getTimeMilliseconds();
				sel += Math.pow(10., nextU.getRms()/10)*(double) dt / 1000.;
				prevU = nextU;
			}
		}
		long intTime = oneBandControl.getParameters().measurementInterval;
		if (prevU != du) {
			intTime += (du.getTimeMilliseconds()-prevU.getTimeMilliseconds())/1000;
		}
		du.setSEL(10*Math.log10(sel), (int) intTime); 
	}

}
