package rawDeepLearningClassifier.layoutFX.dlTransfroms;

import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.SimpleTransform;
import org.jamdev.jpamutils.wavFiles.AudioData;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TitledPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.utilityPanes.SimpleFilterPaneFX;

/**
 * Pane for a peak search trim transform. 
 * 
 * @author Jamie Macaulay
 *
 */
public class PeakTrimTransformPane extends DLTransformPane {
	
	/**
	 * The transform associated with the settings pane. 
	 */
	private DLTransform simpleTransfrom;
	
	/**
	 * Controls for changing peak search settings. 
	 */
	private SimpleFilterPaneFX filterPane;

	/**
	 * Choice box for changing the type of peak search algorithm
	 */
	private ComboBox<String> peakSelectionBox;

	/**
	 * Spinner for changing the target length
	 */
	private Spinner<Integer> targetLenSpinner; 
	
	
	public PeakTrimTransformPane(DLTransform dlTransfrom) {
		super();
		this.simpleTransfrom= dlTransfrom;		
		this.setCenter(createFilterPane());
//		this.setStyle("-fx-background-color:orangered;");	
		
	}
	
	private Node createFilterPane() {
		
		peakSelectionBox = new ComboBox<String>();
		peakSelectionBox.getItems().add(AudioData.PEAK_MAX, "Max. Peak");
		
		peakSelectionBox.valueProperty().addListener((obsVal, oldVal, newVal)->{
			this.notifySettingsListeners();
		});
		
		//spinner for changing filter order.
		targetLenSpinner = new Spinner<Integer>(1,50,4,1);
		targetLenSpinner.valueProperty().addListener((obsVal, oldVal, newVal)->{
			this.notifySettingsListeners();
		});
		targetLenSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);

		PamHBox filterTypeHolder = new PamHBox(); 
		filterTypeHolder.setSpacing(5);
		filterTypeHolder.setAlignment(Pos.CENTER_LEFT);
		filterTypeHolder.getChildren().addAll(peakSelectionBox, new Label("Target length"), targetLenSpinner);
		
		TitledPane titledPane = new TitledPane(simpleTransfrom.getDLTransformType().toString(), filterTypeHolder); 

		//			PamBorderPane borderPane = new PamBorderPane(); 
		//			borderPane.setTop(new Label(simpleTransfrom.getDLTransformType().toString()));
		//			borderPane.setCenter(hBox);

		titledPane.setExpanded(false);

		return titledPane;
	}
	

	@Override
	public DLTransform getDLTransform() {
		return this.getParams(simpleTransfrom) ;
	}

	@Override
	public DLTransform getParams(DLTransform dlTransform) {
		
//		System.out.println("GET PARAMS: FILTER");

		SimpleTransform simpleTransform = (SimpleTransform) dlTransform;
		
		simpleTransform.setParams(new Number[]{targetLenSpinner.getValue(), peakSelectionBox.getSelectionModel().getSelectedIndex()});
		
		return simpleTransform;
	}

	@Override
	public void setParams(DLTransform dlTransform) {
		
//		System.out.println("SET PARAMS: FILTER");
		
		SimpleTransform simpleTransform = (SimpleTransform) dlTransform;
		
		//get the selection model. 
		peakSelectionBox.getSelectionModel().select(simpleTransform.getParams()[1].intValue());
		targetLenSpinner.getValueFactory().setValue(simpleTransform.getParams()[0].intValue());
		
	}

}
