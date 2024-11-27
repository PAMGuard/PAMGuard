package rawDeepLearningClassifier.dlClassification.delphinID;

import java.util.ArrayList;

import org.jamdev.jpamutils.spectrum.Spectrum;

import PamUtils.PamArrayUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import PamguardMVC.RawDataTransforms;
import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;


/**
 * Convert a group of clicks to an average spectrum. 
 */
public class Clicks2Spectrum {
	
	private Spectrum spectrum;


	public Clicks2Spectrum(SegmenterDetectionGroup clickGroup, int fftLen) {
		this.spectrum = clicks2Spectrum(clickGroup.getSubDetections(), clickGroup.getSampleRate(), fftLen);
	}
	
	

	public Clicks2Spectrum(SegmenterDetectionGroup clickGroup, Clks2SpectrumParams transformParams) {
		this(clickGroup, transformParams.fftLength); 
	}



	/**
	 * Convert clicks to an average spectrum. 
	 * @param whistleValues - whistle values. 
	 * @param startseg - the start segment in seconds.
	 * @param seglen - the segment length in seconds. 
	 * @param freqLimits - the frequency limits for the spectrum. 
	 * @param minFragSize - the minimum fragment length in seconds. 
	 * @return the average spectrum. 
	 */
	public static Spectrum clicks2Spectrum(ArrayList<? extends PamDataUnit> arrayList, float sampleRate, int fftLen) {


		//create an average spectrum
		double[] fftAverage = new double[(int) (fftLen/2)]; 
		double[] fftClk;
		for (int i=0;i<arrayList.size(); i++) {
			
			fftClk = ((RawDataHolder) arrayList.get(i)).getDataTransforms().getPowerSpectrum(0, fftLen); 
			
//			 ComplexArray arr = RawDataTransforms.getComplexSpectrumHann(((RawDataHolder) arrayList.get(i)).getWaveData()[0], fftLen); 
			
//			PamArrayUtils.printArray(arr.getReal()); 

			fftAverage = PamArrayUtils.sum(fftAverage, fftClk); 
		}
		
		fftAverage = PamArrayUtils.divide(fftAverage, arrayList.size());
		
		Spectrum spectrum = new Spectrum(fftAverage, null, sampleRate);


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
