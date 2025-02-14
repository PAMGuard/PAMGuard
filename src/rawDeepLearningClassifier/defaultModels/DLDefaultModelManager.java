package rawDeepLearningClassifier.defaultModels;

import java.util.ArrayList;

import PamModel.SMRUEnable;
import rawDeepLearningClassifier.DLControl;

/**
 * Manages default models. 
 */
public class DLDefaultModelManager {
	
	/**
	 * A list of the default models
	 */
	private ArrayList<DLModel> defaultModels = new ArrayList<DLModel>();
	
	/**
	 * Reference ot the DL control. 
	 */
	private DLControl dlControl;  

	/**
	 * Constructor for the Defulat Model Manager. 
	 * @param dlControl - reference to the controller for this model manager. 
	 */
	public DLDefaultModelManager(DLControl dlControl) {
		this.dlControl = dlControl; 
		defaultModels.add(new RightWhaleModel1());
		defaultModels.add(new HumpbackWhaleGoogle());
		defaultModels.add(new HumpbackWhaleAtlantic());
		if (SMRUEnable.isDevEnable()) {
			defaultModels.add(new MultiSpeciesGoogle());
		}
	}

	
	/**
	 * Get a default model at index i
	 * @param i - the index of the default model
	 */
	public DLModel getDefaultModel(int i) {
		return defaultModels.get(i);
	}
	

	/**
	 * Get the number of default models 
	 * @return the number of default models. 
	 */
	public int getNumDefaultModels() {
		return defaultModels.size();
	}
	
}
