package annotation.calcs.spl;

import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import noiseOneBand.OneBandDataUnit;

public class SPLAnnotation extends DataAnnotation<DataAnnotationType> {

//	private OneBandDataUnit spl;

	private double rms, zeroPeak, peakPeak;
	private double sel;
	
	public SPLAnnotation(DataAnnotationType dataAnnotationType) {
		super(dataAnnotationType);
	}

	@Override
	public String toString() {
		return String.format("<html><table border=\"0\" cellpadding=\"0\" cellspacing=\"2\" width=\"100%%\">" +
							"<tr><td>RMS         </td><td align='right'>%3.1f dB</td></tr>" + 
							"<tr><td>Zero-to-Peak</td><td align='right'>%3.1f dB</td></tr>" + 
							"<tr><td>Peak-to-Peak</td><td align='right'>%3.1f dB</td></tr>" + 
							"<tr><td>SEL         </td><td align='right'>%3.1f dB</td></tr>" + 
							"</table></html>", rms, zeroPeak, peakPeak, sel);
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
				return sel;
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
	 * @param seconds integration time in seconds. 
	 */
	public void setSEL(double sel) {
		this.sel = sel;
	}

	/**
	 * @return the integratedSEL
	 */
	public double getIntegratedSEL() {
		return sel;
	}


	
	

}
