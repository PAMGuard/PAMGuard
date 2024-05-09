package detectionPlotFX.whistleDDPlot;

import detectionPlotFX.plots.RawFFTPlot;
import detectionPlotFX.plots.FFTPlotParams;
import detectionPlotFX.plots.FFTSettingsPane;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import whistlesAndMoans.ConnectedRegionDataUnit;

/**
 * Plot which shows FFT settings controls and a colour box to change whistle colour. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class WhistleSettingsPane extends FFTSettingsPane<ConnectedRegionDataUnit> {
	
	//TODO - need to complete this class; 
	
	/**
	 * Allows fragments colours. 
	 */
	private ColorPicker colorPicker;
	

	public WhistleSettingsPane(Object owner, RawFFTPlot<?> fftPlot) {
		super(owner, fftPlot);
		//add whistle fragment to bottom; 
		super.getFFTPane().getChildren().add(createWhistlePane()); 
	}
	
	
	/**
	 * Create the whistle pane
	 * @return the whistle pane. 
	 */
	private Pane createWhistlePane(){
		
//		//make the colour box smaller so we can fit more stuff in. 
//		super.getColorBox().setPrefWidth(50);
//		super.getSpectroControlPane().setPrefWidth(75);

		//colour picker
		Label contourColourLabel = new Label("Contour Colour");
		colorPicker = new ColorPicker(); 
		
		colorPicker.valueProperty().addListener((obsval, oldVal, newVal)->{
			newSettings(); 
		});
		
//		//buffer
//		Label timeBufferLabel = new Label("Time Buffer");
//		timeBuffer = new PamSpinner<Double>(0, 500, 1, 0.2); 
//		timeBuffer.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
//		
//		//slider for changing the FFT length. 
//		Label fftSpinnerLabel = new Label("FFT Length");
//		ObservableList<Integer> stepSizeListLength=FXCollections.observableArrayList();
//		for (int i=2; i<15; i++){
//			stepSizeListLength.add((int) Math.pow(2,i));
//		}
//		fftSpinnerLength=new PamSpinner<Integer>(stepSizeListLength);
//		fftSpinnerLength.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);		
//		fftSpinnerLength.setEditable(true);
//		
//		//Slider for changing hop size
//		Label windowLengthLabel = new Label("Window Length"); 
//		PamButton pamButton = new PamButton();
////		pamButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.ADJUST, PamGuiManagerFX.iconSize));
//		pamButton.setGraphic(PamGlyphDude.createPamIcon("mdi2a-adjust", PamGuiManagerFX.iconSize));
//		pamButton.setTooltip(new Tooltip("Optimise the window length based on the average frequency slope of the signal"));
//		
//		PamHBox windowLengthHBox= new PamHBox(windowLengthLabel, pamButton); 
//		windowLengthHBox.setAlignment(Pos.CENTER_LEFT);
//		windowLengthHBox.setSpacing(5);
//
//		Slider windowSizeSlider= new Slider(); 
//		windowSizeSlider.setMax(8192);
//		windowSizeSlider.setMin(8);
//		
//		windowSizeSlider.setShowTickLabels(true);
//		windowSizeSlider.setShowTickMarks(true);
//		windowSizeSlider.setMajorTickUnit(1024);
//		windowSizeSlider.setMinorTickCount(0); //disable minor tick marks 


		PamVBox pamVBox = new PamVBox(contourColourLabel, colorPicker); 		
		pamVBox.setSpacing(7);
		
		return pamVBox;
	}
	
	
	/**
	 * Get the params from the current settings of the controls. 
	 * @param wignerParams - the params to set. 
	 * @return the new FFT parameters
	 */
	@Override
	public FFTPlotParams getParams(FFTPlotParams fftPlotParams) {
		((WhistlePlotParams) fftPlotParams).contourColor = this.colorPicker.getValue();
		return super.getParams(fftPlotParams); 
		
	}


	@Override
	public void setParams(FFTPlotParams input) {
		this.colorPicker.setValue(((WhistlePlotParams) input).contourColor);
		super.setParams(input);
		
	}

}
