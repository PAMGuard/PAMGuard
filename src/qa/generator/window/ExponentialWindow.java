package qa.generator.window;

public class ExponentialWindow implements SoundWindow {

	private double decaySeconds;

	public ExponentialWindow(double decaySeconds) {
		this.decaySeconds = decaySeconds;
	}

	@Override
	public void runWindow(double[] wave, double soundSamples, double sampleRate, double sampleOffset) {
		double decaySamples = decaySeconds * sampleRate;
		for (int i = 0; i < wave.length; i++) {
			wave[i] *= Math.exp(-(i+sampleOffset)/decaySamples);
		}
	}

}
