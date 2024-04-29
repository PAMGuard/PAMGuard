package rawDeepLearningClassifier.dlClassification;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javafx.stage.FileChooser.ExtensionFilter;
import rawDeepLearningClassifier.DLControl;

public class DefaultModels {
	
	/**
	 * The default model list. 
	 */
	ArrayList<DefualtModel> defaultModels;
	
	/**
	 * The dlControl.
	 */
	private DLControl dlControl; 
	
	public DefaultModels(DLControl dlControl) {
		this.dlControl = dlControl; 
		defaultModels = new	ArrayList<DefualtModel>(); 
		
		/***/
		
		defaultModels.add(new DefualtModel("Right Whale", null)); 


	}
	
	
	public class DefualtModel {
		
		public DefualtModel(String name, URI filename) {
			this.name = name;
			this.filename = filename; 
		}

		/**
		 * The name of the model. 
		 */
		public String name;
		
		/**
		 * The filename of the model 
		 */
		public  URI filename;
		
		/**
		 * Get the DLClassifier associated with the model. 
		 * Technically this could be overridden to provide a different DLClassifierModel. 
		 * @return the default model classifier. 
		 */
		public DLClassiferModel getDLClassifier() {
			return dlControl.getDlClassifierChooser().selectClassiferModel(filename); 
		}
				
	}

	/**
	 * Get the default models. 
	 * @return the default models. 
	 */
	public List<DefualtModel> getDefaultModels() {
		return defaultModels;
	}
	
}
