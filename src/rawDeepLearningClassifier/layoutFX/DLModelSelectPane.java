package rawDeepLearningClassifier.layoutFX;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;

import org.controlsfx.control.PopOver;

import ai.djl.Device;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.geometry.Insets;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLClassiferModel;
import rawDeepLearningClassifier.dlClassification.DefaultModels;
import rawDeepLearningClassifier.dlClassification.DefaultModels.DefualtModel;


/**
 * A pane which allows users to select a model. 
 * 
 * Models could be potnetially selected from 
 * 1) A file (implemented)
 * 2) A URL (not implemented)
 * 3) A default list of models (not implemented. )
 * 
 * 
 * @author Jamie Macaulay 
 *
 */
public class DLModelSelectPane extends PamBorderPane {
	
	/**
	 * The directory chooser.
	 */
	private FileChooser fileChooser;

	/**
	 * Currently selected file.
	 */
	private File currentSelectedFile = new File(System.getProperty("user.home"));

	/**
	 * The label showing the path to the file. 
	 */
	private Label pathLabel;
	
	
	/**
	 * The label showing the path to file. 
	 */
	private ProgressIndicator modelLoadIndicator;

	/**
	 * The type of classifier selected
	 */
	private Label classiferInfoLabel;

	/**
	 * The DL control. 
	 */
	private DLControl dlControl;

	/**
	 * The current classifier model. 
	 */
	private DLClassiferModel currentClassifierModel;
	
	/**
	 * The default models. 
	 */
	private DefaultModels defaultModels;
	
	/**
	 * Pop over
	 */
	private PopOver urlPopOver;

	private TextField uriTextField; 

	
	public DLModelSelectPane(DLControl dlControl) {
		this.dlControl=dlControl; 
		this.setCenter(createDLSelectPane());
		//the directory chooser. 
		fileChooser = new FileChooser();
		fileChooser.setTitle("Classifier Model Location");
	}
	
	
	public Pane createDLSelectPane() {
		
		 defaultModels = new DefaultModels(dlControl); 
		
	
		classiferInfoLabel = new Label(" Classifier"); 
		//PamGuiManagerFX.titleFont2style(classiferInfoLabel);
		Font font= Font.font(null, FontWeight.BOLD, 11);
		classiferInfoLabel.setFont(font);

		/**Basic classifier info**/
		pathLabel = new Label("No classifier file selected"); 
//		PamButton pamButton = new PamButton("", PamGlyphDude.createPamGlyph(MaterialDesignIcon.FILE, PamGuiManagerFX.iconSize)); 
		PamButton pamButton = new PamButton("", PamGlyphDude.createPamIcon("mdi2f-file", PamGuiManagerFX.iconSize)); 
		
		modelLoadIndicator = new ProgressIndicator(-1);
		modelLoadIndicator.setVisible(false);
		modelLoadIndicator.prefHeightProperty().bind(pamButton.heightProperty().subtract(3));
		
		pamButton.setMinWidth(30);
		pamButton.setTooltip(new Tooltip("Browse to select a model file"));
		

		pamButton.setOnAction((action)->{
			
			fileChooser.getExtensionFilters().clear();
			fileChooser.getExtensionFilters().addAll(getExtensionFilters()); 


			Path path = currentSelectedFile.toPath();
			if(path!=null && Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
				fileChooser.setInitialDirectory(new File(currentSelectedFile.getParent()));
			}
			else { 
				fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			}

			File file = fileChooser.showOpenDialog(null); 

			if (file==null) {
				return; 
			}
			
			modelLoadIndicator.setVisible(true);

			pathLabel.setText("Loading model...");
			
			
            // separate non-FX thread - load the model 
			//on a separate thread so we can show a moving load 
			//bar on the FX thread. Otherwise the GUI locks up  
			//whilst stuff is loaded. 
			new Thread() {
				// runnable for that thread
				public void run() {
					try {
						newModelSelected(file.toURI()); 
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					Platform.runLater(new Runnable() {
						public void run() {
							modelLoadIndicator.setVisible(false);
							updatePathLabel(); 
						}
		
					});
				}			
			}.start();
		});
		
		
		
		
		PamVBox urlBox = new PamVBox(); 
		urlBox.setPadding(new Insets(5,5,5,5));
		urlBox.setSpacing(5);
	
		urlBox.getChildren().add(new Label("Enter the internet address (URL) for the model"));
		
		uriTextField = new TextField(); 
		urlBox.getChildren().add(uriTextField); 
		
		urlPopOver = new PopOver();
		urlPopOver.setContentNode(urlBox);
		urlPopOver.setOnHidden((action)->{
			
			
			if (uriTextField.getText().isEmpty()) return;
			//need to s
			URI uri;
			try {
				uri = new URI(uriTextField.getText());

				HttpURLConnection huc = (HttpURLConnection) uri.toURL().openConnection();

				int responseCode = huc.getResponseCode();

				if (HttpURLConnection.HTTP_OK == responseCode) {

					newModelSelected(uri);
				}
				else {
					PamDialogFX.showWarning("The URL could not be found"); 
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
		});

		
		
		PamButton urlButton = new PamButton("", PamGlyphDude.createPamIcon("mdi2l-link-variant", PamGuiManagerFX.iconSize)); 
		urlButton.setTooltip(new Tooltip("Load a model from a URL"));
		urlButton.setOnAction((action)->{
			urlPopOver.show(urlButton);
		});

		
		MenuButton defaults = new MenuButton(); 
		defaults.setTooltip(new Tooltip("Default models"));
		defaults.setGraphic(PamGlyphDude.createPamIcon("mdi2d-dots-vertical", PamGuiManagerFX.iconSize)); 
		
		for (DefualtModel defaultmodel: defaultModels.getDefaultModels()) {
			defaults.getItems().add(new MenuItem(defaultmodel.name)); 
		}		
		defaults.prefHeightProperty().bind(urlButton.heightProperty());
	
		
		PamHBox hBox = new PamHBox(); 
		hBox.setSpacing(5);
		hBox.getChildren().addAll(modelLoadIndicator, pathLabel, urlButton, pamButton, defaults); 
		hBox.setAlignment(Pos.CENTER_RIGHT);
		
		return hBox; 
	}

	/**
	 * A new model has been selected 
	 * @param 
	 */
	private void newModelSelected(URI file) {
		this.currentClassifierModel = this.dlControl.getDlClassifierChooser().selectClassiferModel(file); 
		if (currentClassifierModel==null) {
			currentClassifierModel.setModel(file);
		}
	}
	
	
	/**
	 * Update the path label and tool tip text; 
	 */
	private void updatePathLabel() {
		if (currentClassifierModel == null ? true : !currentClassifierModel.checkModelOK()) {
			pathLabel.setText("No classifier model loaded: Select model");
			pathLabel.setTooltip(new Tooltip("Use the browse button/ URI botton to select a model or select a default model"));

		}
		else {
			pathLabel .setText(this.currentSelectedFile.getName()); 
			try {
				pathLabel.setTooltip(new Tooltip(this.currentSelectedFile.getPath() 
						+ "\n" +" Processor CPU " + Device.cpu() + "  " +  Device.gpu()));
			}
			catch (Exception e) {
				//sometimes get an error here for some reason
				//does not make a difference other than tooltip. 
				System.err.println("StandardModelPane: Error getting the default device!");
			}
		}

	}

	/**
	 * Get the extension filters for the file selection dialog. 
	 * @return the extension filters for the model file dialog. 
	 */

	public ArrayList<ExtensionFilter> getExtensionFilters() {
		
		
		
		return null; 
	}
	
	
	

}
