package pamViewFX;

import generalDatabase.DBControlUnit;

import java.util.Optional;

import binaryFileStorage.BinaryStore;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.SettingsPane;
import PamController.StorageOptions;
import PamController.StorageParameters;
import PamModel.PamModuleInfo;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.pamDialogFX.PamSettingsDialogFX;
import pamViewFX.fxNodes.utilityPanes.SettingsDialog;
import pamViewFX.fxSettingsPanes.StorageOptionsPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.text.TextAlignment;

/**
 * Pane which holds settings menu. All primary settings are accessed from this pane which sits in a hiding pane to the right 
 * of the main GUI. 
 * @author Jamie Macaulay
 *
 */
public class PamSettingsMenuPane extends PamVBox {
	
	private double leftInset=60;
	private double heightSpacer=2; 
	
	StorageOptionsPane storageOptionPane; 
	
	public PamSettingsMenuPane(){
		
		this.setSpacing(0);
				
		this.setPrefWidth(250);
		
		this.setStyle("-fx-background-color: -fx-darkbackground");

		Label settingsLabel=new Label("Settings");
		settingsLabel.setPadding(new Insets(14,0,10,0));
		settingsLabel.setAlignment(Pos.CENTER);
		settingsLabel.setPrefWidth(Double.MAX_VALUE);
		settingsLabel.setTextAlignment(TextAlignment.CENTER);
		settingsLabel.setStyle("-fx-font-weight: bold;");

		PamButton saveConfig=new PamButton("Save");
		saveConfig.setOnAction((action)->{
			PamSettingManager.getInstance().saveSettings(null);
		});
		styleButton(saveConfig);
		
		PamButton saveConfigAs=new PamButton("Save as..."); 
		saveConfigAs.setOnAction((action)->{
			PamSettingManager.getInstance().saveSettingsAs(null);
		});
		styleButton(saveConfigAs);
		
		PamButton generalSettings=new PamButton("General Settings..."); 
		styleButton(generalSettings);
		
		MenuButton settings=new MenuButton("Module Settings"); 
		settings.setStyle("-fx-background-radius: 0;"
				+ " -fx-border-color: transparent; -fx-padding: 0 0 0 0;");
		settings.setAlignment(Pos.CENTER_LEFT);
		settings.setPrefWidth(Double.MAX_VALUE);
		settings.setPrefHeight(40);
		settings.showingProperty().addListener( (action)->{
			settings.getItems().removeAll(settings.getItems());
			int nUnits=PamController.getInstance().getNumControlledUnits();
			MenuItem menuItem;
			for (int i=0; i<nUnits ;i++){
				final int n=i; //need to make n final- OK since menu re-makes itself every time opened. 
				menuItem=new MenuItem(PamController.getInstance().getControlledUnit(i).getUnitName());
				settings.getItems().add(menuItem);
				menuItem.setOnAction((event) -> { 
					openSettingsDialog( PamController.getInstance().getControlledUnit(n));
				});
			}
		});
			
		PamButton database=new PamButton("Database Storage..."); 
		database.setOnAction((action)->{
			//first, does the database exist?
			if (DBControlUnit.findDatabaseControl()==null){
				//no database exists...
				boolean addDb=PamDialogFX.showMessageDialog("No Database Module Found", "No database module has been added to the data model."
						+ " Would you like to add a database module now?");
				if (addDb){
					PamController.getInstance().addModule(PamModuleInfo.findModuleInfo("generalDatabase.DBControlUnit"), "Database"); 
					openSettingsDialog(DBControlUnit.findDatabaseControl());
				}
			}
			else {
				openSettingsDialog(DBControlUnit.findDatabaseControl());
			}
		}); 
		styleButton(database);

		PamButton binaryStorage=new PamButton("Binary Storage..."); 
		binaryStorage.setOnAction((action)->{
			//first, does the binary storage module exist?
			//first, does the database exist?
			if (BinaryStore.findBinaryStoreControl()==null){
				//no database exists...
				boolean addDb=PamDialogFX.showMessageDialog("No Binary Storage Module Found", 
						"No binary storage module has been added to the data model."
						+ " Would you like to add a binary store now?");
				if (addDb){
					PamController.getInstance().addModule(PamModuleInfo.findModuleInfo("binaryFileStorage.BinaryStore"), "Binary Store"); 
					openSettingsDialog(BinaryStore.findBinaryStoreControl());
				}
			}
			else {
				openSettingsDialog(BinaryStore.findBinaryStoreControl());
			}
		}); 
		styleButton(binaryStorage);

		PamButton storageManager=new PamButton("Storage Manager..."); 
		storageManager.setOnAction((action)->{
			if (storageOptionPane==null ) storageOptionPane=new StorageOptionsPane();
			storageOptionPane.setParams(StorageOptions.getInstance().getStorageParameters());
			Optional<?> optional=showDialog(storageOptionPane); 
			if (optional.isPresent() && optional.get()!=null){
				StorageOptions.getInstance().setStorageParameters((StorageParameters) optional.get());
			}
		});
		styleButton(storageManager);
		
		PamButton help=new PamButton("Help...");
		styleButton(help);
		
		PamButton about=new PamButton("About..."); 
		styleButton(about);
		
		PamButton tip=new PamButton("Tip of the day..."); 
		styleButton(tip);
		
		PamButton website=new PamButton("Website"); 
		styleButton(website);
		
		PamButton contact=new PamButton("Contact and Support"); 
		styleButton(contact);
		
		this.getChildren().addAll(settingsLabel,saveConfig,saveConfigAs, generalSettings, settings, storageManager, database, binaryStorage, help, about, tip, website, contact);

	}
	
	public Optional<?> showDialog(SettingsPane<?> settingsPane){
		PamSettingsDialogFX<?> settingsDialog=new PamSettingsDialogFX(settingsPane); 
		Optional<?> result=settingsDialog.showAndWait(); 
		return result;
	}
	
	public void styleButton(Labeled control){
		Insets buttonInsets=new Insets(0,leftInset,0,leftInset);
		control.setAlignment(Pos.CENTER_LEFT);
		control.setPadding(buttonInsets);
		control.getStyleClass().add("square-button-trans");
		control.setPrefWidth(Double.MAX_VALUE);
		control.setPrefHeight(40);
	}
	
	/**
	 * Open the main settings dialog for a module. 
	 * @param controlledUnit - the controlled unit to open settings for. 
	 */
	private void openSettingsDialog(PamControlledUnit controlledUnit){
		//have intentionally not put a null settings pane check in here.
		//Does any module not have a settings pane?
		if (controlledUnit.getGUI(PamGUIManager.FX)!=null){
			PamControlledGUIFX pamControlledUnit=(PamControlledGUIFX) controlledUnit.getGUI(PamGUIManager.FX);
			SettingsDialog<?> newDialog=new SettingsDialog<>(pamControlledUnit.getSettingsPane());
			newDialog.setResizable(true);
			newDialog.setOnShown((value)->{
				 pamControlledUnit.getSettingsPane().paneInitialized();
			});
			//show the dialog 
			newDialog.showAndWait().ifPresent(response -> {
				if (response!=null) pamControlledUnit.updateParams(); 
			});
			
			//notify stuff that process settings may have changed. 
			PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);
		}
	}
	
	

}
