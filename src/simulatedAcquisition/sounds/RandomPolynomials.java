package simulatedAcquisition.sounds;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class RandomPolynomials extends SimSignal{
	
	private double[] lengthR = {0.5, .9};
	private double[] meanR = {3000, 12000};
	private double[] zeroA = {-2, .43};
	private double[] zeroB = {3.3, 4.5};
	private double[] zeroC = {1.5, 2};
	private boolean addHarmonics;

	private Random r = new Random();
	private double[] harmonicDampingR = {1,3};
	
	private double length, a, b, c, d, damp;
	
	private RandomQuadratics quadraticgenerator;
	
	private float nyquist;
		
	public RandomPolynomials(boolean addHarmonics, float sampleRate) {
		super();
		this.addHarmonics = addHarmonics;
		quadraticgenerator = new RandomQuadratics();
		this.nyquist = sampleRate/2.0f;
	}

	@Override
	public String getName() {
		return "Harmonic Whistles";
	}

	@Override
	public void prepareSignal() {
		length = getRandom(lengthR);
		a = getRandom(zeroA);
		b = getRandom(zeroB);
		c = getRandom(zeroC);
		d = getRandom(meanR);

		damp = getRandom(harmonicDampingR);
		if (b > 0) { // if it starts by going up, make it curve the other way
//			c = -c;
		}
	}
	

	@Override
	public double[] getSignal(int channel, float sampleRate, double sampleOffset) {
		
		if(r.nextDouble()>.5) {
			return quadraticgenerator.getSignal(channel, sampleRate, sampleOffset);
		}
		
		int nSamp = (int) (length * sampleRate);
		ArrayList<Double> soundList = new ArrayList<Double>();
		double t, phase_hz, phase_rad, sweep, f;
		for (int i = 0; i < nSamp; i++) {
			t = (i+sampleOffset)/sampleRate;
			f = (t-2.5)*(t-2.5)*(t-2.5)*(t-2.5)*(t-2.5)*(t-b)*(t-b)*(t-b)*t*t*c*c+d*t;
			phase_hz = f;
			phase_rad = f*2*Math.PI;
			if(phase_hz>nyquist) {
				break;
			}
			if(addHarmonics) {
				soundList.add(Math.sin(phase_rad)+.15*Math.sin(2*phase_rad)+.1*Math.sin(3*phase_rad)+.05*Math.sin(4*phase_rad));
			}else {
				soundList.add(Math.sin(phase_rad));
			}
		}
		double[] sound = soundList.stream().mapToDouble(Double::doubleValue).toArray();
		taperEnds(sound, 10);
		return sound;
	}
	
	private double getRandom(double[] range) {
		return range[0] + (range[1]-range[0]) * r.nextDouble();
	}

	public double[] getLengthR() {
		return lengthR;
	}

	public void setLengthR(double[] lengthR) {
		this.lengthR = lengthR;
	}

	public double[] getMeanR() {
		return meanR;
	}

	public void setMeanR(double[] meanR) {
		this.meanR = meanR;
	}

	
	public double[] getHarmonicDampingR() {
		return harmonicDampingR;
	}

	
}
