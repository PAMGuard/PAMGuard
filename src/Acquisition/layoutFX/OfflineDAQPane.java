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
		OfflineFileParameters p = new OfflineFileParameters();
		p.includeSubFolders = storageLocation.isIncludeSubFolders();
		p.folderName = storageLocation.getFolderName(false);
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
