package Array.layoutFX;

import org.controlsfx.control.PopOver;

import Array.HydrophoneLocators;
import Array.PamArray;
import Array.Streamer;
import Array.sensors.ArraySensorFieldType;
import Array.streamerOrigin.HydrophoneOriginMethod;
import Array.streamerOrigin.HydrophoneOriginMethods;
import Array.streamerOrigin.HydrophoneOriginSystem;
import Array.streamerOrigin.OriginDialogComponent;
import Array.streamerOrigin.OriginSettings;
import PamController.PamController;
import PamController.SettingsPane;
import PamUtils.LatLong;
import javafx.geometry.Pos;
import javafx.scene.Node;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.popOver.PamPopOver;
import pamViewFX.validator.PamValidator;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import net.synedra.validatorfx.Validator;


/**
 * A javaFX settings pane for a streamer. 
 * 
 * @author Jamie Macaulay
 *
 */
public class StreamerSettingsPane extends SettingsPane<Streamer> {
	
	private final static double MAX_TEXTFIELD_WIDTH = 80; 

	
	public PamBorderPane mainPane;
	
	
	private ComboBox<HydrophoneOriginSystem> originMethod;


	private PamBorderPane originPanel; 
	
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
	 * Button for extra origin params. 
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
		originPanel = new PamBorderPane();
		PopOver popOver = new PopOver();
		popOver.setContentNode(originPanel);

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
		
		interpBox.getChildren().addAll(interpMethodLabel, interpPane); 
		interpBox.setAlignment(Pos.CENTER_LEFT);
		interpBox.setMaxWidth(Double.MAX_VALUE);

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
	
		//craet data sources for sensors. 
		ArraySensorFieldType[] sensorFields = ArraySensorFieldType.values();
		sensorComponents = new SensorSourcePane[sensorFields.length];
		//EnableOrientation eo = new EnableOrientation();
		for (int i = 0; i < sensorFields.length; i++) {
			sensorComponents[i] = new SensorSourcePane(sensorFields[i], true, sensorFields[i] != ArraySensorFieldType.HEIGHT);
			//sensorComponents[i].addActionListenr(eo);
		}
			
		localiserMethod = new ComboBox<>();
		int n = HydrophoneLocators.getInstance().getCount();
		for (int i = 0; i < n; i++) {
			localiserMethod.getItems().add(HydrophoneLocators.getInstance().getSystem(i));
		}
		localiserMethod.setMaxWidth(Double.MAX_VALUE);
		
		//hydrophone position and 
		PamGridPane positionPane = new PamGridPane(); 
		positionPane.setHgap(5);
		positionPane.setVgap(5);
		
		ColumnConstraints rc = new ColumnConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, MAX_TEXTFIELD_WIDTH);
		
		
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
		
		
		//Orientaiotn
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
		
		pitch = new TextField(); 
		pitch.setMaxWidth(MAX_TEXTFIELD_WIDTH);

		roll = new TextField(); 
		roll.setMaxWidth(MAX_TEXTFIELD_WIDTH);
		
		col=0;


		Label orientation = new Label("Orientation"); 
		positionPane.add(orientation, col++, row);
		positionPane.add(heading, col++, row);
		positionPane.add(pitch, col++, row);
		positionPane.add(roll, col++, row);
		positionPane.add(new Label(degsLab), col++, row);
		
		PamButton button = new PamButton("Sensors");
		button.setGraphic(PamGlyphDude.createPamIcon("mdi2c-compass-outline", PamGuiManagerFX.iconSize));
		
		PopOver popOver = new PopOver(createSensorPane()); 
		popOver.setDetachable(true);
		
		button.setOnAction((a)->{
			popOver.show(button);
		});
		
		positionPane.add(button,  col++, row);
		
		
		
		
		PamVBox holder= new PamVBox(); 
		holder.setSpacing(5);
		holder.getChildren().addAll(localiserMethod, positionPane); 
		
		return holder; 
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
			originPanel.getChildren().clear();
			currentOriginComponent = null;
			this.originButton.setDisable(true);
		}
		else {
			this.originButton.setDisable(false);
			Pane newComponent = mthDialogComponent.getSettingsPane();
			if (currentOriginComponent != newComponent) {
				originPanel.setCenter(newComponent);
				currentOriginComponent = newComponent;
				mthDialogComponent.setParams();
			}
		}
		enableControls();
	}
	
	
	
	private void enableControls() {
		if (currentOriginMethod != null) {
			interpPane.setAllowedValues(currentOriginMethod.getAllowedInterpolationMethods());
		}
	}

	@Override
	public Streamer getParams(Streamer currParams) {
		return currParams;
	}

	@Override
	public void setParams(Streamer input) {
		defaultStreamer = input;
		
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
	
}
