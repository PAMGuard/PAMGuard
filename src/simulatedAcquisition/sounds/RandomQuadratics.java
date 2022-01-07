package simulatedAcquisition.sounds;

import java.util.Random;

public class RandomQuadratics extends SimSignal {

	private double[] lengthR = {0.1, .6};
	private double[] meanR = {3000, 12000};
	private double[] slopeR = {-20000, 20000};
	private double[] curveR = {0, 60000};
	private Random r = new Random();
	
	private double length, a, b, c;
		
	public RandomQuadratics() {
		super();
	}

	@Override
	public String getName() {
		return "Random Whistles";
	}
	

	/* (non-Javadoc)
	 * @see simulatedAcquisition.sounds.SimSignal#prepareSignal()
	 */
	@Override
	public void prepareSignal() {
		length = getRandom(lengthR);
		a = getRandom(meanR);
		b = getRandom(slopeR);
		c = getRandom(curveR);
		if (b > 0) { // if it starts by going up, make it curve the other way
//			c = -c;
		}
	}

	@Override
	public double[] getSignal(int channel, float sampleRate, double sampleOffset) {

		// check the total range of the signal. 
		double df = Math.abs(b*length + c*length*length);
		// generate start freq so it has to stay in range
		double[] startRange = new double[2];
//		startRange[0] = df + sampleRate / 20;
//		startRange[1] = sampleRate/2-startRange[0];
//		a = getRandom(startRange);
		
		
		int nSamp = (int) (length * sampleRate);
		double[] sound = new double[nSamp];
		double t, phase, sweep;
		for (int i = 0; i < nSamp; i++) {
			t = (i+sampleOffset)/sampleRate;
			phase = (a*t + 0.5*b*t*t + c*t*t*t/3.)*2*Math.PI;
			sound[i] = Math.sin(phase);
		}
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

	public double[] getSlopeR() {
		return slopeR;
	}

	public void setSlopeR(double[] slopeR) {
		this.slopeR = slopeR;
	}

	public double[] getCurveR() {
		return curveR;
	}

	public void setCurveR(double[] curveR) {
		this.curveR = curveR;
	}

}
