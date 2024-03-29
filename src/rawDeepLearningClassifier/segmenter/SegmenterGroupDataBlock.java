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
		// TODO Auto-generated constructor stub
	}

}
