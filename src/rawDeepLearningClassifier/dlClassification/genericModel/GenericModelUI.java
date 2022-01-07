package rawDeepLearningClassifier.dlClassification.genericModel;

import javax.swing.JPanel;

import PamController.SettingsPane;
import PamView.dialog.warn.WarnOnce;
import PamView.dialog.warn.WarnOnceDialog;
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
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
	 * SondSpot classifier. 
	 * @param soundSpotClassifier
	 */
	public GenericModelUI(GenericDLClassifier soundSpotClassifier) {
		this.genericModelClassifier=soundSpotClassifier; 
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
		//		System.out.println("Set model params: " + genericModelClassifier.getGenericDLParams().dlTransfromParams.size()); 
		getSettingsPane().setParams(genericModelClassifier.getGenericDLParams());
	}


	@Override
	public JPanel getSidePanel() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
