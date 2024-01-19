package rawDeepLearningClassifier.layoutFX;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
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
 * Models could be potentially selected from 
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
	protected URI currentSelectedFile = new File(System.getProperty("user.home")).toURI();

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
	DLClassiferModel currentClassifierModel;
	
	/**
	 * The default models. 
	 */
	private DefaultModels defaultModels;
	
	/**
	 * Pop over
	 */
	private PopOver urlPopOver;

	private TextField uriTextField;

	private RawDLSettingsPane rawDLSettingsPane; 

	
	public DLModelSelectPane(RawDLSettingsPane rawDLSettingsPane) {
		this.rawDLSettingsPane=rawDLSettingsPane; 
		this.dlControl=rawDLSettingsPane.getDLControl(); 
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


			Path path = Paths.get(currentSelectedFile); 
			if(path!=null && Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
				fileChooser.setInitialDirectory(new File(new File(currentSelectedFile).getParent()));
			}
			else { 
				fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			}

			File file = fileChooser.showOpenDialog(null); 

			if (file==null) {
				return; 
			}
			
			loadNewModel(file.toURI()); 
			
     
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
	 * Load a new model on a seperate thread. 
	 * @param uri - the uri to the model. 
	 */
	public void loadNewModel(URI uri) {
	     // separate non-FX thread - load the model 
				//on a separate thread so we can show a moving load 
				//bar on the FX thread. Otherwise the GUI locks up  
				//whilst stuff is loaded. 
		if (uri==null) return; 
		
		pathLabel.setText("Loading model...");
		modelLoadIndicator.setVisible(true);
		
				new Thread() {
					// runnable for that thread
					public void run() {
						try {
							URI modelPath = dlControl.getDownloadManager().downloadModel(uri); 
							
							newModelSelected(modelPath); 
							currentSelectedFile = modelPath;
							Thread.sleep(1000); //just show the user something happened if model loading is rapid. 
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						Platform.runLater(new Runnable() {

								public void run() {
									try {
									rawDLSettingsPane.setClassifierPane(); 
									modelLoadIndicator.setVisible(false);
									updatePathLabel(); 
									}
									catch (Exception e) {
										e.printStackTrace();
									}
								}
					
						});
					}			
				}.start();
	}

	/**
	 * A new model has been selected 
	 * @param 
	 */
	private void newModelSelected(URI file) {
		if (file == null) {
			return;
		}
		
		
		
		this.currentClassifierModel = this.dlControl.getDlClassifierChooser().selectClassiferModel(file); 
		
		System.out.println("New classifier model selected!: " + currentClassifierModel); 
		if (currentClassifierModel!=null) {
			currentClassifierModel.setModel(file);
			currentClassifierModel.prepModel();
		}
		
		
	}
	
	
	/**
	 * Update the path label and tool tip text; 
	 */
	protected void updatePathLabel() {
		//System.out.println("Update path label: " + currentClassifierModel.checkModelOK()); 
		if (currentClassifierModel == null) {
			pathLabel.setText("No classifier model loaded: Select model");
			pathLabel.setTooltip(new Tooltip("Use the browse button/ URI botton to select a model or select a default model"));
		}
		
		else if (!currentClassifierModel.checkModelOK()) {
			pathLabel.setText("The model could not be loaded?");
			pathLabel.setTooltip(new Tooltip("Use the browse button/ URI botton to select a model or select a default model"));

		}
		else {
			pathLabel .setText(new File(this.currentSelectedFile).getName()); 
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
				
		
		 ArrayList<String> extensionFilters  = new  ArrayList<String>(); 
		
		 for (DLClassiferModel dlModel: dlControl.getDLModels()) {
			 if (dlModel.getModelUI()!=null) {
				 for (ExtensionFilter extFilter: dlModel.getModelUI().getModelFileExtensions()){
					 extensionFilters.addAll(extFilter.getExtensions()); 
				 }
			 }
		 }
		 
		//Now we don't really want lots of extension filters 
		 ArrayList<ExtensionFilter> dlExtFilter = new  ArrayList<ExtensionFilter>(); 		
		 dlExtFilter.add(new ExtensionFilter("Deep Learning Models", extensionFilters));
		
		return dlExtFilter ; 
	}
	
	
	

}
