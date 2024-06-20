package clickDetector.layoutFX.clickClassifiers;


import fftFilter.FFTFilterParams;
import fftManager.FFTLengthModeled;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.canvas.Canvas;
import net.synedra.validatorfx.GraphicDecoration;
import net.synedra.validatorfx.ValidationMessage;
import net.synedra.validatorfx.Validator;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamSymbolFX;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.picker.SymbolPicker;
import pamViewFX.fxNodes.utilityPanes.FreqBandPane;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
import pamViewFX.fxNodes.utilityPanes.SimpleFilterPaneFX;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;

import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import PamController.SettingsPane;
import PamUtils.PamUtils;
import PamView.PamSymbol;
import PamView.symbol.SymbolData;
import clickDetector.ClickControl;
import clickDetector.ClickClassifiers.basicSweep.CodeHost;
import clickDetector.ClickClassifiers.basicSweep.SweepClassifier;
import clickDetector.ClickClassifiers.basicSweep.SweepClassifierSet;

/**
 * Pane which contains controls to change a SweepClassifierSet. 
 * 
 * @author Jamie Macaulay
 *
 */
public class SweepClassifierSetPaneFX extends SettingsPane<ClickTypeProperty> {

	/**
	 * Holds general options for the classifier. 
	 */
	private OptionsBox optionBox;

	/**
	 * Settings for pre-filtering of click
	 */
	private FilterBox filterBox;

	/**
	 * Reference to the sweep classifier. 
	 */
	private SweepClassifier sweepClassifier;

	/**
	 * Pane to change energy bands. 
	 */
	private EnergyBandBox energyBox;

	/**
	 * Pane to change click length settings. 
	 */
	private ClickLengthBox clickLength;

	/**
	 * Pane to change frequency peak
	 */
	private FrequencySearchBlock freqBox;

	/**
	 * Pane to change zero crossings 
	 */
	private ZeroCrossingsBox zeroCrossingsBox;

	/**
	 * Cloned reference to ClickTypeProperty.
	 */
	private SweepClassifierSet sweepClassifierSet=null;

	/**
	 * Think current click type properties. 
	 */
	private ClickTypeProperty currentClickProperty;

	/**
	 * Changes the amplitude range
	 */
	private AmplitudeBlock amplitudeBlock;
	
	/**
	 * Changes the bearing parameters for clicks. 
	 */
	private BearingBlock bearingBox;
	
	/**
	 * Allows classification based on correlation scores.
	 */
	private XCorrBlock xCorrBox;

	
	private PamBorderPane mainPane = new PamBorderPane();


	public int classifierItemRow;


	public SweepClassifierSetPaneFX(SweepClassifier sweepClassifier){
		super(null);
		this.sweepClassifier=sweepClassifier; 
		mainPane= new PamBorderPane(); 
		mainPane.setCenter(createSweepPane());
	}


	/**
	 * Create pane with controls to change SweepClassiferSet class
	 * @return pane with controls to create a sweep classifier. 
	 */
	private Node createSweepPane(){

		PamVBox holder=new PamVBox();
		holder.setSpacing(15);
		holder.setPadding(new Insets(10,0,0,0));

		optionBox=new OptionsBox();

		/*********Waveform Tab************/
		Tab waveformTab=new Tab("Waveform"); 
		waveformTab.setGraphic(PamGlyphDude.createPamIcon("mdi2w-waveform",	PamGuiManagerFX.iconSize));
		PamVBox waveformHolder=new PamVBox(5); 
		waveformHolder.setPadding(new Insets(10,0,0,0));

		clickLength=new ClickLengthBox(); 
		filterBox=new FilterBox();
		amplitudeBlock = new AmplitudeBlock();
		zeroCrossingsBox=new ZeroCrossingsBox();
		waveformHolder.getChildren().addAll(clickLength, filterBox, amplitudeBlock, zeroCrossingsBox); 
		waveformTab.setContent(waveformHolder);


		/*********Spectrum Tab****************/
		Tab spectrumTab=new Tab("Spectrum"); 
		spectrumTab.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chart-bell-curve",	PamGuiManagerFX.iconSize));
		PamVBox spectrumHolder=new PamVBox(5); 

		energyBox=new EnergyBandBox();
		freqBox=new FrequencySearchBlock();
		spectrumHolder.getChildren().addAll(energyBox, freqBox); 
		spectrumHolder.setPadding(new Insets(10,0,0,0));
		spectrumTab.setContent(spectrumHolder);
		
		/*********Bearing Tab****************/
		Tab bearingTab=new Tab("Bearing"); 
		bearingTab.setGraphic(PamGlyphDude.createPamIcon("mdi2c-compass-outline",	PamGuiManagerFX.iconSize));
		bearingTab.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
		
		PamVBox bearingHolder=new PamVBox(5); 

		bearingBox=new BearingBlock();
		xCorrBox=new XCorrBlock();

		bearingHolder.getChildren().addAll(bearingBox, xCorrBox); 
		bearingHolder.setPadding(new Insets(10,0,0,0));
		bearingTab.setContent(bearingHolder);


		/**********Main Layout**************/

		TabPane tabPane= new TabPane(); 
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		tabPane.getTabs().addAll(waveformTab, spectrumTab, bearingTab); 

		holder.getChildren().add(optionBox);
		holder.getChildren().add(tabPane);

		return holder; 
	}

	/**
	 * Each section in the classifier can be enabled or disabled. 
	 * This is a generic abstract class for each pane.
	 * @author Jamie Macaulay
	 *
	 */
	private abstract class SweepBox extends PamBorderPane {


		/**
		 * Validator to check settings are correct. 
		 */
		private Validator validator = new Validator();


		/**
		 * Check box to enable pane
		 */
		protected PamToggleSwitch enableBox;

		/**
		 * Border pane to hold content
		 */
		private PamBorderPane borderPane;


		private Font disableFont;

		private Label label;
		

		SweepBox(String borderTitle, Boolean enableButton) {

			//create holder pane
			borderPane=new PamBorderPane();
			this.setCenter(borderPane);
			
			PamHBox hBox = new PamHBox();
			hBox.setSpacing(5);

			if (borderTitle != null) {
				label=new Label(borderTitle); 

				PamGuiManagerFX.titleFont2style(label);

				hBox.getChildren().add(label);
			}

			if (enableButton.booleanValue() == true) {

				enableBox = new PamToggleSwitch("");
				//vBox.setPadding(new Insets(0,20,0,0));
				enableBox.setTooltip(new Tooltip("Enable " + borderTitle + " measurements"));

				enableBox.selectedProperty().addListener((obsVal, oldVal, newVal)->{
					disbleControls(!enableBox.isSelected());
				});

				hBox.getChildren().add(0,enableBox);

				//				setOnAction((action)->{
				//					disbleControls(!enableBox.isSelected());
				//					
				//					/**FIXME- this does not seem to work. If titlepane collapsed auto returns to white**/
				//					if (enableBox.isSelected()) this.setTextFill(Color.WHITE);
				//					else this.setTextFill(Color.GRAY);
				//				});								
				//this.setDisable(!enableBox.isSelected());
			}

			this.setTop(hBox);
			//little bit of space between the top and bottom button.m
			BorderPane.setMargin(hBox, new Insets(0,0,5,0));


			/**Don't like this in old swing version*/ 
			//tP.setCenter( description = new Label("", JLabel.CENTER));
			//this.setTop(tP);
		}


		/**
		 * Get the validator for the sweep box. 
		 * @return
		 */
		public Validator getValidator() {
			return validator;
		}


		/**
		 * Set a description for the sweep box. 
		 * @param desc - a description of the control
		 */
		protected void setDescription(String desc) {
			label.setTooltip(new Tooltip(desc));
		}

		//		private void showTopStrip() {
		//			tP.setVisible(enableBox != null && description.getText().length() > 0);
		//		}

		/**
		 * Set the controls to be anabled or disabled. 
		 * @param enabled - true to enable. 
		 */
		protected void setEnableBox(boolean enabled) {
			if (enableBox == null) {
				return;
			}
			enableBox.setSelected(enabled);
			disbleControls(!enabled);
		}


		protected boolean getEnableBox() {
			if (enableBox == null) {
				return false;
			}
			return enableBox.isSelected();
		}


		/**
		 * Get pane which holds content
		 * @return the border pane. 
		 */
		public PamBorderPane getHolderPane() {
			return borderPane;
		}

		/**
		 * Called whenever a new ClickTypeProperty is selected. Sets pane controls to show ClickTypeProperty params
		 */
		protected abstract void setParams();

		/**
		 * Called whenever a ClickTypeProperty should be generated from controls.
		 * @return a new ClickTypeProperty generated from values set by user in controls. 
		 */
		protected abstract boolean getParams();

		/**
		 * Disable all controls within a box. 
		 * @param disable - true to disable controls.  
		 */
		protected abstract void disbleControls(boolean disable);
	}


	/**
	 * General options for the sweep classifier set. This inlcudes, name, symbol, and some basics on
	 * what part of the click to analyse. 
	 * 
	 * @author Jamie Macaulay
	 *
	 */
	private class OptionsBox extends SweepBox implements FFTLengthModeled, CodeHost {

		/**
		 * The size of the preview symbol to show next to the title
		 */
		private static final double TITLE_SYMBOL_SIZE = 20.;

		/**
		 * Text field to set classifier name.
		 */
		private TextField nameField; 

		/**
		 * Pick a colour. 
		 */
		private ColorPicker symbolColour; 

		/**
		 * Choose a symbol for the classifier 
		 */
		private SymbolPicker symbolPicker;

		/**
		 * Spinner to change the spinner code. 
		 */
		private PamSpinner<Integer> codeSpinner;

		/**
		 * Channels box.
		 */
		private ComboBox<String> channelsBox;

		/**
		 * Click length spinner. 
		 */
		private PamSpinner<Integer> clickLengthSpinner;

		/**
		 * Shows lengths of extraction samples in millis.
		 */
		private Label lengthMS;
		
		/**
		 * The property for the PAMSymbol
		 */
		private  SimpleObjectProperty<Canvas> pamSymbolProperty = new SimpleObjectProperty<Canvas>(); 
		
		private PamSymbolFX currentSymbol = new PamSymbolFX(); 

		private ComboBox<String> lengthTypeBox;

		private CheckBox restrictLength;

		private ColorPicker lineColourPicker;

		private ColorPicker fillColourPicker;

		OptionsBox() {
			super(null, false);
			this.getHolderPane().setCenter(createOptionsPane());

		}

		//create the general options 
		private Node createOptionsPane(){

			//set the canvas for the canvas property. 
			pamSymbolProperty.set(new Canvas(25,25));

			PamGridPane pamGridPane=new PamGridPane();
			pamGridPane.setHgap(5);
			pamGridPane.setVgap(5);
			//			pamGridPane.setPadding(new Insets(10,5,5,35));

			pamGridPane.add(new Label("Name"), 0, 0);

			nameField=new TextField();
			nameField.setPrefColumnCount(10);
			pamGridPane.add(nameField, 1, 0);
			PamGridPane.setColumnSpan(nameField, 8);
			PamGridPane.setHgrow(nameField, Priority.ALWAYS);

			getValidator().createCheck()
			.dependsOn("speciesname", nameField.textProperty())
			.withMethod(c -> {
				String userName = c.get("speciesname");
				if (userName == null || userName.length()<=0) {
					c.error("The classifier must have a name");
				}
			})
			.decorates(nameField)
			.immediate();
			;


			pamGridPane.add(new Label("Code"), 0, 1);

			codeSpinner=new PamSpinner<Integer> (1, 500, 0, 1);
			codeSpinner.setEditable(true);
			//codeSpinner.setPrefWidth(150);
			codeSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			pamGridPane.add(codeSpinner, 1, 1);

			//			pamGridPane.add(new Label("Symbol"), 0,1);

			//create colour picker to allow users to change symbol colour. 
			symbolPicker=new SymbolPicker(); 
			symbolPicker.setOnAction((action)->{
				drawSymbolProperty();
			});
			pamGridPane.add(symbolPicker, 3,1);

			pamGridPane.add(new Label("Symbol"), 2,1);

			lineColourPicker = new ColorPicker(); 
			lineColourPicker.setStyle("-fx-color-label-visible: false ;");
			lineColourPicker.setOnAction((action)->{
				symbolPicker.setLineColour(lineColourPicker.getValue());
				drawSymbolProperty();
			});
			pamGridPane.add(lineColourPicker, 4, 1);


			fillColourPicker = new ColorPicker(); 
			fillColourPicker.setStyle("-fx-color-label-visible: false ;");
			fillColourPicker.setOnAction((action)->{
				symbolPicker.setFillColour(fillColourPicker.getValue());
				drawSymbolProperty();
			});
			pamGridPane.add(fillColourPicker, 5, 1);


			//			//create a button to allow users to change symbol shape. 
			//			symbolColour=new ColorPicker(); 
			//			pamGridPane.add(symbolColour, 3, 1);
			//			symbolColour.setOnAction((action)->{
			//				symbolPicker.setFillColour(symbolColour.getValue()); 
			//			});

			//channel options
			pamGridPane.add(new Label("Channels"), 0,2);

			channelsBox = new ComboBox<String>();
			for (int i = 0; i < 3; i++) {
				channelsBox.getItems().add(SweepClassifierSet.getChannelOptionsName(i));
			}
			pamGridPane.add(channelsBox, 1,2);

			PamGridPane.setColumnSpan(channelsBox,8 );

			//restrict parameter to click centre
			PamHBox clickCenterBox=new PamHBox(); 
			clickCenterBox.setSpacing(5); 

			clickCenterBox.getChildren().add(restrictLength = new CheckBox("Trim click")); 

			clickLengthSpinner=new PamSpinner<Integer>(4,102400,128,32); 
			clickLengthSpinner.setEditable(true);
			//clickLengthSpinner.setPrefWidth(150);
			clickLengthSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			clickLengthSpinner.valueProperty().addListener((obsVal, oldVal, newVal)->{
				setLengthLabel();
			});

			clickCenterBox.getChildren().add(clickLengthSpinner);
			Label samplesLabel =new Label("samples");
			clickCenterBox.getChildren().add(samplesLabel); 
			clickCenterBox.getChildren().add(lengthMS=new Label("()")); 
			clickCenterBox.getChildren().add(lengthTypeBox = new ComboBox<String>()); 
			lengthTypeBox.getItems().add("around click center");
			lengthTypeBox.getItems().add("from start of click");
			clickCenterBox.setAlignment(Pos.CENTER_LEFT);

			restrictLength.setOnAction((action)->{
				lengthTypeBox.setDisable(!restrictLength.isSelected());
				clickLengthSpinner.setDisable(!restrictLength.isSelected());
				samplesLabel.setDisable(!restrictLength.isSelected());
				lengthMS.setDisable(!restrictLength.isSelected());
			});


			//			//column constraints
			//			ColumnConstraints col1 = new ColumnConstraints();
			//			col1.setPercentWidth(15);
			//			ColumnConstraints col2 = new ColumnConstraints();
			//			col2.setPercentWidth(35);
			//			ColumnConstraints col3 = new ColumnConstraints();
			//			col3.setPercentWidth(15);
			//			ColumnConstraints col4 = new ColumnConstraints();
			//			col4.setPercentWidth(35);
			//			ColumnConstraints col4 = new ColumnConstraints();
			//			col4.setPercentWidth(35);
			//			
			//			pamGridPane.getColumnConstraints().addAll(col1, col2, col3,col4);

			PamVBox  holder = new PamVBox(); 
			holder.setSpacing(5);
			holder.getChildren().addAll(pamGridPane,clickCenterBox); 

			return holder; 
		}

		private void drawSymbolProperty() {
			
			this.pamSymbolProperty.get().getGraphicsContext2D().clearRect(0, 0,  TITLE_SYMBOL_SIZE, TITLE_SYMBOL_SIZE);
			
			currentSymbol.setSymbol(this.symbolPicker.getValue() == null ? null : this.symbolPicker.getValue().getSymbol());
			currentSymbol.setLineColor(this.lineColourPicker.getValue());
			currentSymbol.setFillColor(this.fillColourPicker.getValue());
			
			if (currentSymbol.getSymbol()!=null) {
				currentSymbol.draw(this.pamSymbolProperty.get().getGraphicsContext2D(), new Point2D(TITLE_SYMBOL_SIZE/2,TITLE_SYMBOL_SIZE/2), TITLE_SYMBOL_SIZE, TITLE_SYMBOL_SIZE); 
			}
			
			this.pamSymbolProperty.setValue(this.pamSymbolProperty.get());
		}

		@Override
		public int getCode() {
			return codeSpinner.getValue();
		}

		@Override
		public void setCode(int code) {
			codeSpinner.getValueFactory().setValue(code);
		}

		@Override
		public int getFFTLength() {
			return clickLengthSpinner.getValue(); 
		}

		@Override
		public void setFFTLength(int fftLength) {
			clickLengthSpinner.getValueFactory().setValue( fftLength);
			float sr = sweepClassifier.getClickDetector().getSampleRate();
			lengthMS.setText(String.format("(%.2f ms)", fftLength * 1000 / sr));
		}

		/**
		 * Set the length in seconds. 
		 */
		private void setLengthLabel() {
			float sr = sweepClassifier.getClickDetector().getSampleRate();
			lengthMS.setText(String.format("(%.2f ms)", clickLengthSpinner.getValue() * 1000 / sr));
		}

		@Override
		protected void setParams() {
			
			//must have a symbol selected by default or a null symbol will be returned.
			symbolPicker.getSelectionModel().select(0);

			//set basic data
			if (sweepClassifierSet == null) {
				//symbolViewer.setSymbol(null);
				nameField.setText("");
				setCode(sweepClassifier.getNextFreeCode(0));
			}
			else {
				symbolPicker.setSymbol(sweepClassifierSet.symbol == null? null : sweepClassifierSet.symbol.getSymbol());
				nameField.setText(sweepClassifierSet.getName());
				setCode(sweepClassifierSet.getSpeciesCode());
			}
			
			

			if (sweepClassifierSet == null) {
				return;
			}
			
			if (sweepClassifierSet.symbol!=null) {
				//set the colours. 
				lineColourPicker.setValue(PamUtilsFX.awtToFXColor(sweepClassifierSet.symbol.getSymbolData().getLineColor()));
				fillColourPicker.setValue(PamUtilsFX.awtToFXColor(sweepClassifierSet.symbol.getSymbolData().getFillColor()));
				symbolPicker.setValue(sweepClassifierSet.symbol.getSymbolData().symbol);
				symbolPicker.setLineColour(lineColourPicker.getValue());
				symbolPicker.setFillColour(fillColourPicker.getValue());
			}

			//set the click length data
			lengthTypeBox.getSelectionModel().select(sweepClassifierSet.restrictedBinstype);

			channelsBox.getSelectionModel().select(sweepClassifierSet.channelChoices);
			restrictLength.setSelected(sweepClassifierSet.restrictLength);
			setFFTLength(sweepClassifierSet.restrictedBins);


			//			nameField.setText(sweepClassifierSet.getName());
			//			codeSpinner.getValueFactory().setValue(sweepClassifierSet.getSpeciesCode());
			//			channelsBox.getSelectionModel().select(sweepClassifierSet.channelChoices);
			//			
			//			//length stuff
			//			clickLengthSpinner.getValueFactory().setValue(sweepClassifierSet.restrictedBins); 

			drawSymbolProperty();

		}

		@Override
		protected boolean getParams() {
			sweepClassifierSet.setName(nameField.getText());

			if (this.symbolPicker.getValue()==null) {
				return showWarning("You must pick a symbol");
			}

			//get the symbol data.
			SymbolData symbolData = new SymbolData(this.symbolPicker.getValue().getSymbol(), 10,10,true, 
					PamUtilsFX.fxToAWTColor(this.fillColourPicker.getValue()), PamUtilsFX.fxToAWTColor(this.lineColourPicker.getValue())); 
			sweepClassifierSet.symbol= new PamSymbol(symbolData);
			if (sweepClassifierSet.getName()==null || sweepClassifierSet.getName().length() <= 0) {
				return showWarning("You must enter a name for this type of click");
			}
			sweepClassifierSet.setSpeciesCode(getCode());

			System.out.println(" sweepClassifierSet: "  +  sweepClassifierSet + "  classifierItemRow: "
					+ classifierItemRow + "sweepClassifierSet.getSpeciesCode(): " + sweepClassifierSet.getSpeciesCode());
			if (sweepClassifier.codeDuplicated(sweepClassifierSet, classifierItemRow) ||
					sweepClassifierSet.getSpeciesCode() <= 0){
				return showWarning("You must enter a unique positive integer species code");
			}
			if (sweepClassifierSet.symbol == null) {
				return showWarning("You must select a symbol");
			}
			sweepClassifierSet.channelChoices = channelsBox.getSelectionModel().getSelectedIndex();
			sweepClassifierSet.restrictLength = restrictLength.isSelected();

			sweepClassifierSet.restrictedBinstype = lengthTypeBox.getSelectionModel().getSelectedIndex();

			try {
				sweepClassifierSet.restrictedBins = clickLengthSpinner.getValue(); 
			}
			catch (NumberFormatException e) {
				return showWarning("Invalid Restricted length value");
			}
			return true;
			//			sweepClassifierSet.setName(nameField.getText());
			//			sweepClassifierSet.setSpeciesCode(codeSpinner.getValue());
			//			sweepClassifierSet.channelChoices=channelsBox.getSelectionModel().getSelectedIndex(); 
			//			sweepClassifierSet.restrictedBins=clickLengthSpinner.getValue(); 
		}

		@Override
		protected void disbleControls(boolean enable) {
			// TODO Auto-generated method stub
		}

		public TextField getNameLabel() {
			return this.nameField;
		}

		public ObservableValue<? extends Node> getNameGraphicsProperty() {
			return pamSymbolProperty;
		}
	

	}


	/**
	 * Filter options for the sweep classifier set
	 * @author Jamie Macaulay
	 */
	private class FilterBox extends SweepBox implements FFTLengthModeled {

		private SimpleFilterPaneFX simpleFilterPane;

		FilterBox() {
			super("Filter Options", true);
			this.getHolderPane().setCenter(createOptionsPane());

		}

		//create the general options 
		private Node createOptionsPane(){

//			PamGridPane pamGridPane=new PamGridPane();
//			pamGridPane.setHgap(5);
//			pamGridPane.setVgap(5);

			simpleFilterPane=new SimpleFilterPaneFX(); 

			return simpleFilterPane.getContentNode(); 

		}

		@Override
		public int getFFTLength() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void setFFTLength(int fftLength) {
			// TODO Auto-generated method stub

		}

		@Override
		protected void setParams() {
			//set sample rate. 
			setEnableBox(sweepClassifierSet.enableFFTFilter);
			simpleFilterPane.setSampleRate(sweepClassifier.getClickDetector().getSampleRate());
			if (sweepClassifierSet.fftFilterParams!=null) simpleFilterPane.setParams(sweepClassifierSet.fftFilterParams);


		}

		@Override
		protected boolean getParams() {
			FFTFilterParams filterParams=simpleFilterPane.getParams(sweepClassifierSet.fftFilterParams);
			if (filterParams!=null){
				sweepClassifierSet.fftFilterParams=filterParams;
			}
			return false;
		}

		@Override
		protected void disbleControls(boolean disable) {
			simpleFilterPane.setDisableFilterPane(disable);
		}

	}

	/**
	 * Click length box. 
	 * @author Jamie Macaulay
	 *
	 */
	private class ClickLengthBox extends SweepBox {

		/**
		 * Spinner for peak smoothing
		 */
		PamSpinner<Integer> smoothing; 

		/**
		 * Spinner for  minimum click length in milliseconds
		 */
		PamSpinner<Double> minLengthms; 

		/**
		 * Spinner for maximum click length in milliseconds
		 */
		PamSpinner<Double> maxLengthms; 

		/**
		 * Spinner for smoothing threshold. 
		 */
		PamSpinner<Double> threshold;

		private PamVBox vboxholder;


		ClickLengthBox() {
			super("Click Length", true);
			this.getHolderPane().setCenter(createClickLengthPane());
		}

		private Node createClickLengthPane(){

			//			PamGridPane gridPane=new PamGridPane();
			//			gridPane.setHgap(5);
			//			gridPane.setVgap(5);

			//threshold 
			//			gridPane.add(new Label("Smoothing"),0,0); 
			smoothing=new PamSpinner<Integer>(3,101,5,2); 
			smoothing.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			//smoothing.setPrefWidth(100);		
			//			gridPane.add(smoothing,1,0); 
			//			gridPane.add(new Label("bins (must be odd)"),2,0); 

			//spinner
			//			gridPane.add(new Label("Threshold"),3,0); 
			threshold=new PamSpinner<Double>(1., 300., 6.,1.);
			threshold.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			//threshold.setPrefWidth(100);
			//			gridPane.add(threshold,4,0); 
			//			gridPane.add(new Label("dB"),5,0); 

			PamHBox clickLengthHolder1=new PamHBox(); 
			clickLengthHolder1.setSpacing(5);
			clickLengthHolder1.getChildren().addAll(new Label("Smoothing"), 
					smoothing, new Label("bins"), new Label("Threshold"), threshold, new Label("dB")); 
			clickLengthHolder1.setAlignment(Pos.CENTER_LEFT);


			//spinner
			//			gridPane.add(new Label("Click Length"),0,1); 

			minLengthms=new PamSpinner<Double>(0.00, 100.00, 0.03,0.01);
			minLengthms.setEditable(true);
			minLengthms.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			//minLengthms.setPrefWidth(130);

			maxLengthms=new PamSpinner<Double>(0.00, 100.00, 0.22,0.01);
			maxLengthms.setEditable(true);
			maxLengthms.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			//maxLengthms.setPrefWidth(130);


			PamHBox clickLengthHolder2=new PamHBox(); 
			clickLengthHolder2.setSpacing(5);
			clickLengthHolder2.getChildren().addAll(new Label("Click Length"), minLengthms, new Label("to"), maxLengthms, new Label("ms")); 
			clickLengthHolder2.setAlignment(Pos.CENTER_LEFT);

			//			gridPane.add(clickLengthHolder2,1,1); 
			//			PamGridPane.setRowSpan(clickLengthHolder2, 5);
			//			PamGridPane.setFillWidth(clickLengthHolder2, false);


			vboxholder=new PamVBox();
			vboxholder.setSpacing(5);
			vboxholder.getChildren().addAll(clickLengthHolder1, clickLengthHolder2);

			return vboxholder; 

		}

		@Override
		protected void setParams() {
			if (sweepClassifierSet == null) {
				return;
			}
			setEnableBox(sweepClassifierSet.enableLength);
			smoothing.getValueFactory().setValue(sweepClassifierSet.lengthSmoothing);
			threshold.getValueFactory().setValue(sweepClassifierSet.lengthdB);
			minLengthms.getValueFactory().setValue( sweepClassifierSet.minLength);
			maxLengthms.getValueFactory().setValue(sweepClassifierSet.maxLength);

		}

		@Override
		protected boolean getParams() {
			sweepClassifierSet.enableLength = getEnableBox();
			try {
				sweepClassifierSet.lengthSmoothing = smoothing.getValue();
				sweepClassifierSet.lengthdB = threshold.getValue();
			}
			catch (NumberFormatException e) {
				return showWarning("Click Length Parameters", 
						"The length smoothing or threshold parameter is invalid");
			}
			if (sweepClassifierSet.lengthSmoothing%2 == 0 || sweepClassifierSet.lengthSmoothing <= 0) {
				return showWarning("The number of length smoothing bins must be odd and positive");
			}
			if (sweepClassifierSet.lengthdB == 0) {
				return showWarning("The length threshold cannot be zero dB");
			}
			if (!sweepClassifierSet.enableLength) {
				return true;
			}
			try {
				sweepClassifierSet.minLength = minLengthms.getValue();
				sweepClassifierSet.maxLength = maxLengthms.getValue();
			}
			catch (NumberFormatException e) {
				return showWarning("Click Length Parameters", 
						"One or more click length parameters is invalid");
			}
			if (sweepClassifierSet.maxLength <= sweepClassifierSet.minLength) {
				return showWarning("Click Length Parameters", 
						"The maximum click length must be greater than the minimum");
			}
			return true;
		}


		@Override
		protected void disbleControls(boolean disable) {
			vboxholder.setDisable(disable);
//			smoothing.setDisable(disable);
//			minLengthms.setDisable(disable);
//			maxLengthms.setDisable(disable);
//			threshold.setDisable(disable);
		}

	}



	/**
	 * Filter options for the sweep classifier set
	 * @author Jamie Macaulay
	 */
	private class EnergyBandBox extends SweepBox {

		/**
		 * Frequency pane for test band
		 */
		private FreqBandPane testBandFreqPane;

		/**
		 * Frequency pane for the first control band. 
		 */
		private ArrayList<FreqBandPane> contralBandFreqPanes;


		/**
		 * Spinner for the first control band
		 */
		private ArrayList<PamSpinner<Double>> thresholdSpinners;

		private PamGridPane gridPaneHolder; 


		EnergyBandBox() {
			super("Energy Band", true);
			this.getHolderPane().setCenter(createOptionsPane());

		}

		//create the general options 
		private Node createOptionsPane(){

			gridPaneHolder=new PamGridPane();
			gridPaneHolder.setHgap(5);
			gridPaneHolder.setVgap(5);

			Label freqLabel=new Label("Frequency (Hz)");
			gridPaneHolder.add(freqLabel, 0, 0);
			gridPaneHolder.add(new Label("Threshold (dB)"), 2, 0);
			PamGridPane.setHalignment(freqLabel, HPos.CENTER);
			PamGridPane.setColumnSpan(gridPaneHolder, 2);

			//test band
			gridPaneHolder.add(new Label("Test Band"), 0, 1);

			testBandFreqPane=new FreqBandPane(Orientation.HORIZONTAL);
			testBandFreqPane.setBandText("");
			
			
			addValidatorFreqCheck(getValidator(), testBandFreqPane, "test band ",  ("test_band"));

			
			
			gridPaneHolder.add(testBandFreqPane, 1, 1);


			contralBandFreqPanes = new ArrayList<FreqBandPane>(); 
			thresholdSpinners = new ArrayList<PamSpinner<Double>>();  

			for (int i=0; i<SweepClassifierSet.nControlBands ; i++) {
				//control band 1
				gridPaneHolder.add(new Label("Control Band"), 0, i+2);
				contralBandFreqPanes.add(new FreqBandPane(Orientation.HORIZONTAL));		
				contralBandFreqPanes.get(i).setBandText("");
				gridPaneHolder.add(contralBandFreqPanes.get(i), 1, i+2);
				thresholdSpinners.add(new PamSpinner<Double>(0,100.,6.,0));
				thresholdSpinners.get(i).setEditable(true);
				gridPaneHolder.add(thresholdSpinners.get(i), 2, i+2);
				thresholdSpinners.get(i).getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
				thresholdSpinners.get(i).setMaxWidth(100);			

				//add validation. 
				final int ii = i; 
				addValidatorFreqCheck(getValidator(), contralBandFreqPanes.get(i), ("controlband "+ii),  ("controlband_"+ii));
				
				
				contralBandFreqPanes.get(i).getLowPassFreq().valueProperty().addListener((obsVal, oldVal, newVal)->{
					getValidator().validate();
				});
				
				contralBandFreqPanes.get(i).getLowPassFreq().valueProperty().addListener((obsVal, oldVal, newVal)->{
					getValidator().validate();
				});



			}

			return gridPaneHolder; 

		}
		
		



		@Override
		protected void setParams() {
			setEnableBox(sweepClassifierSet.enableEnergyBands);
			if (sweepClassifierSet == null) {
				return;
			}

			sweepClassifierSet.checkEnergyParamsAllocation();
			
			testBandFreqPane.getLowPassFreq().getValueFactory().setValue(sweepClassifierSet.testEnergyBand[0]);
			testBandFreqPane.getHighPassFreq().getValueFactory().setValue(sweepClassifierSet.testEnergyBand[1]);
			
			for (int j = 0; j < SweepClassifierSet.nControlBands; j++) {

				contralBandFreqPanes.get(j).getLowPassFreq().getValueFactory().setValue(sweepClassifierSet.controlEnergyBand[j][0]);
				contralBandFreqPanes.get(j).getHighPassFreq().getValueFactory().setValue(sweepClassifierSet.controlEnergyBand[j][1]);

				thresholdSpinners.get(j).getValueFactory().setValue(sweepClassifierSet.energyThresholds[j]);

			}

			//			for (int i = 0; i < 2; i++) {
			//				testEnergy[i].setText(String.format("%3.1f", sweepClassifierSet.testEnergyBand[i]));
			//				for (int j = 0; j < SweepClassifierSet.nControlBands; j++) {
			//					controlEnergy[j][i].setText(String.format("%3.1f", sweepClassifierSet.controlEnergyBand[j][i]));
			//				}
			//			}
			//			for (int j = 0; j < SweepClassifierSet.nControlBands; j++) {
			//				thresholds[j].setText(String.format("%3.1f", sweepClassifierSet.energyThresholds[j]));
			//			}
		}

		@Override
		protected boolean getParams() {
			if ((sweepClassifierSet.enableEnergyBands = getEnableBox()) == false) {
				return true;
			}
			if (sweepClassifierSet == null) {
				return false;
			}
			sweepClassifierSet.checkEnergyParamsAllocation();

			if (this.getValidator().containsErrors()) {
				showValidatorWarning(getValidator()); 
				return false;
			}

			try { 
				
				sweepClassifierSet.testEnergyBand[0] = testBandFreqPane.getLowPassFreq().getValue().doubleValue(); 
				sweepClassifierSet.testEnergyBand[1] = testBandFreqPane.getHighPassFreq().getValue().doubleValue(); 

				for (int j = 0; j < SweepClassifierSet.nControlBands; j++) {
						sweepClassifierSet.controlEnergyBand[j][0] = contralBandFreqPanes.get(j).getLowPassFreq().getValue();
						sweepClassifierSet.controlEnergyBand[j][1] = contralBandFreqPanes.get(j).getHighPassFreq().getValue();
						
						sweepClassifierSet.energyThresholds[j] = thresholdSpinners.get(j).getValue();
				}
	
			}
			catch (NumberFormatException e) {
				return showWarning("Invalid energy band parameter");
			}
			if (sweepClassifierSet.testEnergyBand[1] <= sweepClassifierSet.testEnergyBand[0]) {
				return showWarning("Energh Bands", "The high test band frequency must be greater than the low test band frequency");
			}
			for (int j = 0; j < SweepClassifierSet.nControlBands; j++) {
				if (sweepClassifierSet.controlEnergyBand[j][1] <= sweepClassifierSet.controlEnergyBand[j][0]) {
					return showWarning("Energh Bands", "The high control band frequency must be greater than the low control band frequency");
				}
			}
			return true;
		}

		@Override
		protected void disbleControls(boolean disable) {
			this.gridPaneHolder.setDisable(disable);
			
//			testBandFreqPane.setDisableFreqPane(disable);
//			for (int j = 0; j < SweepClassifierSet.nControlBands; j++) {
//				contralBandFreqPanes.get(j).setDisableFreqPane(disable);
//				thresholdSpinners.get(j).setDisable(disable);
//			}
		}

	}


	/**
	 * Block to specify peak frequency to search for. 
	 * @author Jamie Macaulay
	 *
	 */
	private class FrequencySearchBlock extends SweepBox {

		private PamToggleSwitch peakFreqCheckBox;
		private PamToggleSwitch peakWidthCheckBox;
		private PamToggleSwitch meanFreqCheckBox;


		/**
		 * Pane to set frequency band range		 */
		private FreqBandPane peakFreqPane;
		private PamSpinner<Integer> smoothing;
		private FreqBandPane peakWidthPane;
		private PamSpinner<Double> threshold;
		private FreqBandPane searchRange;
		private FreqBandPane meanFreq;
		
		//the main holder pane. 
		private PamGridPane gridPaneHolder;

		FrequencySearchBlock() {
			super("Peak and Mean Frequency", true);
			this.getHolderPane().setCenter(createFreqSearchPane());
		}

		private Node createFreqSearchPane(){
			
			int gridy=0; 

			gridPaneHolder=new PamGridPane();
			gridPaneHolder.setHgap(5);
			gridPaneHolder.setVgap(5);


			//search and integration range
			gridPaneHolder.add(new Label("Search Range"),1,gridy);

			searchRange=new FreqBandPane(Orientation.HORIZONTAL);		
			searchRange.setBandText("");
			gridPaneHolder.add(searchRange,2,gridy);
			GridPane.setColumnSpan(searchRange, GridPane.REMAINING);

			
			gridy++;

			gridPaneHolder.add(new Label("Smooth"), 1,gridy);

			smoothing=new PamSpinner<Integer>(3,101,5,2); 
			smoothing.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			smoothing.setMaxWidth(100);
			gridPaneHolder.add(smoothing, 2,gridy);

			GridPane.setMargin(smoothing, new Insets(0,0,0,5)); //bit of a hack to make sure everything lines up nicely with the frequency pane. 
			
			//GridPane.setHgrow(smoothing, Priority.NEVER);

			gridPaneHolder.add(new Label("bins"), 3,gridy);


			gridy++;
			//peak frequency
			peakFreqCheckBox=new PamToggleSwitch("");
			peakFreqCheckBox.selectedProperty().addListener((obsVal, oldVal, newVal)->{
				peakFreqPane.setDisableFreqPane(!peakFreqCheckBox.isSelected());
			});

			gridPaneHolder.add(peakFreqCheckBox,0,gridy);

			gridPaneHolder.add(new Label("Peak Frequency"),1,gridy);

			peakFreqPane=new FreqBandPane(Orientation.HORIZONTAL);		
			//peakFreqPane.setHgap(0);
			peakFreqPane.setBandText("");
			gridPaneHolder.add(peakFreqPane,2,gridy);
			GridPane.setColumnSpan(peakFreqPane, GridPane.REMAINING);

			
			addValidatorFreqCheck(getValidator(), peakFreqPane, "peak freq. ",  "peak_freq");
			
			gridy++;

			//peak width
			peakWidthCheckBox=new PamToggleSwitch("");
			peakWidthCheckBox.selectedProperty().addListener((obsVal, oldVal, newVal)->{
				//peakWidthPane.setDisable(!peakWidthCheckBox.isSelected());
				peakWidthPane.setDisableFreqPane(!peakWidthCheckBox.isSelected());
				threshold.setDisable(!peakWidthCheckBox.isSelected());
			});
			

			gridPaneHolder.add(peakWidthCheckBox,0,gridy);
			gridPaneHolder.add(new Label("Peak Width"),1,gridy);

			peakWidthPane=new FreqBandPane(Orientation.HORIZONTAL);		
			peakWidthPane.setBandText("");
			gridPaneHolder.add(peakWidthPane,2,gridy);
			GridPane.setColumnSpan(peakWidthPane, GridPane.REMAINING);
			
			addValidatorFreqCheck(getValidator(), peakWidthPane, "peak width ",  "peak_width");

			gridy++;

			gridPaneHolder.add(new Label(""), 1,gridy);

			threshold=new PamSpinner<Double>(1., 300., 6.,1.);
			threshold.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			threshold.setPrefWidth(100);
			GridPane.setMargin(threshold, new Insets(0,0,0,5)); //bit of a hack to make sure everything lines up nicely with the frequency pane. 

			gridPaneHolder.add(threshold,2,gridy); 

			gridPaneHolder.add(new Label("dB"), 3,gridy);


			gridy++;
			
			//mean frequency
			meanFreqCheckBox=new PamToggleSwitch("");
			meanFreqCheckBox.selectedProperty().addListener((obsVal, oldVal, newVal)->{
				meanFreq.setDisableFreqPane(!meanFreqCheckBox.isSelected());
			});

			gridPaneHolder.add(meanFreqCheckBox,0,gridy);

			gridPaneHolder.add(new Label("Mean Frequency"),1,gridy);

			meanFreq=new FreqBandPane(Orientation.HORIZONTAL);		
			meanFreq.setBandText("");
			gridPaneHolder.add(meanFreq,2,gridy);
			GridPane.setColumnSpan(meanFreq, GridPane.REMAINING);

			addValidatorFreqCheck(getValidator(), meanFreq, "mean freq. ",  "mean_freq");


			return gridPaneHolder;

		}

		@Override
		protected void setParams() {
			setEnableBox(sweepClassifierSet.enableEnergyBands);
		}

		@Override
		protected boolean getParams() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected void disbleControls(boolean disable) {
			this.gridPaneHolder.setDisable(disable);
			
//			peakFreqCheckBox.setDisable(enable);
//			peakWidthCheckBox.setDisable(enable);
//			meanFreqCheckBox.setDisable(enable);
//
//
//			/**
//			 * Pane to set frequency band range		 */
//			peakFreqPane.setDisable(enable);
//			smoothing.setDisable(enable);
//			peakWidthPane.setDisable(enable);
//			threshold.setDisable(enable);
//			searchRange.setDisable(enable);
//			meanFreq.setDisable(enable);
		}

	}



	/**
	 * Block to specify peak frequency to search for. 
	 * @author Jamie Macaulay
	 *
	 */
	private class ZeroCrossingsBox extends SweepBox {

		/**
		 * Spinner for the minimum number of zero crossings
		 */
		private PamSpinner<Integer> zeroCrossingsMin;

		/**
		 * Spinner for the maximum number of zero crossings 
		 */
		private PamSpinner<Integer> zeroCrossingsMax;

		/**
		 * Spinner for the minimum zero crossing frequency sweep in kHz/ms
		 */
		private PamSpinner<Double> freqZeroMin;

		/**
		 * Spinner for the minimum zero crossing frequency sweep in kHz/ms
		 */
		private PamSpinner<Double> freqZeroMax;

		/**
		 * The main holder
		 */
		private PamGridPane gridPaneHolder;

		ZeroCrossingsBox() {
			super("Zero Crossings", true);
			this.getHolderPane().setCenter(createZeroCrossPane());
		}

		private Node createZeroCrossPane(){

			gridPaneHolder=new PamGridPane();
			gridPaneHolder.setHgap(5);
			gridPaneHolder.setVgap(5);

			//Number of zeros crossings
			Label zeroLabel;
			gridPaneHolder.add(zeroLabel = new Label("Number of zero crossings"),0,0);

			zeroCrossingsMin=new PamSpinner<Integer>(0,999999,0,1); 
			zeroCrossingsMin.setEditable(true);
			zeroCrossingsMin.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			zeroCrossingsMin.setPrefWidth(100);
			gridPaneHolder.add(zeroCrossingsMin, 1,0);

			gridPaneHolder.add(new Label("to"),2,0);

			zeroCrossingsMax=new PamSpinner<Integer>(0,999999,0,1); 
			zeroCrossingsMax.setEditable(true);
			zeroCrossingsMax.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			zeroCrossingsMax.setPrefWidth(100);
			gridPaneHolder.add(zeroCrossingsMax, 3,0);

			//this.zeroCrossingsMax=zeroCorssingsMax;



			getValidator().createCheck()
			.dependsOn("minzero", zeroCrossingsMin.valueProperty())
			.withMethod(c -> {
				Integer minZero = c.get("minzero"); 
				if (minZero>=zeroCrossingsMax.getValue() && isPaneShowing()) {
					c.error("Minimum zero corssing must be less than the maximum zero crossing");
				}
			})
			.decorates(zeroCrossingsMin)
			.immediate();

			zeroCrossingsMin.valueProperty().addListener((obsVal, oldval, newVal)->{
				getValidator().validate(); //need to make sure the min undecorates if this changes
			});


			getValidator().createCheck()
			.dependsOn("maxzero", zeroCrossingsMax.valueProperty())
			.withMethod(c -> {
				try {
					Integer maxZero = c.get("maxzero"); 
					if (maxZero<=zeroCrossingsMin.getValue() && isPaneShowing()) { //For some reason the validator does not work with flip panes. 
						c.error("Maxmimum zero crossing must be greater than the minimum zero crossing");
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			})
			.decorates(zeroCrossingsMax)
			.immediate();

			GraphicDecoration.updateDecorations(zeroCrossingsMax);
			//			 
			//			 zeroCorssingsMax.valueProperty().addListener((obsVal, oldval, newVal)->{
			//				 getValidator().validate(); //need to make sure the min undecorates if this changes
			//			 });


			//zero crossing freuquency sweep 

			gridPaneHolder.add(new Label("Zero crossing frequency sweep"),0,1);

			freqZeroMin=new PamSpinner<Double>(0.,999999.,0.,1.); 
			freqZeroMin.setEditable(true);
			freqZeroMin.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			freqZeroMin.setPrefWidth(100);
			gridPaneHolder.add(freqZeroMin, 1,1);



			zeroCrossingsMax.valueProperty().addListener((obsVal, oldval, newVal)->{
				getValidator().validate(); //need to make sure the min undecorates if this changes
			});


			gridPaneHolder.add(new Label("to"),2,1);

			freqZeroMax=new PamSpinner<Double>(0.,999999.,0,1); 
			freqZeroMax.setEditable(true);
			freqZeroMax.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			freqZeroMax.setPrefWidth(100);
			gridPaneHolder.add(freqZeroMax, 3,1);

			getValidator().createCheck()
			.dependsOn("minzerofreq", freqZeroMin.valueProperty())
			.withMethod(c -> {
				Double minFreq = c.get("minzerofreq"); 
				if (minFreq>=freqZeroMax.getValue() && isPaneShowing()) {
					c.error("Minimum zero crossingsfrequency be less than the minimum.");
				}
			})
			.decorates(freqZeroMin)
			.immediate();


			freqZeroMin.valueProperty().addListener((obsVal, oldval, newVal)->{
				getValidator().validate(); //need to make sure the min undecorates if this changes
			});


			getValidator().createCheck()
			.dependsOn("maxzerofreq", freqZeroMax.valueProperty())
			.withMethod(c -> {
				Double maxZero = c.get("maxzerofreq"); 
				if (maxZero<=freqZeroMin.getValue() && isPaneShowing()) {
					c.error("Maxmimum zero crossingsfrequency must be greater than the minimum.");
				}
			})
			.decorates(freqZeroMax)
			.immediate();


			freqZeroMax.valueProperty().addListener((obsVal, oldval, newVal)->{
				getValidator().validate(); //need to make sure the min undecorates if this changes
			});


			gridPaneHolder.add(new Label("KHz/ms"),4,1);


			return gridPaneHolder;

		}

		@Override
		protected void setParams() {
			setEnableBox(sweepClassifierSet.enableZeroCrossings);

			sweepClassifierSet.checkZCAllocation();
			//			setEnableBox(sweepClassifierSet.enableZeroCrossings);
			//enableSweep.setSelected(sweepClassifierSet.enableSweep);

			zeroCrossingsMin.getValueFactory().setValue(sweepClassifierSet.nCrossings[0]);
			zeroCrossingsMax.getValueFactory().setValue(sweepClassifierSet.nCrossings[1]);

			freqZeroMin.getValueFactory().setValue(sweepClassifierSet.zcSweep[0]);
			freqZeroMax.getValueFactory().setValue(sweepClassifierSet.zcSweep[1]);

			getValidator().validate(); //need to make sure the min undecorates if this changes

			//			for (int i = 0; i < 2; i++) {
			//				nZC[i].setText(String.format("%d", sweepClassifierSet.nCrossings[i]));
			//				zcRate[i].setText(String.format("%3.1f", sweepClassifierSet.zcSweep[i]));
			//			}

		}

		@Override
		protected boolean getParams() {

			if (this.getValidator().containsErrors()) {
				showValidatorWarning(getValidator()); 
				return false;
			}

			if ((sweepClassifierSet.enableZeroCrossings = enableBox.isSelected()) == true) {
				sweepClassifierSet.checkZCAllocation();

				sweepClassifierSet.nCrossings = new int[2]; 
				sweepClassifierSet.nCrossings[0] =  zeroCrossingsMin.getValue(); 
				sweepClassifierSet.nCrossings[1] =  zeroCrossingsMax.getValue(); 

				//return showWarning("Zero Crossings", "Invalid number of zero crossings");

			}
			if ((sweepClassifierSet.enableSweep = enableBox.isSelected()) == true) {

				sweepClassifierSet.zcSweep = new double[2]; 
				sweepClassifierSet.zcSweep[0] = freqZeroMin.getValue(); 
				sweepClassifierSet.zcSweep[1] = freqZeroMax.getValue(); 

				//return showWarning("Zero Crossings", "Invalid zero crossing sweep parameter");
			}

			return true;
		}

		@Override
		protected void disbleControls(boolean disable) {
			gridPaneHolder.setDisable(disable);
//			zeroCrossingsMin.setDisable(disable);
//			zeroCrossingsMax.setDisable(disable);
//			freqZeroMin.setDisable(disable);
//			freqZeroMax.setDisable(disable);
		}

	}

	/**
	 * Controls to change the minimum and maximum amplitude range for the classifier. 
	 * @author Jamie Macaulay 
	 *
	 */
	private class AmplitudeBlock extends SweepBox {


		private TextField[] ampRange = new TextField[2];
		
		/**
		 * The main holder
		 */
		private PamGridPane gridPane;

		public AmplitudeBlock() {
			super("Amplitude Range", true);
			setDescription("Set a minimum and maximum click amplitude for this type");

			gridPane= new PamGridPane(); 
			gridPane.setHgap(5);
			gridPane.setVgap(5);

			gridPane.add(new Label("Minimum"), 0, 0);
			gridPane.add(ampRange[0] = new TextField(), 1, 0);
			gridPane.add(new Label("Maximum"), 2, 0);
			gridPane.add(ampRange[1] = new TextField(), 3, 0);
			gridPane.add(new Label("dB"), 4,0); 

			this.getHolderPane().setCenter(gridPane);
		}

		@Override
		protected void setParams() {
			this.setEnableBox(sweepClassifierSet.testAmplitude);
			double[] range = sweepClassifierSet.amplitudeRange;
			if (range == null || range.length != 2) {
				range = new double[2];
				range[0] = 0;
				range[1] = 200;
			}
			for (int i = 0; i < 2; i++) {
				ampRange[i].setText(String.format("%3.1f", range[i]));
			}

			getValidator().validate(); //need to make sure the min undecorates if this change
		}

		@Override
		protected boolean getParams() {

			if (this.getValidator().containsErrors()) {
				showValidatorWarning(getValidator()); 
				return false;
			}

			sweepClassifierSet.testAmplitude = getEnableBox();
			if (getEnableBox()) {
				sweepClassifierSet.amplitudeRange = new double[2];
				for (int i = 0; i < 2; i++) {
					try {
						sweepClassifierSet.amplitudeRange[i] = Double.valueOf(ampRange[i].getText());
					}
					catch (NumberFormatException e) {
						return PamDialogFX.showWarning("Invalid amplitude range");
					}
				}
			}
			return true;
		}

		@Override
		protected void disbleControls(boolean disable) {
			gridPane.setDisable(disable);

//			for (int i = 0; i < 2; i++) {
//				ampRange[i].setDisable(!getEnableBox());
//			}

		}
	}


	/**
	 * 
	 * Parameters for testing bearing values. 
	 * 
	 * @author Jamie Macaulay
	 *
	 */
	private class BearingBlock extends SweepBox {

		/**
		 * The minimum correlation value. 
		 */
		private PamSpinner<Double> minBearing;
		
		/**
		 * The maximum bearing field
		 */
		private PamSpinner<Double> maxBearing;
		/**
		 * True if using multi-channel data
		 */
		boolean multiChan = false;

		/**
		 * Combo box to select whetrher bearings should be kept or excluded within limits. 
		 */
		private ComboBox<String> bearingsExcludeBox;

		private PamHBox bearingHolder;

		
		BearingBlock() {
			super("Bearings", true);
			
			JPanel p = new JPanel();
			
			minBearing = new PamSpinner<Double>(-180., 180., 0., 1.); 
			minBearing.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			minBearing.setMaxWidth(110);
			minBearing.setEditable(true);
			maxBearing = new PamSpinner<Double>(-180., 180., 0., 1.); 
			maxBearing.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			maxBearing.setMaxWidth(110);
			maxBearing.setEditable(true);

			bearingsExcludeBox = new ComboBox<String>(); 
			bearingsExcludeBox.getItems().add("Include only");
			bearingsExcludeBox.getItems().add("Exclude");
			
			bearingHolder = new PamHBox(); 
			bearingHolder.setSpacing(5);
			bearingHolder.setAlignment(Pos.CENTER_LEFT);
		
			p.setLayout(new GridBagLayout());
			
			bearingHolder.getChildren().add(bearingsExcludeBox);
			bearingHolder.getChildren().add(new Label("bearings between "));
			bearingHolder.getChildren().add(minBearing);
			bearingHolder.getChildren().add(new Label("\u00B0 and "));
			bearingHolder.getChildren().add(maxBearing);
			bearingHolder.getChildren().add(new Label("\u00B0"));
			
			
			getValidator() .createCheck().dependsOn("maxbearing_", maxBearing.valueProperty())
			.withMethod(c -> {
				Double maxBearingVal = c.get("maxbearing_"); 
				if (maxBearingVal<=minBearing.getValue() && isPaneShowing()) {
					c.error("Maximum bearing must be greater than the minimum bearing");
				}
				if (maxBearingVal<-180 || maxBearingVal>180) {
					c.error("Bearing values must be between -180\u00B0 and 180\u00B0");
				}
			})
			.decorates(maxBearing)
			.immediate();

			getValidator() .createCheck().dependsOn("minbearing_", minBearing.valueProperty())
			.withMethod(c -> {
				Double minBearingVal = c.get("minbearing_"); 
				if (minBearingVal>maxBearing.getValue() && isPaneShowing()) {
					c.error("Minimum bearing must be less than the maximum bearing");
				}
				if (minBearingVal<-180 || minBearingVal>180) {
					c.error("Bearing values must be between -180\u00B0 and 180\u00B0");
				}
			})
			.decorates(minBearing)
			.immediate();
			
			
			maxBearing.valueProperty().addListener((obsVal, oldVal, newVal)->{
				getValidator().validate();
			});
			
			minBearing.valueProperty().addListener((obsVal, oldVal, newVal)->{
				getValidator().validate();
			});
			
			this.multiChan = checkMultiChan();
			
			this.getHolderPane().setCenter(bearingHolder);
			
			//setParams();

		}

		@Override
		protected void setParams() {			
			
			this.setEnableBox(sweepClassifierSet.enableBearingLims);

			sweepClassifierSet.checkBearingAllocation();
//			setEnableBox(sweepClassifierSet.enableZeroCrossings);
			//enableBearings.setSelected(sweepClassifierSet.enableBearingLims);
			
			if (sweepClassifierSet.excludeBearingLims) bearingsExcludeBox.getSelectionModel().select(1);
			else  bearingsExcludeBox.getSelectionModel().select(0);
			
			this.minBearing.getValueFactory().setValue(Math.toDegrees(sweepClassifierSet.bearingLims[0]));
			this.maxBearing.getValueFactory().setValue(Math.toDegrees(sweepClassifierSet.bearingLims[1]));
			
			this.multiChan = checkMultiChan();
		}
		
	
		@Override
		protected boolean getParams() {

			sweepClassifierSet.excludeBearingLims	 = bearingsExcludeBox.getSelectionModel().getSelectedIndex()==1 ? true : false; 

			sweepClassifierSet.bearingLims[0] = Math.toRadians(minBearing.getValue());

			sweepClassifierSet.bearingLims[1] = Math.toRadians(maxBearing.getValue());
		
			return true;
		}


		@Override
		protected void disbleControls(boolean disable) {
			checkMultiChan();
			
			bearingHolder.setDisable(!multiChan); 
//			bearingsExcludeBox.setDisable(!multiChan);
//			minBearing.setDisable(!multiChan);
//			maxBearing.setDisable(!multiChan);
			if (!multiChan) return;
			bearingHolder.setDisable(disable); 

//			bearingsExcludeBox.setDisable(disable);
//			minBearing.setDisable(disable);
//			maxBearing.setDisable(disable);
			
		}
	}

	
	/**
	 * Check whether the click classifier is set up for multi-channel and set 
	 * the dialog components uprightly
	 */
	private boolean checkMultiChan() {
		boolean multiChan = false; 
		//do we have multi-channel clicks?
		if  (this.sweepClassifier.getClickDetector().getClickControl()!=null) {
			int[] chanGroups = getClickControl().getClickParameters().getGroupedSourceParameters().getChannelGroups();
			multiChan = false;
			
			if (chanGroups==null) return multiChan; 
			
			for (int i=0; i<chanGroups.length; i++) {
				int chans = getClickControl().getClickParameters().getGroupedSourceParameters().getGroupChannels(i);
//				Debug.out.println("Check multi-channel: " + chanGroups[i] + "  num: " + PamUtils.getNumChannels(chans));
				if (PamUtils.getNumChannels(chans)>1) {
					multiChan = true;
					break; 
				}
			}
		}
		else multiChan = true;
//		Debug.out.println("Check multi-channel: " + multiChan);
		
		return multiChan; 
	}
	
	/**
	 * 
	 * Parameters for testing the cross correlation value. 
	 * 
	 * @author Jamie Macaulay
	 *
	 */
	private class XCorrBlock extends SweepBox {

		/**
		 * The minimum correlation value. 
		 */
		private PamSpinner<Double> minCorrelation;
		
		private PamSpinner<Double> minPeakTorugh;

		private PamToggleSwitch minXCorrEnable;

		private PamToggleSwitch minPeakTroughEnable;

		private PamSpinner<Double> minPeakFactor;
		
		/**
		 * True if using multi-channel data
		 */
		boolean multiChan = false;

		private GridPane gridPane;


		XCorrBlock() {
			super("Cross Correlation", true);
			
			minXCorrEnable = new PamToggleSwitch(""); 
			minXCorrEnable.selectedProperty().addListener((obsVal, oldVal, newVal)->{
				disbleControls(!newVal);
			});

			minCorrelation = new PamSpinner<Double>(-Double.MAX_VALUE, Double.MAX_VALUE, 0, 0.1);
			minCorrelation.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);

			minPeakTroughEnable = new PamToggleSwitch(""); 
			minPeakTroughEnable.selectedProperty().addListener((obsVal, oldVal, newVal)->{
				disbleControls(!newVal);
			});
			
			minPeakFactor = new PamSpinner<Double>(-Double.MAX_VALUE, Double.MAX_VALUE, 1., 0.1);
			minPeakFactor.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);

			int x = 0;
			int y = 0;
			
			gridPane = new GridPane();
			gridPane.setHgap(5);
			gridPane.setVgap(5);

			gridPane.add(minXCorrEnable, x, y);
			gridPane.add(new Label("Min. xcorr value "), ++x, y);
			gridPane.add(minCorrelation, ++x, y);
			
			y++;
			x=0; 
			gridPane.add(minPeakTroughEnable, x, y);
			gridPane.add(new Label("Max. xcorr value greater than "), ++x, y);
			gridPane.add(minPeakFactor,++x, y);
			gridPane.add(new Label("* absolute min. value"),++x, y);
			
			this.getHolderPane().setCenter(gridPane);

			this.multiChan = checkMultiChan();
		}

		@Override
		protected void setParams() {
			
			this.setEnableBox(sweepClassifierSet.enableMinXCrossCorr);

			sweepClassifierSet.checkXCCorrAllocation();
//			setEnableBox(sweepClassifierSet.enableZeroCrossings);
			minXCorrEnable.setSelected(sweepClassifierSet.enableMinXCrossCorr);
			minPeakTroughEnable.setSelected(sweepClassifierSet.enablePeakXCorr);
			
			this.minCorrelation.getValueFactory().setValue(sweepClassifierSet.minCorr);
			this.minPeakFactor.getValueFactory().setValue(sweepClassifierSet.corrFactor);
			
			this.multiChan = checkMultiChan();		
		}
	
		@Override
		protected boolean getParams() {
			sweepClassifierSet.enableMinXCrossCorr	 = minXCorrEnable.isSelected();
			sweepClassifierSet.enablePeakXCorr	 = minPeakTroughEnable.isSelected();

			if (minXCorrEnable.isSelected()) {
					sweepClassifierSet.minCorr = minCorrelation.getValue();
			}
			
			if (minPeakTroughEnable.isSelected()) {
					sweepClassifierSet.corrFactor = minPeakFactor.getValue();
			}
			return true;
		}

		@Override
		protected void disbleControls(boolean disable) {
				this.multiChan = checkMultiChan();
			
				minXCorrEnable.setDisable(!multiChan);
				minCorrelation.setDisable(!multiChan);
				minPeakTroughEnable.setDisable(!multiChan);
				minPeakFactor.setDisable(!multiChan);
			
				if (!multiChan) return;
				
				gridPane.setDisable(disable);
				
				minCorrelation.setDisable(!minXCorrEnable.isSelected());
				minPeakFactor.setDisable(!minPeakTroughEnable.isSelected());
		}


	}
	


	/**~*main set and get params functions***/	

	@Override
	public ClickTypeProperty getParams(ClickTypeProperty clickTypeProperty) {

		//set the classifier
		optionBox.getParams();
		clickLength.getParams();
		filterBox.getParams();
		energyBox.getParams();
		freqBox.getParams();
		zeroCrossingsBox.getParams();
		amplitudeBlock.getParams();
		bearingBox.getParams();
		xCorrBox.getParams();


		currentClickProperty.setClickType(sweepClassifierSet);

		return currentClickProperty;
	}

	@Override
	public void setParams(ClickTypeProperty clickTypeProperty) {

		this.sweepClassifierSet=(SweepClassifierSet) clickTypeProperty.getClickType(); 
		this.currentClickProperty=clickTypeProperty;

		optionBox.setParams();
		clickLength.setParams();
		filterBox.setParams();
		energyBox.setParams();
		freqBox.setParams();
		zeroCrossingsBox.setParams();
		amplitudeBlock.setParams();
		bearingBox.setParams();
		xCorrBox.setParams();

	}

	@Override
	public String getName() {
		return "Sweep Classifier";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub

	}


	public StringProperty getNameTextProperty() {
		return optionBox.getNameLabel().textProperty();
	}
	
	
	public ObservableValue<? extends Node> getNameGraphicProperty() {		// TODO Auto-generated method stub
		return optionBox.getNameGraphicsProperty(); 

	}
	
	
	
	
	/**
	 * Add a validator check to the frequency band pane to prevent minimum and maximum frequencies being above or below themselves and 
	 * the maximum frequency being above Nyquist. 
	 * @param freqBandPane - the frequency band pane. 
	 * @param name - the name for user interface
	 * @param ID - the ID of the check. 
	 */
	private void addValidatorFreqCheck(Validator validator, FreqBandPane freqBandPane, String name, String ID) {
		
		validator.createCheck()
		.dependsOn("minfreq_"+ID, freqBandPane.getHighPassFreq().valueProperty())
		.withMethod(c -> {
			Double minfreq = c.get("minfreq_"+ID); 
			if (minfreq>=freqBandPane.getLowPassFreq().getValue() && isPaneShowing()) {
				c.error("Minimum frequency in " + name + "must be lower than the maximum frequency ");
			}
		})
		.decorates(freqBandPane.getHighPassFreq())
		.immediate();

		validator.createCheck()
		.dependsOn("maxfreq_"+ID, freqBandPane.getLowPassFreq().valueProperty())
		.withMethod(c -> {
			Double maxFreq = c.get("maxfreq_"+ID); 
			if (maxFreq<freqBandPane.getHighPassFreq().getValue() && isPaneShowing()) {
				c.error("Maximum control band frequency in " + name + "must be lower than the minimum frequency ");
			}
		})
		.decorates(freqBandPane.getLowPassFreq())
		.immediate();
		
		
		freqBandPane.getLowPassFreq().valueProperty().addListener((obsVal, oldVal, newVal)->{
			validator.validate();
		});
		
		freqBandPane.getHighPassFreq().valueProperty().addListener((obsVal, oldVal, newVal)->{
			validator.validate();
		});
		
	
		validator.createCheck()
		.dependsOn("maxfreq_samplerate_"+ID, freqBandPane.getLowPassFreq().valueProperty())
		.withMethod(c -> {
			Double maxFreq = c.get("maxfreq_samplerate_"+ID); 
			if (maxFreq>sweepClassifier.getClickDetector().getSampleRate()/2 && isPaneShowing()) {
				c.error("The maximum frequency for  " + name +  " cannot be above the Nyquist frequency of " + sweepClassifier.getClickDetector().getSampleRate()/2 + "Hz"); 
			}
		})
		.decorates(freqBandPane.getLowPassFreq())
		.immediate();
		
	}

	private boolean showWarning(String string) {
		PamDialogFX.showWarning(string);
		return false;
	}


	/**
	 * Show a warning. 
	 * @param string - the 
	 * @param string2
	 * @return false usually
	 */
	private boolean showWarning(String string, String string2) {
		//PamController.getInstance();
		PamDialogFX.showWarning(null, string, string2);
		return false;
	}
	
	/**
	 * Get the click control associated with the classifier. 
	 * @return the click control. 
	 */
	public ClickControl getClickControl() {
		return this.sweepClassifier.getClickDetector().getClickControl();
	}


	/**
	 * Show a warning from the validator. 
	 * @param validator - the validator to show warning for. 
	 */
	private void showValidatorWarning(Validator validator) {
		List<ValidationMessage> validatorMessages = validator.getValidationResult().getMessages();

		String messages = ""; 
		for (int i=0; i<validatorMessages.size(); i++) {
			messages+=validatorMessages.get(i).getText()+"\n"; 
		}

		PamDialogFX.showWarning(null, "Error", messages);
	}

	/**
	 * Check whether the pane is showing. 
	 * @return true oif the pane is showing. 
	 */
	private boolean isPaneShowing() {
		return sweepClassifier.getClassifierPane().getFlipPane().isBackVisible();
	}



}
