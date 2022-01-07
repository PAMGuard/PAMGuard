package propagation;

import PamUtils.PamUtils;
import fftManager.FastFFT;

public class FrequencyAttenutationModel implements AttenuationModel {
	
	private FastFFT fastFFT = new FastFFT();

	@Override
	public double[] attenuateWaveform(double[] wave, double sampleRate, double distance) {
		if (wave == null) {
			return null;
		}
		int len = wave.length;
		int fftLen = fastFFT.nextBinaryExp(len);
		double[] complexData = new double[len*2];
//		fastFFT.rfft(wave, complexData, fftLen);
		return null;
	}

}
