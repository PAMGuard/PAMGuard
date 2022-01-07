package noiseOneBand.offline;

import generalDatabase.DBControlUnit;

import java.io.Serializable;

import noiseOneBand.OneBandControl;
import noiseOneBand.OneBandDataBlock;
import noiseOneBand.OneBandDataUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.PamConstants;
import dataMap.OfflineDataMapPoint;
import offlineProcessing.OfflineTask;

public class OneBandSummaryTask extends OfflineTask<OneBandDataUnit>{

	private OneBandDataBlock measureDataBlock;
	
	private OneBandSummaryParams oneBandSummaryParams = new OneBandSummaryParams();
	
	private double[] summedMeans;
	private double[] maxPeakValues;
	private double[] maxPeakPeakValues;
	private int[] dataCount;
	private long[] intervalStart;
	
	OfflineOneBandLogging offlineLogging;

	private DBControlUnit dbControl;
	
	
	public OneBandSummaryTask(OneBandControl oneBandControl, OneBandDataBlock measureDataBlock) {
		super(measureDataBlock);
		this.measureDataBlock = measureDataBlock;
		setParentDataBlock(measureDataBlock);
		PamSettingManager.getInstance().registerSettings(new DbHtSummarySettings());
		offlineLogging = new OfflineOneBandLogging(oneBandControl, measureDataBlock);
	}

	@Override
	public String getName() {
		return "Export data summary";
	}

	@Override
	public void prepareTask() {
		summedMeans = new double[PamConstants.MAX_CHANNELS];
		maxPeakValues = new double[PamConstants.MAX_CHANNELS];
		maxPeakPeakValues = new double[PamConstants.MAX_CHANNELS];
		dataCount = new int[PamConstants.MAX_CHANNELS];
		intervalStart = new long[PamConstants.MAX_CHANNELS];
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			resetChannel(i, 0);
		}
		// now check the database
		dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl != null) {
			dbControl.getDbProcess().checkTable(offlineLogging.getTableDefinition());
		}
	}

	@Override
	public void newDataLoad(long startTime, long endTime,
			OfflineDataMapPoint mapPoint) {
		
	}

	@Override
	public void loadedDataComplete() {
	}

	@Override
	public void completeTask() {
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			exportSummary(i);
		}
	}

	@Override
	public boolean processDataUnit(OneBandDataUnit dataUnit) {
		int chan = PamUtils.getSingleChannel(dataUnit.getChannelBitmap());
		long timeStep = oneBandSummaryParams.intervalSeconds * 1000;
		if (intervalStart[chan] == 0) {
			intervalStart[chan] = dataUnit.getTimeMilliseconds();
			intervalStart[chan] /= timeStep;
			intervalStart[chan] *= timeStep;
		}
		else if (dataUnit.getTimeMilliseconds() >= intervalStart[chan]+timeStep) {
			exportSummary(chan);
			long newStart = dataUnit.getTimeMilliseconds();
			newStart /= timeStep;
			newStart *= timeStep;
			resetChannel(chan, newStart);
//			System.out.println(String.format("Set ch %d interval start to %s", 
//					chan, PamCalendar.formatDateTime(newStart)));
		}
		summedMeans[chan] += Math.pow(10., dataUnit.getRms()/10.);
		maxPeakValues[chan] = Math.max(maxPeakValues[chan], dataUnit.getZeroPeak());
		maxPeakPeakValues[chan] = Math.max(maxPeakPeakValues[chan], dataUnit.getPeakPeak());
		dataCount[chan] ++;
		
		return false;
	}

	/**
	 * Save summary data for one channel
	 * @param chan channel number
	 */
	private void exportSummary(int chan) {
		if (dataCount[chan] == 0) {
			return;
		}
		
		double mean = 10.*Math.log10(summedMeans[chan]/dataCount[chan]);
		// export the data here
		OfflineOneBandDataUnit du = new OfflineOneBandDataUnit(intervalStart[chan], 1<<chan, 0, oneBandSummaryParams.intervalSeconds*1000);
		du.setRms(mean);
		du.setZeroPeak(maxPeakValues[chan]);
		du.setPeakPeak(maxPeakPeakValues[chan]);
		du.setnDatas(dataCount[chan]);
		du.setInterval(oneBandSummaryParams.intervalSeconds);
		
//		System.out.println(String.format("DbHt chan %d %s rms = %3.1f", chan, PamCalendar.formatDateTime(intervalStart[chan]), mean));
		offlineLogging.logData(dbControl.getConnection(), du);
		
	}

	private void resetChannel(int chan, long timeMillis) {
		summedMeans[chan] = 0;
		intervalStart[chan] = timeMillis;
		maxPeakValues[chan] = Integer.MIN_VALUE;
		maxPeakPeakValues[chan] = Integer.MIN_VALUE;
		dataCount[chan] = 0;
		
	}

	@Override
	public boolean hasSettings() {
		return true;
	}

	@Override
	public boolean callSettings() {
		OneBandSummaryParams newParams = OneBandSummaryDialog.showDialog(null, oneBandSummaryParams);
		if (newParams != null) {
			oneBandSummaryParams = newParams.clone();
			return true;
		}
		return false;
	}
	
	private class DbHtSummarySettings implements PamSettings {

		@Override
		public Serializable getSettingsReference() {
			return oneBandSummaryParams;
		}

		@Override
		public long getSettingsVersion() {
			return OneBandSummaryParams.serialVersionUID;
		}

		@Override
		public String getUnitName() {
			return measureDataBlock.getDataName();
		}

		@Override
		public String getUnitType() {
			return "Noise Summary Params";
		}

		@Override
		public boolean restoreSettings(
				PamControlledUnitSettings pamControlledUnitSettings) {
			oneBandSummaryParams = ((OneBandSummaryParams) pamControlledUnitSettings.getSettings()).clone();
			return true;
		}
		
	}

}
