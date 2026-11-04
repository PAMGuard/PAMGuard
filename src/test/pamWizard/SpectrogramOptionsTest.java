package test.pamWizard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import PamController.pamWizard.SpectrogramOptions;
import PamController.pamWizard.SpectrogramRealTimeAutoConfig;
import PamController.pamWizard.SpectrogramViewerAutoConfig;
import PamController.pamWizard.configurations.BlankConfigAutoConfig;
import PamController.pamWizard.configurations.ConfigApplyContext;

/**
 * Tests the decimation and FFT choices offered on the wizard's spectrogram page.
 */
public class SpectrogramOptionsTest {

	@Test
	public void defaultsAreValidAndDoNotDecimate() {
		SpectrogramOptions options = new SpectrogramOptions();
		assertFalse(options.isDecimate());
		assertEquals(1024, options.getFftLength());
		assertEquals(512, options.getFftHop());
		assertNull(options.validate(96000));
		assertEquals(96000, options.getFFTSampleRate(96000));
	}

	@Test
	public void decimatedRateMustBeBelowSourceRate() {
		SpectrogramOptions options = new SpectrogramOptions();
		options.setDecimate(true);
		options.setDecimatedSampleRate(0);
		assertNotNull(options.validate(96000));
		options.setDecimatedSampleRate(96000);
		assertNotNull(options.validate(96000));
		options.setDecimatedSampleRate(8000);
		assertNull(options.validate(96000));
		assertEquals(8000, options.getFFTSampleRate(96000));
	}

	@Test
	public void decimatedRateIgnoredWhenNotDecimating() {
		SpectrogramOptions options = new SpectrogramOptions();
		options.setDecimatedSampleRate(500000);
		assertNull(options.validate(96000));
		assertEquals(96000, options.getFFTSampleRate(96000));
	}

	@Test
	public void fftLengthAndHopChecked() {
		SpectrogramOptions options = new SpectrogramOptions();
		options.setFftLength(1000);
		assertNotNull(options.validate(48000), "not a power of two");
		options.setFftLength(256);
		options.setFftHop(512);
		assertNotNull(options.validate(48000), "hop longer than the FFT");
		options.setFftHop(0);
		assertNotNull(options.validate(48000), "zero hop");
		options.setFftHop(64);
		assertNull(options.validate(48000));
	}

	@Test
	public void presetsAndSuggestionBelowSourceRate() {
		List<Float> presets = SpectrogramOptions.getPresetRates(48000);
		assertFalse(presets.isEmpty());
		for (Float rate : presets) {
			assertTrue(rate < 48000);
		}
		assertEquals(96000, SpectrogramOptions.suggestDecimatedRate(384000));
		assertEquals(2000, SpectrogramOptions.suggestDecimatedRate(8000));
		assertTrue(SpectrogramOptions.getFFTLengths().contains(1024));
	}

	@Test
	public void resolutionUsesDecimatedRate() {
		SpectrogramOptions options = new SpectrogramOptions();
		options.setDecimate(true);
		options.setDecimatedSampleRate(2048);
		options.setFftLength(1024);
		options.setFftHop(512);
		String text = options.describeResolution(96000);
		assertTrue(text.contains("2.00 Hz"), text);
		assertTrue(text.contains("250.0 ms"), text);
		assertTrue(text.contains("50% overlap"), text);
	}

	@Test
	public void onlySpectrogramConfigsHaveOptions() {
		assertTrue(new SpectrogramViewerAutoConfig().hasSpectrogramOptions());
		assertTrue(new SpectrogramRealTimeAutoConfig().hasSpectrogramOptions());
		assertFalse(new BlankConfigAutoConfig().hasSpectrogramOptions());
		assertNotNull(new ConfigApplyContext().getSpectrogramOptions());
	}
}
