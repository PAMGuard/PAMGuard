package rawDeepLearningClassifier.dlClassification.animalSpot;

import java.io.File;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.ToggleSwitch;

import PamController.SettingsPane;
import PamUtils.PamArrayUtils;
import PamView.dialog.warn.WarnOnce;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import rawDeepLearningClassifier.dlClassification.DLClassiferModel;

/**
 * Settings pane for SoundSpot
 * 
 * @author Jamie Macaulay
 *
 */
public abstract class StandardModelPane extends SettingsPane<StandardModelParams> {

	/**
	 * The main pane for the SoundSpot settings.
	 */
	private PamBorderPane mainPane;


	/**
	 * Currently selected file.
	 */
	private File currentSelectedFile = new File(System.getProperty("user.home"));


	/**
	 * Detection spinner
	 */
	private PamSpinner<Double> detectionSpinner;

	//	/**
	//	 * True to use CUDA
	//	 */
	//	private CheckBox useCuda

	/**
	 * A pop over to show the advanced pane. 
	 */
	protected PopOver popOver;

	/**
	 * Advanced settings pane. 
	 */
	private SettingsPane<StandardModelParams>   advSettingsPane;

	/**
	 * The sound spot classifier. 
	 */
	private DLClassiferModel dlClassifierModel;

	/**
	 * Default segment length. 
	 */
	protected ToggleSwitch usedefaultSeg; 

	/**
	 * Parameters clone. 
	 */
	private StandardModelParams paramsClone;

	/**
	 * Species ID box. 
	 */
	private CheckComboBox<String> speciesIDBox;

	/**
	 * The VBox. 
	 */
	private PamVBox vBoxHolder;

	/**
	 * Use the default segment length
	 */
	protected PamHBox defaultSegBox;

	/**
	 * Model indicator. 
	 */
	private ProgressIndicator modelLoadIndicator;



	public StandardModelPane(DLClassiferModel soundSpotClassifier) {
		super(null);
		this.dlClassifierModel=soundSpotClassifier; 

		mainPane = createPane(); 

		setAdvSettingsPane(new StandardAdvModelPane());
	}

	/**
	 * Create the main pane. 
	 * @return the settings pane.
	 */
	private PamBorderPane createPane() {
		PamBorderPane mainPane = new PamBorderPane(); 


		Label classiferInfoLabel = new Label(dlClassifierModel.getName() + " Classifier"); 
		//PamGuiManagerFX.titleFont2style(classiferInfoLabel);
		Font font= Font.font(null, FontWeight.BOLD, 11);
		classiferInfoLabel.setFont(font);

//		PamButton advButton = new PamButton("", PamGlyphDude.createPamGlyph(MaterialDesignIcon.SETTINGS, PamGuiManagerFX.iconSize)); 
		PamButton advButton = new PamButton("", PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize)); 
		advButton.setMinWidth(30);
		advButton.setOnAction((action)->{
			//pop up window with adv settings.
			showAdvPane(advButton); 
		});
		
		
		
		PamHBox advSettingsBox = new PamHBox(); 
		advSettingsBox.setSpacing(5);
		advSettingsBox.getChildren().addAll(new Label("Advanced"), advButton); 
		advSettingsBox.setAlignment(Pos.CENTER);

		usedefaultSeg = new ToggleSwitch (); 
		usedefaultSeg.selectedProperty().addListener((obsval, oldval, newval)->{
			defaultSegmentLenChanged(); 
			//only set the hop if the user physically changes the toggle switch. This is not included in defaultSegmentLenChanged
			//becuase defaultSegmentLenChanged can be called from elsewhere
			int defaultsamples =  getDefaultSamples();
			dlClassifierModel.getDLControl().getSettingsPane().getHopLenSpinner().getValueFactory().setValue((int) defaultsamples/2);
		});
		usedefaultSeg.setPadding(new Insets(0,0,0,0));
		//there is an issue with the toggle switch which means that it has dead space to the left if
		//there is no label. This is a work around. 
		usedefaultSeg.setMaxWidth(20); 


		defaultSegBox = new PamHBox(); 
		defaultSegBox.setSpacing(5);
		defaultSegBox.getChildren().addAll(usedefaultSeg, new Label("Use default segment length")); 
		defaultSegBox.setAlignment(Pos.CENTER_LEFT);
		//defaultSegBox.setStyle("-fx-background-color: blue;");

		PamBorderPane advSettings = new PamBorderPane(); 
		PamBorderPane.setAlignment(defaultSegBox, Pos.CENTER_LEFT);
		//		advSettings.setLeft(useCuda);
		//		PamBorderPane.setAlignment(useCuda, Pos.CENTER);
		advSettings.setLeft(defaultSegBox);
		advSettings.setRight(advSettingsBox);


		/**Classification thresholds etc to set.**/
		Label classiferInfoLabel2 = new Label("Binary Classification Threshold"); 
		classiferInfoLabel2.setFont(font);

		//PamGuiManagerFX.titleFont2style(classiferInfoLabel2);

		/**
		 * There are tow classifiers the detector and the classifier
		 */
		PamGridPane gridPane = new PamGridPane(); 
		gridPane.setHgap(5);
		gridPane.setVgap(5);

		gridPane.add(new Label("Min. prediction"), 0, 0);
		gridPane.add(detectionSpinner = new PamSpinner<Double>(0.0, 1.0, 0.9, 0.1), 1, 0);
		detectionSpinner.setPrefWidth(80);
		detectionSpinner.setEditable(true);
		detectionSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);

		gridPane.add(new Label(""), 2, 0);
		speciesIDBox = new CheckComboBox<String>(); 
		gridPane.add(speciesIDBox, 3, 0);
		speciesIDBox.setMaxWidth(100);
		speciesIDBox.setPrefWidth(100);
		speciesIDBox.prefHeightProperty().bind(detectionSpinner.heightProperty());

		vBoxHolder = new PamVBox(); 
		vBoxHolder.setSpacing(5);
		vBoxHolder.getChildren().addAll(classiferInfoLabel, advSettings, classiferInfoLabel2, gridPane); 
		
		mainPane.setCenter(vBoxHolder);

		return mainPane; 
	}


	/**
	 * The default segment len changed. 
	 */
	private void defaultSegmentLenChanged() {
		if (paramsClone!=null && paramsClone.defaultSegmentLen != null && usedefaultSeg.isSelected()) {

			//System.out.println("Defualt segment length: " + paramsClone.defaultSegmentLen); 

			//cannot use because, if the parent datablock has changed, samplerate will be out of date. 
			//			int defaultsamples = (int) this.soundSpotClassifier.millis2Samples(paramsClone.defaultSegmentLen); 


//			float sR = dlClassifierModel.getDLControl().getSettingsPane().getSelectedParentDataBlock().getSampleRate(); 

			int defaultsamples =  getDefaultSamples();

			//work out the window length in samples
			dlClassifierModel.getDLControl().getSettingsPane().getSegmentLenSpinner().getValueFactory().setValue(defaultsamples);
//			dlClassifierModel.getDLControl().getSettingsPane().getHopLenSpinner().getValueFactory().setValue((int) defaultsamples/2);

			dlClassifierModel.getDLControl().getSettingsPane().getSegmentLenSpinner().setDisable(true); 
		}
		else {
			dlClassifierModel.getDLControl().getSettingsPane().getSegmentLenSpinner().setDisable(false); 
		}
	}
	
	private int getDefaultSamples() {
		float sR = dlClassifierModel.getDLControl().getSettingsPane().getSelectedParentDataBlock().getSampleRate(); 
		int defaultsamples =  (int) (paramsClone.defaultSegmentLen.doubleValue()*sR/1000.0);
		return defaultsamples;
	}

	/**
	 * Called whenever a new model has been selected
	 * @param file - the file. 
	 */
	public abstract void newModelSelected(File file); 

	/**
	 * Sho0w the advanced settings. 
	 * @param advSettingsButton - the advanced settings. 
	 */
	public void showAdvPane(PamButton advSettingsButton) {
		
		
//		dlClassifierModel.getDLControl().getSettingsPane().setAdvPaneContents(getAdvSettingsPane().getContentNode());
//		dlClassifierModel.getDLControl().getSettingsPane().flipToBack();
		

		if (popOver==null) {
			popOver = new PopOver(); 
			popOver.setContentNode(getAdvSettingsPane().getContentNode());
		}

		popOver.showingProperty().addListener((obs, old, newval)->{
			//TODO?
		});
//		
//		popOver.setOnCloseRequest((event)->{
//			getAdvSettingsPane().getParams(this.get); 
//		});

		popOver.show(advSettingsButton);
	}
	
	




	@Override
	public StandardModelParams getParams(StandardModelParams currParams) {
		
		if (currentSelectedFile==null) {
			//uuurgh need to sort this out with FX stuff
			WarnOnce.showWarningFX(null,  "No Model File",  "There is no model file selected in the path: Please select a compatible model" , AlertType.ERROR);

		}
		else {
			if (currentSelectedFile==null){
				currParams.modelPath = null; 
			}
			else {
				currParams.modelPath =  currentSelectedFile.getPath(); 
			}
		}

		currParams.threshold = detectionSpinner.getValue(); 
		//		currParams.useCUDA = useCuda.isSelected(); 

//		System.out.println("StandardModelParams : this.paramsClone.numClasses " + this.paramsClone.numClasses); 
	
		
		boolean[] speciesClass = new boolean[this.paramsClone.numClasses]; 

		for (int i=0; i< speciesClass.length; i++) {
			//System.out.println("Hello get: " + speciesIDBox.getItemBooleanProperty(i).get()); 
			if ( speciesIDBox.getItemBooleanProperty(i)==null) speciesClass[i]=false; 
			else speciesClass[i] = speciesIDBox.getItemBooleanProperty(i).get(); 
		}

		currParams.binaryClassification = speciesClass;
		
		
		currParams = (StandardModelParams) this.getAdvSettingsPane().getParams(currParams);
				
		//get class names from the paramClone as these may have been set by a loaded model
		//instead of a using changing a control.
		currParams.classNames = paramsClone.classNames; 
		currParams.numClasses = paramsClone.numClasses; 
		
		if ((paramsClone.classNames == null ||  paramsClone.classNames.length<=0) && speciesIDBox.getItems()!=null) {
			
			String[] classNames = new String[speciesIDBox.getItems().size()]; 
			for (int i=0; i<speciesIDBox.getItems().size(); i++) {
				classNames[i] = speciesIDBox.getItems().get(i);
			}
			currParams.classNames = this.dlClassifierModel.getDLControl().getClassNameManager().makeClassNames(classNames);
		}
		
//		System.out.println("GET CLASS NAMES: currParams.classNames: " + currParams.classNames + " " +   
//		(currParams.classNames!=null?  currParams.classNames.length: 0 + " " + currParams.numClasses)); 


		currParams.useDefaultSegLen = usedefaultSeg.isSelected(); 
		
		//System.out.println("Get CLASS NAMES: currParams.classNames: " + currParams.classNames + " Num classes " + currParams.numClasses); 
		return currParams;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setParams(StandardModelParams currParams) {
		try {

		this.paramsClone = currParams.clone(); 
		
		//pathLabel .setText(this.currentSelectedFile.getPath()); 

		detectionSpinner.getValueFactory().setValue(Double.valueOf(currParams.threshold));


		//set the params on the advanced pane. 
//		System.out.println("SET PARAMS ADV PANE: " + (paramsClone.classNames == null ? null : paramsClone.classNames.length));
		this.getAdvSettingsPane().setParams(paramsClone);
		
		if (paramsClone.modelPath!=null) {
			//this might 
			currentSelectedFile = new File(paramsClone.modelPath);
			
			//this might change the paramsClone values if the model contains pamguard compatible metadata
			newModelSelected(currentSelectedFile); 
		}
		
		setClassNames(paramsClone);

		usedefaultSeg.setSelected(paramsClone.useDefaultSegLen); 
		defaultSegmentLenChanged();
		
		}
		
		catch (Exception e) {
			e.printStackTrace();
		}
		//updatePathLabel(); 

	}

	/**
	 * Set the class names in the class name combo box. 
	 * @param currParams - the current params
	 */
	private void setClassNames(StandardModelParams currParams) {
		speciesIDBox.getItems().clear();

//		System.out.println("SET CLASS NAMES: currParams.classNames: " + currParams.classNames + " " +  (currParams.classNames!=null ? currParams.classNames.length: 0) + " " + currParams.numClasses); 

		int classNamesLen = 0; 

		if (currParams.classNames!=null) classNamesLen = currParams.classNames.length; 


		for (int i=0; i<Math.max(classNamesLen, currParams.numClasses); i++) {
			if (currParams.classNames!=null && currParams.classNames.length>i && currParams.classNames[i]!=null) {
				speciesIDBox.getItems().add(currParams.classNames[i].className); 
			}
			else {
				speciesIDBox.getItems().add("Class: " + i); 
			}
		}
		
//		System.out.println("Binary classification: set: " + speciesIDBox.getItems().size());
//		PamArrayUtils.printArray(currParams.binaryClassification);

		for (int i=0; i<speciesIDBox.getItems().size(); i++) {
			if (currParams.binaryClassification!=null && i<currParams.binaryClassification.length) {
				speciesIDBox.getItemBooleanProperty(i).set(currParams.binaryClassification[i]);
			}
			else {
				speciesIDBox.getItemBooleanProperty(i).set(true);
			}
		}
	}

	@Override
	public String getName() {
		return "Sound Spot Settings";
	}

	@Override
	public Node getContentNode() {
		return this.mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub

	}

	public StandardModelParams getParamsClone() {
		return paramsClone;
	}

	/**
	 * Set the params clone. 
	 * @param paramsClone
	 */
	public void setParamsClone(StandardModelParams paramsClone) {
		this.paramsClone = paramsClone;
	}

	/**
	 * Get the currently selected model file. 
	 * @return the currently selected model file. 
	 */
	public File getCurrentSelectedFile() {
		return currentSelectedFile;
	}

	/**
	 * Set  the currently selected model file 
	 * @param currentSelectedFile - the currently selected model file. 
	 */
	public void setCurrentSelectedFile(File currentSelectedFile) {
		this.currentSelectedFile = currentSelectedFile;
	}

	/**
	 * Get the advanced settings pane. 
	 * @return the advanced settings pane. 
	 */
	public SettingsPane getAdvSettingsPane() {
		return advSettingsPane;
	}

	/**
	 * Set the advanced settings pane. The advanced s pane to set. 
	 * @param advSettingsPane
	 */
	public void setAdvSettingsPane(SettingsPane  advSettingsPane) {
		this.advSettingsPane = advSettingsPane;
	}

	public PamVBox getVBoxHolder() {
		return vBoxHolder;
	}

}
