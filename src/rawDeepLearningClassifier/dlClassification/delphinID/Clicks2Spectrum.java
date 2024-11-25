package rawDeepLearningClassifier.dlClassification.delphinID;

import java.util.ArrayList;

import org.jamdev.jpamutils.spectrum.Spectrum;

import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;


/**
 * Convert a group of clicks to an average spectrum. 
 */
public class Clicks2Spectrum {
	
	private Spectrum spectrum;


	public Clicks2Spectrum(SegmenterDetectionGroup clickGroup, double[] freqLimits, double fftLen) {
		this.spectrum = clicks2Spectrum(clickGroup, freqLimits, fftLen);
	}
	
	

	/**
	 * Convert whistles to a spectrum. 
	 * @param whistleValues - whistle values. 
	 * @param startseg - the start segment in seconds.
	 * @param seglen - the segment length in seconds. 
	 * @param freqLimits - the frequency limits for the spectrum. 
	 * @param minFragSize - the minimum fragment length in seconds. 
	 * @return the average spectrum. 
	 */
	public Spectrum clicks2Spectrum(SegmenterDetectionGroup clickGroup, double[] freqLimits, double fftLen) {


		//create an average spectrum
		
		
		
		
		Spectrum spectrum = new Spectrum(spectrumD, null, clickGroup.getSampleRate());

		return spectrum; 
	}

	

}
