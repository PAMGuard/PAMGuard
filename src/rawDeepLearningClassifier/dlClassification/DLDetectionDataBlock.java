package rawDeepLearningClassifier.dlClassification;

import PamView.GroupedDataSource;
import PamView.GroupedSourceParameters;
import PamguardMVC.AcousticDataBlock;

/**
 * Holds classified data units from deep learning model. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DLDetectionDataBlock extends AcousticDataBlock<DLDetection> implements GroupedDataSource {

	private DLClassifyProcess dlClassifyProcess;

	public DLDetectionDataBlock(String dataName, DLClassifyProcess parentProcess, int channelMap) {
		super(DLDetection.class, dataName, parentProcess, channelMap);
		this.dlClassifyProcess = parentProcess; 
	}

	@Override
	public GroupedSourceParameters getGroupSourceParameters() {
		return dlClassifyProcess.getDLParams().groupedSourceParams;
	}


}
