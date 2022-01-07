package rawDeepLearningClassifier.dlClassification;

import PamguardMVC.AcousticDataBlock;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

/**
 * Holds classified data units from deep learning model. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DLDetectionDataBlock extends AcousticDataBlock<DLDetection> {

	public DLDetectionDataBlock(String dataName, PamProcess parentProcess, int channelMap) {
		super(DLDetection.class, dataName, parentProcess, channelMap);
		
	}


}
