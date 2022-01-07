package whistleClassifier;

public class WhistleFragment extends Object implements WhistleContour {

	private double[] timesSeconds;
	
	private double[] freqsHz;
	
	
	private static int FITORDER = 2;
	
	private double[] shapeFit;
	
	private int negInflections;
	
	private int posInflections;
	
	/**
	 * Constructor to create a fragment using all of the 
	 * data passed
	 * @param times times (seconds)
	 * @param freqs frequencies (Hz)
	 */
	public WhistleFragment(double[] times, double[] freqs) {
		super();
		this.timesSeconds = times.clone();
		setTimeZero();
		this.freqsHz = freqs.clone();
		
//		calculateParameters();
	}
	
	/**
	 * Constructor to automatically extract the fragment from a longer
	 * segment. 
	 * @param times segment times (seconds)
	 * @param freqs segment frequencies (Hz)
	 * @param start first bin in segment to take
	 * @param fragLen length of fragment to create
	 */
	public WhistleFragment(double[] times, double[] freqs, int start, int fragLen) {
		super();
		createFragment(times, freqs, start, fragLen);
	}
	
	/**
	 * Constructor to automatically extract a fragment from a longer
	 * contour.  
	 * @param whistleContour contour to extract fragment from 
	 * @param start start position in contour
	 * @param fragLen length of fragment
	 */
	public WhistleFragment(WhistleContour whistleContour, int start, int fragLen) {
		super();
		createFragment(whistleContour.getTimesInSeconds(), whistleContour.getFreqsHz(), start, fragLen);
	}
	
	private void createFragment(double[] times, double[] freqs, int start, int fragLen) {
		timesSeconds = new double[fragLen];
		freqsHz = new double[fragLen];
		for (int i = 0; i < fragLen; i++) {
			timesSeconds[i] = times[i+start];
			freqsHz[i] = freqs[i+start];
		}
		setTimeZero();
	}
	/**
	 * Reference all the times in the fragment to zero
	 */
	private void setTimeZero(){
		double t0 = timesSeconds[0];
		for (int i = 0; i < timesSeconds.length; i++) {
			timesSeconds[i] -= t0;
		}
	}
	
	@Override
	public double[] getFreqsHz() {
		return freqsHz;
	}

	@Override
	public double[] getTimesInSeconds() {
		return timesSeconds;
	}

//	public void calculateParameters() {
//		
//		fitQuadratic();
//		
//		// search for inflections - see if the end slope is different to the start slope. 
//		boolean startUp = (shapeFit[1] > 0);
//		boolean endUp = (shapeFit[1] + 2 * timesSeconds[timesSeconds.length-1] * shapeFit[2] > 0);
//		if (startUp && !endUp) {
//			negInflections++;
//		}
//		else if (!startUp && endUp) {
//			posInflections++;
//		}
//	}
//	
//	/**
//	 * Fit a quadratic to the time and frequency data. 
//	 *
//	 */
//	private void fitQuadratic() {
//		
//		Regressions r = new Regressions();
//		double[] quadFit = r.squareFit(timesSeconds, freqsHz);
//		double[] linFit = r.polyFit(timesSeconds, freqsHz, 1);
//		double mean = r.getMean(freqsHz);
//		
//		shapeFit = quadFit;
//		shapeFit[0] = mean;
//		shapeFit[1] = linFit[1];
//	}
//
//	public static int getFITORDER() {
//		return FITORDER;
//	}
//
//	public int getNegInflections() {
//		return negInflections;
//	}
//
//	public int getPosInflections() {
//		return posInflections;
//	}
//
//	public double[] getShapeFit() {
//		return shapeFit;
//	}

}
