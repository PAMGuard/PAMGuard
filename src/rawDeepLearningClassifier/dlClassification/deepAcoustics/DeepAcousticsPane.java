package rawDeepLearningClassifier.dlClassification.deepAcoustics;

import java.io.File;

import PamView.dialog.warn.WarnOnce;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamSpinner;
import rawDeepLearningClassifier.DLStatus;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelPane;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;


/**
 * Settings pane for the Deep Acoustics classifier.
 * This pane allows users to set parameters specific to the Deep Acoustics model, such as confidence thresholds.
 * 
 * @author Jamie Macaulay
 *
 */
public class DeepAcousticsPane extends StandardModelPane {

	private DeepAcousticsClassifier deepAcousticClassifier;

	private PamSpinner<Double> confidenceSpinner;


	public DeepAcousticsPane(DeepAcousticsClassifier deepAcousticClassifier) {
		super(deepAcousticClassifier);
		this.deepAcousticClassifier = deepAcousticClassifier; 

		addConfidenceControls();
	}

	/**
	 * Adds the confidence controls to the pane.
	 * This method is called in the constructor to ensure the controls are added when the pane is created.
	 */
	private void addConfidenceControls() {
		this.getVBoxHolder().getChildren().add(3, createConfidenceControls());
	}

	/**
	 * Creates the confidence controls pane.
	 * This pane contains a spinner to set the minimum confidence threshold for detections.
	 * 
	 * @return Pane containing the confidence controls.
	 */
	private Pane createConfidenceControls() {

		/**
		 * There are tow classifiers the detector and the classifier
		 */
		PamGridPane gridPane = new PamGridPane(); 
		gridPane.setHgap(5);
		gridPane.setVgap(5);

		gridPane.add(new Label("Min. confidence"), 0, 0);
		gridPane.add(confidenceSpinner = new PamSpinner<Double>(0.0, 1.0, 0.9, 0.1), 1, 0);
		confidenceSpinner.setPrefWidth(70);
		confidenceSpinner.setEditable(true);
		confidenceSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		confidenceSpinner.setTooltip(new Tooltip("Set the minimum confidence threshold, if the confidence of a detection is greater than this value it will be considered a valid detection. " +
				"Lowering this value will increase the number of detections, but may also increase false positives."));

		return gridPane;
	}


	@Override
	public DLStatus newModelSelected(File file) {

		//the model has to set some of the parameters for the UI . 

		//A ketos model contains information on the transforms, duration and the class names. 
		this.setCurrentSelectedFile(file);

		if (this.getParamsClone()==null) {
			this.setParamsClone(new DeepAcousticParams()); 
		}


		StandardModelParams params  = getParamsClone(); 


		//prep the model with current parameters; 

		/**
		 * Note that the model prep will determine whether new transforms need to be loaded from the 
		 * model or to use the existing transforms in the settings. 
		 */
		DLStatus status = deepAcousticClassifier.getDLWorker().prepModel(params, deepAcousticClassifier.getDLControl());

		//get the model transforms calculated from the model by the worker and apply them to our temporary params clone. 
		getParamsClone().dlTransfroms = this.deepAcousticClassifier.getDLWorker().getModelTransforms(); 

		//		if (getParamsClone().defaultSegmentLen!=null) {
		//			usedefaultSeg.setSelected(true);
		//		}

		///set the advanced pane parameters. 
		getAdvSettingsPane().setParams(getParamsClone());

		return status;

	}


	@Override
	public StandardModelParams getParams(StandardModelParams currParams) {
		DeepAcousticParams deepParams  = (DeepAcousticParams)  super.getParams(currParams);

		deepParams.minConfidence = confidenceSpinner.getValue();

		return deepParams;
	}

	@Override
	public void setParams(StandardModelParams currParams) {
		super.setParams(currParams);
		
		DeepAcousticParams deepParams = (DeepAcousticParams) currParams;
		confidenceSpinner.getValueFactory().setValue(deepParams.minConfidence);

	}



}