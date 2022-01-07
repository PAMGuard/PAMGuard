package noiseBandMonitor;

import Filters.FilterMethod;

/**
 * Contains data on the performance of a single filter band. 
 * @author Doug Gillespie
 *
 */
public class BandPerformance {

	private FilterMethod filterMethod;
	private double totOutput, totOutput2;
	private double[] frequencyList;
	private double[] gainList;
	private double[] gainListdB;
	private int nPointsPerBand;
	
	/**
	 * @param frequencyList 
	 * @param filterMethod
	 */
	public BandPerformance(int nPointsPerBand, double[] frequencyList, FilterMethod filterMethod) {
		super();
		this.nPointsPerBand = nPointsPerBand;
		this.frequencyList = frequencyList;
		this.filterMethod = filterMethod;
		gainList = new double[frequencyList.length];
		gainListdB = new double[frequencyList.length];
	}

	public void addData(int iF, double output) {
		totOutput += output;
		totOutput2 += (output*output);
		gainList[iF] = output;
		gainListdB[iF] = 20.*Math.log10(output);
	}
	
	/**
	 * 
	 * @return the normalised effective bandwidth. 
	 */
	public double getNormalisedEffectiveBandwidth() {
		return totOutput2 / nPointsPerBand;
	}
	
	/**
	 * 
	 * @return the filter integrated response in dB
	 */
	public double getFilterIntegratedResponse() {
		return 10.*Math.log10(getNormalisedEffectiveBandwidth());
	}

	/**
	 * @return the filterMethod
	 */
	public FilterMethod getFilterMethod() {
		return filterMethod;
	}

	/**
	 * @return the totOutput
	 */
	public double getTotOutput() {
		return totOutput;
	}

	/**
	 * @return the totOutput2
	 */
	public double getTotOutput2() {
		return totOutput2;
	}

	/**
	 * @return the frequencyList
	 */
	public double[] getFrequencyList() {
		return frequencyList;
	}

	/**
	 * @return the gainList
	 */
	public double[] getGainList() {
		return gainList;
	}

	/**
	 * @return the nPointsPerBand
	 */
	public int getnPointsPerBand() {
		return nPointsPerBand;
	}

	public double[] getGainListdB() {
		return gainListdB;
	}

}
