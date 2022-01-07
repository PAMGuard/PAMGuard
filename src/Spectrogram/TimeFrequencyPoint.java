package Spectrogram;

import PamUtils.PamCoordinate;

public class TimeFrequencyPoint implements PamCoordinate {
	
	private long timeMilliseconds; 
	
	private double frequency;
	
	/**
	 * @param timeMilliseconds
	 * @param frequency
	 */
	public TimeFrequencyPoint(long timeMilliseconds, double frequency) {
		super();
		this.timeMilliseconds = timeMilliseconds;
		this.frequency = frequency;
	}

	@Override
	public double getCoordinate(int iCoordinate) {
		switch (iCoordinate) {
		case 0:
			return timeMilliseconds;
		case 1:
			return frequency;
		}
		return 0;
	}

	@Override
	public void setCoordinate(int iCoordinate, double value) {
		switch (iCoordinate) {
		case 0:
			timeMilliseconds = (long) value;
		case 1:
			frequency = value;
		}
	}

	@Override
	public int getNumCoordinates() {
		return 2;
	}

	/**
	 * @return the timeMilliseconds
	 */
	public long getTimeMilliseconds() {
		return timeMilliseconds;
	}

	/**
	 * @param timeMilliseconds the timeMilliseconds to set
	 */
	public void setTimeMilliseconds(long timeMilliseconds) {
		this.timeMilliseconds = timeMilliseconds;
	}

	/**
	 * @return the frequency
	 */
	public double getFrequency() {
		return frequency;
	}

	/**
	 * @param frequency the frequency to set
	 */
	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}

}
