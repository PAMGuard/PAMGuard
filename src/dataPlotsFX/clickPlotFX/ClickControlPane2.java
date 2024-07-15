package dataPlotsFX.clickPlotFX;


import org.controlsfx.control.ToggleSwitch;

import PamView.GeneralProjector.ParameterType;
import PamView.symbol.PamSymbolManager;
import clickDetector.tdPlots.ClickSymbolOptions;
import dataPlotsFX.layout.TDSettingsPane;
import dataPlotsFX.spectrogramPlotFX.SpectrogramControlPane;
import fftManager.layoutFX.FFTPaneFX;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamScrollPane;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.sliders.PamSlider;
import pamViewFX.fxNodes.utilityPanes.GroupedSourcePaneFX;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;
import pamViewFX.fxNodes.utilsFX.DualControlField;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;
import pamViewFX.symbol.StandardSymbolOptionsPane;

/**
 * Pane for changing click detections display properties. 
 * Mainly used for changing click colours, and selecting what channels to show. 
 * <p>
 * This is based on the generic PAMGuard symbol chooser
 * 
 * @author Jamie Macaulay
 *
 */
public class ClickControlPane2 extends PamBorderPane implements TDSettingsPane {

	/**
	 * Flag for the standard threshold frequency pane.
	 */
	private static final int FREQ_PANE_THRESHOLD = 0; 

	/**
	 * Flag for the colour frequency pane. 
	 */
	private static final int FREQ_PANE_COLOUR = 1; 

	/**
	 * The preferred width.
	 */
	private static final double PREF_WIDTH=300; 

	/**
	 * The preferred height.
	 */
	private static final double PREF_HEIGHT=400; 


	private Image clickIcon=new Image(getClass().getResourceAsStream("/Resources/reanalyseClicks.png"));

	/**
	 * Reference to the click plot info. 
	 */
	private ClickPlotInfoFX clickPlotInfo;

	/**
	 * Choice of channels to plot
	 */
	final ChoiceBox<Object> channelChoiceBox;

	/**
	 * The channel list
	 */
	private ObservableList<Object> channelList=FXCollections.observableArrayList();

	/**
	 * The data select pane for clicks.  
	 */
	private DynamicSettingsPane<Boolean> dataSelectPane;

	/**
	 * The symbols options pane. 
	 */
	private StandardSymbolOptionsPane symbolOptionsPane;

	/**
	 * The current pane with frequency options. These are changeable depending on the current selections
	 */
	private PamBorderPane freqPane;

	/**
	 * Pane which shows colour limits for spectrogram of clicks
	 */
	private SpectrogramControlPane spectroControlPane;

	/**
	 * Pane which shows the a slider to allow users to change colour limits of FFT's plotted on the pane.
	 */
	private Pane freqColourPane;


	/**
	 * Pane which shows the a slider to allow users to change spectra peaks plotted on frequency pane. 
	 */
	private Pane freqThresholdPane;

	/**
	 * Length spinner for FFT
	 */
	private PamSpinner<Integer> fftSpinnerLength;

	/**
	 * Spinner for FFT hop. 
	 */
	private Spinner<Integer> fftSpinnerHop;

	/**
	 * The minimum and maximum click width pane. 
	 */
	private DualControlField<Double> minMaxWidthPane;

	/**
	 * The minimum and maximum click height pane. 
	 */
	private DualControlField<Double> minMaxHeightPane;

	/**
	 * The click size pane
	 */
	private Pane clickSizePane;

	/**
	 * Threshold slider for drawing clicks on frequency axis. 
	 */
	private PamSlider slider;

	//	/**
	//	 * The minimum frequency colour limit amplitude
	//	 */
	//	private DoubleProperty amplitudePropertyMin = new SimpleDoubleProperty(); 
	//	
	//	/**
	//	 * The maximum frequency colour limit
	//	 */
	//	private DoubleProperty amplitudePropertyMax = new SimpleDoubleProperty();

	/**
	 * True to disable getParmas  - used when changing the controls which have change listeners.
	 */
	private boolean disableGetParams;

	/**
	 * Holds symbol specific settings for clicks. 
	 */
	private PamVBox symbolOptionsHolder;

	/**
	 * Pane which holds extra symbol options such as click size. 
	 */
	private PamBorderPane extraSymbolPane;

	private ToggleSwitch freqSwitch; 


	public ClickControlPane2(ClickPlotInfoFX clickPlotInfo) {
		super();
		this.clickPlotInfo=clickPlotInfo;

		PamVBox dataSelectHolder= new PamVBox();
		dataSelectHolder.setSpacing(5);
		dataSelectHolder.setPadding(new Insets(5,5,5,5));

		dataSelectPane = createDataSelectPane();
		dataSelectPane.setParams(true);


		Label channelLabel = new Label("Channels");
		PamGuiManagerFX.titleFont2style(channelLabel);

		channelChoiceBox = new ChoiceBox<Object>();
		channelChoiceBox.setPrefWidth(170);
		setChannelItems();

		dataSelectHolder.getChildren().addAll(dataSelectPane.getContentNode(), channelLabel, channelChoiceBox); 

		Tab symbolTab=new Tab("Symbol");
		//put inside a scroll pane so tht on low dpi displays can still access controls
		PamScrollPane scrollPane2=new PamScrollPane(createSymbolOptionsPane().getContentNode());
		//scrollPane2.setFitToWidth(true);
		scrollPane2.setFitToHeight(true);
		scrollPane2.setFitToWidth(true);
		scrollPane2.setHbarPolicy(ScrollBarPolicy.NEVER);
		scrollPane2.getStyleClass().clear();
		symbolTab.setContent(scrollPane2);
		
		Tab dataView=new Tab("Data");
		dataView.setContent(dataSelectHolder);
		dataView.getStyleClass().add("tab-square");

		TabPane tabPane= new TabPane(); 
		tabPane.setSide(Side.TOP);
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		tabPane.getTabs().addAll(symbolTab, dataView);
		tabPane.prefHeightProperty().bind(this.heightProperty());
		tabPane.prefWidthProperty().bind(this.widthProperty());

		//default layout
		enablePane();
		setFreqType();
		setParams(); 
		
		dataSelectPane.addSettingsListener(()->{
			//dynamic settings pane so have to repaint whenever a control is selected. 
			getParams();
			
			/**
			 * If there are raw amplitude or frequency panes that have a buffer of painted units then
			 * these have to be cleared for the data selector
			 */
			clickPlotInfo.getClickRawPlotManager().clear();
			clickPlotInfo.getClickFFTPlotManager().clear();

			
			clickPlotInfo.getTDGraph().repaint(50);
		});

		this.setCenter(tabPane);

		this.setPrefHeight(PREF_HEIGHT);
		this.setPrefWidth(PREF_WIDTH);
	}

	/**
	 * The create data selector pane from the click data block.  
	 * @return the data select pane. 
	 */
	private DynamicSettingsPane<Boolean> createDataSelectPane(){		
		System.out.println("DATA SELECTOR: " + clickPlotInfo.getClickDataSelector());
		return clickPlotInfo.getClickDataSelector().getDialogPaneFX();
	}

	/**
	 * Create the symbol options pane. 
	 * @return the symbol options pane. 
	 */
	private StandardSymbolOptionsPane createSymbolOptionsPane(){

		PamSymbolManager<?> pamSymbolManager=  clickPlotInfo.getDataBlock().getPamSymbolManager();

		//StandardSymbolManager standardSymbolManager = (StandardSymbolManager) pamSymbolManager; 
		//System.out.println("HAS LINE SYMBOL: " +  standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_LINE_LENGTH) 
		//+ "  " + standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_LINE) ); 
		symbolOptionsPane= (StandardSymbolOptionsPane)  pamSymbolManager.getFXOptionsPane(clickPlotInfo.getTDGraph().getUniqueName(), 
				clickPlotInfo.getTDGraph().getGraphProjector()); 

		//		//remove the line box. 
		//		symbolOptionsPane.getVBoxHolder().getChildren().remove(symbolOptionsPane.getLinBox()); 
		//		
		//create a new settings listener
		symbolOptionsPane.addSettingsListener(()->{
			clickPlotInfo.getClickSymbolChooser().notifySettingsChange(); 
			enablePane(); 
			setFreqType();
			newSettings();
		});
		//				
		//		//we want to control where the frequency pane is placed. 
		//		symbolOptionsPane.getVBoxHolder().getChildren().remove(symbolOptionsPane.getFreqPane()); 
		//		symbolOptionsPane.getVBoxHolder().getChildren().remove(symbolOptionsPane.getSymbolBox()); 
		//
		//		//going to hack this a bit and replace the line length box with an FFT slider.
		//		symbolOptionsPane.getVBoxHolder().getChildren().add(symbolOptionsHolder = new PamVBox());
		//		symbolOptionsHolder.setSpacing(5);
		//
		//		freqPane = new PamBorderPane();
		clickSizePane = this.createClickSizePane(); //pane for selecting the size of clicks. 
		//
		/**Frequency Pane**/
		
		freqThresholdPane = createFreqThresholdPane(); //pane for spectrogram showing clicks
		freqColourPane = createFreqColorPane(); //pane for setting colour limits if clicks shown as spectrogram

		freqPane = new PamBorderPane(); 

		PamHBox hBox = new PamHBox(); 
		hBox.setSpacing(5);
		freqSwitch = new ToggleSwitch(); 
		freqSwitch.setMaxWidth(20);
		Label specLabel = new Label("Show Spectrogram "); 
		specLabel.setStyle("-fx-font-weight: bold");	
		hBox.getChildren().addAll(freqSwitch, specLabel);
		freqSwitch.selectedProperty().addListener((obsVal, oldVal, newVal)->{
			enablePane();
			//setFreqPaneType(newVal ? FREQ_PANE_COLOUR : FREQ_PANE_THRESHOLD); 
			newSettings(100);
		});

		Label freqTitle = new Label("Freq. Colour");
		PamGuiManagerFX.titleFont2style(freqTitle);
		
		PamVBox vBox = new PamVBox(); 
		vBox.setSpacing(5);
		vBox.getChildren().addAll(freqTitle, hBox ); 
		freqPane.setTop(vBox);
		
		symbolOptionsPane.getMainPane().setCenter(extraSymbolPane = new PamBorderPane());
		symbolOptionsPane.getMainPane().setMaxWidth(PREF_WIDTH);

		//		symbolOptionsPane.getLinBox().prefWidthProperty().bind(this.widthProperty());
		//symbolOptionsPane.getLinBox().add(symbolOptionsPane.getLineColorPicker(), 1, 1);
		return symbolOptionsPane;
	}


	/**
	 * Set the type of frequency pane
	 * @param type - the frequency pane e.g. 
	 */
	private void setFreqPaneType(int type) {
		//symbolOptionsPane.getContentNode().setDisable(false);
		switch (type) {
		case FREQ_PANE_THRESHOLD:
			//			System.out.println("Set the new pane: " + type); 
			freqPane.setCenter(freqThresholdPane);

			break;
		case FREQ_PANE_COLOUR:
			//symbolOptionsPane.getContentNode().setDisable(true);
			//			System.out.println("Set the new pane: " + type); 
			freqPane.setCenter(freqColourPane);
			break; 
		}
	}


	/**
	 * Set the correct enable and disabling of the pane. This is quite complex because there are a lot of options!
	 */
	public void enablePane() {
		symbolOptionsPane.getVBoxHolder().setDisable(false);

		if (this.isFreqAxis()) {
			setFreqPaneType(FREQ_PANE_THRESHOLD); 
			extraSymbolPane.setCenter(freqPane);
			setFreqPaneType(freqSwitch.isSelected() ? FREQ_PANE_COLOUR : FREQ_PANE_THRESHOLD); 
			
			symbolOptionsPane.getVBoxHolder().setDisable(freqSwitch.isSelected());

		}
		else {
			extraSymbolPane.setCenter(clickSizePane);
		}
		
		//		//the colour choice 
		//		int colourChoice = ((ClickSymbolOptions) clickPlotInfo.getClickSymbolChooser().getSymbolChooser().getSymbolOptions()).colourChoice;
		//		
		//		//clear all children from the pane. 
		//		this.symbolOptionsHolder.getChildren().clear(); 
		//
		//		
		//		symbolOptionsPane.disableSymbolColourBox(false);
		//		symbolOptionsPane.disableSymbolChooserBox(false);
		//		clickSizePane.setDisable(false);
		//		symbolOptionsPane.getFreqPane().setDisable(false);
		//
		//
		//		switch (colourChoice) {
		//		case StandardSymbolOptions.COLOUR_BY_SUPERDET:
		//		case StandardSymbolOptions.COLOUR_SUPERDET_THEN_SPECIAL:
		//		case StandardSymbolOptions.COLOUR_SPECIAL:
		//		case StandardSymbolOptions.COLOUR_FIXED:
		//
		//			if (this.isFreqAxis()) {
		//				symbolOptionsHolder.getChildren().addAll(symbolOptionsPane.getSymbolBox(),this.freqPane); 
		//				this.setFreqPaneType(FREQ_PANE_THRESHOLD);
		//				symbolOptionsPane.disableSymbolChooserBox(true);
		//			}			
		//			else if (this.isStemAxis()) {
		//				symbolOptionsHolder.getChildren().addAll(symbolOptionsPane.getSymbolBox()); 		
		//				symbolOptionsPane.disableSymbolChooserBox(true);
		//			}
		//			else {
		//				symbolOptionsHolder.getChildren().addAll(symbolOptionsPane.getSymbolBox(), this.clickSizePane); 
		//			}
		//			break; 		
		//		case StandardSymbolOptions.COLOUR_HYDROPHONE:
		//			if (this.isFreqAxis()) {
		//				symbolOptionsHolder.getChildren().addAll( this.freqPane); 
		//				this.setFreqPaneType(FREQ_PANE_THRESHOLD);
		//				symbolOptionsPane.disableSymbolChooserBox(true);
		//			}	
		//			else if (this.isStemAxis()) {
		//				//nothing to add here 
		//			}
		//			else {
		//				symbolOptionsHolder.getChildren().addAll(symbolOptionsPane.getSymbolBox(), this.clickSizePane); 		
		//				symbolOptionsPane.disableSymbolColourBox(true);
		//			}
		//			break; 		
		//		case ClickDetSymbolManager.COLOUR_BY_FREQ:
		//			if (this.isFreqAxis()) {
		//				this.setFreqPaneType(FREQ_PANE_COLOUR);
		//				symbolOptionsHolder.getChildren().addAll(this.freqPane); 
		//			}
		//			else if (this.isStemAxis()) {
		//				symbolOptionsHolder.getChildren().addAll(symbolOptionsPane.getFreqPane()); 
		//
		//			}
		//			else {
		//				symbolOptionsHolder.getChildren().addAll(symbolOptionsPane.getSymbolBox(), this.clickSizePane, symbolOptionsPane.getFreqPane()); 
		//				symbolOptionsPane.disableSymbolColourBox(true);
		//			}
		//			break; 
		//		}

	}

	/**
	 * Check whether the frequency axis has been selected 
	 * @return true if the frequency axis is currently selected
	 */
	private boolean isFreqAxis() {
		return clickPlotInfo.getScaleInfo().getDataTypeInfo().dataType == ParameterType.FREQUENCY;
	}

	/**
	 * Check whether the amplitude stem axis has been selected 
	 * @return true if the amplitude stem is currently selected
	 */
	private boolean isStemAxis() {
		return clickPlotInfo.getScaleInfo().getDataTypeInfo().dataType == ParameterType.AMPLITUDE_STEM;
	}

	/**
	 * Create a pane to change the click length.
	 * @return the xclick size pane. 
	 */
	public Pane createClickSizePane() {

		PamVBox vBox = new PamVBox(); 
		vBox.setSpacing(5);

		minMaxWidthPane = new DualControlField<Double>("Min", "Max" , "", 2, 100, 1); 
		minMaxWidthPane.addChangeListener((obsval, oldval, newval)->{
			newSettings();
			//do not allow the min ti be larger than the max. 
			if (minMaxWidthPane.getSpinner().getValue()>=minMaxWidthPane.getSpinner2().getValue()) {
				minMaxWidthPane.getSpinner().getValueFactory().setValue(Math.max(2, minMaxWidthPane.getSpinner2().getValue()-1.0));
				return;
			}

			if (minMaxWidthPane.getSpinner2().getValue()<=minMaxWidthPane.getSpinner().getValue()) {
				minMaxWidthPane.getSpinner2().getValueFactory().setValue(Math.max(3,minMaxWidthPane.getSpinner().getValue()+1.0));
			}
		});
		minMaxWidthPane.setPrefSpinnerWidth(80); 
		Label newLabelWidth = new Label(); 
//		newLabelWidth.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.ARROWS_H, 
//				PamGuiManagerFX.iconSize)); 
		newLabelWidth.setGraphic(PamGlyphDude.createPamIcon("mdi2a-arrow-left-right-bold",	PamGuiManagerFX.iconSize)); 
		newLabelWidth.setPrefWidth(20);
		minMaxWidthPane.getChildren().add(0, newLabelWidth);

		
		//height pane
		minMaxHeightPane = new DualControlField<Double>("Min", "Max" , "", 2, 100, 1);  
		minMaxHeightPane.addChangeListener((obsval, oldval, newval)->{
			newSettings();
			//do not allow the min ti be larger than the max. 
			if (minMaxHeightPane.getSpinner().getValue()>=minMaxHeightPane.getSpinner2().getValue()) {
				minMaxHeightPane.getSpinner().getValueFactory().setValue(minMaxHeightPane.getSpinner2().getValue()-1.0);
				return;
			}

			if (minMaxHeightPane.getSpinner2().getValue()<=minMaxHeightPane.getSpinner().getValue()) {
				minMaxHeightPane.getSpinner2().getValueFactory().setValue(minMaxHeightPane.getSpinner().getValue()+1.0);
			}
		});
		minMaxHeightPane.setPrefSpinnerWidth(80); 

		Label newLabelHeight = new Label(); 
//		newLabelHeight.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.ARROWS_V, 
//				PamGuiManagerFX.iconSize)); 
		newLabelHeight.setGraphic(PamGlyphDude.createPamIcon("mdi2a-arrow-up-down-bold",	PamGuiManagerFX.iconSize)); 
		newLabelHeight.setPrefWidth(20);
		minMaxHeightPane.getChildren().add(0, newLabelHeight);


		Label title = new Label("Click size"); 
		PamGuiManagerFX.titleFont2style(title);

		vBox.getChildren().addAll(title, minMaxWidthPane, minMaxHeightPane); 

		return vBox; 
	}

	/**
	 * There are new settings. Repaints the graph. 
	 */
	private void newSettings() {
		newSettings(0);
	}

	/**
	 * There are new settings. Repaints the graph. 
	 * @param milliswait
	 */
	private void newSettings(long milliswait) {
		//System.out.println("ClickcontrolPane2: New click detector settings: "); 
		getParams();

		//on a parameter change must clear the FFT plot. 
		clickPlotInfo.getClickFFTPlotManager().clear(); 
		clickPlotInfo.getClickRawPlotManager().clear(); 

		clickPlotInfo.getTDGraph().repaint(milliswait);
	}

	/**
	 * Create the pane with a frequency threshold slider. 
	 * @return the frequency threshold pane. 
	 */
	private Pane createFreqThresholdPane() {

		PamVBox vBox = new PamVBox(); 
		vBox.setSpacing(5);


		//going to hack this a bit and replace the line length box with an FFT slider. 
		slider = new PamSlider(0, 100, 50);
		slider.valueProperty().addListener((observable, old_val, new_val) -> {
			newSettings();
		});

		slider.setShowTickMarks(true);
		slider.setShowTickLabels(true);
		slider.setMajorTickUnit(25f);

		//		symbolOptionsPane.getFillColorPicker().valueProperty().addListener((observable, oldValue, newValue) -> {
		//			//change the colour of the colour range slider.    
		//			newSettings();
		//			slider.setTrackColor(PamUtilsFX.awtToFXColor(symbolOptionsPane.getParams(null).symbolData.getFillColor())); 
		//		});		
		//slider.setTrackColor(PamUtilsFX.awtToFXColor(symbolOptionsPane.getParams(null).symbolData.getFillColor())); 

		//		slider.prefWidthProperty().bind(symbolOptionsPane.getLinBox().widthProperty());

		PamHBox.setHgrow(slider, Priority.ALWAYS);

		//System.out.println("Set the new pane: " + type); 
		vBox.getChildren().addAll(new Label("Freq. peak threshold (%)"), slider); 

		return vBox; 
	}

	/**
	 * Create the frequency colouring pane. 
	 * @return the frequency colouring pane. 
	 */
	private Pane createFreqColorPane() {

		PamVBox pamVBox = new PamVBox(); 
		pamVBox.setSpacing(5);


		spectroControlPane = new SpectrogramControlPane(Orientation.HORIZONTAL, true, false, true);


		//colours should change with colour slider. 
		spectroControlPane.getColourSlider().lowValueProperty().addListener((obsval, oldval, newval)->{
			newSettings(500);
		});

		spectroControlPane.getColourSlider().highValueProperty().addListener((obsval, oldval, newval)->{
			newSettings(500);
		});

		spectroControlPane.getColorBox().valueProperty().addListener((obsval, oldval, newval)->{
			newSettings();
		});
		
		//need to do this so that text of colourbar is not squished when in a scrollpane
		spectroControlPane.setMinHeight(85);


		PamGridPane pamGridPane=new PamGridPane();
		pamGridPane.setHgap(5);
		pamGridPane.setVgap(5);

		//FFT length
		pamGridPane.add(new Label("FFT Length"), 0, 0);

		fftSpinnerLength=new PamSpinner<Integer>(FFTPaneFX.createStepList());
		fftSpinnerLength.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		fftSpinnerLength.getValueFactory().valueProperty().addListener((obs, before, after)->{
			if (after==0) fftSpinnerLength.getValueFactory().setValue(before==0 ? 32 : before);
			newSettings(100);
		});
		pamGridPane.add(fftSpinnerLength, 1, 0);

		//FFT Hop
		pamGridPane.add(new Label("FFT Hop"), 0, 1);

		fftSpinnerHop=new PamSpinner<Integer>(32,(int) Math.pow(2,24),512,16);
		fftSpinnerHop.setEditable(true);
		fftSpinnerHop.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		fftSpinnerHop.getValueFactory().valueProperty().addListener((obs, before, after)->{
			
			if (after==0) fftSpinnerHop.getValueFactory().setValue(before==0 ? 32 : before);
			newSettings(100);
		});

		pamGridPane.add(fftSpinnerHop, 1, 1);


		pamVBox.getChildren().addAll(spectroControlPane, pamGridPane); 


		return pamVBox; 
	}


	/**
	 * Set the list i the channel choice box of available channels to show. 
	 */
	public void setChannelItems(){

		channelChoiceBox.getItems().clear();
		channelChoiceBox.getSelectionModel().clearSelection();

		channelList.clear();
		int channels = clickPlotInfo.getClickControl().getClickParameters().getChannelBitmap();
		int[] channelGroups = clickPlotInfo.getClickControl().getClickParameters().getChannelGroups();
		int nChannelGroups = GroupedSourcePaneFX.countChannelGroups(channels, channelGroups);

		String str;
		str="Show all channel groups";
		channelList.add(str);
		int selected = 0;
		if (clickPlotInfo.getDisplayChannels() == 0) selected=0;
		int groupChannels;
		if (nChannelGroups > 1) {
			for (int i = 0; i < nChannelGroups; i++) {
				if (i==0) channelList.add(new Separator());
				str = "Show channels " + GroupedSourcePaneFX.getGroupList(i, channels, channelGroups);
				groupChannels = GroupedSourcePaneFX.getGroupChannels(i, channels, channelGroups);
				if (clickPlotInfo.getDisplayChannels() == groupChannels) selected=i+1;
				channelList.add(str);
			}
		}
		//System.out.println("Channels: "+channelList.toString()+" "+selected);
		channelChoiceBox.setItems(channelList);
		channelChoiceBox.getSelectionModel().select(selected);
		channelChoiceBox.setOnAction((action)->{
			if (channelChoiceBox.getSelectionModel().getSelectedIndex()==-1) return; 
			newSettings(0);
		});

	}

	/**
	 * Set parameters for the clicks control pane. 
	 */
	public void setParams() {

		disableGetParams = true; 

		/**Symbol options***/
		symbolOptionsPane.setParams((ClickSymbolOptions) clickPlotInfo.getClickSymbolChooser().getSymbolChooser().getSymbolOptions());


		if (clickPlotInfo.getClickDisplayParams().minClickLength>=clickPlotInfo.getClickDisplayParams().maxClickLength) {
			clickPlotInfo.getClickDisplayParams().maxClickLength=clickPlotInfo.getClickDisplayParams().minClickLength+1; 
		}

		if (clickPlotInfo.getClickDisplayParams().minClickHeight>=clickPlotInfo.getClickDisplayParams().maxClickHeight) {
			clickPlotInfo.getClickDisplayParams().maxClickHeight=clickPlotInfo.getClickDisplayParams().minClickHeight+1; 
		}

		/***Click sizes***/
		minMaxWidthPane.setValue((double) clickPlotInfo.getClickDisplayParams().minClickLength);
		minMaxWidthPane.setValue2((double) clickPlotInfo.getClickDisplayParams().maxClickLength); 
		minMaxHeightPane.setValue((double) clickPlotInfo.getClickDisplayParams().minClickHeight); 
		minMaxHeightPane.setValue2((double) clickPlotInfo.getClickDisplayParams().maxClickHeight); 		

		//TODO set frequency colour limits. 
		this.fftSpinnerHop.getValueFactory().setValue(clickPlotInfo.getClickDisplayParams().fftHop);
		this.fftSpinnerLength.getValueFactory().setValue(clickPlotInfo.getClickDisplayParams().fftLength);

		if (clickPlotInfo.getClickDisplayParams().colourMap== null) {
			clickPlotInfo.getClickDisplayParams().colourMap = ColourArrayType.HOT; 
		}
		this.spectroControlPane.setColourArrayType(clickPlotInfo.getClickDisplayParams().colourMap);

		if (clickPlotInfo.getClickDisplayParams().freqAmplitudeRange==null) {
			//can happen with old .psfx save files. 
			clickPlotInfo.getClickDisplayParams().freqAmplitudeRange = clickPlotInfo.getClickDisplayParams().getDefaultFreqAmpRange(); 
		}; 

//		if (clickPlotInfo.getClickDisplayParams().freqAmplitudeLimits==null) {
//			//can happen with old .psfx save files. 
			clickPlotInfo.getClickDisplayParams().freqAmplitudeLimits =clickPlotInfo.getClickDisplayParams().getDefaultFreqAmpLimits(); 
//		}

		this.spectroControlPane.setAmplitudeRange(clickPlotInfo.getClickDisplayParams().freqAmplitudeRange, 
				clickPlotInfo.getClickDisplayParams().freqAmplitudeLimits);

		freqSwitch.setSelected(!clickPlotInfo.getClickDisplayParams().thresholdFFT);
		/***Frequency options***/
		//set the slider colour
		slider.setTrackColor(PamUtilsFX.awtToFXColor(symbolOptionsPane.getParams(null).symbolData.getFillColor())); 

		/*** Data select pane****/
		setChannelItems();
		dataSelectPane.setParams(true);

		disableGetParams = false; 

	}

	/**
	 * Get parameters from the control fields. 
	 */
	public void getParams() {


		if (disableGetParams) return; 

		//		if (clickPlotInfo.getClickDisplayParams().minClickLength>=clickPlotInfo.getClickDisplayParams().maxClickLength) {
		//			minMaxWidthPane.getSpinner2().getValueFactory().setValue((double) (clickPlotInfo.getClickDisplayParams().maxClickLength+1)); 
		//		}
		//		
		//		if (clickPlotInfo.getClickDisplayParams().minClickHeight>=clickPlotInfo.getClickDisplayParams().maxClickHeight) {
		//			minMaxWidthPane.getSpinner2().getValueFactory().setValue((double) (clickPlotInfo.getClickDisplayParams().maxClickHeight+1)); 
		//		}

		/***Click sizes***/
		clickPlotInfo.getClickDisplayParams().minClickLength = minMaxWidthPane.getValue().intValue(); 
		clickPlotInfo.getClickDisplayParams().maxClickLength = minMaxWidthPane.getValue2().intValue(); 
		clickPlotInfo.getClickDisplayParams().minClickHeight = minMaxHeightPane.getValue().intValue(); 
		clickPlotInfo.getClickDisplayParams().maxClickHeight = minMaxHeightPane.getValue2().intValue(); 


		/*** Frequency options****/
		//the slider
		clickPlotInfo.getClickDisplayParams().fftCutOf=(slider.maxProperty().get()-slider.getValue())/100.;

		//		clickPlotInfo.getClickDisplayParams().freqAmplitudeLimits = 
		if (clickPlotInfo.getClickDisplayParams().freqAmplitudeRange==null) {
			clickPlotInfo.getClickDisplayParams().freqAmplitudeRange = new double[2]; 
		}
		clickPlotInfo.getClickDisplayParams().freqAmplitudeRange[0] = spectroControlPane.getLowValue(); 
		clickPlotInfo.getClickDisplayParams().freqAmplitudeRange[1] = spectroControlPane.getHighValue(); 

		if (clickPlotInfo.getClickDisplayParams().freqAmplitudeLimits==null) {
			clickPlotInfo.getClickDisplayParams().freqAmplitudeLimits = new double[2]; 
		}
		clickPlotInfo.getClickDisplayParams().freqAmplitudeLimits[0] = spectroControlPane.getColourSlider().getMin(); 
		clickPlotInfo.getClickDisplayParams().freqAmplitudeLimits[1] = spectroControlPane.getColourSlider().getMax(); 

		
		clickPlotInfo.getClickDisplayParams().thresholdFFT = !freqSwitch.isSelected();
		
		clickPlotInfo.getClickDisplayParams().fftHop = this.fftSpinnerHop.getValue().intValue();
		clickPlotInfo.getClickDisplayParams().fftLength = this.fftSpinnerLength.getValue().intValue(); 
		clickPlotInfo.getClickDisplayParams().colourMap = this.spectroControlPane.getColourArrayType(); 
		clickPlotInfo.getClickFFTPlotManager().update(); 
		/*** Data select pane****/

		//dynamic settings pane so have to repaint whenever a control is selected. 
		dataSelectPane.getParams(null);

		int chanGroup; 
		if (channelChoiceBox.getSelectionModel().getSelectedIndex()==0) chanGroup=0; 
		else chanGroup=clickPlotInfo.getClickControl().getClickParameters().
				getChannelBitmap(channelChoiceBox.getSelectionModel().getSelectedIndex()-2); //take away 2 because of separator. 

		//			System.out.println("The channel bitmap is: "+ chanGroup + " selected index: " + channelChoiceBox.getSelectionModel().getSelectedIndex()); 
		//			for (int i=0; i< clickPlotInfo.getClickControl().getClickParameters().getChannelGroups().length; i++) {
		//				System.out.println("Chanmap: Group " + i + " bitmap: "+clickPlotInfo.getClickControl().getClickParameters().
		//				getChannelBitmap(i));
		//			}

		clickPlotInfo.getClickDisplayParams().displayChannels=chanGroup; 



	}

	/**
	 * Called whenever symbols changed to set the correct drawing time for the frequency axis in the 
	 * parameters class. 
	 */
	private void setFreqType() {
		int colourChoice = ((ClickSymbolOptions) clickPlotInfo.getClickSymbolChooser().getSymbolChooser().getSymbolOptions()).colourChoice;
		this.clickPlotInfo.getClickDisplayParams().thresholdFFT = colourChoice != 5; 

	}

	@Override
	public ImageView getHidingIcon() {
		return new ImageView(clickIcon);
	}

	@Override
	public String getShowingName() {
		return clickPlotInfo.getShortName();
	}

	@Override
	public ImageView getShowingIcon() {
		return null;
	}

	@Override
	public Pane getPane() {
		return this;
	}

	/**
	 * Notify the click pane of an update that may change controls 
	 */
	public void notifyUpdate() {
		
		
		//called whenever the y axis data type changes.
		//17/09/2021- this was causing the spectrogram to reset back to normal lines every time 
		//a module dialog was closed. 
		//setFreqType();
		
		
		this.enablePane();
		//in case there has been a global medium update. 
	}

}
