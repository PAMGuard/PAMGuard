package pamViewFX.fxSettingsPanes;

import generalDatabase.DBControlUnit;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import binaryFileStorage.BinaryStore;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxStyles.PamStylesManagerFX;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextAlignment;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.SettingsPane;
import PamController.StorageParameters;
import PamguardMVC.PamDataBlock;

public class StorageOptionsPane  extends SettingsPane<StorageParameters>{
	
	private StorageParameters storageParameters;
	
	private ArrayList<PamDataBlock> dataBlocks;

	private ArrayList<PamDataBlock> usedDataBlocks = new ArrayList<PamDataBlock>();
	
	private ArrayList<CheckBox> dbCheckBoxes = new ArrayList<CheckBox>();
	private ArrayList<CheckBox> bsCheckBoxes = new ArrayList<CheckBox>();

	private PamBorderPane mainPane = new PamBorderPane();
	
	public StorageOptionsPane(){
		super(null);
	}
	
	
	private Pane createControls() {
		PamController pamController = PamController.getInstance();
		BinaryStore binaryStore = (BinaryStore) pamController.findControlledUnit(BinaryStore.defUnitType);
		DBControlUnit database = DBControlUnit.findDatabaseControl();
		dataBlocks = pamController.getDataBlocks();
		PamDataBlock aDataBlock;
		boolean hasDatabase, hasBinaryStore;
		usedDataBlocks.clear();
		dbCheckBoxes.clear();
		bsCheckBoxes.clear();
		
		PamGridPane gridPane=new PamGridPane();
		

		
		
		Label l = new Label("  Binary Store  ");
		l.setTextAlignment(TextAlignment.CENTER);
		if (binaryStore == null) {
			l.setTooltip(new Tooltip("Binary Storage module is not loaded"));
		}
		else {
			l.setTooltip(new Tooltip("Binary Storage is more efficient than the database for many types of detection data"));
		}
		gridPane.add( l, 1, 0);
		
		l = new Label("  Database  ");
		l.setTextAlignment(TextAlignment.CENTER);
		if (binaryStore == null) {
			l.setTooltip(new Tooltip("Database module is not loaded"));
		}
		else {
			l.setTooltip(new Tooltip("Database Storage can be slow for high volume data !"));
		}
		gridPane.add( l, 2, 0);
		gridPane.setVgap(5);
		gridPane.setHgap(5);

		
		CheckBox cb;
		PamControlledUnit pcu;
		
		for (int i = 0; i < dataBlocks.size(); i++) {
			aDataBlock = dataBlocks.get(i);
			hasBinaryStore = aDataBlock.getBinaryDataSource() != null;
			if (hasBinaryStore) {
				hasBinaryStore = aDataBlock.getBinaryDataSource().isDoBinaryStore();
			}
			hasDatabase = aDataBlock.getLogging() != null;
			if (!hasBinaryStore && !hasDatabase) {
				continue;
			}
			usedDataBlocks.add(aDataBlock);
			l = new Label(aDataBlock.getDataName());
			l.setTextAlignment(TextAlignment.LEFT);
			pcu = aDataBlock.getParentProcess().getPamControlledUnit();
			l.setTooltip(new Tooltip("Module: " + pcu.getUnitName() + "-" + aDataBlock.getLongDataName()));
			gridPane.add( l, 0, i+1);

			cb = new CheckBox();
			GridPane.setHalignment(cb, HPos.CENTER);
//			cb.setVisible(hasBinaryStore);
			cb.setDisable(!(hasBinaryStore && binaryStore != null));
			if (hasBinaryStore == false) {
				cb.setTooltip(new Tooltip("Binary storage is not available for this data block"));
			}
			if (hasBinaryStore) {
				cb.setSelected(storageParameters.isStoreBinary(aDataBlock, true));
			}
			bsCheckBoxes.add(cb);
			gridPane.add( cb, 1, i+1);
					
			cb = new CheckBox();
			GridPane.setHalignment(cb, HPos.CENTER);
			cb.setAlignment(Pos.CENTER);
//			cb.setVisible(hasDatabase);
			cb.setDisable(!(hasDatabase && database != null));
			if (hasDatabase == false) {
				cb.setTooltip(new Tooltip("Database storage is not available for this data block"));
			}
			if (hasDatabase) {
				cb.setSelected(storageParameters.isStoreDatabase(aDataBlock, !hasBinaryStore));
			}
			dbCheckBoxes.add(cb);			
			gridPane.add( cb, 2, i+1);

		}
		
		return gridPane; 
		
	}


	@Override
	public StorageParameters getParams(StorageParameters g) {
		boolean storeDatabase, storeBinary;
		int errors = 0;
		for (int i = 0; i < usedDataBlocks.size(); i++) {
			storeDatabase = dbCheckBoxes.get(i).isSelected();
			storeBinary = bsCheckBoxes.get(i).isSelected();
			if (storeDatabase == false && storeBinary == false) {
				errors++;
			}
			storageParameters.setStorageOptions(usedDataBlocks.get(i), storeDatabase, storeBinary);
		}
		if (errors > 0) {
			boolean answerOK=PamDialogFX.showWarning(PamController.getInstance().getMainStage(), "Storage Options Warning",
					"At least one data stream is not connected to any storage type"); 
			if (!answerOK) return null; 
		}
		return storageParameters;
	}

	@Override
	public void setParams(StorageParameters input) {
		storageParameters=input.clone();
		mainPane.setCenter(createControls());
	}

	@Override
	public String getName() {
		return "Storage Options";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

}
