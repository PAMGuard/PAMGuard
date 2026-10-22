package difar;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import PamguardMVC.PamDataUnit;

/**
 * The results of correlating one seed DIFAR clip with its candidate partners.
 * <p>
 * For each partner it holds how the correlation went: the height of the best
 * peak and whether that passed the threshold, or why there was no correlation
 * at all. For the partners that passed, it holds their arrival times: the
 * seed's time plus the measured delay. The seed is included at its own time if
 * any partner passed. Localisation uses the arrival times; the triangulation
 * residual log uses the rest.
 */
public class CorrelatedArrivals {

	/** How the correlation of a partner with the seed went. */
	public enum Status {
		/** A peak passed the threshold; the partner has an arrival time. */
		CORRELATED,
		/** The best peak was below the threshold, or there was no peak at all. */
		BELOW_THRESHOLD,
		/**
		 * The clips could not be compared: one had no demultiplexed audio, or
		 * their sample rates, FFT settings or frequency limits did not match.
		 */
		NOT_COMPARABLE
	}

	/** The correlation of one partner with the seed. */
	public static final class Measurement {

		private final Status status;
		private final double height;
		private final double delaySeconds;

		/**
		 * @param status how the correlation went.
		 * @param height height of the best peak, between -1 and 1, or NaN if
		 * there was none.
		 * @param delaySeconds delay of the best peak, in seconds, or NaN if
		 * there was none.
		 */
		public Measurement(Status status, double height, double delaySeconds) {
			this.status = status;
			this.height = height;
			this.delaySeconds = delaySeconds;
		}

		public Status getStatus() {
			return status;
		}

		/** @return height of the best peak, or NaN if there was none. */
		public double getHeight() {
			return height;
		}

		/** @return delay of the best peak in seconds, or NaN if there was none. */
		public double getDelaySeconds() {
			return delaySeconds;
		}
	}

	private final DifarDataUnit seed;

	private final Map<PamDataUnit, Double> arrivalMillis = new HashMap<>();

	private final Map<PamDataUnit, Measurement> measurements = new HashMap<>();

	/** @param seed the clip the partners were correlated with. */
	public CorrelatedArrivals(DifarDataUnit seed) {
		this.seed = seed;
	}

	/**
	 * Record the correlation of a partner with the seed.
	 * @param partner the partner clip.
	 * @param measurement how its correlation went.
	 */
	public void add(PamDataUnit partner, Measurement measurement) {
		measurements.put(partner, measurement);
		if (measurement.getStatus() == Status.CORRELATED) {
			arrivalMillis.put(partner, seed.getTimeMilliseconds() + measurement.getDelaySeconds() * 1000.);
			arrivalMillis.put(seed, (double) seed.getTimeMilliseconds());
		}
	}

	/** @return the clip the partners were correlated with. */
	public DifarDataUnit getSeed() {
		return seed;
	}

	/**
	 * @return arrival times in milliseconds of the seed and of the partners
	 * that passed the threshold. Empty if none passed. Cannot be changed.
	 */
	public Map<PamDataUnit, Double> getArrivalMillis() {
		return Collections.unmodifiableMap(arrivalMillis);
	}

	/**
	 * @param partner a partner clip.
	 * @return how its correlation with the seed went, or null if it was not
	 * correlated with this seed (or is the seed itself).
	 */
	public Measurement getMeasurement(PamDataUnit partner) {
		return measurements.get(partner);
	}
}
