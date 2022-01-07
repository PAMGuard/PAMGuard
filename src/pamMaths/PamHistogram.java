package pamMaths;

import java.io.PrintStream;

import PamUtils.TimedObservable;

/**
 * class for collecting data into a 1-D histogram. 
 * <p>As well as basic histogramming functions, functions
 * exist for downscaling the histogram data and extracting parameters
 * such as variance, skew, etc. 
 * 
 * @author Doug Gillespie
 *
 */
public class PamHistogram extends TimedObservable implements Cloneable {

	protected double[] data;
	
	protected double loBin, hiBin;

	private double minVal, maxVal;

	private int nBins;
	
	private double histoScale;
	
	private boolean binCentres;
	
	private String name;

	/**
	 * Constructs a histogram object with  minVal
	 * and maxVal, specifying the low edge of the lowest bin and
	 * the high edge of the highest bin respectively. 
	 * @param minVal min Value
	 * @param maxVal max Value
	 * @param nBins number of bins
	 */
	public PamHistogram(double minVal, double maxVal, int nBins) {
		prepareHistogram(minVal, maxVal, nBins, false);
		setDelay(500);
	}
	
	/**
	 * Constructs a histogram object where minVal and maxVal specify the centres of the lower and
	 * upper bins (true) or the lower edge of the lowest bin and the upper edge of the highest bin (false)
	 * @param minVal minimum value (low edge or low bin centre, depending on binCentres parameter)
	 * @param maxVal maximum value (high edge or high bin centre, depending on binCentres parameter)
	 * @param nBins total number of bins
	 * @param binCentres specifies whether the minVal and maxVal specify the centres of the lower and
	 * upper bins (true) or the lower edge of the lowest bin and the upper edge of the highest bin (false)
	 */
	public PamHistogram(double minVal, double maxVal, int nBins, boolean binCentres) {
		prepareHistogram(minVal, maxVal, nBins, binCentres);
	}
	
	/**
	 * Allocates memory and generally prepeare the histogram for use. 
	 * @param minVal minimum value (low edge or low bin centre, depending on binCentres parameter)
	 * @param maxVal maximum value (high edge or high bin centre, depending on binCentres parameter)
	 * @param nBins total number of bins
	 * @param binCentres specifies whether the minVal and maxVal specify the centres of the lower and
	 * upper bins (true) or the lower edge of the lowest bin and the upper edge of the highest bin (false)
	 */
	private void prepareHistogram(double minVal, double maxVal, int nBins, boolean binCentres) {
		this.minVal = minVal;
		this.maxVal = maxVal;
		this.nBins = nBins;
		this.binCentres = binCentres;
		data = new double[nBins];
		if (binCentres) {
			histoScale = nBins / (maxVal - minVal + 1);
		}
		else {
			histoScale = nBins / (maxVal - minVal);
		}
	}
	
	/**
	 * Sets the histogram bin ranges.<p>Note that calling
	 * this function will reset the histogram data. 
	 * @param minVal minimum value (low edge or low bin centre, depending on binCentres parameter)
	 * @param maxVal maximum value (high edge or high bin centre, depending on binCentres parameter)
	 * @param nBins total number of bins
	 */
	public void setRange(double minVal, double maxVal, int nBins) {
		boolean changes = (minVal != this.minVal || maxVal != this.maxVal || nBins != this.nBins);
		prepareHistogram(minVal, maxVal, nBins, binCentres);
		if (changes) {
			notifyObservers(this);
		}
	}
	
	/**
	 * Work out which bin a particular value will call into. 
	 * N.B. This may return < 0 or >= getNBins() if the data fall outsied 
	 * the range of the histogram
	 * @param dataValue
	 * @return bin Bumber
	 */
	public int getBin(double dataValue) {
		return (int) ((dataValue - minVal) * histoScale);
	}
	
	public double getBinCentre(int iBin) {
		if (binCentres) {
			return (iBin / histoScale) + minVal;
		}
		else {
			return ((iBin + 0.5) / histoScale) + minVal;
		}
	}

	/**
	 * Set the histogram data
	 * @param data array must be same length as data array in histogram.
	 */
	public void setData(int[] data) {
		if (data.length != this.data.length) {
			return;
		}
		for (int i = 0; i < data.length; i++) {
			this.data[i] = data[i];
		}
	}

	/**
	 * Add a single unit value to the histogram
	 * and optionally notify all observers
	 * @param newData value of the new data
	 */
	public void addData(double newData, boolean notify) {
		int bin = getBin(newData);
		if (bin < 0) {
			loBin++;
		}
		else if (bin >= nBins) {
			hiBin++;
		}
		else {
			data[bin]++;
		}

		if (notify) {
			notifyObservers();
		}
	}

	/**
	 * Add a single point of data to the histogram
	 * but don't notify observers
	 * @param newData new data point
	 */
	public void addData(double newData) {
		int bin = getBin(newData);
		if (bin < 0) {
			loBin++;
		}
		else if (bin >= nBins) {
			hiBin++;
		}
		else {
			data[bin]++;
		}
	}

	/**
	 * Add weighted data to the histogram
	 * @param newData value of the data
	 * @param weight weight to apply
	 */
	public void addData(double newData, double weight) {
		int bin = getBin(newData);
		if (bin < 0) {
			loBin += weight;
		}
		else if (bin >= nBins) {
			hiBin += weight;
		}
		else {
			data[bin] += weight;
		}

		notifyObservers();
	}
	
	/**
	 * Clear all histogram contents
	 *
	 */
	public void clear() {
		for (int i = 0; i < nBins; i++) {
			data[i] = 0;
		}
		loBin = hiBin = 0;

		notifyObservers(true);
	}
	
	/**
	 * Scale the data in the histogram by the given factor
	 * <p> This is used in various parts of PAMGUARD which 
	 * wish to keep a decaying average distribution - every 
	 * few seconds the data are halved (or some such) so that 
	 * all data are represented, but recent data will carry more
	 * weight in the distributions. 
	 * @param scaleFactor FActor to scale data by (generally < 1)
	 */
	public void scaleData(double scaleFactor) {
		for (int i = 0; i < nBins; i++) {
			data[i] *= scaleFactor;
		}
	}
	
	/**
	 * Get max bin content, including lo and high fields. 
	 * @return max bin content. 
	 */
	public double getMaxContent() {
		double max = Math.max(loBin, hiBin);
		for (int i = 0; i < data.length; i++) {
			max = Math.max(max, data[i]);
		}
		return max;
	}
	
	/**
	 * Calculate the mean of the histogram data
	 * @return mean value
	 */
	public double getMean() {
		double m = 0;
		double n = 0;
		for (int i = 0; i < nBins; i++) {
			m += getBinCentre(i) * data[i];
			n += data[i];
		}
		if (n == 0){
			return 0; // return zero for an empty histogram
		}
		return m/n;
	}
	
	/**
	 * Return the standard deviation of the histogram data
	 * @return standard deviation
	 */
	public double getSTD() {
		return Math.sqrt(getVariance());
	}
	
	/**
	 * Return the variance of the histogram data
	 * @return get variance of distribution in histogram
	 */
	public double getVariance() {
		double v = 0;
		double n = 0;
		double mean = getMean();
		for (int i = 0; i < nBins; i++) {
			v += Math.pow((getBinCentre(i)-mean), 2.) * data[i];
			n += data[i];
		}
		return v/n;
	}
	
	/** 
	 * Get the skewness of the distribution (third moment)
	 * @return skew
	 */
	public double getSkew() {
		double s = 0;
		double n = 0;
		double mean = getMean();
		double std = getSTD();
		for (int i = 0; i < nBins; i++) {
			s += Math.pow((getBinCentre(i)-mean)/std, 3.) * data[i];
			n += data[i];
		}
		if (n == 0) return 0;
		return s/n;
	}

	/** 
	 * Get the kurtosis of the distribution (fourth moment)
	 * taking the definition from Numerical recipes in C p 612
	 * @return kurtosis
	 */
	public double getKurtosis() {
		double k = 0;
		double n = 0;
		double mean = getMean();
		double std = getSTD();
		for (int i = 0; i < nBins; i++) {
			k += Math.pow((getBinCentre(i)-mean), 4.) * data[i];
			n += data[i];
		}		
		if (n == 0) return 0;
		return k/n/std -3;
	}
	
	/**
	 * Get's the modal value of the distribution
	 * @return value of the bin centre for the most populated bin
	 */
	public double getMode() {
		int iB = -1;
		double maxVal = 0;
		for (int i = 0; i < nBins; i++) {
			if (data[i] > maxVal) {
				maxVal = data[i];
				iB = i;
			}
		}
		if (iB < 0) return Double.NaN;
		return getBinCentre(iB);
	}
	
	/** 
	 * Get's the sum total of all bin contents, excluding lo and high bins
	 * @return get total content of all histogram bins
	 */
	public double getTotalContent() {
		double n = 0;
		for (int i = 0; i < nBins; i++) {
			n += data[i];
		}
		return n;
	}
	
//	public double getMoment(int n) {
//		switch (n) {
//		case 0:
//			return 1;
//		case 1:
//			return getMean();
//		case 2:
//			return getSTD();
//		case 3:
//			return getSkew();
//		default:
//			return getHighMoment(n);
//		}
//	}
//	
//	private double getHighMoment(int n) {
//		double m;
//		double lower
//	}
	
	/**
	 * Get the histograms data array. 
	 * @return An array of double data
	 */
	public double[] getData() {
		return data;
	}

	/**
	 * Get the maximum range value of the histogram
	 * @return max value
	 */
	public double getMaxVal() {
		return maxVal;
	}

	/**
	 * Get the minimum range value of the histogram
	 * @return min value
	 */
	public double getMinVal() {
		return minVal;
	}
	
	/**
	 * Where to plot from if bin centres were specified.
	 * @return minimum value for plot axis
	 */
	public double getScaleMinVal() {
		if (binCentres) {
			return minVal - getStep()/2.;
		}
		else {
			return minVal;
		}
	}
	/**
	 * Where to plot to if bin centres were specified.
	 * @return maximum value for plot axis
	 */
	public double getScaleMaxVal() {
		if (binCentres) {
			return maxVal + getStep()/2.;
		}
		else {
			return maxVal;
		}
	}

	/**
	 * Get the number of histogram bins
	 * @return number of histogram bins
	 */
	public int getNBins() {
		return nBins;
	}
	
	/**
	 * Step size between sucessive bins.
	 * @return histogram step size (bin width)
	 */
	public double getStep() {
		return 1./histoScale;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {

		String histString;
		if (name != null) {
			histString = new String(name);
		}
		else {
			histString = new String("Unnamed Histogram");
		}
		
		if (binCentres) {
			histString += String.format(" % d bins centred between %3f and %3f", nBins, minVal, maxVal);
		}
		else {
			histString += String.format(" % d bins with edges between %3f and %3f", nBins, minVal, maxVal);
		}
		
		return histString;
	}

	/**
	 * Get content of high bin - values > max range of histogram
	 * @return high bin content
	 */
	public double getHiBin() {
		return hiBin;
	}

	/**
	 * Get content of low bin - values < min range of histogram
	 * @return low bin content
	 */ 
	public double getLoBin() {
		return loBin;
	}

	public boolean isBinCentres() {
		return binCentres;
	}

	public void setBinCentres(boolean binCentres) {
		this.binCentres = binCentres;
	}

	@Override
	protected PamHistogram clone() {
		try {
			PamHistogram newHist = (PamHistogram) super.clone();
			newHist.data = newHist.data.clone();
			return newHist;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get the centre value of every bin
	 * @return array of bin centres.
	 */
	public double[] getBinCentreValues() {
		double[] binCentres = new double[nBins];
		for (int i = 0; i < nBins; i++) {
			binCentres[i] = getBinCentre(i);
		}
		
		return binCentres;
	}
	
	/**
	 * Get an array of bin edges. This will be one longer than 
	 * the number of bins
	 * @return array of bin edge values. 
	 */
	public double[] getBinEdgeValues() {
		double[] binEdges = new double[nBins+1];
		binEdges[0] = minVal;
		binEdges[nBins] = maxVal;
		double step  = (maxVal-minVal) / nBins;
		for (int i = 1; i < nBins; i++) {
			binEdges[i] = binEdges[i-1]+step;
		}
		return binEdges;
	}

	public void printSummary() {
		printSummary("", System.out);
	}
	
	public void printSummary(String title, PrintStream printStream) {
		if (printStream == null) {
			printStream = System.out;
		}
		if (title == null) {
			title = "Unnamed Histogram";
		}
		double[] cents = getBinCentreValues();
		double max = Math.max(getMaxContent(), 1);
		printStream.printf("%s total content %4.1f, max content %3.1f\n", title, getTotalContent(), getMaxContent());
		printHistoLine(printStream, "Low bin", getLoBin()/max);
		for (int i = 0; i < cents.length; i++) {
			String pre = String.format("%3d:%5.1f %5d", i, cents[i], (int) data[i]);
			printHistoLine(printStream, pre, data[i]/max);
		}
		printHistoLine(printStream, "High bin", getHiBin()/max);
	}
	
	/**
	 * Print summary line = the text and up to 60 stars
	 * @param preText text
	 * @param scaledValue value between 0 and 1. 
	 */
	private void printHistoLine(PrintStream printStream, String preText, double scaledValue) {
		int starCount = (int) Math.round(scaledValue * 60);
		printStream.printf("%16s |", preText);
		for (int s = 0; s < starCount; s++) {
			printStream.printf("*");
		}
		printStream.printf("\n");
	}
	
	

}
