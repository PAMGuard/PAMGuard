package clickTrainDetector.clickTrainAlgorithms;

import Localiser.algorithms.Correlations;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import PamguardMVC.RawDataTransforms;
import cpod.CPODClick;

/**
 * Shared helpers for computing per-click and pair-of-click waveform features -
 * peak frequency and waveform cross-correlation - so the MHT, adaptive and UKF
 * click train algorithms all measure them the same way.
 * <p>
 * All methods degrade gracefully: if a data unit carries no waveform (or no data
 * transforms) the peak-frequency / correlation value cannot be computed and
 * {@link Double#NaN} is returned rather than throwing. Callers treat a NaN as
 * "feature unavailable".
 *
 * @author Jamie Macaulay
 */
@SuppressWarnings("rawtypes")
public class ClickFeatureUtils {

	/**
	 * Which waveform channel to use for correlation / spectra. Assumes the
	 * hydrophones of a grouped detection are close together so the waveforms are
	 * essentially the same; matches {@code CorrelationManager}.
	 */
	private static final int WAV_INDEX = 0;

	/**
	 * One {@link Correlations} instance per thread. {@code Correlations} is not
	 * thread safe (it holds reusable FFT scratch buffers), and training runs on a
	 * different thread from live detection, so give each thread its own.
	 */
	private static final ThreadLocal<Correlations> CORRELATIONS = ThreadLocal.withInitial(Correlations::new);

	private ClickFeatureUtils() {
	}

	/**
	 * The peak frequency of a click in Hz.
	 * <p>
	 * For a {@link CPODClick} the reported peak frequency ({@code getkHz}) is used;
	 * for any other {@link RawDataHolder} the peak of the total power spectrum is
	 * used. CPOD is checked first because a CPOD click is also a
	 * {@link RawDataHolder} but its stored peak frequency is more reliable than the
	 * spectrum of its synthesised waveform.
	 *
	 * @param dataUnit - the click.
	 * @return the peak frequency in Hz, or {@link Double#NaN} if it cannot be
	 *         measured.
	 */
	public static double getPeakFrequency(PamDataUnit dataUnit) {
		if (dataUnit instanceof CPODClick) {
			return 1000.0 * ((CPODClick) dataUnit).getkHz();
		}
		if (dataUnit instanceof RawDataHolder) {
			if (dataUnit.getParentDataBlock() == null) {
				return Double.NaN;
			}
			RawDataHolder holder = (RawDataHolder) dataUnit;
			RawDataTransforms transforms = holder.getDataTransforms();
			int fftLength;
			if (transforms != null) {
				fftLength = transforms.getShortestFFTLength();
			} else {
				// some data units (or test stubs) do not cache a transforms object; build one
				// on demand from the raw waveform so peak frequency can still be measured.
				// Derive the FFT length from the waveform directly (getShortestFFTLength
				// needs a sample duration these units may not carry).
				double[][] waveData = holder.getWaveData();
				if (waveData == null || waveData.length == 0 || waveData[0] == null || waveData[0].length == 0) {
					return Double.NaN;
				}
				transforms = new RawDataTransforms(dataUnit);
				fftLength = PamUtils.getMinFftLength(waveData[0].length);
			}
			double[] powerSpectrum = transforms.getTotalPowerSpectrum(fftLength);
			if (powerSpectrum == null || powerSpectrum.length == 0) {
				return Double.NaN;
			}
			int maxIndex = PamUtils.getMaxIndex(powerSpectrum);
			return (maxIndex / (double) powerSpectrum.length) * dataUnit.getParentDataBlock().getSampleRate() / 2;
		}
		return Double.NaN;
	}

	/**
	 * The maximum normalised waveform cross-correlation (0-1) between two clicks. A
	 * value near 1 means the two clicks have very similar waveforms.
	 *
	 * @param dataUnit1 - the first (earlier) click.
	 * @param dataUnit2 - the second (later) click.
	 * @return the peak correlation in [0, 1], or {@link Double#NaN} if either click
	 *         has no waveform.
	 */
	public static double waveformCorrelation(PamDataUnit dataUnit1, PamDataUnit dataUnit2) {
		double[] wave1 = waveform(dataUnit1);
		double[] wave2 = waveform(dataUnit2);
		if (wave1 == null || wave2 == null || wave1.length == 0 || wave2.length == 0) {
			return Double.NaN;
		}
		Correlations correlations = CORRELATIONS.get();
		double[] corrFunction = correlations.getCorrelation(wave1, wave2, true);
		double[][] interpolatedMaxima = correlations.getInterpolatedMaxima(corrFunction);
		double maxValue = 0.0;
		if (interpolatedMaxima != null && interpolatedMaxima[0].length > 0) {
			for (int j = 0; j < interpolatedMaxima[0].length; j++) {
				if (interpolatedMaxima[1][j] > maxValue) {
					maxValue = interpolatedMaxima[1][j];
				}
			}
		}
		return maxValue;
	}

	/**
	 * Get the waveform of a click (channel {@link #WAV_INDEX}), or null if it has
	 * none.
	 */
	private static double[] waveform(PamDataUnit dataUnit) {
		if (!(dataUnit instanceof RawDataHolder)) {
			return null;
		}
		double[][] waveData = ((RawDataHolder) dataUnit).getWaveData();
		if (waveData == null || waveData.length <= WAV_INDEX || waveData[WAV_INDEX] == null) {
			return null;
		}
		return waveData[WAV_INDEX];
	}

}
