package PamguardMVC.toad;

import PamguardMVC.PamDataUnit;
import fftFilter.FFTFilter;
import fftFilter.FFTFilterParams;

public class FixedTOADFFTFilter<T  extends PamDataUnit<?,?>> extends TOADFFTFilter<T> {

//	private FFTFilter fftFilter;
	private FFTFilterParams fftFilterParams;
	
	private int[] usedBins = null;
	
	private int[] zeroedBins = null;
			
	public FixedTOADFFTFilter(FFTFilterParams fftFilterParams, float sampleRate) {
//		fftFilter = new FFTFilter(fftFilterParams, sampleRate);
		setFFTFilterParams(fftFilterParams, sampleRate);
	}
	
	@Override
	public int[] getUsedFFTBins(T dataUnit, int sampleInSound, float sampleRate, int fftLength) {
		if (fftFilterParams == null) {
			return null;
		}
		// TODO Auto-generated method stub
		return usedBins;
	}
	
	public int[] getZeroedBins(T dataUnit, int sampleInSound, float sampleRate, int fftLength) {
		return zeroedBins;
	}

	/**
	 * @return the fftFilterParams
	 */
	public FFTFilterParams getFFTFilterParams() {
		return fftFilterParams;
	}

	/**
	 * @param fftFilterParams the fftFilterParams to set
	 */
	public void setFFTFilterParams(FFTFilterParams fftFilterParams, float sampleRate) {
		this.fftFilterParams = fftFilterParams;
		usedBins = null;
		zeroedBins = null;
		if (this.fftFilterParams == null) {
			return;
		}
		switch (fftFilterParams.filterBand) {
		case BANDPASS:
			break;
		case BANDSTOP:
			break;
		case HIGHPASS:
			break;
		case LOWPASS:
			break;
		default:
			break;
		
		}
	}

}
