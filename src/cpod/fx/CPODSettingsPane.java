package cpod.fx;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;

import PamController.PamGUIManager;
import PamController.SettingsPane;
import cpod.CPODControl2;
import cpod.CPODParams;
import cpod.CPODUtils.CPODFileType;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;

/**
 * CPOD FX based settings pane. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class CPODSettingsPane extends SettingsPane<CPODParams> {

	private CPODControl2 cpodControl;

	/**
	 * The text field 
	 */
	private PamSpinner<Double> startOffset, timeStretch;

	/**
	 * The main pane. 
	 */
	private PamBorderPane mainPane;


	private TextField pathLabel;

	/**
	 * The extension filter. Holds the types of files that can be imported. 
	 */
	private FileChooser.ExtensionFilter extensionFilterCPOD;
	//	private FileChooser.ExtensionFilter extensionFilterFPOD;


	/**
	 * The directory chooser.
	 */
	private FileChooser fileChooser;

	/**
	 * Directory chooser.
	 */
	private DirectoryChooser folderChooser;

	/**
	 * Progress bar to show the progress of CPOD data import.  
	 */
	private ProgressBar progressBar;

	/**
	 * Toggle switch for sub folders. 
	 */
	private PamToggleSwitch subFolder;


	private File currentFolder;


	private List<File> files;


	private PamButton importButton;


	private Task<Integer> task;

	private boolean running = false;


	private Label filesInfoLabel;

	private Label progressLabel; 

	public CPODSettingsPane(CPODControl2 cpodControl2) {
		super(null);
		this.cpodControl = cpodControl2;


		//define the types of files to be imported ("Note:  add FP1 and FP3 here)
		extensionFilterCPOD = new ExtensionFilter("CPOD file", "*.cp1", "*.cp3", "*.fp1", "*.fp3"); 

		//file chooser
		fileChooser = new FileChooser(); 
		fileChooser.getExtensionFilters().addAll(getExtensionFilters()); 

		//folder chooser
		folderChooser = new DirectoryChooser(); 



		pathLabel = new TextField("No classifier file selected"); 
		pathLabel.setEditable(false);

		filesInfoLabel = new Label(); 


		//		PamButton browsFileButton = new PamButton("", PamGlyphDude.createPamGlyph(MaterialDesignIcon.FILE_MULTIPLE, PamGuiManagerFX.iconSize)); 
		PamButton browsFileButton = new PamButton("", PamGlyphDude.createPamIcon("mdi2f-file-multiple", PamGuiManagerFX.iconSize)); 
		browsFileButton.setMinWidth(30);
		browsFileButton.setTooltip(new Tooltip("Browse to select individual or mutliple CP1 or CP3 files or FP1 or FP3 files"));
		browsFileButton.setOnAction((action)->{

			List<File> files = new LinkedList<File>(fileChooser.showOpenMultipleDialog(this.getFXWindow()));
			setNewFiles(files); 

		});


		//		PamButton browsFolderButton = new PamButton("", PamGlyphDude.createPamGlyph(MaterialDesignIcon.FOLDER, PamGuiManagerFX.iconSize)); 
		PamButton browsFolderButton = new PamButton("", PamGlyphDude.createPamIcon("mdi2f-folder", PamGuiManagerFX.iconSize)); 
		browsFolderButton.setMinWidth(30);
		browsFolderButton.setTooltip(new Tooltip("Browse to a folder contaning CP1 and CP3 or FP1 and FP3 files"));
		browsFolderButton.setOnAction((action)->{

			File file = folderChooser.showDialog(this.getFXWindow());

			setNewFolder(file); 
		});


		subFolder = new PamToggleSwitch("Sub folders"); 


		BorderPane subFolderPane = new PamBorderPane(); 
		subFolderPane.setRight(filesInfoLabel);
		subFolderPane.setLeft(subFolder);

		PamHBox filesPane = new PamHBox(); 
		filesPane.setSpacing(5.);
		filesPane.setAlignment(Pos.CENTER_LEFT);
		HBox.setHgrow(pathLabel, Priority.ALWAYS);
		pathLabel.setMaxWidth(Double.MAX_VALUE);
		pathLabel.prefHeightProperty().bind(browsFolderButton.heightProperty());

		filesPane.getChildren().addAll(pathLabel, browsFileButton, browsFolderButton); 

		//time offset pane. 
		startOffset = new PamSpinner<Double>(); 

		startOffset = new PamSpinner<Double>(0, Double.MAX_VALUE, 0,1); 
		startOffset.setPrefWidth(80);
		startOffset.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		startOffset.setEditable(true);
		startOffset.setTooltip(new Tooltip("Enter the start time offset in seconds"));

		timeStretch = new PamSpinner<Double>(0, Double.MAX_VALUE, 0, 0.1); 
		timeStretch.setPrefWidth(80);
		timeStretch.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		timeStretch.setEditable(true);
		timeStretch.setTooltip(new Tooltip("Enter the start time offset in seconds"));

		PamHBox timeBox = new PamHBox(); 
		timeBox.setSpacing(5.);
		timeBox.setAlignment(Pos.CENTER_LEFT);

		timeBox.getChildren().addAll(new Label("Start offset"), startOffset, new Label("s"), 
				new Label("Time stretching"), timeStretch, new Label("ppm")); 

		//progress bar pane. 

		progressBar = new ProgressBar(); 
		progressBar.setMaxWidth(Double.MAX_VALUE);
		progressBar.setProgress(0);

		PamBorderPane progressPane = new PamBorderPane(); 
		BorderPane.setMargin(progressBar, new Insets(5,5,5,5));

		importButton = new PamButton("Import"); 
		importButton.setDisable(true);

		//		importButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.IMPORT, PamGuiManagerFX.iconSize)); 
		enableImportButton(); 
		importButton.setTooltip(new Tooltip("Click to begin importing CPOD data.\n "
				+ "CPOD data will be stored in binary files\n "
				+ "so this process only need to occur once."));
		importButton.setOnAction((action)->{
			if (!running) {
				importCPODData();
				running = true; 
			}
			else {
				stopImport(); 
				running=false; 
			}
			enableImportButton();
		});
		progressPane.setCenter(progressBar);
		progressPane.setRight(importButton);
		progressPane.setBottom(progressLabel = new Label(""));


		PamVBox mainHolder = new PamVBox(); 
		mainHolder.setSpacing(5);

		Label cpodDataLabel = new Label("CPOD files"); 
		PamGuiManagerFX.titleFont2style(cpodDataLabel);

		Label timeStretchLabel = new Label("Time stretching"); 
		PamGuiManagerFX.titleFont2style(timeStretchLabel);

		Label dataImportLabel = new Label("Import data"); 
		PamGuiManagerFX.titleFont2style(dataImportLabel);

		mainHolder.getChildren().addAll(cpodDataLabel, filesPane, subFolderPane, timeStretchLabel, timeBox, dataImportLabel, progressPane); 

		mainPane = new PamBorderPane(); 
		mainPane.setCenter(mainHolder);
		mainPane.setPadding(new Insets(5,5,5,5));

		//		selectFolder = new SelectFolder(cpodControl.getUnitName() + " Folder", 50, true);
		//		JPanel mainPanel = new JPanel();
		//		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		//		mainPanel.add(selectFolder.getFolderPanel());
		//		JPanel sPanel = new JPanel(new GridBagLayout());
		//		sPanel.setBorder(new TitledBorder("Time Corrections"));
		//		GridBagConstraints c = new PamGridBagContraints();
		//		sPanel.add(new JLabel("Start offset ", JLabel.RIGHT), c);
		//		c.gridx++;
		//		sPanel.add(startOffset = new JTextField(5), c);
		//		startOffset.setToolTipText("Enter the start time offset in seconds");
		//		c.gridx++;
		//		sPanel.add(new JLabel(" s", JLabel.LEFT), c);
		//		c.gridx = 0;
		//		c.gridy++;
		//		sPanel.add(new JLabel("Time strestching ", JLabel.RIGHT), c);
		//		c.gridx++;
		//		sPanel.add(timeStretch = new JTextField(5), c);
		//		timeStretch.setToolTipText("Time streth is in parts per million");
		//		c.gridx++;
		//		sPanel.add(new JLabel(" ppm", JLabel.LEFT), c);
		//		JPanel soPanel = new JPanel(new BorderLayout());
		//		soPanel.add(BorderLayout.WEST, sPanel);
		//		mainPanel.add(soPanel);
		//		
		//		setDialogComponent(mainPanel);
	}





	private void enableImportButton() {
		if (!running) {
			importButton.setGraphic(PamGlyphDude.createPamIcon("mdi2f-file-import", PamGuiManagerFX.iconSize)); 
			importButton.setText("Import");
		}
		else {
			importButton.setGraphic(PamGlyphDude.createPamIcon("mdi2s-stop", PamGuiManagerFX.iconSize)); 
			importButton.setText("Stop");
		}
	}


	/**
	 * Import CPOD data. 
	 */
	private boolean importCPODData() {
		if (files==null) return false; 


		//begins the import
		//		this.tasks = this.cpodControl.getCpodImporter().importCPODData(files);
		this.task = this.cpodControl.importPODData(files);

		if (task ==null) return false; 

		this.progressBar.setProgress(-1.);

		this.progressBar.progressProperty().bind(task.progressProperty());
		this.progressLabel.textProperty().bind(task.messageProperty());

		//run the tasks
		this.cpodControl.getCpodImporter().runTasks(task);

		return true; 
	}

	/**
	 * Called whenever the importing is finished.  
	 */
	private void importingFinished() {

		if (PamGUIManager.isSwing())
			//important to put this here or , if a swing dialog pop ups, then there is thread lock. 
			SwingUtilities.invokeLater(()->{
				PamController.PamController.getInstance().createDataMap();
				PamController.PamController.getInstance().updateDataMap(); 

			}); 
		else {
			PamController.PamController.getInstance().createDataMap();
			PamController.PamController.getInstance().updateDataMap(); 
		}


		Platform.runLater(()->{
			this.running=false; 
			enableImportButton();
			this.progressBar.progressProperty().unbind();
			this.progressLabel.textProperty().unbind();

			progressBar.setProgress(1.);
		}); 

		System.out.println("Importing finished 3:"); 

	}

	/*
	 * Stop the import. 
	 */
	private void stopImport() {
		task.cancel(); 
	}

	/**
	 * Set a folder of files. 
	 * @param folder - the folder. 
	 */
	private void setFileList(File folder) {

		this.currentFolder = folder; 

		if (folder==null) return; 

		files = new ArrayList<File>(); 

		//have a look through the files and check that there are some files to import. 		
		for (int i=0; i<CPODFileType.values().length; i++) {
			try {
				List<File> cp1 = (List<File>) FileUtils.listFiles(folder, 
						new String[]{CPODFileType.values()[i].getText()}, this.subFolder.isSelected());
				System.out.println("Files out: " + cp1);
				files.addAll(cp1); 
			}
			catch (Exception e) {
				System.err.println("Current directory does not exist: " + folder ); 
			}
		}
	}

	/**
	 * Set the files.
	 * @param files - the file list to set. 
	 */
	private void setFileList(List<File> files) {
		this.files = files; 
	}

	/**
	 * Get the extension filter for the file dialog. 
	 * e.g. 
	 * new ExtensionFilter("Pytorch Model", "*.pk")
	 * @return a list of extension fitlers for the file dialog. 
	 */
	public ArrayList<FileChooser.ExtensionFilter> getExtensionFilters(){
		ArrayList<FileChooser.ExtensionFilter> filters = new ArrayList<FileChooser.ExtensionFilter>();
		filters.add(extensionFilterCPOD); 
		//don't add an exstra filter - just have all as one - otherwise the user has to change the 
		//file dialog to FPODs to get it to work. 
		//		filters.add(extensionFilterFPOD); 
		return filters;
	}


	//	@Override
	//	public void restoreDefaultSettings() {
	//		startOffset.setText("0.0");
	//		timeStretch.setText("0.0");
	//	}


	@Override
	public CPODParams getParams(CPODParams currParams) {
		currParams.offlineFolder = currentFolder==null? null:currentFolder.getAbsolutePath(); 
		currParams.subFolders = subFolder.selectedProperty().get();
		try {
			currParams.startOffset = startOffset.getValue();
			currParams.timeStretch = timeStretch.getValue();
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid parameter");
		}
		return currParams;
	}


	@Override
	public void setParams(CPODParams cpodParams) {
		this.pathLabel.setText(cpodParams.offlineFolder);
		subFolder.setSelected(cpodParams.subFolders);

		startOffset.getValueFactory().setValue(cpodParams.startOffset);
		timeStretch.getValueFactory().setValue(cpodParams.timeStretch);


		setNewFolder(cpodParams.offlineFolder==null ? null : new File(cpodParams.offlineFolder)); 
	}

	/**
	 * Called whenever there is a new selected file or folder. 
	 * @param file - the file label to set. 
	 */
	private void setNewFolder(File file) {

		if (file==null) {
			this.pathLabel.setText("No folder or file selected"); 
		}
		else {
			this.pathLabel.setText(file.getAbsolutePath());
			setFileList(file); 

			setFileInfoLabel();
		}

		importButton.setDisable(this.files==null || this.files.size()<1);
	}

	/**
	 * Called whenever there is a new selected file or folder. 
	 * @param file - the file label to set. 
	 */
	private void setNewFiles(List<File> files) {

		if (files==null || files.size()==0) {
			this.pathLabel.setText("No folder or file selected"); 
		}
		else {
			this.pathLabel.setText(files.get(0).getAbsolutePath());

			this.files = files;

			setFileInfoLabel();
		}

		importButton.setDisable(this.files==null || this.files.size()<1);
	}

	private void setFileInfoLabel() {
		if (files.size()>=1) {
			this.filesInfoLabel.setText(files.size() + " CPOD files to import"); 
		}
		else {
			this.filesInfoLabel.setText(files.size() + " CPOD file to import"); 
		}
	}

	private CPODParams showWarning(String string) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getName() {
		return "CPOD Paramters";
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
