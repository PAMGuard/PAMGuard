package rawDeepLearningClassifier.segmenter;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import rawDeepLearningClassifier.dlClassification.ModelResultDataUnit;

/**
 * Holds raw data segments which will be classified. 
 * 
 * @author Jamie Macaulay
 *
 */
public class SegmenterDataBlock extends PamDataBlock<GroupedRawData> {

	public SegmenterDataBlock(String dataName, PamProcess parentProcess, int channelMap) {
		super(ModelResultDataUnit.class, dataName, parentProcess, channelMap);
		this.setNaturalLifetimeMillis(5000); //do not want to keep the data for very long  - it's raw data segmnents so memory intensive
	}

	public boolean shouldNotify() {
		//need this to notify classifier in viewer mode. 
		return true;
	}
	
}
