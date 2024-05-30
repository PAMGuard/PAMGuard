package pamViewFX.fxNodes.utilityPanes;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;
import Filters.FilterBand;
import PamController.SettingsPane;
import fftFilter.FFTFilterParams;


/**
 * A much simpler filter pane with just few options
 * @author Jamie Macualay
 *
 */
public class SimpleFilterPaneFX extends DynamicSettingsPane<FFTFilterParams>{
	

	/**
	 * Temporary clone of FFTFilterParams
	 */
	private FFTFilterParams fftFilterParams;

	//private JComboBox filterBands;
	private RadioButton highPass;

	private RadioButton bandPass;

	private RadioButton lowPass;

	private RadioButton bandStop;
	
	/**
	 * The layout  orientation of the pane.
	 */
	private Orientation orientation=Orientation.HORIZONTAL;

	/**
	 * Pane which holds frequency spinners. 
	 */
	private FreqBandPane freqPane; 

	private PamBorderPane mainPane = new PamBorderPane();

	/**
	 * Create a simple filter pane. 
	 */
	public SimpleFilterPaneFX() {
		super(null);
		createFilterPane();
	}
	
	/**
	 * Create a simple filter pane.
	 * @param orientation - orientation of the pane. This changes the layout to be vertical or horizontal. 
	 */
	public SimpleFilterPaneFX(Orientation orientation) {
		super(null);
		this.orientation=orientation; 
		createFilterPane();
	}
	
	
	/**
	 * Create the filter pane. 
	 */
	private void createFilterPane(){
		
		//Filter selected pane
		PamVBox filterType=new PamVBox(); 
		filterType.setSpacing(5);
		
		filterType.getChildren().add(highPass = new RadioButton("High Pass"));
		filterType.getChildren().add(lowPass = new RadioButton("Low Pass"));
		filterType.getChildren().add(bandPass = new RadioButton("Band Pass"));
		filterType.getChildren().add(bandStop = new RadioButton("Band Stop"));
		
		ToggleGroup buttonGroup = new ToggleGroup();
		buttonGroup.getToggles().add(highPass);
		buttonGroup.getToggles().add(lowPass);
		buttonGroup.getToggles().add(bandPass);
		buttonGroup.getToggles().add(bandStop);
		
		highPass.setOnAction((action)->{
			enableControls();
			notifyFilterChange();
		});
		
		lowPass.setOnAction((action)->{
			enableControls();
			notifyFilterChange();
		});
		bandPass.setOnAction((action)->{
			enableControls();
			notifyFilterChange();
		});
		bandStop.setOnAction((action)->{
			enableControls();
			notifyFilterChange();
		});
		
		// frequency pane
		 freqPane=new FreqBandPane();
		 
		 freqPane.getLowPassFreq().valueProperty().addListener((obs, oldVal, newVal)->{
				notifyFilterChange();
		 });
		 
		 freqPane.getHighPassFreq().valueProperty().addListener((obs, oldVal, newVal)->{
				notifyFilterChange();
		 });
		
//		highPassFreq=new PamSpinner<Double>(10.,500000.,2000.,2000.);
//		highPassFreq.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
//		highPassFreq.getValueFactory().valueProperty().addListener((obs, before, after)->{
//			if (after>=lowPassFreq.getValue()) highPassFreq.getValueFactory().setValue(Math.max(10,lowPassFreq.getValue()-100)); 
//			if (after>sampleRate/2.) highPassFreq.getValueFactory().setValue(sampleRate/2.); 
//			if (fftFilterParams!=null) fftFilterParams.highPassFreq=highPassFreq.getValue().floatValue();
//		});
//		highPassFreq.setEditable(true);
//		//highCut.setPrefColumnCount(6);
//		freqPane.add(new Label("High Pass"), 0, 0);
//		freqPane.add(highPassFreq, 1, 0);
//		freqPane.add(new Label("Hz"), 2, 0);
//
//		lowPassFreq=new PamSpinner<Double>(10.,500000.,2000.,2000.);
//		lowPassFreq.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
//		lowPassFreq.getValueFactory().valueProperty().addListener((obs, before, after)->{
//			if (after<=highPassFreq.getValue()) lowPassFreq.getValueFactory().setValue(Math.min(sampleRate/2.,highPassFreq.getValue()+100));
//			if (after>sampleRate/2.) lowPassFreq.getValueFactory().setValue(sampleRate/2.); 
//			if (fftFilterParams!=null)  fftFilterParams.lowPassFreq=lowPassFreq.getValue().floatValue();
//			enableControls();
//		});
//		lowPassFreq.setEditable(true);
//		//lowCut.setPrefColumnCount(6);
//		freqPane.add(new Label("Low Pass"), 0, 1);
//		freqPane.add(lowPassFreq, 1, 1);
//		freqPane.add(new Label("Hz"), 2, 1);
		 
		PamBorderPane.setAlignment(freqPane, Pos.CENTER);
		freqPane.setAlignment(Pos.CENTER_LEFT);


		mainPane.setLeft(filterType); 
		if (orientation==Orientation.HORIZONTAL){
			mainPane.setCenter(freqPane);
			freqPane.setPadding(new Insets(5,5,5,15));

		}
		else{
			mainPane.setBottom(freqPane);
		}
	}

	/**
	 * Called whenever any control on the filter pane is changed. 
	 */
	private void notifyFilterChange() {
		super.notifySettingsListeners();
	}

	@Override
	public  void setParams(FFTFilterParams input) {
		//set params
		fftFilterParams=input.clone();
		//sampelrate 
		
		highPass.setSelected(fftFilterParams.filterBand == FilterBand.HIGHPASS);
		lowPass.setSelected(fftFilterParams.filterBand == FilterBand.LOWPASS);
		bandPass.setSelected(fftFilterParams.filterBand == FilterBand.BANDPASS);
		bandStop.setSelected(fftFilterParams.filterBand == FilterBand.BANDSTOP);
		
		freqPane.getHighPassFreq().getValueFactory().setValue(fftFilterParams.highPassFreq);
		freqPane.getLowPassFreq().getValueFactory().setValue(fftFilterParams.lowPassFreq);
		enableControls();
	}

	@Override
	public FFTFilterParams getParams(FFTFilterParams fftFilterParams) {
		if (fftFilterParams==null) fftFilterParams=new FFTFilterParams();
		fftFilterParams.filterBand = getBand();
		try {
			//if (fftFilterParams.filterBand != FilterBand.HIGHPASS) {
				fftFilterParams.lowPassFreq = freqPane.getLowPassFreq().getValue();
			//}
			//if (fftFilterParams.filterBand != FilterBand.LOWPASS) {
				fftFilterParams.highPassFreq =freqPane.getHighPassFreq().getValue(); 
			//}
		}
		catch (NumberFormatException e) {
			 PamDialogFX.showWarning(null, "Filter Settings Error", "Invalid frequency parameter");
			 return null; 
		}
		return fftFilterParams;
	}


	private FilterBand getBand() {
		if (highPass.isSelected()) return FilterBand.HIGHPASS;
		if (lowPass.isSelected()) return FilterBand.LOWPASS;
		if (bandPass.isSelected()) return FilterBand.BANDPASS;
		if (bandStop.isSelected()) return FilterBand.BANDSTOP;
		return null;
	}
	
	private void enableControls() {
		FilterBand b = getBand();
		freqPane.getHighPassFreq().setDisable(b == FilterBand.LOWPASS);
		freqPane.getLowPassFreq().setDisable(b == FilterBand.HIGHPASS);
	}


	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Filter Settings";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Disable or enable the filter pane. 
	 * @param disable - true to disable the pane
	 */
	public void setDisableFilterPane(boolean disable){
		mainPane.setDisable(disable);
		
//		highPass.setDisable(disable);
//		lowPass.setDisable(disable);
//		bandPass.setDisable(disable);
//		bandStop.setDisable(disable);
//		freqPane.setDisableFreqPane(disable);
	}
	
	public double getSampleRate() {
		return freqPane.getSampleRate();
	}

	public void setSampleRate(double sampleRate) {
		freqPane.setSampleRate(sampleRate);
	}

	public void addValueChangeListener(Object object) {
		// TODO Auto-generated method stub
		
	}

}
