package test.difar;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;

import difar.DIFARTargetMotionInformation;
import difar.DifarLocalisationResiduals;
import difar.targetmotion.Simplex2D;
import difar.targetmotion.TargetMotionResult;

/**
 * How DIFAR localisation behaves when the measurements carry realistic errors.
 * <p>
 * These tests use correct matches throughout. Every detection is of the same
 * call, and only the measurements are noisy. They answer two questions: how far
 * the calculated position sits from the truth, and how often a correct match is
 * thrown away by the residual limits.
 * <p>
 * The numbers are printed, since they are the evidence for choosing those
 * limits. The assertions are deliberately loose, and are there to catch a
 * change that breaks the fit rather than to pin down exact values.
 */
public class DifarLocalisationNoiseTest {

	/** Trials per case. Enough for a stable median, fast enough to run often. */
	private static final int TRIALS = 500;

	/** Bearing error of a real DIFAR measurement, in degrees. */
	private static final double BEARING_NOISE_DEG = 5.0;

	/** Timing error of a clip marked by hand, in seconds. */
	private static final double TIMING_NOISE_S = 2.0;

	private static final double[][] TWO_BUOYS = {{0, 0}, {15000, 0}};
	private static final double[][] THREE_BUOYS = {{0, 0}, {15000, 0}, {7500, -12000}};

	/** A source inside the buoy field. */
	private static final double[] NEAR_SOURCE = {5000, 8000};

	/** A source well outside it, where bearings cross at a shallow angle. */
	private static final double[] FAR_SOURCE = {5000, 40000};

	@Test
	public void pairWithRealisticNoise() {
		Result result = run(TWO_BUOYS, NEAR_SOURCE, BEARING_NOISE_DEG, TIMING_NOISE_S);
		result.print("two buoys, source inside the field");
		assertTrue(result.medianPositionError < 5000,
				"median position error should stay within a few km: " + result.medianPositionError);
		assertTrue(result.keptFraction(20, 8) > 0.75,
				"most correct matches should pass the limits: " + result.keptFraction(20, 8));
	}

	@Test
	public void tripletWithRealisticNoise() {
		Result result = run(THREE_BUOYS, NEAR_SOURCE, BEARING_NOISE_DEG, TIMING_NOISE_S);
		result.print("three buoys, source inside the field");
		assertTrue(result.medianPositionError < 5000,
				"median position error should stay within a few km: " + result.medianPositionError);
	}

	@Test
	public void distantSourceIsLessCertain() {
		Result near = run(TWO_BUOYS, NEAR_SOURCE, BEARING_NOISE_DEG, TIMING_NOISE_S);
		Result far = run(TWO_BUOYS, FAR_SOURCE, BEARING_NOISE_DEG, TIMING_NOISE_S);
		near.print("near source");
		far.print("far source");
		assertTrue(far.medianPositionError > near.medianPositionError,
				"a distant source should be harder to place than a near one");
	}

	/**
	 * Bearing errors alone should not produce timing residuals, and timing
	 * errors alone should not produce bearing residuals. This is what lets an
	 * operator tell the two apart.
	 */
	@Test
	public void residualsSeparateTheTwoErrorSources() {
		Result bearingsOnly = run(TWO_BUOYS, NEAR_SOURCE, BEARING_NOISE_DEG, 0);
		Result timingOnly = run(TWO_BUOYS, NEAR_SOURCE, 0, TIMING_NOISE_S);
		bearingsOnly.print("bearing noise only");
		timingOnly.print("timing noise only");
		assertTrue(timingOnly.medianDelayResidual > bearingsOnly.medianDelayResidual,
				"timing noise should show up as a timing residual");
	}

	/**
	 * Run a set of trials.
	 * @param buoys buoy positions in metres
	 * @param source source position in metres
	 * @param bearingNoiseDeg standard deviation of the bearing errors
	 * @param timingNoiseSeconds standard deviation of the timing errors
	 * @return the spread of position errors and residuals.
	 */
	private Result run(double[][] buoys, double[] source, double bearingNoiseDeg, double timingNoiseSeconds) {
		Random random = new Random(20260918L);
		Result result = new Result(TRIALS);
		for (int trial = 0; trial < TRIALS; trial++) {
			DifarTestScenario scenario = new DifarTestScenario();
			for (double[] buoy : buoys) {
				scenario.addDetection(buoy, source,
						random.nextGaussian() * bearingNoiseDeg,
						random.nextGaussian() * timingNoiseSeconds);
			}
			DIFARTargetMotionInformation info = scenario.build(TIMING_NOISE_S);
			Simplex2D simplex = new Simplex2D();
			simplex.setStartPoint(info.getMeanPosition());
			TargetMotionResult[] results = simplex.runModel(info);
			if (results == null || results.length != 1 || results[0] == null
					|| results[0].getLatLong() == null) {
				result.failures++;
				continue;
			}
			DifarLocalisationResiduals residuals =
					DifarLocalisationResiduals.calculate(info, results[0].getLatLong());
			result.add(DifarTestScenario.distanceFrom(results[0].getLatLong(), source),
					residuals.getMaxBearingErrorDegrees(),
					residuals.getMaxTimeDelayErrorSeconds());
		}
		result.finish();
		return result;
	}

	/** The spread of position errors and residuals over a set of trials. */
	private static class Result {

		private final double[] positionErrors;
		private final double[] bearingResiduals;
		private final double[] delayResiduals;
		private int n = 0;
		private int failures = 0;

		private double medianPositionError;
		private double positionError90;
		private double medianBearingResidual;
		private double medianDelayResidual;
		private double delayResidual90;

		Result(int trials) {
			positionErrors = new double[trials];
			bearingResiduals = new double[trials];
			delayResiduals = new double[trials];
		}

		void add(double positionError, double bearingResidual, double delayResidual) {
			positionErrors[n] = positionError;
			bearingResiduals[n] = bearingResidual;
			delayResiduals[n] = delayResidual;
			n++;
		}

		void finish() {
			medianPositionError = percentile(positionErrors, 0.5);
			positionError90 = percentile(positionErrors, 0.9);
			medianBearingResidual = percentile(bearingResiduals, 0.5);
			medianDelayResidual = percentile(delayResiduals, 0.5);
			delayResidual90 = percentile(delayResiduals, 0.9);
		}

		/** @return the fraction of trials that would pass the given limits. */
		double keptFraction(double maxBearingDeg, double maxDelaySeconds) {
			if (n == 0) {
				return 0;
			}
			int kept = 0;
			for (int i = 0; i < n; i++) {
				if (bearingResiduals[i] <= maxBearingDeg && delayResiduals[i] <= maxDelaySeconds) {
					kept++;
				}
			}
			return (double) kept / n;
		}

		private double percentile(double[] values, double fraction) {
			if (n == 0) {
				return Double.NaN;
			}
			double[] sorted = Arrays.copyOf(values, n);
			Arrays.sort(sorted);
			return sorted[Math.min(n - 1, (int) Math.round(fraction * (n - 1)))];
		}

		void print(String label) {
			System.out.printf("%s: %d trials, %d failed to fit%n", label, n, failures);
			System.out.printf("  position error: median %.0f m, 90%% %.0f m%n",
					medianPositionError, positionError90);
			System.out.printf("  bearing residual: median %.2f deg%n", medianBearingResidual);
			System.out.printf("  timing residual: median %.2f s, 90%% %.2f s%n",
					medianDelayResidual, delayResidual90);
			System.out.printf("  kept at 20 deg and 3 s: %.0f%%, at 20 deg and 8 s: %.0f%%%n",
					100 * keptFraction(20, 3), 100 * keptFraction(20, 8));
		}
	}
}
