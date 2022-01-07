package fftManager.layoutFX;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.utilityPanes.FreqResolutionPane;
import pamViewFX.fxNodes.utilityPanes.SourcePaneFX;
import spectrogramNoiseReduction.layoutFX.SpectrogramNoisePaneFX;
import PamController.PamController;
import PamController.SettingsPane;
import PamDetection.RawDataUnit;
import PamguardMVC.PamDataBlock;
import Spectrogram.WindowFunction;
import fftManager.ClickRemoval;
import fftManager.FFTParameters;
import fftManager.PamFFTControl;

/**
 * Settings pane for the FFT module. Allows users to change FFT length, hop, and set noise suppression algorithms.  
 * @author Jamie Macaulay
 *
 */
public class FFTPaneFX extends SettingsPane<FFTParameters>{

	/**
	 * The source for the FFT data source.  
	 */
	private SourcePaneFX sourcePane;

	/**
	 * Holds different window functions. 
	 */
	private ComboBox<String> windowFunction;

	/**
	 * Spinner to change FFT hop size. Also editable to set custom size
	 */
	private PamSpinner<Integer> fftSpinnerHop;

	/**
	 * Spinner to change FFT length. 
	 */
	private PamSpinner<Integer> fftSpinnerLength;

	/**
	 * Shows time and frequency resolution. 
	 */
	private FreqResolutionPane resolutionPane;

	/**
	 * Reference to FFT control. 
	 */
	private PamFFTControl fftControl;

	/**
	 * Pane allowing users to change noise filtering
	 */
	private SpectrogramNoisePaneFX spectrogramNoisePaneFX;

	/**
	 * Check box for click removal. 
	 */
	private CheckBox clickRemoval;

	private final String clickRemoveMessage = 
			"<html>Click removal measures the standard deviation of<p>" +
					"the time series data and then multiplies the signal<p>" +
					"by a factor which increases rapidly for large signal<p>" +
					"components. This has the effect of reducing the<p>magnitude " +
					"of short duration transient signals such as<p>echolocation clicks</html>";

	private TextField clickThreshold;

	private TextField clickPower;

	private FFTParameters fftParameters;

	/**
	 * The main pane. 
	 */
	private PamBorderPane mainPane;

	public FFTPaneFX(PamFFTControl fftControl){
		super(null); 
		this.fftControl=fftControl; 
		TabPane pamTabbedPane=new TabPane();
		pamTabbedPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		pamTabbedPane.getTabs().add(new Tab("FFT", createFFTPane()));
		pamTabbedPane.getTabs().add(new Tab("Click Removal", createClickSuppressionPane()));
		pamTabbedPane.getTabs().add(new Tab("Spectral Noise Removal", createSpecNoisePane()));

		//		Button newButton=new Button("Test");
		//		newButton.setOnAction((action)-> {
		//			pane.layout();
		//			pamTabbedPane.layout();
		//			Stage stage = (Stage) this.getScene().getWindow();
		//			stage.sizeToScene();
		//		});
		//		this.setTop(newButton);
		mainPane=new PamBorderPane(); 
		mainPane.setCenter(new PamBorderPane(pamTabbedPane));
	}

	/**
	 * Create pane to allow users to change noise filter settings. 
	 * @return noise remnoval pane
	 */
	private Node createSpecNoisePane(){
		spectrogramNoisePaneFX=new SpectrogramNoisePaneFX(fftControl.getSpectrogramNoiseProcess());
		return spectrogramNoisePaneFX.getContentNode();
	}

	/**
	 * Create pane to set click suppression params. 
	 * @return node for changing click suppression params
	 */
	private Node createClickSuppressionPane(){

		Label title=new Label("Click Suppression");
//		title.setFont(PamGuiManagerFX.titleFontSize2);
		PamGuiManagerFX.titleFont2style(title);


		PamGridPane gridPane=new PamGridPane();
		gridPane.setHgap(5);
		gridPane.setVgap(5);

		clickRemoval = new CheckBox("Suppress clicks"); 
		clickRemoval.setOnAction((action)->{
			clickThreshold.setDisable(!clickRemoval.isSelected());
			clickPower.setDisable(!clickRemoval.isSelected());
		});

		clickRemoval.setTooltip(new Tooltip(clickRemoveMessage));
		gridPane.add(clickRemoval, 0, 0);

		clickThreshold=new TextField();
		clickThreshold.setPrefColumnCount(6);
		gridPane.add(clickThreshold, 1, 1);
		gridPane.add(new Label("Threshold (STD's)"), 0, 1);

		clickPower=new TextField();
		clickPower.setPrefColumnCount(6);
		gridPane.add(clickPower, 1, 2);
		gridPane.add(new Label("Power"), 0, 2);

		PamVBox vBox=new PamVBox();
		vBox.setSpacing(5);
		vBox.getChildren().addAll(title, gridPane); 
		//vBox.setAlignment(Pos.TOP_CENTER);

		return vBox; 

	}


	/**
	 * Create Pane for changing FFT settings. 
	 * @return pane for changing FFT settings 
	 */
	private Pane createFFTPane(){

		PamVBox vBox=new PamVBox();
		vBox.setSpacing(5);

		sourcePane = new SourcePaneFX("Raw data source for FFT", RawDataUnit.class, true, true);
		PamGuiManagerFX.titleFont2style(sourcePane.getTitleLabel());

		vBox.getChildren().add(sourcePane);

		Label fftParamsLabel = new Label("FFT Parameters");
//		fftParamsLabel.setFont(PamGuiManagerFX.titleFontSize2);
		PamGuiManagerFX.titleFont2style(fftParamsLabel);

		vBox.getChildren().add(fftParamsLabel);

		PamGridPane pamGridPane=new PamGridPane();
		pamGridPane.setHgap(5);
		pamGridPane.setVgap(5);

		//FFT length
		pamGridPane.add(new Label("FFT Length"), 0, 0);

		fftSpinnerLength=new PamSpinner<Integer>(createStepList());
		fftSpinnerLength.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		fftSpinnerLength.getValueFactory().valueProperty().addListener((obs, before, after)->{
			updateResolutionPane();
		});
		pamGridPane.add(fftSpinnerLength, 1, 0);

		//FFT Hop
		pamGridPane.add(new Label("FFT Hop"), 0, 1);

		fftSpinnerHop=new PamSpinner<Integer>(2,(int) Math.pow(2,24),512,128);
		fftSpinnerHop.setEditable(true);
		fftSpinnerHop.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		fftSpinnerHop.getValueFactory().valueProperty().addListener((obs, before, after)->{
			updateResolutionPane();
		});

		pamGridPane.add(fftSpinnerHop, 1, 1);

		PamButton defaultHop=new PamButton();
//		defaultHop.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.REFRESH, PamGuiManagerFX.iconSize-3));
		defaultHop.setGraphic(PamGlyphDude.createPamIcon("mdi2r-refresh", PamGuiManagerFX.iconSize-3));
		defaultHop.setOnAction((action)->{
			fftSpinnerHop.getValueFactory().setValue((int) (fftSpinnerLength.getValue().intValue()/2));
			updateResolutionPane();
		});
		pamGridPane.add(defaultHop, 2, 1);

		//Window type
		pamGridPane.add(new Label("Window"), 0, 2);

		windowFunction=new ComboBox<String>();
		pamGridPane.add(windowFunction, 1, 2);

		vBox.getChildren().add(pamGridPane);

		//Frequency and time resolution info
		vBox.getChildren().add(resolutionPane=new FreqResolutionPane());

		return vBox; 
	}

	/**
	 * Create a step list of FFTlength sizes for a spinner
	 * @return the step list. 
	 */
	public static ObservableList<Integer> createStepList() {
		ObservableList<Integer> stepSizeListLength=FXCollections.observableArrayList();
		for (int i=2; i<15; i++){
			stepSizeListLength.add((int) Math.pow(2,i));
		}
		return stepSizeListLength;
	}

	/**
	 * Update frequency and time resolution info. 
	 */
	private void updateResolutionPane(){
		if (sourcePane.getSource()==null) return; 
		resolutionPane.setParams(sourcePane.getSource().getSampleRate(), fftSpinnerLength.getValue(), fftSpinnerHop.getValue());
	}

	@Override
	public FFTParameters getParams(FFTParameters params) {
		try {
			//			fftParameters.rawDataSource = sourceList.getSelectedItem().toString();
			fftParameters.dataSource = sourcePane.getSourceIndex();
			fftParameters.dataSourceName = sourcePane.getSourceName();
			fftParameters.fftLength = fftSpinnerLength.getValue(); 
//			System.out.println("FFT HOP: " + fftSpinnerHop.getValue());
			fftParameters.fftHop = fftSpinnerHop.getValue(); 
			fftParameters.channelMap = sourcePane.getChannelList();

			fftParameters.clickRemoval = clickRemoval.isSelected();
			if (fftParameters.clickRemoval) {
				fftParameters.clickThreshold = Double.valueOf(clickThreshold.getText());
				fftParameters.clickPower = Integer.valueOf(clickPower.getText());
				if (fftParameters.clickPower < 2 || fftParameters.clickPower%2 == 1) {
					PamDialogFX.showMessageDialog("Error in Click Suppression Params", "Power must be a positive even number");
					return null;
				}
			}

			if (fftParameters.channelMap == 0) return null;

			fftParameters.windowFunction = windowFunction.getSelectionModel().getSelectedIndex();

			if (FFTParameters.isValidLength(fftParameters.fftLength) == false) {
				System.err.println("FFTPaneFX: the FFT Length is not valid...");
				return null;
			}
			//		System.out.println("FFTParametersDialog getChannelList fftParameters.channelMap:" + fftParameters.channelMap);
			fftParameters.spectrogramNoiseSettings = spectrogramNoisePaneFX.getParams();
			if (fftParameters.spectrogramNoiseSettings==null) {
				System.err.println("FFTPaneFX: The spectrogram noise settings params were null...");
				return null;
			}
			fftParameters.spectrogramNoiseSettings.channelList = fftParameters.channelMap;

			return fftParameters;


		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	@Override
	public void setParams(FFTParameters fftParameters) {

		this.fftParameters=fftParameters.clone();

		// and fill in the data source list (may have changed - or might in later versions)
		ArrayList<PamDataBlock> rd = PamController.getInstance().getRawDataBlocks();
		PamDataBlock  datablock = PamController.getInstance().getRawDataBlock(fftParameters.dataSourceName);
		
//		System.out.println("Data block to set for FFT source: "+datablock.getDataName() + " FFT PARAMS: "+fftParameters.dataSourceName);
		
		//fft settings
		sourcePane.setSource(datablock);
		sourcePane.setChannelList(fftParameters.channelMap); //set selected channels

		fftSpinnerLength.getValueFactory().setValue(fftParameters.fftLength);
		fftSpinnerHop.getValueFactory().setValue(fftParameters.fftHop);

		windowFunction.getItems().removeAll(windowFunction.getItems());
		String[] winNames = WindowFunction.getNames();
		for (int i = 0; i < winNames.length; i++) {
			windowFunction.getItems().add(winNames[i]);
		}
		windowFunction.getSelectionModel().select(fftParameters.windowFunction);

		updateResolutionPane();

		//click suppression params
		clickRemoval.setSelected(fftParameters.clickRemoval);
		clickThreshold.setDisable(!clickRemoval.isSelected());
		clickPower.setDisable(!clickRemoval.isSelected());
		if (fftParameters.clickThreshold == 0) {
			fftParameters.clickThreshold = ClickRemoval.defaultClickThreshold;
		}
		if (fftParameters.clickPower == 0) {
			fftParameters.clickPower = ClickRemoval.defaultClickPower;
		}
		clickThreshold.setText(String.format("%.1f", fftParameters.clickThreshold));
		clickPower.setText(String.format("%d", fftParameters.clickPower));

		//noise suppression settings. 
		spectrogramNoisePaneFX.setParams(fftParameters.spectrogramNoiseSettings);

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
		// TODO Auto-generated method stub

	}

}

