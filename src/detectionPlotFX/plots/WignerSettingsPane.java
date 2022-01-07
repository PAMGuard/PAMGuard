package detectionPlotFX.plots;

import PamController.SettingsPane;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.spectrogramPlotFX.SpectrogramControlPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilsFX.ColourArray;

public class WignerSettingsPane<D extends PamDataUnit> extends SettingsPane<WignerPlotParams> {

	/**
	 * The spectrogram settings pane. Changes the colour of the plot. 
	 */
	private SpectrogramControlPane spectroSettings;
	
	/**
	 * Reference to the wigner plot. 
	 */
	private WignerPlot<D> wignerPlot;

	/**
	 * Limit the transform around the click center. 
	 */
	private CheckBox limitTransform;

	/**
	 * Spinner to set the transform length. 
	 */
	private PamSpinner<Integer> transformSpinner;

	/**
	 * Choice box
	 */
	private ChoiceBox<Integer> channelChoice;

	/**
	 * A reference to the wigner plot params if used as a setting pane. 
	 */
	private WignerPlotParams wignerPlotParams;
	
	/**
	 * True if the plot is automatically updated on any control change;
	 */
	private boolean allowDynamicUpdate=true;

	/**
	 * True if the wigner image is rebuilt dynamically on colour changes. False means 
	 * the image is only rebuilt once colour sliders have stopped moving. 
	 */
	private boolean dynamicColourChanging = true; 
	
	private PamBorderPane mainPane = new PamBorderPane();

	public WignerSettingsPane(WignerPlot<D> wignerPlot) {
		super(null);
		this.wignerPlot=wignerPlot;
		createWignerSettingsPane();
	}
	
	/**
	 * Create the settings pane. 
	 */
	private void createWignerSettingsPane(){
		
		PamVBox holderPane=new PamVBox();
		holderPane.setSpacing(5);
		
//		Label wignerLabel=new Label("Wigner Options");
//		wignerLabel.setFont(PamGuiManagerFX.titleFontSize2);
//		holderPane.getChildren().add(wignerLabel);
		
		this.spectroSettings= new SpectrogramControlPane(Orientation.HORIZONTAL, true, false, true);

		
		spectroSettings.getColorBox().valueProperty().addListener((ov,  t,  t1) -> {                
			newSettings();
		});

		spectroSettings.getColourSlider().showTickLabelsProperty().setValue(false);
		spectroSettings.getColourSlider().showTickMarksProperty().setValue(false);
		spectroSettings.getColourSlider().minProperty().setValue(0);
		spectroSettings.getColourSlider().maxProperty().setValue(1);
		
		spectroSettings.getColourSlider().highValueProperty().addListener((ov,  t,  t1) -> {                
			if (dynamicColourChanging) newSettings();
		});
		
		spectroSettings.getColourSlider().highValueChangingProperty().addListener((ov,  t,  t1) -> {  
			//only change when slider stops moving
			if (!dynamicColourChanging && t!=t1 && !t1.booleanValue()) newSettings();
		});

		spectroSettings.getColourSlider().lowValueProperty().addListener((ov,  t,  t1) -> {  
			if (dynamicColourChanging)  newSettings();
		});
		
		spectroSettings.getColourSlider().lowValueChangingProperty().addListener((ov,  t,  t1) -> {  
			//only change when slider stops moving
			if (!dynamicColourChanging && t!=t1 && !t1.booleanValue()) newSettings();
		});
			
		//create the transform settings. 
		Label transLabel=new Label("Transform length"); 

		ObservableList<Integer> stepSizeListLength=FXCollections.observableArrayList();
		for (int i=2; i<15; i++){
			stepSizeListLength.add((int) Math.pow(2,i));
		}
		transformSpinner=new PamSpinner<Integer>(stepSizeListLength);
		transformSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		transformSpinner.setPrefWidth(100);
		transformSpinner.valueProperty().addListener((obs, oldVal, newVal)->{
			newSettings();
		});
		Label binsLabel=new Label("bins"); 
		
		PamHBox transformBinsPane=new PamHBox();
		transformBinsPane.setAlignment(Pos.CENTER_LEFT);
		transformBinsPane.setSpacing(5);
		transformBinsPane.getChildren().addAll(transLabel, transformSpinner, binsLabel);
		
		//create transform pane. 
		limitTransform=new CheckBox("Limit transform length");
		limitTransform.setTooltip(new Tooltip("Limit the transform to a specified number of bins around the waveform center"));
		limitTransform.setOnAction((action)->{
			transformBinsPane.setDisable(!limitTransform.isSelected());
			newSettings();
		});
		
		//create the CHANNEL pane
		PamHBox channelPane=new PamHBox();
		channelPane.setAlignment(Pos.CENTER_LEFT);
		channelPane.setSpacing(5);
		channelChoice=new ChoiceBox<Integer>();
		channelChoice.valueProperty().addListener((obs, oldVal, newVal)->{
			newSettings();
		});
		channelPane.getChildren().addAll(new Label("Channel"), channelChoice);
		
		holderPane.getChildren().addAll(spectroSettings, limitTransform, transformBinsPane,channelPane);
		
		//set the pane. 
		holderPane.setPadding(new Insets(30,10,0,10));
		mainPane.setCenter(holderPane);
		
	}
	
	/**
	 * Check whether the image changes dynamically with the colour slider. 
	 * @return true if the image changes dynamically with the colour slider. 
	 */
	public boolean isDynamicColourChanging() {
		return dynamicColourChanging;
	}

	/**
	 * Set whether the image changes dynamically with the colour slider. 
	 * @param dynamicColourChanging
	 */
	public void setDynamicColourChanging(boolean dynamicColourChanging) {
		this.dynamicColourChanging = dynamicColourChanging;
	}

	/**
	 * Called whenever a control is set to allow for settings updates as they happen
	 */
	public void newSettings(){
		//System.out.println("WignerSettingsPane: newSetting");
		if (allowDynamicUpdate){
			
			//check the wigner params 
			wignerPlot.checWignerRecalc(getParams(new WignerPlotParams())); 
			
			//now set the new settings. 
			wignerPlot.setWignerParameters(getParams(wignerPlot.getWignerParameters()));
			wignerPlot.reDrawLastUnit();
		}
	}

	/**
	 * Get the params from the current settings of the controls. 
	 * @param wignerParams - the params to set. 
	 * @return
	 */
	@Override
	public WignerPlotParams getParams(WignerPlotParams wignerParams) {
		
		wignerParams.setColourMap(ColourArray.getColourArrayType(spectroSettings.getColorBox().getValue()));
		wignerParams.maxColourVal=spectroSettings.getColourSlider().getHighValue();
		wignerParams.minColourVal=spectroSettings.getColourSlider().getLowValue();
			
		wignerParams.limitLength=limitTransform.isSelected();
		wignerParams.manualLength=transformSpinner.getValue();
		
			
		if (channelChoice.getValue()==null) wignerParams.chan=0;
		else wignerParams.chan=channelChoice.getValue();
				
		
		return wignerParams;
	}

	@Override
	public void setParams(WignerPlotParams input) {
		this.wignerPlotParams=input.clone();

		allowDynamicUpdate=false; //set so that controls aren't recalling stuff. 
		
		spectroSettings.getColorBox().setValue(ColourArray.getName(wignerPlotParams.colorArray));
		
		limitTransform.setSelected(wignerPlotParams.limitLength);
		transformSpinner.getValueFactory().setValue(wignerPlotParams.manualLength);
		
		spectroSettings.getColourSlider().setHighValue(wignerPlotParams.maxColourVal);
		spectroSettings.getColourSlider().setLowValue(wignerPlotParams.minColourVal);

		channelChoice.setValue(wignerPlotParams.chan);

		allowDynamicUpdate=true;
	}

	@Override
	public String getName() {
		return "Wigner Plot Params";
	}

	@Override
	public Pane getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}


	public synchronized void setChannelList(int channelBitmap) {
		allowDynamicUpdate=false;
		int[] chan=PamUtils.getChannelArray(channelBitmap);
		channelChoice.getItems().clear();
		for (int i=0; i<chan.length; i++){
			channelChoice.getItems().add(chan[i]);
		}
		allowDynamicUpdate=true;
	}
	
	

}
