package rawDeepLearningClassifier.dlClassification.delphinID;

import java.util.ArrayList;
import java.util.Arrays;

import org.jamdev.jpamutils.JamArr;
import org.jamdev.jpamutils.spectrum.Spectrum;

import PamUtils.PamArrayUtils;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;


/**
 * Convert a group of clicks to an average spectrum. 
 */
public class Clicks2Spectrum {
	
	private Spectrum spectrum;


	public Clicks2Spectrum(SegmenterDetectionGroup clickGroup, int fftLen, boolean spectradB, boolean hann) {
		this.spectrum = clicks2Spectrum(clickGroup.getSubDetections(), clickGroup.getSampleRate(), fftLen, spectradB, hann);
	}
	
	

	public Clicks2Spectrum(SegmenterDetectionGroup clickGroup, Clks2SpectrumParams transformParams) {
		this(clickGroup, transformParams.fftLength, transformParams.spectrumdB, transformParams.hann); 
	}



	/**
	 * Convert clicks to an average spectrum. 
	 * @param whistleValues - whistle values. 
	 * @param startseg - the start segment in seconds.
	 * @param seglen - the segment length in seconds. 
	 * @param freqLimits - the frequency limits for the spectrum. 
	 * @param minFragSize - the minimum fragment length in seconds. 
	 * @param spectrumdB - true to average the log spectra instead of linear spectra
	 * @return the average spectrum. 
	 */
	public static Spectrum clicks2Spectrum(ArrayList<? extends PamDataUnit> arrayList, float sampleRate, int fftLen, boolean spectrumdB, boolean hann) {


		//create an average spectrum
		double[] fftAverage = new double[(int) (fftLen/2)]; 
		double[] fftClk;
		for (int i=0;i<arrayList.size(); i++) {
			
			fftClk = ((RawDataHolder) arrayList.get(i)).getDataTransforms().getPowerSpectrum(0, fftLen, hann); 
			
//			 ComplexArray arr = RawDataTransforms.getComplexSpectrumHann(((RawDataHolder) arrayList.get(i)).getWaveData()[0], fftLen); 
			
//			PamArrayUtils.printArray(arr.getReal()); 			
//			System.out.println("fft len: " + i + "  " + fftAverage.length + " " +  fftClk.length); 
			
			if (spectrumdB) {
				double fftClkdB = 0 ;
				
				//important otherwise we alter the power spectrum stored within the click
				fftClk = Arrays.copyOf(fftClk, fftClk.length);

				//convert to log
				for (int j=0; j<fftClk.length; j++) {
					if (spectrumdB) {
						fftClkdB=20*Math.log10(fftClk[j]); 
					}
					else {
						fftClkdB=fftClk[j]; 
					}
//					if (Double.isNaN(fftClkdB)) {
//						System.out.println("Point is NaN: " + i + "  " + j + "  " + fftClk[j]); 
//					}
					fftClk[j] = fftClkdB; 
				}
				
			}

			fftAverage = PamArrayUtils.sum(fftAverage, fftClk); 
		}
		
//		System.out.println(fftAverage + " " +  arrayList.size()); 
		
		fftAverage = PamArrayUtils.divide(fftAverage, arrayList.size());
		
		fftAverage = JamArr.subtract(fftAverage, JamArr.min(fftAverage)); 

		
		Spectrum spectrum = new Spectrum(fftAverage, null, sampleRate);

		return spectrum; 
	}


	public Spectrum getSpectrum() {
		return spectrum;
	}
	
	
	public static class Clks2SpectrumParams extends DetectionGroupTransformParams {
		
		/**
		 * The FFT length in samples. 
		 */
		private int fftLength;
		
		/**
		 * True to average log spectra instead of linear spectra
		 */
		public boolean spectrumdB = true;

		/**
		 * True to apply a hanning window to click wave before FFT. 
		 */
		public boolean hann = false;

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
