package annotation.calcs.measurement;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;

import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RawDataUnavailableException;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;

/**
 * A set of sound measurement annotation types, one per {@link SoundMeasurement}, which
 * share their spectrogram settings and their calculation. The sound under a data unit is
 * loaded and measured once and the result is cached, so annotating a data unit with many
 * measurements costs no more than annotating it with one.
 *
 * @author Jamie Macaulay
 */
public class SoundMeasurementGroup {

	/**
	 * Parameters shared by all the measurements. Every measurement type's options hold a
	 * reference to this same object.
	 */
	private SoundMeasurementParameters parameters = new SoundMeasurementParameters();

	private ArrayList<SoundMeasurementAnnotationType> annotationTypes = new ArrayList<>();

	/**
	 * Finds the raw data to measure for a data unit. Can be null.
	 */
	private Function<PamDataUnit, PamRawDataBlock> rawSourceFinder;

	/*
	 * Cache of the last measurement. The measurement types are called one after another
	 * for the same data unit, so one entry is enough.
	 */
	private PamDataUnit cachedUnit;
	private String cachedKey;
	private EnumMap<SoundMeasurement, Double> cachedValues;

	/**
	 * Create the group and an annotation type for each measurement.
	 * @param rawSourceFinder finds the raw data block for a data unit. If null, or it returns
	 * null, the first raw source of the unit's data block (or the first raw data block) is used.
	 */
	public SoundMeasurementGroup(Function<PamDataUnit, PamRawDataBlock> rawSourceFinder) {
		this.rawSourceFinder = rawSourceFinder;
		for (SoundMeasurement m : SoundMeasurement.values()) {
			annotationTypes.add(new SoundMeasurementAnnotationType(this, m));
		}
	}

	/**
	 * @return one annotation type per measurement, in the order the measurements are defined.
	 */
	public List<SoundMeasurementAnnotationType> getAnnotationTypes() {
		return annotationTypes;
	}

	/**
	 * @return the parameters shared by all the measurements.
	 */
	public SoundMeasurementParameters getParameters() {
		return parameters;
	}

	/**
	 * Set the parameters for all the measurements.
	 * @param parameters new parameters.
	 */
	public void setParameters(SoundMeasurementParameters parameters) {
		if (parameters == null) {
			return;
		}
		this.parameters = parameters;
		for (SoundMeasurementAnnotationType type : annotationTypes) {
			type.getMeasurementOptions().setParameters(parameters);
		}
		invalidate();
	}

	/**
	 * Clear the cached measurement so the next request re-measures, e.g. when the user
	 * asks for measurements to be recalculated.
	 */
	public void invalidate() {
		cachedUnit = null;
		cachedKey = null;
		cachedValues = null;
	}

	/**
	 * Measure the sound within the time and frequency limits of a data unit. Results are
	 * cached until the data unit or its limits change.
	 * @param pamDataUnit the data unit.
	 * @return all the measurements, or null if they couldn't be made (a reason is printed).
	 */
	public synchronized EnumMap<SoundMeasurement, Double> measure(PamDataUnit pamDataUnit) {
		String key = cacheKey(pamDataUnit);
		if (pamDataUnit == cachedUnit && key.equals(cachedKey)) {
			return cachedValues;
		}
		EnumMap<SoundMeasurement, Double> values = doMeasure(pamDataUnit);
		// failures are cached too, so a unit without sound isn't loaded once per measurement.
		cachedUnit = pamDataUnit;
		cachedKey = key;
		cachedValues = values;
		return values;
	}

	private String cacheKey(PamDataUnit unit) {
		double[] f = unit.getFrequency();
		return String.format("%d_%d_%s_%d_%d_%d_%d", unit.getTimeMilliseconds(),
				unit.getEndTimeInMilliseconds(), f == null ? "null" : f[0] + "_" + f[1], unit.getChannelBitmap(),
				parameters.fftLength, parameters.fftHop, parameters.windowType);
	}

	private EnumMap<SoundMeasurement, Double> doMeasure(PamDataUnit pamDataUnit) {
		long start = pamDataUnit.getTimeMilliseconds();
		long end = pamDataUnit.getEndTimeInMilliseconds();
		if (end <= start) {
			return null;
		}
		PamRawDataBlock rawBlock = findRawDataBlock(pamDataUnit);
		if (rawBlock == null || rawBlock.getSampleRate() <= 0) {
			System.out.println("Sound measurements: no raw data source for UID " + pamDataUnit.getUID());
			return null;
		}
		int channel = findChannel(pamDataUnit.getChannelBitmap(), rawBlock.getChannelMap());
		if (channel < 0) {
			return null;
		}
		float sampleRate = rawBlock.getSampleRate();
		double[] wav = loadSamples(rawBlock, start, end, channel);
		int expected = (int) ((end - start) * sampleRate / 1000.);
		if (wav == null || wav.length < Math.max(2, expected / 2)) {
			System.out.printf("Sound measurements: could not get the sound for UID %d from %s (%d of %d samples)\n",
					pamDataUnit.getUID(), rawBlock.getLongDataName(), wav == null ? 0 : wav.length, expected);
			return null;
		}

		double[] f = pamDataUnit.getFrequency();
		double fLow = 0, fHigh = sampleRate / 2.;
		if (f != null && f.length >= 2) {
			fLow = Math.max(0, Math.min(f[0], f[1]));
			fHigh = Math.min(sampleRate / 2., Math.max(f[0], f[1]));
		}
		if (fHigh <= fLow) {
			System.out.printf("Sound measurements: the frequency range of UID %d is above the Nyquist frequency of %s\n",
					pamDataUnit.getUID(), rawBlock.getLongDataName());
			return null;
		}
		SoundMeasurementCalculator calculator = new SoundMeasurementCalculator(parameters.fftLength,
				parameters.fftHop, parameters.windowType);
		return calculator.calculate(wav, sampleRate, fLow, fHigh);
	}

	/**
	 * Get the samples for a period. Uses data already in memory if it covers the whole
	 * period, otherwise (in viewer mode) loads the data directly into a collector, so that
	 * other loads into the same data block (e.g. for displays) can't clear the data
	 * between it being loaded and read.
	 */
	private double[] loadSamples(PamRawDataBlock rawBlock, long start, long end, int channel) {
		try {
			RawDataUnit[] inMemory = rawBlock.getAvailableSamples(start, end - start, 1 << channel);
			if (inMemory != null && inMemory.length > 0 && inMemory[0].getTimeMilliseconds() <= start
					&& inMemory[0].getEndTimeInMilliseconds() >= end - 1) {
				return inMemory[0].getRawData();
			}
		}
		catch (RawDataUnavailableException e) {
			// not in memory - load it below.
		}
		if (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW) {
			return null;
		}
		SampleCollector collector = new SampleCollector(channel);
		rawBlock.getOfflineData(new OfflineDataLoadInfo(collector, null, start, end, 0, true));
		return collector.getSamples(start, end, rawBlock.getSampleRate());
	}

	/**
	 * Collects the samples of one channel from an offline data load.
	 */
	private static class SampleCollector extends PamObserverAdapter {

		private int channel;

		private ArrayList<RawDataUnit> units = new ArrayList<>();

		SampleCollector(int channel) {
			this.channel = channel;
		}

		@Override
		public String getObserverName() {
			return "Sound measurements";
		}

		@Override
		public void addData(PamObservable observable, PamDataUnit pamDataUnit) {
			if (pamDataUnit instanceof RawDataUnit && (pamDataUnit.getChannelBitmap() & (1 << channel)) != 0) {
				units.add((RawDataUnit) pamDataUnit);
			}
		}

		/**
		 * @return the samples between start and end, or null if none arrived.
		 */
		double[] getSamples(long start, long end, float sampleRate) {
			if (units.isEmpty()) {
				return null;
			}
			int total = 0;
			for (RawDataUnit u : units) {
				total += u.getRawData().length;
			}
			double[] all = new double[total];
			int pos = 0;
			for (RawDataUnit u : units) {
				double[] d = u.getRawData();
				System.arraycopy(d, 0, all, pos, d.length);
				pos += d.length;
			}
			long firstMillis = units.get(0).getTimeMilliseconds();
			int i0 = (int) Math.max(0, Math.round((start - firstMillis) * sampleRate / 1000.));
			int i1 = (int) Math.min(total, Math.round((end - firstMillis) * sampleRate / 1000.));
			if (i1 <= i0) {
				return null;
			}
			double[] out = new double[i1 - i0];
			System.arraycopy(all, i0, out, 0, out.length);
			return out;
		}
	}

	private PamRawDataBlock findRawDataBlock(PamDataUnit pamDataUnit) {
		if (rawSourceFinder != null) {
			PamRawDataBlock rawBlock = rawSourceFinder.apply(pamDataUnit);
			if (rawBlock != null) {
				return rawBlock;
			}
		}
		PamDataBlock parent = pamDataUnit.getParentDataBlock();
		if (parent != null) {
			PamRawDataBlock rawBlock = parent.getFirstRawSourceDataBlock();
			if (rawBlock != null) {
				return rawBlock;
			}
		}
		return PamController.getInstance().getRawDataBlock(0);
	}

	/**
	 * Get the lowest channel of the data unit which is in the raw data.
	 * @return channel number or -1 if there are no raw channels.
	 */
	private int findChannel(int unitChannels, int rawChannels) {
		int common = unitChannels & rawChannels;
		if (common == 0) {
			// e.g. sequence numbers in the data unit. Use the first raw channel.
			common = rawChannels;
		}
		return common == 0 ? -1 : PamUtils.getLowestChannel(common);
	}

}
