package Localiser.algorithms;

/**
 * Peak position in a 2D array. 
 * @author Doug Gillespie
 *
 */
public class PeakPosition2D {

	double bin0, bin1;
	double height;
	
	/**
	 * 
	 * @param binX Peak X (first dimension)
	 * @param binY Peak Y (second dimension)
	 * @param height
	 */
	public PeakPosition2D(double bin0, double bin1, double height) {
		super();
		this.bin0 = bin0;
		this.bin1 = bin1;
		this.height = height;
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

	/**
	 * @return the bin0
	 */
	public double getBin0() {
		return bin0;
	}

	/**
	 * @param bin0 the bin0 to set
	 */
	public void setBin0(double bin0) {
		this.bin0 = bin0;
	}

	/**
	 * @return the bin1
	 */
	public double getBin1() {
		return bin1;
	}

	/**
	 * @param bin1 the bin1 to set
	 */
	public void setBin1(double bin1) {
		this.bin1 = bin1;
	}
	
	
}
