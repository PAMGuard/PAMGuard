package simulatedAcquisition.sounds;

import java.util.Arrays;

public class BranchedChirp extends ClickSound {

//	private ClickSound branch;
	private double f2, f3;
	private double[] sound;
	public BranchedChirp( double f0, double f1, double f2, double f3,
			double duration) {
		super("Branched Chirp", f0, f1, duration, WINDOWTYPE.TAPER10);
		this.f0 = f0;
		this.f1 = f1;
		this.f2 = f2;
		this.f3 = f3;
//		addChirp(f0, f1, .25, f2, duration*3/2);
//		addChirp(f0, f1, .6, f3, duration);
	}
	
	private void addChirp(int channel, float sampleRate, double sampleOffset, double f0, double f1, double tFrac, double f3, double duration) {
		double f = f0*(1-tFrac) + f1*tFrac;
		ClickSound newChirp = new ClickSound("Chirp", f, f3, duration, WINDOWTYPE.TAPER10);
		int bin1 = (int) (tFrac * sound.length);
		double[] chirpSound = newChirp.getSignal(channel, sampleRate, sampleOffset);
		int bin2 = bin1+chirpSound.length;
		sound = Arrays.copyOf(sound, Math.max(sound.length, bin2));
		int sB = 0;
		for (int i = bin1; i < bin2; i++, sB++) {
			sound[i] += chirpSound[sB];
		}
	}
	
	
	private void generateBranches(int channel, float sampleRate, double sampleOffset) {
		sound = super.getSignal(channel, sampleRate, sampleOffset);
		addChirp(channel, sampleRate, sampleOffset, f0, f1, .25, f2, duration*3/2);
		addChirp(channel, sampleRate, sampleOffset, f0, f1, .6, f3, duration);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Branched " + super.getName();
	}

	@Override
	public double[] getSignal(int channel, float sampleRate, double sampleOffset) {
		generateBranches(channel, sampleRate, sampleOffset);
		return sound;
	}
}
