package Acquisition.layoutFX;

import java.io.File;

import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.utilityPanes.SelectFolderFX;
import javafx.scene.Node;
import javafx.scene.control.Label;
import PamController.OfflineFileDataStore;
import PamController.PamController;
import PamController.SettingsPane;
import dataMap.filemaps.OfflineFileParameters;

public class OfflineDAQPane extends SettingsPane<OfflineFileParameters>{
	
	/**
	 * The data store for offline data. 
	 */
	private OfflineFileDataStore offlineRawDataStore;

	/**
	 * The location of the file store. 
	 */
	private SelectFolderFX storageLocation;

	//mainpane,
	private PamBorderPane mainPane;

	/**
	 * The parameters this pane was last set with. Kept so that settings this pane
	 * doesn't show, such as an explicit list of selected files, survive a trip
	 * through the dialog.
	 */
	private OfflineFileParameters currentParams;


	public OfflineDAQPane(OfflineFileDataStore acquisitionControl){
		super(null);
		this.mainPane= new PamBorderPane();
		mainPane.setCenter(createOfflinePane());
		
	}
	
	private Node createOfflinePane(){
		PamVBox vBoxHolder=new PamVBox();

		//the location of files. 
		Label sourceLabel=new Label("Sound Files");
		PamGuiManagerFX.titleFont2style(sourceLabel);
	
		storageLocation=new SelectFolderFX(10); 
		
		vBoxHolder.getChildren().addAll(sourceLabel, storageLocation); 
		
		return vBoxHolder; 
		
	}


	@Override
	public String getName() {
		return "Sound Files";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}
	
	public void setParams(OfflineFileParameters p) {
		//enableOffline.setSelected(p.enable);
		currentParams = p;
		storageLocation.setFolderName(p.folderName);
		storageLocation.setIncludeSubFolders(p.includeSubFolders);
		enableControls();
	}
	
	private void enableControls() {
		// TODO Auto-generated method stub
	}

	private boolean checkFolder(String file) {
		if (file == null) {
			return false;
		}
		File f = new File(file);
		if (f.exists() == false) {
			return false;
		}
		return true;
	}
	
	public OfflineFileParameters getParams() {
		OfflineFileParameters p = currentParams == null ? new OfflineFileParameters() : currentParams.clone();
		p.includeSubFolders = storageLocation.isIncludeSubFolders();
		String previousFolder = p.folderName;
		p.folderName = storageLocation.getFolderName(false);
		/*
		 * This pane only ever selects a folder, so if the user has changed it then any
		 * list of individually selected files no longer applies.
		 */
		if (previousFolder == null || previousFolder.equals(p.folderName) == false) {
			p.setSelectedFiles((String[]) null);
		}
		if (checkFolder(p.folderName) == false && p.enable) {
			if (p.folderName == null) {
				PamDialogFX.showWarning(PamController.getInstance().getMainStage(), "Error in file store", "No storage folder selected");
				return null;
			}
			else {
				String err = String.format("The folder %s does not exist", p.folderName);
				PamDialogFX.showWarning(PamController.getInstance().getMainStage(),"Error in file store", err);
				return null;
			}
		}
		return p;
	}

	@Override
	public OfflineFileParameters getParams(OfflineFileParameters currParams) {
		return getParams() ;
	}

}
