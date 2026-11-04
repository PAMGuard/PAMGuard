package annotation.calcs.measurement;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import Spectrogram.WindowFunction;

/**
 * Spectrogram settings shared by all the sound measurements.
 *
 * @author Jamie Macaulay
 */
public class SoundMeasurementParameters implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	/**
	 * FFT length in samples for the spectrogram based measurements.
	 */
	public int fftLength = 512;

	/**
	 * Hop between spectrogram frames in samples.
	 */
	public int fftHop = 256;

	/**
	 * Window function (see {@link WindowFunction}).
	 */
	public int windowType = WindowFunction.HANNING;

	@Override
	public SoundMeasurementParameters clone() {
		try {
			return (SoundMeasurementParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		return PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
	}

}
