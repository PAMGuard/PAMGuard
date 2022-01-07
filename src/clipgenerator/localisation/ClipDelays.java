package clipgenerator.localisation;

import Localiser.algorithms.Correlations;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import clipgenerator.ClipControl;
import clipgenerator.ClipDataUnit;
import fftManager.Complex;

/**
 * Class for calculating clip delays when clip has 
 * multiple channels of data. 
 * Keep as simple as possible so this code can eventually
 * move to becoming a more general localiser. 
 * @author Doug Gillespie
 *
 */
public class ClipDelays {

	private ClipControl clipControl;
	
	private SpectrogramCorrelator spectrogramCorrelator = new SpectrogramCorrelator();

	public ClipDelays(ClipControl clipControl) {
		this.clipControl = clipControl;
	}

	/**
	 * Calculate all the delays from a clip. 
	 * @param clipDataUnit
	 * @return an array of delays between the different channels. 
	 */
	public double[] getDelays(ClipDataUnit clipDataUnit) {
		int nChan = PamUtils.PamUtils.getNumChannels(clipDataUnit.getChannelBitmap());
		if (nChan <= 1) {
			return null;
		}
		/**
		 * Check the hydrophone spacings to get the maximum delay
		 */
		int hydrophoneMap = clipDataUnit.getChannelBitmap();
		try {
			PamRawDataBlock rawDataBlock = clipDataUnit.getParentDataBlock().getRawSourceDataBlock();
			if (rawDataBlock != null) {
				hydrophoneMap = rawDataBlock.getChannelListManager().channelIndexesToPhones(hydrophoneMap);
			}
		}
		catch (Exception e) {	}
		float sampleRate = clipControl.getClipProcess().getSampleRate();
		double[] maxDelays =spectrogramCorrelator.correlations.getMaxDelays(sampleRate, 
				hydrophoneMap, clipDataUnit.getTimeMilliseconds());
	
		int fftLength = 512;
		int fftHop = fftLength/2;
		StashedSpectrum[] stashedSpectra = new StashedSpectrum[nChan];
		for (int i = 0; i < nChan; i++) {
			stashedSpectra[i] = new StashedSpectrum(i, clipDataUnit.generateComplexSpectrogram(i, fftLength, fftHop));
		}
		int nDelays = ((nChan-1) * nChan) / 2;
		double[] delays = new double[nDelays];
		int iDelay = 0;
		
		/**
		 * Limit bearing analysis to frequency range of the block. 
		 */
		int[] analBins = {0, fftLength/2};
		double[] fRange = clipDataUnit.getFrequency();
		if (fRange != null) for (int i = 0; i < 2; i++) {
			analBins[i] = (int) Math.round(fRange[i] * fftLength / sampleRate);
			analBins[i] = Math.max(0, Math.min(fftLength/2, analBins[i]));
		}
		if (analBins[1] == 0) analBins[1] = fftLength/2;
		
		
		for (int i = 0; i < nChan; i++) {
			for (int j = i+1; j < nChan; j++) {
				double[] d = spectrogramCorrelator.getDelay(stashedSpectra[i].specData, stashedSpectra[j].specData, 
						maxDelays[iDelay], analBins);
				if (d == null) {
					return null;
				}
				delays[iDelay++] = d[0];
//				System.out.println(String.format("Clip delay channels %d and %d = %3.2f samples, height %3.2f",
//						i, j, d[0], d[1]));
			}
		}
		
		return delays;
	}
	
	private class StashedSpectrum {
		Complex[][] specData;
		int iChan;
		public StashedSpectrum(int iChan, Complex[][] specData) {
			super();
			this.iChan = iChan;
			this.specData = specData;
		}
		
	}
	
}
