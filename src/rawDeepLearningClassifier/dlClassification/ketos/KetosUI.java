package rawDeepLearningClassifier.dlClassification.ketos;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import PamController.SettingsPane;
import javafx.stage.FileChooser.ExtensionFilter;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.layoutFX.DLCLassiferModelUI;

/**
 * UI components for the generic model UI. 
 * 
 * @author Jamie Macaulay
 *
 */
public class KetosUI  implements DLCLassiferModelUI {
	
	/**
	 * Pane containing controls to set up the OrcaSPot classifier. 
	 */
	private KetosModelPane ketosSettingsPane;
	
	/**
	 * The sound spot classifier. 
	 */
	private KetosClassifier ketosClassifier;

	private ArrayList<ExtensionFilter> extensionFilters;
	
	/**
	 * SondSpot classifier. 
	 * @param soundSpotClassifier
	 */
	public KetosUI(KetosClassifier ketosClassifier) {
		this.ketosClassifier=ketosClassifier; 
		
		extensionFilters = new ArrayList<ExtensionFilter>(); 
		//import the settings holder
		extensionFilters.add(new ExtensionFilter("Ketos Model", "*.ktpb")); 
	}

	@Override
	public SettingsPane<StandardModelParams> getSettingsPane() {
		if (ketosSettingsPane==null) {
			ketosSettingsPane = new  KetosModelPane(ketosClassifier); 
		}
		return ketosSettingsPane;
		
	}

	@Override
	public void getParams() {
		KetosDLParams ketosParams =  (KetosDLParams) getSettingsPane().getParams(ketosClassifier.getKetosParams()); 
		ketosClassifier.setKetosParams(ketosParams); 
	}

	
	@Override
	public void setParams() {
//		System.out.println("Set model params: " + genericModelClassifier.getGenericDLParams().dlTransfromParams.size()); 
		getSettingsPane().setParams(ketosClassifier.getKetosParams());
	}
	

	@Override
	public JPanel getSidePanel() {	
		//no side pane for Ketos just now. 
		return null;
	}
	
	@Override
	public List<ExtensionFilter> getModelFileExtensions() {
		return extensionFilters;
	}


	
}