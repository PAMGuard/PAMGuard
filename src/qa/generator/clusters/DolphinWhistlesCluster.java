package qa.generator.clusters;

import qa.generator.distributions.QAGamma;
import qa.generator.distributions.QAGaussian;
import qa.generator.distributions.QARandomSpread;
import qa.generator.sequence.QASequenceGenerator;
import qa.generator.sequence.RandomSequenceGenerator;
import qa.generator.sounds.StandardSoundGenerator;
import whistlesAndMoans.ConnectedRegionDataUnit;
import qa.generator.sounds.RandomToneGenerator;

public class DolphinWhistlesCluster extends StandardQACluster {

	private static final int nWhistles = 10;

	public DolphinWhistlesCluster() {
		super("Dolphin whistles", "1.0", makeSequencegenerator(), makeSoundGenerator());
	}

	private static QASequenceGenerator makeSequencegenerator() {
		return new RandomSequenceGenerator(new QARandomSpread(true, 1), 
				new QAGaussian(149.7, 13), nWhistles);		
	}
	
	private static StandardSoundGenerator makeSoundGenerator() {
		RandomToneGenerator wslGen = new RandomToneGenerator(2000, 24000, 2, 1.5);
		wslGen.setHarmonics(StandardSoundGenerator.NOHARMONICS);
		wslGen.setDurationDistribution(new QAGamma(0.5, .2));
		return wslGen;
	}
	
	@Override
	public Class getPrimaryDetectorType() {
		return ConnectedRegionDataUnit.class;
	}



}
