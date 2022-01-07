package pamMaths;

public class PamLogHistogram extends PamHistogram {

	public PamLogHistogram(double minVal, double maxVal, int nBins) {
		super(Math.log(minVal), Math.log(maxVal), nBins);
	}

	/**
	 * @param minVal
	 * @param maxVal
	 * @param nBins
	 * @param binCentres
	 */
	public PamLogHistogram(double minVal, double maxVal, int nBins, boolean binCentres) {
		super(Math.log(minVal), Math.log(maxVal), nBins, binCentres);
	}

	@Override
	public int getBin(double dataValue) {
		return super.getBin(Math.log(dataValue));
	}

	@Override
	public double getBinCentre(int iBin) {
		return Math.exp(super.getBinCentre(iBin));
	}

	@Override
	public void addData(double newData, boolean notify) {
		// no need to do anything since log conversion is done in getBin
		super.addData(newData, notify);
	}

	@Override
	public void addData(double newData) {
		// no need to do anything since log conversion is done in getBin
		super.addData(newData);
	}

	@Override
	public void addData(double newData, double weight) {
		// no need to do anything since log conversion is done in getBin
		super.addData(newData, weight);
	}

	@Override
	public double getMean() {
		return Math.exp(super.getMean());
	}

	@Override
	public double getMaxVal() {
		return Math.exp(super.getMaxVal());
	}

	@Override
	public double getMinVal() {
		return Math.exp(super.getMinVal());
	}

	@Override
	public double getScaleMinVal() {
		return Math.exp(super.getScaleMinVal());
	}

	@Override
	public double getScaleMaxVal() {
		return Math.exp(super.getScaleMaxVal());
	}

	@Override
	public double[] getBinCentreValues() {
		return exp(super.getBinCentreValues());
	}
	
	/**
	 * Math.exp of an array for converting back from log values. 
	 * @param lin logged values
	 * @return exponent of input
	 */
	private double[] exp(double[] lin) {
		if (lin == null) return null;
		double[] a = new double[lin.length];
		for (int i = 0; i < lin.length; i++) {
			a[i] = Math.exp(lin[i]);
		}
		return a;
	}

	@Override
	public double[] getBinEdgeValues() {
		return exp(super.getBinEdgeValues());
	}

}
