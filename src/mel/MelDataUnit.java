package mel;

import PamUtils.complex.ComplexArray;
import fftManager.FFTDataUnit;

public class MelDataUnit extends FFTDataUnit {
	
	private double[] melData;

	public MelDataUnit(long timeMilliseconds, int channelBitmap, long startSample, long durationSamples, ComplexArray melData, int iSlice) {
		super(timeMilliseconds, channelBitmap, startSample, durationSamples, melData, iSlice);
//		melData = melData;
	}

//	@Override
//	public double[] getMagnitudeData() {
//		return melData;
//	}



}
