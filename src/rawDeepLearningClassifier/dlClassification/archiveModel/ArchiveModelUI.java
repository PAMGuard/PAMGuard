package rawDeepLearningClassifier.dlClassification.archiveModel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import PamController.SettingsPane;
import javafx.scene.Node;
import javafx.stage.FileChooser.ExtensionFilter;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelPane;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.layoutFX.DLCLassiferModelUI;

public class ArchiveModelUI  implements DLCLassiferModelUI {

	/**
	 * Pane containing controls to set up the OrcaSPot classifier. 
	 */
	protected StandardModelPane standardSettingsPane;

	/**
	 * The sound spot classifier. 
	 */
	private ArchiveModelClassifier archiveModelClassifier;

	/**
	 * The extension filter. 
	 */
	private ArrayList<ExtensionFilter> extensionFilters;



	/**
	 * SondSpot classifier. 
	 * @param soundSpotClassifier
	 */
	public ArchiveModelUI(ArchiveModelClassifier archiveClassifier) {
		this.archiveModelClassifier=archiveClassifier; 

//		extensionFilters = new ArrayList<ExtensionFilter>(); 
//
//		String[] fileExt = archiveModel.getFileExtensions();
//
//		extensionFilters.add(new ExtensionFilter(archiveClassifier.getName(), fileExt)); 

	}

	@Override
	public SettingsPane<StandardModelParams> getSettingsPane() {
		if (standardSettingsPane==null) {
			standardSettingsPane = new  ArchiveModelPane(archiveModelClassifier); 
		}
		return standardSettingsPane;
	}

	@Override
	public void getParams() {
		StandardModelParams params =  getSettingsPane().getParams(archiveModelClassifier.getDLParams()); 
		archiveModelClassifier.setDLParams(params); 
	}


	@Override
	public void setParams() {
		//		System.out.println("Set model params: " + genericModelClassifier.getGenericDLParams().dlTransfromParams.size()); 
		getSettingsPane().setParams(archiveModelClassifier.getDLParams());
	}


	@Override
	public JPanel getSidePanel() {	
		//no side pane for Ketos just now. 
		return null;
	}

	@Override
	public List<ExtensionFilter> getModelFileExtensions() {
		if (extensionFilters == null) {
			extensionFilters = new ArrayList<ExtensionFilter>(); 
			extensionFilters.add(new ExtensionFilter(archiveModelClassifier.getName(), archiveModelClassifier.getFileExtensions())); 
		} 
		return extensionFilters;
	}
	
	/**
	 * Set the extension filters. 
	 * @param extensionFilters
	 */
	public void setExtensionFilters(ArrayList<ExtensionFilter> extensionFilters) {
		this.extensionFilters = extensionFilters;
	}

	@Override
	public Node getIcon() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Get the archive model classifier. 
	 * @return the archive model classifier.
	 */
	public ArchiveModelClassifier getArchiveModelClassifier() {
		return archiveModelClassifier;
	}




}