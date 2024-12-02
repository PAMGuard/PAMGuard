package binaryFileStorage.layoutFX;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.utilityPanes.SelectFolderFX;
import PamController.PamController;
import PamController.SettingsPane;
import binaryFileStorage.BinaryStore;
import binaryFileStorage.BinaryStoreSettings;

public class BinaryStoreSettingsPaneFX extends SettingsPane<BinaryStoreSettings> {
	
	/**
	 * The binary store settings. 
	 */
	private BinaryStoreSettings binaryStoreSettings;
	
	
	private SelectFolderFX storageLocation;
	
	/**
	 * Check box to automatically create new binary files 
	 */
	private CheckBox autoNewFiles;
	
	/**
	 * Check box to put binary files in dated sub folders. 
	 */
	private CheckBox dateSubFolders;
	
	/**
	 * Text field to limit binary file size. 
	 */
	private TextField fileLength;
	
	/**
	 * Check box to limit binary file size
	 */
	private CheckBox limitfileSize;
	
	/**
	 * Field for setting binary file size. 
	 */
	private TextField fileSize;
	
	private String errorTitle = "Binary Storage Options";


	private PamBorderPane mainPane;


	public BinaryStoreSettingsPaneFX(BinaryStore binaryStore) {
		super(null);
		this.mainPane= new PamBorderPane();
		createBinaryStorePane();
		//Padding only seems to work if using CSS
		mainPane.setStyle("-fx-padding: 5px;");
	}
	
	
	/**
	 *Create the settings pane for the Binary store 
	 */
	private void createBinaryStorePane(){
		
		PamVBox paneHolder=new PamVBox(); 
		paneHolder.setSpacing(5);
		paneHolder.getChildren().add(storageLocation=new SelectFolderFX("Binary Storage Folder", 50,true));

		PamGridPane gridPane=new PamGridPane();
		gridPane.setHgap(5);
		gridPane.setVgap(5);

		//want the last column to take up all space
		ColumnConstraints column0 = new ColumnConstraints();
		ColumnConstraints column1 = new ColumnConstraints(100);
		ColumnConstraints column2 = new ColumnConstraints(100,100,Double.MAX_VALUE);
		column2.setHgrow(Priority.ALWAYS);
		gridPane.getColumnConstraints().addAll(column0, column1, column2);

		gridPane.add(autoNewFiles=new CheckBox("Store new data in sub folders by date"), 0,0);

		gridPane.add(dateSubFolders=new CheckBox("Automatically start new files every"),0,1);
		dateSubFolders.setOnAction((action)->{
			enableControls();
		});
		gridPane.add(fileLength=new TextField(),1,1);
		gridPane.add(new Label("minutes"),2,1);
		fileLength.setPrefColumnCount(3);

		gridPane.add(limitfileSize=new CheckBox("Limit the size of data files to"),0,2);
		limitfileSize.setOnAction((action)->{
			enableControls();
		});
		gridPane.add(fileSize=new TextField(),1,2);
		gridPane.add(new Label("MB"),2,2);

		fileLength.setPrefColumnCount(3);
		
		paneHolder.getChildren().add(gridPane);

		this.mainPane.setCenter(paneHolder);

	}

	@Override
	public BinaryStoreSettings getParams(BinaryStoreSettings binaryStoreSettings1) {
		if (this.binaryStoreSettings == null) {
			System.err.println("The binary store settings are null");
			return null;
		}
		binaryStoreSettings.setStoreLocation(storageLocation.getFolderName(true));
		binaryStoreSettings.autoNewFiles = autoNewFiles.isSelected();
		binaryStoreSettings.datedSubFolders = dateSubFolders.isSelected();
		binaryStoreSettings.limitFileSize = limitfileSize.isSelected();
		if (binaryStoreSettings.autoNewFiles) {
			try {
				binaryStoreSettings.fileSeconds = Integer.valueOf(fileLength.getText()) * 60;
				if (binaryStoreSettings.fileSeconds <= 0) {
				 PamDialogFX.showWarning(null, errorTitle, "File length must be > 0");
				 return null;
				}
			}
			catch (NumberFormatException e) {
				 PamDialogFX.showWarning(PamController.getInstance().getGuiManagerFX().getMainScene(), errorTitle, "Invalid file length data");
				 return null;
			}
		}
		if (binaryStoreSettings.limitFileSize) {
			try {
				binaryStoreSettings.maxFileSize = Integer.valueOf(fileSize.getText());
				if (binaryStoreSettings.maxFileSize <= 0) {
					 PamDialogFX.showWarning(PamController.getInstance().getGuiManagerFX().getMainScene(), errorTitle, "File size must be > 0");
					 return null;
				}
			}
			catch (NumberFormatException e) {
				 PamDialogFX.showWarning(PamController.getInstance().getGuiManagerFX().getMainScene(), errorTitle, "Invalid file size data");
				 return null;
			}
		}
		return binaryStoreSettings;
	}

	@Override
	public void setParams(BinaryStoreSettings input) {
		binaryStoreSettings=input.clone();
//		
		storageLocation.setFolderName(binaryStoreSettings.getStoreLocation());
		autoNewFiles.setSelected(binaryStoreSettings.autoNewFiles);
		dateSubFolders.setSelected(binaryStoreSettings.datedSubFolders);
		fileLength.setText(String.format("%d", binaryStoreSettings.fileSeconds/60));
		limitfileSize.setSelected(binaryStoreSettings.limitFileSize);
		fileSize.setText(String.format("%d", binaryStoreSettings.maxFileSize));
		storageLocation.layout();
		
		enableControls();
	}
	
	public void enableControls() {
		fileLength.setDisable(!autoNewFiles.isSelected());
		fileSize.setDisable(!limitfileSize.isSelected());
	}

	@Override
	public String getName() {
		return "Binary Store Settings";
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
