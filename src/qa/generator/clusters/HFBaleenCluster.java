package qa.generator.clusters;

import qa.generator.distributions.QAGamma;
import qa.generator.distributions.QAGaussian;
import qa.generator.distributions.QARandomSpread;
import qa.generator.sequence.RandomSequenceGenerator;
import qa.generator.sounds.RandomToneGenerator;
import qa.generator.sounds.StandardSoundGenerator;
import qa.generator.window.EndsWindow;
import whistlesAndMoans.ConnectedRegionDataUnit;

public class HFBaleenCluster extends StandardQACluster {

	private static RandomToneGenerator wslGen;
	
	
	public HFBaleenCluster(){
		super("HF Baleen", "1.0", new RandomSequenceGenerator(new QARandomSpread(true, 10), 
				new QAGaussian(163, 10), 1), wslGen = new RandomToneGenerator(50, 1000, 2, 1.5));
		wslGen.setHarmonics(StandardSoundGenerator.NOHARMONICS);
		wslGen.setDurationDistribution(new QAGamma(2, 0.5));
		wslGen.setSoundWindow(new EndsWindow(0.1));
	}

	@Override
	public Class getPrimaryDetectorType() {
		return ConnectedRegionDataUnit.class;
	}

}
