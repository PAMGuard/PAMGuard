package fftManager;

/**
 * Marker interface for an FFTDataUnit subclass whose getSpectrogramData()
 * values are not FFT magnitude, such as bearing in degrees from the Azigram
 * plugin. getMagnitudeData() still returns the true magnitude of each cell.
 * <p>
 * Displays check for this interface to decide whether to fade cells using
 * getAlphaData(). An explicit marker is more reliable than inferring the
 * data type by comparing array values or references.
 *
 * @author brian_mil
 */
public interface NonMagnitudeSpectrogramData {
}
