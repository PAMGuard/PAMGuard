package qa.generator.distributions;

import java.util.Arrays;

import Filters.Filter;
import Filters.FilterBand;
import Filters.FilterParams;
import Filters.IirfFilter;

/**
 * Make a correlated sequence of values with a given width and mean. <p>
 * This can be used to generate slowly varying amplitude and ICI sequences.
 * Works by generating the numbers from a base distribution and then
 * filtering them at a frequency of 1/correlation using a 'correlation' order butterworth filter.  
 *  
 * @author dg50
 *
 */
public class QACorrelatedSequence extends QADistribution {

	private int correlation;
	private QADistribution baseDistribution;
	private IirfFilter filter;
	private double mean;

	/**
	 * 
	 * Construct a correlated sequence of values with given mean and distribution width
	 * using a gamma distribution. 
	 * @param integrate integrate the values (e.g. to give times rather than inter-click intervals)
	 * @param baseDistribution underlying distribution. This will probably need to be set up with a 
	 * width (e.g. the Guassian sigma) of sqrt(correlation) times the width you want in the distribution 
	 * of the output data.  
	 * @param mean mean value
	 * @param width width of distribution
	 * @param correlation order of the correlation
	 */
	public QACorrelatedSequence(boolean integrate, QADistribution baseDistribution, double mean, double width, int correlation) {
		super(false, integrate);
		this.correlation = correlation;
		this.baseDistribution = baseDistribution;
		this.mean = mean;
//		baseDistribution = new QAGamma(mean, width * Math.sqrt(correlation));
		FilterParams filterParams = new FilterParams();
		filterParams.filterBand = FilterBand.LOWPASS;
		filterParams.lowPassFreq = (float) (1./(2.*correlation));
		filterParams.filterOrder = correlation;
		filter = new IirfFilter(0, 1, filterParams);
		filter.prepareFilter();
	}
	/**
	 * Construct a correlated sequence of values with given mean and distribution width
	 * using a gamma distribution. 
	 * @param integrate integrate the values (e.g. to give times rather than inter-click intervals)
	 * @param mean mean value
	 * @param width width of distribution
	 * @param correlation order of the correlation
	 */
	public QACorrelatedSequence(boolean integrate, double mean, double width, int correlation) {
		this(integrate, new QAGamma(mean, width * Math.sqrt(correlation)), mean, width, correlation);
	}

	@Override
	protected double[] createValues(int nValues) {
		// pad the data a bit to help it in settling. 
		double[] values = baseDistribution.createValues(nValues + correlation);
		filter.resetFilter();
		/**
		 * After the filter is reset each time, it's initialised as though all inputs
		 * were zero. If the mean is far from zero, it can take a very long 
		 * time (Ord(correlation*correlation)) calls to settle the filter - it's IIR
		 * so it never really happens - So it's much safer to subtract off the mean, 
		 * so it's centred at zero, filter, then add the main back on afterwards. 
		 */
		double reference = 0;
		for (int i = 0; i < values.length; i++) {
			reference += values[i]; // calculate mean
		}
		reference /= values.length;
		for (int i = 0; i < values.length; i++) {
			values[i]-=reference; // Remove mean
		}
		
		filter.runFilter(values); // run filter centred at zero
		
		for (int i = 0; i < values.length; i++) {
			values[i]+=reference; // add back the mean
		}
		values = Arrays.copyOfRange(values, correlation, values.length);
		return values;
	}
	@Override
	public double[] getRange(double nSigma) {
		return baseDistribution.getRange();
	}

}
