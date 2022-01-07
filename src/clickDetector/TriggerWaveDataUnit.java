package clickDetector;

import PamDetection.RawDataUnit;

public class TriggerWaveDataUnit extends RawDataUnit {
	
	private double longFilterValue;

	public TriggerWaveDataUnit(long timeMilliseconds, int channelBitmap, long startSample, long duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
	}
	
	/**
	 * @return the longFilterValue
	 */
	public double getLongFilterValue() {
		return longFilterValue;
	}

	/**
	 * @param longFilterValue the longFilterValue to set
	 */
	public void setLongFilterValue(double longFilterValue) {
		this.longFilterValue = longFilterValue;
	}


}
