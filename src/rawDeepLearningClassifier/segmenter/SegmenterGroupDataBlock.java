package rawDeepLearningClassifier.segmenter;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

/**
 * 
 * @author Jamie Macaulay
 *
 */
public class SegmenterGroupDataBlock extends PamDataBlock<SegmenterDetectionGroup> {

	public SegmenterGroupDataBlock(String dataName, PamProcess parentProcess, int channelMap) {
		super(SegmenterDetectionGroup.class, dataName, parentProcess, channelMap);
		this.setNaturalLifetimeMillis(15000); //do not want to keep the data for very long  - it's raw data segmnents so memory intensive

	}
	

}
