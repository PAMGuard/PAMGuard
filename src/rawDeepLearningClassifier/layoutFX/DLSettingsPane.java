package rawDeepLearningClassifier.layoutFX;

import java.io.Serializable;
import java.util.ArrayList;

import org.controlsfx.control.PopOver;
import PamController.PamGUIManager;
import PamController.SettingsPane;
import PamDetection.RawDataUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.dataSelector.NullDataSelectorCreator;
import clickDetector.ClickDetection;
import clipgenerator.ClipDataUnit;
import javafx.application.Platform;
import javafx.util.Duration;
import javafx.scene.text.Text;
import javafx.scene.Cursor;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.DoubleProperty;
import javafx.animation.Timeline;
import javafx.animation.KeyValue;
import javafx.animation.KeyFrame;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.GroupedSourcePaneFX;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
import pamViewFX.validator.PamValidator;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.DLStatus;
import rawDeepLearningClassifier.RawDLParams;
import rawDeepLearningClassifier.dlClassification.DLClassiferModel;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import warnings.PamWarning;

/**
 * The settings pane. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DLSettingsPane  extends SettingsPane<RawDLParams>{


	public static double MAX_WIDTH = 330; 

	/**
	 * The source for the FFT data source.  
	 */
	private GroupedSourcePaneFX sourcePane;

	/**
	 * Reference to DL control 
	 */
	private DLControl dlControl;

	/**
	 * The main pane. 
	 */
	private PamBorderPane mainPane;

	//	/**
	//	 * Combo box which allows users to select model. 
	//	 */
	//	private ComboBox<String> dlModelBox;

	/**
	 * The window length spinner for the segmenter process
	 */
	private DLPamSpinner<Integer> windowLength;

	/**
	 * The hop length for the segmenter progress. 
	 */
	private DLPamSpinner<Integer> hopLength;

	/**
	 * The pane in which the classiifer pane sits. 
	 */
	private PamBorderPane classifierPane;

	/**
	 * Set the maximum number of segments that can be re-merged
	 */
	private PamSpinner<Integer>  reMergeSeg;

	/**
	 * Enables or disables the data selector
	 */
	private PamToggleSwitch dataSelectorCheckBox;

	/**
	 * Show the advance settings. 
	 */
	private PamButton dataSelectorButton;

	/**
	 * Pop over
	 */
	private PopOver popOver;

	/**
	 * Pane which holds the data selector. 
	 */
	private HBox dataSelectorPane;

	private Label infoLabel;

	private DLModelSelectPane modelSelectPane;

	private PamToggleSwitch segEnableSwitch;

	private PamGridPane segmenterGridPane;


	private ArrayList<Class> currentAllowedDataTypes;

	private DLWarningDialog dlWarningDialog ;

	private String currentClassifierModel;

	private PamValidator validator;

	/**
	 * Warning sign shown next to the window/hop info label if the segment length does
	 * not match the default segment length of the model e.g. because the source sample
	 * rate has changed.
	 */
	private Label segWarningIcon;

	/**
	 * The icon inside segWarningIcon. Its colour is animated when the warning appears.
	 */
	private Text segWarningGlyph;

	/**
	 * Label in the tooltip which describes the segment length mismatch.
	 */
	private Label segWarningTooltipLabel;

	/**
	 * Label in the pop over which describes the segment length mismatch.
	 */
	private Label segWarningPopLabel;

	/**
	 * Pop over shown when the user clicks the warning sign.
	 */
	private PopOver segWarningPopOver;

	/**
	 * Animation which pulses the warning sign between red and orange.
	 */
	private Timeline segWarningPulse;

	/**
	 * Fraction between orange (0) and red (1) used by the pulse animation.
	 */
	private final DoubleProperty segWarningRedness = new SimpleDoubleProperty(0);

	private static final Color SEG_WARN_ORANGE = Color.web("#ef6c00");

	private static final Color SEG_WARN_RED = Color.web("#d50000");


	public DLSettingsPane(DLControl dlControl){
		super(null); 
		this.dlControl=dlControl; 
		 validator = new PamValidator();
		//		Button newButton=new Button("Test");
		//		newButton.setOnAction((action)-> {
		//			pane.layout();
		//			pamTabbedPane.layout();
		//			Stage stage = (Stage) this.getScene().getWindow();
		//			stage.sizeToScene();
		//		});
		//		this.setTop(newButton);


		mainPane=new PamBorderPane(); 
		mainPane.setCenter(createDLPane());
		mainPane.setPadding(new Insets(5,5,5,5));

		//bit messy but have to set different sizes for FX and Swing due to differences in how they handle sizing.
		if (!PamGUIManager.isFX()){
			mainPane.setMinHeight(460);
			mainPane.setMaxWidth(MAX_WIDTH);
			mainPane.setPrefWidth(MAX_WIDTH);
		}
		//this.getAdvPane().setMaxWidth(MAX_WIDTH);
		dlWarningDialog = new DLWarningDialog(this); 
		


		//mainPane.getStylesheets().add(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getDialogCSS()); 

	}


	/**
	 * Create Pane for changing FFT settings. 
	 * @return pane for changing FFT settings 
	 */
	private Pane createDLPane(){

		PamVBox vBox=new PamVBox();
		vBox.setSpacing(5);

		sourcePane = new GroupedSourcePaneFX("Raw Sound Data", null, true, false, true);
		excludeOwnDataBlocks();
		setDefaultSourceList();
		
//		sourcePane.addSourceType(ClickDetection.class, false);
//		sourcePane.addSourceType(ClipDataUnit.class, false);
//		sourcePane.addSourceType(ConnectedRegionDataUnit.class, false);
		

		vBox.getChildren().add(sourcePane);
		sourcePane.prefWidthProperty().bind(vBox.widthProperty());
		sourcePane.setMaxWidth(Double.MAX_VALUE);

		sourcePane.getDataBlockBox().setOnAction((action)->{
			//need to create a data selector if one exists. 
			this.dlControl.createDataSelector(getSelectedParentDataBlock()); 
			//enable the controls to show a data selector or not. 
			enableControls(); 
			
			this.setSegInfoLabel();//refresh label
		});

		//create the detection
		vBox.getChildren().add(createDataSelectorPane());


		// the segmentation parameters
		Label segLabel = new Label("Segmentation"); 
		PamGuiManagerFX.titleFont2style(segLabel); 


		segEnableSwitch = new PamToggleSwitch("Enable");
		segEnableSwitch.selectedProperty().addListener((obsVal, oldVal, newVal)->{
			enableControls(); 
		});
		segEnableSwitch.setVisible(false); //set visible by default
		segEnableSwitch.setAlignment(Pos.CENTER_RIGHT);
		

		//segmentation can have an option to disable for certain input datablocks
		PamBorderPane segmenterPane = new PamBorderPane();
		segmenterPane.setLeft(segLabel);
		segmenterPane.setRight(segEnableSwitch);

		//add to the main pane
		vBox.getChildren().add(segmenterPane);

		windowLength = new DLPamSpinner<Integer>(0, Integer.MAX_VALUE, 10,  10000); 
		windowLength.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		windowLength.setEditable(true);
		windowLength.valueProperty().addListener((obsVal, oldVal, newVal)->{
			setSegInfoLabel(); 
		});

		hopLength =    new DLPamSpinner<Integer>(0, Integer.MAX_VALUE, 10,  10000); 
		hopLength.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		hopLength.setEditable(true);
		hopLength.valueProperty().addListener((obsVal, oldVal, newVal)->{
			setSegInfoLabel(); 
		});
		
		 validator.createCheck()
         .dependsOn(("hop_length" + this), hopLength.valueProperty())
         .withMethod(c -> {
           if (hopLength.getValue()>windowLength.valueProperty().get()) {
	              c.error("The hop length cannot be greater than the segment length.");
           }
         })
         .decorates(hopLength)
         .immediate();

		//re-merge segments spinner - the minimum value should be 1 which means no re-merging.
		reMergeSeg =    new PamSpinner<Integer>(1, Integer.MAX_VALUE, 1,  1); 
		reMergeSeg.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		reMergeSeg.setEditable(true);

		//button to set default hop size
		Button defaultButton = new Button();
		defaultButton.getStyleClass().add("icon-button");
		//		defaultButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.REFRESH, PamGuiManagerFX.iconSize-3));
		defaultButton.setGraphic(PamGlyphDude.createPamIcon("mdi2r-refresh", PamGuiManagerFX.iconSize-3));
		defaultButton.setTooltip(new Tooltip("Set default hop size"));
		defaultButton.setOnAction((action)->{

			hopLength.getValueFactory().setValue(Math.round(windowLength.getValue()/2));
		});

		segmenterGridPane = new PamGridPane(); 
		segmenterGridPane.setHgap(5);

		ColumnConstraints col1 = new ColumnConstraints();
		col1.setHgrow( Priority.SOMETIMES );
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setHgrow( Priority.ALWAYS );
		segmenterGridPane.getColumnConstraints().addAll(col1, col2 );

		segmenterGridPane.add(new Label("Window length"), 0, 0);
		segmenterGridPane.add(windowLength, 1, 0);
		segmenterGridPane.add(new Label("samples"), 2, 0);

		segmenterGridPane.add(new Label("Hop length"), 0, 1);
		segmenterGridPane.add(hopLength, 1, 1);
		segmenterGridPane.add(new Label("samples"), 2, 1);
		segmenterGridPane.add(defaultButton, 3, 1);

		segmenterGridPane.add(new Label("Max. re-merge"), 0, 2);
		segmenterGridPane.add(reMergeSeg, 1, 2);
		segmenterGridPane.add(new Label("segments"), 2, 2);

		vBox.getChildren().add(segmenterGridPane);

		infoLabel = new Label();
		HBox infoBox = new HBox(5, infoLabel, createSegWarningIcon());
		infoBox.setAlignment(Pos.CENTER_LEFT);
		vBox.getChildren().add(infoBox);


		Label label2 = new Label("Deep Learning Model"); 
		label2.setPadding(new Insets(5,0,0,0));
		PamGuiManagerFX.titleFont2style(label2);

		vBox.getChildren().add(label2);

		//Pane which allows users to select a model from a file, link or default model. 
		modelSelectPane = new DLModelSelectPane(this); 

		//create pane which shows the classifier settings
		classifierPane = new PamBorderPane(); 

		//bump this in case no settings
		segEnableSwitch.setSelected(true);

		vBox.getChildren().addAll(modelSelectPane, classifierPane);

		return vBox; 
	}


	/**
	 * Create the data selector. 
	 * @return the data selector. 
	 */
	private Pane createDataSelectorPane() {
		dataSelectorPane = new PamHBox();
		dataSelectorPane.setSpacing(5);
		dataSelectorPane.setAlignment(Pos.CENTER_LEFT);

		dataSelectorCheckBox = new PamToggleSwitch("Detection Selector"); 
		dataSelectorCheckBox.selectedProperty().addListener((obsval, oldval, newval)->{
			enableControls(); 
		});
		dataSelectorButton = new PamButton();
		dataSelectorButton.getStyleClass().add("icon-button");
		//		dataSelectorButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS, PamGuiManagerFX.iconSize));
		dataSelectorButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize));
		dataSelectorButton.setOnAction((action)->{
			showAdvPane();
		});
		dataSelectorPane.getChildren().addAll(dataSelectorCheckBox, dataSelectorButton);
		return dataSelectorPane; 
	}

	/**
	 * Set extra information in the info label. 
	 */
	private void setSegInfoLabel() {
		String text; 
		if (sourcePane.getSource()==null) {
			text = String.format("Window - s Hop: - s (no source data)"); 
		}
		else {
			float sR =  sourcePane.getSource().getSampleRate(); 
			double windowLenS = windowLength.getValue()/sR;
			double hopLengthS = hopLength.getValue()/sR;

			text = String.format("Window %.3f s Hop: %.3f s", windowLenS, hopLengthS); 
		}
		
		//just incase this is not called from the FX thread
		Platform.runLater(()->{
			infoLabel.setText(text);
			checkSegmentLength();
		});
	}

	/**
	 * Create the warning sign which tells users that the segment length does not match
	 * the model's default segment length. Hovering shows a tooltip and clicking shows a
	 * pop over, both with the warning text and a button to set the correct segment
	 * length and hop.
	 * @return the warning sign.
	 */
	private Label createSegWarningIcon() {
		segWarningGlyph = PamGlyphDude.createPamIcon("mdi2a-alert", SEG_WARN_ORANGE, PamGuiManagerFX.iconSize);

		segWarningIcon = new Label();
		segWarningIcon.setGraphic(segWarningGlyph);
		segWarningIcon.setCursor(Cursor.HAND);
		segWarningIcon.setVisible(false);
		segWarningIcon.managedProperty().bind(segWarningIcon.visibleProperty());

		//colour the icon from the pulse animation.
		segWarningRedness.addListener((obs, oldVal, newVal)->{
			setSegWarningColour(SEG_WARN_ORANGE.interpolate(SEG_WARN_RED, newVal.doubleValue()));
		});

		//pulse between orange and red for 5 seconds (10 x 0.5 s) and then stay orange.
		segWarningPulse = new Timeline(
				new KeyFrame(Duration.ZERO, new KeyValue(segWarningRedness, 0)),
				new KeyFrame(Duration.millis(500), new KeyValue(segWarningRedness, 1)));
		segWarningPulse.setAutoReverse(true);
		segWarningPulse.setCycleCount(10);
		segWarningPulse.setOnFinished((e)->segWarningRedness.set(0));

		//tooltip on hover
		segWarningTooltipLabel = new Label();
		Tooltip tooltip = new Tooltip();
		tooltip.setGraphic(createSegWarningContent(segWarningTooltipLabel));
		tooltip.setShowDelay(Duration.millis(300));
		tooltip.setShowDuration(Duration.INDEFINITE);
		//give the user time to move the mouse into the tooltip to press the button.
		tooltip.setHideDelay(Duration.seconds(2));
		segWarningIcon.setTooltip(tooltip);

		//pop over on click
		segWarningPopLabel = new Label();
		PamBorderPane popHolder = new PamBorderPane(createSegWarningContent(segWarningPopLabel));
		popHolder.setPadding(new Insets(5,5,5,5));
		segWarningPopOver = new PopOver(popHolder);
		segWarningPopOver.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
		segWarningIcon.setOnMouseClicked((e)->{
			tooltip.hide();
			if (segWarningPopOver.isShowing()) segWarningPopOver.hide();
			else segWarningPopOver.show(segWarningIcon);
		});

		return segWarningIcon;
	}

	/**
	 * Set the colour of the warning sign.
	 * @param colour - the colour.
	 */
	private void setSegWarningColour(Color colour) {
		segWarningGlyph.setStyle("-fx-icon-code: \"mdi2a-alert\"; -fx-icon-color: "
				+ PamGlyphDude.colourToHex(colour) + "; -fx-icon-size: " + PamGuiManagerFX.iconSize + ";");
	}

	/**
	 * Create the content for the segment warning tooltip or pop over: the warning text
	 * and a button to set the model's default segment length and hop.
	 * @param textLabel - the label which will hold the warning text.
	 * @return the content node.
	 */
	private Pane createSegWarningContent(Label textLabel) {
		textLabel.setWrapText(true);
		textLabel.setMaxWidth(350);

		PamButton setDefaultSegButton = new PamButton("Set model segment length and hop");
		setDefaultSegButton.setTooltip(new Tooltip("Set the segment length to the model's default for the current "
				+ "source sample rate and the hop to half the segment length"));
		setDefaultSegButton.setOnAction((action)->{
			setModelDefaultSegment();
			if (segWarningPopOver!=null) segWarningPopOver.hide();
		});

		PamVBox content = new PamVBox();
		content.setSpacing(5);
		content.setAlignment(Pos.CENTER_LEFT);
		content.getChildren().addAll(textLabel, setDefaultSegButton);
		return content;
	}

	/**
	 * Get the model's default segment length in milliseconds.
	 * @return the default segment length in millis or null if the current model has no default segment length.
	 */
	private Double getModelDefaultSegLen() {
		if (modelSelectPane==null || modelSelectPane.currentClassifierModel==null) return null;
		Serializable modelSettings = modelSelectPane.currentClassifierModel.getDLModelSettings();
		if (!(modelSettings instanceof StandardModelParams)) return null;
		Double segLen = ((StandardModelParams) modelSettings).defaultSegmentLen;
		if (segLen==null || segLen<=0) return null;
		return segLen;
	}

	/**
	 * Check whether the segment length matches the default segment length of the
	 * current model at the current source sample rate. If not, show a warning with a
	 * button to set the correct segment length and hop. A mismatch usually happens
	 * when the source is changed to one with a different sample rate because the
	 * segment length is stored in samples.
	 */
	private void checkSegmentLength() {
		if (segWarningIcon==null) return;

		boolean show = false;
		Double segLen = getModelDefaultSegLen();
		@SuppressWarnings("rawtypes")
		PamDataBlock source = sourcePane.getSource();
		if (segLen!=null && source!=null && source.getSampleRate()>0 && !segmenterGridPane.isDisabled()) {
			float sR = source.getSampleRate();
			int defaultSamples = (int) (segLen*sR/1000.);
			int currentSamples = windowLength.getValue()==null ? 0 : windowLength.getValue();
			//allow a sample of leeway for rounding errors
			if (defaultSamples>0 && Math.abs(currentSamples-defaultSamples)>1) {
				show = true;
				String warnText = String.format("The segment length (%d samples, %.1f ms) does not match the model's "
						+ "default segment length of %.1f ms which is %d samples at %.0f Hz. "
						+ "This often happens if the source has a different sample rate.",
						currentSamples, 1000.*currentSamples/sR, segLen, defaultSamples, sR);
				segWarningTooltipLabel.setText(warnText);
				segWarningPopLabel.setText(warnText);
			}
		}

		if (segWarningIcon.isVisible()!=show) {
			segWarningIcon.setVisible(show);
			if (show) {
				//draw attention to the new warning
				segWarningPulse.playFromStart();
			}
			else {
				segWarningPulse.stop();
				segWarningRedness.set(0);
				segWarningPopOver.hide();
			}
		}
	}

	/**
	 * Set the segment length to the model's default segment length for the current
	 * source sample rate and set the hop to half the segment length.
	 */
	private void setModelDefaultSegment() {
		Double segLen = getModelDefaultSegLen();
		if (segLen==null || sourcePane.getSource()==null) return;
		setSegmentLength(segLen);
		setHopLength(segLen/2);
	}

	/**
	 * Creates pane allowing the user to change fine scale things such as error limits. 
	 * @return the pop over pane. 
	 */
	public void showAdvPane() {

		popOver = new PopOver(); 
		PamBorderPane holder = new PamBorderPane(dlControl.getDataSelector().getDialogPaneFX().getContentNode()); 
		holder.setPadding(new Insets(5,5,5,5));
		popOver.setContentNode(holder);

		//set the parameters
		popOver.showingProperty().addListener((obs, old, newval)->{
			if (newval) {
				//showing
				dlControl.getDataSelector().getDialogPaneFX().setParams(true);
			}
			else {
				//hiding
				dlControl.getDataSelector().getDialogPaneFX().getParams(true);
			}
		});
		
	
		popOver.show(dataSelectorButton);
	} 

	/**
	 * Enable the controls. 
	 */
	private void enableControls() {
		this.dataSelectorPane.setVisible(true);
		//only show the data selector box for detec tion data. 
		if (sourcePane.getSource() == null) this.dataSelectorPane.setVisible(false);

		else if (sourcePane.getSource().getDataSelectCreator() instanceof NullDataSelectorCreator) {
			//^bit messy but cannot think of a better way to do it. 
			this.dataSelectorPane.setVisible(false);
		}

		dataSelectorButton.setDisable(!dataSelectorCheckBox.isSelected());

		boolean segEnable = true; //should we enable segmenter controls
		if (sourcePane.getSource() instanceof PamRawDataBlock) {
			//if a raw data block then we always enable segmentation no matter what
			segEnable=true;
			segEnableSwitch.setVisible(false);
		}
		else {
			segEnable = segEnableSwitch.isSelected();
			segEnableSwitch.setVisible(true);
		}

		segmenterGridPane.setDisable(!segEnable);	
		infoLabel.setDisable(!segEnable);	

		checkSegmentLength();
	}



	/**
	 * Get the segment length spinner. 
	 * @return the segment spinner. 
	 */
	public PamSpinner<Integer> getSegmentLenSpinner() {
		return windowLength;
	}

	/**
	 * Get the segment hop spinner. 
	 * @return the segment spinner. 
	 */
	public PamSpinner<Integer> getHopLenSpinner() {
		return this.hopLength;
	}
	
	
	/**
	 * Get the spinner which sets the maximum number of segments that can be re-merged.
	 * @return the spinner which sets the maximum number of segments that can be re-merged.
	 */
	public PamSpinner<Integer> getMaxRemergeSpinner() {
		return this.reMergeSeg;
	}




	/**
	 * Set the classifier pane. 
	 */
	protected void setClassifierPane() {
		//set the classifier Pane.class 
		
		//reset all the spinners incase classifier types have set them to disabled
		this.windowLength.setDisable(false);
		this.hopLength.setDisable(false);
		this.reMergeSeg.setDisable(false);
		
		if (modelSelectPane.currentClassifierModel!=null && modelSelectPane.currentClassifierModel.getModelUI()!=null) {

			//now set the classifier pane to the model UI settings pane.
			classifierPane.setCenter(modelSelectPane.currentClassifierModel.getModelUI().getSettingsPane().getContentNode()); 

			//now check the source requirements for the model and set the source list accordingly.
			//For example, if the model requires a specific type of data then set the source list to that type.
			if (modelSelectPane.currentClassifierModel!=null) {
				setSourceList(modelSelectPane.currentClassifierModel); //this should be above setParams so that the classifier sets the correct sample rate
				modelSelectPane.currentClassifierModel.getModelUI().setParams(); 
			}
			else {
				classifierPane.setCenter(null); 
				setDefaultSourceList();
			}

		}
		else {
			//if there is no classifier model then set the classifier pane to null
			classifierPane.setCenter(null); 
			setDefaultSourceList();
		}

		//the model may have a different default segment length.
		checkSegmentLength();
	}


	/**
	 * Exclude all data blocks produced by this deep learning module from the
	 * source list. A module must never use its own output as its input (that
	 * would be recursive), although output from <i>other</i> deep learning
	 * modules is still allowed.
	 */
	@SuppressWarnings("rawtypes")
	private void excludeOwnDataBlocks() {
		if (sourcePane == null || dlControl == null) return;
		for (int i = 0; i < dlControl.getNumPamProcesses(); i++) {
			PamProcess process = dlControl.getPamProcess(i);
			if (process == null) continue;
			for (int j = 0; j < process.getNumOutputDataBlocks(); j++) {
				PamDataBlock block = process.getOutputDataBlock(j);
				if (block != null) {
					sourcePane.excludeDataBlock(block, true);
				}
			}
		}
	}

	/**
	 * Set the allowed sources for this type of model. 
	 * @param currentClassifierModel - the allowed sources. 
	 */
	private void setSourceList(DLClassiferModel currentClassifierModel) {
		
		PamDataBlock currentSource = sourcePane.getSource();
		
		//we don't want to set the source list again and gain if the model has changed. 
		if (currentClassifierModel==null) {
			setDefaultSourceList();
			currentAllowedDataTypes=null;
			sourcePane.setSourceIndex(0); //select the first source by default
			return;
		}
		
		//System.out.println("SET SOURCE LIST: " + currentClassifierModel.getAllowedDataTypes()); 
		
		if (isNewDataType(currentAllowedDataTypes, currentClassifierModel.getAllowedDataTypes()))  {
		//set the source list for a given classifier model. 
		if (currentClassifierModel.getAllowedDataTypes()==null) {
			//default is for models to require raw sound data
			setDefaultSourceList() ;
		}
		else {
			sourcePane.setTitleText("Detection data");
			sourcePane.clearSourceTypeList();
			sourcePane.setSourceIndex(-1);
			for (@SuppressWarnings("rawtypes") Class type: currentClassifierModel.getAllowedDataTypes()) {
				sourcePane.addSourceType(type, false);
			}
		}
			currentAllowedDataTypes = currentClassifierModel.getAllowedDataTypes();
		}
		//System.out.println("SET SOURCE LIST: " + sourcePane.getSourceCount()); 

		//something has gone wrong but at least have sound acquisition for  sample rate.  
		if  (sourcePane.getSourceCount()<=0) {
			setDefaultSourceList();
		}
		
		
		//now find whether the current source is still valid.
		if (currentSource!=null) {
			for (int i=0; i<sourcePane.getSourceCount(); i++) {
				if (sourcePane.getSource(i)==currentSource) {
					sourcePane.setSourceIndex(i);
					return;
				}
			}
		}
		else {
			sourcePane.setSourceIndex(0); //select the first source by default
		}
	}
	
	private boolean isNewDataType(ArrayList<Class> currentAllowedDataTypes1, ArrayList<Class> currentAllowedDataTypes2) {
		
		if (currentAllowedDataTypes1==null && currentAllowedDataTypes2!=null) return true;
		if (currentAllowedDataTypes2==null && currentAllowedDataTypes1!=null) return true;
		if (currentAllowedDataTypes2==null && currentAllowedDataTypes1==null) return false;
		if (!currentAllowedDataTypes2.equals(currentAllowedDataTypes2)) return true;
		
		return false;

	}


	/**
	 * Set the default data sources which are anything that contains raw acoustic data. 
	 */
	private void setDefaultSourceList() {
		sourcePane.setTitleText("Raw Sound Data");
		sourcePane.clearSourceTypeList();
		sourcePane.addSourceType(RawDataUnit.class, false);
		sourcePane.addSourceType(ClickDetection.class, false);
		sourcePane.addSourceType(ClipDataUnit.class, false);
	}


	@Override
	public RawDLParams getParams(RawDLParams currParams) {

		if (currParams==null ) currParams = new RawDLParams(); 

		@SuppressWarnings("rawtypes")
		PamDataBlock rawDataBlock = sourcePane.getSource();
		if (rawDataBlock == null){
			Platform.runLater(()->{
				dlWarningDialog.showWarning("There is no datablock set. The segmenter must have a datablock set."); 
			}); 
			return null;
		}
		
		//check the source pane validator
		if (sourcePane.getChannelValidator().containsErrors()) {
			Platform.runLater(()->{
				String content = PamValidator.list2String(sourcePane.getChannelValidator().getValidationResult().getMessages()); 
				dlWarningDialog.showWarning(content); 
			}); 
			return null;
		}
		
		//check this panes validator
		if (validator.containsErrors()) {
			Platform.runLater(()->{
				String content = PamValidator.list2String(validator.getValidationResult().getMessages()); 
				dlWarningDialog.showWarning(content); 
			}); 
			return null;
		}

		sourcePane.getParams(currParams.groupedSourceParams);

		//		currParams.modelSelection = dlModelBox.getSelectionModel().getSelectedIndex(); 

		if (windowLength.getValue() == 0 || hopLength.getValue()==0){
			Platform.runLater(()->{
				dlWarningDialog.showWarning("Neither the hop nor window length can be zero"); 
			});
			return null;
		}

		currParams.rawSampleSize = windowLength.getValue(); 
		currParams.sampleHop = hopLength.getValue(); 
		currParams.maxMergeHops = reMergeSeg.getValue(); 


		if (modelSelectPane.currentClassifierModel == null) {
			currParams.modelSelection = -1; 
		}
		else {
			currParams.modelSelection = dlControl.getDLModels().indexOf((modelSelectPane.currentClassifierModel)); 
		}


		//		//update any changes
		//		if (this.dlControl.getDLModels().get(dlModelBox.getSelectionModel().getSelectedIndex()).getModelUI()!=null){
		//			this.dlControl.getDLModels().get(dlModelBox.getSelectionModel().getSelectedIndex()).getModelUI().getParams(); 
		//			
		//			//display any warnings from the settings. 
		//			ArrayList<PamWarning> warnings = this.dlControl.getDLModels().get(dlModelBox.getSelectionModel().getSelectedIndex()).checkSettingsOK();
		//			showWarnings(warnings); 
		//			
		//			for (int i=0; i<warnings.size(); i++) {
		//				if (warnings.get(i).getWarnignLevel()>1) {
		//					//Serious error. Do not close dialog. 
		//					return null; 
		//				}
		//			}
		//		}

		currParams.useDataSelector = dataSelectorCheckBox.isSelected(); 

		if (dlControl.getDataSelector()!=null) {
			dlControl.getDataSelector().getDialogPaneFX().getParams(true); 
		}


		//need to make sure we call get params for the current model when the oK button is pressed. 
		if (this.modelSelectPane.currentClassifierModel!=null) {
			if (this.modelSelectPane.currentClassifierModel.getModelUI()!=null) {
				this.modelSelectPane.currentClassifierModel.getModelUI().getParams();
			}
		}

		currParams.modelURI = this.modelSelectPane.currentSelectedFile; 

		currParams.enableSegmentation =	segEnableSwitch.isSelected();

		return currParams;
	}


	public static PamWarning statusToWarnings(DLStatus dlStatus) {
		PamWarning pamWarning = new PamWarning(dlStatus.getName(), dlStatus.getDescription(), dlStatus.isError() ? 2 : 1);
		return pamWarning;
	}


	@Override
	public void setParams(RawDLParams currParams) {
				
		sourcePane.sourceChanged();
		excludeOwnDataBlocks();
		sourcePane.setSourceList();
		sourcePane.setParams(currParams.groupedSourceParams);

		dlControl.createDataSelector(sourcePane.getSource());

		//set the classifier model. 
		if (currParams.modelURI  !=null) {
			modelSelectPane.currentClassifierModel = dlControl.getDLModel(); 
		}

		//		dlModelBox.getSelectionModel().select(currParams.modelSelection);
		windowLength.getValueFactory().setValue(currParams.rawSampleSize);

		hopLength.getValueFactory().setValue(currParams.sampleHop);

		reMergeSeg.getValueFactory().setValue(currParams.maxMergeHops);

		dataSelectorCheckBox.setSelected(currParams.useDataSelector);

		//setClassifierPane(); 
		
		setSegInfoLabel();

		segEnableSwitch.setSelected(currParams.enableSegmentation);

		//		//set up the model and the custom pane if necessary.  
		System.out.println("DLSettingsPane: setParams 5");


		//this.modelSelectPane.updatePathLabel(); 
		//this.setClassifierPane();
		
		enableControls(); 
		
		//important that we do not se the model URI before the model select pane is set up - otherwise the 
		//pane freezes and not a great user experience.  Once this has finished then setClassifierPane will be called.
		this.modelSelectPane.loadNewModel(currParams.modelURI); 

		Platform.runLater(()->{
		//For some reason, in the FX GUI, this causes a root used in multiple scenes exceptions...not sure why. 
			sourcePane.getChannelValidator().validate();
		});

	}


	@Override
	public String getName() {
		return "Raw Deep Learning Parameters";
	}

	@Override
	public helpFX.HelpPoint getHelpPointFX() {
		return new helpFX.HelpPoint("deep_learning_classifier.md");
	}


	@Override
	public Pane getContentNode() {
		return mainPane;
	}


	@Override
	public void paneInitialized() {
		//don't know why this is required but causes a root used in multiple scenes exceptions...really not sure why.
		Platform.runLater(()->{
			sourcePane.getChannelValidator().validate();
		});
	}

	/**
	 * Get the data block currently selected in the pane. 
	 * @return the data block currently selected in the pane. 
	 */
	@SuppressWarnings("rawtypes")
	public PamDataBlock getSelectedParentDataBlock() {
		return sourcePane.getSource();
	}

	/**
	 * Get the DLControl associated with the pane. 
	 * @return a reference to the DLControl. 
	 */
	public DLControl getDLControl() {
		return  dlControl;
	}

	/**
	 * Convenience class to set the segment length in samples from milliseconds
	 * @param defaultSegmentLen - the segment length in milliseconds. 
	 */
	public void setSegmentLength(Double defaultSegmentLen) {
		if (defaultSegmentLen==null) return;


		double sR = getDLControl().getSettingsPane().getSelectedParentDataBlock().getSampleRate(); 

		//System.out.println("Set the segment length: " + defaultSegmentLen + " sR " + sR);

		//automatically set the default segment length. 
		getSegmentLenSpinner().getValueFactory().setValue((int) (sR*defaultSegmentLen/1000.));
	}

	/**
	 * Convenience class to set the hop length in samples from milliseconds
	 * @param defaultSegmentLen - the segment length in milliseconds. 
	 */
	public void setHopLength(Double hopLength) {
		if (hopLength==null) return;

		double sR = getDLControl().getSettingsPane().getSelectedParentDataBlock().getSampleRate(); 
		//automatically set the default segment length. 
		getHopLenSpinner().getValueFactory().setValue((int) (sR*hopLength/1000.));
	}


	public void showWarningDialog(DLStatus status) {
		dlWarningDialog.showWarningDialog(status);
		
	}

	
	class DLPamSpinner<T> extends PamSpinner<T>{
		
		public DLPamSpinner() {
			super();
			// TODO Auto-generated constructor stub
		}

		public DLPamSpinner(double min, double max, double initialValue, double amountToStepBy) {
			super(min, max, initialValue, amountToStepBy);
			// TODO Auto-generated constructor stub
		}

		public DLPamSpinner(double min, double max, double initialValue) {
			super(min, max, initialValue);
			// TODO Auto-generated constructor stub
		}

		public DLPamSpinner(int min, int max, int initialValue, int amountToStepBy) {
			super(min, max, initialValue, amountToStepBy);
			// TODO Auto-generated constructor stub
		}

		public DLPamSpinner(int min, int max, int initialValue) {
			super(min, max, initialValue);
			// TODO Auto-generated constructor stub
		}

		public DLPamSpinner(ObservableList<T> arg0) {
			super(arg0);
			// TODO Auto-generated constructor stub
		}

		public DLPamSpinner(SpinnerValueFactory<T> arg0) {
			super(arg0);
			// TODO Auto-generated constructor stub
		}

		/**
		 * Save the time in seconds as this does not chage with sample rate
		 */
		private double timeSeconds; 
		
		public double getTimeSeconds() {
			return timeSeconds;
		}

		public void setTimeSeconds(double timeSeconds) {
				this.timeSeconds = timeSeconds;
		}
		
	}




}
