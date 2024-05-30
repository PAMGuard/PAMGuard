package rawDeepLearningClassifier.dlClassification.ketos;

import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelClassifier;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelWorker;

/**
 * Classifier which uses deep learning models from Meridian's Ketos framework.
 * <p>
 * Ketos uses TensorFlow models and packages them inside a zipped .ktpb file
 * which contains a JSON file for the transforms and a .pb model. Users can
 * select a .ktpb file - PAMGaurd will decompress it, find the JSON file, set up
 * the transforms and load the model.
 * <p>
 * Details on Meridians framework can be found at https://meridian.cs.dal.ca/2015/04/12/ketos/
 * <p>
 * KetosClassifier2 is a more abstracted version of KetosClassifer which inherits most functionality from ArchiveModel
 * @author Jamie Macaulay
 *
 */
public class KetosClassifier2 extends ArchiveModelClassifier {
	
	public static String MODEL_NAME = "Ketos";

	/**
	 * The file extensions
	 */
	private String[] fileExtensions = new String[] {"*.ktpb"};


	private KetosWorker2 ketosWorker;
	
	public KetosClassifier2(DLControl dlControl) {
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
		if (ketosWorker==null) {
			ketosWorker= new KetosWorker2(); 
		}
		return ketosWorker;
	}

	/**
	 * Create the parameters class for the model. This can be overridden for bespoke parameters. 
	 *classes. 
	 * @return a new parameters class object. 
	 */
	public StandardModelParams makeParams() {
		return new KetosDLParams();
	}
	

}
