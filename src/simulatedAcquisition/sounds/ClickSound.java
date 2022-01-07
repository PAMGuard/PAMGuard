package simulatedAcquisition.sounds;

import Spectrogram.WindowFunction;

/**
 * Generate short click like sounds. These can be chirped (to look like 
 * beaked whales) and a variety of windows are available. 
 * @author doug
 *
 */
public class ClickSound extends SimSignal {

	private double[] sound;
	protected double f0, f1;
	protected double duration;
	private String name;
	WINDOWTYPE windowType;
	public enum WINDOWTYPE {FLAT, HANN, DECAY, TAPER10};
	
	public ClickSound(String name, double f0, double f1, double duration, WINDOWTYPE windowType) {
		super();
		this.f0 = f0;
		this.f1 = f1;
		this.windowType = windowType;
		this.duration = duration;
		if (f0 == f1) {
			this.name = String.format("%s %d kHz %3.2fms", name, (int)(f0/1000.), duration * 1000);		
		}
		else {
			this.name = String.format("%s %d-%d kHz %3.2fms", name, (int)(f0/1000.), (int)(f1/1000.), duration * 1000);
		}
	}
	
	protected void generateSound(float sampleRate, double sampleOffset) {
		int nSamp = (int) (duration * sampleRate);
		double sweep = (f1-f0)/duration;
		sound = new double[nSamp];
		double t, phase;
		for (int i = 0; i < nSamp; i++) {
			t = (i + sampleOffset)/sampleRate ;
			if (t > duration) break;
			phase = (f0*t + 0.5 * sweep * t * t)*2*Math.PI;
			sound[i] = Math.sin(phase);
//			sound[i] = Math.sin(i/sampleRate*2*Math.PI*f0) * (1.-i/(double)nSamp);
		}
		switch (windowType) {
		case FLAT:
			break;
		case HANN:
			taperEnds(sound, 50);
			break;
		case TAPER10:
			taperEnds(sound, 10);
			break;
		case DECAY:
			for (int i = 0; i < nSamp; i++) {
				sound[i] *= (1.-i/(double)nSamp);
			}
			break;
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public double[] getSignal(int channel, float sampleRate, double sampleOffset) {
		generateSound(sampleRate, sampleOffset);
		return sound;
	}


}
