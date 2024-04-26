package rawDeepLearningClassifier.layoutFX;

import java.util.ArrayList;

import org.controlsfx.control.PopOver;

import PamController.PamGUIManager;
import PamController.SettingsPane;
import PamDetection.RawDataUnit;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.NullDataSelectorCreator;
import clickDetector.ClickDetection;
import clipgenerator.ClipDataUnit;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.utilityPanes.GroupedSourcePaneFX;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.DLStatus;
import rawDeepLearningClassifier.RawDLParams;
import warnings.PamWarning;
import whistlesAndMoans.ConnectedRegionDataUnit;

/**
 * The settings pane. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DLSettingsPane  extends SettingsPane<RawDLParams>{


	public static double MAX_WIDTH = 270; 

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
	private PamSpinner<Integer> windowLength;

	/**
	 * The hop length for the segmenter progress. 
	 */
	private PamSpinner<Integer> hopLength;

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



	public DLSettingsPane(DLControl dlControl){
		super(null); 
		this.dlControl=dlControl; 
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

		if (!PamGUIManager.isFX()){
			mainPane.setMinHeight(430);
			mainPane.setMaxWidth(MAX_WIDTH);
			mainPane.setPrefWidth(MAX_WIDTH);
		}
		//this.getAdvPane().setMaxWidth(MAX_WIDTH);


		//mainPane.getStylesheets().add(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getDialogCSS()); 

	}


	/**
	 * Create Pane for changing FFT settings. 
	 * @return pane for changing FFT settings 
	 */
	private Pane createDLPane(){

		PamVBox vBox=new PamVBox();
		vBox.setSpacing(5);

		sourcePane = new GroupedSourcePaneFX("Raw Sound Data", RawDataUnit.class, true, false, true);
		sourcePane.addSourceType(ClickDetection.class, false);
		sourcePane.addSourceType(ClipDataUnit.class, false);
		sourcePane.addSourceType(ConnectedRegionDataUnit.class, false);


		vBox.getChildren().add(sourcePane);
		sourcePane.prefWidthProperty().bind(vBox.widthProperty());
		sourcePane.setMaxWidth(Double.MAX_VALUE);

		sourcePane.getDataBlockBox().setOnAction((action)->{
			//need to create a data selector if one exists. 
			this.dlControl.createDataSelector(getSelectedParentDataBlock()); 
			//enable the controls to show a data selector or not. 
			enableControls(); 
		});

		//create the detection
		vBox.getChildren().add(createDataSelectorPane());


		// the segmentation params
		Label label = new Label("Segmentation"); 
		PamGuiManagerFX.titleFont2style(label); 

		vBox.getChildren().add(label);

		windowLength = new PamSpinner<Integer>(0, Integer.MAX_VALUE, 10,  10000); 
		windowLength.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		windowLength.setEditable(true);
		windowLength.valueProperty().addListener((obsVal, oldVal, newVal)->{
			setSegInfoLabel(); 
		});

		hopLength =    new PamSpinner<Integer>(0, Integer.MAX_VALUE, 10,  10000); 
		hopLength.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		hopLength.setEditable(true);
		hopLength.valueProperty().addListener((obsVal, oldVal, newVal)->{
			setSegInfoLabel(); 
		});

		reMergeSeg =    new PamSpinner<Integer>(0, Integer.MAX_VALUE, 1,  1); 
		reMergeSeg.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		reMergeSeg.setEditable(true);

		//button to set default hop size
		Button defaultButton = new Button();
		//		defaultButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.REFRESH, PamGuiManagerFX.iconSize-3));
		defaultButton.setGraphic(PamGlyphDude.createPamIcon("mdi2r-refresh", PamGuiManagerFX.iconSize-3));
		defaultButton.setTooltip(new Tooltip("Set default hop size"));
		defaultButton.setOnAction((action)->{
			hopLength.getValueFactory().setValue(Math.round(windowLength.getValue()/2));
		});

		PamGridPane segmenterGridPane = new PamGridPane(); 
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

		vBox.getChildren().add(infoLabel = new Label());


		Label label2 = new Label("Deep Learning Model"); 
		label2.setPadding(new Insets(5,0,0,0));
		PamGuiManagerFX.titleFont2style(label2);

		vBox.getChildren().add(label2);

		/**
		 * Pane which allows users to select a model type. 
		 */
		modelSelectPane = new DLModelSelectPane(this); 

		//		//add the possible deep learning models. 
		//		dlModelBox= new ComboBox<String>();
		//		for (int i=0; i<dlControl.getDLModels().size(); i++) {
		//			dlModelBox.getItems().add(dlControl.getDLModels().get(i).getName()); 
		//		}
		//		dlModelBox.prefWidthProperty().bind(vBox.widthProperty());
		//
		//		dlModelBox.setOnAction((action)->{
		//			setClassifierPane(); 
		//			if (mainPane!=null) {
		//				if (mainPane.getScene().getWindow() instanceof Stage) {
		//					Stage stage = (Stage) mainPane.getScene().getWindow();
		//					stage.sizeToScene();
		//				}
		//			}
		//			//this.dlControl.getAnnotationType().getSymbolModifier(symbolChooser).
		//		});
		//
		//		vBox.getChildren().add(dlModelBox);

		classifierPane = new PamBorderPane(); 

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
		infoLabel.setText(text);
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


		popOver.showingProperty().addListener((obs, old, newval)->{
			if (newval) {
				dlControl.getDataSelector().getDialogPaneFX().setParams(true);
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
	 * Set the classifier pane. 
	 */
	protected void setClassifierPane() {
		
		
		//set the classifier Pane.class 
		if (modelSelectPane.currentClassifierModel!=null && modelSelectPane.currentClassifierModel.getModelUI()!=null) {
			
			classifierPane.setCenter(modelSelectPane.currentClassifierModel.getModelUI().getSettingsPane().getContentNode()); 

			if (modelSelectPane.currentClassifierModel!=null) {
				modelSelectPane.currentClassifierModel.getModelUI().setParams(); 
			}
			else {
				classifierPane.setCenter(null); 
			}

		}
		else {
			classifierPane.setCenter(null); 
		}
	}


	@Override
	public RawDLParams getParams(RawDLParams currParams) {

		if (currParams==null ) currParams = new RawDLParams(); 

		@SuppressWarnings("rawtypes")
		PamDataBlock rawDataBlock = sourcePane.getSource();
		if (rawDataBlock == null){
			Platform.runLater(()->{
				PamDialogFX.showWarning("There is no datablock set. The segmenter must have a datablock set."); 
			}); 
			return null;
		}

		sourcePane.getParams(currParams.groupedSourceParams);

		//		currParams.modelSelection = dlModelBox.getSelectionModel().getSelectedIndex(); 

		if (windowLength.getValue() == 0 || hopLength.getValue()==0){
			Platform.runLater(()->{
				PamDialogFX.showWarning("Neither the hop nor window length can be zero"); 
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

		return currParams;
	}


	public static PamWarning statusToWarnings(DLStatus dlStatus) {
		PamWarning pamWarning = new PamWarning(dlStatus.getName(), dlStatus.getDescription(), dlStatus.isError() ? 2 : 1);
		return pamWarning;
	}

	/**
	 * Show a warning dialog for the status
	 * @param the status to show
	 */
	public void showWarning(DLStatus dlWarning) {
		ArrayList<PamWarning> dlWarnings = new ArrayList<PamWarning>();
		dlWarnings.add(statusToWarnings(dlWarning)); 
		showWarning(dlWarnings); 
	}

	/**
	 * Show a warning dialog. 
	 * @param the warning to show.
	 */
	public void showWarning(PamWarning dlWarning) {
		ArrayList<PamWarning> dlWarnings = new ArrayList<PamWarning>();
		dlWarnings.add(dlWarning); 
		showWarning(dlWarnings); 
	}


	/**
	 * Show a warning dialog. 
	 * @param dlWarnings - list of warnings - the most important will be shown. 
	 */
	public void showWarning(ArrayList<PamWarning> dlWarnings) {

		if (dlWarnings==null || dlWarnings.size()<1) return; 

		String warnings ="";


		boolean error = false; 
		for (int i=0; i<dlWarnings.size(); i++) {
			warnings += dlWarnings.get(i).getWarningMessage() + "\n\n";
			if (dlWarnings.get(i).getWarnignLevel()>1) {
				error=true; 
			}
		}

		final String warningsF = warnings; 
		final boolean errorF = error; 
		Platform.runLater(()->{
			WarnOnce.showWarningFX(null,  "Deep Learning Settings Warning",  warningsF , errorF ? AlertType.ERROR : AlertType.WARNING);
			//			WarnOnce.showWarning( "Deep Learning Settings Warning",  warningsF , WarnOnce.WARNING_MESSAGE);
		});

		//user presses OK - these warnings are just a message - they do not prevent running the module.
	}

	@Override
	public void setParams(RawDLParams currParams) {

		sourcePane.setParams(currParams.groupedSourceParams);
		sourcePane.sourceChanged();


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

		setClassifierPane(); 

		enableControls(); 

		setSegInfoLabel();

		//		//set up the model and the custom pane if necessary.  
		this.modelSelectPane.loadNewModel(currParams.modelURI); 
		//this.modelSelectPane.updatePathLabel(); 
		this.setClassifierPane();

		//For some reason, in the FX GUI, this causes a root used in multiple scenes exceptions...not sure why. 
		Platform.runLater(()->{
			sourcePane.getChannelValidator().validate();
		});

	}


	@Override
	public String getName() {
		return "Raw Deep Learning Parameters";
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

		System.out.println("Set the segment length: " + defaultSegmentLen + " sR " + sR);

		//automatically set the default segment length. 
		getDLControl().getSettingsPane().getSegmentLenSpinner().getValueFactory().setValue((int) (sR*defaultSegmentLen/1000.));
	}

	/**
	 * Convenience class to set the hop length in samples from milliseconds
	 * @param defaultSegmentLen - the segment length in milliseconds. 
	 */
	public void setHopLength(Double hopLength) {
		if (hopLength==null) return;

		double sR = getDLControl().getSettingsPane().getSelectedParentDataBlock().getSampleRate(); 
		//automatically set the default segment length. 
		getDLControl().getSettingsPane().getHopLenSpinner().getValueFactory().setValue((int) (sR*hopLength/1000.));
	}



}
