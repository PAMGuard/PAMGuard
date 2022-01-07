package qa.generator.clusters;

import qa.generator.distributions.QAGamma;
import qa.generator.distributions.QAGaussian;
import qa.generator.distributions.QARandomSpread;
import qa.generator.sequence.RandomSequenceGenerator;
import qa.generator.sounds.RandomToneGenerator;
import qa.generator.sounds.StandardSoundGenerator;
import qa.generator.window.EndsWindow;
import whistlesAndMoans.ConnectedRegionDataUnit;

public class LFBaleenCluster extends StandardQACluster {

	private static RandomToneGenerator wslGen;
	
	/**
	 * V 1.0 Based on planning doc of 28 July 2019.
	 * rms SL = 189 +/- 6. So have used o-p of 192+/- 6. 
	 * F range 15 - 100. + constraint of only sweeping one octave. 
	 * 2 second duration.  
	 * Generate one every 10s maximum
	 */
	
	public LFBaleenCluster(){
		super("LF Baleen", "1.0", new RandomSequenceGenerator(new QARandomSpread(true, 10), 
				new QAGaussian(192, 6), 1), wslGen = new RandomToneGenerator(15, 100, 2, 1.));
		wslGen.setHarmonics(StandardSoundGenerator.NOHARMONICS);
		wslGen.setDurationDistribution(new QAGamma(2, 0.5));
		wslGen.setSoundWindow(new EndsWindow(0.2));
	}

	@Override
	public Class getPrimaryDetectorType() {
		return ConnectedRegionDataUnit.class;
	}

}
