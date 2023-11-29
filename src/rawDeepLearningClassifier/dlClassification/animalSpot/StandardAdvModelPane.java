package rawDeepLearningClassifier.dlClassification.animalSpot;

import java.util.ArrayList;

import org.controlsfx.control.ToggleSwitch;
import org.jamdev.jdl4pam.transforms.DLTransfromParams;

import PamController.SettingsPane;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import rawDeepLearningClassifier.layoutFX.dlTransfroms.DLImageTransformPane;
import rawDeepLearningClassifier.layoutFX.dlTransfroms.DLTransformImage;


/**
 * Advanced settings for models - allows the users to define which transfroms to use. . 
 * 
 * 20:33:02|D|dataOpts: {
    "sr": 256000,
    "preemphases": 0.98,
    "n_fft": 256,
    "hop_length": 8,
    "n_freq_bins": 256,
    "fmin": 40000,
    "fmax": 100000,
    "freq_compression": "linear",
    "min_level_db": -100,
    "ref_level_db": 0
}
 * @author Jamie Macaulay 
 *
 */
public class StandardAdvModelPane extends SettingsPane<StandardModelParams> {
	
	/**
	 * The main pane. 
	 */
	protected PamBorderPane mainPane;
	
	/**
	 * The default parameters. 
	 */
	private StandardModelParams defaultParams = new StandardModelParams(); 
	
	/*
	 * Toggle switch for settings defaults. 
	 */
	private ToggleSwitch toggleSwitch;

	/**
	 * The pane fwith toggle switch for defaults 
	 */
	private Pane defaultTogglePane;

	/**
	 * Pane which holds and allows users to edit dlTransforms; 
	 */
	private DLImageTransformPane transfromPane;
	
	
	/**
	 * The DL transforms image. 
	 */
	private DLTransformImage dlImage;



	public StandardAdvModelPane() {
		super(null);
		mainPane = new PamBorderPane(); 
		
		defaultTogglePane = createTogglePane(); 
		transfromPane = new DLImageTransformPane(); 
		mainPane.setPadding(new Insets(2,2,2,2));
		mainPane.setPrefWidth(400);
		
	}
	
	private Pane createTogglePane() {
		
		PamHBox holderPane = new PamHBox(); 
		holderPane.setSpacing(5);
		
		toggleSwitch = new ToggleSwitch(); 
		toggleSwitch.selectedProperty().addListener((obsval, oldval, newval)->{
			//change the other switches 
			setDefaultEnabled(newval); 
		});
		toggleSwitch.setSelected(true);
		
		holderPane.getChildren().addAll(toggleSwitch, new Label("Use default model settings"));
		
		return holderPane; 
	
	}

	/**
	 * Set the controls to be enabled by default. 
	 * @param true to set the controls to enabled. 
	 */
	private void setDefaultEnabled(Boolean newval) {
		if (transfromPane!=null) transfromPane.setDisable(newval); 
	}

	@Override
	public StandardModelParams getParams(StandardModelParams currParams) {
		currParams.dlTransfroms = transfromPane.getDLTransforms(); 
			
		currParams.dlTransfromParams = new ArrayList<DLTransfromParams>();
//		for (int i=0; i<currParams.dlTransfroms.size(); i++) {
//			//currParams.dlTransfromParams.add(currParams.dlTransfroms.get(i).getDLTransformType()); 
//		}
		currParams.useDefaultTransfroms = toggleSwitch.isSelected(); 
		return currParams; 
	}

	@Override
	public void setParams(StandardModelParams params) {
		//System.out.println("Set advanced pane params: " + params.useDefaultTransfroms);
		if (params.dlTransfroms==null) {
			mainPane.setTop(null);
			mainPane.setCenter(new Label("A model must be loaded before \n "
					+ "advanced settings are available"));
			mainPane.setBottom(null);
			toggleSwitch.setSelected(params.useDefaultTransfroms);
		}
		else {
			mainPane.setTop(defaultTogglePane);
			mainPane.setCenter(transfromPane);
			
			BorderPane.setMargin(defaultTogglePane, new Insets(5));
			transfromPane.setDisable(params.useDefaultTransfroms);
			toggleSwitch.setSelected(params.useDefaultTransfroms);
			
			transfromPane.setTransforms(params.dlTransfroms); 
			//set the image 
			mainPane.setBottom(dlImage);
		}
	}
	

	@Override
	public String getName() {
		return "SoundSpot Adv. Params";
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
	 * Get the default paramters. 
	 * @return the default paramters. 
	 */
	public StandardModelParams getDefaultParams() {
		return defaultParams;
	}

	public void setDefaultParams(StandardModelParams defaultParams) {
		this.defaultParams = defaultParams;
	}
	
	

}
