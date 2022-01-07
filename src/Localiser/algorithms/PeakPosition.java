package Localiser.algorithms;

/**
 * Information about  a peak found by PeakSearch algorithms.
 * @author Doug Gillespie
 *
 */
public class PeakPosition {

	double bin;
	
	double height;

	public PeakPosition(double bin, double height) {
		super();
		this.bin = bin;
		this.height = height;
	}

	/**
	 * @return the bin
	 */
	public double getBin() {
		return bin;
	}

	/**
	 * @param bin the bin to set
	 */
	public void setBin(double bin) {
		this.bin = bin;
	}

	/**
	 * @return the height
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(double height) {
		this.height = height;
	}
}
