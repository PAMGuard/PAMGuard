package beamformer.continuous;

import PamUtils.complex.ComplexArray;
import fftManager.FFTDataUnit;

public class BeamFormerDataUnit extends FFTDataUnit {

	public BeamFormerDataUnit(long timeMilliseconds, int channelBitmap, int sequenceBitmap, long startSample, long duration,
			ComplexArray fftData, int fftSlice) {
		super(timeMilliseconds, channelBitmap, startSample, duration, fftData, fftSlice);
		setSequenceBitmap(sequenceBitmap);
	}

}
