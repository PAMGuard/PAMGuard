package rawDeepLearningClassifier.dlClassification.delphinID;

import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelClassifier;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelWorker;

/**
 * A classifier based on the delphinID method which uses whistle contours to predict
 * dolphin species. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DelphinIDClassifier extends ArchiveModelClassifier{

	private static final String MODEL_NAME = "delphinID";
	
	/**
	 * Reference to the worker
	 */
	private DelphinIDWorker delphinIDWorker;

	public DelphinIDClassifier(DLControl dlControl) {
		super(dlControl);
	}
	
	@Override
	public String getName() {
		return MODEL_NAME;
	}
	
	@Override
	public ArchiveModelWorker getModelWorker() {
		if (delphinIDWorker==null) {
			delphinIDWorker= new DelphinIDWorker(); 
		}
		return delphinIDWorker;
	}

	
}
