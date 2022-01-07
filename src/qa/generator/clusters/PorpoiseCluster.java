package qa.generator.clusters;

import clickDetector.ClickDetection;
import qa.generator.distributions.QACorrelatedSequence;
import qa.generator.distributions.QADistribution;
import qa.generator.distributions.QAGamma;
import qa.generator.sequence.QASequenceGenerator;
import qa.generator.sequence.RandomSequenceGenerator;
import qa.generator.sounds.RandomToneGenerator;
import qa.generator.sounds.StandardSoundGenerator;
import qa.generator.window.RaisedSinWindow;

public class PorpoiseCluster extends StandardQACluster {

	static double meanICI = .4;
	static double stdICI = meanICI/3;

	/**
	 * SL distribution rms = 140 - 190 Assume rims
	 * so use centre of 165+3 = 168 for 0 - peak
	 * and STD 1/4 range = 12.5 
	 */
	private static double meanSL = 168;
	private static double slDistr = 12.5;
	
	private static final int nClicks = 10;

	public PorpoiseCluster() {
		super("Porpoise", "1.0", makeSequencegenerator(), makeSoundGenerator());
	}
	
	private static QASequenceGenerator makeSequencegenerator() {
		return new RandomSequenceGenerator(new QACorrelatedSequence(true, meanICI, stdICI,8), 
				new QACorrelatedSequence(false, meanSL, slDistr, 6), nClicks);
	}
	
	private static StandardSoundGenerator makeSoundGenerator() {
		QADistribution freqDist = new QAGamma(130000, 5);
		QADistribution[] freqDists = {freqDist};
		RandomToneGenerator toneGen = new RandomToneGenerator(freqDists, "Porpoise");
		toneGen.setDurationDistribution(new QAGamma(77.0e-6, 7e-6));
		toneGen.setSoundWindow(new RaisedSinWindow());
		return toneGen;
	}
	
	@Override
	public Class getPrimaryDetectorType() {
		return ClickDetection.class;
	}
}
