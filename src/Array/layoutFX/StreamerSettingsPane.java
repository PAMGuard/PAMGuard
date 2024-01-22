package Array.layoutFX;

import org.controlsfx.control.PopOver;

import Array.HydrophoneLocator;
import Array.HydrophoneLocators;
import Array.PamArray;
import Array.Streamer;
import Array.sensors.ArrayParameterType;
import Array.sensors.ArraySensorFieldType;
import Array.streamerOrigin.HydrophoneOriginMethod;
import Array.streamerOrigin.HydrophoneOriginMethods;
import Array.streamerOrigin.HydrophoneOriginSystem;
import Array.streamerOrigin.OriginDialogComponent;
import Array.streamerOrigin.OriginSettings;
import PamController.PamController;
import PamController.SettingsPane;
import PamUtils.LatLong;
import PamguardMVC.PamDataBlock;
import javafx.geometry.Pos;
import javafx.scene.Node;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.validator.PamValidator;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import net.synedra.validatorfx.Validator;


/**
 * A JavaFX settings pane for a streamer. 
 * 
 * @author Jamie Macaulay
 *
 */
public class StreamerSettingsPane extends SettingsPane<Streamer> {

	private final static double MAX_TEXTFIELD_WIDTH = 80; 


	public PamBorderPane mainPane;

	/**
	 * Combo Box which shows which origin methods are available. 
	 */
	private ComboBox<HydrophoneOriginSystem> originMethod;

	/**
	 * The origin pane;
	 */
	private PamBorderPane originPane; 

	/**
	 * The default streamer
	 */
	public Streamer defaultStreamer;


	/**
	 * The current array 
	 */
	private PamArray currentArray;

	/**
	 * The current origin methods
	 */
	private HydrophoneOriginMethod currentOriginMethod;

	/*
	 * The current origin method pane.
	 */
	private Pane currentOriginComponent;

	/**
	 * Interpolation panel
	 */
	private InterpChoicePane interpPane;


	private TextField xPos;


	private Validator validator = new PamValidator();


	private TextField yPos;


	private TextField zPos;


	private TextField zPosErr;


	private TextField xPosErr;


	private Label depthLabel;


	private TextField yPosErr;


	private Label depthLabel2;


	private TextField heading;


	private TextField roll;


	private TextField pitch;


	private ComboBox localiserMethod;


	private SensorSourcePane[] sensorComponents;


	private Label depthSensorLabel;


	/**
	 * Button for extra origin parameters. 
	 */
	private PamButton originButton;



	public StreamerSettingsPane() {
		super(null); 

		mainPane = new PamBorderPane(); 
		mainPane.setCenter(getStreamerPane());

	}

	/**
	 * Create the streamer pane
	 * @return get the pane. 
	 */
	private Pane getStreamerPane(){

		String reciever = PamController.getInstance().getGlobalMediumManager().getRecieverString(); 

		Label label = new Label("Geo-reference Position");
		PamGuiManagerFX.titleFont2style(label);

		//holds advanced setings for new origin methods. 
		originPane = new PamBorderPane();
		PopOver popOver = new PopOver();
		popOver.setContentNode(originPane);

		originMethod = new ComboBox<HydrophoneOriginSystem>();		
		originButton = new PamButton();
		originButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-crosshairs-gps"));
		originButton.setOnAction((a)->{
			popOver.show(originButton);
		});
		originMethod.setMaxWidth(Double.MAX_VALUE);

		PamHBox originHolder = new PamHBox();
		originHolder.setSpacing(5);
		originHolder.setAlignment(Pos.CENTER_LEFT);
		originHolder.getChildren().addAll(originMethod,originButton);
		originHolder.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(originMethod, Priority.ALWAYS);

		int n = HydrophoneOriginMethods.getInstance().getCount();
		for (int i = 0; i < n; i++) {
			originMethod.getItems().add(HydrophoneOriginMethods.getInstance().getMethod(i));
		}

		Label hydroMovementLabel = new Label(reciever +" Movement Model");

		//listener for when a new origin method is called. 
		originMethod.setOnAction((action)->{
			newOriginMethod();
		});

		interpPane = new InterpChoicePane();
		Label inteprlabel = new Label("Interpolation");
		PamGuiManagerFX.titleFont2style(inteprlabel);

		PamHBox interpBox = new PamHBox(); 
		interpBox.setSpacing(5);
		Label interpMethodLabel = new Label("Method");

		Region spacer = new Region();
		spacer.prefWidthProperty().bind(originButton.widthProperty());
		interpBox.getChildren().addAll(interpMethodLabel, interpPane, spacer); 
		interpBox.setAlignment(Pos.CENTER_LEFT);
		interpBox.setMaxWidth(Double.MAX_VALUE);
		interpPane.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(interpPane, Priority.ALWAYS);

		//add all stuff to the holder
		PamVBox holder = new PamVBox();
		holder.getChildren().addAll(label, originHolder, hydroMovementLabel, createLocatorPane(), inteprlabel, interpBox);
		holder.setSpacing(5);


		return holder; 

	}

	/**
	 * Create the locator pane. 
	 * @return the pane containing controls. 
	 */
	public Pane createLocatorPane() {

		localiserMethod = new ComboBox<>();
		int n = HydrophoneLocators.getInstance().getCount();
		for (int i = 0; i < n; i++) {
			localiserMethod.getItems().add(HydrophoneLocators.getInstance().getSystem(i));
		}
		localiserMethod.setMaxWidth(Double.MAX_VALUE);

		PamHBox loclaiserMethodHolder = new PamHBox();
		loclaiserMethodHolder.setSpacing(5);
		loclaiserMethodHolder.setAlignment(Pos.CENTER_LEFT);
		Label spacer = new Label();
		spacer.prefWidthProperty().bind(originButton.widthProperty());
		loclaiserMethodHolder.getChildren().addAll(localiserMethod, spacer);
		loclaiserMethodHolder.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(localiserMethod, Priority.ALWAYS);

		//hydrophone position and 
		PamGridPane positionPane = new PamGridPane(); 
		positionPane.setHgap(5);
		positionPane.setVgap(5);

		ColumnConstraints rc = new ColumnConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, MAX_TEXTFIELD_WIDTH);
		
		//Orientation pane. 
		//create data sources for sensors. 
		ArraySensorFieldType[] sensorFields = ArraySensorFieldType.values();
		sensorComponents = new SensorSourcePane[sensorFields.length];
		//EnableOrientation eo = new EnableOrientation();
		for (int i = 0; i < sensorFields.length; i++) {
			sensorComponents[i] = new SensorSourcePane(sensorFields[i], true, sensorFields[i] != ArraySensorFieldType.HEIGHT);
			sensorComponents[i].setOnAction((e)->{
				enableOrientationPane(); 
			});
		}
		PamButton button = new PamButton("Sensors");
		button.setGraphic(PamGlyphDude.createPamIcon("mdi2c-compass-outline", PamGuiManagerFX.iconSize));

		PopOver popOver = new PopOver(createSensorPane()); 
		popOver.setDetachable(true);

		button.setOnAction((a)->{
			popOver.show(button);
		});


		//this sets all text fields to the correct width - but of naff hack but what grid pane needs to work. 
		for (int i=1; i<5; i++) {
			positionPane.getColumnConstraints().add(rc);
		}

		xPos=new TextField();
		xPos.setMaxWidth(MAX_TEXTFIELD_WIDTH);
		HydrophoneSettingsPane.addTextValidator(xPos, "x position", validator); 
		yPos=new TextField();
		yPos.setMaxWidth(MAX_TEXTFIELD_WIDTH);
		HydrophoneSettingsPane.addTextValidator(yPos, "y position", validator); 
		zPos=new TextField();
		zPos.setMaxWidth(MAX_TEXTFIELD_WIDTH);
		HydrophoneSettingsPane.addTextValidator(zPos, "z position", validator); 


		depthLabel = new Label("Depth"); 
		depthLabel.setAlignment(Pos.CENTER);

		depthSensorLabel = new Label("Depth Sensor"); 
		depthSensorLabel.setAlignment(Pos.CENTER);


		xPosErr=new TextField();
		xPosErr.setMaxWidth(MAX_TEXTFIELD_WIDTH);
		HydrophoneSettingsPane.addTextValidator(xPosErr, "x error", validator); 
		yPosErr=new TextField();
		yPosErr.setMaxWidth(MAX_TEXTFIELD_WIDTH);
		HydrophoneSettingsPane.addTextValidator(yPosErr, "y error", validator); 
		zPosErr=new TextField();
		zPosErr.setMaxWidth(MAX_TEXTFIELD_WIDTH);
		depthLabel2 = new Label(""); //changes with air or water mode. 
		depthLabel2.setAlignment(Pos.CENTER);
		HydrophoneSettingsPane.addTextValidator(zPosErr, "z error", validator); 

		int col=0; 
		int row=0; 


		Label xLabel = new Label("x"); 
		xLabel.setAlignment(Pos.CENTER);

		Label yLabel = new Label("y"); 
		yLabel.setAlignment(Pos.CENTER);

		//Orientations

		String degsLab = LatLong.deg + " ";


		col=1;
		positionPane.add(xLabel, col++, row);
		positionPane.add(yLabel, col++, row);
		positionPane.add(depthLabel, col++, row);
		col++;
		positionPane.add(depthSensorLabel, col++, row);

		col=0;
		row++;

		Label positionLabel = new Label("Position"); 
		positionPane.add(positionLabel, col++, row);
		positionPane.add(xPos, col++, row);
		positionPane.add(yPos, col++, row);
		positionPane.add(zPos, col++, row);
		positionPane.add(new Label("(m)"), col++, row);
		positionPane.add(sensorComponents[ArraySensorFieldType.HEIGHT.ordinal()].getPane(),  col++, row);

		col=0;
		row++;

		Label errLabel = new Label("Error"); 
		positionPane.add(errLabel, col++, row);
		positionPane.add(xPosErr, col++, row);
		positionPane.add(yPosErr, col++, row);
		positionPane.add(zPosErr, col++, row);
		positionPane.add(new Label("(m)"), col++, row);

		//Orientation
		col=1;
		row++;

		Label headingLabel = new Label("Heading"); 
		headingLabel.setAlignment(Pos.CENTER);

		Label pitchLabel = new Label("Pitch"); 
		pitchLabel.setAlignment(Pos.CENTER);

		Label rolllabel = new Label("Roll"); 
		rolllabel.setAlignment(Pos.CENTER);


		positionPane.add(headingLabel, col++, row);
		positionPane.add(pitchLabel, col++, row);
		positionPane.add(rolllabel, col++, row);

		row++;

		heading = new TextField(); 
		heading.setMaxWidth(MAX_TEXTFIELD_WIDTH);
		HydrophoneSettingsPane.addTextValidator(heading, "heading", validator); 

		pitch = new TextField(); 
		pitch.setMaxWidth(MAX_TEXTFIELD_WIDTH);
		HydrophoneSettingsPane.addTextValidator(pitch, "pitch", validator); 
		
		roll = new TextField(); 
		roll.setMaxWidth(MAX_TEXTFIELD_WIDTH);
		HydrophoneSettingsPane.addTextValidator(roll, "roll", validator); 
		
		col=0;

		Label orientation = new Label("Orientation"); 
		positionPane.add(orientation, col++, row);
		positionPane.add(heading, col++, row);
		positionPane.add(pitch, col++, row);
		positionPane.add(roll, col++, row);
		positionPane.add(new Label(degsLab), col++, row);
		

		positionPane.add(button,  col++, row);

		PamVBox holder= new PamVBox(); 
		holder.setSpacing(5);
		holder.getChildren().addAll(loclaiserMethodHolder, positionPane); 

		return holder; 
	}

	/**
	 * Enables or disables controls in the orientation pane.
	 */
	private void enableOrientationPane() {
		for (int i=0; i<sensorComponents.length; i++) {
			if (sensorComponents[i]==null || sensorComponents[i].getParameterType()==null) continue;
			boolean enable = sensorComponents[i].getParameterType().equals(ArrayParameterType.FIXED);
			switch (sensorComponents[i].getSensorType()) {
			case HEADING:
				heading.setDisable(!enable);
				break;
			case HEIGHT:
				break;
			case PITCH:
				pitch.setDisable(!enable);
				break;
			case ROLL:
				roll.setDisable(!enable);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Create a pane to set where sensor data comes from
	 * @return the sensor pane. 
	 */
	private Pane createSensorPane() {
		PamBorderPane pane = new PamBorderPane(); 

		//hydrophone position and 
		PamGridPane positionPane = new PamGridPane(); 
		positionPane.setHgap(5);
		positionPane.setVgap(5);

		int col=0; 
		int row=0; 

		positionPane.add(new Label("Heading"), col++, row);
		positionPane.add(sensorComponents[ArraySensorFieldType.HEADING.ordinal()].getPane(), col++, row);

		row++;
		col=0;
		positionPane.add(new Label("Pitch"), col++, row);
		positionPane.add(sensorComponents[ArraySensorFieldType.PITCH.ordinal()].getPane(), col++, row);

		row++;
		col=0;
		positionPane.add(new Label("Roll"), col++, row);
		positionPane.add(sensorComponents[ArraySensorFieldType.ROLL.ordinal()].getPane(), col++, row);

		Label orientLabel = new Label("Orientation Data");
		PamGuiManagerFX.titleFont2style(orientLabel);

		pane.setTop(orientLabel);
		pane.setCenter(positionPane);

		return pane;
	}

	/**
	 * Create a new origin method. 
	 */
	public void newOriginMethod() {

		int methInd = originMethod.getSelectionModel().getSelectedIndex();
		if (methInd < 0) {
			return;
		}
		HydrophoneOriginSystem currentSystem = HydrophoneOriginMethods.getInstance().getMethod(this.originMethod.getSelectionModel().getSelectedIndex());
		currentOriginMethod = currentSystem.createMethod(currentArray, defaultStreamer);
		try {
			OriginSettings os = defaultStreamer.getOriginSettings(currentOriginMethod.getClass());
			if (os != null) {
				currentOriginMethod.setOriginSettings(os);
			}
		}
		catch (Exception e) {
			// will throw if it tries to set the wrong type of settings. 
			e.printStackTrace();
		}

		OriginDialogComponent mthDialogComponent = currentOriginMethod.getDialogComponent(); 

		if (mthDialogComponent == null) {
			originPane.getChildren().clear();
			currentOriginComponent = null;
			this.originButton.setDisable(true);
		}
		else {
			this.originButton.setDisable(false);
			Pane newComponent = mthDialogComponent.getSettingsPane();
			if (currentOriginComponent != newComponent) {
				originPane.setCenter(newComponent);
				currentOriginComponent = newComponent;
				mthDialogComponent.setParams();
			}
		}
		
		enableControls();
	}



	private void enableControls() {
		if (currentOriginMethod != null) {
			System.out.println("Enable: selected interp: "  + interpPane.getSelectedInterpType());

			interpPane.setAllowedValues(currentOriginMethod.getAllowedInterpolationMethods());
			System.out.println("Enable controls: "  + interpPane.getSelectedInterpType());
			if (interpPane.getSelectedInterpType()<0) {
				interpPane.setSelection(0);
			}
		}
		 enableOrientationPane();
	}

	@Override
	public Streamer getParams(Streamer currParams) {
//		System.out.println("GETPARAMS: "	 + currParams); 
		double zCoeff = PamController.getInstance().getGlobalMediumManager().getZCoeff(); 

		try {
			defaultStreamer.setX(Double.valueOf(xPos.getText()));
			defaultStreamer.setY(Double.valueOf(yPos.getText()));
			defaultStreamer.setZ(zCoeff*Double.valueOf(zPos.getText()));
			defaultStreamer.setDx(Double.valueOf(xPosErr.getText()));
			defaultStreamer.setDy(Double.valueOf(yPosErr.getText()));
			defaultStreamer.setDz(Double.valueOf(zPosErr.getText()));		
		}
		catch (NumberFormatException e) {
			System.err.println("Streamer getParams: There is a problem with one of the position parameters in the streamer panel");
			return null;
		}

		defaultStreamer.setStreamerName(currParams.getStreamerName());
		int im = interpPane.getSelectedInterpType();
		System.out.println("Streamer gwetParams: Origin interpolator: " + interpPane.getSelectedInterpType()); 

		if (im < 0) {
			System.err.println("Streamer getParams: There is an index problem with the interpolation selection streamer panel: index = " + im);
		}
		currentArray.setOriginInterpolation(im);
		//			try {
		//				streamer.setBuoyId1(Integer.valueOf(buoyId.getText()));
		//			}
		//			catch (NumberFormatException e) {
		//				streamer.setBuoyId1(null);
		//			}
		HydrophoneLocator locator = HydrophoneLocators.getInstance().
				getSystem(localiserMethod.getSelectionModel().getSelectedIndex()).getLocator(currentArray, defaultStreamer);
		if (originPane != null) {
			//			MasterLocator masterLocator = currentArray.getMasterLocator();
			//			int streamerIndex = currentArray.indexOfStreamer(streamer);
			//			if (streamerIndex < 0) {
			//				streamerIndex = currentArray.getNumStreamers();
			//			}
			//			masterLocator.setHydrophoneLocator(streamerIndex, locator);
			if (currentOriginMethod == null) {
				System.err.println("Streamer getParams: No hydrophoneorigin method selected in streamer panel");
			}
		}
		
		OriginDialogComponent mthDialogComponent = currentOriginMethod.getDialogComponent();
		if (mthDialogComponent != null) {
			if (mthDialogComponent.getParams() == false) {
				System.err.println("Streamer: The origin settings pane returned false suggesting paramters are not correct.");
				return null;
			}
		}

		//			defaultStreamer.setEnableOrientation(enableOrientation.isSelected());
		//			if (enableOrientation.isSelected()) {
		defaultStreamer.setHeading(getDoubleValue(heading));
		defaultStreamer.setPitch(getDoubleValue(pitch));
		defaultStreamer.setRoll(getDoubleValue(roll));
		//			}
		
		if (!heading.isDisable() && defaultStreamer.getHeading() == null) {
			System.err.println("Streamer getParams: You must enter a fixed value for the streamer heading");
		}
		if (!pitch.isDisable() && defaultStreamer.getPitch() == null) {
			System.err.println("Streamer getParams: You must enter a fixed value for the streamer pitch");
		}
		if (!roll.isDisable() && defaultStreamer.getRoll() == null) {
			System.err.println("Streamer getParams: You must enter a fixed value for the streamer roll");
		}

		/**
		 * We may have large lists of the streamers which we meant to use the
		 * orientation data from or not. The enable orientation check box will enable or
		 * disable orientation for ALL streamers which are loaded into memory.
		 */
//		System.out.println("CURRENTORIGINMETHOD: "	 + currentOriginMethod); 
//		System.out.println("LOCATORMETHOD: "		 + locator); 

		defaultStreamer.setHydrophoneOrigin(currentOriginMethod);
		defaultStreamer.setHydrophoneLocator(locator);
		defaultStreamer.setOriginSettings(currentOriginMethod.getOriginSettings());
		defaultStreamer.setLocatorSettings(locator.getLocatorSettings());

		ArraySensorFieldType[] sensorFields = ArraySensorFieldType.values();
		for (int i = 0; i < sensorFields.length; i++) {
			ArrayParameterType fieldType = sensorComponents[i].getParameterType();
			defaultStreamer.setOrientationTypes(sensorFields[i], fieldType);
			if (fieldType == ArrayParameterType.SENSOR) {
				PamDataBlock dataBlock = sensorComponents[i].getDataBlock();
				defaultStreamer.setSensorDataBlocks(sensorFields[i], dataBlock == null ? null : dataBlock.getLongDataName());
			}
		}

		return defaultStreamer;
	}

	@Override
	public void setParams(Streamer input) {
		if (input==null) {
			System.out.print("Streamer setParams: The input streamer is null");
		}
		this.defaultStreamer=input;
		// origin methods
		//		MasterLocator masterLocator = currentArray.getMasterLocator();
		//		int streamerIndex = currentArray.indexOfStreamer(streamer);
		HydrophoneLocator hLocator = defaultStreamer.getHydrophoneLocator();
		if (hLocator != null) {
			int locatorIndex = HydrophoneLocators.getInstance().indexOfClass(hLocator.getClass());
			localiserMethod.getSelectionModel().select(locatorIndex);

			HydrophoneOriginMethod originMethod = defaultStreamer.getHydrophoneOrigin();
			if (originMethod != null) {
				int originIndex = HydrophoneOriginMethods.getInstance().indexOfClass(originMethod.getClass());
				this.originMethod.getSelectionModel().select(originIndex);
			}
		}
		else {
			localiserMethod.getSelectionModel().select(0);
		}

		//streamerName.setText(defaultStreamer.getStreamerName());
		xPos.setText(String.valueOf(defaultStreamer.getX()));
		yPos.setText(String.valueOf(defaultStreamer.getY()));
		zPos.setText(String.valueOf(PamController.getInstance().getGlobalMediumManager().getZCoeff()*defaultStreamer.getZ()));
		xPosErr.setText(String.valueOf(defaultStreamer.getDx()));
		yPosErr.setText(String.valueOf(defaultStreamer.getDy()));
		zPosErr.setText(String.valueOf(defaultStreamer.getDz()));
		//		if (streamer.getBuoyId1() != null) {
		//			buoyId.setText(streamer.getBuoyId1().toString());
		//		}
		//		else {
		//			buoyId.setText("");
		//		}

		HydrophoneOriginMethod mth = defaultStreamer.getHydrophoneOrigin();
		if (mth==null) {
			originMethod.getSelectionModel().select(0);
			newOriginMethod();
			mth = currentOriginMethod;
			//defaultStreamer.setHydrophoneOrigin(HydrophoneOriginMethods.getInstance().getMethod(0).createMethod(currentArray, defaultStreamer));
		}
		
		
		OriginDialogComponent mthDialogComponent = mth.getDialogComponent();
		if (mthDialogComponent != null) {
			System.out.println("Streamer setParams: Set origin component: "); 
			mthDialogComponent.setParams();
		}

//		System.out.println("Streamer setParams: Set orientation: " + defaultStreamer.getHeading() +  "  " + defaultStreamer.getPitch() +  "  " + defaultStreamer.getRoll()); 
		
		heading	.setText(orientation2Text(defaultStreamer.getHeading()));
		pitch	.setText(orientation2Text(defaultStreamer.getPitch()));
		roll	.setText(orientation2Text(defaultStreamer.getRoll()));
		
		System.out.println("Streamer setParams: Origin interpolator: " + currentArray.getOriginInterpolation() + "  " + currentOriginMethod.getAllowedInterpolationMethods()); 
		

		if (currentArray.getOriginInterpolation()<0 || currentArray.getOriginInterpolation()>=currentOriginMethod.getAllowedInterpolationMethods()) {
			System.err.println("Streamer setParams: Origin interpolator value of " +  currentArray.getOriginInterpolation() + " not allowed for origin method - setting to first allowed method:"); 
			interpPane.setSelection(0); 
		}
		else {
			interpPane.setSelection(currentArray.getOriginInterpolation());
		}
		
		System.out.println("Streamer setParams: selected interp: "  + interpPane.getSelectedInterpType());


		ArraySensorFieldType[] sensorFields = ArraySensorFieldType.values();
		for (int i = 0; i < sensorFields.length; i++) {
			ArrayParameterType fieldType = defaultStreamer.getOrientationTypes(sensorFields[i]);
			String fieldDataBlock = defaultStreamer.getSensorDataBlocks(sensorFields[i]);
			sensorComponents[i].setParameterType(fieldType);
			if (fieldType == ArrayParameterType.SENSOR && fieldDataBlock != null) {
				sensorComponents[i].setDataBlock(PamController.getInstance().getDataBlockByLongName(fieldDataBlock));
			}
		}

		setRecieverLabels() ;
		enableControls();

	}
	
	private String orientation2Text(Double ang) {
		if (ang == null) return String.valueOf(0.0); 
		else return String.format("%3.1f", ang); 
	}

	@Override
	public String getName() {
		return "Streamer Pane";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {

	}

	public void setRecieverLabels() {
		String recieverDepthString = PamController.getInstance().getGlobalMediumManager().getZString(); 

		depthLabel.setText(recieverDepthString ); 
		depthSensorLabel.setText(recieverDepthString + " Sensor"); 

	}

	private Double getDoubleValue(TextField textField) {
		String txt = textField.getText();
		if (txt == null || txt.length() == 0) {
			return null;
		}
		Double val;
		try {
			val = Double.valueOf(txt);
			return val;
		}
		catch (NumberFormatException e) {
			System.err.println("Invalid orientation information: " + txt);
			return null;
		}
	}

	public void setCurrentArray(PamArray currentArray2) {
		this.currentArray=currentArray2;
		
	}
}
