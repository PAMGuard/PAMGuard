package rawDeepLearningClassifier.dlClassification.koogu;

import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelClassifier;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.DLModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;

/**
 * Classifier which uses deep learning models from Koogus' framework.
 * <p>
 * Koogu uses TensorFlow models and packages them inside a zipped .kgu file
 * which contains a standard JSON file for the transforms and a .pb model. Users can
 * select a .kgu file - PAMGaurd will decompress it, find the JSON file, set up
 * the transforms and load the model.
 * <p>
 * Details on Koogu framework can be found athttps://github.com/shyamblast/Koogu?tab=readme-ov-file
 * @author Jamie Macaulay
 *
 */
public class KooguClassifier extends ArchiveModelClassifier {
	
	
	public static String MODEL_NAME = "Koogu";
	
	/**
	 * The file extensions
	 */
	private String[] fileExtensions = new String[] {"*.kgu"};

	private KooguModelWorker kooguWorker;


	public KooguClassifier(DLControl dlControl) {
		super(dlControl);
	}
	
	@Override
	public String[] getFileExtensions() {
		return fileExtensions;
	}
	
	@Override
	public String getName() {
		return MODEL_NAME;
	}
	
	@Override
	public ArchiveModelWorker getModelWorker() {
		if (kooguWorker==null) {
			kooguWorker= new KooguModelWorker(); 
		}
		return kooguWorker;
	}



}