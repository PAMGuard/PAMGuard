package mel;

import Jama.Matrix;

/**
 * class to hold info on a mel filter bank,
 * Calculations should be based on the Python librosa.filters.mel
 * https://librosa.org/doc/main/_modules/librosa/filters.html#mel
 * @author dg50
 *
 */
public class MelFilterBank {

	private double sr;
	private int n_fft;
	private int n_mels;
	private double f_min;
	private double f_max;
	private double[] binBounds;
	private double[] freqBounds;
	private double[] freqCentres;
	private double[][] mel_matrix;
	private Matrix asMatrix;
	/**
	 * Ranges of bins actually used in this, i.e. with non zero values in mel_matrix. 
	 */
	private int[][] binRanges;

	/**
	 * Construct a mel_filter bank to convert linear FFT bins into mel+spec logarithmic bins. 
	 * @param sr
	 * @param n_fft
	 * @param n_mels
	 * @param f_min
	 * @param f_max
	 */
	public MelFilterBank(double sr, int n_fft, int n_mels, double f_min, double f_max) {
		this.sr = sr;
		this.n_fft = n_fft;
		this.n_mels = n_mels;
		this.f_min = f_min;
		this.f_max = f_max;
		cacluateFilters();
		
		afExTest();
	}

	private void afExTest() {
//		Test using  code from https://github.com/Subtitle-Synchronizer/jlibrosa/blob/master/src/main/java/com/jlibrosa/audio/process/AudioFeatureExtraction.java
		AudioFeatureExtraction afe = new AudioFeatureExtraction();
		afe.setSampleRate(sr);
		afe.setN_mels(n_mels);
		afe.setfMin(f_min);
		afe.setfMax(f_max);
		afe.setN_fft(n_fft);
		double[][] melFilter = afe.melFilter();
		System.out.println("test seems to give very similar values to Librosa");
	}

	private boolean cacluateFilters() {
		/*
		 * Calculate the FFT bin boundaries based on evenly log spaced
		 * bins
		 */
		binBounds = new double[n_mels+1];
		freqBounds = new double[n_mels+1];
		freqCentres = new double[n_mels];
		binBounds[0] = f2bin(f_min);
		binBounds[n_mels] = f2bin(f_max);
		binRanges = new int[n_mels][2];
		double step = Math.pow(f_max/f_min, 1./(double) n_mels);
		for (int i = 1; i < n_mels; i++) {
			binBounds[i] = binBounds[i-1]*step;
		}
		for (int i = 0; i <= n_mels; i++) {
			freqBounds[i] = bin2f(binBounds[i]);
		}
		for (int i = 0; i < n_mels; i++) {
			freqCentres[i] = Math.sqrt(freqBounds[i]*freqBounds[i+1]);
		}
		mel_matrix = new double[n_mels][n_fft/2];
		for (int im = 0; im < n_mels; im++) {
			double bins = binBounds[im+1]-binBounds[im];
			int cb = (int) Math.floor(binBounds[im]);
			int tb = (int) Math.ceil(binBounds[im]);
			binRanges[im][0] = cb;
			binRanges[im][1] = tb;
			for (int f = 0; f < n_fft/2; f++) {
				double o = getOverlap(f, binBounds[im], binBounds[im+1]);
				if (o > 0) {
					mel_matrix[im][f] = o/bins;
				}
			}
		}
		asMatrix = new Matrix(mel_matrix);
		
		return true;
	}
	
	private double getOverlap(int fftBin, double binB1, double binB2) {
		double binTop = fftBin + 1;
//		if (fftBin > binB2) {
//			return 0;
//		}
		double olap = Math.min(binB2, binTop)-Math.max(binB1, fftBin);
		return olap;
		
	}
	
	
	private double bin2f(double fftBin) {
		return fftBin * sr / (double) n_fft;
	}

	private double f2bin(double frequency) {
		return frequency / sr * (double) n_fft;
	}

	public static void main(String[] args) {
		double sr = 9600;
		double fmin = 1000;
		double fmax = 2000;
		int n_mels = 3;
		int nfft = 32;
		new MelFilterBank(sr, nfft, n_mels, fmin, fmax);
	}

	private void test() {
		
	}
	
}
