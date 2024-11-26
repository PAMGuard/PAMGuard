package rawDeepLearningClassifier.dlClassification.delphinID;

import org.jamdev.jpamutils.spectrum.Spectrum;

import PamUtils.PamArrayUtils;
import PamguardMVC.RawDataHolder;
import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;


/**
 * Convert a group of clicks to an average spectrum. 
 */
public class Clicks2Spectrum {
	
	private Spectrum spectrum;


	public Clicks2Spectrum(SegmenterDetectionGroup clickGroup, double[] freqLimits, int fftLen) {
		this.spectrum = clicks2Spectrum(clickGroup, freqLimits, fftLen);
	}
	
	

	public Clicks2Spectrum(SegmenterDetectionGroup clickGroup, Clks2SpectrumParams transformParams) {
		this(clickGroup, transformParams.freqLimits, transformParams.fftLength); 
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
	public Spectrum clicks2Spectrum(SegmenterDetectionGroup clickGroup, double[] freqLimits, int fftLen) {


		//create an average spectrum
		double[] fftAverage = new double[(int) (fftLen/2)]; 
		double[] fftClk;
		for (int i=0;i<clickGroup.getSubDetectionsCount(); i++) {
			fftClk = ((RawDataHolder) clickGroup.getSubDetection(i)).getDataTransforms().getPowerSpectrum(0, fftLen); 
		
			fftAverage = PamArrayUtils.sum(fftAverage, fftClk); 
		}
		
		fftAverage = PamArrayUtils.divide(fftAverage, clickGroup.getSubDetectionsCount());
		
		Spectrum spectrum = new Spectrum(fftAverage, null, clickGroup.getSampleRate());

		return spectrum; 
	}


	public Spectrum getSpectrum() {
		return spectrum;
	}
	
	
	public static class Clks2SpectrumParams extends DetectionGroupTransformParams {
		
		
		private int fftLength;
		/**
		 * Set the FFT length in samples
		 * @param fftLength
		 */
		public void setFftLength(int fftLength) {
			this.fftLength = fftLength;
		}

		/**
		 * Get the frequency limits in samples
		 * @return
		 */
		public int getFFTLength() {
			return fftLength;
		}
		
	}

	

}
