package rawDeepLearningClassifier.dlClassification.animalSpot;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import PamController.SettingsPane;
import javafx.scene.Node;
import javafx.stage.FileChooser.ExtensionFilter;
import rawDeepLearningClassifier.layoutFX.DLCLassiferModelUI;


/**
 * UI components for the SoundSpot deep learning model. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class SoundSpotUI implements DLCLassiferModelUI {
	
	/**
	 * Pane containing controls to set up the OrcaSPot classifier. 
	 */
	private SoundSpotModelPane soundSpotPane;
	
	/**
	 * The sound spot classifier. 
	 */
	private SoundSpotClassifier soundSpotClassifier;
	
	
	/**
	 * The extension filter for sound spot models. 
	 */
	private ArrayList<ExtensionFilter> extensionFilters; 
	
	/**
	 * SondSpot classifier. 
	 * @param soundSpotClassifier
	 */
	public SoundSpotUI(SoundSpotClassifier soundSpotClassifier) {
		this.soundSpotClassifier=soundSpotClassifier; 
		
		extensionFilters = new  ArrayList<ExtensionFilter> (); 
		extensionFilters.add(new ExtensionFilter("Pytorch Model", "*.pk")); 
	}

	@Override
	public SettingsPane<StandardModelParams> getSettingsPane() {
		if (soundSpotPane==null) {
			soundSpotPane = new  SoundSpotModelPane(soundSpotClassifier); 
		}
		return soundSpotPane;
	}

	@Override
	public void getParams() {
		StandardModelParams orcaSpotParams =  getSettingsPane().getParams(soundSpotClassifier.getSoundSpotParams()); 
		soundSpotClassifier.setSoundSpotParams(orcaSpotParams.clone()); //be safe and clone.  
	}

	@Override
	public void setParams() {
		 getSettingsPane().setParams(soundSpotClassifier.getSoundSpotParams());
		
	}


	@Override
	public JPanel getSidePanel() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Get a list of extension fitlers for the file dialog. 
	 * e.g. 
	 * new ExtensionFilter("Pytorch Model", "*.pk")
	 * @return a list of extension fitlers for the file dialog. 
	 */
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
