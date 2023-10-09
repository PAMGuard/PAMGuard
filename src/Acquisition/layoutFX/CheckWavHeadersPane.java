package Acquisition.layoutFX;

import java.io.File;
import java.util.ArrayList;
import Acquisition.AudioFileFuncs;
import Acquisition.FolderInputSystem;
import Acquisition.WavFileFuncs;
import Acquisition.pamAudio.PamAudioFileFilter;
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;


/**
 * Pane with controls to fix wave file headers. 
 * @author Jamie Macaulay
 *
 */
public class CheckWavHeadersPane extends PamBorderPane {


	/**
	 * The text area.
	 */
	private TextArea textArea;

	/**
	 * The folder name label.
	 */
	private Label folderName;

	/**
	 * Progress bar showing progress of file write.
	 */
	private ProgressBar progressBar;
	
	
	private boolean running, ran;

/**
 * Check file workers. 
 */
	private CheckFiles checkFilesWorker;
	
	/**
	 * The files. 
	 */
	private ArrayList<File> allFiles = new ArrayList<File>();

	/**
	 * The folder input system. 
	 */
	private FolderInputSystem folderInputSystem;

	/**
	 * Look at sub folders. 
	 */
	private boolean subFolders;

	/**
	 * The folder. 
	 */
	private File folder;

	/**
	 * The number of files. 
	 */
	private int nFiles;

	private int doneFiles;

	private int nErrors;

	/**
	 * Simple double property.
	 */
	private SimpleDoubleProperty progressProperty = new SimpleDoubleProperty(0);

	private PamButton runButton;

	/**
	 * Constructor for the CheckWavHeadersPane
	 * @param folderInputSystem - the folder input system. 
	 */
	public CheckWavHeadersPane(FolderInputSystem folderInputSystem) {
		//this.setCenter(new Label("Hello fix wav pane"));
		
		PamVBox mainPane = new PamVBox(); 
		mainPane.setSpacing(5);
		
		this.folderInputSystem=folderInputSystem;

		folderName = new Label(" ");
		//PamGuiManagerFX.titleFont2style(folderName);
		
		textArea = new TextArea();
		textArea.setEditable(false);
		ScrollPane scrollPane = new ScrollPane(textArea);
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		//scrollPane.setPrefSize(322, 300);
		
		PamHBox pamHBox = new PamHBox();
		pamHBox.setAlignment(Pos.CENTER_LEFT);
		pamHBox.setSpacing(5);
		
		runButton = new PamButton();
		runButton.setGraphic(PamGlyphDude.createPamIcon("mdi2p-play"));
		runButton.setOnAction((action)->{
			checkFiles();
		});

		progressBar = new ProgressBar();
		PamHBox.setHgrow(progressBar, Priority.ALWAYS);
		progressBar.prefHeightProperty().bind(runButton.heightProperty());
		
		pamHBox.getChildren().addAll(runButton, progressBar);
		progressBar.setMaxWidth(Double.MAX_VALUE);
		
		mainPane.getChildren().addAll(folderName, textArea, pamHBox);
		
		this.setCenter(mainPane);

		this.setPadding(new Insets(5,0,5,15));
	}
	
	
	void setParams() {
		running = ran = false;
		subFolders = folderInputSystem.getFolderInputParameters().subFolders;
		if (subFolders) {
			folderName.setText(folderInputSystem.getCurrentFolder() + " + sub folders");
		}
		else {
			folderName.setText(folderInputSystem.getCurrentFolder());
		}
		
		if (folderInputSystem.getCurrentFolder()!=null){
			folder = new File(folderInputSystem.getCurrentFolder());
		}
		else folder = null; 
			
		textArea.setText(" ");
		allFiles.clear();
		nFiles = countFiles(folder);
		progressProperty.setValue(0);
		progressBar.progressProperty().bind(progressProperty);
		//progressBar.setMaximum(Math.max(nFiles, 1));
		enableControls();
	}
	
	
	private int countFiles(File folder) {
		int nF = 0;
		File[] files = folder.listFiles(new PamAudioFileFilter());
		if (files == null) return 0;
		File file;
		for (int i = 0; i < files.length; i++) {
			file = files[i];
			if (file.isDirectory() && subFolders) {
				System.out.println(file.getAbsoluteFile());
				nF += countFiles(file.getAbsoluteFile());
			}
			else if (file.isFile()) {
				allFiles.add(file);
				nF++;
			}
		}
		return nF;
	}
	
	/**
	 * Return true if there is an error
	 * @param aFile file to check, aif or wav
	 * @return true if there is an error
	 */
	private int checkFile(File aFile) {
		if (aFile.exists() == false || aFile.isDirectory() == true) {
			return AudioFileFuncs.FILE_DOESNTEXIST;
		}
		String fileName = aFile.getName();
		// get the bit after the dot
		int dotPos = fileName.lastIndexOf('.');
		if (dotPos < 0) {
			return AudioFileFuncs.FILE_UNKNOWNTYPE;
		}
		String fileEnd = fileName.substring(dotPos+1);
		if (fileEnd.equalsIgnoreCase("wav")) {
			return checkWavFile(aFile);
		}
		else if (fileEnd.equalsIgnoreCase("aif")) {
			return checkAifFile(aFile);
		} 
		return AudioFileFuncs.FILE_UNKNOWNTYPE;
	}
	
	private void enableControls() {
		//getOkButton().setEnabled(nFiles > 0 & running == false && ran == false);
		//getCancelButton().setEnabled(running == false);
	}
	

	private void checkFiles() {
		running = true;
		textArea.setText(String.format("Checking %d files ...\n", nFiles));
		doneFiles = 0;
		nErrors = 0;
		enableControls();
		checkFilesWorker = new CheckFiles();

        Thread th = new Thread(checkFilesWorker);
        th.setDaemon(true);
        th.start();
	}
	

	private void jobDone() {
		running = false;
		ran = true;
		enableControls();
		textArea.appendText(String.format("\n\n%d file headers contained errors", nErrors));
	}
	

	private int checkAifFile(File file) {
		return AudioFileFuncs.FILE_CANTOPEN;
	}
	

	private int checkWavFile(File aFile) {
		return WavFileFuncs.checkHeader(aFile, true);
	}
	
	
	private class CheckFiles extends Task<Integer> {

		@Override
		protected Integer call() throws Exception {
			
			try {
			/*
			 *  need to loop over files again
			 *  for each file, report on progress with it's name and 
			 *  whether or not it had an error
			 */
			File aFile;
			System.out.println("Analaysing files: Start: " + allFiles.size() );
			for (int i = 0; i < allFiles.size(); i++) {
				System.out.println("Analaysing files: " + i);
				final int error = checkFile(aFile = allFiles.get(i));
				final File aFile1 = aFile;
				Platform.runLater(()->{
					textArea.appendText(String.format("\n File %s %s" , aFile1.getName() , 
							error == AudioFileFuncs.FILE_OK ? "OK" : ("Error " + error)));
				});
				//progressBar.progressProperty().bind(null);
				progressProperty.setValue(100*i/(double) allFiles.size());
			}
			return null;
			}
			catch (Exception e) {
				e.printStackTrace();
				return null; 
			}
		}

		@Override
		protected void done() {
			super.done();
			jobDone();
		}

	}

	

}
