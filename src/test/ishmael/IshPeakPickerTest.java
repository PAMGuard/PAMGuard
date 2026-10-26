package test.ishmael;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import IshmaelDetector.IshPeakPicker;
import IshmaelDetector.IshPeakPicker.Event;

/**
 * Tests for the Ishmael peak picker on synthetic detection functions with known answers.
 * <p>
 * Two rates, as the three Ishmael detectors produce them, both from 250 Hz audio:
 * <ul>
 * <li>spectrogram (spectrogram correlation, energy sum): one value per FFT slice, hop 16,
 * so 15.625 values per second, each 16 audio samples after the last</li>
 * <li>matched filter: one value per audio sample</li>
 * </ul>
 * A synthetic detection function is a list of runs, each some number of values above
 * (2.0) or below (0.0) the threshold (1.0). A detection's duration runs from its first to
 * its last value over threshold, in audio samples, plus one: (k - 1) * step + 1 for k
 * values over threshold.
 * <p>
 * Includes the two unit errors found since PAMGuard 2.02.15: the refractory time counted
 * in audio samples instead of slices (fixed September 2025), and the minimum time counted
 * in slices instead of audio samples (fixed here). No PAMGuard runtime is needed.
 */
public class IshPeakPickerTest {

	private static final double FS = 250, HOP = 16;
	private static final double THRESH = 1, HIGH = 2, LOW = 0;

	/** A detection function: alternating runs of values over and under threshold. */
	private static List<Double> function(int... runs) {
		List<Double> f = new ArrayList<>();
		boolean over = true;
		for (int n : runs) {
			for (int i = 0; i < n; i++) {
				f.add(over ? HIGH : LOW);
			}
			over = !over;
		}
		return f;
	}

	/** Leading quiet, then runs, then enough quiet to end any detection. */
	private static List<Double> withQuiet(int lead, int... runs) {
		List<Double> f = new ArrayList<>();
		for (int i = 0; i < lead; i++) {
			f.add(LOW);
		}
		f.addAll(function(runs));
		for (int i = 0; i < 10_000; i++) {
			f.add(LOW);
		}
		return f;
	}

	private static List<Event> run(IshPeakPicker p, List<Double> f, long step) {
		List<Event> events = new ArrayList<>();
		for (int i = 0; i < f.size(); i++) {
			Event e = p.add(i * step, f.get(i));
			if (e != null) {
				events.add(e);
			}
		}
		return events;
	}

	private static IshPeakPicker spectrogram(double minTime, double maxTime, double refractory) {
		return new IshPeakPicker(THRESH, minTime, maxTime, refractory, FS, FS / HOP);
	}

	private static IshPeakPicker matchedFilter(double minTime, double maxTime, double refractory) {
		return new IshPeakPicker(THRESH, minTime, maxTime, refractory, FS, FS);
	}

	@Test
	void oneCallGivesOneDetectionWithItsStartAndDuration() {
		List<Event> e = run(spectrogram(0, 0, 1), withQuiet(100, 20), (long) HOP);
		assertEquals(1, e.size());
		assertEquals(100 * 16, e.get(0).startSample);
		assertEquals(19 * 16 + 1, e.get(0).durationSamples);
	}

	@Test
	void peakIsTheHighestValueAndItsSample() {
		List<Double> f = withQuiet(10, 5);
		f.set(12, 7.0);
		List<Event> e = run(spectrogram(0, 0, 1), f, (long) HOP);
		assertEquals(1, e.size());
		assertEquals(7.0, e.get(0).peakHeight);
		assertEquals(12 * 16, e.get(0).peakSample);
	}

	@Test
	void valueEqualToThresholdIsNotOver() {
		List<Double> f = withQuiet(10, 5);
		for (int i = 10; i < 15; i++) {
			f.set(i, THRESH);
		}
		assertTrue(run(spectrogram(0, 0, 1), f, (long) HOP).isEmpty());
	}

	/**
	 * Minimum time 3 s is 750 audio samples. 47 slices span 46 * 16 + 1 = 737 samples, under;
	 * 48 span 753, over. Before the fix, 3 s was 46 slices compared with samples, so four
	 * slices (49 samples, 0.2 s) were enough.
	 */
	@Test
	void minimumTimeIsInSecondsAtTheSpectrogramRate() {
		IshPeakPicker p = spectrogram(3, 0, 1);
		assertEquals(750, p.getMinTimeSamples());
		assertTrue(run(spectrogram(3, 0, 1), withQuiet(10, 4), (long) HOP).isEmpty(), "0.2 s call accepted");
		assertTrue(run(spectrogram(3, 0, 1), withQuiet(10, 47), (long) HOP).isEmpty(), "2.9 s call accepted");
		assertEquals(1, run(spectrogram(3, 0, 1), withQuiet(10, 48), (long) HOP).size(), "3.0 s call rejected");
	}

	/** Fin whale settings: 0.5 s is 125 samples; 8 slices span 113, 9 span 129. */
	@Test
	void finMinimumTimeOfHalfASecond() {
		assertTrue(run(spectrogram(0.5, 0, 1), withQuiet(10, 8), (long) HOP).isEmpty());
		assertEquals(1, run(spectrogram(0.5, 0, 1), withQuiet(10, 9), (long) HOP).size());
	}

	@Test
	void minimumTimeAtTheMatchedFilterRate() {
		// 0.5 s is 125 samples: 125 values span 125 samples, 124 span 124
		assertTrue(run(matchedFilter(0.5, 0, 0.1), withQuiet(10, 124), 1).isEmpty());
		assertEquals(1, run(matchedFilter(0.5, 0, 0.1), withQuiet(10, 125), 1).size());
	}

	/** Maximum time 1 s is 15 slices over threshold (15.625 per second). */
	/**
	 * Settings that reproduce 2.02.15 to 2.02.19, where the minimum time was effectively
	 * divided by the FFT hop, at hop 16: fin 0.5 s accepted calls of 2 slices or more, song
	 * 3 s calls of 4 slices or more. 0.06 s and 0.19 s give the same here, so results from
	 * those versions can be reproduced with a fixed PAMGuard.
	 */
	@Test
	void settingsThatReproduceTheOldMinimumTime() {
		assertTrue(run(spectrogram(0.06, 0, 1), withQuiet(10, 1), (long) HOP).isEmpty());
		assertEquals(1, run(spectrogram(0.06, 0, 1), withQuiet(10, 2), (long) HOP).size());
		assertTrue(run(spectrogram(0.19, 0, 1), withQuiet(10, 3), (long) HOP).isEmpty());
		assertEquals(1, run(spectrogram(0.19, 0, 1), withQuiet(10, 4), (long) HOP).size());
	}

	@Test
	void maximumTimeCountsSlicesOverThreshold() {
		assertEquals(1, run(spectrogram(0, 1, 1), withQuiet(10, 15), (long) HOP).size());
		assertTrue(run(spectrogram(0, 1, 1), withQuiet(10, 16), (long) HOP).isEmpty());
	}

	/**
	 * Refractory time 1 s is 15 slices. A dip of 14 slices under threshold leaves one call;
	 * a dip of 15 ends it, and the next run starts a second detection.
	 */
	@Test
	void dipsShorterThanTheRefractoryTimeDoNotSplitACall() {
		IshPeakPicker p = spectrogram(0, 0, 1);
		assertEquals(15, p.getRefractorySteps());
		List<Event> merged = run(spectrogram(0, 0, 1), withQuiet(10, 10, 14, 10), (long) HOP);
		assertEquals(1, merged.size());
		assertEquals(10 * 16, merged.get(0).startSample);
		assertEquals((10 + 14 + 10 - 1) * 16 + 1, merged.get(0).durationSamples);
		List<Event> split = run(spectrogram(0, 0, 1), withQuiet(10, 10, 15, 10), (long) HOP);
		assertEquals(2, split.size());
		assertEquals((10 + 10 + 15) * 16, split.get(1).startSample);
	}

	/**
	 * The case in the September 2025 report: calls closer together than the refractory
	 * time. With 0 s every call is a detection; with 1 s, calls under 1 s apart merge. In
	 * 2.02.15 to 2.02.17 the refractory time was counted in audio samples, so 1 s merged
	 * everything within 16 s.
	 */
	@Test
	void refractoryTimeMergesOnlyCallsCloserThanIt() {
		// five 0.5 s calls (8 slices), with gaps of 0.5, 2, 0.5 and 3 s (8, 31, 8, 47 slices)
		List<Double> f = withQuiet(10, 8, 8, 8, 31, 8, 8, 8, 47, 8);
		assertEquals(5, run(spectrogram(0, 0, 0), f, (long) HOP).size());
		assertEquals(3, run(spectrogram(0, 0, 1), f, (long) HOP).size());
	}

	@Test
	void aCallIsReportedOnlyAfterTheRefractoryTimeBelowThreshold() {
		IshPeakPicker p = spectrogram(0, 0, 1);
		long s = 0;
		for (int i = 0; i < 10; i++) {
			assertNull(p.add(s, HIGH));
			s += HOP;
		}
		for (int i = 0; i < 14; i++) {
			assertNull(p.add(s, LOW), "reported before the refractory time");
			s += HOP;
		}
		assertTrue(p.add(s, LOW) != null, "not reported at the refractory time");
	}

	@Test
	void changingSettingsKeepsACallInProgress() {
		IshPeakPicker p = spectrogram(0, 0, 1);
		long s = 0;
		for (int i = 0; i < 10; i++) {
			p.add(s, HIGH);
			s += HOP;
		}
		p.setParams(THRESH, 0, 0, 1, FS, FS / HOP);
		Event e = null;
		for (int i = 0; i < 20 && e == null; i++) {
			e = p.add(s, LOW);
			s += HOP;
		}
		assertTrue(e != null);
		assertEquals(0, e.startSample);
	}

	@Test
	void resetForgetsACallInProgress() {
		IshPeakPicker p = spectrogram(0, 0, 1);
		for (int i = 0; i < 10; i++) {
			p.add(i * 16, HIGH);
		}
		p.reset();
		for (int i = 10; i < 40; i++) {
			assertNull(p.add(i * 16, LOW));
		}
	}

	/**
	 * Same calls in seconds give the same detections at both rates, apart from the
	 * granularity of the spectrogram rate.
	 */
	@Test
	void bothRatesAgreeInSeconds() {
		// 2 s calls separated by 3 s, at each rate
		List<Event> spec = run(spectrogram(1, 0, 1), withQuiet(16, 32, 47, 32), (long) HOP);
		List<Event> mf = run(matchedFilter(1, 0, 1), withQuiet(256, 512, 752, 512), 1);
		assertEquals(2, spec.size());
		assertEquals(2, mf.size());
		for (int i = 0; i < 2; i++) {
			assertEquals((double) mf.get(i).startSample, (double) spec.get(i).startSample, 16.0);
			assertEquals((double) mf.get(i).durationSamples, (double) spec.get(i).durationSamples, 16.0);
		}
	}
}
