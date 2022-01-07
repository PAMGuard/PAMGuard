package rawDeepLearningClassifier.dlClassification.genericModel;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamHBox;

/**
 * Pane for importing an exporting .pgtf settings files. 
 * @author Jamie Macaulay 
 *
 */
public abstract class ImportExportPane extends PamHBox {
	

	/**
	 * Currently selected .pgtf file.
	 */
	private File currentSettingsFile = new File(System.getProperty("user.home"));
	
	/**
	 * The file chooser. 
	 */
	private FileChooser fileChooser;
	
	
	
	public ImportExportPane() {
		fileChooser = new FileChooser();  
		//open a settings file. 
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Deep Learning Model Parameters", "*.pdtf"));
		
//		ArrayList<ExtensionFilter> extensionFilters = new ArrayList<ExtensionFilter>(); 
//		extensionFilters.add(new ExtensionFilter("Deep Learning Settings File", "*.pdtf")); 
//		
		
		this.setSpacing(5);
		this.setAlignment(Pos.CENTER_LEFT);
		this.getChildren().addAll( createSettingsImportPane(), createSettingsExportFile()); 
	}
		
	

	/**
	 * Create the setting import pane 
	 * @return
	 */
	public Pane createSettingsImportPane() {
		
		//import the settings holder. 
		PamHBox importSettingsHolder = new PamHBox(); 
		importSettingsHolder.setSpacing(5);
		
		
		//import the settings holder.
		Button button = new Button("Import"); 
//		button.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.FILE_XML, PamGuiManagerFX.iconSize));
		button.setGraphic(PamGlyphDude.createPamIcon("mdi2f-file-excel", PamGuiManagerFX.iconSize));
		button.setOnAction((action)->{

			Path path = currentSettingsFile.toPath();
			
			if(path!=null && Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
				fileChooser.setInitialDirectory(new File(currentSettingsFile.getParent()));
			}
			else { 
				fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			}

			File file = fileChooser.showOpenDialog(null); 

			if (file==null) {
				return; 
			}

			importSettingFile(file); 
			
		});
	
		//importSettingsHolder.getChildren().add(new Label("Import settings")); 
		importSettingsHolder.getChildren().add(button); 
		importSettingsHolder.setAlignment(Pos.CENTER_RIGHT);
		
		return importSettingsHolder; 
	}

	
	/**
	 * Create the setting import pane 
	 * @return
	 */
	public Pane createSettingsExportFile() {
		
		//import the settings holder. 
		PamHBox importSettingsHolder = new PamHBox(); 
		importSettingsHolder.setSpacing(5);
		
		
		//import the settings holder.
		Button button = new Button("Export"); 
//		button.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.FILE_EXPORT, PamGuiManagerFX.iconSize));
		button.setGraphic(PamGlyphDude.createPamIcon("mdi2f-file-export", PamGuiManagerFX.iconSize));
		button.setOnAction((action)->{

			Path path = currentSettingsFile.toPath();
			
			if(path!=null && Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
				fileChooser.setInitialDirectory(new File(currentSettingsFile.getParent()));
			}
			else { 
				fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			}

			File file = fileChooser.showSaveDialog(null); 

			if (file==null) {
				return; 
			}

			exportSettings(file); 
			
		});
	
		//importSettingsHolder.getChildren().add(new Label("Export settings")); 
		importSettingsHolder.getChildren().add(button); 
		importSettingsHolder.setAlignment(Pos.CENTER_RIGHT);
		
		return importSettingsHolder; 
	}



	public abstract void exportSettings(File file);



	public abstract void importSettingFile(File file);

}
