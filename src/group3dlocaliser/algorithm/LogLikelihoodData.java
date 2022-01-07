package group3dlocaliser.algorithm;

public class LogLikelihoodData implements FitTestValue{

	private double llValue;
	
	private int nDF;

	/**
	 * @param llValue Log Likelihood value
	 * @param nDF Number of degreees of freedom
	 */
	public LogLikelihoodData(double llValue, int nDF) {
		super();
		this.llValue = llValue;
		this.nDF = nDF;
	}

	@Override
	public double getTestValue() {
		return llValue;
	}

	@Override
	public int getDegreesOfFreedom() {
		return nDF;
	}

	@Override
	public double getTestScore() {
		return llValue;
	}

}
