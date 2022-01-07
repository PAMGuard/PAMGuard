package Spectrogram;

import fftManager.FFTDataBlock;

public interface SpectrogramParametersUser {

	public SpectrogramParameters getSpectrogramParameters();
	
	public void setSpectrogramParameters(SpectrogramParameters spectrogramParameters);
	
	public FFTDataBlock getFFTDataBlock();
}
