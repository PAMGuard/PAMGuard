package Acquisition.layoutFX;

import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.PopOver;

import Acquisition.filedate.FileDate;
import Acquisition.filedate.FileDateObserver;
import PamUtils.PamCalendar;
import PamUtils.worker.filelist.WavFileType;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Duration;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxStyles.PamStylesManagerFX;

/**
 * Shows  the file date. 
 * 
 * @author Jamie Macaulay
 *
 */
public class FileDataDialogStripFX extends PamBorderPane {

	/**
	 * Reference to the file date class. 
	 */
	private FileDate fileDate;

	/**
	 * The format label. 
	 */
	private Label formatLabel;

	/**
	 * The file time text field. 
	 */
	private TextField fileTime;

	/**
	 * the settings button
	 */
	private PamButton settingsButton;

	/**
	 * Observers for the file data. 
	 */
	private ArrayList<FileDateObserver> observers = new ArrayList<>();

	/**
	 * Pop over to show advanced settings. 
	 */
	private PopOver popOver;

	/**
	 * Advanced settings for the time parser. 
	 */
	private FileDatePane<?> advDatePane;

	/**
	 * The current list of audio files. 
	 */
	private ObservableList<WavFileType> currentFileList;

	/**
	 * Button to move to the next date. 
	 */
	private PamButton prevButton;

	/**
	 * Button to move to the previous date. 
	 */
	private PamButton nextButton;

	private int index = 0; 


	public FileDataDialogStripFX(FileDate fileDate) {
		this.fileDate = fileDate;

		advDatePane =  fileDate.doSettings(); 

//		settingsButton = new PamButton("",PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS, PamGuiManagerFX.iconSize));
		settingsButton = new PamButton("",PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize));
		formatLabel = new Label("");

		prevButton = new PamButton(); 
//		prevButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.CHEVRON_LEFT, PamGuiManagerFX.iconSize));
		prevButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-left", PamGuiManagerFX.iconSize));
		prevButton.setOnAction((action)->{
			setFileTime(--index); 
			enableControls(); 
		});
		prevButton.setPrefWidth(15);
		prevButton.setStyle("-fx-border-color:  transparent;");

		nextButton = new PamButton(); 
//		nextButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.CHEVRON_RIGHT, PamGuiManagerFX.iconSize));
		nextButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-right", PamGuiManagerFX.iconSize));
		nextButton.setOnAction((action)->{
			setFileTime(++index);
			enableControls(); 
		});
		nextButton.setPrefWidth(15);
		nextButton.setStyle("-fx-border-color:  transparent;");


		fileTime = new TextField();
		fileTime.prefHeightProperty().bind(settingsButton.heightProperty());
		fileTime.setEditable(false);
		fileTime.setText("Start time  |  File name");

		PamGridPane gridPane = new PamGridPane(); 

		gridPane.setHgap(0);
		gridPane.setVgap(5);

		Label dateFormat = new Label("Date format"); 
		PamGuiManagerFX.titleFont2style(dateFormat);
		gridPane.add(dateFormat, 0, 0);
		GridPane.setColumnSpan(dateFormat, 3);

		//gridPane.add(formatLabel, 1, 0);

		gridPane.add(new Label("File date"), 0, 1);
		gridPane.add(prevButton, 1, 1);
		gridPane.add(fileTime, 2, 1);
		gridPane.add(nextButton, 3, 1);
		gridPane.add(settingsButton, 4, 1);

		GridPane.setHgrow(fileTime, Priority.ALWAYS);

		settingsButton.setOnAction((action)->{
			settingsButton();
		}
				);

		settingsButton.setDisable(!fileDate.hasSettings());
		settingsButton.setTooltip(new Tooltip("File date settings and options"));

		if (advDatePane==null) {
			settingsButton.setDisable(true);
		}
		else advDatePane.setParams();

		enableControls(); 
		setCenter(gridPane); 
	}

	/**
	 * Called whenever the settings button is pressed. 
	 */
	private void settingsButton() {
		showAdvPane(settingsButton);
	}

	//	/**
	//	 * Set the date in the text box
	//	 * @param fileDateMillis
	//	 */
	//	public void setDate(long fileDateMillis) {
	//		fileTime.setText(PamCalendar.formatDateTime(fileDateMillis));
	//	}

	public void setFormat(String format) {
		formatLabel.setText(format);
	}

	private void notifyObservers() {
		for (FileDateObserver obs:observers) {
			obs.fileDateChange(fileDate);
		}
	}

	public void addObserver(FileDateObserver observer) {
		observers.add(observer);
	}

	public boolean removeObserver(FileDateObserver observer) {
		return observers.remove(observer);
	}

	/**
	 * Sho0w the advanced settings. 
	 * @param advSettingsButton - the advanced settings. 
	 */
	private void showAdvPane(PamButton advSettingsButton) {

		if (popOver==null) {
			popOver = new PopOver(advDatePane.getContentNode()); 
			popOver.setFadeInDuration(new Duration(100));
			popOver.setFadeOutDuration(new Duration(100));
		}

		popOver.showingProperty().addListener((obs, old, newval)->{
			
			
			if (!newval) {
				System.out.println("Get params"); 
				advDatePane.getParams();
			}
		});


		popOver.show(advSettingsButton);

		((Parent) popOver.getSkin().getNode()).getStylesheets()
		.add(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getSlidingDialogCSS());

		advDatePane.setParams();
	}

	public FileDatePane<?> getAdvDatePane() {
		return advDatePane;
	}

	/**
	 * Enable the controls. 
	 */
	private void enableControls() {
		prevButton.setDisable(false);
		nextButton.setDisable(false);
		settingsButton.setDisable(false);
		fileTime.setDisable(false);

		if (currentFileList== null) {
			prevButton.setDisable(true);
			nextButton.setDisable(true);
			//settingsButton.setDisable(true);
			fileTime.setDisable(true);
			return; 
		}
		if (index==0) prevButton.setDisable(true);
		if (index==currentFileList.size()-1) nextButton.setDisable(true);
	}

	/**
	 * Set the file time. 
	 */
	public void setFileTime(int i) {
		this.index = i; 

		if (currentFileList== null || currentFileList.size()==0) {
			this.fileTime.setText("");
			return; 
		}

		if (index<0) index = 0; 
		else if (index>currentFileList.size()-1) index = currentFileList.size()-1; 

		long timeMillis = fileDate.getTimeFromFile(currentFileList.get(i));

		String dateTime = PamCalendar.formatDBDateTime(timeMillis, true); 

		dateTime += "   |   ";

		dateTime +=  FilenameUtils.getName(currentFileList.get(i).getAbsolutePath()); 

		this.fileTime.setText(dateTime);
	}

	/**
	 * Called whenever there is a new file list (Might just contain a single file); 
	 * @param fileList - the file list. 
	 */
	public void setFileList(ObservableList<WavFileType> fileList) {
		this.currentFileList = fileList; 
		setFileTime(0); //set the first file time in the list to show. 
		
		advDatePane.setFilelist(fileList); 

		advDatePane.setParams(); 


		enableControls(); 
	}


}
