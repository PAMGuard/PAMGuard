package signal.snr;

import PamUtils.FrequencyFormat;

public class SNRData {

	private double snr; // signal to noise
	private double f0; // peak frequency
	private double durationS; // call duration
	private double bandwidthHz; // call bandwidth. 
	private double[] frequencySearch; // frequency search range (optional)
	
	/**
	 * @param snr
	 * @param f0
	 * @param durationS
	 * @param bandwidthHz
	 * @param frequencySearch
	 */
	public SNRData(double snr, double f0, double durationS, double bandwidthHz, double[] frequencySearch) {
		super();
		this.snr = snr;
		this.f0 = f0;
		this.durationS = durationS;
		this.bandwidthHz = bandwidthHz;
		this.frequencySearch = frequencySearch;
	}

	/**
	 * @param snr
	 * @param f0
	 * @param durationS
	 * @param bandwidthHz
	 * @param frequencySearch
	 */
	public SNRData(double snr, double f0, double durationS, double bandwidthHz) {
		super();
		this.snr = snr;
		this.f0 = f0;
		this.durationS = durationS;
		this.bandwidthHz = bandwidthHz;		this.frequencySearch = frequencySearch;
	}

	/**
	 * Get SNR as a power ratio (i.e. NOT in decibels). 
	 * @return the snr
	 */
	public double getSnr() {
		return snr;
	}

	/**
	 * Peak frequency of power spectrum
	 * @return the f0
	 */
	public double getF0() {
		return f0;
	}

	/**
	 * call duration in seconds. 
	 * @return the durationS
	 */
	public double getDurationS() {
		return durationS;
	}

	/**
	 * call bandwidth in Hz
	 * @return the bandwidthHz
	 */
	public double getBandwidthHz() {
		return bandwidthHz;
	}

	/**
	 * Search range used in parameter estimation. 
	 * @return the frequencySearch
	 */
	public double[] getFrequencySearch() {
		return frequencySearch;
	}

	/**
	 * Get the Cramer-Rao lower bound based on the fundamental frequency, SNR and time-bandwidth product.  
	 * @return
	 */
	public double getCRLB() {
		return 1./Math.sqrt(2*getDurationS()*getBandwidthHz()*getSnr()) / (2*Math.PI*getF0());
	}

	@Override
	public String toString() {
		return String.format("SNR=%3.1fdB, dT=%3.2fms. dF=%s, f0=%s, CRLB=%3.4f", 10.*Math.log10(snr), durationS*1000, 
				FrequencyFormat.formatFrequency(bandwidthHz, true),FrequencyFormat.formatFrequency(f0, true), getCRLB());
	}


}
