package pamViewFX;

import java.io.File;
import java.util.Optional;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.SettingsPane;
import PamController.StorageOptions;
import PamController.StorageParameters;
import PamController.soundMedium.GlobalMedium;
import PamController.soundMedium.GlobalMedium.SoundMedium;
import PamModel.PamModuleInfo;
import PamUtils.PamFileFilter;
import binaryFileStorage.BinaryStore;
import dataModelFX.connectionNodes.ModuleIconFactory;
import generalDatabase.DBControlUnit;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.pamDialogFX.PamSettingsDialogFX;
import pamViewFX.fxNodes.utilityPanes.SettingsDialog;
import pamViewFX.fxSettingsPanes.StorageOptionsPane;
import pamViewFX.fxStyles.PamStylesManagerFX;

/**
 * Pane which holds settings menu. All primary settings are accessed from this pane which sits in a hiding pane to the right 
 * of the main GUI. 
 * 
 * @author Jamie Macaulay
 *
 */
public class PamSettingsMenuPane extends PamVBox {
	
	private double leftInset=60;
	private double heightSpacer=2; 
	
	StorageOptionsPane storageOptionPane; 
	
	public String warning =  "	Changing to between air and water requires a PAMGuard restart\n"
			+ "	for some display changes to take effect. Settings such as\n"
			+ " sound speed, reciever sensitivity values and data unit amplitudes\n"
			+ " will be recalculated or set to new default values.\n"
			+ " <p>Data processing changes are ready to use immediately."; 
	
	public PamSettingsMenuPane(){
		
		this.setSpacing(0);
				
		this.setPrefWidth(250);
		
		this.setStyle("-fx-background-color: -fx-darkbackground");
		
		this.getStylesheets().addAll(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getSlidingDialogCSS());

		Label settingsLabel=new Label("Settings");
		settingsLabel.setPadding(new Insets(14,0,10,0));
		settingsLabel.setAlignment(Pos.CENTER);
		settingsLabel.setPrefWidth(Double.MAX_VALUE);
		settingsLabel.setTextAlignment(TextAlignment.LEFT);
		settingsLabel.setStyle("-fx-font-weight: bold;");

		PamButton saveConfig=new PamButton("Save");
		saveConfig.setOnAction((action)->{
			PamSettingManager.getInstance().saveSettings(null);
		});
		styleButton(saveConfig);
		saveConfig.setGraphic(PamGlyphDude.createPamIcon("mdi2c-content-save-outline", 
				PamGuiManagerFX.iconSize));
		
		PamButton saveConfigAs=new PamButton("Save as..."); 
		saveConfigAs.setOnAction((action)->{
			saveSettingsAs();
		});
		styleButton(saveConfigAs);
		saveConfigAs.setGraphic(PamGlyphDude.createPamIcon("mdi2c-content-save-move-outline", 
			 PamGuiManagerFX.iconSize));
		
		//Air or water mode
		ToggleButton toggleButton1 = new ToggleButton("Water");
		toggleButton1.setPrefWidth(60);
		toggleButton1.setTooltip(new Tooltip(GlobalMedium.getToolTip(SoundMedium.Water))); 
		toggleButton1.setOnAction((action)->{
			if (PamController.getInstance().getGlobalMediumManager().getGlobalMediumParameters().currentMedium==SoundMedium.Water) return; //do nothing. 
				PamController.getInstance().getGlobalMediumManager().setCurrentMedium(SoundMedium.Water);
		});
		

	    ToggleButton toggleButton2 = new ToggleButton("Air");
	    toggleButton2.setPrefWidth(60);
	    toggleButton2.setTooltip(new Tooltip(GlobalMedium.getToolTip(SoundMedium.Air))); 
	    toggleButton2.setOnAction((action)->{
			if (PamController.getInstance().getGlobalMediumManager().getGlobalMediumParameters().currentMedium==SoundMedium.Air) return; //do nothing. 
			PamController.getInstance().getGlobalMediumManager().setCurrentMedium(SoundMedium.Air);
		});

	    ToggleGroup toggleGroup = new ToggleGroup();
	    toggleButton1.setToggleGroup(toggleGroup);
	    toggleButton2.setToggleGroup(toggleGroup);
	    toggleGroup.selectToggle(toggleButton1);

	    Label mediumLabel = new Label("Sound Medium"); 
	    mediumLabel.setAlignment(Pos.CENTER_LEFT);
	    mediumLabel.setPadding(new Insets(0,0,0,15));
	    mediumLabel.setGraphic(PamGlyphDude.createPamIcon("mdi2w-waves", 
			 PamGuiManagerFX.iconSize));
		//styleButton(mediumLabel);

	    
	    PamHBox toggleButtonBox = new PamHBox(); 
	    toggleButtonBox.setAlignment(Pos.CENTER_RIGHT);
	    toggleButtonBox.setSpacing(5);
	    toggleButtonBox.getChildren().addAll(toggleButton1, toggleButton2); 
	    toggleButtonBox.setMaxWidth(Double.MAX_VALUE);
	    
	    PamHBox mediumToggleBox = new PamHBox(mediumLabel,toggleButtonBox);
	    HBox.setHgrow(toggleButtonBox, Priority.ALWAYS);
	    mediumToggleBox.setAlignment(Pos.CENTER_LEFT);
	    mediumToggleBox.setSpacing(5);
	    mediumToggleBox.setPadding(new Insets(0,5,0,0));

		
		PamButton generalSettings=new PamButton("General Settings..."); 
		styleButton(generalSettings);
		generalSettings.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog", 
					 PamGuiManagerFX.iconSize));
		
		MenuButton settings=new MenuButton("Module Settings"); 
		settings.setPopupSide(Side.RIGHT);
		settings.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cogs", 
				 PamGuiManagerFX.iconSize));
//		settings.setStyle("-fx-background-radius: 0;"
//				+ " -fx-border-color: transparent; -fx-padding: 0 0 0 0;");
		
		styleButton(settings);
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
				
//				Node icon = ModuleIconFactory.getInstance().getModuleNode(PamController.getInstance().getControlledUnit(i).getPamModuleInfo().getClassName());
//				icon.setScaleX(0.25);
//				icon.setScaleY(0.25);
//				menuItem.setGraphic(icon);
				
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
		database.setGraphic(PamGlyphDude.createPamIcon("mdi2d-database", 
				 PamGuiManagerFX.iconSize));

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
		binaryStorage.setGraphic(PamGlyphDude.createPamIcon("mdi2f-file-table", 
				 PamGuiManagerFX.iconSize));

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
		storageManager.setGraphic(PamGlyphDude.createPamIcon("mdi2d-database-cog", 
				 PamGuiManagerFX.iconSize));		
		
		PamButton help=new PamButton("Help...");
		styleButton(help);
		help.setGraphic(PamGlyphDude.createPamIcon("mdi2h-help-circle-outline", 
				 PamGuiManagerFX.iconSize));	
		
		PamButton about=new PamButton("About..."); 
		styleButton(about);
		about.setGraphic(PamGlyphDude.createPamIcon("mdi2i-information-outline", 
				 PamGuiManagerFX.iconSize));
//		PamButton tip=new PamButton("Tip of the day..."); 
//		styleButton(tip);
		
		PamButton website=new PamButton("Website"); 
		styleButton(website);
		website.setGraphic(PamGlyphDude.createPamIcon("mdi2e-earth", 
				 PamGuiManagerFX.iconSize));
		
		PamButton contact=new PamButton("Found a bug?"); 
		styleButton(contact);
		contact.setGraphic(PamGlyphDude.createPamIcon("mdi2b-bug", 
				 PamGuiManagerFX.iconSize));
		
		PamButton checkForUpdates=new PamButton("Check for updates"); 
		styleButton(checkForUpdates);
		checkForUpdates.setGraphic(PamGlyphDude.createPamIcon("mdi2r-refresh", 
				 PamGuiManagerFX.iconSize));
		
		this.getChildren().addAll(settingsLabel,saveConfig,saveConfigAs, new Separator(),  mediumToggleBox, generalSettings, settings, new Separator(), 
				storageManager, database, binaryStorage, new Separator(), help, checkForUpdates, website, contact, about);

	}
	
	public Optional<?> showDialog(SettingsPane<?> settingsPane){
		PamSettingsDialogFX<?> settingsDialog=new PamSettingsDialogFX(settingsPane); 
		Optional<?> result=settingsDialog.showAndWait(); 
		return result;
	}
	
	public void styleButton(Labeled control){
		Insets buttonInsets=new Insets(0,leftInset,0,leftInset);
		control.setAlignment(Pos.CENTER_LEFT);
		control.setStyle("-fx-alignment: center-left;");
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
	
	
	/**
	 * Save settings to a new psf file.
	 * @param frame parent frame for dialog.
	 */
	public void saveSettingsAs() {
		/*
		 * get a new file name, set that as the current file
		 * then write all settings to it.
		 */
		File file = null;
		String currentfileName  = PamSettingManager.getInstance().getSettingsFileName();
		if (currentfileName != null) {
			file =new File(currentfileName);
		}
		
		FileChooser fileChooser = new FileChooser();
		 fileChooser.setTitle("Open Resource File");
		 fileChooser.getExtensionFilters().addAll(
		         new ExtensionFilter("PAMGuard settings files", "*.xml", "*.psfx"));
		 if (currentfileName!=null) {
			 fileChooser.setInitialDirectory(file);
		 }
		 
		 File selectedFile = fileChooser.showOpenDialog(this.getScene().getWindow());
		 
		 if (selectedFile == null) {
		    return;
		 }
		 
		 selectedFile = PamFileFilter.checkFileEnd(selectedFile, PamSettingManager.getCurrentSettingsFileEnd(), true);


		System.out.println("Saving settings to file " + selectedFile.getAbsolutePath());

		// Insert the new file into the top of the recent psf file list.  Also check
		// if we are running remotely, which probably means the user double-clicked on
		// a psf to start Pamguard.  In that case, change the remotePSF pointer to
		// the new file as well
		PamSettingManager.getInstance().setDefaultFile(selectedFile.getAbsolutePath());
		
		if (PamSettingManager.remote_psf != null) {
			PamSettingManager.remote_psf = selectedFile.getAbsolutePath();
		}

		PamSettingManager.getInstance().saveSettings(PamSettingManager.SAVE_PSF);

		PamController.getInstance().getGuiFrameManager().sortFrameTitles();

	}
	
}
