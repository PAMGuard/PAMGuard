package cepstrum;

import PamUtils.complex.ComplexArray;
import fftManager.FFTDataUnit;

public class CepstrumDataUnit extends FFTDataUnit {

	public CepstrumDataUnit(long timeMilliseconds, int channelBitmap, long startSample, long duration,
			ComplexArray fftData, int fftSlice) {
		super(timeMilliseconds, channelBitmap, startSample, duration, fftData, fftSlice);
		// TODO Auto-generated constructor stub
	}

}
