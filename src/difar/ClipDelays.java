package difar;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import difar.CorrelatedArrivals.Measurement;
import difar.CorrelatedArrivals.Status;

/**
 * Time delays between DIFAR clips of the same call, measured by
 * cross-correlating their spectrograms with {@link ClipCorrelator}.
 * <p>
 * Each clip's spectrogram is made once from the omni channel of its
 * demultiplexed audio, and each pair's delay is measured once. Both are kept
 * in caches of limited size. A delay depends only on the two clips' audio and
 * frequency limits, not on the bearing chosen in the DIFARGram, so matching a
 * clip again after a click on the DIFARGram finds its delays in the cache and
 * does not correlate again.
 * <p>
 * The best peak is kept whatever its height, and the threshold is applied
 * afterwards, so the height of a peak that fell short can be reported, and a
 * new threshold needs no new correlation.
 * <p>
 * The demultiplexed audio is taken to start at the clip's time. That holds
 * for the AMMC demultiplexer. The Greeneridge demultiplexer trims audio where
 * it lost lock on the carrier, and the trim is not recorded, so its clips can
 * start later than their time.
 * <p>
 * Matching runs on the demultiplexing worker's thread, the offline task
 * thread, and the Swing event thread after a click on the DIFARGram. The
 * methods here are synchronized, so one correlation runs at a time.
 */
public class ClipDelays {

	/** Most spectrograms kept. About 150 kB each for a 2 s clip. */
	private static final int MAX_SPECTROGRAMS = 200;

	/** Most pair delays kept. */
	private static final int MAX_DELAYS = 20000;

	private final DifarControl difarControl;

	private final Map<String, double[][]> spectrograms = lru(MAX_SPECTROGRAMS);

	/** The best peak for each pair, whatever its height. */
	private final Map<String, Measurement> delays = lru(MAX_DELAYS);

	private ClipCorrelator correlator;

	/** The settings the correlator and the cached delays were made with. */
	private String settingsKey;

	private int correlated, fromCache, noPeak, noAudio;
	private long correlationNanos;

	/** Sum of the heights of the peaks that passed the threshold, for the summary. */
	private double passedHeights;
	private int passed;

	/**
	 * @param difarControl the DIFAR module, for its settings and FFT sizes.
	 */
	public ClipDelays(DifarControl difarControl) {
		this.difarControl = difarControl;
	}

	/**
	 * Correlate a call on one clip with another: the delay is its arrival
	 * time on the other clip minus its arrival time on the first.
	 * @param first the first clip, normally the seed being matched.
	 * @param other a clip on another sonobuoy that may hold the same call.
	 * @param maxDelaySeconds largest delay possible between the two
	 * sonobuoys, in seconds.
	 * @return how the correlation went: the best peak's height and delay, and
	 * whether it passed the threshold. Never null.
	 */
	public synchronized Measurement measure(DifarDataUnit first, DifarDataUnit other, double maxDelaySeconds) {
		DifarParameters params = difarControl.getDifarParameters();
		checkSettings(params);
		Measurement measurement = correlate(first, other, maxDelaySeconds);
		if (measurement.getStatus() == Status.NOT_COMPARABLE) {
			noAudio++;
			return measurement;
		}
		if (Double.isNaN(measurement.getHeight()) || measurement.getHeight() < params.correlationThreshold) {
			noPeak++;
			return new Measurement(Status.BELOW_THRESHOLD, measurement.getHeight(), measurement.getDelaySeconds());
		}
		passed++;
		passedHeights += measurement.getHeight();
		return new Measurement(Status.CORRELATED, measurement.getHeight(), measurement.getDelaySeconds());
	}

	/**
	 * @return the best peak between two clips, from the cache if it is there,
	 * with status CORRELATED whatever its height, or NOT_COMPARABLE.
	 */
	private Measurement correlate(DifarDataUnit first, DifarDataUnit other, double maxDelaySeconds) {
		Measurement notComparable = new Measurement(Status.NOT_COMPARABLE, Double.NaN, Double.NaN);
		float sampleRate = first.getDisplaySampleRate();
		int fftLength = difarControl.getDifarProcess().getDisplayFFTLength(first);
		int hop = difarControl.getDifarProcess().getDisplayFFTHop(first);
		if (sampleRate <= 0 || other.getDisplaySampleRate() != sampleRate
				|| difarControl.getDifarProcess().getDisplayFFTLength(other) != fftLength
				|| difarControl.getDifarProcess().getDisplayFFTHop(other) != hop) {
			return notComparable;
		}
		int[] bins = binRange(first.getFrequency(), other.getFrequency(), sampleRate, fftLength);
		if (bins == null) {
			return notComparable;
		}
		String key = String.format("%d:%d:%d:%d:%d:%d:%.3f", first.getUID(), first.getTimeMilliseconds(),
				other.getUID(), other.getTimeMilliseconds(), bins[0], bins[1], maxDelaySeconds);
		Measurement cached = delays.get(key);
		if (cached != null) {
			fromCache++;
			return cached;
		}
		double[][] specA = getSpectrogram(first, fftLength, hop);
		double[][] specB = getSpectrogram(other, fftLength, hop);
		if (specA == null || specB == null) {
			return notComparable;
		}
		long start = System.nanoTime();
		double offsetSeconds = (other.getTimeMilliseconds() - first.getTimeMilliseconds()) / 1000.;
		List<ClipCorrelator.Peak> peaks = correlator.correlate(specA, specB, (double) hop / sampleRate,
				offsetSeconds, bins, maxDelaySeconds);
		correlationNanos += System.nanoTime() - start;
		correlated++;
		Measurement measurement = peaks.isEmpty() ? new Measurement(Status.CORRELATED, Double.NaN, Double.NaN)
				: new Measurement(Status.CORRELATED, peaks.get(0).getHeight(), peaks.get(0).getDelaySeconds());
		delays.put(key, measurement);
		return measurement;
	}

	/**
	 * Report what was done since the last report, and start counting again.
	 * @return a line for the console, or null if nothing was correlated.
	 */
	public synchronized String takeSummary() {
		String summary = null;
		if (correlated + fromCache + noAudio > 0) {
			summary = String.format("DIFAR: %d pairs of clips looked up (%d correlated in %.1f s, %d from the cache): "
					+ "%d passed the threshold, with a mean peak of %.2f; %d had no peak above it; "
					+ "%d could not be correlated (no audio, or different FFT settings or bands)",
					passed + noPeak + noAudio, correlated, correlationNanos / 1e9, fromCache,
					passed, passed > 0 ? passedHeights / passed : Double.NaN, noPeak, noAudio);
		}
		correlated = fromCache = noPeak = noAudio = passed = 0;
		correlationNanos = 0;
		passedHeights = 0;
		return summary;
	}

	/**
	 * Forget every spectrogram and delay, for example after clips are
	 * reloaded or their audio changes.
	 */
	public synchronized void clear() {
		spectrograms.clear();
		delays.clear();
	}

	/** Make a new correlator, and forget the delays, if the correlation settings changed. */
	private void checkSettings(DifarParameters params) {
		String key = params.correlationSmoothing + ":" + params.correlationMinSeparation + ":"
				+ params.correlationMinOverlap;
		if (!key.equals(settingsKey)) {
			// every peak is kept here; the threshold is applied in measure()
			correlator = new ClipCorrelator(-1, 1, params.correlationMinOverlap,
					params.correlationSmoothing, params.correlationMinSeparation);
			delays.clear();
			settingsKey = key;
		}
	}

	/**
	 * @return the spectrogram of a clip's omni channel, made once and kept,
	 * or null if the clip has no demultiplexed audio.
	 */
	private double[][] getSpectrogram(DifarDataUnit unit, int fftLength, int hop) {
		String key = String.format("%d:%d:%d:%d", unit.getUID(), unit.getTimeMilliseconds(), fftLength, hop);
		double[][] spec = spectrograms.get(key);
		if (spec != null) {
			return spec;
		}
		double[][] demux = unit.getDemuxedDecimatedData();
		if (demux == null || demux.length < 1 || demux[0] == null) {
			return null;
		}
		// the demultiplexed channels are omni, east-west, north-south
		spec = ClipCorrelator.spectrogram(demux[0], fftLength, hop);
		if (spec != null) {
			spectrograms.put(key, spec);
		}
		return spec;
	}

	/**
	 * The bins covering both clips' frequency limits.
	 * @return first bin and one past the last, or null if the limits do not
	 * overlap or are missing.
	 */
	static int[] binRange(double[] fA, double[] fB, float sampleRate, int fftLength) {
		if (fA == null || fB == null) {
			return null;
		}
		double low = Math.max(fA[0], fB[0]);
		double high = Math.min(fA[1], fB[1]);
		if (high <= low) {
			return null;
		}
		double binWidth = sampleRate / fftLength;
		int first = Math.max(0, (int) Math.floor(low / binWidth));
		int last = Math.min(fftLength / 2, (int) Math.ceil(high / binWidth) + 1);
		return last - first < 2 ? null : new int[] {first, last};
	}

	/** A map that forgets its least recently used entries beyond a size. */
	private static <K, V> Map<K, V> lru(int maxEntries) {
		return new LinkedHashMap<K, V>(16, 0.75f, true) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
				return size() > maxEntries;
			}
		};
	}
}
