package noiseBandMonitor;

import java.util.ArrayList;

import Filters.FilterMethod;

/**
 * Class to assess the performance of a bank of filters and decimators. 
 * @author Doug Gillespie
 *
 */
public class BandAnalyser {

	private ArrayList<DecimatorMethod> decimationFilters;
	private ArrayList<FilterMethod> bandFilters;
	private double topSampleRate;
	private BandPerformance[] bandPerformances;
	private int[] decimatorIndexes;
//	private NoiseBandControl noiseBandControl;
	NoiseBandSettings noiseBandSettings;
	/**
	 * @param topSampleRate
	 * @param decimatorFilters
	 * @param bandFilters
	 */
	public BandAnalyser(NoiseBandControl noiseBandControl, double topSampleRate, NoiseBandSettings noiseBandSettings) {
		super();
//		this.noiseBandControl = noiseBandControl;
		this.noiseBandSettings = noiseBandSettings;
		this.topSampleRate = topSampleRate;
		this.decimationFilters = noiseBandControl.makeDecimatorFilters(noiseBandSettings, topSampleRate);
		this.bandFilters = noiseBandControl.makeBandFilters(noiseBandSettings, decimationFilters, topSampleRate);
		decimatorIndexes = noiseBandControl.getDecimatorIndexes();
	}
	
	public BandPerformance[] calculatePerformance() {
		if (bandFilters == null) {
			return null;
		}
		bandPerformances = new BandPerformance[bandFilters.size()];
		double lowestFreq = bandFilters.get(bandFilters.size()-1).getFilterParams().getCenterFreq() / 8.;
		double highestFreq = topSampleRate / 2;
		double lowestFreq2 = Math.log(lowestFreq) / Math.log(2.);
		double highestFreq2 = Math.log(highestFreq) / Math.log(2.);
		int nPointsPerOctave = 300;
		int nPointsPerBand = nPointsPerOctave;
		double logStep = 1./(double)(nPointsPerOctave);
		double bandSize = noiseBandSettings.bandType.getBandRatio();
		nPointsPerBand /= (Math.log(2.)/Math.log(bandSize));
//		if (noiseBandSettings.bandType == BandType.THIRDOCTAVE) {
//			nPointsPerBand /= 3.;
//		}
//		if (noiseBandSettings.bandType == BandType.TENTHOCTAVE) {
//			nPointsPerBand /= 10.;
//		}
		double f;
		double sampleRate = topSampleRate;
		double omega;
		double[] decimatorOutput = new double[decimationFilters.size()+1]; 
		double[] decimatorConstants = new double[decimationFilters.size() + 1];
		double[] bandConstants = new double[bandFilters.size()];
		double[] decimatedFrequency = new double[decimationFilters.size() + 1];
		double[] frequencyList;
		double input, output;
		decimatorConstants[0] = 1;
		for (int i = 0; i < decimationFilters.size(); i++) {
			decimatorConstants[i+1] = decimationFilters.get(i).getFilterMethod().getFilterGainConstant();
		}
		int nFreqs = (int) ((highestFreq2-lowestFreq2) / logStep) + 1;
		frequencyList = new double[nFreqs];
		int iF = 0;
		for (double logf = lowestFreq2; logf <= highestFreq2; logf+=logStep) {
			f = Math.pow(2, logf);
			if (iF >= frequencyList.length) {
				break;
			}
			frequencyList[iF++] = f;
		}
		for (int i = 0; i < bandFilters.size(); i++) {
			bandConstants[i] = bandFilters.get(i).getFilterGainConstant();
			bandPerformances[i] = new BandPerformance(nPointsPerBand, frequencyList, bandFilters.get(i));
		}
		for (iF = 0; iF < nFreqs; iF++) {
			f = frequencyList[iF];
			decimatedFrequency[0] = f;
			decimatorOutput[0] = 1;
			sampleRate = topSampleRate;
			for (int d = 0; d < decimationFilters.size(); d++) {
				omega = Math.PI*2.*f/sampleRate;
				decimatorOutput[d+1] = decimatorOutput[d] * 
					decimationFilters.get(d).getFilterMethod().getFilterGain(omega) /
					decimatorConstants[d+1];  
//				for (int b = 0; b < bandFilters.size(); b++) {
//					if ()
//				}
				
				
				f = getDecimatedFreq(f, sampleRate);
				sampleRate = decimationFilters.get(d).getOutputSampleRate();
				decimatedFrequency[d+1] = f;
			}
			for (int b = 0; b < bandFilters.size(); b++) {
				input = decimatorOutput[decimatorIndexes[b]+1];
				f = decimatedFrequency[decimatorIndexes[b]+1]; // signal frequency after decimation and aliassing. 
				FilterMethod bandFilter = bandFilters.get(b);
				sampleRate = bandFilter.getSampleRate(); // input sample rate of filter. 
				omega = Math.PI*2.*f/sampleRate;
				output = input*bandFilter.getFilterGain(omega)/bandConstants[b];
				bandPerformances[b].addData(iF, output);
			}
		}
		return bandPerformances;
	}

	/**
	 * Work out what this frequency is going to turn into after
	 * div 2. decimation. 
	 * @param f signal frequency
	 * @param sampleRate sample rate prior to decimation. 
	 * @return aliased frequency. 
	 */
	private double getDecimatedFreq(double f, double sampleRate) {
		double newNiquist = sampleRate / 4;
//		if (f < newNiquist) {
//			return f; // below new niquist. 
//		}
		while (f < 0 || f > newNiquist) {
			if (f < 0) {
				f = -f;
			}
			else {
				f = newNiquist - (f-newNiquist);
			}
		}
		
		return f;
	}

	/**
	 * @return the decimationFilters
	 */
	public ArrayList<DecimatorMethod> getDecimationFilters() {
		return decimationFilters;
	}

	/**
	 * @return the bandFilters
	 */
	public ArrayList<FilterMethod> getBandFilters() {
		return bandFilters;
	}

	/**
	 * @return the bandPerformances
	 */
	public BandPerformance[] getBandPerformances() {
		return bandPerformances;
	}
	
}
