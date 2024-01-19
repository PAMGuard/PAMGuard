package rawDeepLearningClassifier.defaultModels;

import java.net.URI;

import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;

/**
 * A default model that can be loaded from a file or a URL. 
 */
public interface DLModel {
	
	/**
	 * Get a brief description of the model. 
	 * @return a brief description of the model. 
	 */
	public String getDescription();
	
	/**
	 * Get a name for the model
	 * @return
	 */
	public String getName();
	
	/**
	 * Get the citation for the model
	 * @return - the citation.
	 */
	public String getCitation();
	
	/**
	 * Get the URI to the model file
	 * @return the model URI. 
	 */
	public URI getModelURI();
	
	/**
	 * Get the model settings - these are used to ensure the model is set up correctly once loaded.  
	 */
	public StandardModelParams getModelSettings(); 
	
	/**
	 * The model name. This is used if, for example, a model is downloaded as a zip file and the model
	 * file is located somewhere within the saved folder. For Tensorflow models this will often be saved_model.pb
	 * @return the model name; 
	 */
	public String getModelName();
	
	

}
