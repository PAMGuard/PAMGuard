package rawDeepLearningClassifier.dlClassification.archiveModel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import PamController.SettingsPane;
import javafx.stage.FileChooser.ExtensionFilter;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelPane;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.layoutFX.DLCLassiferModelUI;

public class ArchiveModelUI  implements DLCLassiferModelUI {

	/**
	 * Pane containing controls to set up the OrcaSPot classifier. 
	 */
	private StandardModelPane standardSettingsPane;

	/**
	 * The sound spot classifier. 
	 */
	private ArchiveModelClassifier archiveModel;

	/**
	 * The extension filter. 
	 */
	private ArrayList<ExtensionFilter> extensionFilters;



	/**
	 * SondSpot classifier. 
	 * @param soundSpotClassifier
	 */
	public ArchiveModelUI(ArchiveModelClassifier archiveClassifier) {
		this.archiveModel=archiveClassifier; 

//		extensionFilters = new ArrayList<ExtensionFilter>(); 
//
//		String[] fileExt = archiveModel.getFileExtensions();
//
//		extensionFilters.add(new ExtensionFilter(archiveClassifier.getName(), fileExt)); 

	}

	@Override
	public SettingsPane<StandardModelParams> getSettingsPane() {
		if (standardSettingsPane==null) {
			standardSettingsPane = new  ArchiveModelPane(archiveModel); 
		}
		return standardSettingsPane;
	}

	@Override
	public void getParams() {
		StandardModelParams params =  getSettingsPane().getParams(archiveModel.getDLParams()); 
		archiveModel.setDLParams(params); 
	}


	@Override
	public void setParams() {
		//		System.out.println("Set model params: " + genericModelClassifier.getGenericDLParams().dlTransfromParams.size()); 
		getSettingsPane().setParams(archiveModel.getDLParams());
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
			extensionFilters.add(new ExtensionFilter(archiveModel.getName(), archiveModel.getFileExtensions())); 
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



}