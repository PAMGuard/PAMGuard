package qa.generator.clusters;

import qa.generator.distributions.QACorrelatedSequence;
import qa.generator.distributions.QADistribution;
import qa.generator.distributions.QAFixed;
import qa.generator.distributions.QAGamma;
import qa.generator.distributions.QAGaussian;
import qa.generator.sequence.QASequenceGenerator;
import qa.generator.sequence.RandomSequenceGenerator;
import qa.generator.sounds.RandomToneGenerator;
import qa.generator.sounds.StandardSoundGenerator;
import qa.generator.window.EndsWindow;
import whistlesAndMoans.ConnectedRegionDataUnit;

/**
 * Using Munger SL distribution of 178/176. Use 177, add 3 for 0-peak, so 180dB0-p
 * use distribution Guass width of 1/4 range. Range is about 10dB, so use 2.5 (seems very narrow !). 
 * Why as Park's SL measurements so much lower at 137-162 ?
 * Make upsweeps from around 90 to 170Hz
 * @author dg50
 *
 */
public class RightWhaleCluster extends StandardQACluster {
	
	private static RandomToneGenerator wslGen;
	static QADistribution[] startAndEnd = {new QAGamma(90, 15), new QAGamma(110, 20), new QAGamma(170, 30)};
	
	public RightWhaleCluster() {
		super("Right Whale", "1.0", makeSequencegenerator(), makeSoundGenerator());
	}

	private static QASequenceGenerator makeSequencegenerator() {
		return new RandomSequenceGenerator(new QAFixed(true, 3.), 
				new QAGaussian(180, 2.5), 1);
	}
	
	private static StandardSoundGenerator makeSoundGenerator() {
		wslGen = new RandomToneGenerator(startAndEnd, "Right Whale");
		wslGen.setHarmonics(StandardSoundGenerator.NOHARMONICS);
		wslGen.setDurationDistribution(new QAGamma(0.75, 0.25));
		wslGen.setSoundWindow(new EndsWindow(0.1));
		return wslGen;
	}
	
	@Override
	public Class getPrimaryDetectorType() {
		return ConnectedRegionDataUnit.class;
	}

}
