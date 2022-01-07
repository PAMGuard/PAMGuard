package rawDeepLearningClassifier.dlClassification;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

/**
 * Holds classified data units from deep learning model. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DLModelDataBlock extends PamDataBlock<DLDataUnit> {

	public DLModelDataBlock(String dataName, PamProcess parentProcess, int channelMap) {
		super(DLDataUnit.class, dataName, parentProcess, channelMap);
		
	}
}
