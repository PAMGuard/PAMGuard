package simulatedAcquisition.sounds;

import java.util.Random;

import Localiser.algorithms.PeakSearch;

public class WhiteNoise extends SimSignal {

	long firstSample;
	long lastSample;
	int nSamples;
	double[] randomData;
	private Random random;
	private int firstChannel;
	private PeakSearch peakSearch = new PeakSearch(true);
	
	public WhiteNoise() {
		super();
		random = new Random(1L);
	}

	@Override
	public String getName() {
		return "Localised white noise (one second pulse)";
	}

	@Override
	public double[] getSignal(int channel, float sampleRate, double sampleOffset) {
		if (this.nSamples == 0) {
			this.nSamples = (int) sampleRate;
			firstChannel = channel;
		}
		if (firstChannel == channel) {
			simulateNewData(nSamples+3);
		}
		/*
		 * Now interpolate between samples to get a new data array from the middle of that one ...
		 */
		double[] w = new double[nSamples];
		int y10 = 0;
		int y20 = 1;
		int y30 = 2;
		if (sampleOffset >= 0.5) {
			y10 = 1;
			y20 = 2;
			y30 = 3;
			sampleOffset -= 1.;
		}
		for (int i = 0, y1 = y10, y2 = y20, y3 = y30; i < nSamples; i++, y1++, y2++, y3++) {
			double samplePos = 1 + sampleOffset;
			if (samplePos < 0 || samplePos > randomData.length) {
				continue;
			}
			w[i] = peakSearch.parabolicHeight(sampleOffset, randomData[y1], randomData[y2], randomData[y3]);
		}
		return w;
	}

	private void simulateNewData(int length) {
		randomData = new double[length];
		for (int i = 0; i < length; i++) {
			randomData[i] = random.nextGaussian();
		}
	}

}
