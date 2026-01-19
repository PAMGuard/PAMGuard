package dbht;

import Acquisition.AcquisitionProcess;
import Filters.FIRArbitraryFilter;
import Filters.Filter;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import dbht.offline.DbHtDatagramProvider;

public class DbHtProcess extends PamProcess {
	
	/**
	 * value to be considered Double.Nan as an int
	 */
	public static final int nanValue = -327;
	
	protected DbHtControl dbHtControl;
	private DbHtChannelProcess[] channelProcesses = new DbHtChannelProcess[PamConstants.MAX_CHANNELS];
	private PamRawDataBlock rawSourceDataBlock;
	private PamRawDataBlock waveOutDataBlock;
	private DbHtDataBlock measureDataBlock;

	public DbHtProcess(DbHtControl dbHtControl) {
		super(dbHtControl, null, "DbHt Measurement");
		this.dbHtControl = dbHtControl;
		waveOutDataBlock = new PamRawDataBlock(dbHtControl.getUnitName() + " audio", this, 0, 1);
		addOutputDataBlock(waveOutDataBlock);
		measureDataBlock = new DbHtDataBlock(dbHtControl.getUnitName() + " Measurements", this, 1);
		measureDataBlock.setBinaryDataSource(new DbHtDataSource(dbHtControl, measureDataBlock));
		measureDataBlock.SetLogging(new DbHtLogging(dbHtControl, measureDataBlock));
		measureDataBlock.setDatagramProvider(new DbHtDatagramProvider(dbHtControl));
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

	@Override
	public void setupProcess() {
		super.setupProcess();
		DbHtParameters params = dbHtControl.dbHtParameters;
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
		
	}

	@Override
	public void prepareProcess() {
		super.prepareProcess();
		if (rawSourceDataBlock == null) {
			return;
		}
		DbHtParameters params = dbHtControl.dbHtParameters;
		double sr = rawSourceDataBlock.getSampleRate();
		try {
			params.calculateFilterThings(sr);
		} catch (DbHtException e) {
			e.printStackTrace();
			return;
		}
		FIRArbitraryFilter faf = new FIRArbitraryFilter(sr, null);
		faf.setResponse(params.getFilterFrequencies(getSampleRate()), params.getFilterGains(sr), 
				params.filterLogOrder, params.chebyGamma);
		int nTap = faf.calculateFilter();
		if (nTap == 0) {
			// filter creation failes
			String msg = "Filter creation failed. Probably due to filter points > Nyquist frequency";
			WarnOnce.showWarning(dbHtControl.getUnitName(), msg, WarnOnce.WARNING_MESSAGE);
		}
		Filter firFilter;
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if (channelProcesses[i] != null) {
				firFilter = faf.createFilter(i);
				channelProcesses[i].prepareProcess(firFilter);
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
	public DbHtDataBlock getMeasureDataBlock() {
		return measureDataBlock;
	}

	private class DbHtChannelProcess {
		private int chan;
		private int chanMap;
		private Filter firFilter;
		private long outputStepSize; // step size for output
		private long nextOutputSample; // sample number for next output. 
		private long processedSamples;
		private double dataSum, dataSum2;
		private double maxVal, minVal, lastMax, lastMin, maxZP, maxPP;
		private double dataGain;
		private AcquisitionProcess daqProcess;
		private double htMinVal;
		
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
			firFilter.runFilter(data, newData);
			newDataUnit.setRawData(newData, true);
			waveOutDataBlock.addPamData(newDataUnit);
			
			makeMeasures(newData, newDataUnit.getStartSample());
		}
		
		private void makeMeasures(double[] filteredData, long startSample) {
			int n = filteredData.length;
			boolean isMin, isMax;
			for (int i = 0; i < n; i++) {
				dataSum += filteredData[i];
				dataSum2 += (filteredData[i]*filteredData[i]);
				maxVal = Math.max(maxVal, filteredData[i]);
				minVal = Math.min(minVal, filteredData[i]);
				if (i > 0 && i < n-1) {
					isMax = (filteredData[i] > filteredData[i-1] & filteredData[i] < filteredData[i+1]);
					isMin = (filteredData[i] < filteredData[i-1] & filteredData[i] < filteredData[i+1]);
					if (isMax) {
						lastMax = filteredData[i];
						maxPP = Math.max(maxPP, lastMax-lastMin);
						maxZP = Math.max(maxZP, lastMax);
					}
					if (isMin) {
						lastMin = filteredData[i];
						maxPP = Math.max(maxPP, lastMax-lastMin);
						maxZP = Math.max(maxZP, -lastMin);
					}
				}
				if (processedSamples == nextOutputSample) {
					double rms = Math.sqrt((dataSum2-dataSum/outputStepSize)/(outputStepSize-1));
					long timeMills = absSamplesToMilliseconds(startSample+n);
					DbHtDataUnit du = new DbHtDataUnit(timeMills, chanMap, startSample+n, outputStepSize);
					du.setRms(convertDBHT(rms));
					du.setZeroPeak(convertDBHT(maxZP));
					du.setPeakPeak(convertDBHT(maxPP));
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
				return DbHtProcess.nanValue;
			}
			if (Double.isNaN(rawValue)) {
				return DbHtProcess.nanValue;
			}
			double dbVal = daqProcess.rawAmplitude2dB(rawValue, chan, false);
			// now take off the most sensitive part of the ht response. 
			return dbVal-htMinVal;
		}

		public void prepareProcess(Filter firFilter) {
			this.firFilter = firFilter;
			outputStepSize = dbHtControl.dbHtParameters.measurementInterval;
			if (outputStepSize == 0) {
				outputStepSize = 1;
			}
			outputStepSize *= getSampleRate();
			nextOutputSample = outputStepSize-1;
			processedSamples = 0;
			zeroStuff();
			htMinVal = dbHtControl.dbHtParameters.getLowestThreshold();
			dataGain = measureDataBlock.getDataGain(chan);
			daqProcess = (AcquisitionProcess) rawSourceDataBlock.getSourceProcess();
			if (daqProcess != null) {
				daqProcess.prepareFastAmplitudeCalculation(chan);
			}
		}
		
		private void zeroStuff() {
			maxVal = minVal = lastMax = lastMin = maxZP = maxPP = 0;
			dataSum = dataSum2 = 0;
		}
		
	}
}
