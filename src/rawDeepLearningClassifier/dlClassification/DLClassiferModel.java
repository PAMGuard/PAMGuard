package rawDeepLearningClassifier.dlClassification;

import java.io.Serializable;
import java.util.ArrayList;

import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.layoutFX.DLCLassiferModelUI;
import rawDeepLearningClassifier.segmenter.SegmenterProcess.GroupedRawData;
import warnings.PamWarning;

/**
 * The classifier model. Each classifier must satisfy this interface.
 * 
 * @author Jamie Macaulay
 *
 */
public interface DLClassiferModel {

	/**
	 * Run the deep learning model on a list of grouped raw data units and return a
	 * corresponding list of model results.
	 * <p>
	 * Note the reason we use list is that often it is more efficient to get a model
	 * to predict a stacked group of inputs rather than one at a time.
	 * 
	 * @return the deep learning model.
	 */
	public ArrayList<? extends PredictionResult> runModel(ArrayList<GroupedRawData> rawDataUnit);

	/**
	 * Prepare the model. This is called on PAMGuard start up.
	 */
	public void prepModel();

	/**
	 * Called whenever PAMGuard stops.
	 */
	public void closeModel();

	/**
	 * Get the name of the model.
	 * 
	 * @return the name of the model.
	 */
	public String getName();

	/**
	 * Get any UI components for the model. Can be null.
	 * 
	 * @return UI components for the model.
	 */
	public DLCLassiferModelUI getModelUI();

	/**
	 * A settings object that can be saved.
	 * 
	 * @return the settings object.
	 */
	public Serializable getDLModelSettings();

	/**
	 * Get the number of output classes.
	 * 
	 * @return the number of output classes.
	 */
	public int getNumClasses();

	/**
	 * Get the number of output classes.
	 * 
	 * @return the number of output classes.
	 */
	public DLClassName[] getClassNames();

	/**
	 * Reference to the dlControl
	 * @return reference to the DL control. 
	 */
	public DLControl getDLControl();

	/**
	 * Check whether a model has been selected and can be loaded successfully. 
	 */
	public boolean checkModelOK();
	
	/**
	 * Get warnings for the classifier model. This is called when the user confirms settings and 
	 * used to return a warning dialog. 
	 * @return a list of warnings. If the list is null or size() is zero then settings are OK. 
	 */
	public ArrayList<PamWarning> checkSettingsOK();
	
	

}
