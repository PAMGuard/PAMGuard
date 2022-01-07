package difar.demux;

import java.awt.Component;
import java.awt.Window;
import java.util.Arrays;

import Filters.FilterBand;
import Filters.FilterParams;
import Filters.FilterType;
import Filters.IirfFilter;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import signal.Hilbert;
import difar.DemuxObserver;
import difar.DifarDataUnit;
import difar.DifarParameters;
import fftFilter.FFTFilter;
import fftFilter.FFTFilterParams;
import fftManager.Complex;
import fftManager.FastFFT;

/**
 * Experimental java based demultiplexer. Demultiplexing is conducted in the
 * frequency domain. This demultiplexer is suitable for postprocessing clips,
 * but is not suitable for real-time processing of each sample. Takes an audio
 * recorded from a DIFAR sonobuoy, ANSSQ 53B-D, and do the demodulation to
 * obtain the omnidirectional and two orthogonal directional signals (om ew ns).
 * 
 * NOTE: this function does demodulation, filters and then decimates the
 * demodulated output.
 * 
 * NOTE: This function is experimental, is not guaranteed to work correctly,
 * propbably uses more memory than is reasonable, doesn't work very fast,
 * and, in general, will probably eat all your data.  
 * 
 * REFERENCES Delagrange A (1992) DIFAR Multiplexer-Demultiplexer System, Latest
 * Improvements. Report for Naval Surface Warfare Center.Silver Spring, Maryland
 * 
 * @author Brian Miller 2014-04-07
 */
public class AmmcDemux extends DifarDemux {

	
	@Override
	public boolean configDemux(DifarParameters difarParams, double sampleRate) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	/**
	 * Demultiplexing in the frequency domain using FFTFilters and Hilbert transforms.
	 */
	public DifarResult processClip(double[] difarClip, double sampleRate,
			int decimationFactor, DemuxObserver observer, DifarDataUnit difarDataUnit) {

		/*
		 * Define some basic demodulation parameters such as bandwidth of omni,
		 * directional, and pilot tones
		 */
		double bandwidth = 4000;
		double phasePilot = 15000;
		double freqPilot = 7500;
		
		int n = difarClip.length;

		int nOutput = difarClip.length/decimationFactor;
		DifarResult difarResult = new DifarResult(nOutput);
		
		// 1). compute Fourier transform of the signal 
		FastFFT fFFT = new FastFFT();
		//Complex [] fftData = Complex.createComplexArray(difarClip); // Copy the difarClip before taking in-place FFT 
		Hilbert h = new Hilbert();
		int nextPow2 = FastFFT.nextBinaryExp(n);
		int m = PamUtils.log2(nextPow2);
		Complex [] fftData = fFFT.rfft(difarClip, null, m);
		double freqStep = sampleRate/nextPow2;
		
		double[] ew = new double[n];
		double[] ns = new double[n];
		double[] om = new double[n];
		double[] pp = new double[n];

		/*
		 *  First locate the frequency of the 7.5 kHz pilot tone. This should  be
		 *  easy because there should be no other signals nearby. Here we still
		 *  look for the maximum FFT value from 7.45 kHz to 7.55 kHz in order 
		 *  to account for clock/oscillator imperfections in the sonobuoy or 
		 *  recording chain.
		 */
		int lowIx, highIx;
		double lowFreq, highFreq, halfBandwidth,freqPilotMag;
		freqPilotMag = Double.MIN_VALUE;
		
		halfBandwidth = 100; //Hz
		lowFreq = freqPilot - halfBandwidth;
		highFreq = freqPilot + halfBandwidth;
		lowIx = (int) Math.floor(lowFreq/freqStep);
		highIx = (int) Math.ceil(highFreq/freqStep);
		
		for (int i=lowIx; i<= highIx; i++){
			if (fftData[i].mag() > freqPilotMag){
				freqPilotMag = fftData[i].mag();
				freqPilot = i*freqStep;
			}
		}

		/*
		 *  The 15 kHz pilot is the one used for demodulation, but is more difficult
		 * to locate due to the adjacent modulated directional signals. Fortunately 
		 * the DIFAR spec indicates that this pilot should be exactly double that of
		 * the frequency pilot.
		 */
		phasePilot = freqPilot * 2; // Hz


		// Frequency band limits for the directional signals as found in the DIFAR-modulated "audio" signal.
		double difarLowFreq  = phasePilot - bandwidth;
		double difarHighFreq = phasePilot + bandwidth;
		
		double pilotHalfBandwidth = 4; // Hz
		double pilotLow = phasePilot - pilotHalfBandwidth;
		double pilotHigh = phasePilot + pilotHalfBandwidth;
		
		/*
 		 * The phase pilot is used to demodulate the directional signals. Obtain the
		 * time-domain phase pilot by "zeroing" all other FFT bins and then taking
		 * the inverse FFT (ie. a brickwall bandpass filter in the frequency
		 * domain). Otherwise, would have to use a very steep and narrow bandpass
		 * filter.
		 */		
		FFTFilterParams diFilterParams = new FFTFilterParams(); 
		FFTFilterParams ppFilterParams = new FFTFilterParams();
		FFTFilterParams notchFilterParams = new FFTFilterParams();
		ppFilterParams.filterBand = FilterBand.BANDPASS;
		diFilterParams.filterBand = FilterBand.BANDPASS;
		notchFilterParams.filterBand = FilterBand.BANDSTOP;
		
		ppFilterParams.lowPassFreq = pilotLow;
		notchFilterParams.lowPassFreq = pilotLow;
		ppFilterParams.highPassFreq = pilotHigh;
		notchFilterParams.highPassFreq = pilotHigh;
		diFilterParams.lowPassFreq = difarLowFreq;
		diFilterParams.highPassFreq = difarHighFreq;
		
		FFTFilter diFilter = new FFTFilter(diFilterParams, (float) sampleRate);
		FFTFilter ppFilter = new FFTFilter(ppFilterParams, (float) sampleRate);
		FFTFilter notchFilter = new FFTFilter(notchFilterParams, (float) sampleRate);
		
		ppFilter.runFilter(difarClip, pp);
		diFilter.runFilter(difarClip, om);
		//Remember to exclude the phase pilot from the directional signal
		notchFilter.runFilter(om);
		
		ComplexArray PP = h.getHilbertC(pp);
		double hLen = PP.length();
		
		double ppMag = getRMS(pp)/Math.sqrt(2);
		for (int i=0; i<n; i++){ 
			ns[i] = (-PP.getImag(i) * om[i]) / (ppMag * hLen);
			ew[i] = (-PP.getReal(i) * om[i]) / (ppMag * hLen);
		}

		FilterParams filtParams = new FilterParams(); 
		filtParams.filterBand = FilterBand.LOWPASS;
		filtParams.filterOrder = 8;
		filtParams.filterType = FilterType.BUTTERWORTH;
		filtParams.lowPassFreq = (float) (sampleRate/2/decimationFactor);
		IirfFilter filters = new IirfFilter(0, sampleRate, filtParams);
		
		filters.runFilter(difarClip,om);
		filters.runFilter(ew);
		filters.runFilter(ns);
		int iOut = 0;
		for (int i = 0; i < n; i++){
			if ((i+1)%decimationFactor == 0) {
				om[iOut] = om[i];
				ew[iOut] = ew[i];
				ns[iOut] = ns[i];
				difarResult.lock_15[iOut] = true; //TODO: Remove lock code once locks are consolidated to Greeneridge demux
				difarResult.lock_75[iOut] = true;
				iOut++;
			}
		}
		
		difarResult.om = Arrays.copyOfRange(om, 0, nOutput);
		difarResult.ew = Arrays.copyOfRange(ew, 0, nOutput);
		difarResult.ns = Arrays.copyOfRange(ns, 0, nOutput);;

		return difarResult;
	}

	@Override
	public boolean hasOptions() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean showOptions(Window window, DifarParameters difarParams) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Component getDisplayComponent() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private double getRMS(double [] signal) {
		double rms = 0;
		for (int i = 0; i < signal.length; i++){
			rms += signal[i] * signal[i];
		}
		rms = Math.sqrt(rms/signal.length);
		return rms;
	}
}
