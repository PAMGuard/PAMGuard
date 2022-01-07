package whistlesAndMoans.layoutFX;

import fftManager.FFTDataUnit;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamTabPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.utilityPanes.GroupedSourcePaneFX;
import spectrogramNoiseReduction.layoutFX.SpectrogramNoisePaneFX;
import whistlesAndMoans.WhistleMoanControl;
import whistlesAndMoans.WhistleToneParameters;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import PamController.SettingsPane;

public class WhistleMoanSettingsPaneFX extends SettingsPane<WhistleToneParameters>{
	
	WhistleToneParameters whistleToneParams;
	
	/**
	 * The source pane for the whistle and moan detector. 
	 */
	private GroupedSourcePaneFX sourcePane;

	/**
	 * Reference to whistle and moan detector. 
	 */
	private WhistleMoanControl whistleMoanControl;

	/**
	 * FFT noise reduction pane. 
	 */
	private SpectrogramNoisePaneFX spectrogramNoisePaneFX;

	private PamSpinner<Double> maxFreqSpinner;

	private PamSpinner<Double> minFreqSpinner;

	private ComboBox<String> connectTypeBox;

	private PamSpinner<Integer> minLengthSpinner;

	private PamSpinner<Integer> minPixelsSpinner;

	private ComboBox<String> fragmentationBox;

	private PamSpinner<Integer> maxCrossLengthSpinner;
	
	private PamBorderPane pamBorderPane;
	
	/**
	 * Default spinner width. 
	 */
	private static double SPINNER_WIDTH = 80; 
	
	public WhistleMoanSettingsPaneFX(WhistleMoanControl whistleMoanControl){
		super(null);
		this.whistleMoanControl=whistleMoanControl;
		
		TabPane pamTabbedPane=new TabPane();
//		pamTabbedPane.setAddTabButton(false);
		pamTabbedPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		pamTabbedPane.getTabs().add(new Tab("Whistle Detection", createWhistlePane()));
		pamTabbedPane.getTabs().add(new Tab("Noise and Thresholding", createSpecNoisePane()));
		
		pamBorderPane = new PamBorderPane(pamTabbedPane);
	}
	
	private Pane createWhistlePane(){
		
		PamVBox vBox=new PamVBox();
		vBox.setSpacing(5);
		
		sourcePane = new GroupedSourcePaneFX( "FFT Data Source for Whistle and Moan", FFTDataUnit.class, true, true, true);
		sourcePane.setMaxWidth(Double.MAX_VALUE);
		vBox.getChildren().add(sourcePane);
		
		Label titleLabel = new Label("Contour Connections");
//		titleLabel.setFont(PamGuiManagerFX.titleFontSize2);
		PamGuiManagerFX.titleFont2style(titleLabel);
		
		vBox.getChildren().add(titleLabel);		
		
		//Contour Connection settings
		PamGridPane gridPane=new PamGridPane();
		gridPane.setVgap(5);
		gridPane.setHgap(5);
//	    ColumnConstraints column1 = new ColumnConstraints();
//	    column1.setPercentWidth(30);
//	    ColumnConstraints column2 = new ColumnConstraints();
//	    column2.setPercentWidth(50);
//	    ColumnConstraints column3 = new ColumnConstraints();
//	    column3.setPercentWidth(20);
//	    gridPane.getColumnConstraints().addAll(column1, column2, column3);
//	    gridPane.setGridLinesVisible(true);
	     
		gridPane.add(new Label("Min Frequency"), 0, 0);
		minFreqSpinner=new PamSpinner<Double>(0,1000000.,0,2000.);
		minFreqSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		minFreqSpinner.getValueFactory().valueProperty().addListener((obs, before, after)->{
			if (after>whistleMoanControl.getWhistleToneProcess().getSampleRate()/2.) {
				minFreqSpinner.getValueFactory().setValue(whistleMoanControl.getWhistleToneProcess().getSampleRate()/2.); 
			}
		});
		minFreqSpinner.setEditable(true);
		gridPane.add(minFreqSpinner, 1, 0);
		gridPane.add(new Label("Hz"), 2, 0);

		gridPane.add(new Label("Max Frequency"), 0, 1);		
		maxFreqSpinner=new PamSpinner<Double>(0,1000000.,whistleMoanControl.getWhistleToneProcess().getSampleRate()/2.,2000.);
		maxFreqSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		maxFreqSpinner.getValueFactory().valueProperty().addListener((obs, before, after)->{
			if (after>whistleMoanControl.getWhistleToneProcess().getSampleRate()/2.) {
				maxFreqSpinner.getValueFactory().setValue(whistleMoanControl.getWhistleToneProcess().getSampleRate()/2.); 
			}
		});
		maxFreqSpinner.setEditable(true);
		gridPane.add(maxFreqSpinner, 1, 1);
		//highCut.setPrefColumnCount(6)
		gridPane.add(new Label("Hz"), 2, 1);
		
		gridPane.add(new Label("Connection Type"), 0, 2);
		connectTypeBox=new ComboBox<String>();
		connectTypeBox.getItems().add("Connect 4 (sides only)");
		connectTypeBox.getItems().add("Connect 8 (sides and diagonals)");
		connectTypeBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(connectTypeBox, Priority.ALWAYS);
		gridPane.add(connectTypeBox, 1, 2);
		GridPane.setColumnSpan(connectTypeBox, 4);
		
		gridPane.add(new Label("Minh Length"), 0, 3);		
		minLengthSpinner=new PamSpinner<Integer>(1,100,10,2);
		minLengthSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		minLengthSpinner.setEditable(true);
		minLengthSpinner.setMaxWidth(SPINNER_WIDTH);
		minLengthSpinner.setMinWidth(10);
		gridPane.add(minLengthSpinner, 1, 3);
		//highCut.setPrefColumnCount(6)
		gridPane.add(new Label("time slices"), 2, 3);
		
		gridPane.add(new Label("Min Total size"), 0, 4);		
		minPixelsSpinner=new PamSpinner<Integer>(1,100,20,2);
		minPixelsSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		minPixelsSpinner.setEditable(true);
		gridPane.add(minPixelsSpinner, 1, 4);
		//highCut.setPrefColumnCount(6)
        Label pixelsLbl=new Label("pixels"); 
        GridPane.setHalignment(pixelsLbl, HPos.LEFT);
		gridPane.add(pixelsLbl, 2, 4);

		gridPane.add(new Label("Crossing and Joining"), 0, 5);
		fragmentationBox=new ComboBox<String>();
		fragmentationBox.getItems().add("Leave branched regions intact");
		fragmentationBox.getItems().add("Discard branched regions");
		fragmentationBox.getItems().add("Separate all branches");
		fragmentationBox.getItems().add("Re-link across joins");
		GridPane.setHgrow(fragmentationBox, Priority.ALWAYS);
		fragmentationBox.setMaxWidth(Double.MAX_VALUE);
		gridPane.add(fragmentationBox, 1, 5);
		fragmentationBox.setOnAction((action)->{
			enableControls();
			
		});
		GridPane.setColumnSpan(fragmentationBox, 4);

		gridPane.add(new Label("Max Cross Length"), 0, 6);		
		maxCrossLengthSpinner=new PamSpinner<Integer>(1,100,5,2);
		maxCrossLengthSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		maxCrossLengthSpinner.setEditable(true);
		gridPane.add(maxCrossLengthSpinner, 1, 6);
		//highCut.setPrefColumnCount(6)
		gridPane.add(new Label("time slices"), 2, 6);

		vBox.getChildren().add(gridPane);
		
		return vBox;
	}
	
	private Node createSpecNoisePane(){
		spectrogramNoisePaneFX=new SpectrogramNoisePaneFX(whistleMoanControl.getSpectrogramNoiseProcess());
		return spectrogramNoisePaneFX.getContentNode();
	}

	@Override
	public WhistleToneParameters getParams(WhistleToneParameters wmdParams) {
		
		if (whistleToneParams==null) whistleToneParams = wmdParams;
		
		//set source and grouping data 
		if (sourcePane.getParams(whistleToneParams) == false) {
			return null;
		}

		if (whistleToneParams.getChanOrSeqBitmap() == 0) {
			PamDialogFX.showWarning(null, "Whistle and Moan Detector Warning", "You must select at least one detection channel");
			return null; 
		}
		try {
			whistleToneParams.setMinFrequency(minFreqSpinner.getValue());
			whistleToneParams.setMaxFrequency(maxFreqSpinner.getValue());
			whistleToneParams.minLength = minLengthSpinner.getValue();
			whistleToneParams.minPixels = minPixelsSpinner.getValue();
			whistleToneParams.maxCrossLength = maxCrossLengthSpinner.getValue();
		}
		catch (NumberFormatException e) {
			return null;
		}
		if (connectTypeBox.getSelectionModel().getSelectedIndex() == 1) {
			whistleToneParams.setConnectType(8);
		}
		else {
			whistleToneParams.setConnectType(4);
		}
		whistleToneParams.fragmentationMethod = fragmentationBox.getSelectionModel().getSelectedIndex();

		boolean ok = checkMethods();
		if (ok == false) {
			String msg = "For the Whistle and tone detector to work, you must use \n" +
					"the following spectrogram noise reduction methods:\n" +
					"Median filter\n" +
					"Average Subtraction\n" +
					"Thresholding\n";
			PamDialogFX.showWarning(null, "Whistle and Moan Detector Warning", msg);
			return null;
		}

		//noise suppression settings 
		whistleToneParams.setSpecNoiseSettings(spectrogramNoisePaneFX.getParams());
		if (whistleToneParams.getSpecNoiseSettings()==null) {
			return null;
		}

		return whistleToneParams;
	}
	
	/**
	 * Enable and disbale controls. 
	 */
	private void enableControls() {
		maxCrossLengthSpinner.setDisable(fragmentationBox.getSelectionModel().getSelectedIndex() != WhistleToneParameters.FRAGMENT_RELINK);
	}
		

	@Override
	public void setParams(WhistleToneParameters wmdParams) {
		whistleToneParams=wmdParams.clone();
		
		sourcePane.setParams(whistleToneParams);
		sourcePane.sourceChanged();
		
		//if (datablock!=null) System.out.println("Whistle and Moan datablock: "+datablock.getDataName()); 
		
		//make sure the noise free fft data can't be its own parent. 
		sourcePane.clearExcludeList();
		sourcePane.excludeDataBlock(whistleMoanControl.getSpectrogramNoiseProcess().getOutputDataBlock(), true);
		
		
		minFreqSpinner.getValueFactory().setValue(wmdParams.getMinFrequency());
		maxFreqSpinner.getValueFactory().setValue(wmdParams.getMaxFrequency(whistleMoanControl.getWhistleToneProcess().getSampleRate()));
		if (wmdParams.getConnectType() == 8) {
			connectTypeBox.getSelectionModel().select(1);
		}
		else {
			connectTypeBox.getSelectionModel().select(0);
		}
		minLengthSpinner.getValueFactory().setValue(wmdParams.minLength);
		minPixelsSpinner.getValueFactory().setValue(wmdParams.minPixels);
		
		fragmentationBox.getSelectionModel().select(wmdParams.fragmentationMethod);
		
		maxCrossLengthSpinner.getValueFactory().setValue(wmdParams.maxCrossLength);
		
		spectrogramNoisePaneFX.setParams(wmdParams.getSpecNoiseSettings());
		
		enableControls();

//		//System.out.println("Data block to set for FFT source: "+datablock.getDataName() + " FFT PARAMS: "+fftParameters.dataSource);
//		//fft settings
//		sourcePane.setChannelList(whistleToneParams.getChannelBitmap()); //set selected channels
	}
	
	
	/**
	 * Check that the right noise reduction methods have been selected. 
	 * @return true if enough noise reduction is in place. 
	 */
	private boolean checkMethods() {
		// The methods we really need are numbers  0, 1 and 3
		// the Gaussian smoothing is optional. 
		int[] required = {0, 1, 3};
		for (int i = 0; i < required.length; i++) {
			if (spectrogramNoisePaneFX.hasProcessed(required[i]) == false) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String getName() {
		return "Whistle and Moan Settings";
	}

	@Override
	public Node getContentNode() {
		return pamBorderPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

}
