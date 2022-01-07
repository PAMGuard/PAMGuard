package PamDetection;

import fftManager.Complex;

/**
 * Class to provide clip functions to a pam data unit. 
 * Initially written to provide functions to the click detector, but
 * I split it off into a separate class so that the functions can be
 * more easily extended to other detection types if necessary. 
 * @author Doug Gillespie
 *
 */
public class WaveClipFunctions {

	private int channelMap;
	
	private double[][] waveData;
	
	private byte[][] compressedWaveData;

	private double[][] powerSpectra;

	private double[] totalPowerSpectrum;

	private Complex[][] complexSpectrum;
	
	private Complex[][] filteredComplexSpectrum;
	
	private int currentSpectrumLength = 0;
	
	private double[][] analyticWaveform;
}
