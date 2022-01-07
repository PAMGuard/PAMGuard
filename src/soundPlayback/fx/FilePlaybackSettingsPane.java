package soundPlayback.fx;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import PamguardMVC.PamDataBlock;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;
import soundPlayback.FilePlayback;
import soundPlayback.FilePlaybackDevice;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackParameters;


/**
 * The settings pane for file playback. 
 * @author Jamie Macaulay
 *
 */
public class FilePlaybackSettingsPane extends PlaybackSettingsPane {

	private static final int FSLEN = 9;

	private PamVBox panel;

	private PlaybackControl playbackControl;

	private ComboBox<String> deviceTypes;

	private ComboBox<String> soundCards;

	private Label inputSampleRate;

	private Spinner<Double> playbackSpeed;

	private Spinner<Double> outputSampleRate;

	private Label decimateInfo;

	private FilePlayback filePlayback;

	private PlaybackParameters playbackParameters;

	private PamButton defButton;

	private boolean isRT;

	private PamDataBlock dataSource;

	/**
	 * Dialog component for sound playback when input is from a file. 
	 * <p>
	 * Have now implemented a system whereby playback can be over 
	 * a number of device types. For now this will be sound cards and NI 
	 * cards so that we can generate real audio data at V high frequency
	 * for some real time testing. 
	 * <p>
	 * Playback from file is easy since there is no need to synchronise sound input with 
	 * sound output. 
	 * @param playbackControl
	 */
	public FilePlaybackSettingsPane(FilePlayback filePlayback) {

		this.filePlayback = filePlayback;
		this.playbackControl = filePlayback.getPlaybackControl();

		panel = new PamVBox();
		panel.setSpacing(5); 
		panel.setPadding(new Insets(5,0,0,0));

		Label label = new Label("Options"); 
		PamGuiManagerFX.titleFont2style(label);

		//the main holder panes. 
		PamVBox vBox = new PamVBox(); 
		vBox.setSpacing(5);


		deviceTypes = new ComboBox<String>();
		deviceTypes.prefWidthProperty().bind(vBox.widthProperty());
		deviceTypes.setOnAction(action->{
			new NewDeviceType();
		});

		soundCards = new ComboBox<String>();
		soundCards.prefWidthProperty().bind(vBox.widthProperty());
		soundCards.setOnAction(action->{
			new NewSoundCard();
		});

		vBox.getChildren().add(label);

		vBox.getChildren().add(new Label("Output device type"));
		vBox.getChildren().add(deviceTypes);

		vBox.getChildren().add(new Label("Output device name"));
		vBox.getChildren().add(soundCards);


		PamHBox sampleRatePane = new PamHBox(); 
		sampleRatePane.setSpacing(5.0);
		sampleRatePane.setAlignment(Pos.CENTER_LEFT);

		sampleRatePane.getChildren().add(decimateInfo = new Label("-"));
//		decimateInfo.setTextAlignment(TextAlignment.CENTER);
//		decimateInfo.setMinWidth(50);
		sampleRatePane.getChildren().add(new Label("from"));
		sampleRatePane.getChildren().add(inputSampleRate = new Label("-"));
		sampleRatePane.getChildren().add(new Label("Hz to"));
		sampleRatePane.setPrefWidth(190); //needed to stop changing label size jumping other component.s 
		
		outputSampleRate = new Spinner<Double>(1000., 576000., 48000., 2000.); 
		outputSampleRate.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		outputSampleRate.valueProperty().addListener((obsVal, oldVal, newVal)->{
			playSpeedChange();
		}); 
		outputSampleRate.setPrefWidth(100);

		GridPane pane = new GridPane();
		//pane.prefWidthProperty().bind(panel.widthProperty());
		
		pane.setHgap(5.0);
		pane.setVgap(5.0);
		
		int row=0; 

		pane.add(sampleRatePane, 0, row);
		GridPane.setHgrow(sampleRatePane, Priority.ALWAYS);
		pane.add(outputSampleRate, 1, row);
		pane.add(new Label("Hz"), 2, row);

		//	sampleRatePane.add(sampleRatePane, 1, 1);
		//	sampleRatePane.add(new Label("Hz"), 2, 1);

		row++; 
		playbackSpeed = new Spinner<Double>(1., 1000., 1., 1.); 
		playbackSpeed.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		playbackSpeed.valueProperty().addListener((obsVal, oldVal, newVal)->{
			playSpeedChange();
		}); 

		StringConverter<Double> doubleConverter = new StringConverter<Double>() {
			
			@Override
			public String toString(Double object) {
				return "x" + object;
			}
			
			@Override
			public Double fromString(String string) {
				try {
					return Double.valueOf(string);
				} catch (Exception ex) {
					throw new RuntimeException(ex);}
			}
		};

		playbackSpeed.getValueFactory().setConverter(doubleConverter);
		playbackSpeed.setPrefWidth(100);
		playbackSpeed.getValueFactory().setValue(1.0);

		defButton=new PamButton();
//		defButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.REFRESH, PamGuiManagerFX.iconSize-3));
		defButton.setGraphic(PamGlyphDude.createPamIcon("mdi2r-refresh", PamGuiManagerFX.iconSize-3));
		defButton.setOnAction((action)->{
			playbackSpeed.getValueFactory().setValue(1.0);
			//		if (defaultRate.isSelected()) {
			//			saySampleRate();
			enableControls();
		});
		defButton.prefHeightProperty().bind(playbackSpeed.heightProperty().subtract(3));

//		PamHBox playBackPane = new PamHBox(); 
//		playBackPane.setSpacing(5.0);
//		playBackPane.setAlignment(Pos.CENTER_RIGHT);
		
		row++;
		Label playBack = new Label("Playback speed");
		pane.add(playBack, 0, row);
		GridPane.setHalignment(playBack, HPos.RIGHT);
		pane.add(playbackSpeed, 1, row);
		pane.add(defButton, 2, row);

		panel.getChildren().addAll(label, vBox, pane); 

		//
		//	GridBagLayout layout = new GridBagLayout();
		//	panel.setLayout(layout);
		//	GridBagConstraints c = new PamGridBagContraints();
		//	c.gridwidth = 4;
		//	c.anchor = GridBagConstraints.WEST;
		//	c.fill = GridBagConstraints.HORIZONTAL;
		//	c.gridx = c.gridy = 0;
		//	PamDialog.addComponent(panel, new JLabel("Output device type ..."), c);
		//	c.gridy++;
		//	PamDialog.addComponent(panel, deviceTypes, c);
		//	c.gridy++;
		//	PamDialog.addComponent(panel, new JLabel("Output device name ..."), c);
		//	c.gridy++;
		//	PamDialog.addComponent(panel, soundCards, c);

		//	c.gridy++;
		//	c.gridx = 0;
		//	c.gridwidth = 1;
		//	panel.add(new JLabel("Source sample rate ", JLabel.RIGHT), c);
		//	c.gridx++;
		//	panel.add(inputSampleRate = new JTextField(FSLEN), c);
		//	c.gridx++;
		//	panel.add(new JLabel(" Hz"), c);
		//	c.gridy++;
		//	c.gridx = 0;
		//	panel.add(new JLabel("Output sample rate ", JLabel.RIGHT), c);
		//	c.gridx++;
		//	panel.add(outputSampleRate = new JTextField(FSLEN), c);
		//	c.gridx++;
		//	panel.add(new JLabel(" Hz"), c);
		//	c.gridx=1;
		//	c.gridy++;
		//	c.gridwidth = 3;
		//	panel.add(decimateInfo = new JLabel(), c);
		//	c.gridx = 0;
		//	c.gridy++;
		//	c.gridwidth = 1;
		//	panel.add(new JLabel("Playback Speed"), c);
		//	c.gridx +=1;
		//	panel.add(playbackSpeed = new JTextField(5), c);
		//	c.gridx++;
		//	panel.add(defButton = new Button("Default"), c);
		//	c.gridy++;
		//	c.gridx = 0;
		//	c.gridwidth = 4;
		//	playSpeedSlider = new PlaySpeedSlider();
		//	panel.add(playSpeedSlider.getSlider(), c);
		//	
		//	defButton.addActionListener(new DefSampleRateAction());
		//	playbackSpeed.setEditable(false);
		//	inputSampleRate.setEditable(false);
		//	playSpeedSlider.addChangeListener(new ChangeListener() {
		//		@Override
		//		public void stateChanged(ChangeEvent e) {
		//			playSpeedChange();
		//		}
		//	});

		//	outputSampleRate.addFocusListener(new FocusAdapter() {
		//		@Override
		//		public void focusLost(FocusEvent e) {
		//			sayDecimateInfo();
		//		}
		//	});


		deviceTypes.setTooltip(new Tooltip("Select type of output device"));
		soundCards.setTooltip(new Tooltip("Select output device"));
		inputSampleRate.setTooltip(new Tooltip("new Tooltip(This is the sample rate of the incoming data."));
		outputSampleRate.setTooltip(new Tooltip(PamUtilsFX.htmlToNormal("<html>This is the sample rate data will be played back at.<br>"+
				"Data will automatically be decimated or upsampled to convert from input sample rate to the output sample rate."+
				"<br>It must be set to a rate that is possible with the selected output device.")));
		playbackSpeed.setTooltip(new Tooltip("This is the playback speed relative to the true data speed." ));

	}


	private void playSpeedChange() {
		//playbackSpeed.setText(playSpeedSlider.getRatioString());
		sayDecimateInfo();
	}

	/**
	 * Get the decimate string. 
	 * @return the decimate string. 
	 */
	private String getDecimateString() {

		String decimateString = null;
		double inFS =  playbackControl.getPlaybackProcess().getSampleRate();
		double outFS = inFS;

		outFS = outputSampleRate.getValue();

		double deciFac = inFS/outFS*playbackSpeed.getValue(); 
		if (deciFac == 1) {
			decimateString = "No Decimator";
		}
		else if (deciFac > 1.) {
			decimateString = String.format("Decimate x%3.1f", deciFac);
		}
		else {
			decimateString = String.format("Upsample x%3.1f", 1./deciFac);
		}

		return decimateString;

	}

	/**
	 * Work out what the decimation rate is and say it. 
	 */
	private void sayDecimateInfo() {
		decimateInfo.setText(getDecimateString());
	}


	@Override
	public void dataSourceChanged(PamDataBlock dataSource) {
		if (dataSource == null) {
			return;
		}
		this.dataSource = dataSource;
		if (this.playbackParameters != null) {
			setParams(playbackParameters);
		}
	}

	private double getInputRate() {
		if (dataSource != null) {
			return dataSource.getSampleRate();
		}
		else {
			return playbackControl.getPlaybackProcess().getSampleRate();
		}
	}

	@Override
	PlaybackParameters getParams(PlaybackParameters playbackParameters) {
		playbackParameters.deviceType = deviceTypes.getSelectionModel().getSelectedIndex();
		playbackParameters.deviceNumber = soundCards.getSelectionModel().getSelectedIndex();
		//	playbackParameters.defaultSampleRate = defaultRate.isSelected();
		double sourceSR = playbackControl.getSourceSampleRate();
		try {
			double playbackRate =outputSampleRate.getValue();
			playbackParameters.setPlaybackRate((float) playbackRate); 
			playbackParameters.setPlaybackSpeed(playbackSpeed.getValue());
		}
		catch (NumberFormatException ex) {
			return null;
		}
		return playbackParameters;
	}

	@Override
	public void setParams(PlaybackParameters playbackParameters) {


		isRT = playbackControl.isRealTimePlayback();
		this.playbackParameters = playbackParameters;
		deviceTypes.getItems().clear();
		for (int i = 0; i < filePlayback.getFilePBDevices().size(); i++) {
			deviceTypes.getItems().add(filePlayback.getFilePBDevices().get(i).getName());
		}
		deviceTypes.getSelectionModel().select(playbackParameters.deviceType);

		double fs = getInputRate();
		inputSampleRate.setText(String.format("%3.1f", fs));
		outputSampleRate.getValueFactory().setValue((double) playbackParameters.getPlaybackRate());
		this.playbackSpeed.getValueFactory().setValue(playbackParameters.getPlaybackSpeed());
		playSpeedChange();
		//	
		//	soundCards.removeAllItems();
		//	ArrayList<Info> mixers = SoundCardSystem.getOutputMixerList();
		//	for (int i = 0; i < mixers.size(); i++) {
		//		soundCards.addItem(mixers.get(i).getName());
		//	}
		//	soundCards.setSelectedIndex(playbackParameters.deviceNumber);

		//	defaultRate.setSelected(playbackParameters.defaultSampleRate);

		//	saySampleRate();

		fillDeviceSpecificList();

		enableControls();
	}

	private class NewDeviceType implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			fillDeviceSpecificList();
			//		PlaybackDialog parentDialog = getParentDialog();
			//		if (parentDialog != null) {
			//			parentDialog.pack();
			//		}
		}

	}

	private class NewSoundCard implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (filePlayback != null) {
				filePlayback.notifyObservers();
			}

		}
	}

	public void fillDeviceSpecificList() {
		int deviceType = deviceTypes.getSelectionModel().getSelectedIndex(); 
		deviceType = Math.max(0, deviceType);
		FilePlaybackDevice selectedDeviceType = filePlayback.getFilePBDevices().get(deviceType);
		soundCards.getItems().clear();
		String[] devList = selectedDeviceType.getDeviceNames();
		for (int i = 0; i < devList.length; i++) {
			soundCards.getItems().add(devList[i]);
		}
		if (playbackParameters.deviceNumber < devList.length) {
			soundCards.getSelectionModel().select(playbackParameters.deviceNumber);
		}
	}

	//void saySampleRate() {
	//	double playbackRate;
	//	try {
	//		playbackRate = Float.valueOf(sampleRate.getText());
	//	}
	//	catch (NumberFormatException ex) {
	//		playbackRate = 0;
	//	}
	//	
	//	playbackRate = playbackControl.getSourceSampleRate(); 
	//	playbackRate = playbackControl.playbackParameters.getPlaybackSpeed(playbackRate);
	//	
	//	if (defaultRate.isSelected() || playbackRate == 0) {
	//		playbackRate = playbackControl.playbackProcess.getSampleRate();
	//	}
	//	
	//	sampleRate.setText(String.format("%.0f", playbackRate));
	//	
	//}

	private void enableControls() {
		//	boolean ad = autoDecimate.isSelected();
		//	defaultRate.setEnabled(ad == false);
		//	if (ad == true) {
		//		defaultRate.setSelected(false);
		//	}
		//	sampleRate.setEnabled(defaultRate.isSelected() == false);
		this.playbackSpeed.setDisable(isRT);
		defButton.setDisable(isRT);
		if (isRT) {
			this.playbackSpeed.getValueFactory().setValue(1.0);
		}
	}


	Pane getPane() {
		// TODO Auto-generated method stub
		return panel; 
	}

}
