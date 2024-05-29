package detectionPlotFX.plots;

import java.util.Arrays;

import PamguardMVC.PamDataUnit;
import Spectrogram.WindowFunction;
import dataPlotsFX.spectrogramPlotFX.SpectrogramControlPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilsFX.ColourArray;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;
import pamViewFX.fxNodes.comboBox.ColorComboBox;

/**
 * The settings pane for the FFT plot. 
 * @author Jamie Macaulay 
 *
 * @param <D>
 */
public class FFTSettingsPane<D extends PamDataUnit> extends DynamicSettingsPane<FFTPlotParams> {
	
	/**
	 * The spectrogram settings pane. 
	 */
	private SpectrogramControlPane spectroControlPane;

	/**
	 * Dynamically change the colours. 
	 */
	private boolean dynamicColourChanging = true;

	/**
	 * Reference to the FFTPlot
	 */
	private FFTPlot fftPlot;

	/**
	 * Allow dynamic updates- i.e. updating as controls are changing. 
	 */
	private boolean allowDynamicUpdate = true; 

	/**
	 * Parameters for the FFT pane. 
	 */
	private FFTPlotParams fftPlotParams; 

	/**
	 * The main holder pane. 
	 */
	private PamBorderPane mainPane = new PamBorderPane();

	/**
	 * The main holder for controls. 
	 */
	private PamHBox holder;

	/**
	 * The FFT spinner control
	 */
	private PamSpinner<Integer> fftLengthSpinner;

	/**
	 * FFT hop and window loength control. 
	 */
	private PamSpinner<Integer>  fftHopSpinner;


	/**
	 * The time buffer control. 
	 */
	private PamSpinner<Double> timeBufferSpinner;

	/**
	 * The window
	 */
	private ChoiceBox<String> windowBox;

	private Pane fftPane;


	/**
	 * The FFT settings pane. 
	 */
	public FFTSettingsPane(Object owner, FFTPlot fftPlot) {
		super(owner);
		this.fftPlot=fftPlot; 
		createFFTPlotPane();
		this.setAllowNotify(true);
	}

	/**
	 * Create the plot pane. 
	 */
	private void createFFTPlotPane() {

		this.spectroControlPane= new SpectrogramControlPane(Orientation.VERTICAL, true, false, true);

		spectroControlPane.getColorBox().valueProperty().addListener((ov,  t,  t1) -> {                
			newSettings();
		});

		spectroControlPane.getColourSlider().showTickLabelsProperty().setValue(true);
		spectroControlPane.getColourSlider().showTickMarksProperty().setValue(true);
		spectroControlPane.getColourSlider().minProperty().setValue(0);
		spectroControlPane.getColourSlider().maxProperty().setValue(1);

		spectroControlPane.getColourSlider().highValueProperty().addListener((ov,  t,  t1) -> {                
			if (dynamicColourChanging) newSettings();
		});

		spectroControlPane.getColourSlider().highValueChangingProperty().addListener((ov,  t,  t1) -> {  
			//only change when slider stops moving
			if (!dynamicColourChanging && t!=t1 && !t1.booleanValue() && notLastColourValues()) newSettings();
		});

		spectroControlPane.getColourSlider().lowValueProperty().addListener((ov,  t,  t1) -> {  
			if (dynamicColourChanging)  newSettings();
		});

		spectroControlPane.getColourSlider().lowValueChangingProperty().addListener((ov,  t,  t1) -> {  
			//only change when slider stops moving
			if (!dynamicColourChanging && t!=t1 && !t1.booleanValue() && notLastColourValues()) newSettings();
		});
		

		//make the colour box smaller so we can fit more stuff in. 
		spectroControlPane.getColorBox().setPrefWidth(50);
		spectroControlPane.setPrefWidth(75);
		
		holder = new PamHBox(); 
		holder.setSpacing(5);
		holder.setPadding(new Insets(5,5,5,10));

		holder.getChildren().addAll(spectroControlPane,  fftPane = createFFTSettingsPane() ); 

		mainPane.setCenter(holder);
	}
	
	/**
	 * 
	 * @param enable - true to enable. 
	 */
	public void enableTimeBufferSpinner(boolean enable) {
		if (enable && !fftPane.getChildren().contains(timeBufferSpinner)) {
			fftPane.getChildren().add(timeBufferSpinner);
		}
		else {
			fftPane.getChildren().remove(timeBufferSpinner);
		}
	}

	/**
	 * True if the colour values have changed. Stops the colour values from repainting twice. 
	 * @return true of the colour limits have changed. 
	 */
	private boolean notLastColourValues() {
		if (spectroControlPane.getColourSlider().getHighValue()==this.fftPlotParams.freqAmplitudeRange[1] && 
				spectroControlPane.getColourSlider().getLowValue()==this.fftPlotParams.freqAmplitudeRange[0]) return false; 
		else return true;
	}

	/**
	 * The pane which controls FFT length, hop and window size
	 * @return the FFT settings pane.
	 */
	protected Pane createFFTSettingsPane() {

		//buffer
		Label timeBufferLabel = new Label("Time Buffer");
		timeBufferSpinner = new PamSpinner<Double>(0, 500, 1, 0.2); 
		timeBufferSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		timeBufferSpinner.setPrefWidth(100);
		timeBufferSpinner.setEditable(true);
		timeBufferSpinner.valueProperty().addListener((obsval, oldVal, newVal)->{
			newSettings(); 
		});
		
		//slider for changing the FFT length. 
		Label fftSpinnerLabel = new Label("FFT Length");
		ObservableList<Integer> stepSizeListLength=FXCollections.observableArrayList();
		for (int i=2; i<15; i++){
			stepSizeListLength.add((int) Math.pow(2,i));
		}
		fftLengthSpinner=new PamSpinner<Integer>(stepSizeListLength);
		fftLengthSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);		
		fftLengthSpinner.setPrefWidth(100);
		fftLengthSpinner.valueProperty().addListener((obsval, oldVal, newVal)->{
			newSettings(); 
		});
		
		
		//Slider for changing hop size
		Label windowLengthLabel = new Label("Window Length"); 
//		//button for optimising the spectrogram automatically //TODO
//		PamButton pamButton = new PamButton();
////		pamButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.ADJUST, PamGuiManagerFX.iconSize));
//		pamButton.setGraphic(PamGlyphDude.createPamIcon("mdi2a-adjust", PamGuiManagerFX.iconSize));
//		pamButton.setTooltip(new Tooltip("Optimise the window length based on the average frequency slope of the signal"));
//		//TEMP
//		pamButton.setOnAction((action)->{
//			fftPlot.reloadImage=true;
//		});
//		//TEMP
		
//		PamHBox windowLengthHBox= new PamHBox(windowLengthLabel, pamButton); 
		PamHBox windowLengthHBox= new PamHBox(windowLengthLabel); 
		windowLengthHBox.setAlignment(Pos.CENTER_LEFT);
		windowLengthHBox.setSpacing(5);
		PamHBox.setHgrow(windowLengthHBox, Priority.ALWAYS);

		fftHopSpinner=new PamSpinner<Integer>(stepSizeListLength);
		fftHopSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);		
		fftHopSpinner.setPrefWidth(100);
		fftHopSpinner.valueProperty().addListener((obsval, oldVal, newVal)->{
			newSettings(); 
		});
		
		Label windowTypeLabel = new Label("Window Type"); 
		windowBox  = new ChoiceBox<String>(); 
		windowBox.getItems().addAll(Arrays.asList(WindowFunction.getNames())); 
		windowBox.setOnAction((action)->{
			newSettings(); 
		});

		PamVBox pamVBox = new PamVBox(timeBufferLabel, timeBufferSpinner,
				fftSpinnerLabel, fftLengthSpinner, windowLengthHBox, fftHopSpinner, 
				windowTypeLabel, windowBox); 	
		
		pamVBox.setSpacing(7);
		
		return pamVBox; 
	}

	/**
	 * Get the FFTLength Spinner. 
	 * @return - the FFT spinner.
	 */
	public PamSpinner<Integer> getFftLengthSpinner() {
		return fftLengthSpinner;
	}

	/**
	 * Check new settings. 
	 */
	protected void newSettings() {
		super.notifySettingsListeners();
	}

	/**
	 * Get the params from the current settings of the controls. 
	 * @param wignerParams - the params to set. 
	 * @return the new FFT parameters
	 */
	@Override
	public FFTPlotParams getParams(FFTPlotParams fftPlotParams) {
		
		fftPlotParams.setColourMap(ColourArray.getColourArrayType(spectroControlPane.getColorBox().getValue()));
		fftPlotParams.freqAmplitudeRange[1]=spectroControlPane.getColourSlider().getHighValue();
		fftPlotParams.freqAmplitudeRange[0]=spectroControlPane.getColourSlider().getLowValue();
		
		if (fftPlotParams.freqAmplitudeRange[0] > fftPlotParams.freqAmplitudeRange[1]) {
			fftPlotParams.freqAmplitudeRange[1] = fftPlotParams.freqAmplitudeRange[0]+5; 
		}
		
		//time buffer
		fftPlotParams.detPadding=(long) (timeBufferSpinner.getValue()*1000);
		
		//get the FFT params
		fftPlotParams.fftLength = fftLengthSpinner.getValue(); 
		fftPlotParams.fftHop = fftHopSpinner.getValue();
		fftPlotParams.fftHop=Math.max(fftPlotParams.fftHop, 4); //hop should not be less than 4;  
		

		fftPlotParams.windowFunction=this.windowBox.getSelectionModel().getSelectedIndex(); 
		
		return fftPlotParams; 
	}


	@Override
	public void setParams(FFTPlotParams input) {
		this.fftPlotParams=input.clone();
		

		allowDynamicUpdate=false; //set so that controls aren't recalling stuff. 

		spectroControlPane.getColorBox().setValue(ColourArray.getName(fftPlotParams.colorArray));
		
		
		if (fftPlotParams.freqAmplitudeRange[0] > fftPlotParams.freqAmplitudeRange[1]) {
			fftPlotParams.freqAmplitudeRange[1] = fftPlotParams.freqAmplitudeRange[0]+5; 
		}
		

//		System.out.println("Color bar: " + fftPlotParams.minColourValue+ "  " +fftPlotParams.lowerColourValue + 
//				"  " + fftPlotParams.upperColourValue+ "  " +fftPlotParams.lowerColourValue);

		//these lines must be in this order or else the colour slider is totally messed up
		/****/
		spectroControlPane.getColourSlider().setMax(fftPlotParams.freqAmplitudeLimits[1]);
		spectroControlPane.getColourSlider().setMin(fftPlotParams.freqAmplitudeLimits[0]);
		
		spectroControlPane.getColourSlider().setHighValue(fftPlotParams.freqAmplitudeRange[1]);
		spectroControlPane.getColourSlider().setLowValue(fftPlotParams.freqAmplitudeRange[0]);
		/***/
		
		//time buffer
		this.timeBufferSpinner.getValueFactory().setValue(fftPlotParams.detPadding/1000.);
		
		//set FFT params
		this.fftLengthSpinner.getValueFactory().setValue(fftPlotParams.fftLength); 
		this.fftHopSpinner.getValueFactory().setValue(fftPlotParams.fftHop); 
		
		windowBox.getSelectionModel().select(fftPlotParams.windowFunction);

		allowDynamicUpdate=true;
	}

	@Override
	public String getName() {
		return "FFT Settings Pane";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stuff
	}
	
	/**
	 * This is the main pane which holds the spectrogram colour and FFT settings. 
	 * @return the VBox holder.
	 */
	public PamHBox getHolderPane() {
		return holder; 
	}
	
	
	/**
	 * Get the Pane which holds the FFT Settings e.. length hop etc. 
	 * @return the pane with controls for FFT settings.
	 */
	public Pane getFFTPane() {
		return fftPane; 
	}

	/**
	 * Get the colour combo box which allows users to change the gradient colour
	 * of the spectrogram. 
	 * @return the colour combo box. 
	 */
	public ColorComboBox getColorBox() {
		return this.spectroControlPane.getColorBox();
	}
	
	/**
	 * Get the pane with basic controls for the spectrogram. 
	 * @return the spectrogram control pane. 
	 */
	public SpectrogramControlPane getSpectroControlPane() {
		return this.spectroControlPane;
	}
	

}
