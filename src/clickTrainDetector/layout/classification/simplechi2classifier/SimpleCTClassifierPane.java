package clickTrainDetector.layout.classification.simplechi2classifier;

import org.controlsfx.control.PopOver;

import PamController.SettingsPane;
import PamguardMVC.PamDataBlock;
import clickTrainDetector.classification.simplechi2classifier.Chi2ThresholdClassifier;
import clickTrainDetector.classification.simplechi2classifier.Chi2ThresholdParams;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
import pamViewFX.fxNodes.utilsFX.ControlField;

/**
 * The simple chi^2 classifier pane. 
 * 
 * @author Jamie Macaulay
 *
 */
public class SimpleCTClassifierPane extends SettingsPane<Chi2ThresholdParams>  {
	

	private static final double LABEL_WIDTH = 115;

	/**
	 * The main pane. 
	 */
	private Pane mainPane; 

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

	private PamToggleSwitch dataSelectorCheckBox;

	private PamButton dataSelectorButton;

	private PopOver popOver;

	public SimpleCTClassifierPane(Chi2ThresholdClassifier simpleChi2Classifier) {
		super(null);
		this.simpleChi2Classifier=simpleChi2Classifier; 
		mainPane = createSimpleClassifierPane(Orientation.VERTICAL);
	}
	
	public SimpleCTClassifierPane() {
		super(null);
		mainPane = createSimpleClassifierPane(Orientation.VERTICAL);
	}
	
	public SimpleCTClassifierPane(Chi2ThresholdClassifier simpleChi2Classifier, Orientation orientation) {
		super(null);
		this.simpleChi2Classifier=simpleChi2Classifier; 
		mainPane = createSimpleClassifierPane(orientation);
	}


	/**
	 * Create simple classifier pane. 
	 */
	private Pane createSimpleClassifierPane(Orientation orientation) {

		Pane vBox;
		
		if (orientation == Orientation.VERTICAL) {
			vBox = new PamVBox(); 
			((PamVBox) vBox).setSpacing(5);
		}
		else {
			vBox = new PamHBox(); 
			((PamHBox) vBox).setSpacing(5);
		}

		vBox.setPadding(new Insets(5,0,0,0));
		
		PamHBox pamHBox = new PamHBox();
		pamHBox.setSpacing(5);
	
		
		// create spinner. 		
		chi2Threshold = new ControlField<Double>("X\u00b2 Threshold		", "", 0.0, Double.MAX_VALUE, 25.0);
		chi2Threshold.getSpinner().getValueFactory().setConverter(PamSpinner.createStringConverter(0));		
		chi2Threshold.setTooltip(new Tooltip(
							"A click train has a X\u00b2 value which is based on the consistancy of inter detection interval \n"
						+ 	"amplitude and other factors. The calculation of X\u00b2 changes depending on the click train \n"
						+ 	"detector is used."));
		chi2Threshold.getLabel1().setPrefWidth(LABEL_WIDTH);

		minClicks = new ControlField<Double>("Min. Clicks	", "", 0, Integer.MAX_VALUE, 5);
		minClicks.setTooltip(new Tooltip(
				"The minimum number of detections."));
		minClicks.getSpinner().setEditable(true);
		minClicks.getLabel1().setPrefWidth(LABEL_WIDTH);
		
	
		

		minTime = new ControlField<Double>("Min. Time		", "s", 0.0, Double.MAX_VALUE, 1.0);
		minTime.getSpinner().getValueFactory().setConverter(PamSpinner.createStringConverter(2));		
		minTime.setTooltip(new Tooltip(
				"The minimum time for a click train."));
		minTime.getSpinner().setEditable(true);
		minTime.getLabel1().setPrefWidth(LABEL_WIDTH);

		chi2Threshold.getSpinner().setEditable(true);
		
		vBox.getChildren().addAll(chi2Threshold, minClicks, minTime);

		return vBox;

	}
	
	/**
	 * Create the data selector. 
	 * @return the data selector. 
	 */
	private Pane createDataSelectorPane() {
		PamHBox hbox = new PamHBox();
		hbox.setSpacing(5);
		hbox.setAlignment(Pos.CENTER_LEFT);

		dataSelectorButton = new PamButton();
//		dataSelectorButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS, PamGuiManagerFX.iconSize));
		dataSelectorButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize));
		dataSelectorButton.setOnAction((action)->{
			showDataSelectorPane();
		});
		hbox.getChildren().addAll(dataSelectorButton);
		return hbox; 
	}
	
	
	/**
	 * Creates pane allowing the user to change fine scale things such as error limits. 
	 * @return the pop over pane. 
	 */
	public void showDataSelectorPane() {
		
		

		if (popOver==null) {
			popOver = new PopOver(); 
			PamBorderPane holder = new PamBorderPane(simpleChi2Classifier.getDataSelector().getDialogPaneFX().getContentNode()); 
			holder.setPadding(new Insets(5,5,5,5));
			popOver.setContentNode(holder);
		}

		popOver.showingProperty().addListener((obs, old, newval)->{
			if (newval) {
				simpleChi2Classifier.getDataSelector().getDialogPaneFX().setParams(true);
			}
		});

		popOver.show(dataSelectorButton);
	} 

	@Override
	public Chi2ThresholdParams getParams(Chi2ThresholdParams currParams) {
		
		currParams.chi2Threshold=chi2Threshold.getSpinner().getValue(); 
		//HACK - for some reason Integer spinner is returning a double
		currParams.minClicks=minClicks.getSpinner().getValue().intValue(); 
		currParams.minTime=minTime.getSpinner().getValue(); 
		
		if (simpleChi2Classifier!=null && simpleChi2Classifier.getDataSelector()!=null) {
			simpleChi2Classifier.getDataSelector().getDialogPaneFX().getParams(true); 
		}
		
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
	public Pane getMainPane() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub

	}
	



}
