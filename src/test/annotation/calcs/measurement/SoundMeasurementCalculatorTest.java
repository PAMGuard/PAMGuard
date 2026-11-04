package test.annotation.calcs.measurement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Random;

import org.junit.jupiter.api.Test;

import Spectrogram.WindowFunction;
import annotation.calcs.measurement.SoundMeasurement;
import annotation.calcs.measurement.SoundMeasurementAnnotationType;
import annotation.calcs.measurement.SoundMeasurementCalculator;
import annotation.calcs.measurement.SoundMeasurementGroup;
import annotation.calcs.measurement.SoundMeasurementParameters;

/**
 * Tests for the sound measurements made on spectrogram annotations.
 */
public class SoundMeasurementCalculatorTest {

	private static final double FS = 8000;

	private static double[] tone(double freq, double amplitude, int n) {
		double[] x = new double[n];
		for (int i = 0; i < n; i++) {
			x[i] = amplitude * Math.sin(2 * Math.PI * freq * i / FS);
		}
		return x;
	}

	private static SoundMeasurementCalculator calculator() {
		return new SoundMeasurementCalculator(512, 256, WindowFunction.HANNING);
	}

	@Test
	public void toneFrequencyMeasurements() {
		double[] x = tone(1000, 0.5, 8000);
		EnumMap<SoundMeasurement, Double> m = calculator().calculate(x, FS, 500, 2000);
		double binWidth = FS / 512;
		assertEquals(1000, m.get(SoundMeasurement.PEAK_FREQUENCY), binWidth);
		assertEquals(1000, m.get(SoundMeasurement.CENTER_FREQUENCY), binWidth);
		// all the energy is in a couple of bins, so the bandwidths are tiny and entropy low.
		assertTrue(m.get(SoundMeasurement.BANDWIDTH_90) <= 2 * binWidth);
		assertTrue(m.get(SoundMeasurement.AGGREGATE_ENTROPY) < 2);
		assertEquals(1500, m.get(SoundMeasurement.DELTA_FREQUENCY), 1e-9);
		assertEquals(1.0, m.get(SoundMeasurement.DELTA_TIME), 1e-9);
		assertEquals(1 + (8000 - 512) / 256, m.get(SoundMeasurement.LENGTH).intValue());
	}

	@Test
	public void toneWaveformMeasurements() {
		double[] x = tone(1000, 0.5, 8000);
		EnumMap<SoundMeasurement, Double> m = calculator().calculate(x, FS, 500, 2000);
		assertEquals(0.5 / Math.sqrt(2), m.get(SoundMeasurement.RMS_AMPLITUDE), 1e-3);
		assertEquals(0.5, m.get(SoundMeasurement.PEAK_AMPLITUDE), 1e-3);
		assertEquals(0.5, m.get(SoundMeasurement.MAX_AMPLITUDE), 1e-3);
		assertEquals(-0.5, m.get(SoundMeasurement.MIN_AMPLITUDE), 1e-3);
		// the tone is in the band, so filtering doesn't change the RMS ...
		assertEquals(0.5 / Math.sqrt(2), m.get(SoundMeasurement.FILTERED_RMS_AMPLITUDE), 0.01);
		// ... but a band away from the tone removes nearly all of it.
		EnumMap<SoundMeasurement, Double> m2 = calculator().calculate(x, FS, 2500, 3500);
		assertTrue(m2.get(SoundMeasurement.FILTERED_RMS_AMPLITUDE) < 0.05);
	}

	@Test
	public void burstTimeMeasurements() {
		// silence for the first half second, then a tone for the second half.
		double[] x = new double[8000];
		double[] t = tone(1000, 0.5, 4000);
		System.arraycopy(t, 0, x, 4000, 4000);
		EnumMap<SoundMeasurement, Double> m = calculator().calculate(x, FS, 500, 2000);
		double frame = 256 / FS;
		assertEquals(0.75, m.get(SoundMeasurement.CENTER_TIME), 2 * frame);
		assertTrue(m.get(SoundMeasurement.TIME_5) >= 0.5 - 2 * frame);
		assertEquals(0.5, m.get(SoundMeasurement.DURATION_90), 4 * frame);
		assertTrue(m.get(SoundMeasurement.PEAK_POWER_TIME) >= 0.5 - frame);
		assertTrue(m.get(SoundMeasurement.MAX_TIME) >= 0.5);
	}

	@Test
	public void noiseHasHighEntropy() {
		Random r = new Random(42);
		double[] x = new double[8000];
		for (int i = 0; i < x.length; i++) {
			x[i] = r.nextGaussian() * 0.1;
		}
		EnumMap<SoundMeasurement, Double> m = calculator().calculate(x, FS, 0, FS / 2);
		int nBins = 512 / 2 + 1;
		double maxEntropy = Math.log(nBins) / Math.log(2);
		// white noise is spread evenly over frequency so is close to maximum entropy.
		assertTrue(m.get(SoundMeasurement.AGGREGATE_ENTROPY) > 0.95 * maxEntropy);
		assertEquals(FS / 4, m.get(SoundMeasurement.CENTER_FREQUENCY), FS / 20);
	}

	@Test
	public void shortClipStillMeasured() {
		// shorter than one FFT - should fall back to a smaller FFT.
		double[] x = tone(1000, 0.5, 100);
		EnumMap<SoundMeasurement, Double> m = calculator().calculate(x, FS, 500, 2000);
		assertNotNull(m);
		// 64 point FFT with a 32 sample hop fits twice into 100 samples.
		assertEquals(2, m.get(SoundMeasurement.LENGTH).intValue());
		assertEquals(1000, m.get(SoundMeasurement.PEAK_FREQUENCY), FS / 64);
	}

	@Test
	public void noData() {
		assertNull(calculator().calculate(new double[0], FS, 0, 1000));
		assertNull(calculator().calculate(null, FS, 0, 1000));
	}

	@Test
	public void columnNamesUnique() {
		HashSet<String> names = new HashSet<>();
		for (SoundMeasurement m : SoundMeasurement.values()) {
			assertTrue(names.add(m.getColumnName().toLowerCase()), "Duplicate column " + m.getColumnName());
			assertTrue(m.getColumnName().matches("[A-Za-z0-9]+"));
		}
	}

	@Test
	public void oneUniqueTypePerMeasurement() {
		SoundMeasurementGroup group = new SoundMeasurementGroup(null);
		assertEquals(SoundMeasurement.values().length, group.getAnnotationTypes().size());
		HashSet<String> names = new HashSet<>();
		HashSet<String> codes = new HashSet<>();
		for (SoundMeasurementAnnotationType type : group.getAnnotationTypes()) {
			assertTrue(names.add(type.getAnnotationName()), "Duplicate name " + type.getAnnotationName());
			assertTrue(codes.add(type.getShortIdCode()), "Duplicate code " + type.getShortIdCode());
			assertEquals(4, type.getShortIdCode().length());
		}
	}

	@Test
	public void parametersShared() {
		SoundMeasurementGroup group = new SoundMeasurementGroup(null);
		SoundMeasurementParameters params = new SoundMeasurementParameters();
		params.fftLength = 1024;
		// setting the options of one measurement changes the settings for them all.
		SoundMeasurementAnnotationType first = group.getAnnotationTypes().get(0);
		first.getMeasurementOptions().setParameters(params);
		first.setAnnotationOptions(first.getMeasurementOptions());
		for (SoundMeasurementAnnotationType type : group.getAnnotationTypes()) {
			assertEquals(1024, type.getMeasurementOptions().getParameters().fftLength);
		}
		assertEquals(1024, group.getParameters().fftLength);
	}
}
