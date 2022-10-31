package pamViewFX.fxSettingsPanes;

import java.io.File;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import PamController.PamSettingManager;
import PamController.SettingsFileData;
import PamController.SettingsPane;
import PamUtils.PamFileFilter;

/**
 * Pne which allows a user to select a .psfx settings file. 
 * @author Jamie Macaulay
 *
 */
public class SettingsFilePane extends SettingsPane<SettingsFileData> {
	
	/**
	 * Holds start up files recent files. 
	 */
	private ComboBox<String> fileBox;
	
	/**
	 * Check box to show settings dialog at start up. 
	 */
	private CheckBox alwaysShow;
	
	private SettingsFileData settingsFileData;

	private FileChooser fileChooser;
	
	private PamBorderPane mainPane = new PamBorderPane();

	public SettingsFilePane(){
		super(null);
		createSettingsLoadPane();
	}
	
	private void createSettingsLoadPane(){
		
		PamVBox vBox=new PamVBox();
		vBox.setSpacing(5);
		
		Label label=new Label("Recent Config Files");
		PamGuiManagerFX.titleFont2style(label);
//		label.setFont(PamGuiManagerFX.titleFontSize2);
		vBox.getChildren().add(label);

		//folder select and browse pane.
		PamHBox folderSelectPane=new PamHBox(); 
		folderSelectPane.setSpacing(5);
		
		fileBox=new ComboBox<String>();
				
		PamButton browseButton = new PamButton();
		//browseButton.setGraphic(Glyph.create("FontAwesome|FILE").size(22).color(Color.WHITE.darker()));
//		browseButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.FILE, Color.WHITE, PamGuiManagerFX.iconSize));
		browseButton.setGraphic(PamGlyphDude.createPamIcon("mdi2f-file", Color.WHITE, PamGuiManagerFX.iconSize));
		//browseButton.prefHeightProperty().bind(fileBox.heightProperty());
		browseButton.setOnAction((action)->{
			browseFile(false);
		});
		browseButton.prefHeightProperty().bind(fileBox.heightProperty());
		
		
		PamButton browseSaveButton = new PamButton();
//		browseSaveButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.PLUS, Color.WHITE, PamGuiManagerFX.iconSize));
		browseSaveButton.setGraphic(PamGlyphDude.createPamIcon("mdi2p-plus", Color.WHITE, PamGuiManagerFX.iconSize));
		//browseSaveButton.prefHeightProperty().bind(fileBox.heightProperty());
		browseSaveButton.setOnAction((action)->{
			browseFile(true);
		});
		folderSelectPane.getChildren().addAll(fileBox, browseSaveButton, browseButton);
		vBox.getChildren().add(folderSelectPane);
		
		browseSaveButton.prefHeightProperty().bind(fileBox.heightProperty());

		
		fileBox.prefHeightProperty().bind(browseButton.heightProperty());
		
//		//option to show on start up. 
//		Label optionLabel=new Label("Options");
//		label.setFont(PamGuiManagerFX.titleFontSize2);
//		vBox.getChildren().add(optionLabel);
		
//		alwaysShow=new CheckBox("Show on start up");
//		vBox.getChildren().add(alwaysShow);

		mainPane.setCenter(vBox);

	}

	/**
	 * Browse for settings for file.
	 */
	private void browseFile(boolean create) {
		if (fileChooser==null){
			fileChooser = new FileChooser();
		}
		fileChooser.setTitle("Open Resource File");
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("psfx files", "*.psfx"));
		//TODO - add xml files?
		
		File  selectedFile =null;
		if (create) selectedFile = fileChooser.showSaveDialog(null);
		else  selectedFile = fileChooser.showOpenDialog(null);

		if (selectedFile==null) return;

		selectedFile = PamFileFilter.checkFileEnd(selectedFile, PamSettingManager.fileEndx, true);
		settingsFileData.recentFiles.add(0, selectedFile);
		fillFileList();
		// shouldn't need this line, but it seems to make all the difference in updating
		// the list
		//fileList.setSelectedItem(newFile.getName());
		fileBox.getSelectionModel().select(0);
	}

	@Override
	public SettingsFileData getParams(SettingsFileData s) {
		File selFile = settingsFileData.recentFiles.get(fileBox.getSelectionModel().getSelectedIndex());
		
		settingsFileData.setFirstFile(selFile);
		fileBox.getSelectionModel().select(0); //reset the combobox. Need this toi make szure we don;t get weird bugs in the files used. 
		
		System.out.println("Get Params: Sel index = " + fileBox.getSelectionModel().getSelectedIndex()+ " File: "+selFile);
//		for (int i=0; i<settingsFileData.recentFiles.size(); i++){
//			System.out.println("File: "+i+ " "+settingsFileData.recentFiles.get(i));
//		}

//		settingsFileData.showFileList = alwaysShow.isSelected();
		settingsFileData.showFileList =true;
		return settingsFileData;
	}

	@Override
	public void setParams(SettingsFileData input) {
		this.settingsFileData=input.clone();
		fillFileList();
//		if (settingsFileData != null) {
////			alwaysShow.setSelected(settingsFileData.showFileList);
//			alwaysShow.setSelected(true);
//		}
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Load PAMGUARD Config";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}
	
	private void fillFileList() {
		fileBox.getItems().removeAll(fileBox.getItems());
		if (settingsFileData != null) {
			for (int i = 0; i < settingsFileData.recentFiles.size(); i++){
				fileBox.getItems().add(settingsFileData.recentFiles.get(i).getName());
			}
			if (settingsFileData.recentFiles.size() > 0) {
				fileBox.getSelectionModel().select(0);
			}
		}
	}

}
