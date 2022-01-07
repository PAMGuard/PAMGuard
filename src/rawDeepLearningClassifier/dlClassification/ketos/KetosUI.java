package rawDeepLearningClassifier.dlClassification.ketos;

import javax.swing.JPanel;

import PamController.SettingsPane;
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
	
	/**
	 * SondSpot classifier. 
	 * @param soundSpotClassifier
	 */
	public KetosUI(KetosClassifier ketosClassifier) {
		this.ketosClassifier=ketosClassifier; 
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
		KetosDLParams genericParams =  (KetosDLParams) getSettingsPane().getParams(ketosClassifier.getKetosParams()); 
		ketosClassifier.setKetosParams(genericParams); 
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
	
}