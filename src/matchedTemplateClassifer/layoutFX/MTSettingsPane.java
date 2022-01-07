package matchedTemplateClassifer.layoutFX;

import java.util.ArrayList;

import PamController.PamController;
import PamController.SettingsPane;
import PamguardMVC.PamDataBlock;
import PamguardMVC.debug.Debug;
import clickDetector.ClickDetection;
import clickDetector.ClickClassifiers.basicSweep.SweepClassifierSet;
import clickTrainDetector.CTDataUnit;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import matchedTemplateClassifer.MTClassifier;
import matchedTemplateClassifer.MTClassifierControl;
import matchedTemplateClassifer.MatchedTemplateParams;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamTabPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.picker.SymbolPicker;
import pamViewFX.fxNodes.utilityPanes.SourcePaneFX;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

/**
 * Settings Pane for matched template parameters. 
 * 
 * @author Jamie Macaulay
 *
 */
public class MTSettingsPane extends SettingsPane<MatchedTemplateParams> {

	/**
	 * The current settings class
	 */
	private MatchedTemplateParams mtParamsCurrent;

	/**
	 * The source pane. 
	 */
	private SourcePaneFX sourcePane;

	/**
	 * Reference to the control. 
	 */
	private MTClassifierControl mtClassifierControl;

	/**
	 * Selects number of bin around click center to use if useBinsCheckBox is selected 
	 */
	private PamSpinner<Integer> clickLengthSpinner;

	/**
	 * The dB drop in Length.
	 */
	private PamSpinner<Double> lengthdBSpinner;

	/**
	 * Smoothing bins
	 */
	private PamSpinner<Integer> smoothSpinner;

	/**
	 * Label shoping bin length
	 */
	private Label lengthMS;


	/**
	 * True to use only n bins around click center
	 */
	private CheckBox useBinsCheckBox;

	/**
	 * Selects how to classify for grouped data
	 */
	private ComboBox<String> channelClssf;

	/**
	 * 	The colour picker for the symbol fill. 
	 */
	private ColorPicker symbolFillColourPicker;

	/**
	 * The symbol picker
	 */
	private SymbolPicker symbolPicker;

	/**
	 * The main pane./ 
	 */
	private PamBorderPane mainPane = new PamBorderPane();

	/**
	 * Spinner to change the click type flag.
	 */
	private PamSpinner<Integer> typeSpinner;

	/**
	 * Tab pane which holds all the classifiers, each classifier within a tab 
	 */
	private PamTabPane pamTabPane;

	/**
	 * Selects the type of normalisation for the input click.
	 */
	private ComboBox<String> normBox;

	/**
	 * The MT Classifier 
	 * @param mtClassifierControl
	 */
	public MTSettingsPane(MTClassifierControl mtClassifierControl){
		super(null);
		this.mtClassifierControl=mtClassifierControl; 
		this.mtParamsCurrent= new MatchedTemplateParams(); 
		mainPane.setCenter(createPane());
	}

	/**
	 * Create the pane
	 */
	private Pane createPane(){

		PamVBox holder = new PamVBox(); 
		holder.setSpacing(5);
		holder.setPadding(new Insets(5,5,5,5));
		holder.setPrefWidth(700);

		//did not simply use RawDataHolder.class here because it included a bunch of temporary click trains 
		//and other data unit types which are not suitable for the matched click classifier.
		//***Add new data unit types here***//
		sourcePane = new SourcePaneFX("Click Data Source", ClickDetection.class, false, true);
		sourcePane.addSourceType(CTDataUnit.class, false);
		PamGuiManagerFX.titleFont2style(sourcePane.getTitleLabel());

		//sourcePane.setTitleFont(PamGuiManagerFX.titleFontSize2); 

		Label label = new Label("General Classifier Settings"); 
//		label.setFont(PamGuiManagerFX.titleFontSize2);
		PamGuiManagerFX.titleFont2style(label);
		
		PamHBox channelClssfBox= new PamHBox(); 
		channelClssfBox.setSpacing(5);
		channelClssfBox.setAlignment(Pos.CENTER_LEFT);

		channelClssf= new ComboBox<String>(); 
		for (int i=0; i<2; i++) {
			channelClssf.getItems().add(SweepClassifierSet.getChannelOptionsName(i)); 
		}

		channelClssfBox.getChildren().addAll(new Label("Channel Options"), channelClssf);

		//create the type and symbol boxes. 
		Label typeLabel = new Label("Click Type"); 
		typeSpinner = new PamSpinner<Integer>(100,256, 101, 1); 
		typeSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		typeSpinner.setPrefWidth(75);
		typeSpinner.setEditable(true);

		//FIXME- need to add this to register typing...
		typeSpinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				typeSpinner.increment(0); // won't change value, but will commit editor
			}
		});

		PamHBox typePane = new PamHBox();
		typePane.setSpacing(5);
		typePane.getChildren().addAll(typeLabel, typeSpinner); 
		typePane.setAlignment(Pos.CENTER);

		Pane symbolPane= createSymbolPane(); 

		PamHBox symbolHolder = new PamHBox(); 
		symbolHolder.prefWidthProperty().bind(holder.widthProperty()); 
		symbolHolder.getChildren().addAll(typePane,symbolPane);
		symbolHolder.setSpacing(15);

		//Click length pane
		Label lengthLabel = new Label("Click Waveform"); 
		PamGuiManagerFX.titleFont2style(lengthLabel);

//		lengthLabel.setFont(PamGuiManagerFX.titleFontSize2);

		useBinsCheckBox= new CheckBox("Restrict paramter extraction to "); 
		useBinsCheckBox.setOnAction((action)->{
			clickLengthSpinner.setDisable(!useBinsCheckBox.isSelected());
			lengthdBSpinner.setDisable(!useBinsCheckBox.isSelected());
			smoothSpinner.setDisable(!useBinsCheckBox.isSelected());
		}); 

		int clickBin = 0; 
		int n=2; 			
		ObservableList<Integer> lengthList= FXCollections.observableArrayList(); 

		while (clickBin<=102400){
			clickBin=(int) Math.pow(2, n); 
			lengthList.add(clickBin); 
			n++; 
		}

		clickLengthSpinner=new PamSpinner<Integer>(lengthList); 
		clickLengthSpinner.setPrefWidth(100);
		clickLengthSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		clickLengthSpinner.valueProperty().addListener((obsVal, oldVal, newVal)->{
			setLengthText(clickLengthSpinner.getValue()); 
		});

		lengthdBSpinner=new PamSpinner<Double>(0.1,300,6,0.5); 
		lengthdBSpinner.setPrefWidth(100);
		lengthdBSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		lengthdBSpinner.setEditable(true);

		int binVal = 1;
		n=1;
		ObservableList<Integer> binList= FXCollections.observableArrayList(); 
		while (binVal<=1023){
			binVal=binVal+2; 
			binList.add(binVal); 
			n++; 
		}

		smoothSpinner=new PamSpinner<Integer>(binList); 
		smoothSpinner.setPrefWidth(100);
		smoothSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		smoothSpinner.setEditable(false);

		PamHBox clickLengthPane= new PamHBox(); 
		clickLengthPane.setSpacing(5);
		clickLengthPane.getChildren().addAll(useBinsCheckBox, clickLengthSpinner, 
				new Label("samples"), lengthMS= new Label("")); 
		clickLengthPane.setAlignment(Pos.CENTER_LEFT);

		PamHBox clickLengthPane2= new PamHBox(); 
		clickLengthPane2.setSpacing(5);
		clickLengthPane2.getChildren().addAll(new Label("Peak threshold"),  lengthdBSpinner, 
				new Label("dB"), new Label("Smoothing"), smoothSpinner, new Label("bins")); 
		clickLengthPane2.setAlignment(Pos.CENTER_LEFT);

		//click normalisation 
		normBox = new ComboBox<String>();
		normBox.getItems().addAll("peak to peak", "RMS", "none"); 

		PamHBox clickNormPane= new PamHBox(); 
		clickNormPane.setSpacing(5);
		clickNormPane.getChildren().addAll(new Label("Amplitude Normalisation"), normBox);
		clickNormPane.setAlignment(Pos.CENTER_LEFT);


		PamVBox pamVBox = new PamVBox();
		pamVBox.setSpacing(5);
		pamVBox.getChildren().addAll(clickLengthPane, clickLengthPane2); 

		holder.getChildren().addAll(sourcePane,  label, channelClssfBox, symbolHolder,
				lengthLabel, pamVBox , clickNormPane, createClassifierPane());

		//get the classifier pane. 

		return holder;

	}


	/**
	 * Create pane to select a symbol. 
	 * @return a pane for selecting symbols. 
	 */
	private Pane createSymbolPane() {

		PamHBox hBox= new PamHBox(); 
		hBox.setSpacing(5);
		hBox.setAlignment(Pos.CENTER);

		//Default symbol option 
		Label linelength = new Label("Symbol");
		//linelength.setFont(PamGuiManagerFX.titleFontSize2);

		symbolFillColourPicker= new ColorPicker(); 
		symbolFillColourPicker.setStyle("-fx-color-label-visible: false;");
		symbolFillColourPicker.valueProperty().addListener((obsVal, oldVal, newVal)->{
			symbolPicker.setFillColour(newVal);
			symbolPicker.setLineColour(newVal);
		});

		symbolPicker= new SymbolPicker(); 
		symbolFillColourPicker.prefHeightProperty().bind(symbolPicker.heightProperty());

		PamHBox.setHgrow(symbolPicker, Priority.ALWAYS);

		hBox.getChildren().addAll(linelength, symbolPicker, new Label("Fill"), symbolFillColourPicker);

		return hBox;

	}

	/**
	 * Set the 
	 * @param fftLength
	 */
	public void setLengthText(int fftLength) {
		float sr = mtClassifierControl.getPamProcess(0).getSampleRate();
		lengthMS.setText(String.format("(%.2f ms) around click center", fftLength * 1000. / sr));
	}


	/**
	 * Create the classifier pane. 
	 * @return the classifier pane.
	 */ 
	private Node createClassifierPane(){

		//with just one classifier.
		pamTabPane = new PamTabPane(); 
		pamTabPane.setAddTabButton(true);
//		pamTabPane.getAddTabButton().setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.ADD, PamGuiManagerFX.iconSize));
		pamTabPane.getAddTabButton().setGraphic(PamGlyphDude.createPamIcon("mdi2p-plus", PamGuiManagerFX.iconSize));
		pamTabPane.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		pamTabPane.getAddTabButton().setTooltip(new Tooltip(
				"Add a new template. The classifier will check all templates \n"
						+ 	"and classify the click if any template passes it's threshold \n"
						+ 	"value. All template match, reject and threshold values are \n"
						+ 	"stored as annotations in binary files."));

		pamTabPane.getAddTabButton().setOnAction((action)->{
			MTClassifier mtClassifier = new MTClassifier();
			MatchTab tab = new MatchTab(new MTClassifierPane(
					mtClassifierControl, mtClassifier), pamTabPane.getTabs().size());

			pamTabPane.getTabs().add(tab); 
		});

		//list change listener to make sure cannot close the last tab (otherwise pane disappears)
		pamTabPane.getTabs().addListener((ListChangeListener<? super Tab>) c->{
			if (pamTabPane.getTabs().size()==1) {
				pamTabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
			}
			else {
				pamTabPane.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
			}
		});

		// create the first classifier pane. 		
		MatchTab tab = new MatchTab(new MTClassifierPane(mtClassifierControl, 
				mtParamsCurrent.classifiers.get(0)), 0); 
		pamTabPane.getTabs().add(tab); 

		return pamTabPane; 
	}

	/**
	 * Class to hold a classifier pnae inside a tab. 
	 * @author Jamie Macaulay 
	 *
	 */
	private class MatchTab extends Tab {

		private MTClassifierPane mtClassifierPane;

		/**
		 * Constructor the Match tab
		 * @param mtClassifierPane
		 * @param listPos
		 */
		public MatchTab(MTClassifierPane mtClassifierPane, int listPos) {
			this.mtClassifierPane = mtClassifierPane;
			this.setContent(mtClassifierPane.getContentNode());
			this.setText(("Template " + (listPos)));
		}

		/**
		 * Get the matched template classifier pane associated with the tab
		 * @return the match template classifier pane. 
		 */
		private MTClassifierPane mtClassifierPane() {
			return mtClassifierPane;
		}

	}


	@Override
	public MatchedTemplateParams getParams(MatchedTemplateParams p) {

		//System.out.println("GET PARAMS: MatchedTemplateParams ");
		mtParamsCurrent.dataSourceIndex = sourcePane.getSourceIndex();
		mtParamsCurrent.dataSourceName = sourcePane.getSourceLongName();

		mtParamsCurrent.peakSearch = useBinsCheckBox.isSelected();
		mtParamsCurrent.restrictedBins =  clickLengthSpinner.getValue();

		mtParamsCurrent.peakSmoothing = this.smoothSpinner.getValue(); 
		mtParamsCurrent.lengthdB= this.lengthdBSpinner.getValue(); 

		mtParamsCurrent.normalisationType = normBox.getSelectionModel().getSelectedIndex(); 
		if (mtParamsCurrent.normalisationType<0 || 	mtParamsCurrent.normalisationType>2) {
			mtParamsCurrent.normalisationType = 1; 
		}
		
		//get classifier specific settings. In future this could be multiple panes. 		
		mtParamsCurrent.classifiers.clear();
		MTClassifier classifier;
		for (int i=0; i<this.pamTabPane.getTabs().size(); i++) {

			classifier = ((MatchTab) pamTabPane.getTabs().get(i)).mtClassifierPane().getParams(null);
			//must set normalisation from global settings.
			classifier.normalisation = mtParamsCurrent.normalisationType;

			mtParamsCurrent.classifiers.add(classifier);

		}

		//channel classification
		mtParamsCurrent.channelClassification = channelClssf.getSelectionModel().getSelectedIndex();

		//set the type
		mtParamsCurrent.type=this.typeSpinner.getValue().byteValue();

		//set the correct symbol. 
		mtParamsCurrent.pamSymbol.setFillColor(PamUtilsFX.fxToAWTColor(this.symbolFillColourPicker.getValue()));
		mtParamsCurrent.pamSymbol.setLineColor(PamUtilsFX.fxToAWTColor(this.symbolFillColourPicker.getValue()));
		if (symbolPicker.getValue()!=null) {
			mtParamsCurrent.pamSymbol.symbol=this.symbolPicker.getValue().getSymbol(); 
		}



		return mtParamsCurrent;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setParams(MatchedTemplateParams mtParamsCurrent) {

		this.mtParamsCurrent=mtParamsCurrent.clone();

		// and fill in the data source list (may have changed - or might in later versions)
		ArrayList<PamDataBlock> rd = PamController.getInstance().getDataBlocks(ClickDetection.class, true); 
		PamDataBlock  datablock = PamController.getInstance().getDataBlockByLongName(mtParamsCurrent.dataSourceName);

		if (datablock==null && rd.size()>0){
			Debug.out.print("Hello my datablock is null: ");
			datablock=rd.get(0);
		}

		Debug.out.println("Data block to set for Matched Template source: "+ 
		(datablock==null ? null : datablock.getDataName())+ "   " + mtParamsCurrent.dataSourceName); 

		//the click length params. 
		useBinsCheckBox.setSelected(mtParamsCurrent.peakSearch);
		clickLengthSpinner.getValueFactory().setValue(mtParamsCurrent.restrictedBins);
		smoothSpinner.getValueFactory().setValue(mtParamsCurrent.peakSmoothing);
		lengthdBSpinner.getValueFactory().setValue(mtParamsCurrent.lengthdB);

		//FFT settings
		sourcePane.setSourceList();
		sourcePane.setSource(datablock);

		channelClssf.getSelectionModel().select(mtParamsCurrent.channelClassification);
		//		useBinsCheckBox.setSelected(mtParamsCurrent.useRestrictedBins);
		//		clickLengthSpinner.getValueFactory().setValue(mtParamsCurrent.restrictedBins);

		//we don;t want to be remaking panes the whole time so remove and add tabs if necessary. 
		int ntabs=this.pamTabPane.getTabs().size(); 
		if (ntabs>mtParamsCurrent.classifiers.size()) {
			while (ntabs>mtParamsCurrent.classifiers.size()) {
				pamTabPane.getTabs().remove(pamTabPane.getTabs().size()-1); 
				ntabs--;
			}
		}
		else if (ntabs<mtParamsCurrent.classifiers.size()) {
			while (pamTabPane.getTabs().size()<mtParamsCurrent.classifiers.size()) {
				pamTabPane.getTabs().add(new MatchTab(new MTClassifierPane(
						mtClassifierControl, new MTClassifier()), pamTabPane.getTabs().size())); 
				ntabs++;
			}
		}

		if (mtParamsCurrent.classifiers.size()==0) mtParamsCurrent.classifiers.add(new MTClassifier()); 
		else {
			for (int i=0; i<mtParamsCurrent.classifiers.size(); i++) {
				//				MatchTab tab = new MatchTab(new MTClassifierPane(
				//						mtClassifierControl, mtParamsCurrent.classifiers.get(0)), pamTabPane.getTabs().size()); 
				((MatchTab) pamTabPane.getTabs().get(i)).mtClassifierPane().setParams(mtParamsCurrent.classifiers.get(i));
				//				pamTabPane.getTabs().add(tab); 
			}		
		}
		//set the type
		this.typeSpinner.getValueFactory().setValue(new Integer(mtParamsCurrent.type));

		//set the correct symbol. 
		//		System.out.println(mtParamsCurrent.pamSymbol.getFillColor().toString()); 
		symbolFillColourPicker.setValue(PamUtilsFX.awtToFXColor(mtParamsCurrent.pamSymbol.getFillColor()));
		symbolPicker.setValue(mtParamsCurrent.pamSymbol.symbol);
		symbolPicker.setFillColour(symbolFillColourPicker.getValue());
		symbolPicker.setLineColour(symbolFillColourPicker.getValue());

		if (mtParamsCurrent.normalisationType<0 || 	mtParamsCurrent.normalisationType>2) {
			mtParamsCurrent.normalisationType = 1; 
		}
		normBox.getSelectionModel().select(	mtParamsCurrent.normalisationType);

	}



	@Override
	public String getName() {
		return "Matched Template Parameters";
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
