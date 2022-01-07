package qa.generator.clusters;

import clickDetector.ClickDetection;
import qa.generator.sequence.DolphinSequenceGenerator;
import qa.generator.sounds.DolphinClickGenerator;

public class DolphinClicksCluster extends StandardQACluster {

	public DolphinClicksCluster() {
		super("Dolphin Clicks", "1.0", new DolphinSequenceGenerator(20), new DolphinClickGenerator(60000));
	}

	@Override
	public Class getPrimaryDetectorType() {
		return ClickDetection.class;
	}

}
