package clickDetector.layoutFX.clickClassifiers;


import fftFilter.FFTFilterParams;
import fftManager.FFTLengthModeled;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
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
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.picker.SymbolPicker;
import pamViewFX.fxNodes.utilityPanes.FreqBandPane;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
import pamViewFX.fxNodes.utilityPanes.SimpleFilterPaneFX;
import pamViewFX.PamGuiManagerFX;

import PamController.SettingsPane;
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
	
	PamBorderPane mainPane = new PamBorderPane();
	
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
		PamVBox spectrumHolder=new PamVBox(5); 

		energyBox=new EnergyBandBox();
		freqBox=new FrequencySearchBlock();
		spectrumHolder.getChildren().addAll(energyBox, freqBox); 
		spectrumHolder.setPadding(new Insets(10,0,0,0));
		spectrumTab.setContent(spectrumHolder);

		/**********Main Layout**************/
		
		TabPane tabPane= new TabPane(); 
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		tabPane.getTabs().addAll(waveformTab, spectrumTab); 
					
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
		 * Check box to enable pane
		 */
		private PamToggleSwitch enableBox;
		
		/**
		 * Border pane to hold content
		 */
		private PamBorderPane borderPane;
		
		
		private Font disableFont;

		private Label label;
		

		SweepBox(String borderTitle, Boolean enableButton) {

			//create holder pnae
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

			/**Don't like this in old swing version*/ 
			//tP.setCenter( description = new Label("", JLabel.CENTER));
			//this.setTop(tP);
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
			disbleControls(enabled);
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
	 * General options for the sweep classifier set
	 * @author Jamie Macaulay
	 *
	 */
	private class OptionsBox extends SweepBox implements FFTLengthModeled, CodeHost {
		
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

		OptionsBox() {
			super("General Options", false);
			this.getHolderPane().setCenter(createOptionsPane());
			
		}
		
		//create the general options 
		private Node createOptionsPane(){
			
			PamGridPane pamGridPane=new PamGridPane();
			pamGridPane.setHgap(5);
			pamGridPane.setVgap(5);
//			pamGridPane.setPadding(new Insets(10,5,5,35));

			pamGridPane.add(new Label("Name"), 0, 0);

			nameField=new TextField();
			nameField.setPrefColumnCount(10);
			pamGridPane.add(nameField, 0, 0);
			PamGridPane.setColumnSpan(nameField, 2);

			
			pamGridPane.add(new Label("Code"), 3, 0);

			codeSpinner=new PamSpinner<Integer> (0, 500, 0, 1);
			codeSpinner.setEditable(true);
			//codeSpinner.setPrefWidth(150);
			codeSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			pamGridPane.add(codeSpinner, 1, 0);
			
//			pamGridPane.add(new Label("Symbol"), 0,1);
			
			//create colour picker to allow users to change symbol colour. 
			symbolPicker=new SymbolPicker(); 
			pamGridPane.add(symbolPicker, 2, 0);
			
			pamGridPane.add(new Label("Symbol"), 3,0);

//			//create a button to allow users to change symbol shape. 
//			symbolColour=new ColorPicker(); 
//			pamGridPane.add(symbolColour, 3, 1);
//			symbolColour.setOnAction((action)->{
//				symbolPicker.setFillColour(symbolColour.getValue()); 
//			});
			
			//channel options
			pamGridPane.add(new Label("Channels"), 0,1);
			
			channelsBox = new ComboBox<String>();
			for (int i = 0; i < 3; i++) {
				channelsBox.getItems().add(SweepClassifierSet.getChannelOptionsName(i));
			}
			pamGridPane.add(channelsBox, 1,1);
			
			PamGridPane.setColumnSpan(channelsBox, 7);
			
			//restrict parameter to click centre
			PamHBox clickCenterBox=new PamHBox(); 
			clickCenterBox.setSpacing(5); 

			clickCenterBox.getChildren().add(new CheckBox("Analyse click ")); 
			
			clickLengthSpinner=new PamSpinner<Integer>(4,102400,128,32); 
			clickLengthSpinner.setEditable(true);
			//clickLengthSpinner.setPrefWidth(150);
			clickLengthSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			
			clickCenterBox.getChildren().add(clickLengthSpinner);
			clickCenterBox.getChildren().add(new Label("samples")); 
			clickCenterBox.getChildren().add(lengthMS=new Label("()")); 
			clickCenterBox.getChildren().add(new Label("around click center.")); 
			clickCenterBox.setAlignment(Pos.CENTER_LEFT);
			PamGridPane.setColumnSpan(clickCenterBox, 4);
			PamGridPane.setHgrow(clickCenterBox, Priority.ALWAYS);
			pamGridPane.add(clickCenterBox, 0,2);

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

			return pamGridPane; 

		}

		@Override
		public int getCode() {
			// TODO Auto-generated method stub
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

		@Override
		protected void setParams() {
			nameField.setText(sweepClassifierSet.getName());
			codeSpinner.getValueFactory().setValue(sweepClassifierSet.getSpeciesCode());
			channelsBox.getSelectionModel().select(sweepClassifierSet.channelChoices);
			clickLengthSpinner.getValueFactory().setValue(sweepClassifierSet.restrictedBins); 
			
		}

		@Override
		protected boolean getParams() {
			sweepClassifierSet.setName(nameField.getText());
			sweepClassifierSet.setSpeciesCode(codeSpinner.getValue());
			sweepClassifierSet.channelChoices=channelsBox.getSelectionModel().getSelectedIndex(); 
			sweepClassifierSet.restrictedBins=clickLengthSpinner.getValue(); 
			return true;
		}

		@Override
		protected void disbleControls(boolean enable) {
			// TODO Auto-generated method stub
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
			
			PamGridPane pamGridPane=new PamGridPane();
			pamGridPane.setHgap(5);
			pamGridPane.setVgap(5);
			
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
					smoothing, new Label("bins (must be odd)"), new Label("Threshold"), threshold, new Label("dB")); 
			clickLengthHolder1.setAlignment(Pos.CENTER_LEFT);

			
			//spinner
//			gridPane.add(new Label("Click Length"),0,1); 
			
			minLengthms=new PamSpinner<Double>(0.00, 1.00, 0.03,0.01);
			minLengthms.setEditable(true);
			minLengthms.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			//minLengthms.setPrefWidth(130);
						
			maxLengthms=new PamSpinner<Double>(0.00, 1.00, 0.22,0.01);
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

			
			PamVBox vboxholder=new PamVBox();
			vboxholder.setSpacing(5);
			vboxholder.getChildren().addAll(clickLengthHolder1, clickLengthHolder2);
			
			return vboxholder; 

		}

		@Override
		protected void setParams() {
			smoothing.getValueFactory().setValue(sweepClassifierSet.peakSmoothing);
			minLengthms.getValueFactory().setValue(sweepClassifierSet.minLength);
			maxLengthms.getValueFactory().setValue(sweepClassifierSet.maxLength);
			threshold.getValueFactory().setValue(sweepClassifierSet.lengthdB);
		}

		@Override
		protected boolean getParams() {
			sweepClassifierSet.peakSmoothing=smoothing.getValue();
			sweepClassifierSet.minLength=minLengthms.getValue();
			sweepClassifierSet.maxLength=maxLengthms.getValue();
			sweepClassifierSet.lengthdB=threshold.getValue();
			return true;
		}

		@Override
		protected void disbleControls(boolean disable) {
			smoothing.setDisable(disable);
			minLengthms.setDisable(disable);
			maxLengthms.setDisable(disable);
			threshold.setDisable(disable);
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
		private FreqBandPane contralBandFreqPane1;
		
		/**
		 * Frequency pane for the second control band. 
		 */
		private FreqBandPane contralBandFreqPane2;
		
		/**
		 * Spinner for the first control band
		 */
		PamSpinner<Double> thresholdSpinner1; 

		/**
		 * Spinner for the second control band
		 */
		PamSpinner<Double> thresholdSpinner2; 

		EnergyBandBox() {
			super("Energy Band", true);
			this.getHolderPane().setCenter(createOptionsPane());
			
		}
		
		//create the general options 
		private Node createOptionsPane(){
			
			PamGridPane pamGridPane=new PamGridPane();
			pamGridPane.setHgap(5);
			pamGridPane.setVgap(5);
			
			Label freqLabel=new Label("Frequency (Hz)");
			pamGridPane.add(freqLabel, 0, 0);
			pamGridPane.add(new Label("Threshold (dB)"), 2, 0);
			PamGridPane.setHalignment(freqLabel, HPos.CENTER);
			PamGridPane.setColumnSpan(pamGridPane, 2);

			//test band
			pamGridPane.add(new Label("Test Band"), 0, 1);

			testBandFreqPane=new FreqBandPane(Orientation.HORIZONTAL);
			testBandFreqPane.setBandText("");
			pamGridPane.add(testBandFreqPane, 1, 1);
			
			//control band 1
			pamGridPane.add(new Label("Control Band"), 0, 2);
			contralBandFreqPane1=new FreqBandPane(Orientation.HORIZONTAL);		
			contralBandFreqPane1.setBandText("");
			pamGridPane.add(contralBandFreqPane1, 1, 2);
			thresholdSpinner1=new PamSpinner<Double>(0,100.,6.,0);
			thresholdSpinner1.setEditable(true);
			pamGridPane.add(thresholdSpinner1, 2, 2);
			thresholdSpinner1.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			thresholdSpinner1.setPrefWidth(100);

			//control band 2
			pamGridPane.add(new Label("Control Band"), 0, 3);
			contralBandFreqPane2=new FreqBandPane(Orientation.HORIZONTAL);
			contralBandFreqPane2.setBandText("");
			pamGridPane.add(contralBandFreqPane2, 1, 3);
			thresholdSpinner2=new PamSpinner<Double>(0,100.,6.,0);
			thresholdSpinner2.setEditable(true);
			thresholdSpinner2.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			pamGridPane.add(thresholdSpinner2, 2, 3);
			thresholdSpinner2.setPrefWidth(100);
				
			return pamGridPane; 

		}
		
		
		@Override
		protected void setParams() {
			//set sample rate. 
		}

		@Override
		protected boolean getParams() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected void disbleControls(boolean disable) {
			testBandFreqPane.setDisableFreqPane(disable);
			contralBandFreqPane1.setDisableFreqPane(disable);
			contralBandFreqPane2.setDisableFreqPane(disable);
			thresholdSpinner1.setDisable(disable);
			thresholdSpinner2.setDisable(disable);
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

		FrequencySearchBlock() {
			super("Peak and Mean Frequency", true);
			this.getHolderPane().setCenter(createFreqSearchPane());
		}
		
		private Node createFreqSearchPane(){
			
			PamGridPane pamGridPane=new PamGridPane();
			pamGridPane.setHgap(5);
			pamGridPane.setVgap(5);
			
			
			//search and integration range
			pamGridPane.add(new Label("Search Range"),1,0);

			searchRange=new FreqBandPane(Orientation.HORIZONTAL);		
			searchRange.setBandText("");
			pamGridPane.add(searchRange,2,0);
			
			pamGridPane.add(new Label("Smoothing"), 3,0);
			
			smoothing=new PamSpinner<Integer>(3,101,5,2); 
			smoothing.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			smoothing.setPrefWidth(100);
			pamGridPane.add(smoothing, 4,0);
			
			pamGridPane.add(new Label("bins"), 5,0);

			
			//peak frequency
			peakFreqCheckBox=new PamToggleSwitch("");
			peakFreqCheckBox.selectedProperty().addListener((obsVal, oldVal, newVal)->{
				peakFreqPane.setDisableFreqPane(!peakWidthCheckBox.isSelected());
			});
			
			pamGridPane.add(peakFreqCheckBox,0,1);
			
			pamGridPane.add(new Label("Peak Frequency"),1,1);

			peakFreqPane=new FreqBandPane(Orientation.HORIZONTAL);		
			peakFreqPane.setBandText("");
			pamGridPane.add(peakFreqPane,2,1);
					
			
			//peak width
			peakWidthCheckBox=new PamToggleSwitch("");
			peakWidthCheckBox.selectedProperty().addListener((obsVal, oldVal, newVal)->{
				//peakWidthPane.setDisable(!peakWidthCheckBox.isSelected());
				peakWidthPane.setDisableFreqPane(!peakWidthCheckBox.isSelected());
				threshold.setDisable(!peakWidthCheckBox.isSelected());
			});
			
			pamGridPane.add(peakWidthCheckBox,0,2);
			
			pamGridPane.add(new Label("Peak Width"),1,2);

			peakWidthPane=new FreqBandPane(Orientation.HORIZONTAL);		
			peakWidthPane.setBandText("");
			pamGridPane.add(peakWidthPane,2,2);
			
			pamGridPane.add(new Label("Threshold"), 3,2);
			
			threshold=new PamSpinner<Double>(1., 300., 6.,1.);
			threshold.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			threshold.setPrefWidth(100);
			
			pamGridPane.add(threshold,4,2); 
			
			pamGridPane.add(new Label("dB"), 5,2);

			
			//mean frequency
			meanFreqCheckBox=new PamToggleSwitch("");
			meanFreqCheckBox.selectedProperty().addListener((obsVal, oldVal, newVal)->{
				meanFreq.setDisableFreqPane(!peakWidthCheckBox.isSelected());
			});
			
			pamGridPane.add(meanFreqCheckBox,0,3);

			pamGridPane.add(new Label("Mean Frequency"),1,3);

			meanFreq=new FreqBandPane(Orientation.HORIZONTAL);		
			meanFreq.setBandText("");
			pamGridPane.add(meanFreq,2,3);
			
			return pamGridPane;
			
		}

		@Override
		protected void setParams() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected boolean getParams() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected void disbleControls(boolean enable) {
			// TODO Auto-generated method stub
			
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
		private PamSpinner<Integer> zeroCorssingsMin;
		
		/**
		 * Spinner for the maximum number of zero crossings 
		 */
		private PamSpinner<Integer> zeroCorssingsMax;

		/**
		 * Spinner for the minimum zero crossing frequency sweep in kHz/ms
		 */
		private PamSpinner<Double> freqZeroMin;

		/**
		 * Spinner for the minimum zero crossing frequency sweep in kHz/ms
		 */
		private PamSpinner<Double> freqZeroMax;

		ZeroCrossingsBox() {
			super("Zero Crossings", true);
			this.getHolderPane().setCenter(createZeroCrossPane());
		}
		
		private Node createZeroCrossPane(){
			
			PamGridPane pamGridPane=new PamGridPane();
			pamGridPane.setHgap(5);
			pamGridPane.setVgap(5);
			
			//Number of zeros crossings
			pamGridPane.add(new Label("Number of zero corssings"),0,0);
			
			zeroCorssingsMin=new PamSpinner<Integer>(0,999999,0,1); 
			zeroCorssingsMin.setEditable(true);
			zeroCorssingsMin.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			zeroCorssingsMin.setPrefWidth(100);
			pamGridPane.add(zeroCorssingsMin, 1,0);
			
			pamGridPane.add(new Label("to"),2,0);
			
			zeroCorssingsMax=new PamSpinner<Integer>(0,999999,0,1); 
			zeroCorssingsMax.setEditable(true);
			zeroCorssingsMax.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			zeroCorssingsMax.setPrefWidth(100);
			pamGridPane.add(zeroCorssingsMax, 3,0);
			
			//zero crossing freuquency sweep 
			
			pamGridPane.add(new Label("Zero crossing frequency sweep"),0,1);
			
			freqZeroMin=new PamSpinner<Double>(0.,999999.,0.,1.); 
			freqZeroMin.setEditable(true);
			freqZeroMin.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			freqZeroMin.setPrefWidth(100);
			pamGridPane.add(freqZeroMin, 1,1);
			
			pamGridPane.add(new Label("to"),2,1);
			
			freqZeroMax=new PamSpinner<Double>(0.,999999.,0,1); 
			freqZeroMax.setEditable(true);
			freqZeroMax.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			freqZeroMax.setPrefWidth(100);
			pamGridPane.add(freqZeroMax, 3,1);
			
			pamGridPane.add(new Label("KHz/ms"),4,1);

			
			return pamGridPane;
			
		}

		@Override
		protected void setParams() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected boolean getParams() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected void disbleControls(boolean disable) {
			zeroCorssingsMin.setDisable(disable);
			zeroCorssingsMax.setDisable(disable);
			freqZeroMin.setDisable(disable);
			freqZeroMax.setDisable(disable);
		}
		
	}
	
	/**
	 * Controls to change the minimum and maximum amplitude range for the classifier. 
	 * @author Jamie Macaulay 
	 *
	 */
	private class AmplitudeBlock extends SweepBox {
		private TextField[] ampRange = new TextField[2];
		
		public AmplitudeBlock() {
			super("Amplitude Range", true);
			setDescription("Set a minimum and maximum click amplitude for this type");
			
			PamGridPane gridPane= new PamGridPane(); 
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
		}

		@Override
		protected boolean getParams() {
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
			for (int i = 0; i < 2; i++) {
				ampRange[i].setDisable(getEnableBox());
			}
			
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

}
