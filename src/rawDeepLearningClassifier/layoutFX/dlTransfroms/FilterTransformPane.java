package rawDeepLearningClassifier.layoutFX.dlTransfroms;

import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.SimpleTransform;
import org.jamdev.jdl4pam.transforms.WaveTransform;
import org.jamdev.jpamutils.wavFiles.AudioData;
import org.jamdev.jpamutils.wavFiles.FilterParams;

import Filters.FilterBand;
import fftFilter.FFTFilterParams;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.SimpleFilterPaneFX;

/**
 * Pane for a waveform filter. 
 */
public class FilterTransformPane extends DLTransformPane {
	
	/**
	 * The transform associated with the settings pane. 
	 */
	private DLTransform simpleTransfrom;
	
	/**
	 * Controls for changing filter settings. 
	 */
	private SimpleFilterPaneFX filterPane;

	/**
	 * Choice box for changing the filter type - e.g. Butterworth
	 */
	private ComboBox<String> filterMethodBox;

	/**
	 * Spinner for changing the filter order. 
	 */
	private Spinner<Integer> orderSpinner; 
	
	
	public FilterTransformPane(DLTransform dlTransfrom) {
		super();
		this.simpleTransfrom= dlTransfrom;		
		this.setCenter(createFilterPane());
//		this.setStyle("-fx-background-color:orangered;");	
		
	}
	
	private Node createFilterPane() {
		
		PamVBox mainPane = new PamVBox();
		mainPane.setSpacing(5);
		
		filterMethodBox = new ComboBox<String>();
		filterMethodBox.getItems().add(AudioData.BUTTERWORTH, 	"Butterworth");
		filterMethodBox.getItems().add(AudioData.CHEBYSHEV, 	"Chebyshev");
		
		filterMethodBox.valueProperty().addListener((obsVal, oldVal, newVal)->{
			this.notifySettingsListeners();
		});
		
		//spinner for changing filter order.
		orderSpinner = new Spinner<Integer>(1,50,4,1);
		orderSpinner.valueProperty().addListener((obsVal, oldVal, newVal)->{
			this.notifySettingsListeners();
		});
		orderSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);

	
		PamHBox filterTypeHolder = new PamHBox(); 
		filterTypeHolder.setSpacing(5);
		filterTypeHolder.setAlignment(Pos.CENTER_LEFT);
		filterTypeHolder.getChildren().addAll(filterMethodBox, new Label("Order"), orderSpinner);
		
		//create the filter pane - use the standard pG one - a bit of hack but no point in basically replicating 
		//this whole pane again. 
		filterPane = new  SimpleFilterPaneFX();
		
		filterPane.addSettingsListener(()->{
			this.notifySettingsListeners();
		});
		
		mainPane.getChildren().addAll(new Label("Filter Type"), filterTypeHolder,  filterPane.getContentNode()); 
		
		TitledPane titledPane = new TitledPane(simpleTransfrom.getDLTransformType().toString(), mainPane); 

		//			PamBorderPane borderPane = new PamBorderPane(); 
		//			borderPane.setTop(new Label(simpleTransfrom.getDLTransformType().toString()));
		//			borderPane.setCenter(hBox);

		titledPane.setExpanded(false);

		return titledPane;
	}
	
	/**
	 * Convert parameters array to filter parameters. 
	 * @param params - the parameters to set. 
	 * @return the FFTfilterParams object with params from fileParams
	 */
	private FFTFilterParams transform2FilterParams(FilterParams filtParams) {
	
		FFTFilterParams filtParamsPG = new FFTFilterParams();
		
		switch (filtParams.filterType) {
		case AudioData.LOWPASS:
			filtParamsPG.filterBand = FilterBand.LOWPASS;
			break;
		case AudioData.HIGHPASS:
			filtParamsPG.filterBand = FilterBand.HIGHPASS;
			break;
		case AudioData.BANDPASS:
			filtParamsPG.filterBand = FilterBand.BANDPASS;
			break;
		}
		
		filtParamsPG.lowPassFreq = filtParams.highCut;
		filtParamsPG.highPassFreq = filtParams.lowCut;
		
	
	return filtParamsPG;

	}
	
	private FilterParams getFFTFilterSettings(FilterParams params) {
		
		FFTFilterParams paramsFFT = filterPane.getParams(new FFTFilterParams()); 
		
		switch (paramsFFT.filterBand) {
		case BANDPASS:
			params.filterType = AudioData.BANDPASS;
			break;
		case BANDSTOP:
			break;
		case HIGHPASS:
			params.filterType = AudioData.HIGHPASS;
			break;
		case LOWPASS:
			params.filterType = AudioData.LOWPASS;
			break;
		}
		
		params.highCut =  paramsFFT.lowPassFreq;
		params.lowCut =  paramsFFT.highPassFreq;

		return params;
	}

	@Override
	public DLTransform getDLTransform() {
		return this.getParams(simpleTransfrom) ;
	}

	@Override
	public DLTransform getParams(DLTransform dlTransform) {
		
//		System.out.println("GET PARAMS: FILTER");

		SimpleTransform simpleTransform = (SimpleTransform) dlTransform;
		
		//create filter params object. 
		FilterParams filtParams = new FilterParams();
		filtParams = getFFTFilterSettings(filtParams);
		
		filtParams.filterMethod = filterMethodBox.getSelectionModel().getSelectedIndex();
		filtParams.order = orderSpinner.getValue();
		
		simpleTransform.setParams(WaveTransform.filterParams2transform(filtParams));
		
		return simpleTransform;
	}

	@Override
	public void setParams(DLTransform dlTransform) {
		
//		System.out.println("SET PARAMS: FILTER");
		
		SimpleTransform simpleTransform = (SimpleTransform) dlTransform;
		
		//get filter params as an object to try and make less mistakes!
		FilterParams filtParams = WaveTransform.transform2FilterParams(simpleTransform.getParams());
		
		//get the selection model. 
		filterMethodBox.getSelectionModel().select(filtParams.filterMethod);
		orderSpinner.getValueFactory().setValue(filtParams.order);
		
		//bit of a hack but otherwise have to rewrite filter GUI. 
		filterPane.setParams(transform2FilterParams(filtParams));
		
	}

}
