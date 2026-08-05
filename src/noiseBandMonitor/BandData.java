package noiseBandMonitor;

import PamUtils.FrequencyFormat;

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
	private BandType bandType;
	private double minFreq;
	private double maxFreq;
	private double referenceFreq;
	private double bandRatio;
	private double halfBand;
	
	private static double[] nomBandFreqs = {800, 1000, 1250};
	/**
	 * 
	 * @param bandType type of band = OCTAVE_BAND or THIRD_OCTAVE_BAND
	 * @param minFreq minimum frequency of a band
	 * @param maxFreq maximum frequency of any data (highest band may have to 
	 * stop some way below this). 
	 */
	BandData(BandType bandType, double minFreq, double maxFreq, double referenceFreq) {
		this.bandType = bandType;
		this.minFreq = minFreq;
		this.maxFreq = maxFreq;
		this.referenceFreq = referenceFreq;
		this.bandRatio = bandType.getBandRatio();
		this.halfBand = Math.sqrt(bandRatio);
		calculateBands();
	}
	
//	private void calculateBands() {
//		double bandRatio;
//		int bandStep;
//		if (bandType == BandType.OCTAVE) {
//			bandStep = 3;
////			bandRatio = Math.pow(2., 1./2.);
//		}
//		else {
//			bandStep = 1;
////			bandRatio = Math.pow(2., 1./6.);
//		}
//		if (bandType == null) {
//			return;
//		}
//		bandRatio = bandType.getBandRatio();
//		double bandHalfRatio = Math.sqrt(bandRatio);
//		/*
//		 * Work out range of bands by band number
//		 */
//		int firstBand = 30;
//		while (calcCentreFreq(firstBand) >= minFreq) {
//			firstBand-=bandStep;
//		}
//		while (calcCentreFreq(firstBand) < minFreq) {
//			firstBand+=bandStep;
//		}
//		int lastBand = 30;
//		while (calcCentreFreq(lastBand) * bandRatio <= maxFreq) {
//			lastBand+=bandStep;
//		}
//		while (calcCentreFreq(lastBand) * bandRatio > maxFreq) {
//			lastBand-=bandStep;
//		}
//		double f1 = calcCentreFreq(firstBand);
//		double f2 = calcCentreFreq(lastBand);
//		
//		int nBands = (int) Math.round(Math.log(f2/f1)/Math.log(bandRatio));
//		if (bandType.standardBand()) {
//			nBands = (lastBand-firstBand)/bandStep+1;
//		}
//		if (nBands <= 0) {
//			return;
//		}
//		bandNumbers = new int[nBands];
//		bandCentres = new double[nBands];
//		bandLoEdges = new double[nBands];
//		bandHiEdges = new double[nBands];
//		bandNames = new String[nBands];
//		int nOctaves = 0, bandPos = 0;
//		
//		int iBand = firstBand;
//		for (int bandIndex = 0; bandIndex < nBands; bandIndex++, iBand+=bandStep) {
//			bandNumbers[bandIndex] = iBand;
//			if (bandType.standardBand()) {
//				bandCentres[bandIndex] = calcCentreFreq(iBand);
//				bandHiEdges[bandIndex] = bandCentres[bandIndex]*bandHalfRatio;
//				bandLoEdges[bandIndex] = bandCentres[bandIndex]/bandHalfRatio;
//			}
//			else {
//				bandCentres[bandIndex] = f1*Math.pow(bandRatio, bandIndex);
//				bandHiEdges[bandIndex] = bandCentres[bandIndex]*bandHalfRatio;
//				bandLoEdges[bandIndex] = bandCentres[bandIndex]/bandHalfRatio;
//			}
//			if (bandType == BandType.OCTAVE) {
//				bandNames[bandIndex] = Double.toString(bandCentres[bandIndex]);
//			}
//			else {
//				nOctaves = (int) Math.floor((iBand-29.)/3.);
////				if (iBand < 29) nOctaves--;
//				bandPos = (iBand+1)%3;
//				double nameFreq = nomBandFreqs[bandPos] * Math.pow(2., nOctaves); 
//				bandNames[bandIndex] = Double.toString(nameFreq);
//			}
////			System.out.println(String.format("Band %d  Name %s centre %6.3f range %6.3f - %6.3f", 
////					bandNumbers[bandIndex], 
////					bandNames[bandIndex], 
////					bandCentres[bandIndex], bandLoEdges[bandIndex], bandHiEdges[bandIndex]));
////			bandIndex++;
//		}
//		
//	}
	
	/**
	 * New method for calculating frequency bands without using band numbers, 
	 * only min and max frequencies, reference frequency and band ratios. 
	 */
	private void calculateBands() {
		int minB = getMinBand();
		int maxB = getMaxBand();
		int nBand = maxB-minB+1;
		if (nBand <= 0) {
			return;
		}
		bandCentres = new double[nBand];
		bandLoEdges = new double[nBand];
		bandHiEdges = new double[nBand];
		bandNames = new String[nBand];
		double fCent = referenceFreq * Math.pow(bandRatio, minB);
		double[] f = new double[2]; // for formatting band name. 
		for (int i = 0; i < nBand; i++) {
			bandCentres[i] = fCent;
			bandLoEdges[i] = fCent/halfBand;
			bandHiEdges[i] = fCent*halfBand;	
			f[0] = bandLoEdges[i];
			f[1] = bandHiEdges[i];
			bandNames[i] = FrequencyFormat.formatFrequencyRange(f, true);
			fCent *= bandRatio;
		}
	}
	
	private int getMaxBand() {
		double f = referenceFreq;
		int ind = 0;
		// go down a bit if necessary. Will happen if nyquist < 1000
		while (f > maxFreq/halfBand) {
			f /= bandRatio;
			ind--;
		}
		while (f <= maxFreq/halfBand/bandRatio) {
			f *= bandRatio;
			ind++;
		}
		return ind;
	}

	private int getMinBand() {
		double f = referenceFreq;
		int ind = 0;
		// go up a bit if necessary. Will happen if min freq > 1000
		while (f < minFreq*halfBand) {
			f *= bandRatio;
			ind++;
		}
		while(f >= minFreq*halfBand*bandRatio) {
			f /= bandRatio;
			ind--;
		}
		return ind;
	}
	static public double calcCentreFreq(int bandNumber) {
		return calcCentreFreq(bandNumber, BandType.THIRDOCTAVE, 1000);
	}
	/**
	 * Get a frequency for the centre of a ANSI standard band. 
	 * Band 30 is 1000Hz, and numbers are then 1/3 octaves either
	 * side of this. This means that we don't quite hit decadal 
	 * values which is a possibility for parts of the standard. 
	 * @param bandNumber
	 * @return
	 */
	static public double calcCentreFreq(int bandNumber, BandType bandType, double fNom) {
		double step = bandType.getBandRatio();
		return Math.pow(step, ((double)bandNumber-30.))*fNom;
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
	
	/**
	 * Half width of a band = sqrt of the distance between
	 * band centres. 
	 * @param bandType
	 * @return
	 */
	static public double getBandHalfWidth(BandType bandType) {
		return Math.sqrt(bandType.getBandRatio());
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
