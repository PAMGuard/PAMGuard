package pamViewFX.fxNodes.utilityPanes;

import java.io.File;
import java.util.ArrayList;

import org.controlsfx.glyphfont.Glyph;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import PamController.PamController;
import PamUtils.FileFunctions;
import PamUtils.FolderChangeListener;

/**
 * Creates a pane which allows users to select a folder.
 * @author Jamie Macaulay
 *
 */
public class SelectFolderFX extends PamBorderPane{
	
	private int textLength = 50;
	private String borderLabel;
	private PamButton browseButton;
	private TextField folderName;
	private CheckBox includeSubFoldersCheckBox;
	private boolean showSubFolderOption = false;
	private ArrayList<FolderChangeListener> folderChangeListeners = new ArrayList<FolderChangeListener>();
	private DirectoryChooser directoryChooser=new DirectoryChooser(); 


	public SelectFolderFX(String borderLabel, int textLength, boolean showSubFolderOption) {
		this.showSubFolderOption = showSubFolderOption;
		createPanel(borderLabel, textLength);
	}

	public SelectFolderFX(String borderLabel, int textLength) {
		createPanel(borderLabel, textLength);
	}

	public SelectFolderFX(int textLength) {
		createPanel("Select Folder", textLength);
	}

	public void createPanel(String borderLabel, int textLength) {
		this.textLength = textLength;
		this.borderLabel = borderLabel;
		
		PamVBox mainPane=new PamVBox();
		mainPane.setSpacing(5);
		
		//title
		Label titleLabel;
		mainPane.getChildren().add(titleLabel=new Label(borderLabel));
		PamGuiManagerFX.titleFont2style(titleLabel);

//		titleLabel.setFont(PamGuiManagerFX.titleFontSize2);
		
		//folder select and browse pane.
		PamHBox folderSelectPane=new PamHBox(); 
		folderSelectPane.setSpacing(5);
		browseButton = new PamButton();
		browseButton.setGraphic(Glyph.create("FontAwesome|FOLDER").size(22).color(Color.WHITE.darker()));
		browseButton.setOnAction((action)->{
			browseDirectories();
		});
		
		folderName=new TextField();
		folderName.setEditable(true);
		PamHBox.setHgrow(folderName, Priority.ALWAYS);
		folderName.prefHeightProperty().bind(browseButton.heightProperty()); //text filed and browse button should be same height
		
		folderSelectPane.getChildren().addAll(folderName, browseButton);
		mainPane.getChildren().add(folderSelectPane);
		
		//include sub folders pane.
		includeSubFoldersCheckBox=new CheckBox("Include Sub Folders");
		includeSubFoldersCheckBox.setOnAction((action)->{
			notifyChangeListeners();
		});
		mainPane.getChildren().add(includeSubFoldersCheckBox);
		
		this.setCenter(mainPane);
		
	}
	
	
	
	private void browseDirectories() {

		configureFolderChooser(directoryChooser);
		File initialDir=new File(folderName.getText());
		if (initialDir.isDirectory()) directoryChooser.setInitialDirectory(new File(folderName.getText()));
//		else directoryChooser.setInitialDirectory(System.getProperties().); //TODO - system docs directory.

		File folder=directoryChooser.showDialog(PamController.getMainStage());

		if (folder!=null) {
			folderName.setText(folder.getPath());
			notifyChangeListeners();
		}

	}
		
	private void configureFolderChooser(DirectoryChooser directoryChooser2) {
		directoryChooser.setTitle("Select Folder");

	}
	
	public void setEnabled(boolean enable) {
		browseButton.setDisable(!enable);
		includeSubFoldersCheckBox.setDisable(!enable);
	}


	private void notifyChangeListeners() {
		String newName = getFolderName(false);
		for (int i = 0; i < folderChangeListeners.size(); i++) {
			folderChangeListeners.get(i).folderChanged(newName, isIncludeSubFolders());
		}
	}

	/**
	 * Set the state of the sub folder check box.
	 * @param includeSubfolders true to check the box.
	 */
	public void setIncludeSubFolders(boolean includeSubfolders) {
		includeSubFoldersCheckBox.setSelected(includeSubfolders);
	}

	/**
	 * Get the state of the sub folder check box.
	 * @return true if selected
	 */
	public boolean isIncludeSubFolders() {
		return includeSubFoldersCheckBox.isSelected();
	}

	/**
	 * Add a folder change listener to receive notification if the 
	 * browse button was used to select a new folder. 
	 * @param folderChangeListener change listener
	 */
	public void addFolderChangeListener(FolderChangeListener folderChangeListener) {
		folderChangeListeners.add(folderChangeListener);
	}

	/**
	 * Remove a folder change listener
	 * @param folderChangeListener
	 */
	public void removeFolderChangeListener(FolderChangeListener folderChangeListener) {
		folderChangeListeners.remove(folderChangeListener);
	}

	/**
	 * Get the folder name and optionally check and create the
	 * path for data storage. 
	 * @param checkPath set true to check / create the storage path.
	 * @return Path string, or null if path check fails. 
	 */
	public String getFolderName(boolean checkPath) {
		String folder = folderName.getText();
		File f = new File(folder);
		if (checkPath) {
			if (f.exists() == false) {
				if (PamDialogFX.showMessageDialog("Select Directory", "The directory " + folder + " does not exist " +
						"do you want to create it ?")){
					if (f.mkdirs() == false) {
						// print a warning message
						PamDialogFX.showWarning(PamController.getMainStage(), "Error", "The folder " + folder + " could not be created");
					}
				}
							
//				if (JOptionPane.showConfirmDialog(folderPanel, "The directory " + folder + " does not exist " +
//						"do you want to create it ?", "Select Directory", JOptionPane.YES_NO_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
//					if (f.mkdirs() == false) {
//						// print a warning message
//						JOptionPane.showMessageDialog(folderPanel, "The folder " + folder + " could not be created", 
//								"Error", JOptionPane.ERROR_MESSAGE);
//					}
//				}
			}
			if (f.exists() == false) return null;
			FileFunctions.setNonIndexingBit(f);
		}
		return folder;
	}

	public void setFolderName(String folderName) {
		if (folderName != null) {
			this.folderName.setText(folderName);
		}
		else {
			this.folderName.setText("");
		}
	}

	public void setTextLength(int textLength) {
		this.textLength = textLength;
		folderName.setPrefColumnCount(textLength);
	}

//	public boolean isShowSubFolderOption() {
//		return showSubFolderOption;
//	}
//
//	
//	public void setShowSubFolderOption(boolean showSubFolderOption) {
//		this.showSubFolderOption = showSubFolderOption;
//		setVisibleControls();
//	}

	private void setVisibleControls() {
		includeSubFoldersCheckBox.setVisible(showSubFolderOption);
	}

}
