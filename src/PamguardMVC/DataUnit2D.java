package PamguardMVC;

import PamguardMVC.superdet.SuperDetection;

/**
 * Data units that can be plotted on the FX 2D displays, such as FFT data, beam former 
 * output, etc.  
 * @author Doug Gillespie
 *
 * @param <T>
 * @param <U>
 */
abstract public class DataUnit2D<T extends PamDataUnit, U extends SuperDetection> extends PamDataUnit<T, U> {

	public DataUnit2D(DataUnitBaseData basicData) {
		super(basicData);
	}

	public DataUnit2D(long timeMilliseconds, int channelBitmap, long startSample, long durationSamples) {
		super(timeMilliseconds, channelBitmap, startSample, durationSamples);
	}

	public DataUnit2D(long timeMilliseconds) {
		super(timeMilliseconds);
	}
	
	/**
	 * 
	 * @return data for plotting. Should be converted to the same scale as is 
	 * used by the plot axis (usually dB, but might be counts or some other data type)
	 */
	abstract public double[] getMagnitudeData();

}
