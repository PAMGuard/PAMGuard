package rawDeepLearningClassifier.dlClassification.deepAcoustics;

import java.io.File;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
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

	private PamSpinner<Double> mergeSpinner;

	private Spinner<Double> mergePercentageSpinner;

	private PamToggleSwitch mergBoxesSwitch;

	public DeepAcousticsPane(DeepAcousticsClassifier deepAcousticClassifier) {
		super(deepAcousticClassifier);
		this.deepAcousticClassifier = deepAcousticClassifier; 



		PamHBox hBox = new PamHBox();
		hBox.setSpacing(5);
		hBox.setAlignment(Pos.CENTER_LEFT);
		
		PamGridPane gridPane = getPredictionGridPane();

		int row = 1; 

		//create a new label for the confidence controls.
		createConfidenceControls(gridPane, row) ;
		row++;

		//create a new label for the bounding box merge controls.
		Label label = new Label("Overlapping detection merge");
		//font to use for title labels. 
		Font font= Font.font(null, FontWeight.BOLD, 11);
		label.setFont(font);

		
		//hmmmm...not great as this assumes we will never add anything to the vbox from the 
		//parent class...
		this.getVBoxHolder().getChildren().addAll(label, createMergeControls()); 
	
	}

	private Pane createMergeControls() {
		
		Label label1 = new Label("with >=");
		Label label2 = new Label("overlap");
		
		mergBoxesSwitch = new PamToggleSwitch("Merge detections");
		mergBoxesSwitch.selectedProperty().addListener((obsVal, oldVal, newVal)->{
			mergePercentageSpinner.setDisable(!newVal); 
			label1.setDisable(!newVal); 
			label2.setDisable(!newVal); 
		});
		
		PamHBox hBox = new PamHBox();
		hBox.setSpacing(5);
		hBox.setAlignment(Pos.CENTER_LEFT);
		

		hBox.getChildren().add(mergBoxesSwitch);
		hBox.getChildren().add(label1);

		//Spinner for the merge percentage

		// Create a Spinner with a Double value factory
		mergePercentageSpinner = new Spinner<>();
		mergePercentageSpinner.setEditable(true);
		mergePercentageSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		mergePercentageSpinner.setTooltip(new Tooltip("Set the minimum required overlap of two detections before they are merged. For example if set to 50% "
				+ "then the time frequency area of a detection has to overlap by at least 50% for two detecitons to be merged"));
		
		// Define a value factory for the spinner. 
		// Parameters are: min value, max value, initial value, increment step.
		double minValue = 0.0;
		double maxValue = 100.0;
		double initialValue = 50.0;
		double step = 1.0;
		SpinnerValueFactory<Double> valueFactory = 
				new SpinnerValueFactory.DoubleSpinnerValueFactory(minValue, maxValue, initialValue, step);
		mergePercentageSpinner.setValueFactory(valueFactory);

		// Create a custom StringConverter
		StringConverter<Double> converter = new StringConverter<>() {

			// This method formats the number for display in the Spinner's text field
			@Override
			public String toString(Double object) {
				// Return the number followed by a percentage sign
				if (object == null) {
					return "";
				}
				return String.format("%.1f%%", object);
			}

			// This method is used to parse the String back to a Double,
			// which is needed if the user types a value directly.
			@Override
			public Double fromString(String string) {
				if (string == null || string.isEmpty()) {
					return null;
				}
				// Remove the percentage sign and parse the remaining string
				String cleanedString = string.replace("%", "").trim();
				try {
					return Double.parseDouble(cleanedString);
				} catch (NumberFormatException e) {
					return null; // Handle potential errors gracefully
				}
			}
		};

		// Set the custom converter on the spinner's editor and value factory
		mergePercentageSpinner.getEditor().setTextFormatter(
				new javafx.scene.control.TextFormatter<>(converter, initialValue, c -> {
					try {
						// This is an optional part to validate user input as they type
						if (c.getControlNewText().isEmpty()) {
							return c;
						}
						Double.parseDouble(c.getControlNewText().replace("%", ""));
						return c;
					} catch (NumberFormatException e) {
						return null; // Reject invalid characters
					}
				}));
		valueFactory.setConverter(converter);


		hBox.getChildren().add(mergePercentageSpinner);
		hBox.getChildren().add(label2);

		return hBox;
	}

	/**
	 * Creates the confidence controls pane.
	 * This pane contains a spinner to set the minimum confidence threshold for detections.
	 * 
	 * @return Pane containing the confidence controls.
	 */
	private Pane createConfidenceControls(PamGridPane gridPane, int row) {

		/**
		 * There are two classifiers the detector and the classifier
		 */
		gridPane.add(new Label("Min. confidence"), 0, row);
		gridPane.add(confidenceSpinner = new PamSpinner<Double>(0.0, 1.0, 0.9, 0.1), 1, row);
		confidenceSpinner.setPrefWidth(60);
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
		
		deepParams.minMergeOverlap =  mergePercentageSpinner.getValue().floatValue()/100.0f; 
		deepParams.mergeOverlap = mergBoxesSwitch.isSelected();

		return deepParams;
	}

	
	@Override
	public void setParams(StandardModelParams currParams) {
		super.setParams(currParams);

		DeepAcousticParams deepParams = (DeepAcousticParams) currParams;
		confidenceSpinner.getValueFactory().setValue(deepParams.minConfidence);
		
		mergePercentageSpinner.getValueFactory().setValue((double) deepParams.minMergeOverlap*100.0);
		mergePercentageSpinner.setDisable(!deepParams.mergeOverlap); 

		mergBoxesSwitch.setSelected(deepParams.mergeOverlap);
		
		enableControls(currParams); 	
	}

	
	private void enableControls(StandardModelParams currParams) {
		if (currParams!=null && currParams.modelPath!=null) {
			this.deepAcousticClassifier.getDLControl().getSettingsPane().getMaxRemergeSpinner().setDisable(true);

		}
		else {
			this.deepAcousticClassifier.getDLControl().getSettingsPane().getMaxRemergeSpinner().setDisable(false);

		}
		
	}



}