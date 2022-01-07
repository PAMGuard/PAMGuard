package Acquisition.layoutFX;

import java.util.TimeZone;

import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.textfield.CustomTextField;

import Acquisition.filedate.StandardFileDate;
import Acquisition.filedate.StandardFileDateSettings;
import PamUtils.PamCalendar;
import PamUtils.worker.filelist.WavFileType;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

/**
 * 
 * Settings pane for a standard file date.
 * 
 * @author Jamie Macaulay
 *
 */
public class StandardFileDatePane extends FileDatePane<StandardFileDateSettings> {

	PamBorderPane mainPane;

	/**
	 * The grid pane which holds manual date controls. 
	 */
	private PamGridPane manualDatePane;

	/**
	 * Text field which allows the user to set a custom date format. 
	 */
	private CustomTextField dateFromatTxtField;

	private Label dateExampleLabel;

	/**
	 * Combo box which allows users to select a time zone. 
	 */
	private ComboBox<String> timeZoneBox;

	private String[] timeZoneIds;

	private PamToggleSwitch autoDate;

	private PamToggleSwitch dayLightSavings;

	private PamSpinner<Double> additionalOffset;

	private StandardFileDate standardFileDate;

	private ObservableList<WavFileType> currentFiles; 

	public StandardFileDatePane(StandardFileDate standardFileDate) {
		super();
		this.standardFileDate=standardFileDate; 
		mainPane = new PamBorderPane(); 
		//		System.out.println("Standard File data pane"); 
		mainPane.setCenter(createDatePane());
		this.setParams();
		mainPane.setPrefWidth(350);
	}


	private Pane createDatePane() {
		PamVBox holder = new PamVBox(); 
		holder.setSpacing(5);
		holder.setPadding(new Insets(5,5,5,5));

		Label autoDateLabel = new Label("Date-Time Format"); 
		PamGuiManagerFX.titleFont2style(autoDateLabel);

		autoDate = new PamToggleSwitch("Auto date"); 
		autoDate.selectedProperty().addListener((obsval, oldVal, newVal)->{
			manualDatePane.setDisable(newVal);
			setDateTextField(standardFileDate.getSettings());
		});

		//create a text field to allow the users to set a custom date format. 
		manualDatePane = new PamGridPane(); 
		manualDatePane.setHgap(5);
		manualDatePane.setVgap(5);

		String text = "<html>Use # to replace all non-date numeric charcters. Count carefully!<br>" +
				"e.g. to get a date from a file 1677738025.180912065628.d24.d8.wav use <br>###########yyMMddhhmmss########</html>";

		text += "<html><body style='width: 350px'>" + 
				"See <a href=\"https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html\">"
				+ "https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html</a> for " +
				"information on date and time codes, as well as examples of common formats.";

		PamButton helpButton = new PamButton(); 
//		helpButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.HELP_CIRCLE_OUTLINE, 15));
		helpButton.setGraphic(PamGlyphDude.createPamIcon("mdi2h-help-circle-outline", 15));
		final String message = text; 
		helpButton.setOnAction((action) ->{
			PamDialogFX.showMessageDialog("Manual Date Format", PamUtilsFX.htmlToNormal(message));
		});
		helpButton.setStyle("-fx-border-color: transparent; -fx-background-color: transparent");
		helpButton.setMaxSize(16, 16);

		dateFromatTxtField = new CustomTextField(); 
		dateFromatTxtField.setRight(helpButton); 
//		dateFromatTxtField.setOnKeyReleased(event -> {
//			if (event.getCode() == KeyCode.ENTER){
//				// do what is to do
//				this.getParams();
//				setExampeLabel(standardFileDate.getSettings()); 
//			}
//		});
		
		dateFromatTxtField.textProperty().addListener((obsVal, oldVal, newVal) ->{
			//the property
			setExampeLabel(standardFileDate.getSettings()); 
	    });


		dateFromatTxtField.setTooltip(
				new Tooltip(PamUtilsFX.htmlToNormal(text))); 

		PamGridPane.setHgrow(dateFromatTxtField, Priority.ALWAYS);
		PamGridPane.setColumnSpan(dateFromatTxtField, 2);

		int row = 0; 

		manualDatePane.add(new Label("Format"), 0, row);
		manualDatePane.add(dateFromatTxtField, 1, row);

		row++; 
		//create a pane to show examples of the current date format
		manualDatePane.add(new Label("Example: "), 0, row);
		manualDatePane.add(dateExampleLabel = new Label(""), 1, row);

		//Create a pane to show time zones
		Label timeZoneLabel = new Label("Time Zone"); 
		PamGuiManagerFX.titleFont2style(timeZoneLabel);

		PamHBox timeZonePane = new PamHBox();  
		timeZonePane.setSpacing(5);

		timeZoneBox  = new ComboBox<String>(); 
		timeZoneIds = TimeZone.getAvailableIDs();
		TimeZone tz;
		String tzStr;
		for (int i = 0; i < timeZoneIds.length; i++) {
			tz = TimeZone.getTimeZone(timeZoneIds[i]);
			if (tz.getRawOffset() < 0) {
				tzStr = String.format("UTC%3.1f %s (%s)", (double)tz.getRawOffset()/3600000., tz.getID(), tz.getDisplayName());
			}
			else {
				tzStr = String.format("UTC+%3.1f %s (%s)", (double)tz.getRawOffset()/3600000., tz.getID(), tz.getDisplayName());
			}
			timeZoneBox.getItems().add(tzStr);
		}

		row = 0; 
		Label timeZone = new Label("Time zone"); 
		timeZone.setMinWidth(60);
		timeZonePane.setAlignment(Pos.CENTER_LEFT);
		timeZonePane.getChildren().addAll(timeZone, timeZoneBox);
		//		timeZonePane.getChildren().add(timeZoneBox, 1, row);

		dayLightSavings = new PamToggleSwitch("Use daylight savings"); 
		dayLightSavings.setAlignment(Pos.CENTER_LEFT);
		row++;
		//		
		//		timeZonePane.add(dayLightSavings, 0, row);
		//		PamGridPane.setColumnSpan(dayLightSavings, 3);


		//		PamHBox autoDateHolder = new PamHBox(); 
		//		autoDateHolder.setSpacing(5);
		//		ToggleSwitch toggleSwitch = new ToggleSwitch(); 
		//		toggleSwitch.setMaxWidth(20);
		//		autoDateHolder.getChildren().addAll(toggleSwitch, new Label("Auto date")); 
		//		toggleSwitch.selectedProperty().addListener((obsval, oldVal, newVal)->{
		//			manualDataPane.setDisable(newVal);
		//		});

		row++; 
		additionalOffset = new PamSpinner<Double>(0.0,Double.MAX_VALUE,0.0,1.0); 
		additionalOffset.setPrefWidth(80);
		additionalOffset.setEditable(true);
		additionalOffset.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);

		PamHBox addiotonalOffsetPane  = new PamHBox(); 
		addiotonalOffsetPane.setAlignment(Pos.CENTER_LEFT);
		addiotonalOffsetPane.setSpacing(5);
		addiotonalOffsetPane.getChildren().addAll(new Label("Add"), 
				additionalOffset, new Label("seconds to each file"));


		holder.getChildren().addAll(autoDateLabel, autoDate, manualDatePane, timeZoneLabel,
				timeZonePane, dayLightSavings, addiotonalOffsetPane);
		
		
		return holder; 
	}


	@Override
	public StandardFileDateSettings getParams(StandardFileDateSettings currParams) {
		
		StandardFileDateSettings standardFileSettings = currParams.clone(); 

		//standardFileSettings.setUseBespokeFormat(!autoDate.isSelected());
		standardFileSettings.setUseBespokeFormat(false);


		if (standardFileSettings.isUseBespokeFormat()) {
			standardFileSettings.setForcedDateFormat(dateFromatTxtField.getText());
		}

		if (timeZoneBox.getSelectionModel().getSelectedItem()!=null) {
			standardFileSettings.setTimeZoneName(timeZoneBox.getSelectionModel().getSelectedItem());
		}
		standardFileSettings.setAdjustDaylightSaving(dayLightSavings.isSelected());


		standardFileSettings.setAdditionalOffsetMillis((long) this.additionalOffset.getValue().doubleValue()*1000);


		return standardFileSettings;
	}

	@Override
	public void setParams(StandardFileDateSettings standardFileDateSettings) {
		int idInd = getIdIndex(standardFileDateSettings.getTimeZoneName());
		
		System.out.println("Time zone name: " +  standardFileDateSettings.getTimeZoneName() + "  " + idInd); 
		if (idInd >= 0) {
			timeZoneBox.getSelectionModel().select(idInd);
		}
		
		autoDate.setSelected(!standardFileDateSettings.isUseBespokeFormat());
		dayLightSavings.setSelected(standardFileDateSettings.isAdjustDaylightSaving());

		//	forcePCTime.setSelected(standardFileDateSettings.isForcePCTime());
		additionalOffset.getValueFactory().setValue((double) standardFileDateSettings.getAdditionalOffsetMillis() / 1000.);

		setDateTextField(standardFileDateSettings);

		manualDatePane.setDisable(autoDate.isSelected());
		setDateTextField(standardFileDate.getSettings());

		//dateFromatTxtField.setText(standardFileDateSettings.getForcedDateFormat());

		//soundTrapDate.setVisible(allowCustomFormats);
		//enableContols();
	}

	/**
	 * Set the text in the date field.
	 * @param standardFileDateSettings
	 */
	private void setDateTextField(StandardFileDateSettings standardFileDateSettings) {
		if (autoDate.isSelected()) {
			dateFromatTxtField.setText(standardFileDateSettings.getDateTimeFormatToUse());
		}
		else {
			dateFromatTxtField.setText(standardFileDateSettings.getForcedDateFormat());
		}
	}

	/**
	 * Set the example label
	 * @param standardFileDateSettings
	 */
	private void setExampeLabel(StandardFileDateSettings standardFileDateSettings) {
		dateExampleLabel.setTextFill(Color.WHITE);
		if (currentFiles==null) {
			dateExampleLabel.setText("No loaded files to test date format");
		}
		else {
			long timeMillis = standardFileDate.getTimeFromFile(this.currentFiles.get(0)); 
			if (timeMillis==0) {
				dateExampleLabel.setText("Could not parse date format:");
				dateExampleLabel.setTextFill(Color.RED);

			}
			else {
				String dateTime = PamCalendar.formatDBDateTime(timeMillis, true); 

				dateTime += "   |   ";

				dateTime +=  FilenameUtils.getName(this.currentFiles.get(0).getAbsolutePath()); 
				dateExampleLabel.setText(dateTime);
			}
		}

	}

	/**
	 * Get ID index. 
	 * @param tzId - the time zone index: 
	 * @return the time zone index. 
	 */
	private int getIdIndex(String tzId) {
		//System.out.println(tzId); 
		if (tzId == null) {
			tzId = "UTC";
		}
		for (int i = 0; i < timeZoneIds.length; i++) {
			//System.out.println("hello : " + tzId+ " ||| " + timeZoneIds[i]);
			if (tzId.contains(timeZoneIds[i])) {
				return i;
			}
		}
		return -1;
	}


	@Override
	public String getName() {
		return "Standard File Date pane";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub

	}


	@Override
	public void setParams() {
		setParams(standardFileDate.getSettings()); 
	}


	@Override
	public void getParams() {
		StandardFileDateSettings settings = getParams(standardFileDate.getSettings()); 
		if (settings!=null) {
			standardFileDate.setSettings(settings); 
		}
		else {
			System.err.println("Warning: the settings was null"); 
		}
	}


	@Override
	protected void setFilelist(ObservableList<WavFileType> fileList) {
		this.currentFiles = fileList; 
	}


	//	private void setParams() {
	//		int idInd = getIdIndex(standardFileDateSettings.getTimeZoneName());
	//		if (idInd >= 0) {
	//			timeZones.setSelectedIndex(idInd);
	//		}
	//		autoFormat.setSelected(standardFileDateSettings.isUseBespokeFormat() == false);
	//		manualFormat.setSelected(standardFileDateSettings.isUseBespokeFormat());
	//		daylightSaving.setSelected(standardFileDateSettings.isAdjustDaylightSaving());
	//		forcePCTime.setSelected(standardFileDateSettings.isForcePCTime());
	//		additionalOffset.setText(String.format("%5.3f", (double) standardFileDateSettings.getAdditionalOffsetMillis() / 1000.));
	//		customDateTimeFormat.setText(standardFileDateSettings.getForcedDateFormat());
	//		soundTrapDate.setVisible(allowCustomFormats);
	//		enableContols();
	//		this.pack();
	//	}
}
