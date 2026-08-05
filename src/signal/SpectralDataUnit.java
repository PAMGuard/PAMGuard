package signal;

import PamUtils.complex.ComplexArray;
import Spectrogram.WindowFunction;
import fftManager.FFTDataUnit;
import fftManager.FastFFT;

public class SpectralDataUnit extends FFTDataUnit{
	
	/**
	 * The binwidth of an FFT bin in Hz
	 */
	private double df;
	private float fs;
	private int N;
	
	
	private double[] fDomain;
	private double[] magnitude;
	private double[] magSquared;
	
	double[] window;
	
	public SpectralDataUnit(double[] waveform, int fftLength, float fs, double[] windowFunction) {
		this(System.currentTimeMillis(), 0, 0, waveform.length, null, 0, fs);
		this.window = windowFunction;
		ComplexArray fftData = fft(waveform, fftLength);
		this.setFftData(fftData);
	}

	public SpectralDataUnit(long timeMilliseconds, int channelBitmap, long startSample, long duration, ComplexArray fftData, int fftSlice, float fs) {
		super(timeMilliseconds, channelBitmap, startSample, duration, fftData, fftSlice);
		this.setFftData(fftData);
		this.fs = fs;
	}
	
	@Override
	public void setFftData(ComplexArray fftData) {
		super.setFftData(fftData);
		if(fftData==null) {
			return;
		}
		this.N = fftData.length();
		
		this.df = (fs/2.0)/(double)N;
		System.out.println(fs+" "+N+" "+df);
		
	}
	
	public double[] getFrequencyDomain() {
		if(fDomain==null) calculateFDomain();
		return fDomain;
	}
	
	
	private double[] psd;
	private AmplitudeDomain.Form psdForm;
	private AmplitudeDomain.Unit psdUnit;
	
	public double[] PSD(AmplitudeDomain.Form form, AmplitudeDomain.Unit unit) {
		if(psd==null) calcPSD();
		return psd;
	}
	
	public double[] magnitude(AmplitudeDomain.Form form, AmplitudeDomain.Unit unit) {
		if(magnitude==null) calculateMagnitude();
		return magnitude;
	}
	
	public double[] power(AmplitudeDomain.Form form, AmplitudeDomain.Unit unit) {
		if(magSquared==null) calculateMagSquared();
		return magSquared;
	}
	
	private void calcPSD() {
		if (magSquared==null) calculateMagSquared();
		psd = new double[N];
		for(int i=0;i<N;i++) {
			double sxx = magSquared[i];
			psd[i] = sxx/df;
		}
	}
	
	private void calculateMagnitude() {
		magnitude = this.getFftData().mag();
	}
	
	private void calculateMagSquared() {
		magSquared = this.getFftData().magsq();
	}
	
	private void calculateFDomain() {
		double[] fDomain = new double[N];
		System.out.println();
		for(int i=0;i<N;i++) {
			fDomain[i] = (i+1)*df;
			System.out.print(fDomain[i]+" ");
		}
		System.out.println();

		this.fDomain = fDomain;
	}
	
	
	/**
	 * Simplified version from pamguard's RawDataTransform.getComplexSpectrum(int channel, int fftLength)
	 * @param waveform
	 * @param fftLength
	 * @return
	 */
	private ComplexArray fft(double[] waveform, int fftLength) {
		FastFFT fastFFT = new FastFFT();
		double[] paddedRawData;
		double[] rawData = waveform;
		int i, mn;

		paddedRawData = new double[fftLength];

		mn = Math.min(fftLength, rawData.length);

		for (i = 0; i < mn; i++) {
			paddedRawData[i] = rawData[i];//-rotData[i];
		}
		for (i = mn; i < fftLength; i++) {
			paddedRawData[i] = 0;
		}
		
		double[] windowedData = windowData(paddedRawData,fftLength);
		
		ComplexArray transform = fastFFT.rfft(windowedData, fftLength);
						
		return transform;
	
	}
	
	private double[] windowData(double[] waveform, int fftLength) {
		double[] windowedData = new double[fftLength];
		for (int w = 0; w < fftLength; w++) {
			if(w>=waveform.length) {
				windowedData[w] = 0 * window[w];
			}else {
				windowedData[w] = waveform[w] * window[w];
			}
		}
		return windowedData;
	}

	public double getBinWidth() {
		return df;
	}

	public float getSampleRate() {
		return fs;
	}

}
