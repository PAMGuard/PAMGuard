package dbht;

import PamguardMVC.AcousticDataUnit;
import PamguardMVC.PamDataUnit;

public class DbHtDataUnit extends PamDataUnit<PamDataUnit,PamDataUnit> implements AcousticDataUnit {
	
	private double rms, zeroPeak, peakPeak;

	public DbHtDataUnit(long timeMilliseconds, int channelBitmap,
			long startSample, long duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
		// TODO Auto-generated constructor stub
	}

	public double getMeasure(int iMeasure) {
		switch (iMeasure) {
		case 0:
			return rms;
		case 1:
			return zeroPeak;
		case 2:
			return peakPeak;
		default:
			return Double.NaN;
		}
	}
	/**
	 * @return the rms
	 */
	public double getRms() {
		return rms;
	}

	/**
	 * @param rms the rms to set
	 */
	public void setRms(double rms) {
		this.rms = rms;
	}

	/**
	 * @return the zeroPeak
	 */
	public double getZeroPeak() {
		return zeroPeak;
	}

	/**
	 * @param zeroPeak the zeroPeak to set
	 */
	public void setZeroPeak(double zeroPeak) {
		this.zeroPeak = zeroPeak;
	}

	/**
	 * @return the peakPeak
	 */
	public double getPeakPeak() {
		return peakPeak;
	}

	/**
	 * @param peakPeak the peakPeak to set
	 */
	public void setPeakPeak(double peakPeak) {
		this.peakPeak = peakPeak;
	}

}
