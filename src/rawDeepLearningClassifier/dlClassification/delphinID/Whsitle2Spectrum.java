package rawDeepLearningClassifier.dlClassification.delphinID;

import java.util.ArrayList;

import org.jamdev.jpamutils.spectrum.Spectrum;

import rawDeepLearningClassifier.dlClassification.delphinID.Whistles2Image.Whistle2ImageParams;
import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;


/**
 * Functions to convert a group fo whistle contours to an average spectrum. 
 */
public class Whsitle2Spectrum {
	
	private Spectrum spectrum;


	public Whsitle2Spectrum(SegmenterDetectionGroup whistleGroups, double[] freqLimits, double minFragSize) {
		this.spectrum = whistle2Spectrum(whistleGroups, freqLimits,  minFragSize);
	}


	public Whsitle2Spectrum(SegmenterDetectionGroup whistleGroups, Whistle2spectrumParams transformParams) {
		this.spectrum = whistle2Spectrum(whistleGroups, transformParams.getFreqLimits(),  transformParams.minFragSize);

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
	public Spectrum whistle2Spectrum(SegmenterDetectionGroup whistleGroups, double[] freqLimits, double minFragSize) {

		//convert whistle contrours to time frequency points. 
		ArrayList<double[][]> points = Whistles2Image.whistContours2Points(whistleGroups, minFragSize);

		//concatonate the contours into a singe aray
		double[][] tfCntours = concatWhistleContours(points);

		double[] spectrumD  = whistle2AverageArray(tfCntours,  0,  whistleGroups.getSegmentDuration(), freqLimits);

		Spectrum spectrum = new Spectrum(spectrumD, whistleGroups.getFrequency(), whistleGroups.getParentDataBlock().getSampleRate());

		return spectrum; 
	}

	
	/**
	 * Convert an ArrayList of individual contours to a single list of time and frequency values of all contours. 
	 * @param points - individual and moan contours with each point time (seconds) and frequency (Hz).
	 * @return the concatonated whistle and moan contours with each point time (seconds) and frequency (Hz).
	 */
	public  static double[][] concatWhistleContours(	ArrayList<double[][]> points){
		int n=0;
		for (int i=0; i<points.size(); i++) {
			for (int j=0; j<points.get(i).length; j++) {
				n++;
			}
		}
		
		double[][] doubleArr = new double[n][2]; 
		
		n=0;
		for (int i=0; i<points.size(); i++) {
			for (int j=0; j<points.get(i).length; j++) {

				doubleArr[n][0] = points.get(i)[j][0];
				doubleArr[n][1] = points.get(i)[j][1];
			}
		}


		return doubleArr; 
	}



	/**
	 * Convert whistle contours to an average normalised spectrum
	 * @param whistleValues - the whistle contour values (Column 1 is time in seconds and column 2 is frequency).
	 * @param startseg - the start segment in seconds.
	 * @param seglen - the segment length in seconds. 
	 * @param freqLimits - the frequency limits for the spectrum. 
	 * @return the average spectrum. 
	 */
	public static double[] whistle2AverageArray(double[][] whistleValues, double startseg, double seglen, double[] freqLimits) {
		//now perform the image transform in Java 
		//		double[] freqLimits = new double[] {2000., 20000.};

		double freqBin = 100.;

		int nbins = (int) ((freqLimits[1] -freqLimits[0])/freqBin);

		System.out.println("No. of bins: " + nbins + "no. of whistle points: " + whistleValues.length);

		double[] peakBins = new double[nbins];
		double minFreq, maxFreq;
		int n;
		int ntot = 0;
		for (int i=0; i<nbins; i++) {

			minFreq = i*freqBin+freqLimits[0];
			maxFreq = (i+1)*freqBin+freqLimits[0];

			n=0;
			for (int j=0; j<whistleValues.length ; j++) {
				if (whistleValues[j][1]>= minFreq && whistleValues[j][1]< maxFreq && whistleValues[j][0]>=startseg && whistleValues[j][0]<(startseg+seglen)) {
					n++;
				}
			}

			ntot=ntot+n;

			System.out.println("Bin: " + minFreq + " Hz   " + n); 
			peakBins[i]=n;
		}

		if (ntot!=0) {
			for (int i=0; i<nbins; i++) {
				peakBins[i]=peakBins[i]/ntot;

			}
		}

		return peakBins;
	}
	
	

	public Spectrum getSpectrum() {
		return spectrum;
	}


	public void setSpectrum(Spectrum spectrum) {
		this.spectrum = spectrum;
	}
	
	public static class Whistle2spectrumParams extends WhistleTransformParams {
		
	
		/**
		 * Get the frequency limits in Hz
		 * @return
		 */
		public double[] getFreqLimits() {
			// TODO Auto-generated method stub
			return freqLimits;
		}
		
	}


}
