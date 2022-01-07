package qa.generator.window;

public class HannWindow implements SoundWindow {

	public HannWindow() {
	}

	@Override
	public void runWindow(double[] wave, double soundSamples, double sampleRate, double sampleOffset) {
		int firstSamp = (int) Math.max(0, sampleOffset);
		int lastSamp = (int) Math.min(wave.length, soundSamples + sampleOffset);
		double intlOffset = sampleOffset - Math.floor(sampleOffset);
		for (int i = firstSamp; i < lastSamp; i++) {
			double t = i+intlOffset;
			wave[i] *= Math.pow(Math.sin(t/soundSamples*Math.PI),2);
		}
	}

}
