package clickTrainDetector.layout.classification.simplechi2classifier;

import PamController.SettingsPane;
import clickTrainDetector.classification.simplechi2classifier.Chi2ThresholdClassifier;
import clickTrainDetector.classification.simplechi2classifier.Chi2ThresholdParams;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilsFX.ControlField;

/**
 * The simple chi^2 classifier pane. 
 * 
 * @author Jamie Macaulay
 *
 */
public class SimpleCTClassifierPane extends SettingsPane<Chi2ThresholdParams>  {
	

	/**
	 * The main pane. 
	 */
	private PamVBox mainPane; 

	/**
	 * The chi^2 threshold. 
	 */
	private ControlField<Double> chi2Threshold;

	/**
	 * Reference to the simple classifier. 
	 */
	private Chi2ThresholdClassifier simpleChi2Classifier;

	/**
	 * Minimum number of clicks.
	 */
	private ControlField<Double> minClicks;

	/**
	 * Minimum number of time. 
	 */
	private ControlField<Double> minTime;

	public SimpleCTClassifierPane(Chi2ThresholdClassifier simpleChi2Classifier) {
		super(null);
		this.simpleChi2Classifier=simpleChi2Classifier; 
		mainPane = createSimpleClassifierPane();
	}
	
	public SimpleCTClassifierPane() {
		super(null);
		mainPane = createSimpleClassifierPane();
	}


	/**
	 * Create simple classifier pane. 
	 */
	private PamVBox createSimpleClassifierPane() {

		PamVBox vBox = new PamVBox(); 
		vBox.setPadding(new Insets(5,0,0,0));
		vBox.setSpacing(5);
		
		// create spinner. 		
		chi2Threshold = new ControlField<Double>("X\u00b2 Threshold		", "", 0.0, Double.MAX_VALUE, 25.0);
		chi2Threshold.getSpinner().getValueFactory().setConverter(PamSpinner.createStringConverter(0));		
		chi2Threshold.setTooltip(new Tooltip(
							"A click train has a X\u00b2 value which is based on the consistancy of inter detection interval \n"
						+ 	"amplitude and other factors. The calculation of X\\\\u00b2 changes depending on the click train \n"
						+ 	"detector is used."));

		minClicks = new ControlField<Double>("Minimum Clicks	", "", 0, Integer.MAX_VALUE, 5);
		minClicks.setTooltip(new Tooltip(
				"The minimum number of detections."));
		minClicks.getSpinner().setEditable(true);

		minTime = new ControlField<Double>("Minimum Time		", "s", 0.0, Double.MAX_VALUE, 1.0);
		minTime.getSpinner().getValueFactory().setConverter(PamSpinner.createStringConverter(2));		
		minTime.setTooltip(new Tooltip(
				"The minimum time for a click train."));
		minTime.getSpinner().setEditable(true);

		chi2Threshold.getSpinner().setEditable(true);
		
		vBox.getChildren().addAll(chi2Threshold, minClicks, minTime);

		return vBox;

	}

	@Override
	public Chi2ThresholdParams getParams(Chi2ThresholdParams currParams) {
		
		currParams.chi2Threshold=chi2Threshold.getSpinner().getValue(); 
		//HACK - for some reason Integer spinner is returning a double
		currParams.minClicks=minClicks.getSpinner().getValue().intValue(); 
		currParams.minTime=minTime.getSpinner().getValue(); 
		
		return currParams;
	}

	@Override
	public void setParams(Chi2ThresholdParams input) {
		chi2Threshold.getSpinner().getValueFactory().setValue(input.chi2Threshold);
		//HACK - for some reason Integer spinner is returning a double
		minClicks.getSpinner().getValueFactory().setValue((double) input.minClicks);
		minTime.getSpinner().getValueFactory().setValue(input.minTime);
	}

	@Override
	public String getName() {
		return "Simple Click Train Classifier Parameters";
	}

	@Override
	public Node getContentNode() {
		return  mainPane; 
	}
	
	/**
	 * Get the main pane. 
	 * @return the main pane. 
	 */
	public PamVBox getMainPane() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub

	}



}
