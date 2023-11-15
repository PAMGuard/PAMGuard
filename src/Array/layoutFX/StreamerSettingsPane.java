package Array.layoutFX;

import Array.PamArray;
import Array.Streamer;
import Array.streamerOrigin.HydrophoneOriginMethod;
import Array.streamerOrigin.HydrophoneOriginMethods;
import Array.streamerOrigin.HydrophoneOriginSystem;
import Array.streamerOrigin.OriginDialogComponent;
import Array.streamerOrigin.OriginSettings;
import PamController.PamController;
import PamController.SettingsPane;
import javafx.geometry.Pos;
import javafx.scene.Node;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.validator.PamValidator;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
	private InterpSettingsPane interpPane;


	private TextField xPos;


	private Validator validator = new PamValidator();


	private TextField yPos;


	private TextField zPos;


	private TextField zPosErr;


	private TextField xPosErr;


	private Label depthLabel;


	private TextField yPosErr;


	private Label depthLabel2;

	
	
	public StreamerSettingsPane() {
		super(null); 
		
		mainPane = new PamBorderPane(); 
		mainPane.setCenter(getStreamerPane());
	
	}

	/**
	 * Create the streamer pane
	 * @return
	 */
	private Pane getStreamerPane(){
		
		String reciever = PamController.getInstance().getGlobalMediumManager().getRecieverString(); 
		
		Label label = new Label("Geo-reference Position");
		
		originMethod = new ComboBox<HydrophoneOriginSystem>();
		originPanel = new PamBorderPane();
		
		int n = HydrophoneOriginMethods.getInstance().getCount();
		for (int i = 0; i < n; i++) {
			originMethod.getItems().add(HydrophoneOriginMethods.getInstance().getMethod(i));
		}
		
		Label hydroMovement = new Label(reciever +" Movement Model");

		
		//listener for when a new origin method is called. 
		originMethod.setOnAction((action)->{
			newOriginMethod();
		});
		
		interpPane = new InterpSettingsPane();
		Label inteprlabel = new Label("Interpolation");

		
		//add all stuff to the holder
		PamVBox holder = new PamVBox();
		holder.getChildren().addAll(label, originMethod, hydroMovement, createLocatorPane(), interpPane);
		holder.setSpacing(5);
		
		
		return holder; 

	}
	
	public Pane createLocatorPane() {
		
		PamGridPane positionPane = new PamGridPane(); 
		positionPane.setHgap(5);
		positionPane.setVgap(5);
	

		double maxWidth =10; 

		xPos=new TextField();
		xPos.setMaxWidth(maxWidth);
		HydrophoneSettingsPane.addTextValidator(xPos, "x position", validator); 
		yPos=new TextField();
		yPos.setMaxWidth(maxWidth);
		HydrophoneSettingsPane.addTextValidator(yPos, "y position", validator); 
		zPos=new TextField();
		zPos.setMaxWidth(maxWidth);
		HydrophoneSettingsPane.addTextValidator(zPos, "z position", validator); 
		depthLabel = new Label("Depth"); 
		depthLabel.setAlignment(Pos.CENTER);

		xPosErr=new TextField();
		xPosErr.setMaxWidth(50);
		HydrophoneSettingsPane.addTextValidator(xPosErr, "x error", validator); 
		yPosErr=new TextField();
		yPosErr.setMaxWidth(50);
		HydrophoneSettingsPane.addTextValidator(yPosErr, "y error", validator); 
		zPosErr=new TextField();
		zPosErr.setMaxWidth(50);
		depthLabel2 = new Label(""); //changes with air or water mode. 
		depthLabel2.setAlignment(Pos.CENTER);
		HydrophoneSettingsPane.addTextValidator(zPosErr, "z error", validator); 
		
		return positionPane; 
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
		}

		OriginDialogComponent mthDialogComponent = currentOriginMethod.getDialogComponent(); 
		if (mthDialogComponent == null) {
			originPanel.getChildren().clear();
			currentOriginComponent = null;
		}
		else {
			Pane newComponent = mthDialogComponent.getSettingsPane();
			if (currentOriginComponent != newComponent) {
				originPanel.getChildren().clear();
				currentOriginComponent = newComponent;
				originPanel.setCenter(newComponent);
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
