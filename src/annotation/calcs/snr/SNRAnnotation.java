package annotation.calcs.snr;

import annotation.DataAnnotation;
import annotation.DataAnnotationType;

public class SNRAnnotation extends DataAnnotation<DataAnnotationType> {

	private double snr;
	
	public SNRAnnotation(DataAnnotationType dataAnnotationType) {
		super(dataAnnotationType);
	}

	/**
	 * @return the snr
	 */
	public double getSnr() {
		return snr;
	}

	/**
	 * @param snr the snr to set
	 */
	public void setSnr(double snr) {
		this.snr = snr;
	}

	@Override
	public String toString() {
		return String.format("%3.1fdB", snr);
	}

	
	

}
