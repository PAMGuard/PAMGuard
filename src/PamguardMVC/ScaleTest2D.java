package PamguardMVC;

import dataPlotsFX.data.DataTypeInfo;
import fftManager.FFTDataUnit;

public class ScaleTest2D extends DataBlock2D {
	
	private boolean logScale;

	public ScaleTest2D(Class unitClass, String dataName, PamProcess parentProcess, int channelMap) {
		super(unitClass, dataName, parentProcess, channelMap);
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		ScaleTest2D s2d = new ScaleTest2D(FFTDataUnit.class, null, null, 0);
		s2d.test();
	}

	private void test() {
		logScale = true;
		int nB = getDataWidth(0);
		for (int i = 0; i <= nB; i++ ) {
			double v = bin2Value(i, 0);
			double b = value2bin(v, 0);
			System.out.printf("Bin %d, value %3.1f, bin2 %3.1f\n", i, v, b);
		}
	}

	@Override
	public int getHopSamples() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDataWidth(int sequenceNumber) {
		return 100;
	}

	@Override
	public double getMinDataValue() {
		// TODO Auto-generated method stub
		return 1000;
	}

	@Override
	public double getMaxDataValue() {
		// TODO Auto-generated method stub
		return 10000;
	}

	@Override
	public DataTypeInfo getScaleInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLogScale() {
		// TODO Auto-generated method stub
		return logScale;
	}

}
