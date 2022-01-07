package pamMaths;

import Layout.PamAxis;
import PamUtils.TimedObservable;

/**
 * Class for collecting two dimensional histogram data. 
 * @author Doug Gillespie
 * @see PamHistogram
 *
 */
public class PamHistogram2 extends TimedObservable {
	
	protected double[][] data;
	
	protected double loBin[][], hiBin[][];

	private double minVal[], maxVal[];

	private int nBins[];
	
	private double histoScale[];
	
	private boolean binCentres;
	
	private String name;

	public PamHistogram2(double minVal1, double maxVal1, int nBins1, 
			double minVal2, double maxVal2, int nBins2) {
		prepareHistogram(minVal1, maxVal1, nBins1, minVal2, maxVal2, nBins2, false);
		double[] a = {minVal1, minVal2};
		
		setDelay(500);
	}
	
	public PamHistogram2(double minVal1, double maxVal1, int nBins1, 
			double minVal2, double maxVal2, int nBins2, boolean binCentres) {
		prepareHistogram(minVal1, maxVal1, nBins1, minVal2, maxVal2, nBins2, binCentres);
		setDelay(500);
		PamAxis p;
	}

	public void prepareHistogram(double minVal1, double maxVal1, int nBins1, 
			double minVal2, double maxVal2, int nBins2, boolean binCentres) {
		minVal = new double[2];
		maxVal = new double[2];
		nBins = new int[2];
		histoScale = new double[2];
		loBin = new double[2][];
		hiBin = new double[2][];
		data = new double[nBins1][nBins2];
	}
}
