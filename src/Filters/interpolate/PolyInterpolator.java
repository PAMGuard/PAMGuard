package Filters.interpolate;

public abstract class PolyInterpolator implements Interpolator {

	protected double[] internalData;
	protected int order;

	public PolyInterpolator(int order) {
		this.order = order;
	}

	@Override
	public double getSampleDelay() {
		return order/2;
	}

	@Override
	public void setInputData(double[] inputArray) {
		double[] oldInternal = internalData;
		if (internalData == null) {
			internalData = new double[inputArray.length + order];
		}
		else if (internalData.length != inputArray.length + order) {
			internalData = new double[inputArray.length + order];
		}
		if (oldInternal != null) {
			for (int i = 0, k = oldInternal.length-order; i < order; i++, k++) {
				internalData[i] = oldInternal[k];
			}
		}
		System.arraycopy(inputArray, 0, internalData, order, inputArray.length);
	}

}
