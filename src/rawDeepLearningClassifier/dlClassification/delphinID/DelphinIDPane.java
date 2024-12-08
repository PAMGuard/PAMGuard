package rawDeepLearningClassifier.dlClassification.delphinID;


import java.io.File;

import PamController.SettingsPane;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelPane;
import rawDeepLearningClassifier.dlClassification.delphinID.DelphinIDParams.DelphinIDDataType;

/**
 * Settings pane for delphin ID. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DelphinIDPane extends SettingsPane<DelphinIDParams> {

	/**
	 * The main pane. 
	 */
	private Pane mainPane;

	/**
	 * Reference to the delphinID classifier 
	 */
	private DelphinIDClassifier delphinUIClassifier;

	private PamSpinner<Double> detectionDensitySpinner;

	private Slider decisionSlider;

	private DelphinIDParams currentParams;

	private File currentSelectedFile;

	private Label detectionDensity;

	private DoubleSpinnerValueFactory whislteValueFactory;

	private DoubleSpinnerValueFactory clickValueFactory;

	private Label minDensityLabel;

	public DelphinIDPane(DelphinIDClassifier delphinUIClassifier) {
		super(null);
		this.delphinUIClassifier = delphinUIClassifier; 
		mainPane =  createPane();
	}

	private Pane createPane() {

		//font to use for title labels. 
		Font font= Font.font(null, FontWeight.BOLD, 11);

		Label classifierIcon;
		classifierIcon = new Label("DelphinID");
		PamGuiManagerFX.titleFont2style(classifierIcon);
		//todo - will need to figure out colour of icon using CSS. 
		Node icon = PamGlyphDude.createPamIcon("mdi2r-rss", Color.BLACK, PamGuiManagerFX.iconSize);
		icon.getStyleClass().add(getName()); 
		icon.setRotate(45);
		classifierIcon.setGraphic(icon);
		classifierIcon.setContentDisplay(ContentDisplay.RIGHT);


		//		String settings = currentParams.toString();
		//		classifierIcon.setTooltip(new Tooltip(settings));

		PamVBox vBox = new PamVBox(); 
		vBox.setSpacing(5.);
		
		clickValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1., Double.MAX_VALUE, 5., 1.);
		whislteValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, 0.3, 0.1);

		/**Classification thresholds etc to set.**/
		detectionDensity = new Label("Detection Density"); 
		detectionDensity.setFont(font);
		String tooltip = "Set the minimum detection density to attempt to classify.";
		detectionDensity.setTooltip(new Tooltip(tooltip));
		detectionDensitySpinner = new PamSpinner<Double>();
		detectionDensitySpinner.setValueFactory(whislteValueFactory);
		detectionDensitySpinner.setPrefWidth(70);
		detectionDensitySpinner.setEditable(true);
		detectionDensitySpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);

		PamHBox minDensityHolder = new PamHBox();
		minDensityHolder.setAlignment(Pos.CENTER_RIGHT);
		minDensityHolder.setSpacing(5);
		minDensityLabel = new Label("Min. detectection density");
		minDensityHolder.getChildren().addAll(minDensityLabel, detectionDensitySpinner);

		/**Classification thresholds etc to set.**/
		Label classiferInfoLabel2 = new Label("Decision Threshold"); 
		classiferInfoLabel2.setTooltip(new Tooltip("Set the minimum prediciton value for selected classes. If a prediction exceeds this value "
				+ "a detection will be saved."));
		classiferInfoLabel2.setFont(font);

		decisionSlider = new Slider();
		decisionSlider.setMin(0);
		decisionSlider.setMax(1);
		decisionSlider.setMajorTickUnit(0.2);
		decisionSlider.setMinorTickCount(10);
		decisionSlider.valueProperty().addListener((obsVal, oldVal, newVal)->{
			classiferInfoLabel2.setText(String.format("Decision Threshold %.2f", newVal));
		});
		decisionSlider.setShowTickMarks(true);
		decisionSlider.setShowTickLabels(true);

		vBox.getChildren().addAll(classifierIcon, detectionDensity, minDensityHolder, classiferInfoLabel2, decisionSlider);

		return vBox;
	}

	@Override
	public DelphinIDParams getParams(DelphinIDParams currParams) {
		currParams.threshold = decisionSlider.getValue();
		currParams.minDetectionValue = detectionDensitySpinner.getValue();
		return currParams;
	}

	@Override
	public void setParams(DelphinIDParams input) {
		this.currentParams = input;
		decisionSlider.setValue(input.threshold);
		detectionDensitySpinner.getValueFactory().setValue(input.minDetectionValue);
				
		if (input.getDataType()==null) {
			input.dataType = DelphinIDDataType.WHISTLES;
		}
		
		//set the correct label and minimum detection value
		switch (input.getDataType()) {
		case CLICKS:
			this.minDensityLabel.setText("Minimum no. clicks");
			minDensityLabel.setTooltip(new Tooltip("Set the minimum number of clicks before a segment is classified"));
			detectionDensitySpinner.setValueFactory(clickValueFactory);
			break;
		case WHISTLES:
			minDensityLabel.setTooltip(new Tooltip("Set the minimum  whistle density before a segment is classified"));
			this.minDensityLabel.setText("Minimum whistle density");
			detectionDensitySpinner.setValueFactory(whislteValueFactory);
			break;
		default:
			break;
		
		}

		if (input.modelPath!=null) {
			//this might 
			currentSelectedFile = new File(currentParams.modelPath);

			//this might change the paramsClone values if the model contains pamguard compatible metadata
			newModelSelected(currentSelectedFile); 
		}
	}

	private void newModelSelected(File currentSelectedFile2) {
		if (currentParams!=null && currentParams.defaultSegmentLen != null) {

			//System.out.println("Defualt segment length: " + paramsClone.defaultSegmentLen); 

			//cannot use because, if the parent datablock has changed, samplerate will be out of date. 
			//			int defaultsamples = (int) this.soundSpotClassifier.millis2Samples(paramsClone.defaultSegmentLen); 


			//			float sR = dlClassifierModel.getDLControl().getSettingsPane().getSelectedParentDataBlock().getSampleRate(); 

			int defaultsamples =  StandardModelPane.getDefaultSamples(delphinUIClassifier, currentParams);

			//work out the window length in samples
			delphinUIClassifier.getDLControl().getSettingsPane().getSegmentLenSpinner().getValueFactory().setValue(defaultsamples);
			//			dlClassifierModel.getDLControl().getSettingsPane().getHopLenSpinner().getValueFactory().setValue((int) defaultsamples/2);

			delphinUIClassifier.getDLControl().getSettingsPane().getSegmentLenSpinner().setDisable(true); 
		}
		else {
			delphinUIClassifier.getDLControl().getSettingsPane().getSegmentLenSpinner().setDisable(false); 
		}

	}



	@Override
	public String getName() {
		return "delphinIDParams";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub

	}

}
