package qa.generator.clusters;

import qa.generator.distributions.QADistribution;
import qa.generator.distributions.QAGamma;
import qa.generator.distributions.QAGaussian;
import qa.generator.distributions.QARandomSpread;
import qa.generator.sequence.QASequenceGenerator;
import qa.generator.sequence.RandomSequenceGenerator;
import qa.generator.sounds.RandomToneGenerator;
import qa.generator.sounds.StandardSoundGenerator;
import qa.generator.window.EndsWindow;
import whistlesAndMoans.ConnectedRegionDataUnit;

public class BlackFishCluster extends StandardQACluster {


	/**
	 * SL distribution rms = 114 - 130
	 * use mean + 3 = 122+3 = 125;
	 * spread = (130-114)/4 = 4 
	 */
	private static double meanSL = 125;
	private static double slDistr = 4;
	
	

	public BlackFishCluster() {
		super("Black fish", "1.0", makeSequencegenerator(), makeSoundGenerator());

	}

	private static QASequenceGenerator makeSequencegenerator() {
		return new RandomSequenceGenerator(new QARandomSpread(true, 2), 
				new QAGaussian(meanSL, slDistr), 2);
	}

	private static StandardSoundGenerator makeSoundGenerator() {
		RandomToneGenerator toneGen = new RandomToneGenerator(4000, 8000, 2, .6);
		toneGen.setDurationDistribution(new QAGamma(.4,  .01));
		toneGen.setSoundWindow(new EndsWindow(.1));
		toneGen.setHarmonics(StandardSoundGenerator.makeSquareHarmonics(7));
		return toneGen;
	}

	@Override
	public Class getPrimaryDetectorType() {
		return ConnectedRegionDataUnit.class;
	}

}
