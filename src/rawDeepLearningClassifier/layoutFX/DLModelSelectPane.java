package rawDeepLearningClassifier.layoutFX;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.controlsfx.control.PopOver;

import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
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
import pamViewFX.fxNodes.PamTextField;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.DLStatus;
import rawDeepLearningClassifier.dlClassification.DLClassiferModel;
import rawDeepLearningClassifier.dlClassification.DefaultModels;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.layoutFX.defaultModels.DefaultModelPane;


/**
 * A pane which allows users to select a model and then loads the model on 
 * on a different thread, showing a progress indicator. 
 * 
 * Models could be potentially selected from 
 * 1) A file (implemented)
 * 2) A URL (not implemented)
 * 3) A default list of models. 
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

	private DLSettingsPane rawDLSettingsPane;

	private DefaultModelPane defaultModelPane; 


	public DLModelSelectPane(DLSettingsPane rawDLSettingsPane) {
		this.rawDLSettingsPane=rawDLSettingsPane; 
		this.dlControl=rawDLSettingsPane.getDLControl(); 
		this.setCenter(createDLSelectPane());
		//the directory chooser. 
		fileChooser = new FileChooser();
		fileChooser.setTitle("Classifier Model Location");
	}


	public Pane createDLSelectPane() {


		classiferInfoLabel = new Label(" Classifier"); 
		//PamGuiManagerFX.titleFont2style(classiferInfoLabel);
		Font font= Font.font(null, FontWeight.BOLD, 11);
		classiferInfoLabel.setFont(font);

		/**Basic classifier info**/
		pathLabel = new Label("No classifier file selected"); 
		//		PamButton pamButton = new PamButton("", PamGlyphDude.createPamGlyph(MaterialDesignIcon.FILE, PamGuiManagerFX.iconSize)); 
		PamButton pamButton = new PamButton("", PamGlyphDude.createPamIcon("mdi2f-file", PamGuiManagerFX.iconSize));
		pamButton.getStyleClass().add("icon-button");
		pathLabel.setMinWidth(100);

		
		modelLoadIndicator = new ProgressIndicator(-1);
		modelLoadIndicator.setVisible(false);
		modelLoadIndicator.prefHeightProperty().bind(pamButton.heightProperty().subtract(3));

		pamButton.setMinWidth(30);
		pamButton.setTooltip(new Tooltip("Load a model from a file"));


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


		PamHBox urlBox = new PamHBox(); 
		urlBox.setSpacing(5);
		
		PamButton download = new PamButton();
		download.getStyleClass().add("icon-button");
		download.setTooltip(new Tooltip("Download the model from set URL"));
		download.setGraphic(PamGlyphDude.createPamIcon("mdi2d-download", PamGuiManagerFX.iconSize));
		download.setDisable(true);
		
		download.setOnAction((action)->{

			if (uriTextField.getText().isEmpty()) return;

			try {
				URI uri = new URI(uriTextField.getText());

				loadNewModel(uri);

			} catch (Exception e) {
				PamDialogFX.showWarning("A valid URL could not be created"); 
				e.printStackTrace();
			} 

		});
		
		final String urlHelpText = "Enter the internet address (URL) for the model"; 

		uriTextField = new PamTextField();
		uriTextField.prefHeightProperty().bind(download.heightProperty());
		uriTextField.setPrefWidth(300);
		uriTextField.setText("Enter the internet address (URL) for the model");
		uriTextField.textProperty().addListener((obsVal, oldVal, newVal)->{
			if (uriTextField.textProperty().get().isBlank() || uriTextField.textProperty().get().equals(urlHelpText)) {
				download.setDisable(true);
			}
			else {
				download.setDisable(false);
			}
		});
		
		urlBox.getChildren().addAll(uriTextField, download);
		
		PamVBox urlHolder = new PamVBox(); 
		urlHolder.setPadding(new Insets(5,5,5,5));
		urlHolder.setSpacing(5);
		urlHolder.getChildren().addAll(new Label("Download Model"), urlBox);

		urlPopOver = new PopOver();
		urlPopOver.setContentNode(urlHolder);
		
		PamButton urlButton = new PamButton("", PamGlyphDude.createPamIcon("mdi2l-link-variant", PamGuiManagerFX.iconSize)); 
		urlButton.getStyleClass().add("icon-button");
		urlButton.setTooltip(new Tooltip("Load a model from a URL"));
		urlButton.setOnAction((action)->{
			urlPopOver.show(urlButton);
		});


		PamButton defaults = new PamButton(); 
		defaults.getStyleClass().add("icon-button");
		defaults.setTooltip(new Tooltip("Default models"));
		defaults.setGraphic(PamGlyphDude.createPamIcon("mdi2d-dots-vertical", PamGuiManagerFX.iconSize)); 
		defaults.setTooltip(new Tooltip("Load a default model"));

//		for (DefualtModel defaultmodel: defaultModels.getDefaultModels()) {
//			defaults.getItems().add(new MenuItem(defaultmodel.name)); 
//		}		
//		defaults.prefHeightProperty().bind(urlButton.heightProperty());
		
		defaultModelPane = new DefaultModelPane(this.dlControl.getDefaultModelManager()); 
		defaultModelPane.setPadding(new Insets(5,5,5,5));
		defaultModelPane.defaultModelProperty().addListener((obsVal, oldVal, newVal)->{
			if (newVal!=oldVal) {
				//a default model needs to be loaded. 
				Task<DLStatus> task = loadNewModel(newVal.getModelURI());
				
				task.setOnSucceeded((val)->{
					if (currentClassifierModel!=null) {
						
						//set the parameters from the classifer model to have correct
						//transfroms etc. 
						newVal.setParams(currentClassifierModel.getDLModelSettings());
						
						//set the correct classifier pane. 
						rawDLSettingsPane.setClassifierPane(); 
						
						//need to make sure the classifier pane is updated
						currentClassifierModel.getModelUI().setParams();
						
						//need to update the segment length...
						if (currentClassifierModel.getDLModelSettings() instanceof StandardModelParams) {
							double segLen = ((StandardModelParams) currentClassifierModel.getDLModelSettings()).defaultSegmentLen;
							this.rawDLSettingsPane.setSegmentLength(segLen);
							//set to half the hop size too
							this.rawDLSettingsPane.setHopLength(segLen/2);
						}
						
					}
					else {
						//this should never happen unless there is no internet
						System.err.println("Default model failed ot load: "+ newVal);
					}
				});
			}
		});
		
		PopOver defualtModelPopOver = new PopOver();
		defualtModelPopOver.setContentNode(defaultModelPane);
		
		defaults.setOnAction((action)->{
			defualtModelPopOver.show(defaults);
		});

		PamHBox hBox = new PamHBox(); 
		hBox.setSpacing(5);
		hBox.getChildren().addAll(modelLoadIndicator, pathLabel, urlButton, pamButton, defaults); 
		hBox.setAlignment(Pos.CENTER_RIGHT);
		PamHBox.setHgrow(pathLabel, Priority.ALWAYS);

		return hBox; 
	}

	/**
	 * Load a new model on a seperate thread. 
	 * @param uri - the uri to the model. 
	 */
	public Task<DLStatus> loadNewModel(URI uri) {
		// separate non-FX thread - load the model 
		//on a separate thread so we can show a moving load 
		//bar on the FX thread. Otherwise the GUI locks up  
		//whilst stuff is loaded. 
		if (uri==null) return null; 

//		pathLabel.setText("Loading model...");
		modelLoadIndicator.setVisible(true);

		Task<DLStatus> task = new LoadTask(uri);
		
		modelLoadIndicator.progressProperty().bind(task.progressProperty());
		
		pathLabel.setGraphic(null); //remove error icon if there is one. 
		pathLabel.textProperty().bind(task.messageProperty());


		//start the load taks. 
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
		
		return task;



		//		   Thread th = new Thread(task);
		//	         th.setDaemon(true);
		//	         th.start();
		//		
		//				new Thread() {
		//					// runnable for that thread
		//					public void run() {
		//						try {
		//							
		//							//either downloads the model or instantly returns the model path. 
		//							URI modelPath = dlControl.getDownloadManager().downloadModel(uri); 
		//							
		//							newModelSelected(modelPath); 
		//							currentSelectedFile = modelPath;
		//							Thread.sleep(1000); //just show the user something happened if model loading is rapid. 
		//						} catch (InterruptedException e) {
		//							// TODO Auto-generated catch block
		//							e.printStackTrace();
		//						}
		//
		//						Platform.runLater(new Runnable() {
		//
		//								public void run() {
		//									try {
		//									rawDLSettingsPane.setClassifierPane(); 
		//									modelLoadIndicator.setVisible(false);
		//									updatePathLabel(); 
		//									}
		//									catch (Exception e) {
		//										e.printStackTrace();
		//									}
		//								}
		//					
		//						});
		//					}			
		//				}.start();
	}


	/**
	 * A new model has been selected 
	 * @param the load status of the model. 
	 */
	private DLStatus newModelSelected(URI file) {

		if (file == null) {
			currentClassifierModel=null;
			return DLStatus.FILE_NULL;
		}

		this.currentClassifierModel = this.dlControl.getDlClassifierChooser().selectClassiferModel(file); 

		System.out.println("New classifier model selected!: " + currentClassifierModel); 
		if (currentClassifierModel!=null) {
			
			try {
			
				File fileChck = new File(file);

	            if (!fileChck.exists()) {
					return DLStatus.MODEL_FILE_EXISTS;
	            }
				
				//we are loading model from a file - anything can happen so put in a try catch. 				
				DLStatus status = currentClassifierModel.setModel(file);

				if (status.isError()) {
					System.err.println("Model load failed: " + status);
					currentClassifierModel=null;
					return status;
				}
				
				return DLStatus.MODEL_LOAD_SUCCESS;
			}
			catch (Exception e) {
				e.printStackTrace();
				currentClassifierModel=null;
				return DLStatus.MODEL_LOAD_FAIL;
			}
		}
		else {
			currentClassifierModel=null;
			return DLStatus.MODEL_LOAD_FAIL;
		}
	}
	
	private void showWarningDialog(DLStatus status) {
		this.rawDLSettingsPane.showWarningDialog(status);
//		 PamDialogFX.showError(status.getName(), status.getDescription()); 
	}

	
	/**
	 * Create an error icon based on the status message. 
	 * @param status - the status
	 * @return error icon or null of the status is not an error. 
	 */
	private Node createErrorIcon(DLStatus status) {
		Node decoration = null; 
		if (status.isError()){
			 decoration = PamGlyphDude.createPamIcon("mdi2c-close-circle-outline", Color.RED, 10);
		}
		else if (status.isWarning()) {
			 decoration = PamGlyphDude.createPamIcon("mdi2c-close-circle-outline", Color.ORANGE, 10);			
		}
		return decoration;
	}

	/**
	 * Update the path label and tool tip text
	 */
	protected void updatePathLabel(DLStatus status) {
		
		/**
		 * This a bit complicated. We want the label to show errors if they have occured and warning to prompt the 
		 * user to do things. The sequence of this is quite important to get right. 
		 */
		
		pathLabel.setGraphic(null);

		if (currentClassifierModel == null) {
			//no frameowrk could be selected for the model. 
			pathLabel.setGraphic(createErrorIcon(DLStatus.NO_MODEL_LOADED));
			pathLabel.setText("No classifier model loaded: Select model");
			pathLabel.setTooltip(new Tooltip("Use the browse button/ URI botton to select a model or select a default model"));
		}
		else if (currentClassifierModel.getModelStatus().isError()) {
			pathLabel.setGraphic(createErrorIcon(currentClassifierModel.getModelStatus()));
			pathLabel.setText(currentClassifierModel.getModelStatus().getName());
			pathLabel.setTooltip(new Tooltip(currentClassifierModel.getModelStatus().getDescription()));
		}
		else if (status.isError()) {
			pathLabel.setGraphic(createErrorIcon(status));
			pathLabel.setText(status.getName());
			pathLabel.setTooltip(new Tooltip(status.getDescription()));
		}
		else {
			pathLabel .setText(new File(this.currentSelectedFile).getName());
			//show a warning icon if needed, and a rich tooltip explaining which
			//processor (CPU, graphics card, Apple Silicon) the model will run on.
			pathLabel.setTooltip(createProcessorTooltip(status));
		}

	}

	/**
	 * Create a rich tooltip for the model path label. The tooltip clearly shows
	 * the user which processor the deep learning model will run on (e.g. a CPU, a
	 * graphics card or an Apple Silicon chip) along with the deep learning engine,
	 * the model path and any warnings.
	 *
	 * @param status the current model load status.
	 * @return a tooltip describing the model and its processor.
	 */
	private Tooltip createProcessorTooltip(DLStatus status) {

		DLProcessorInfo procInfo = DLProcessorInfo.getCurrentProcessorInfo();

		PamVBox content = new PamVBox();
		content.setSpacing(4);
		content.setPadding(new Insets(8));
		content.setMaxWidth(360);
		//self contained dark panel so the tooltip looks the same in light and dark themes.
		content.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 6;");

		//header: processor icon + friendly processor name.
		Node icon = PamGlyphDude.createPamIcon(procInfo.getIconString(), procInfo.getAccentColor(), 24);
		Label procLabel = new Label("Runs on: " + procInfo.getFriendlyName());
		procLabel.setFont(Font.font(null, FontWeight.BOLD, 13));
		procLabel.setTextFill(Color.WHITE);
		PamHBox header = new PamHBox();
		header.setSpacing(8);
		header.setAlignment(Pos.CENTER_LEFT);
		header.getChildren().addAll(icon, procLabel);
		content.getChildren().add(header);

		//longer description of what the processor is doing.
		if (procInfo.getDetail() != null && !procInfo.getDetail().isEmpty()) {
			content.getChildren().add(tooltipDetailLabel(procInfo.getDetail(), Color.web("#E0E0E0")));
		}

		//deep learning engine, version and raw device.
		String engineText = "Engine: " + procInfo.getEngineDescription();
		if (procInfo.getDeviceString() != null && !procInfo.getDeviceString().isEmpty()) {
			engineText += "   Device: " + procInfo.getDeviceString();
		}
		content.getChildren().add(tooltipDetailLabel(engineText, Color.web("#9E9E9E")));

		//the model file path.
		content.getChildren().add(tooltipDetailLabel("Model: " + this.currentSelectedFile.getPath(),
				Color.web("#9E9E9E")));

		//any warning from the load status.
		if (status != null && status.isWarning()) {
			content.getChildren().add(tooltipDetailLabel("Warning: " + status.getDescription(),
					Color.web("#FFB74D")));
		}

		Tooltip tooltip = new Tooltip();
		//let the content pane draw the background so styling is consistent across themes.
		tooltip.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
		tooltip.setGraphic(content);
		tooltip.setMaxWidth(380);
		tooltip.setWrapText(true);
		return tooltip;
	}

	/**
	 * Create a small wrapped label for a line of tooltip detail text.
	 *
	 * @param text  the text to show.
	 * @param color the text colour.
	 * @return a wrapped label.
	 */
	private Label tooltipDetailLabel(String text, Color color) {
		Label label = new Label(text);
		label.setWrapText(true);
		label.setTextFill(color);
		label.setMaxWidth(344);
		return label;
	}

	/**
	 * Get the extension filters for the file selection dialog. 
	 * @return the extension filters for the model file dialog. 
	 */

	public ArrayList<ExtensionFilter> getExtensionFilters() {


		ArrayList<String> extensionFilters  = new  ArrayList<String>(); 

		for (DLClassiferModel dlModel: dlControl.getDLModels()) {
			//System.out.println("Model: " + dlModel.getModelUI());

			if (dlModel.getModelUI()!=null) {
				for (ExtensionFilter extFilter: dlModel.getModelUI().getModelFileExtensions()){
					//System.out.println("Extensions: " + extFilter.getExtensions());
					extensionFilters.addAll(extFilter.getExtensions()); 
				}
			}
		}

		//Now we don't really want lots of extension filters 
		ArrayList<ExtensionFilter> dlExtFilter = new  ArrayList<ExtensionFilter>(); 		
		dlExtFilter.add(new ExtensionFilter("Deep Learning Models", extensionFilters));

		return dlExtFilter ; 
	}

	/**
	 * Task for loading and/or downloading deep learning models. 
	 */
	class LoadTask extends Task<DLStatus> {

		private URI uri;
		
		

		public LoadTask(URI uri) {
			this.uri=uri; 
			
			
			//TODO - not the best as some other part of the program could be using download listeners...
			dlControl.getDownloadManager().clearDownloadListeners();

			dlControl.getDownloadManager().addDownloadListener((status, bytesDownLoaded)->{
				 updateMessage( status,  bytesDownLoaded); 
			}); 

		}
		
		private void updateMessage(DLStatus status, long bytesDownLoaded) {
			//the updates have their own messages but let's take some more control here. 
			this.updateProgress(-1, 1); //set to intermediate
			//System.out.println("Status: " + status);
			switch (status) {
			case CONNECTION_TO_URL:
				this.updateMessage("Checking URL");
				break;
			case DOWNLOADING:
				this.updateMessage(String.format("Download %.2f MB", ((double) bytesDownLoaded)/1024./1024.));
				break;
			case DOWNLOAD_FINISHED:
				this.updateMessage("Download complete");
				break;
			case DOWNLOAD_STARTING:
				this.updateMessage("Download starting");
				break;
			default:
				this.updateMessage(status.getDescription());
				break;
		}
		}


		@Override 
		public DLStatus call() throws Exception {
			try {
				this.updateMessage("Loading model...");

				//either downloads the model or instantly returns the model path if it's a file. 
				//The download listener will update progress if this starts downloading a file. 
				URI modelPath = dlControl.getDownloadManager().downloadModel(uri);
				
				
				if (modelPath==null) {
					return DLStatus.MODEL_DOWNLOAD_FAILED;
				}

				this.updateMessage("Loading model...");

				DLStatus result = newModelSelected(modelPath); 
				
				currentSelectedFile = modelPath;
				Thread.sleep(1000); //just show the user something happened if model loading is rapid. 
				
				return result;
				
			} catch (Exception e) {
				System.out.println("-----UNABLE TO LOAD MODEL-----");
				currentClassifierModel=null; //this will reset the pane
				e.printStackTrace();
				return DLStatus.MODEL_LOAD_FAIL;
			}
		}

		private void finishedLoading() {
						
			if (this.getValue().isError()) {
				//show a warnign dialog
				showWarningDialog(this.getValue()); 
			}
			
			rawDLSettingsPane.setClassifierPane(); 
			modelLoadIndicator.setVisible(false);

			//important to stop set a bound property exception. 
			pathLabel.textProperty().unbind();
			modelLoadIndicator.progressProperty().unbind();
			
			//repack the dialog so it fits the new settings
			rawDLSettingsPane.getDLControl().settingsDialogChange();

			updatePathLabel(this.getValue()); 
		}

		@Override protected void succeeded() {
			super.succeeded();
			finishedLoading();
			updateMessage("Done!");
		}

		@Override protected void cancelled() {
			super.cancelled();
			finishedLoading();
			updateMessage("Cancelled!");
		}

		@Override protected void failed() {
			super.failed();
			finishedLoading();
			updateMessage("Failed!");
		}
	};


}
