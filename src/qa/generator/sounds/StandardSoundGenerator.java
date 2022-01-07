package qa.generator.sounds;

import qa.generator.QASound;
import qa.generator.QASoundGenerator;
import qa.generator.window.SoundWindow;

public class StandardSoundGenerator extends QASoundGenerator {

	private StandardSoundParams standardSoundParams = new StandardSoundParams(0.0015, 4000, 8000);
	
	private SoundWindow soundWindow;
	
	private double[] harmonics = NOHARMONICS;
	
	public static final double[] NOHARMONICS = {1.};
	
	private double[] frequencyRange;

	/**
	 * @param latLong
	 */
	public StandardSoundGenerator(String name) {
		super(name);
	}
	
	public static double[] makeSquareHarmonics(int nH) {
		int n = 2*nH + 1;
		double[] h = new double[n];
		for (int i = 0; i <= nH; i++) {
			h[i*2] = 1./(2*i+1);
		}
		return h;
	}
	
	public static double[] makeTriangularHarmonics(int nH) {
		int n = 2*nH + 1;
		double[] h = new double[n];
		for (int i = 0; i <= nH; i++) {
			h[i*2] = Math.pow(-1, i) * Math.pow(1./(2*i+1), 2);
		}
		return h;
	}

	@Override
	public QASound generateSound(long sourceSample, double sampleRate, double[] delays, double[] amplitudes) {
		int nChan = delays.length;
		long[] firstSample = new long[nChan];
		double[] sampleOffsets = new double[nChan];
		double[][] wave = new double[nChan][];
		if (harmonics == null) {
			harmonics = NOHARMONICS;
		}
		for (int i = 0; i < nChan; i++) {
			double startSamp = sourceSample + delays[i];
			firstSample[i] = (long) Math.ceil(startSamp);
//			double amp = this.dB2Amplitude(amplitudes[i], standardSoundParams.getAmplitudeDB());
			wave[i] = makeWave(sampleRate, (firstSample[i] - startSamp), amplitudes[i]);
		}
		return new StandardSound(getName(), firstSample, wave, frequencyRange);
	}
	/**
	 * 
	 * @param sampleOffset actual time of first sample - between 0 and 1
	 * @param amplitude amplitude multiplier. 
	 * @return waveform for a single channel
	 */
	public final double[] makeWave(double sampleRate, double sampleOffset, double amplitude) {
		double[] phase = makePhase(sampleRate, sampleOffset, amplitude);
		if (phase == null) {
			return null;
		}
		double[] wav = new double[phase.length];
		double minF = sampleRate/2,maxF=0;
		for (int h = 0, hm = 1; h < harmonics.length; h++, hm++) {
			if (harmonics[h] == 0) {
				continue;
			}
			for (int i = 1; i < phase.length; i++) {
				double f = (phase[i]-phase[i-1])*hm;
				minF = Math.min(f, minF);
				maxF = Math.max(f, maxF);
				if (f > Math.PI) continue;
				wav[i] += (Math.sin(phase[i]*hm) * amplitude * harmonics[h]); 
			}
		}
		if (soundWindow != null) {
			soundWindow.runWindow(wav, sampleRate * standardSoundParams.getDurationS(), sampleRate, sampleOffset);
		}
		frequencyRange = new double[2];
		if (wav != null && wav.length >= 2) {
			frequencyRange[0] = minF/Math.PI*sampleRate/2;
			frequencyRange[1] = maxF/Math.PI*sampleRate/2;
		}
		else {
			frequencyRange[0] = 0;
			frequencyRange[1] = sampleRate/2;
		}
		return wav;
	}
	
	protected double[] makePhase(double sampleRate, double sampleOffset, double amplitude) {
		int nSamp = (int) Math.ceil(sampleRate * standardSoundParams.getDurationS());
		double[] phase = new double[nSamp];
		double sweep = (standardSoundParams.getEndFrequency() - standardSoundParams.getStartFrequency()) / 
				standardSoundParams.getDurationS();
		for (int i = 0; i < nSamp; i++) {
			double t = (i + sampleOffset) / sampleRate;
			phase[i] = 2 * Math.PI * (standardSoundParams.getStartFrequency()*t + sweep*t*t/2.);
		}
		return phase;
	}

	/**
	 * @return the standardSoundParams
	 */
	public StandardSoundParams getStandardSoundParams() {
		return standardSoundParams;
	}

	/**
	 * @param standardSoundParams the standardSoundParams to set
	 */
	public void setStandardSoundParams(StandardSoundParams standardSoundParams) {
		this.standardSoundParams = standardSoundParams;
	}

	/**
	 * @return the soundWindow
	 */
	public SoundWindow getSoundWindow() {
		return soundWindow;
	}

	/**
	 * @param soundWindow the soundWindow to set
	 */
	public void setSoundWindow(SoundWindow soundWindow) {
		this.soundWindow = soundWindow;
	}

	/**
	 * @return the harmonics
	 */
	public double[] getHarmonics() {
		return harmonics;
	}

	/**
	 * @param harmonics the harmonics to set
	 */
	public void setHarmonics(double[] harmonics) {
		this.harmonics = harmonics;
	}

	@Override
	public double[] getFrequencyRange() {
		double f1 = Math.min(standardSoundParams.getStartFrequency(), standardSoundParams.getEndFrequency());
		double f2 = Math.max(standardSoundParams.getStartFrequency(), standardSoundParams.getEndFrequency());
		double[] range = {f1, f2};
		return range;
	}

	@Override
	public double[] getDurationRange() {
		double t = standardSoundParams.getDurationS();
		double[] range = {t, t};
		return range;
	}	

}
