package dataPlotsFX.rawClipDataPlot;

import PamController.PamController;
import PamView.symbol.PamSymbolManager;
import dataPlotsFX.layout.TDSettingsPane;
import dataPlotsFX.spectrogramPlotFX.SpectrogramControlPane;
import fftManager.layoutFX.FFTPaneFX;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;
import pamViewFX.symbol.FXSymbolOptionsPane;
import pamViewFX.symbol.StandardSymbolOptionsPane;

/**
 * The clip plot pane.
 * 
 * @author Jamie Macaulay 
 *
 */
public class RawClipSettingsPane extends PamBorderPane implements TDSettingsPane {

	private static final double PREF_WIDTH = 300;

	/*
	 * The raw clip info. 
	 */
	private RawClipDataInfo rawClipDataInfo;

	/**
	 * The icon for the pane. 
	 */
	private Node icon = new Canvas(20,20); 

	private SpectrogramControlPane spectroControlPane;

	private Spinner<Integer> fftSpinnerHop;

	private PamSpinner<Integer> fftSpinnerLength;

	private PamVBox holder;

	private boolean disableGetParams;

	private String showingName = "Clip Settings";

	private FXSymbolOptionsPane<?> symbolOptionsPane;

	/**
	 * Toggle switch for showing a box or a spectrogram. 
	 */
	private PamToggleSwitch spectroToggle; 


	/**
	 * The clip plot pane. 
	 */
	public RawClipSettingsPane(RawClipDataInfo rawClipDataInfo){
		this.rawClipDataInfo = rawClipDataInfo; 
		createPane();
		this.setPrefWidth(PREF_WIDTH);
		setParams(); 

	}

	public void createPane() {
		holder = new PamVBox(); 
		holder.setSpacing(5);
		holder.getChildren().addAll( createSymbolOptionsPane().getContentNode(), createFreqColorPane());
		this.setPadding(new Insets(5,10,5,10)); 

		this.setCenter(holder);
	}


//	/**
//	 * Set the colour of the icon which sits on hiding tab pane. Recolours so similar to spectrogram colour array
//	 */
//	private void setIconColour(){
//		setIconColour( getColourArrayType());
//	}

	/**
	 * Get the current colour array type. 
	 * @return
	 */
	private ColourArrayType getColourArrayType() {
		return this.rawClipDataInfo.getRawClipParams().colourMap;
	}


//	/**
//	 * Set the colour of the icon which sits on hiding tab pane. Recolours so similar to spectrogram colour array
//	 */
//	private void setIconColour(ColourArrayType colourArrayType){
//		icon.getGraphicsContext2D().setFill(ColourArray.getLinerGradient(Orientation.VERTICAL, icon.getHeight(), colourArrayType));
//		icon.getGraphicsContext2D().fillRect(0, 0, 20, 20);
//		//differentiate from spectrogram. 
//		icon.getGraphicsContext2D().setStroke(Color.WHITE);
//		icon.getGraphicsContext2D().strokeRect(2, 2, 16, 16);
//	}


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


		PamGridPane pamGridPane=new PamGridPane();
		pamGridPane.setHgap(5);
		pamGridPane.setVgap(5);

		//FFT length
		pamGridPane.add(new Label("FFT Length"), 0, 0);

		fftSpinnerLength=new PamSpinner<Integer>(FFTPaneFX.createStepList());
		fftSpinnerLength.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		fftSpinnerLength.getValueFactory().valueProperty().addListener((obs, before, after)->{
			if (after==0) fftSpinnerLength.getValueFactory().setValue(before==0 ? 128 : before);
			newSettings(100);
		});
		pamGridPane.add(fftSpinnerLength, 1, 0);

		//FFT Hop
		pamGridPane.add(new Label("FFT Hop"), 0, 1);

		fftSpinnerHop=new PamSpinner<Integer>(4,(int) Math.pow(2,24),512,32);
		fftSpinnerHop.setEditable(true);
		fftSpinnerHop.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		fftSpinnerHop.getValueFactory().valueProperty().addListener((obs, before, after)->{
			if (after==0) fftSpinnerHop.getValueFactory().setValue(before==0 ? 64 : before);
			newSettings(100);
		});

		pamGridPane.add(fftSpinnerHop, 1, 1);
		

		this.spectroToggle = new PamToggleSwitch("Show spectrogram"); 
		spectroToggle.selectedProperty().addListener((obsVal, oldVal, newVal)->{
			enableSpectroPane();
			newSettings();
		});

		Label freqTitle = new Label("Freq. colour options");
		PamGuiManagerFX.titleFont2style(freqTitle);
//		freqTitle.setFont(PamGuiManagerFX.titleFontSize2);

		pamVBox.getChildren().addAll(
				freqTitle, spectroToggle, spectroControlPane, pamGridPane); 

		return pamVBox; 
	}

	public void enableSpectroPane(){
		spectroControlPane.setDisable(!spectroToggle.isSelected());
		fftSpinnerLength.setDisable(!spectroToggle.isSelected());
		fftSpinnerHop.setDisable(!spectroToggle.isSelected());
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
		getParams();

		//on a parameter change must clear the FFT plot. 
		this.rawClipDataInfo.getFFTplotManager().clear(); 
		this.rawClipDataInfo.getRawWavePlotManager().clear();

		this.rawClipDataInfo.getTDGraph().repaint(milliswait);
	}


	public void setParams() {

		disableGetParams = true; 

		this.spectroControlPane.setAmplitudeRange(rawClipDataInfo.getRawClipParams().freqAmplitudeRange, 
				rawClipDataInfo.getRawClipParams().freqAmplitudeLimits);

		this.fftSpinnerHop.getValueFactory().setValue(rawClipDataInfo.getRawClipParams().fftHop);
		this.fftSpinnerLength.getValueFactory().setValue(rawClipDataInfo.getRawClipParams().fftLength);

		if (rawClipDataInfo.getRawClipParams().colourMap== null) {
			rawClipDataInfo.getRawClipParams().colourMap = ColourArrayType.HOT; 
		}
		this.spectroControlPane.setColourArrayType(rawClipDataInfo.getRawClipParams().colourMap);

		if (rawClipDataInfo.getRawClipParams().freqAmplitudeRange==null) {
			//can happen with old .psfx save files. 
			rawClipDataInfo.getRawClipParams().freqAmplitudeRange = PamController.getInstance().getGlobalMediumManager().getDefaultdBHzScales(); 
		}; 

		if (rawClipDataInfo.getRawClipParams().freqAmplitudeLimits==null) {
			//can happen with old .psfx save files. 
			rawClipDataInfo.getRawClipParams().freqAmplitudeLimits = PamController.getInstance().getGlobalMediumManager().getDefaultdBHzScales(); 
		}
		
		spectroToggle.setSelected(rawClipDataInfo.getRawClipParams().showSpectrogram);
		enableSpectroPane();
		 
		disableGetParams = false; 

	}


	/**
	 * Get parameters. 
	 */
	private void getParams() {

		if (!disableGetParams) {

			if (rawClipDataInfo.getRawClipParams().freqAmplitudeRange==null) {
				rawClipDataInfo.getRawClipParams().freqAmplitudeRange = new double[2]; 
			}
			rawClipDataInfo.getRawClipParams().freqAmplitudeRange[0] = spectroControlPane.getLowValue(); 
			rawClipDataInfo.getRawClipParams().freqAmplitudeRange[1] = spectroControlPane.getHighValue(); 

			rawClipDataInfo.getRawClipParams().fftHop = this.fftSpinnerHop.getValue().intValue();
			rawClipDataInfo.getRawClipParams().fftLength = this.fftSpinnerLength.getValue().intValue(); 
			rawClipDataInfo.getRawClipParams().colourMap = this.spectroControlPane.getColourArrayType(); 
			rawClipDataInfo.getFFTplotManager().update(); 
			
			rawClipDataInfo.getRawClipParams().showSpectrogram = spectroToggle.isSelected(); 
			
			spectroToggle.setSelected(rawClipDataInfo.getRawClipParams().showSpectrogram);

		}

		rawClipDataInfo.settingsUpdate(); 
	}
	
	/**
	 * Create the symbol options pane. 
	 * @return the symbol options pane. 
	 */
	private StandardSymbolOptionsPane createSymbolOptionsPane(){

		PamSymbolManager<?> pamSymbolManager=  rawClipDataInfo.getDataBlock().getPamSymbolManager();

		symbolOptionsPane= pamSymbolManager.getFXOptionsPane(rawClipDataInfo.getTDGraph().getUniqueName(), 
				rawClipDataInfo.getTDGraph().getGraphProjector()); 

		//create a new settings listener
		symbolOptionsPane.addSettingsListener(()->{
			newSettings();
		});
		
		//		symbolOptionsPane.getLinBox().prefWidthProperty().bind(this.widthProperty());
		//symbolOptionsPane.getLinBox().add(symbolOptionsPane.getLineColorPicker(), 1, 1);

		return (StandardSymbolOptionsPane) symbolOptionsPane;
	}


	@Override
	public Node getHidingIcon() {
		return icon;
	}

	@Override
	public String getShowingName() {
		return showingName;
	}
	
	/**
	 * Set the name on the pane
	 * @param showingName - the name that will show in the pane. 
	 */
	public void setShowingName(String showingName) {
		this.showingName= showingName; 
	}

	@Override
	public Node getShowingIcon() {
		return null;
	}
	
	public void setIcon(Node showingIcon) {
		this.icon= showingIcon; 
	}

	@Override
	public Pane getPane() {
		return this;
	}

}
