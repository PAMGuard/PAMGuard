package qa.generator.sounds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;

import PamUtils.LatLong;
import PamguardMVC.debug.Debug;
import qa.generator.QASound;
import qa.generator.distributions.QADistribution;
import qa.generator.window.EndsWindow;

/**
 * Whistle generator makes random whistles using a 2nd order polynomial shape
 * derived from three equally spaced points in time. 
 * @author dg50
 *
 */
public class RandomToneGenerator extends StandardSoundGenerator {

	private int nPolyPoints = 5;
	private double minFreq, maxFreq;
	private PolynomialCurveFitter polyFitter;
	private double[] polyVals;
	private QADistribution[] freqeuncyDistributions;
	private QADistribution durationDistribution;
	private double chirpLen;
	private double[] frequencyRange;
	private double[] durationRange = {0.1, 0.4};
	/**
	 * Max sweep is the maximum that any sound can sweep through in Octaves. e.g. if 
	 * we make a whistle generator that can generate between 1kHz and 24kHz, we don't want
	 * single sounds sweeping from 1 to 24 kHz, rather, we want something that will sweep
	 * over an octave or two somewhere within that range.  
	 */
	private Double maxSweepOctaves;

	/**
	 * Make a random tone generator 
	 * @param minFreq minimum frequency 
	 * @param maxFreq maximum frequency
	 * @param nPolyPoints number of random polynomial points. 
	 * @param maximum sweep for any single sound. If this is null, then other 'points' can use the
	 * full frequency range. 
	 */
	public RandomToneGenerator(double minFreq, double maxFreq, int nPolyPoints, Double maxSweepOctaves) {
		super("Whistle");
		this.minFreq = minFreq;
		this.maxFreq = maxFreq;
		this.nPolyPoints = nPolyPoints;
		polyFitter = PolynomialCurveFitter.create(nPolyPoints-1);
		setSoundWindow(new EndsWindow(128));
		double[] range = {minFreq, maxFreq};
		frequencyRange = range;
		this.maxSweepOctaves = maxSweepOctaves;
	}

	/**
	 * Make a random tone generator. Frequencies will be totally unconstrained within the
	 * set limits. 
	 * @param minFreq minimum frequency 
	 * @param maxFreq maximum frequency
	 * @param nPolyPoints number of random polynomial points. 
	 */
	public RandomToneGenerator(double minFreq, double maxFreq, int nPolyPoints) {
		this(minFreq, maxFreq, nPolyPoints, null);
	}

	/**
	 * Make a random tone generator with distributions pulled from the 
	 * list of distributions
	 * @param freqeuncyDistributions list of frequency point distributions
	 */
	public RandomToneGenerator(QADistribution[] freqeuncyDistributions, String name) {
		super(name);
		//		this.minFreq = minFreq;
		//		this.maxFreq = maxFreq;
		this.nPolyPoints = freqeuncyDistributions.length;
		this.freqeuncyDistributions = freqeuncyDistributions;
		polyFitter = PolynomialCurveFitter.create(nPolyPoints-1);
		setSoundWindow(new EndsWindow(128));
		if (freqeuncyDistributions != null) {
			minFreq = Double.MAX_VALUE;
			maxFreq = 0;
			for (int i = 0; i < freqeuncyDistributions.length; i++) {
				double[] rd = freqeuncyDistributions[i].getRange(2.); 
				minFreq = Math.min(minFreq, rd[0]);
				maxFreq = Math.max(maxFreq, rd[1]);
			}
			double[] range = {minFreq, maxFreq};
			frequencyRange = range;
		}
	}

	/* (non-Javadoc)
	 * @see qa.generator.sounds.StandardSoundGenerator#generateSound(long, double, double[], double[])
	 */
	@Override
	public QASound generateSound(long sourceSample, double sampleRate, double[] delays, double[] amplitudes) {
		double[] chirpFreqs = new double[nPolyPoints];
		double[] chirpTimes = new double[nPolyPoints];
		Collection<WeightedObservedPoint> points = new ArrayList<>(nPolyPoints);
		if (durationDistribution == null) {
			chirpLen = Math.random() * .3 + 0.1; // length between .1 and .4s
		}
		else {
			double[] t = durationDistribution.getValues(1);
			chirpLen = t[0];
		}
		for (int i = 0; i < nPolyPoints; i++) {
			if (freqeuncyDistributions == null) {
				/*
				 * Using simpler method, whereby freq points are taken from flat distributions. 
				 */
				chirpFreqs[i] = Math.random() * (maxFreq-minFreq) + minFreq;
			}
			else {
				/**
				 * More sophist' method that uses distributions for each 
				 * frequency point. 
				 */
				double[] f = freqeuncyDistributions[i].getValues(1);
				chirpFreqs[i] = f[0];
			}
			chirpTimes[i] = chirpLen*i/(nPolyPoints-1);
		}
		if (maxSweepOctaves != null) {
			chirpFreqs = constrianChrip(maxSweepOctaves, chirpFreqs);
		}
		for (int i = 0; i < nPolyPoints; i++) {
			points.add(new WeightedObservedPoint(1, chirpTimes[i], chirpFreqs[i]));
		}
//		Debug.out.println("Fitting " + getName() + " chirp points to " + Arrays.toString(chirpTimes) + "; " + Arrays.toString(chirpFreqs));
		polyVals = polyFitter.fit(points);
		return super.generateSound(sourceSample, sampleRate, delays, amplitudes);
	}

	/**
	 * Constrain a chirp so that it never sweeps through more than the specified
	 * frequency range. Done on a log scale so that the squash is done towards
	 * the geometric centre of the frequency range. 
	 * @param maxSweepOctaves
	 * @param chirpFreqs
	 * @return constrained corner frequencies. 
	 */
	private double[] constrianChrip(Double maxSweepOctaves, double[] chirpFreqs) {
		if (maxSweepOctaves == null || chirpFreqs == null || chirpFreqs.length < 2) {
			return chirpFreqs;
		}
//		double[] logFreq = new double[chirpFreqs];
		double minFreq, maxFreq;
		minFreq = maxFreq = chirpFreqs[0]; 
		for (int i = 1; i < chirpFreqs.length; i++) {
			minFreq = Math.min(minFreq, chirpFreqs[i]);
			maxFreq = Math.min(maxFreq, chirpFreqs[i]);
		}
		double nOct = Math.log(maxFreq/minFreq) / Math.log(2);
		if (nOct <= maxSweepOctaves) {
			// no need to squash the distributions
			return chirpFreqs;
		}
		double squashFactor = maxSweepOctaves / nOct; // will be a number < 1
		double centreFreq;
		if (minFreq > 0) {
			centreFreq = Math.sqrt(minFreq*maxFreq);
		}
		else {
			// should never happen, but will stop sqrt exception if it does. 
			centreFreq = (maxFreq + minFreq) / 2;
		}
		double[] newChirp = new double[chirpFreqs.length];
		for (int i = 1; i < chirpFreqs.length; i++) {
			newChirp[i] = Math.pow(chirpFreqs[i]/centreFreq, squashFactor) * centreFreq;
		}
		return newChirp;
	}

	/* (non-Javadoc)
	 * @see qa.generator.sounds.StandardSoundGenerator#makeWave(double, double, double)
	 */
	@Override
	public double[] makePhase(double sampleRate, double sampleOffset, double amplitude) {
		int nSamp = (int) Math.ceil(sampleRate * chirpLen);
		double[] phase = new double[nSamp];
		for (int i = 0; i < nSamp; i++) {
			double t = (i + sampleOffset) / sampleRate;
			double wt = polyVals[0]*t;// + sweep*t*t/2.);
			for (int o = 1; o < polyVals.length; o++) {
				wt += polyVals[o]*Math.pow(t, o+1)/(o+1);
			}
			phase[i] = wt * 2 * Math.PI;
		}
		return phase;
	}

	/**
	 * @return the durationDistribution
	 */
	public QADistribution getDurationDistribution() {
		return durationDistribution;
	}

	/**
	 * @param durationDistribution the durationDistribution to set
	 */
	public void setDurationDistribution(QADistribution durationDistribution) {
		this.durationDistribution = durationDistribution;
		durationRange = durationDistribution.getRange(2);
	}

	/* (non-Javadoc)
	 * @see qa.generator.sounds.StandardSoundGenerator#getFrequencyRange()
	 */
	@Override
	public double[] getFrequencyRange() {
		return frequencyRange;
	}

	@Override
	public double[] getDurationRange() {
		return durationRange;
	}

	/**
	 * @return the maxSweepOctaves
	 */
	public Double getMaxSweepOctaves() {
		return maxSweepOctaves;
	}

	/**
	 * @param maxSweepOctaves the maxSweepOctaves to set
	 */
	public void setMaxSweepOctaves(Double maxSweepOctaves) {
		this.maxSweepOctaves = maxSweepOctaves;
	}

}
