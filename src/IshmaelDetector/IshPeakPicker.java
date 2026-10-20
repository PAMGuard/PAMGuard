package IshmaelDetector;

/**
 * The peak picker of the Ishmael detectors, for one channel: turns a detection function
 * into detections (events).
 * <p>
 * The detection function arrives one value at a time, each with the audio sample it
 * belongs to. For the spectrogram correlation and energy sum detectors there is one
 * value per FFT slice, so consecutive values are an FFT hop apart in samples; for the
 * matched filter there is one value per audio sample.
 * <p>
 * A detection starts at the first value over threshold. It ends once the function has
 * been at or below threshold for the refractory time ("Min time before next detection"),
 * so short dips inside a call do not split it. It is kept if
 * <ul>
 * <li>its duration, from the first to the last value over threshold, is at least the
 * minimum time, and</li>
 * <li>it was over threshold for no more than the maximum time.</li>
 * </ul>
 * Times are converted to the units they are compared in: the minimum time to audio
 * samples, since a duration is measured in audio samples; the maximum time and the
 * refractory time to detection function steps, since they are compared with counts of
 * steps.
 * <p>
 * Plain Java with no PAMGuard dependencies, so that it can be tested on synthetic
 * detection functions (see test.ishmael.IshPeakPickerTest). {@link IshPeakProcess} keeps
 * one picker per channel.
 */
public class IshPeakPicker {

	/** A detection: start and duration in audio samples, and its peak. */
	public static class Event {
		/** Audio sample of the first value over threshold. */
		public final long startSample;
		/** From the first to the last value over threshold, in audio samples, plus one. */
		public final long durationSamples;
		/** Audio sample of the highest value. */
		public final long peakSample;
		/** The highest value. */
		public final double peakHeight;

		Event(long startSample, long durationSamples, long peakSample, double peakHeight) {
			this.startSample = startSample;
			this.durationSamples = durationSamples;
			this.peakSample = peakSample;
			this.peakHeight = peakHeight;
		}

		@Override
		public String toString() {
			return String.format("Event start %d, duration %d, peak %.3f at %d", startSample, durationSamples,
					peakHeight, peakSample);
		}
	}

	private double threshold;

	/** Minimum duration, audio samples. */
	private long minTimeSam;

	/** Maximum number of steps over threshold. */
	private long maxTimeN;

	/** Steps at or below threshold that end a detection. */
	private long refractoryN;

	/** Minimum gap between the end of one detection and the start of the next, audio samples. */
	private long refractorySam;

	private long startSample, endSample, peakSample;
	private double peakHeight;
	private long nOverThresh, nUnderThresh;
	private long lastStartSample = -1, lastDurationSamples = -1;

	/**
	 * @param threshold the detection function must exceed this
	 * @param minTime minimum duration, seconds; 0 for none
	 * @param maxTime maximum time over threshold, seconds; 0 for none
	 * @param refractoryTime time at or below threshold that ends a detection, and minimum gap
	 * between detections, seconds
	 * @param audioRate sample rate of the audio the detection function was computed from, Hz
	 * @param detectionRate values of the detection function per second: the audio rate for the
	 * matched filter, audio rate / FFT hop for the spectrogram detectors
	 */
	public IshPeakPicker(double threshold, double minTime, double maxTime, double refractoryTime,
			double audioRate, double detectionRate) {
		setParams(threshold, minTime, maxTime, refractoryTime, audioRate, detectionRate);
		reset();
	}

	/**
	 * Change the settings, keeping any call in progress and the last detection, as
	 * PAMGuard recalculates settings when sample rates are announced. Arguments as for the
	 * constructor.
	 */
	public void setParams(double threshold, double minTime, double maxTime, double refractoryTime,
			double audioRate, double detectionRate) {
		this.threshold = threshold;
		minTimeSam = Math.max(0, (long) (audioRate * minTime));
		// maxTimeN + 1 is tested below, so "no maximum" must not overflow
		maxTimeN = maxTime == 0 ? Long.MAX_VALUE - 1 : Math.max(0, (long) (detectionRate * maxTime));
		refractoryN = Math.max(0, (long) (detectionRate * refractoryTime));
		refractorySam = Math.max(0, (long) (audioRate * refractoryTime));
	}

	/**
	 * Add the next value of the detection function.
	 * @param sample audio sample the value belongs to
	 * @param value the detection function
	 * @return a detection that this value completes, or null
	 */
	public Event add(long sample, double value) {
		if (value > threshold && nOverThresh <= maxTimeN + 1) {
			if (nOverThresh == 0) {
				peakHeight = Double.NEGATIVE_INFINITY;
				startSample = sample;
			}
			endSample = sample;
			nUnderThresh = 0;
			nOverThresh++;
			if (value >= peakHeight) {
				peakHeight = value;
				peakSample = sample;
			}
			return null;
		}
		nUnderThresh++;
		if (nUnderThresh < refractoryN) {
			// calls cross threshold many times: wait before ending this one
			return null;
		}
		Event event = null;
		if (nOverThresh > 0 && durationSamples() >= minTimeSam && nOverThresh <= maxTimeN) {
			long gap = Math.abs(startSample - (lastStartSample + lastDurationSamples));
			if (lastStartSample == -1 || startSample < lastStartSample || gap >= refractorySam) {
				event = new Event(startSample, durationSamples(), peakSample, peakHeight);
			}
			lastStartSample = startSample;
			lastDurationSamples = durationSamples();
		}
		clearCall();
		return event;
	}

	/** Forget everything, including the last detection, e.g. at the start of a run. */
	public void reset() {
		clearCall();
		nUnderThresh = 0;
		lastStartSample = -1;
		lastDurationSamples = -1;
	}

	private void clearCall() {
		nOverThresh = 0;
		startSample = endSample = 0;
		peakHeight = 0;
	}

	private long durationSamples() {
		return endSample - startSample + 1;
	}

	/** @return minimum duration, audio samples. */
	public long getMinTimeSamples() {
		return minTimeSam;
	}

	/** @return steps at or below threshold that end a detection. */
	public long getRefractorySteps() {
		return refractoryN;
	}
}
