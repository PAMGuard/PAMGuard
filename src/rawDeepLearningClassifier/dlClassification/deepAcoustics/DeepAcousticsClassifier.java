package rawDeepLearningClassifier.dlClassification.deepAcoustics;

import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelClassifier;


/**
 * A classifier based on the Deep Acoustics method which uses object detection models within spectrograms to predict
 * dolphin whistle detections and then classify to species.. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DeepAcousticsClassifier extends ArchiveModelClassifier {

	public DeepAcousticsClassifier(DLControl dlControl) {
		super(dlControl);
	}
	
	@Override
	public String getName() {
		//important because this is used to identify model from JSON file
		return "deepacoustics";
	}
	
	


}
