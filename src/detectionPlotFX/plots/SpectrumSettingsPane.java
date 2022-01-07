package detectionPlotFX.plots;

import PamController.PamController;
import PamController.SettingsPane;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;

public class SpectrumSettingsPane extends  SettingsPane<SpectrumPlotParams> {
	
	/**
	 * Reference to the spectrum
	 */
	private SpectrumPlot spectrumPlot;
	private RadioButton plotSpectrogram;
	private RadioButton plotCepstrum;
	private CheckBox logScale;
	private CheckBox smoothData;
	
	private PamSpinner<Double> scaleField;
	private ComboBox<String> channelChoice;
	private PamSpinner<Integer> smoothBins;
	private SpectrumPlotParams clickSpectrumParams;
	
	private PamBorderPane mainPane = new PamBorderPane();

	public SpectrumSettingsPane(SpectrumPlot spectrumPlot){
		super(null);
		this.spectrumPlot=spectrumPlot; 
		mainPane.setCenter(createSpectrumPlot());
		mainPane.setMinHeight(300);
	}

	/**
	 * Create the spectrum settings plot. 
	 * @return the spectrum settings pane. 
	 */
	private Node createSpectrumPlot() {
		
		
		PamVBox holderBox= new PamVBox();
		holderBox.setSpacing(5);
		holderBox.setPadding(new Insets(15,5,5,15));
		//holderBox.setAlignment(Pos.CENTER);

		
		Label plotTypeLabel=new Label("Plot Type");
//		plotTypeLabel.setFont(PamGuiManagerFX.titleFontSize2);
		PamGuiManagerFX.titleFont2style(plotTypeLabel);
		
		plotSpectrogram = new RadioButton("Plot Spectrum");
		plotCepstrum = new RadioButton("Plot Cepstrum");
		ToggleGroup toggleGroup=new ToggleGroup();
		toggleGroup.selectedToggleProperty().addListener((oldVal, newVal, obsVal)->{
			newSettings();
		});
		toggleGroup.getToggles().addAll(plotSpectrogram, plotCepstrum, plotCepstrum);
		
		holderBox.getChildren().addAll(plotTypeLabel, plotSpectrogram, plotCepstrum);
		
		Label scaleTypeLabel=new Label("Scale");
//		scaleTypeLabel.setFont(PamGuiManagerFX.titleFontSize2);
		PamGuiManagerFX.titleFont2style(scaleTypeLabel);

		holderBox.getChildren().addAll(scaleTypeLabel); 
				
		PamGridPane holderGrid=new PamGridPane(); 
		holderGrid.setHgap(5);
		holderGrid.setVgap(5);
		holderGrid.getColumnConstraints().add(new ColumnConstraints(90)); 
		
		logScale=new CheckBox("Log scale");
		logScale.setOnAction((action)->{
			scaleField.setDisable(!logScale.isSelected());
			newSettings();
		});
		
		holderGrid.add(logScale, 0, 0);
		holderGrid.add(new Label("ScaleRange"), 0, 1);
		scaleField=new PamSpinner<Double>(2., 100., 30.,5.);
		scaleField.valueProperty().addListener((oldVal, newVal, obsVal)->{
			newSettings();
		});
		scaleField.setEditable(true);
		scaleField.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		holderGrid.add(scaleField, 1, 1);
		scaleField.setPrefWidth(100);
		holderGrid.add(new Label("dB"), 2, 1);

		
		//scaleField=new LabelledTextField(new Label("ScaleRange"), new Label("dB"));
		//holderBox.getChildren().addAll(scaleTypeLabel, logScale, scaleField);
		
//		PamHBox channelHolder=new PamHBox();
//		channelHolder.setSpacing(5);
//		channelHolder.getChildren().addAll(new Label("Channel"), channelChoice=new ComboBox<String>());
//		channelChoice.getItems().add("Show individual channels");
//		channelChoice.getItems().add("Show overall means");
		
		Label plotOptions=new Label("Plot Options");
//		plotOptions.setFont(PamGuiManagerFX.titleFontSize2);
		PamGuiManagerFX.titleFont2style(plotOptions);

		holderGrid.add(plotOptions,0,2); 
		GridPane.setColumnSpan(plotOptions, 3); //allow combobox to take up more room. 

		
		holderGrid.add(new Label("Channel"), 0, 3);
		holderGrid.add(channelChoice=new ComboBox<String>(), 1, 3);
		channelChoice.valueProperty().addListener((oldVal, newVal, obsVal)->{
			newSettings();
		});
		channelChoice.getItems().add("Show individual channels");
		channelChoice.getItems().add("Show overall means");
		GridPane.setColumnSpan(channelChoice, 3); //allow combobox to take up more room. 
		

		smoothData=new CheckBox("Smooth");
		smoothData.setOnAction((action)->{
			smoothBins.setDisable(!smoothData.isSelected());
			newSettings();
		});
		smoothBins=new PamSpinner<Integer>(1, 51, 5,2);
		smoothBins.valueProperty().addListener((oldVal, newVal, obsVal)->{
			newSettings();
		});
		smoothBins.setEditable(true);
		smoothBins.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		smoothBins.setPrefWidth(100);
		Label label=new Label("bin filter");
		
//		PamHBox smoothHolder=new PamHBox();
//		smoothHolder.setSpacing(5);
//		smoothHolder.setAlignment(Pos.CENTER_LEFT);
//		smoothHolder.getChildren().addAll(smoothData, smoothBins, label);
		
		holderGrid.add(smoothData, 0, 4);
		holderGrid.add(smoothBins, 1, 4);
 
		holderGrid.add(label, 2, 4);

		holderBox.getChildren().addAll(holderGrid);
		
		enableControls();

		return holderBox;
	}
	
	
	private void enableControls() {
		smoothBins.setDisable(!smoothData.isSelected());
		scaleField.setDisable(!logScale.isSelected());
	}

	@Override
	public void setParams(SpectrumPlotParams clickSpectrumParams) {
		this.clickSpectrumParams=clickSpectrumParams.clone();
		
		plotSpectrogram.setSelected(clickSpectrumParams.plotCepstrum == false);
		plotCepstrum.setSelected(clickSpectrumParams.plotCepstrum == true);
		logScale.setSelected(clickSpectrumParams.logScale);
		scaleField.getValueFactory().setValue(clickSpectrumParams.logRange);
		
		//System.out.println("clickSpectrumParams.channelChoice: " + clickSpectrumParams.channelChoice); 
		
		if (clickSpectrumParams.channelChoice<0) clickSpectrumParams.channelChoice = SpectrumPlotParams.CHANNELS_SINGLE;
		channelChoice.getSelectionModel().select(clickSpectrumParams.channelChoice);
		
		smoothData.setSelected(clickSpectrumParams.smoothPlot);
		
		
		smoothBins.getValueFactory().setValue(clickSpectrumParams.plotSmoothing);	
		enableControls();
	}
	
	public SpectrumPlotParams getParams(SpectrumPlotParams clickSpectrumParams) {
		clickSpectrumParams.plotCepstrum = plotCepstrum.isSelected();
		clickSpectrumParams.logScale = logScale.isSelected();
		try {
			clickSpectrumParams.logRange = scaleField.getValue();
		}
		catch (NumberFormatException e) {
			 PamDialogFX.showWarning(PamController.getMainStage(), "Spectrum Plot Params Warning", "Invalid range value");
			 return null;
		}
		clickSpectrumParams.logRange = Math.abs(clickSpectrumParams.logRange);
		if (clickSpectrumParams.logRange == 0) {
			PamDialogFX.showWarning(PamController.getMainStage(), "Spectrum Plot Params Warning", "The Scale range must be greater than zero");
			//return showWarning("The Scale range must be greater than zero");
			 return null;
		}
		
		clickSpectrumParams.channelChoice = channelChoice.getSelectionModel().getSelectedIndex();
		if (clickSpectrumParams.smoothPlot = smoothData.isSelected() == true) {
			try {
				clickSpectrumParams.plotSmoothing = smoothBins.getValue();
			}
			catch(NumberFormatException e) {
				 PamDialogFX.showWarning(PamController.getMainStage(), "Spectrum Plot Params Warning", "Invalid smoothing constant");
				 return null;
				//return showWarning("Invalid smoothing constant");
			}
			if (clickSpectrumParams.plotSmoothing%2 == 0 || clickSpectrumParams.plotSmoothing <= 0) {
				 PamDialogFX.showWarning(PamController.getMainStage(), "Spectrum Plot Params Warning", "The Smoothing constant must be a positive odd integer");
				 return null;
			}
		}
		
		return clickSpectrumParams;
	}

	/**
	 * Called whenever a control is set to allow for settings updates as they happen
	 */
	public void newSettings(){
		getParams(spectrumPlot.getSpectrumParams());
		spectrumPlot.reDrawLastUnit();
	}

	@Override
	public String getName() {
		return "Spectrum";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		
	}


}
