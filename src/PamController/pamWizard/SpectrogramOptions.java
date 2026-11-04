package PamController.pamWizard;

import java.util.ArrayList;
import java.util.List;

import fftManager.FFTParameters;

/**
 * The spectrogram choices made in the import wizard: whether to decimate the
 * sound data before the FFT, and the FFT length and hop.
 * <p>
 * Decimating first is worthwhile for long, high sample rate recordings of low
 * frequency sounds - a 1 kHz baleen whale call is lost in the bottom few pixels
 * of a 192 kHz spectrogram, and the FFT has to do far more work than is needed.
 * With decimation on, a Decimator module is put between Sound Acquisition and the
 * FFT, so both the FFT and the spectrogram display run on the decimated data.
 *
 * @author Jamie Macaulay
 */
public class SpectrogramOptions {

	/** Smallest FFT length offered. */
	public static final int MIN_FFT_LENGTH = 16;

	/** Largest FFT length offered. */
	public static final int MAX_FFT_LENGTH = 65536;

	/**
	 * Sample rates offered for decimation. Only those below the rate of the imported
	 * files are shown, but any other value can be typed in.
	 */
	private static final float[] PRESET_RATES = { 500, 1000, 2000, 4000, 8000, 16000, 24000, 32000, 48000,
			96000, 192000, 256000, 384000 };

	private boolean decimate = false;

	/** Sample rate to decimate to, in Hz. Zero means not yet chosen. */
	private float decimatedSampleRate = 0;

	private int fftLength = 1024;

	private int fftHop = 512;

	/**
	 * @return true if a decimator should be put in front of the FFT.
	 */
	public boolean isDecimate() {
		return decimate;
	}

	public void setDecimate(boolean decimate) {
		this.decimate = decimate;
	}

	/**
	 * The sample rate to decimate to. Only used if {@link #isDecimate()}.
	 *
	 * @return the decimated sample rate in Hz, or zero if none has been chosen.
	 */
	public float getDecimatedSampleRate() {
		return decimatedSampleRate;
	}

	public void setDecimatedSampleRate(float decimatedSampleRate) {
		this.decimatedSampleRate = decimatedSampleRate;
	}

	/**
	 * @return the FFT length in samples.
	 */
	public int getFftLength() {
		return fftLength;
	}

	public void setFftLength(int fftLength) {
		this.fftLength = fftLength;
	}

	/**
	 * @return the FFT hop in samples.
	 */
	public int getFftHop() {
		return fftHop;
	}

	public void setFftHop(int fftHop) {
		this.fftHop = fftHop;
	}

	/**
	 * The sample rate the FFT will run at.
	 *
	 * @param sourceSampleRate sample rate of the imported files in Hz.
	 * @return the decimated rate if decimating, otherwise the source rate.
	 */
	public float getFFTSampleRate(float sourceSampleRate) {
		return (decimate && decimatedSampleRate > 0) ? decimatedSampleRate : sourceSampleRate;
	}

	/**
	 * A sensible decimated sample rate to start the user off with: the largest
	 * preset rate no more than a quarter of the source rate.
	 *
	 * @param sourceSampleRate sample rate of the imported files in Hz, or zero if
	 *                         unknown.
	 * @return a suggested rate in Hz.
	 */
	public static float suggestDecimatedRate(float sourceSampleRate) {
		if (sourceSampleRate <= 0) {
			return 2000;
		}
		float suggested = PRESET_RATES[0];
		for (float rate : PRESET_RATES) {
			if (rate <= sourceSampleRate / 4) {
				suggested = rate;
			}
		}
		return suggested;
	}

	/**
	 * The preset decimated sample rates which are below the source rate.
	 *
	 * @param sourceSampleRate sample rate of the imported files in Hz, or zero if
	 *                         unknown, in which case all presets are returned.
	 * @return the preset rates in Hz, lowest first.
	 */
	public static List<Float> getPresetRates(float sourceSampleRate) {
		List<Float> rates = new ArrayList<>();
		for (float rate : PRESET_RATES) {
			if (sourceSampleRate <= 0 || rate < sourceSampleRate) {
				rates.add(rate);
			}
		}
		return rates;
	}

	/**
	 * Every FFT length on offer - the powers of two from {@value #MIN_FFT_LENGTH}
	 * to {@value #MAX_FFT_LENGTH}.
	 *
	 * @return the FFT lengths, shortest first.
	 */
	public static List<Integer> getFFTLengths() {
		List<Integer> lengths = new ArrayList<>();
		for (int len = MIN_FFT_LENGTH; len <= MAX_FFT_LENGTH; len *= 2) {
			lengths.add(len);
		}
		return lengths;
	}

	/**
	 * Check the options make sense for the imported data.
	 *
	 * @param sourceSampleRate sample rate of the imported files in Hz, or zero if
	 *                         unknown.
	 * @return a message saying what is wrong, or null if the options are fine.
	 */
	public String validate(float sourceSampleRate) {
		if (decimate) {
			if (decimatedSampleRate <= 0) {
				return "Please enter a sample rate to decimate the data to.";
			}
			if (sourceSampleRate > 0 && decimatedSampleRate >= sourceSampleRate) {
				return String.format("The decimated sample rate must be lower than the sample rate of the "
						+ "imported files (%s).", SoundFileSummary.formatRate(sourceSampleRate));
			}
		}
		if (!FFTParameters.isValidLength(fftLength) || fftLength < MIN_FFT_LENGTH || fftLength > MAX_FFT_LENGTH) {
			return String.format("The FFT length must be a power of two between %d and %d.", MIN_FFT_LENGTH,
					MAX_FFT_LENGTH);
		}
		if (fftHop <= 0 || fftHop > fftLength) {
			return "The FFT hop must be at least 1 and no more than the FFT length.";
		}
		return null;
	}

	/**
	 * The frequency and time resolution these options give, for showing next to the
	 * controls.
	 *
	 * @param sourceSampleRate sample rate of the imported files in Hz, or zero if
	 *                         unknown.
	 * @return a one line description, or an empty string if it can't be worked out.
	 */
	public String describeResolution(float sourceSampleRate) {
		float fs = getFFTSampleRate(sourceSampleRate);
		if (fs <= 0 || fftLength <= 0 || fftHop <= 0) {
			return "";
		}
		double overlap = 100. * (fftLength - fftHop) / fftLength;
		return String.format("Frequency resolution %s, time step %s (%.0f%% overlap), maximum frequency %s",
				formatHz(fs / fftLength), formatSeconds(fftHop / fs), Math.max(0, overlap),
				SoundFileSummary.formatRate(fs / 2));
	}

	/**
	 * Describe in plain words what these options will build, for the wizard summary.
	 *
	 * @param sourceSampleRate sample rate of the imported files in Hz, or zero if
	 *                         unknown.
	 * @return the description, one sentence per line.
	 */
	public String describe(float sourceSampleRate) {
		StringBuilder text = new StringBuilder();
		if (decimate) {
			text.append("A decimator will reduce the sample rate");
			if (sourceSampleRate > 0) {
				text.append(" from ").append(SoundFileSummary.formatRate(sourceSampleRate));
			}
			text.append(" to ").append(SoundFileSummary.formatRate(decimatedSampleRate))
					.append(" before the FFT.\n");
		}
		text.append(String.format("The FFT will use a length of %d samples and a hop of %d samples.\n",
				fftLength, fftHop));
		String resolution = describeResolution(sourceSampleRate);
		if (!resolution.isEmpty()) {
			text.append(resolution).append(".\n");
		}
		return text.toString();
	}

	private static String formatHz(double hz) {
		return hz < 10 ? String.format("%.2f Hz", hz) : String.format("%.1f Hz", hz);
	}

	private static String formatSeconds(double seconds) {
		return seconds < 1 ? String.format("%.1f ms", seconds * 1000) : String.format("%.2f s", seconds);
	}
}
