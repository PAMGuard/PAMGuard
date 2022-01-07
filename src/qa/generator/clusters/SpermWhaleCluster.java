package qa.generator.clusters;

import clickDetector.ClickDetection;
import qa.generator.distributions.QACorrelatedSequence;
import qa.generator.distributions.QADistribution;
import qa.generator.distributions.QAFixed;
import qa.generator.distributions.QAGamma;
import qa.generator.location.QALocationGenerator;
import qa.generator.sequence.QASequenceGenerator;
import qa.generator.sequence.RandomSequenceGenerator;
import qa.generator.sounds.StandardSoundGenerator;
import qa.generator.sounds.StandardSoundParams;
import qa.generator.testset.QATestSet;
import qa.generator.testset.SimpleTestSet;
import qa.generator.window.HannWindow;

public class SpermWhaleCluster extends StandardQACluster {

	private static StandardSoundGenerator soundGen;
	static double meanICI = .5;
	static double stdICI = meanICI/3;
	
	private QADistribution depthDistribution;
	/**
	 * Using rms 186 from Mathias (2018) + 3 for 0-p,
	 * Generating sequences of 20 clicks so can use click train detection. 
	 * Mean ICI 0.5s (not very important,so want reasonably fast to get it over with, ici can very by up to .17s)
	 */
	
	public SpermWhaleCluster() {
		super("Sperm Whale", "1.0", new RandomSequenceGenerator(new QACorrelatedSequence(true, meanICI, stdICI,8), 
				new QACorrelatedSequence(false, 189, 2, 6), 20), soundGen = new StandardSoundGenerator("Sperm Whale"));
		soundGen.setStandardSoundParams(new StandardSoundParams(0.001, 5000, 6000));
		soundGen.setSoundWindow(new HannWindow());	
		depthDistribution = new QAGamma(800, 200);
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
