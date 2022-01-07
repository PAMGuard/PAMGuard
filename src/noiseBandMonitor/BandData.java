package noiseBandMonitor;

/**
 * Class to create a set of octave or third octave bands
 * using base-two exact steps according to the standard
 * ANSI S1.11-2004. 
 * <p>
 * Each band had a mid frequency, a band number and a name. 
 * @author Doug Gillespie
 *
 */
public class BandData {
//	static public final int OCTAVE_BAND = 1;
//	static public final int THIRD_OCTAVE_BAND = 2;
	
	private int[] bandNumbers;
	private double[] bandCentres;
	private double[] bandHiEdges;
	private double[] bandLoEdges;
	private String[] bandNames;
	
	private static double[] nomBandFreqs = {800, 1000, 1250};
	/**
	 * 
	 * @param bandType type of band = OCTAVE_BAND or THIRD_OCTAVE_BAND
	 * @param minFreq minimum frequency of a band
	 * @param maxFreq maximum frequency of any data (highest band may have to 
	 * stop some way below this). 
	 */
	BandData(BandType bandType, double minFreq, double maxFreq) {
		double bandRatio;
		int bandStep;
		if (bandType == BandType.OCTAVE) {
			bandStep = 3;
			bandRatio = Math.pow(2., 1./2.);
		}
		else {
			bandStep = 1;
			bandRatio = Math.pow(2., 1./6.);
			
		}
		/*
		 * Work out range of bands by band number
		 */
		int firstBand = 30;
		while (calcFreq(firstBand) >= minFreq) {
			firstBand-=bandStep;
		}
		while (calcFreq(firstBand) < minFreq) {
			firstBand+=bandStep;
		}
		int lastBand = 30;
		while (calcFreq(lastBand) * bandRatio <= maxFreq) {
			lastBand+=bandStep;
		}
		while (calcFreq(lastBand) * bandRatio > maxFreq) {
			lastBand-=bandStep;
		}
		
		int nBands = (lastBand-firstBand)/bandStep+1;
		if (nBands <= 0) {
			return;
		}
		bandNumbers = new int[nBands];
		bandCentres = new double[nBands];
		bandLoEdges = new double[nBands];
		bandHiEdges = new double[nBands];
		bandNames = new String[nBands];
		int nOctaves = 0, bandPos = 0;
		
		int bandIndex = 0;
		for (int iBand = firstBand; iBand <= lastBand; iBand+=bandStep) {
			bandNumbers[bandIndex] = iBand;
			bandCentres[bandIndex] = calcFreq(iBand);
			bandHiEdges[bandIndex] = bandCentres[bandIndex]*bandRatio;
			bandLoEdges[bandIndex] = bandCentres[bandIndex]/bandRatio;
			if (bandType == BandType.OCTAVE) {
				bandNames[bandIndex] = Double.toString(bandCentres[bandIndex]);
			}
			else {
				nOctaves = (int) Math.floor((iBand-29.)/3.);
//				if (iBand < 29) nOctaves--;
				bandPos = (iBand+1)%3;
				double nameFreq = nomBandFreqs[bandPos] * Math.pow(2., nOctaves); 
				bandNames[bandIndex] = Double.toString(nameFreq);
			}
//			System.out.println(String.format("Band %d  Name %s centre %6.3f range %6.3f - %6.3f", 
//					bandNumbers[bandIndex], 
//					bandNames[bandIndex], 
//					bandCentres[bandIndex], bandLoEdges[bandIndex], bandHiEdges[bandIndex]));
			bandIndex++;
		}
		
	}
	
	static public double calcFreq(int bandNumber) {
		return Math.pow(2., ((double)bandNumber-30.)/3.)*1000.;
	}
	

	/**
	 * Get the highest band number which has a centre frequency 
	 * below the given frequency in Hz. 
	 * @param frequency Frequency in Hz
	 * @return Band number (30 = 1000 Hz). 
	 */
	static public int getLowBandNumber(double frequency) {
		return (int) Math.floor(getBandNumber(frequency));
	}
	/**
	 * Get the lowest band number which has a centre frequency 
	 * above the given frequency in Hz. 
	 * @param frequency Frequency in Hz
	 * @return Band number (30 = 1000 Hz). 
	 */
	static public int getHighBandNumber(double frequency) {
		return (int) Math.ceil(getBandNumber(frequency));
	}
	
	/**
	 * Get the band number as a non-integer value. 
	 * @param frequency Frequency in Hz
	 * @return Band number (30 = 1000 Hz). 
	 */
	static private double getBandNumber(double frequency) {
		double f = frequency / 1000.;
		f = Math.log(f)/Math.log(2.);
		return 3*f + 30;
	}
	
	static public double getBandHalfWidth(BandType bandType) {
		return bandType.getBandRatio();
	}

	/**
	 * @return the bandNumbers
	 */
	public int[] getBandNumbers() {
		return bandNumbers;
	}

	/**
	 * @return the bandCentres
	 */
	public double[] getBandCentres() {
		return bandCentres;
	}

	/**
	 * @return the bandHiEdges
	 */
	public double[] getBandHiEdges() {
		return bandHiEdges;
	}

	/**
	 * @return the bandLoEdges
	 */
	public double[] getBandLoEdges() {
		return bandLoEdges;
	}

	/**
	 * @return the bandNames
	 */
	public String[] getBandNames() {
		return bandNames;
	}
}
