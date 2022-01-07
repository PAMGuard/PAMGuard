package qa.generator.clusters;

import clickDetector.ClickDetection;
import qa.generator.distributions.QACorrelatedSequence;
import qa.generator.distributions.QADistribution;
import qa.generator.distributions.QAGamma;
import qa.generator.distributions.QAGaussian;
import qa.generator.sequence.QASequenceGenerator;
import qa.generator.sequence.RandomSequenceGenerator;
import qa.generator.sounds.RandomToneGenerator;
import qa.generator.sounds.StandardSoundGenerator;
import qa.generator.window.RaisedSinWindow;

public class BeakedWhaleCluster extends StandardQACluster {

	static double meanICI = .4;
	static double stdICI = meanICI/3;
	
	/**
	 * SL distribution rms = 162 - 197
	 * use mean + 3 = 182.5 & 1/4 of spread = 8.8 
	 */
	private static double meanSL = 182.5;
	private static double slDistr = 8.8;
	
	private QADistribution depthDistribution;
	/*
	 * The tagged whales started vocalizing at depths ranging from 178 m to 643 m, 
	 * with maximum depths ranging from 530 m to 1390 m for the different dives. 
	 * The mean foraging depth of the whales during regular clicking was at 698m (range 435m – 1110 m) 
	 * for the animals from El Hierro and 833m (range 392 m – 1190m) for the animals from the Bahamas 
	 * (Table 1). Whales’ depth during vocalization periods (Fig. 4 and 5) had a mean value of 753 m 
	 * (561 m – 880 m 5 - 95 % quantiles).
	 * 
	 * Think I'll use the El Hierro, since has larger confidnece interval, so mean = 833, STD=(1190-392)/4=200
	 */
	
	
	public BeakedWhaleCluster() {
		super("Beaked Whale", "1.0", makeSequencegenerator(), makeSoundGenerator());
		depthDistribution = new QAGamma(753, 150);
	}
	
	private static QASequenceGenerator makeSequencegenerator() {
		return new RandomSequenceGenerator(new QACorrelatedSequence(true, meanICI, stdICI,8), 
				new QACorrelatedSequence(false, meanSL, slDistr, 6), 10);

	}
	
	private static StandardSoundGenerator makeSoundGenerator() {
		QAGaussian bwStarts = new QAGaussian(20000, 5);
		QAGaussian bwEnds = new QAGaussian(45000, 5);
		QADistribution[] freqs = {bwStarts, bwEnds};
		RandomToneGenerator toneGen = new RandomToneGenerator(freqs, "Beaked Whale");
		toneGen.setDurationDistribution(new QAGamma(.3e-3,  .03e-3));
		toneGen.setSoundWindow(new RaisedSinWindow());
		return toneGen;
	}

	@Override
	public Class getPrimaryDetectorType() {
		return ClickDetection.class;
	}

	@Override
	public QADistribution getDepthDistribution() {
		return depthDistribution;
	}
}
