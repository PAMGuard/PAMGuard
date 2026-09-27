package clickTrainDetector.clickTrainAlgorithms.ukf.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import PamguardMVC.PamDataUnit;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtMAT.SimpleClick;
import clickTrainDetector.clickTrainAlgorithms.mht.test.SimpleClickDataBlock;
import clickTrainDetector.clickTrainAlgorithms.ukf.AffinityNN;
import clickTrainDetector.clickTrainAlgorithms.ukf.AffinityTrainer;
import clickTrainDetector.clickTrainAlgorithms.ukf.UKFParams;
import clickTrainDetector.clickTrainAlgorithms.ukf.UKFTracker;

/**
 * Tests for the UKF affinity-network trainer. Synthesises a few well-separated
 * annotated "events", generates training examples, trains the MLP, and checks
 * that it learns to tell true continuations from distractors and that the saved
 * JSON round-trips through {@link AffinityNN#fromFile}.
 *
 * @author Jamie Macaulay
 */
@SuppressWarnings("rawtypes")
public class TestAffinityTrainer {

	private static final float SR = 500000;

	private static int failures = 0;

	public static void main(String[] args) throws Exception {

		// three amplitude-separated trains, interleaved in time, act as annotated events.
		List<List<PamDataUnit>> events = new ArrayList<>();
		events.add(makeTrain(0, 1.00, 0.10, 130, 25, new Random(1)));
		events.add(makeTrain(100, 1.04, 0.11, 110, 25, new Random(2)));
		events.add(makeTrain(200, 1.08, 0.09, 92, 25, new Random(3)));

		UKFParams params = new UKFParams();
		params.maxICI = 0.4;
		params.useAmplitude = true;
		params.useBearing = false;

		AffinityTrainer.Dataset data = AffinityTrainer.generateExamples(events, params, false);
		assertTrue("Examples generated (" + data.nPositive + " positive, " + data.nNegative + " negative)",
				data.nPositive > 20 && data.nNegative > 20);

		File out = File.createTempFile("affinity_trained", ".json");
		out.deleteOnExit();
		AffinityTrainer.Result result = AffinityTrainer.trainAndExport(events, params, false,
				AffinityTrainer.DEFAULT_HIDDEN, AffinityTrainer.DEFAULT_EPOCHS, null, out);

		assertTrue("Trained network separates true vs distractor (accuracy " + pct(result.accuracy) + ")",
				result.accuracy > 0.8);

		// the saved file round-trips and behaves like the trained network.
		AffinityNN loaded = AffinityNN.fromFile(out, UKFTracker.FEATURE_DIM);
		double meanPos = 0;
		double meanNeg = 0;
		int nP = 0;
		int nN = 0;
		for (int i = 0; i < data.x.length; i++) {
			if (data.y[i] > 0.5) {
				meanPos += loaded.affinity(data.x[i]);
				nP++;
			} else {
				meanNeg += loaded.affinity(data.x[i]);
				nN++;
			}
		}
		meanPos /= nP;
		meanNeg /= nN;
		assertTrue("Loaded network scores true continuations above distractors (pos " + fmt(meanPos) + " > neg "
				+ fmt(meanNeg) + ")", meanPos > meanNeg + 0.2);

		testPeakFreqFeature();
		testDropoutRobustness();

		System.out.println("\n==================================");
		if (failures == 0) {
			System.out.println("ALL AFFINITY TRAINER TESTS PASSED");
		} else {
			System.out.println(failures + " AFFINITY TRAINER TEST(S) FAILED");
		}
		System.out.println("==================================");
		System.exit(failures == 0 ? 0 : 1);
	}

	/**
	 * Two interleaved trains with identical amplitude but clicks of different peak
	 * frequency. With the peak-frequency feature enabled the trainer should compute
	 * it from the waveforms and learn to tell true continuations from distractors.
	 */
	private static void testPeakFreqFeature() throws Exception {
		SimpleClickDataBlock block = new SimpleClickDataBlock();
		double[] waveA = clickWaveform(40000, 64);
		double[] waveB = clickWaveform(120000, 64);
		List<PamDataUnit> trainA = makeWaveTrain(block, 0, 1.00, 0.10, 120, waveA, 25, new Random(11));
		List<PamDataUnit> trainB = makeWaveTrain(block, 100, 1.05, 0.10, 120, waveB, 25, new Random(12));
		List<List<PamDataUnit>> events = new ArrayList<>();
		events.add(trainA);
		events.add(trainB);

		UKFParams params = new UKFParams();
		params.maxICI = 0.4;
		params.useAmplitude = true; // identical amplitude, so not discriminative here
		params.useBearing = false;
		params.usePeakFreq = true;
		params.useCorrelation = false;

		AffinityTrainer.Dataset data = AffinityTrainer.generateExamples(events, params, false);
		boolean pfPresent = false;
		for (double[] row : data.x) {
			if (row[6] != UKFTracker.ABSENT) {
				pfPresent = true;
				break;
			}
		}
		assertTrue("Peak-frequency feature is computed from the waveforms", pfPresent);

		AffinityTrainer.Result result = AffinityTrainer.trainAndExport(events, params, false,
				AffinityTrainer.DEFAULT_HIDDEN, AffinityTrainer.DEFAULT_EPOCHS, null, null);
		assertTrue("Network separates peak-frequency-distinct trains (accuracy " + pct(result.accuracy) + ")",
				result.accuracy > 0.8);
	}

	/**
	 * A network trained (with feature dropout) on trains separable by both amplitude
	 * and peak frequency should still produce valid affinities and keep separating
	 * true continuations from distractors when peak frequency is switched off at
	 * inference (fed as {@link UKFTracker#ABSENT}) - i.e. it degrades gracefully.
	 */
	private static void testDropoutRobustness() throws Exception {
		SimpleClickDataBlock block = new SimpleClickDataBlock();
		double[] waveA = clickWaveform(40000, 64);
		double[] waveB = clickWaveform(120000, 64);
		List<PamDataUnit> trainA = makeWaveTrain(block, 0, 1.00, 0.10, 130, waveA, 30, new Random(13));
		List<PamDataUnit> trainB = makeWaveTrain(block, 100, 1.04, 0.11, 95, waveB, 30, new Random(14));
		List<List<PamDataUnit>> events = new ArrayList<>();
		events.add(trainA);
		events.add(trainB);

		UKFParams params = new UKFParams();
		params.maxICI = 0.4;
		params.useAmplitude = true;
		params.useBearing = false;
		params.usePeakFreq = true;
		params.useCorrelation = false;

		AffinityTrainer.Result result = AffinityTrainer.trainAndExport(events, params, false,
				AffinityTrainer.DEFAULT_HIDDEN, AffinityTrainer.DEFAULT_EPOCHS, null, null);
		AffinityTrainer.Dataset data = AffinityTrainer.generateExamples(events, params, false);

		// disable peak frequency at inference and check the network stays well-behaved.
		double meanPos = 0;
		double meanNeg = 0;
		int nP = 0;
		int nN = 0;
		boolean allValid = true;
		for (int i = 0; i < data.x.length; i++) {
			double[] row = data.x[i].clone();
			row[6] = UKFTracker.ABSENT; // peak frequency switched off
			double aff = result.network.affinity(row);
			if (Double.isNaN(aff) || aff < 0 || aff > 1) {
				allValid = false;
			}
			if (data.y[i] > 0.5) {
				meanPos += aff;
				nP++;
			} else {
				meanNeg += aff;
				nN++;
			}
		}
		meanPos /= nP;
		meanNeg /= nN;
		assertTrue("Affinities stay valid probabilities with peak frequency disabled", allValid);
		assertTrue("Network still separates via remaining features when peak frequency disabled (pos " + fmt(meanPos)
				+ " > neg " + fmt(meanNeg) + ")", meanPos > meanNeg + 0.1);
	}

	/** A damped sinusoid click waveform at the given frequency (Hz). */
	private static double[] clickWaveform(double frequency, int nSamples) {
		double[] wf = new double[nSamples];
		for (int i = 0; i < nSamples; i++) {
			double env = Math.exp(-3.0 * i / nSamples);
			wf[i] = env * Math.sin(2 * Math.PI * frequency * i / SR);
		}
		return wf;
	}

	/**
	 * Build a click train whose clicks share a waveform shape (with small per-click
	 * amplitude noise), add them to the block (so they carry a parent data block for
	 * peak-frequency / correlation), and return them.
	 */
	private static List<PamDataUnit> makeWaveTrain(SimpleClickDataBlock block, int uidStart, double t0, double ici,
			double amp, double[] waveform, int n, Random rnd) {
		List<PamDataUnit> clicks = new ArrayList<>();
		double t = t0;
		for (int i = 0; i < n; i++) {
			double a = amp + rnd.nextGaussian() * 1.0;
			double[] wf = new double[waveform.length];
			for (int j = 0; j < waveform.length; j++) {
				wf[j] = waveform[j] * (1 + 0.02 * rnd.nextGaussian());
			}
			SimpleClick click = new SimpleClick(uidStart + i, Double.valueOf(t), Double.valueOf(a), null, wf, SR);
			block.addPamData(click);
			clicks.add(click);
			t += ici + rnd.nextGaussian() * 0.003;
		}
		return clicks;
	}

	private static List<PamDataUnit> makeTrain(int uidStart, double t0, double ici, double amp, int n, Random rnd) {
		List<PamDataUnit> clicks = new ArrayList<>();
		double t = t0;
		for (int i = 0; i < n; i++) {
			double a = amp + rnd.nextGaussian() * 1.0;
			clicks.add(new SimpleClick(uidStart + i, t, a, SR));
			t += ici + rnd.nextGaussian() * 0.003;
		}
		return clicks;
	}

	private static String pct(double v) {
		return String.format("%.1f%%", v * 100);
	}

	private static String fmt(double v) {
		return String.format("%.3f", v);
	}

	private static void assertTrue(String message, boolean condition) {
		if (condition) {
			System.out.println("[PASS] " + message);
		} else {
			System.out.println("[FAIL] " + message);
			failures++;
		}
	}

}
