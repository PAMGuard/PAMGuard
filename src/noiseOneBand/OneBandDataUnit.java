package noiseOneBand;

import PamguardMVC.AcousticDataUnit;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

public class OneBandDataUnit extends PamDataUnit<PamDataUnit,SuperDetection> implements AcousticDataUnit {
	
	private double rms, zeroPeak, peakPeak;
	private Double integratedSEL;
	private int selIntegationTime;

	public OneBandDataUnit(long timeMilliseconds, int channelBitmap,
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
		case 3:
			if (integratedSEL != null) {
				return integratedSEL;
			}
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

	/**
	 * Set sound exposure level measured over many data units. 
	 * @param sel sound exposure level
	 * @param selSeconds integration time in seconds. 
	 */
	public void setSEL(double sel, int selSeconds) {
		integratedSEL = sel;
		this.selIntegationTime = selSeconds;
	}

	/**
	 * @return the integratedSEL
	 */
	public Double getIntegratedSEL() {
		return integratedSEL;
	}

	/**
	 * @return the selIntegationTime
	 */
	public int getSelIntegationTime() {
		return selIntegationTime;
	}

}
