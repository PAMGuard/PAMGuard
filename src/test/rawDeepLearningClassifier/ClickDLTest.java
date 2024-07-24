package test.rawDeepLearningClassifier;

import org.junit.jupiter.api.Test;

import rawDeepLearningClassifier.dlClassification.genericModel.GenericModelParams;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericModelWorker;

public class ClickDLTest {

	
	@Test
	public void clickDLTest() {
		//relative paths to the resource folders.
		System.out.println("*****Click classification Deep Learning*****"); 

		//relative paths to the resource folders.
		String relModelPath  =	"D:/Dropbox/PAMGuard_dev/Deep_Learning/click_classifier_Thomas/best_model/saved_model.pb";


		GenericModelWorker genericModelWorker = new GenericModelWorker(); 

		GenericModelParams genericModelParams = new GenericModelParams(); 

		genericModelParams.modelPath = relModelPath;
		
		genericModelWorker.prepModel(genericModelParams, null);

	
	}
	
}
