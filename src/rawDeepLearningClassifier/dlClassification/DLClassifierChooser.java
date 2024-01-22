package rawDeepLearningClassifier.dlClassification;

import java.net.URI;

import rawDeepLearningClassifier.DLControl;

/**
 * Selects which type of DL classiifer to use. 
 * @author Jamie Macaulay
 * 
 *
 */
public class DLClassifierChooser {
	
	private DLControl dlControl;


	public DLClassifierChooser(DLControl dlControl) {
		this.dlControl = dlControl; 
	}
	
	
	public DLClassiferModel selectClassiferModel(URI modelURI) {
		
		//check for model compatibility. 
		for (DLClassiferModel model: dlControl.getDLModels()) {
			if (model.isModelType(modelURI)) return model; 
		}
		
		//return the generic model. 
		return dlControl.getDLModels().get(dlControl.getDLModels().size()-1); 
	}

}
