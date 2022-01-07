package gpl.whiten;

import PamUtils.complex.ComplexArray;

/**
 * Whitens data in time using the methods provided by Gerald d'Spain and used in 
 * Tyler Helbles paper.
 * Uses order statistics to calculate a mean background over the last minute of data.  
 * @author dg50
 *
 */
public class Base3xWhitener implements TimeWhitener {

	/**
	 * Number of points for order filters. 
	 */
	private int nPoints;
	
	/**
	 * Whitening factor. 
	 */
	private double whiteFac = 1.;

	private int binLo;

	private int binHigh;
	
	private InfiniteSort[] infiniteSorts;
	
	private double[][] toSmooth;
	
	final double[][] cross = {{0., .125, 0.},{.125, .5, .125}, {0., .125, 0.}};
	
	public Base3xWhitener(int nPoints, double whiteFac, int binLo, int binHigh) {
		this.nPoints = nPoints;
		this.whiteFac = whiteFac;
		if (this.whiteFac == 0) this.whiteFac = 1.;
		this.binLo = binLo;
		this.binHigh = binHigh;
		int nFilters = binHigh-binLo+1; 
		infiniteSorts = new InfiniteSort[nFilters];
		for (int i = 0; i < nFilters; i++) {
			infiniteSorts[i] = new InfiniteSort(nPoints);
		}
		/**
		 * Make the data array 2 inds bigger so we can run the smoothing
		 * kernel more easily. 
		 */
		toSmooth = new double[3][nFilters+2];
	}

	@Override
	public void addBackground(double[] rawData) {
		int n = rawData.length;
		for (int is = 0; is < n; is++) {
//			if (is == 0 && infiniteSorts[0].getTotalCalls() == 24) {
//				boolean ok = infiniteSorts[is].checkSort();
//			}
			infiniteSorts[is].addData(rawData[is]);
//			if (is == 0) {
//				boolean ok = infiniteSorts[is].checkSort();
//			}
		}
	}

	@Override
	public double[] getBackground() {
		int n = infiniteSorts.length;
		double[] wData = new double[n];
		for (int is = 0; is < n; is++) {
			wData[is] = infiniteSorts[is].getCentralMean();;
		}
		
		return wData;
	}
	
	@Override
	public double[] whitenData(double[] specMean, double[] specData) {
		
		/*
		 * Run the smoothing kernel. Easy because toSmooth 
		 * already contains the extra padding and can put output
		 * immediately into the correct bin b.  
		 * this is code at the bottom of SIO's GPL_whiten function. 
		 */

		/*
		 * before starting need to rotate the 
		 * convoluted data history. 
		 */
		double[] a = toSmooth[0];
		toSmooth[0] = toSmooth[1];
		toSmooth[1] = toSmooth[2];
		toSmooth[2] = a; // new data will go into a
		/*
		 * Pad wData by an extra two bins since it's used in a convolution in the 
		 * contour finder later on. Much easier to add the passing now. 
		 */
		double[] wData = new double[binHigh-binLo+1];
		for (int b = binLo, is = 0; b <= binHigh; b++, is++) {
			// this is what's happening at line 26 in whiten_matrix spc=sp-fac*mu*ones(1,sz2)
			a[is+1] = (specData[is] - whiteFac*specMean[is]); // offset a[] by 1 for later convolution.
			// then what at line 10 in GPL_whiten 
			a[is+1] = Math.abs(a[is+1]/specMean[is]);
		}
		for (int b = binLo, is = 0; b <= binHigh; b++, is++) {
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					wData[is] += cross[i][j]*toSmooth[i][j+is];
				}
			}
//			wData[b] = Math.max(0.,  wData[b]);
		}
		
		
		return wData;
	}
	
	/**
	 * Tell the sorters to automatically fill themselves with repeated
	 * data at startup when autoInitialise input datas have arrived. This 
	 * can speed up algorithm settling at detector startup. 
	 * @param autoInitialise
	 */
	public void setAutoInitialise(int autoInitialise) {
		for (int i = 0; i < infiniteSorts.length; i++) {
			infiniteSorts[i].setAutoInitialise(autoInitialise);
		}
	}
	
//	/**
//	 * Take the mean of the central part of the data - that is the half of the
//	 * data which has the smallest difference between its upper and lower bounds. 
//	 * @param data unsorted data array
//	 * @param sortInds sort indexes
//	 * @return mean value for the most stable 50% of the data.  
//	 */
//	private double getCentralMean(double[] data, int[] sortInds) {
//		int n = data.length/2;
//		double min = data[sortInds[n]]-data[sortInds[0]];
//		int minInd = 0;
//		for (int i = 1, j = n+1; i < n; i++, j++) {
//			double d = data[sortInds[j]]-data[sortInds[i]];
//			if (d < min) {
//				min = d;
//				minInd = i;
//			}
//		}
//		/*
//		 * minInd will now be the index of the closest point between the lower and upper halves of the data. 
//		 */
//		double tot = 0;
//		for (int i = minInd; i < n+minInd; i++) {
//			tot += data[sortInds[i]];
//		}
//		return tot/n;
//	}

}
