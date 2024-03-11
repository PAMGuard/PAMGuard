package noiseMonitor;

import noiseMonitor.alarm.NoiseAlarmCounter;
import noiseMonitor.alarm.NoiseAlarmProvider;
import noiseMonitor.species.TethysNoiseDataProvider;
import tethys.TethysControl;
import tethys.pamdata.TethysDataProvider;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.FixedSpeciesManager;
import alarm.AlarmCounter;
import alarm.AlarmCounterProvider;
import alarm.AlarmDataSource;
import PamUtils.FrequencyFormat;
import PamUtils.PamUtils;
import PamguardMVC.DataAutomation;
import PamguardMVC.DataAutomationInfo;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class NoiseDataBlock extends PamDataBlock<NoiseDataUnit> implements AlarmDataSource {

	/*
	 * flags to indicate what types of measure are included in the data block 
	 */
	static public final int NOISE_MEAN = 0x1;
	static public final int NOISE_MEDIAN = 0x2;
	static public final int NOISE_LO95 = 0x4;
	static public final int NOISE_HI95 = 0x8;
	static public final int NOISE_MIN = 0x10;
	static public final int NOISE_MAX = 0x20;
	static public final int NOISE_PEAK = 0x40;
	
	static public final int NNOISETYPES = 7;
	
	double[] bandLoEdges;
	double[] bandHiEdges;
	String[] bandNames;
	String[] bandLongNames;

	private NoiseAlarmProvider noiseAlarmCounter;
	/**
	 * These are the names used in the database columns, so don't change them on pain of 
	 * nothing ever working ever again !
	 */
	public static final String[] measureNames = {"mean", "median", "low95", "high95", "Min", "Max", "Peak"};
	public static final String[] displayNames = {"Mean", "Median", "Lower 95%", "Upper 95%", "Minimum", "Maximim", "Peak"};
	
	private int statisticTypes;
	private TethysNoiseDataProvider tethysNoiseDataProvider;
	private FixedSpeciesManager fixedSpeciesManager;
	
	public NoiseDataBlock(String dataName,
			PamProcess parentProcess, int channelMap) {
		super(NoiseDataUnit.class, dataName, parentProcess, channelMap);
		
	}

	public void masterClockUpdate(long milliSeconds, long clockSample) {
		super.masterClockUpdate(milliSeconds, clockSample);
	}
	/**
	 * @param measureTypes the measureTypes to set
	 */
	public void setStatisticTypes(int measureTypes) {
		this.statisticTypes = measureTypes;
	}

	/**
	 * @return the measureTypes
	 */
	public int getStatisticTypes() {
		return statisticTypes;
	}
	
	public int getNumUsedStats() {
		return PamUtils.getNumChannels(statisticTypes);
	}
	
	public static String getMeasureName(int measureType) {
		switch(measureType) {
		case NOISE_MEAN:
			return measureNames[0];
		case NOISE_MEDIAN:
			return measureNames[1];
		case NOISE_LO95:
			return measureNames[2];
		case NOISE_HI95:
			return measureNames[3];
		case NOISE_MIN:
			return measureNames[4];
		case NOISE_MAX:
			return measureNames[5];
		case NOISE_PEAK:
			return measureNames[6];
		}
		return null;
	}
	
	public String[] getUsedMeasureNames() {
		int nMeas = getNumUsedStats();
		String[] names = new String[nMeas];
		for (int i = 0; i < nMeas; i++) {
			int iM = PamUtils.getNthChannel(i, statisticTypes);
			names[i] = measureNames[iM];
		}
		return names;
	}

	public int getNumMeasurementBands() {
		if (bandLoEdges == null) {
			return 0;
		}
		return bandLoEdges.length;
	}
	public double[] getBandLoEdges() {
		return bandLoEdges;
	}

	public void setBandLoEdges(double[] bandLoEdges) {
		this.bandLoEdges = bandLoEdges;
	}

	public double[] getBandHiEdges() {
		return bandHiEdges;
	}

	public void setBandHiEdges(double[] bandHiEdges) {
		this.bandHiEdges = bandHiEdges;
	}
	
	public double getLowestFrequency() {
		if (bandLoEdges == null || bandLoEdges.length == 0) {
			return 1.;
		}
		double lf = bandLoEdges[0];
		for (int i = 1; i < bandLoEdges.length; i++) {
			lf = Math.min(lf, bandLoEdges[i]);
		}
		return lf;
	}

	public double getLowestFrequency10() {
		if (getLowestFrequency() <= 0) {
			return 1;
		}
		double v = Math.floor(Math.log10(getLowestFrequency()));
		return Math.pow(10., v);
	}
	
	public double getHighestFrequency() {
		if (bandHiEdges == null || bandHiEdges.length == 0) {
			return 1000.;
		}
		double hf = bandHiEdges[0];
		for (int i = 1; i < bandHiEdges.length; i++) {
			hf = Math.max(hf, bandHiEdges[i]);
		}
		return hf;
	}

	public double getHighestFrequency10() {
		double v = Math.ceil(Math.log10(getHighestFrequency()));
		return Math.pow(10., v);
	}
	
	public String[] getColumnNames() {
//		int nChan = PamUtils.getNumChannels(getChannelMap());
		if (bandLoEdges == null) {
			return null;
		}
		int nTypes = PamUtils.getNumChannels(statisticTypes);
		int nBands = bandLoEdges.length;
		int totalNames = nTypes * nBands;
		if (totalNames == 0) {
			return null;
		}
		String[] allNames = new String[totalNames];
		int statType;
		int iMeas = 0;
		for (int b = 0; b < nBands; b++) {
			for (int t = 0; t < nTypes; t++) {
				statType = PamUtils.getNthChannel(t, statisticTypes);
				allNames[iMeas++] = createDBColumnName(b, statType);
			}
		}
		return allNames;
	}
	
	public String createDBColumnName(int iBand, int iMeasure) {
//		NoiseMeasurementBand nmb = noiseSettings.getMeasurementBand(iBand);
		String mName = NoiseDataBlock.measureNames[iMeasure];
		String bandName = bandNames[iBand];
		int f1 = (int) bandLoEdges[iBand];
		int f2 = (int) bandHiEdges[iBand];
		mName = measureNames[iMeasure];
//		return String.format("%s %d %d %s", nmb.name, (int)nmb.f1, (int)nmb.f2, mName);
		return String.format("%s %d %d %s", bandName, f1, f2, mName);
	}

	public String getDefaultBandName(int iBand) {
		if (bandLoEdges == null || bandHiEdges == null || bandLoEdges.length <= iBand) {
			return null;
		}
		double[] f = new double[2];
		f[0] = bandLoEdges[iBand];
		f[1] = bandHiEdges[iBand];
		return FrequencyFormat.formatFrequencyRange(f, true);
	}

	public String[] getBandNames() {
		return bandNames;
	}

	public void setBandNames(String[] bandNames) {
		this.bandNames = bandNames;
	}

	public String[] getBandLongNames() {
		return bandLongNames;
	}

	public void setBandLongNames(String[] bandLongNames) {
		this.bandLongNames = bandLongNames;
	}
	
	public String getBandName(int iBand) {
		if (bandNames == null) {
			return getDefaultBandName(iBand);
		}
		return bandNames[iBand];
	}
	
	public String getBandLongName(int iBand) {
		if (bandLongNames == null) {
			return getDefaultBandName(iBand);
		}
		return bandLongNames[iBand];
	}
	
	/**
	 * Get the bitmap for the ith statistic. 
	 * @param statIndex
	 * @return
	 */
	public int statIndexToBit(int statIndex) {
		return 1<<PamUtils.getNthChannel(statIndex, statisticTypes);
	}

	@Override
	public AlarmCounterProvider getAlarmCounterProvider() {
		if (noiseAlarmCounter == null) {
			noiseAlarmCounter = new NoiseAlarmProvider(this);
		}
		return noiseAlarmCounter;
	}

	@Override
	public DataAutomationInfo getDataAutomationInfo() {
		return new DataAutomationInfo(DataAutomation.AUTOMATIC);
	}

	@Override
	public TethysDataProvider getTethysDataProvider(TethysControl tethysControl) {
		if (tethysNoiseDataProvider == null) {
			tethysNoiseDataProvider = new TethysNoiseDataProvider(tethysControl, this);
		}
		return tethysNoiseDataProvider;
	}

	@Override
	public DataBlockSpeciesManager<NoiseDataUnit> getDatablockSpeciesManager() {
		if (fixedSpeciesManager == null) {
			fixedSpeciesManager = new FixedSpeciesManager<NoiseDataUnit>(this, -10, "anthropogenic", "noise");
		}
		return fixedSpeciesManager;
	}
	

}
