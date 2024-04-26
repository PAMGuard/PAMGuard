package rawDeepLearningClassifier.dlClassification.genericModel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import PamController.SettingsPane;
import PamView.dialog.warn.WarnOnce;
import PamView.dialog.warn.WarnOnceDialog;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser.ExtensionFilter;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.layoutFX.DLCLassiferModelUI;


/**
 * UI components for the generic model UI. 
 * 
 * @author Jamie Macaulay
 *
 */
public class GenericModelUI  implements DLCLassiferModelUI {

	/**
	 * Pane containing controls to set up the OrcaSPot classifier. 
	 */
	private GenericModelPane soundSpotPane;

	/**
	 * The sound spot classifier. 
	 */
	private GenericDLClassifier genericModelClassifier;
	
	/**
	 * The extension filter for sound spot models. 
	 */
	private ArrayList<ExtensionFilter> extensionFilters;

	/**
	 * SondSpot classifier. 
	 * @param soundSpotClassifier
	 */
	public GenericModelUI(GenericDLClassifier soundSpotClassifier) {
		this.genericModelClassifier=soundSpotClassifier; 
		
		//must add an additional import settings button. 
		extensionFilters = new ArrayList<ExtensionFilter>(); 

		//import the settings holder
		extensionFilters.add(new ExtensionFilter("TensorFlow Model", "*.pb")); 
		extensionFilters.add(new ExtensionFilter("Pytorch Model", 	"*.pk"));
	}

	@Override
	public SettingsPane<StandardModelParams> getSettingsPane() {
		if (soundSpotPane==null) {
			soundSpotPane = new  GenericModelPane(genericModelClassifier); 
		}
		return soundSpotPane;
	}

	@Override
	public void getParams() {
		
		GenericModelParams genericParams =  (GenericModelParams) getSettingsPane().getParams(genericModelClassifier.getGenericDLParams());
				
		genericModelClassifier.setGenericModelParams(genericParams.clone()); //be safe and clone.  	
	}


	@Override
	public void setParams() {
//		System.out.println("SE MODEL UI PARAMS: " + genericModelClassifier.getGenericDLParams().dlTransfromParams.size()); 
		getSettingsPane().setParams(genericModelClassifier.getGenericDLParams());
	}


	@Override
	public JPanel getSidePanel() {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public List<ExtensionFilter> getModelFileExtensions() {
		return extensionFilters;
	}

	@Override
	public Node getIcon() {
		// TODO Auto-generated method stub
		return null;
	}
}
