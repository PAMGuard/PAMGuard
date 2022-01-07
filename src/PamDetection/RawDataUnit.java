package PamDetection;

import PamguardMVC.AcousticDataUnit;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

/**
 * Holds raw data from a sound file. 
 * 
 * @author Doug Gillespie. 
 *
 */
public class RawDataUnit extends PamDataUnit<PamDataUnit,SuperDetection> implements AcousticDataUnit {

	double[] rawData = null;
	
	/**
	 * The file samples are the total number
	 * of samples into an audio file the raw data unit starts at. It
	 * is not necessarily the same as start sample depending on how the file
	 * has been loaded. 
	 */
	private long fileSamples = -1; 

	public RawDataUnit(long timeMilliseconds, int channelBitmap, long startSample, long duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
	}

	/**
	 * @return Returns the rawData.
	 */
	public double[] getRawData() {
		return rawData;
	}

	/**
	 * @param rawData The rawData to set.
	 */
	public void setRawData(double[] rawData) {
		setRawData(rawData, false);
	}

	/**
	 * @param rawData The rawData to set.
	 */
	public void setRawData(double[] rawData, boolean setAmplitude) {
		this.rawData = rawData;
		setSampleDuration((long) rawData.length);
		if (setAmplitude) {
			double maxValue = 0.;
			for (int i = 0; i < rawData.length; i++) {
				maxValue = Math.max(maxValue, Math.abs(rawData[i]));
			}
			setMeasuredAmpAndType(maxValue, DataUnitBaseData.AMPLITUDE_SCALE_LINREFSD);
		}
	}

	/**
	 * Set the file samples. The file samples are the total number
	 * of samples into an audio file the raw data unit starts at. It
	 * is not necessarily the same as start sample depending on how the file
	 * has been loaded. 
	 * @param fileSamples - the numeber of samples (not bytes) into the audio file. 
	 */
	public void setFileSamples(long fileSamples) {
		this.fileSamples= fileSamples;
	}
	
	/**
	 * Get the file samples. The file samples are the total number
	 * of samples into an audio file the raw data unit starts at. It
	 * is not necessarily the same as start sample depending on how the file
	 * has been loaded. 
	 */
	public long getFileSamples() {
		return fileSamples;
	}
	
}
