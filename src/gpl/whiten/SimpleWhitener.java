package gpl.whiten;

import PamUtils.complex.ComplexArray;

public class SimpleWhitener implements TimeWhitener {

	double[] background;
	private double scaleFactor;
	
	public SimpleWhitener(double scaleFac) {
		if (scaleFac > 1) {
			scaleFac = 1./scaleFac;
		}
		this.scaleFactor = scaleFac;
	}

	@Override
	public double[] whitenData(double[] background, double[] rawData) {
		int n = rawData.length;
		if (background == null || background.length != n) {
			background = new double[n];
		}
		double[][] data = new double[2][n];
		for (int i = 0; i < n; i++) {
			double raw = rawData[i];
			data[0][i] = Math.max(raw - background[i], 0);
			data[1][i] = background[i];
			background[i] += (raw-background[i])*scaleFactor;
		}
		return data[0];
	}


	@Override
	public void addBackground(double[] specData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double[] getBackground() {
		// TODO Auto-generated method stub
		return null;
	}

}
