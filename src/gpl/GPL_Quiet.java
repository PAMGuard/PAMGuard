package gpl;

import gpl.whiten.InfiniteSort;
import gpl.whiten.InfiniteSortGroup;
import gpl.whiten.MovingMatrix;
import gpl.whiten.WhitenMatrix;
import gpl.whiten.WhitenVector;

/**
 * Recreate functionality of SIO function GPL_Quiet. 
 * @author dg50
 *
 */
public class GPL_Quiet {

	private GPLProcess gplProcess;
	private int channel;
	private MovingMatrix historyMatrix;
	private int nTimeBins;
	private int nFreqBins;
	double[] u;
	double[] y;
	private WhitenMatrix yWhitener1, yWhitener2;
	private WhitenVector whitenVector;
	private InfiniteSort baselineSort, b0sort;
	private boolean allZero = true;

	public GPL_Quiet(GPLProcess gplProcess, int channel, int nTimeBins, int nFreqBins) {
		this.gplProcess = gplProcess;
		this.channel = channel;
		this.nTimeBins = nTimeBins;
		this.nFreqBins = nFreqBins;
		historyMatrix = new MovingMatrix(nTimeBins, nFreqBins);
		u = new double[nFreqBins];
		y = new double[nFreqBins];
		yWhitener1 = new WhitenMatrix(nFreqBins, nTimeBins, 1.0);
		yWhitener2 = new WhitenMatrix(nFreqBins, nTimeBins, 1.0);
		whitenVector = new WhitenVector();
		baselineSort = new InfiniteSort(nTimeBins);
//		baselineSort.setStop(1406);
		b0sort = new InfiniteSort(nTimeBins);
	}
	
	/**
	 * Process next FFT slice of data. 
	 * @param whiteData whitened FFT data. 
	 * @return several things all packed up in QUIETData. 
	 */
	public QuietStruct processData(double[] specData, double[] whiteData) {
		if (allZero && checkAllZero(whiteData)) {
			return null;
		}
		
		QuietStruct q = new QuietStruct();
		historyMatrix.addData(whiteData);
		double uFac = Math.sqrt(historyMatrix.getSumTime2()[historyMatrix.getiTime()]);
		double[] yFac = historyMatrix.getSumFreq2();
		for (int i = 0; i < nFreqBins; i++) {
			u[i] = whiteData[i]/uFac;
			y[i] = whiteData[i]/Math.sqrt(yFac[i]);
		}
		/*
		 * Then whiten u over frequency (just needs a sort and a mean subtraction
		 * so can use Java sort which will be fastest)
		 * And whiten y over time - so need another history system 
		 * for each frequency using InfiniteSorts. 
		 */
		y = yWhitener1.whitenData(y);
		u = whitenVector.whitenVector(u, 1.);
		
		GPLParameters gplParams = gplProcess.getGplControlledUnit().getGplParameters();
		double[] bas = new double[whiteData.length];
		for (int i = 0; i < bas.length; i++) {
			bas[i] = Math.pow(Math.abs(u[i]), gplParams.xp1) * Math.pow(Math.abs(y[i]), gplParams.xp2);
		}
		double baseline0 = 0;
		for (int i = 0; i < bas.length; i++) {
			baseline0 += bas[i]*bas[i]; 
		}
		/*
		 * Now skip the bit of GPL_quiet which seems to get rid of exceptionally loud 
		 * areas. discuss with GS whether this is really needed. 
		 */
		baselineSort.addData(baseline0);
		double quiet_base = baselineSort.getCentralMean();
		/*
		 * Need to take the min value of this b0 value over our entire 
		 * history period, so will need another sorter for b0
		 * so that we can take it' sminimum value. Don't actually need
		 * it - can just to -(min(baselineSort)-quiet_base)) and 
		 * get the same value.  
		 */
		double b0 = baseline0 - quiet_base;
		baseline0 = b0/quiet_base;
//		b0sort.addData(baseline0);
		double noise_floor = (quiet_base-baselineSort.getSortedData(0))/quiet_base; // lowest point in baseline data. 
		boolean ks = baseline0 < gplParams.noise_ceiling * noise_floor;
		if (ks) {
			// data failed at the first !
		}
		
		
		
		q.baseline0 = baseline0;
		q.baseline1 = baseline0;
		q.noise_floor = noise_floor;
		q.quiet_base = quiet_base;
		return q;
	}

	private boolean checkAllZero(double[] whiteData) {
		for (int i = 0; i < whiteData.length; i++) {
			if (whiteData[i] != 0.) {
				allZero = false;
				return false;
			}
		}
		return true;
	}
}
