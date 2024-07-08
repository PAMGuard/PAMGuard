package Acquisition.layoutFX;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.glyphfont.Glyph;

import Acquisition.FileInputParameters;
import Acquisition.FolderInputParameters;
import Acquisition.FolderInputSystem;
import Acquisition.pamAudio.PamAudioFileFilter;
import PamController.PamController;
import PamController.PamFolders;
import PamUtils.PamCalendar;
import PamUtils.worker.PamWorker;
import PamUtils.worker.filelist.FileListData;
import PamUtils.worker.filelist.WavFileType;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamComboBox;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamProgressBar;
import pamViewFX.fxNodes.PamVBox;

/**
 * JavaFX pane for the folder input of the sound acquisition. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class FolderInputPane extends DAQSettingsPane<FolderInputParameters>{

	private static final double UTIL_BUTTON_WIDTH = 120;

	/**
	 * Shows current and previous files. 
	 */
	private PamComboBox<String> fileNames;

	/**
	 * Browse file button
	 */
	private PamButton browseFileButton;

	/**
	 * Browse for a folder button. 
	 */
	private PamButton browseFolderButton;

	/**
	 * Dialog for choosing folders. 
	 */
	private DirectoryChooser directoryChooser=new DirectoryChooser(); 

	/**
	 * File date text. 
	 */
	private Label fileDateText;

	/**
	 * Check boxes for merging files and selecting whether to look in sub folders. 
	 */
	private ToggleSwitch subFolders;

	/**
	 * Pane which shows date format and allows time zone changes. 
	 */
	private FileDataDialogStripFX fileDateStrip;

	/**
	 * The folder input system. 
	 */
	private FolderInputSystem folderInputSystem;

	/**
	 * Button for checking files. 
	 */
	private PamButton checkFiles;

	/**
	 * Check box for repeating file analysis. 
	 */
	private CheckBox repeat, mergeFiles;

	/**
	 * Table view
	 */
	private TableView<WavFileType> table = new TableView<WavFileType>();

	/**
	 * File chooser dialog. 
	 */
	protected final FileChooser fileChooser = new FileChooser();

	/**
	 * The main pane. 
	 */
	private Pane mainPane;

	/**
	 * The progress bar. 
	 */
	private PamProgressBar progressBar;

	/**
	 * The progress label for large datasets. 
	 */
	private Label progressLabel;

	/**
	 * Reference to the Aquisition settings pane which holds this pane. 
	 */
	private AcquisitionPaneFX acquisitionPaneFX;

	/**
	 * Pane to fix the headers of wave files. 
	 */
	private CheckWavHeadersPane fixWavPane;

	/**
	 * Toggle button for merging contigious files
	 */
	private ToggleButton mergeContigious;

	//	/**
	//	 * The folder input system. 
	//	 * @param folderInputSystem - the folder system. 
	//	 * @return 
	//	 */
	//	public FolderInputPane(FolderInputSystem folderInputSystem) {
	//		super(); 
	//		this.folderInputSystem=folderInputSystem; 
	//		this.mainPane=createDAQPane(); 
	//	}

	public FolderInputPane(FolderInputSystem folderInputSystem2, AcquisitionPaneFX acquisitionPaneFX) {
		super(); 
		this.folderInputSystem=folderInputSystem2; 
		this.acquisitionPaneFX = acquisitionPaneFX; 
		this.mainPane=createDAQPane(); 
		this.mainPane.setPrefWidth(300);
	}

	/**
	 * Create the DAQ pane. 
	 * @return the DAQ pane. 
	 */
	private Pane createDAQPane() {

		PamBorderPane p = new PamBorderPane();

		PamVBox pamVBox=new PamVBox(); 
		pamVBox.setSpacing(5);

		Label titleLabel;
		pamVBox.getChildren().add(titleLabel=new Label("Select Audio File"));
		PamGuiManagerFX.titleFont2style(titleLabel);

		//file combo box. 
		PamHBox fileSelectBox=new PamHBox();
		fileSelectBox.setSpacing(5);
		fileSelectBox.getChildren().addAll(fileNames = new PamComboBox<String>(), browseFileButton=new PamButton(), browseFolderButton=new PamButton() );

		fileNames.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(fileNames, Priority.ALWAYS);	
		fileNames.setOnAction((action)->{
			//check if a file or a folder and select a folder. 
			//TODO
		});

		browseFileButton.setGraphic(PamGlyphDude.createPamIcon("mdi2f-file-multiple", PamGuiManagerFX.iconSize));
		browseFileButton.prefHeightProperty().bind(fileSelectBox.heightProperty()); //make browse button same height as combo box. 
		browseFileButton.setMinWidth(40);
		browseFileButton.setOnAction( (action) ->{	                
			selectFolder(false); 
		});
		browseFileButton.setTooltip(new Tooltip("Select a folder of files"));


		browseFolderButton.setGraphic(PamGlyphDude.createPamIcon("mdi2f-folder", PamGuiManagerFX.iconSize));
		browseFolderButton.prefHeightProperty().bind(fileSelectBox.heightProperty()); //make browse button same height as combo box. 
		browseFolderButton.setMinWidth(40);
		browseFolderButton.setOnAction( (action) ->{	                
			selectFolder(true);
		});		
		browseFolderButton.setTooltip(new Tooltip("Select a single file"));


		fileNames.prefHeightProperty().bind(browseFolderButton.heightProperty());

		//progress bar for loading in large numbers of wav files
		progressBar = new PamProgressBar(); 
		progressBar.setProgress(0);
		//progressBar.setVisible(false);
		progressBar.setMaxWidth(Double.POSITIVE_INFINITY);
		progressBar.setPrefHeight(10);
		progressBar.setVisible(false);

		//table showing all the wav files
		//		pamVBox.getChildren().add(createTablePane());

		PamHBox subFolderPane = new PamHBox(); 
		subFolderPane.setSpacing(5);
		subFolders=new ToggleSwitch();
		subFolders.setMaxWidth(30);
		subFolders.selectedProperty().addListener((obsVal, oldVal, newVal)->{
			//Need to redo file loading here.

		});
		subFolderPane.getChildren().addAll(subFolders, new Label("Sub Folders")); 


		fileDateStrip=new FileDataDialogStripFX(folderInputSystem.getAquisitionControl().getFileDate()); 
		fileDateStrip.setMaxWidth(Double.MAX_VALUE);
		fileDateStrip.prefWidthProperty().bind(pamVBox.widthProperty());
		
		
		fixWavPane = new CheckWavHeadersPane(folderInputSystem); 
		
		Label utilsLabel=new Label("Sound file utilities");
		PamGuiManagerFX.titleFont2style(utilsLabel);
		
		pamVBox.getChildren().addAll(fileSelectBox, subFolderPane, progressBar,  createTablePane(), 
				fileDateText=new Label(), utilsLabel, createUtilsPane());

		//allow users to check file headers in viewer mode. 
		//		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
		checkFiles = new PamButton("Check File Headers...");
		checkFiles.setOnAction((action)->{ 
			folderInputSystem.checkFileHeaders();
		});
		//pamVBox.getChildren().addAll(checkFiles);

		//mergeFiles=new CheckBox("Merge contiguous files");
		//		pamVBox.getChildren().addAll(subFolders, mergeFiles);
		//
		//		if (PamguardVersionInfo.getReleaseType() == PamguardVersionInfo.ReleaseType.BETA) {
		//			pamVBox.getChildren().add(repeat=new CheckBox("Repeat")); 
		//		}

		return pamVBox; 		
	}		
	
	
	/**
	 * Create
	 * @return
	 */
	private Pane createUtilsPane() {
		
		PamHBox hBox = new PamHBox();
		hBox.setSpacing(5);
		hBox.setAlignment(Pos.CENTER_LEFT);
		
		
		//Time stamp pane
		PamButton time = new PamButton("Time stamps"); 
		time.setPrefWidth(UTIL_BUTTON_WIDTH);
		time.setGraphic(PamGlyphDude.createPamIcon("mdi2a-av-timer", PamGuiManagerFX.iconSize));
		time.setOnAction((action)->{
			acquisitionPaneFX.getAdvancedPane().setCenter(this.fileDateStrip.getAdvDatePane().getContentNode());
			acquisitionPaneFX.getAdvancedLabel().setText("File Time Stamps");
			acquisitionPaneFX.getFlipPane().flipToBack();
		});
		
		acquisitionPaneFX.getFlipPane().flipFrontProperty().addListener((obsVal, oldVal, newVal)->{
			this.fileDateStrip.getAdvDatePane().getParams();
		});

		
		PamButton wavFix = new PamButton("Fix wav"); 
		wavFix.setPrefWidth(UTIL_BUTTON_WIDTH);
		wavFix.setGraphic(PamGlyphDude.createPamIcon("mdi2f-file-settings", PamGuiManagerFX.iconSize));
		wavFix.setOnAction((action)->{
			acquisitionPaneFX.getAdvancedLabel().setText("Fix Wave Files");
			acquisitionPaneFX.getAdvancedPane().setCenter(this.fixWavPane);
			fixWavPane.setParams();
			acquisitionPaneFX.getFlipPane().flipToBack();
		});
		
		mergeContigious = new ToggleButton("Merge files");
		mergeContigious.setPrefWidth(UTIL_BUTTON_WIDTH);
		mergeContigious.setGraphic(PamGlyphDude.createPamIcon("mdi2s-set-merge", PamGuiManagerFX.iconSize));

		hBox.getChildren().addAll(time, wavFix, mergeContigious);
		
		return hBox;
	}

	/**
	 * Create the table pane. 
	 * @return the table pane. 
	 */
	private Pane createTablePane() {

		table.setEditable(true);

		TableColumn<WavFileType, Boolean > useWavFileColumn = new TableColumn<>( "Use" );
		useWavFileColumn.setCellValueFactory(cellData -> cellData.getValue().useWavFileProperty());		
		useWavFileColumn.setCellFactory( tc -> new CheckBoxTableCell<>());
		useWavFileColumn.setEditable(true);
		useWavFileColumn.setMaxWidth(40);

		TableColumn<WavFileType, String > fileNameColumn = new TableColumn<WavFileType, String >("File Name");
		fileNameColumn.setCellValueFactory( f ->  new SimpleStringProperty(f.getValue().getName()));

		TableColumn<WavFileType, String > startTimeColumn = new TableColumn<WavFileType, String > ("Start Time");
		startTimeColumn.setCellValueFactory( f -> {
			//little bit more complex here because the file start time comes from a standard date class.
			long millisDate = folderInputSystem.getFileStartTime(f.getValue());
			return new SimpleStringProperty(PamCalendar.formatDBDateTime(millisDate, true)); 
		});
		startTimeColumn.setPrefWidth(200);

		TableColumn<WavFileType, Float > durationColumn = new TableColumn<WavFileType, Float > ("Duration");
		durationColumn.setCellValueFactory(cellData -> new SimpleFloatProperty(cellData.getValue().getDurationInSeconds()).asObject());

		TableColumn<WavFileType, Float > sampleRateColumn = new TableColumn<WavFileType, Float > ("sR");
		sampleRateColumn.setCellValueFactory(cellData -> new SimpleFloatProperty(cellData.getValue().getAudioInfo().getSampleRate()).asObject());
		//		

		TableColumn<WavFileType, Integer > chanColumn = new TableColumn<WavFileType, Integer > ("Chan");
		chanColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAudioInfo().getChannels()).asObject());
		useWavFileColumn.setPrefWidth(70);


		TableColumn<WavFileType, String > typeColumn = new TableColumn<WavFileType, String > ("Type");
		typeColumn.setCellValueFactory( f -> {
			return new SimpleStringProperty(FilenameUtils.getExtension(f.getValue().getPath()));
		});

		table.getColumns().addAll(useWavFileColumn, fileNameColumn, startTimeColumn, durationColumn, sampleRateColumn, chanColumn, typeColumn);

		table.setMaxHeight(200);

		return new PamBorderPane(table); 
	}

	/**
	 * Open a dialog to select either a folder or a list of files. 
	 * @param folderDir - true to use directory chooser, false to use multiple file chooser. 
	 */
	protected void selectFolder(boolean folderDir) {

		List<File> files=null; 

		if (folderDir){
			files=new ArrayList<File>();
			configureFolderChooser(directoryChooser);
			File folder=directoryChooser.showDialog(PamController.getMainStage());
			if (folder == null){
				return; 
			}
			files.add(folder);

		}
		else {
			configureFileChooser(fileChooser);
			files=fileChooser.showOpenMultipleDialog(PamController.getMainStage()); 
			if (files == null || files.size()<=0) {
				return; 
			}
		}
		//now have a list of files or a list with one folder. 

		/*
		 * if it's a single directory that's been selected, then 
		 * set that with setNewFile. If multiple files and directories
		 * are accepted, select the parent directory of all of them. 
		 */
		if (files.size() <= 0) return;
		else if (files.size() == 1) {
			setNewFile(files.get(0).toString(), folderInputSystem.getFolderInputParameters());
		}
		else {
			// take the folder name from the first file
			File aFile = files.get(0);
			setNewFile(aFile.getAbsolutePath(), folderInputSystem.getFolderInputParameters());
		}


		File[] filesArr = new File[files.size()];
		for (int i=0; i<filesArr.length; i++) {
			filesArr[i] = files.get(i); 
		}
		
		folderInputSystem.getFolderInputParameters().setSelectedFiles(filesArr);
		folderInputSystem.makeSelFileList();
		//		folderInputSystem.makeSelFileList(folderInputSystem.getFolderInputParameters().getSelectedFiles());

	}


	/**
	 * Set params for the file chooser.
	 * @param fileChooser - filechooser to configure. 
	 */
	protected static void configureFileChooser(
			final FileChooser fileChooser) {      
		fileChooser.setTitle("Open Sound File");
		fileChooser.setInitialDirectory(
				new File(PamFolders.getDefaultProjectFolder())
				);                 
		PamAudioFileFilter audioFilter= new PamAudioFileFilter();
		for (int i=0; i<audioFilter.getFileExtensions().size(); i++){
			fileChooser.getExtensionFilters().addAll(
					new FileChooser.ExtensionFilter(audioFilter.getFileExtensions().get(i), ("*"+audioFilter.getFileExtensions().get(i))));
		}
	}

	/**
	 * Called when user selects a file or folder in the dialog. 
	 * @param newFile - the new file or folder. 
	 */
	public void setNewFile(String newFile, FolderInputParameters currParams) {
		currParams.recentFiles.remove(newFile);
		currParams.recentFiles.add(0, newFile);
		fillFileList(currParams);
	}

	/**
	 * Fill the list of old folder paths and file paths. This is NOT the 
	 * list of wav files. 
	 */
	private void fillFileList(FolderInputParameters currParams) {
		// the array list will always be set up so that the items are in most
		// recently used order ...
		fileNames.getItems().clear();
		String file;
		if (currParams.recentFiles.size() == 0) return;
		for (int i = 0; i < currParams.recentFiles.size(); i++){
			file = currParams.recentFiles.get(i);
			if (file == null || file.length() == 0) continue;
			fileNames.getItems().add(file);
		}
		fileNames.getSelectionModel().select(0);
	}

	private void configureFolderChooser(DirectoryChooser directoryChooser2) {
		// TODO Auto-generated method stub

	}

	@Override
	public FolderInputParameters getParams(FolderInputParameters folderInputParameters) {

		folderInputParameters.subFolders = subFolders.isSelected();
		//		folderInputParameters.mergeFiles = mergeFiles.isSelected();
		//		folderInputParameters.repeatLoop = repeat.isSelected();

		String file = (String) fileNames.getSelectionModel().getSelectedItem(); 
		if (file != null && file.length() > 0) {
			folderInputParameters.recentFiles.remove(file);
			folderInputParameters.recentFiles.add(0, file);
			// check we're not building up too long a list. 
			while (folderInputParameters.recentFiles.size() > FileInputParameters.MAX_RECENT_FILES) {
				folderInputParameters.recentFiles.remove(folderInputParameters.recentFiles.size()-1);
				folderInputParameters.recentFiles.trimToSize();
			}
		}

		//now need to have the final list of wav files. Some of these may have been deslected in the table. 
		ArrayList<String> selectedFiles = new ArrayList<String>();

		for (WavFileType wavTypes: table.getItems()) {
			if (wavTypes.useWavFile()) {
				selectedFiles.add(wavTypes.getPath()); 
			}
		}

		folderInputParameters.setSelectedFiles(selectedFiles.toArray(new String[0]));
		//		if (skipSecondsField!=null) {
		//			try {
		//				Double skipSeconds = Double.valueOf(skipSecondsField.getText())*1000.; // saved in millis. 
		//				fileInputParameters.skipStartFileTime = skipSeconds.longValue();
		//			}
		//			catch (Exception e) {
		//				return false; 
		//			}
		//		}
		
		folderInputParameters.mergeFiles = mergeContigious.isSelected();
		
		folderInputParameters.subFolders = this.subFolders.isSelected(); 
		
		return folderInputParameters; 
	}

	@Override
	public void setParams(FolderInputParameters currParams) {

		//set the whether sub folders are selected. 
		subFolders.setSelected(currParams.subFolders);

		//fill the file list. 
		fillFileList(currParams); 
		
		if (table.getItems()==null || table.getItems().size()==0) {
			folderInputSystem.makeSelFileList();
		}
		
		fileDateStrip.setFileList(table.getItems()); 
		
		mergeContigious.setSelected(currParams.mergeFiles);
		
	}


	//	@Override
	//	public boolean dialogGetParams() {
	//		folderInputParameters.subFolders = subFolders.isSelected();
	//		folderInputParameters.mergeFiles = mergeFiles.isSelected();
	//		folderInputParameters.repeatLoop = repeat.isSelected();
	//		currentFile = 0;
	//		if (skipSecondsField!=null) {
	//			try {
	//				Double skipSeconds = Double.valueOf(skipSecondsField.getText())*1000.; // saved in millis. 
	//				folderInputParameters.skipStartFileTime = skipSeconds.longValue();
	//			}
	//			catch (Exception e) {
	//				return false; 
	//			}
	//		}
	//		return super.dialogGetParams();
	//	}
	//
	//	@Override
	//	public void dialogSetParams() {
	//		// do a quick check to see if the system type is stored in the parameters.  This field was added
	//		// to the FileInputParameters class on 23/11/2020, so any psfx created before this time
	//		// would hold a null.  The system type is used by the getParameterSet method to decide
	//		// whether or not to include the parameters in the XML output
	//		if (fileInputParameters.systemType==null) fileInputParameters.systemType=getSystemType();
	//		
	//		super.dialogSetParams();
	//		subFolders.setSelected(folderInputParameters.subFolders);
	//		mergeFiles.setSelected(folderInputParameters.mergeFiles);
	//		repeat.setSelected(folderInputParameters.repeatLoop);
	//		if (skipSecondsField!=null) {
	//			skipSecondsField.setText(String.format("%.1f", fileInputParameters.skipStartFileTime/1000.));
	//		}
	//	}


	@Override
	public String getName() {
		return "Folder Input Params";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub

	}

	/**
	 * Called once the wav worker thread has finished making the audio file list. 
	 * @param fileListData  - the list of new audio files. 
	 */
	public void newFileList(FileListData<WavFileType> fileListData) {
		System.out.println("FolderInputPane: New file list: " + fileListData.getFileCount()); 
		ObservableList<WavFileType> fileList = FXCollections.observableArrayList(fileListData.getListCopy());
		this.table.getItems().clear();
		this.table.getItems().addAll(fileList);

		fileDateStrip.setFileList(fileList); 

		//need to set the sample rate and channels in the main pane.
		if (fileList!=null && fileList.size()>0) {
			this.acquisitionPaneFX.setSampleRate(fileList.get(0).getAudioInfo().getSampleRate());
			this.acquisitionPaneFX.setChannels(fileList.get(0).getAudioInfo().getChannels());
		}

		progressBar.setVisible(false);

		//		//folderInputSystem.interpretNewFile(newFile);
		//		File[] selFiles = currParams.getSelectedFileFiles();
		//		if (selFiles!=null && selFiles.length > 0) {
		//			fileDateStrip.setDate(folderInputSystem.getFileStartTime(selFiles[0]));
		//		}
	}

	/**
	 * Called whenever a new file worker is initialised to search a folder for wav files. 
	 * @param worker - the new file worker being used. 
	 */
	public void setFileWorker(PamWorker<FileListData<WavFileType>> worker) {

		if (worker==null) {
			progressBar.progressProperty().unbind(); 
			return; 
		}

		//System.out.println("File list worker: " + worker ); 
		//must ensure this is on the FX thread
		//Platform.runLater(()->{
		this.progressBar.progressProperty().bind(worker.getPamWorkProgress().getProgressProperty()); 
		progressBar.setVisible(true);

		//}); 

	}

	/***these are the functions called by the aquisition main settings pane**/

	@Override
	public void setParams() {		
		//set the parameters for the dialog. 
		this.setParams(folderInputSystem.getFolderInputParameters());
	}

	@Override
	public boolean getParams() {
		FolderInputParameters params = this.getParams(folderInputSystem.getFolderInputParameters());
		if (params == null) return false;
		else {
			this.folderInputSystem.setFolderInputParameters(params);
			return true; 
		}
	}



}
