package rawDeepLearningClassifier.dlClassification.orcaSpot;

import java.util.List;

import javax.swing.SwingUtilities;

import PamController.SettingsPane;
import javafx.scene.Node;
import javafx.stage.FileChooser.ExtensionFilter;
import rawDeepLearningClassifier.layoutFX.DLCLassiferModelUI;

/**
 * The user interface for OrcaSpot classifier. 
 */
public class OrcaSpotClassifierUI implements DLCLassiferModelUI {
	
	/**
	 * Pane containing controls to set up the OrcaSPot classiifer. 
	 */
	private OrcaSpotPane orcaSpotPane;
	
	/**
	 * The orcaSpot side panel 
	 */
	private OrcaSpotSidePanel orcaSpotSidePanel; 
	
	/**
	 * The orca spot classifier. 
	 */
	private OrcaSpotClassifier orcaSpotClassiifer; 
	
	public OrcaSpotClassifierUI(OrcaSpotClassifier orcaSpotClassiifer) {
		this.orcaSpotClassiifer = orcaSpotClassiifer; 
	}

	@Override
	public SettingsPane<OrcaSpotParams2> getSettingsPane() {
		if (orcaSpotPane==null) {
			orcaSpotPane = new OrcaSpotPane(); 
		}
		return orcaSpotPane;
	}

	@Override
	public void getParams() {
		OrcaSpotParams2 orcaSpotParams =  getSettingsPane().getParams(orcaSpotClassiifer.getOrcaSpotParams()); 
		orcaSpotClassiifer.setOrcaSpotParams(orcaSpotParams.clone()); //be safe and clone.  
		
	}

	@Override
	public void setParams() {
		 getSettingsPane() .setParams(orcaSpotClassiifer.getOrcaSpotParams());
		
	}

	/**
	 * Notify the UI of an update 
	 * @param type
	 */
	public void notifyUpdate(int type) {
	     SwingUtilities.invokeLater(new Runnable() {
	         public void run() {
	        	 getSidePanel().updateNewParams();
	         }
	     });
	}

	@Override
	public OrcaSpotSidePanel getSidePanel() {
		if (orcaSpotSidePanel==null) {
			orcaSpotSidePanel = new OrcaSpotSidePanel(orcaSpotClassiifer); 
		}
		return orcaSpotSidePanel; 
	}

	@Override
	public List<ExtensionFilter> getModelFileExtensions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

}
