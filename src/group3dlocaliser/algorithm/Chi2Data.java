package group3dlocaliser.algorithm;

public class Chi2Data implements FitTestValue {

	private double chi2;
	private int ndf;

	public Chi2Data(double chi2, int ndf) {
		this.chi2 = chi2;
		this.ndf = ndf;
	}

	/**
	 * @return the chi2
	 */
	public double getChi2() {
		return chi2;
	}

	/**
	 * @param chi2 the chi2 to set
	 */
	public void setChi2(double chi2) {
		this.chi2 = chi2;
	}

	@Override
	public int getDegreesOfFreedom() {
		return ndf;
	}

	/**
	 * @param ndf the ndf to set
	 */
	public void setDegreesOfFreedom(int ndf) {
		this.ndf = ndf;
	}

	@Override
	public double getTestValue() {
		return chi2;
	}

	@Override
	public double getTestScore() {
		return -chi2;
	}

}
