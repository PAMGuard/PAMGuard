package rawDeepLearningClassifier.dlClassification.deepAcoustics;

import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.yolo.YoloClassifier;


/**
 * A classifier based on the Deep Acoustics method which uses object detection
 * models within spectrograms to predict dolphin whistle detections and then
 * classify to species.
 * <p>
 * Deep Acoustics models are YOLO models and so this is a YoloClassifier with
 * its own name (which is used to identify the model from its JSON file) and
 * parameters.
 *
 * @author Jamie Macaulay
 *
 */
public class DeepAcousticsClassifier extends YoloClassifier {

	public static String MODEL_NAME = "DeepAcoustics";


	public DeepAcousticsClassifier(DLControl dlControl) {
		super(dlControl);
	}


	@Override
	public String getName() {
		//important because this is used to identify model from JSON file
		return MODEL_NAME;
	}

	@Override
	public StandardModelParams makeParams() {
		return new DeepAcousticParams();
	}

}
