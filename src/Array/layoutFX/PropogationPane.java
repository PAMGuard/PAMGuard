package Array.layoutFX;

import Array.PamArray;
import PamController.SettingsPane;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.validator.PamValidator;

/**
 * Pane for settings some basic environmental variables. 
 */
public class PropogationPane extends SettingsPane<PamArray> {
	
	Pane mainPane; 
	
	private TextField speedOfSound; 
	
	private TextField speedOfSoundError; 
	
	private PamValidator validator; 


	public PropogationPane() {
		super(null);	
		validator= new PamValidator(); 
		mainPane = createEnvironmentPane();
	}

	/**
	 * Create the pane for setting propogation conditions. 
	 * @return the  pane with controls to change environmental variables. 
	 */
	private Pane createEnvironmentPane() {
		speedOfSound = new TextField(); 
		speedOfSound.setPrefColumnCount(6);

		validator.createCheck()
		.dependsOn("speed_of_sound", speedOfSound.textProperty())
		.withMethod(c -> {
			try {
			String posVal = c.get("speed_of_sound");
				if (posVal.isEmpty() || Double.valueOf(posVal)==null) {
					c.error("The input for speed of sound is invalid");
				}
			}
			catch (Exception e) {
				c.error("The input for speed of sound is invalid");
			}
		})
		.decorates(speedOfSound).immediate();
	
				
		speedOfSoundError = new TextField(); 
		speedOfSoundError.setPrefColumnCount(4);
		
		validator.createCheck()
		.dependsOn("speed_of_sound_error", speedOfSoundError.textProperty())
		.withMethod(c -> {
			try {
			String posVal = c.get("speed_of_sound_error");
				if (posVal.isEmpty() || Double.valueOf(posVal)==null) {
					c.error("The input for speed of sound error is invalid");
				}
		}
		catch (Exception e) {
			c.error("The input for speed of sound is invalid");
		}
		})
		.decorates(speedOfSoundError).immediate();
		
		PamHBox hBox = new PamHBox(); 
		hBox.setSpacing(5);
		hBox.setAlignment(Pos.CENTER);
	
		
		hBox.getChildren().addAll(new Label("Speed of sound"), speedOfSound, new Label("\u00B1"), speedOfSoundError, new Label("m/s")); 

		return hBox;
	}

	@Override
	public PamArray getParams(PamArray currParams) {
		if (validator.containsErrors()) return null;
		
		currParams.setSpeedOfSound(Double.valueOf(speedOfSound.getText()));
		currParams.setSpeedOfSoundError(Double.valueOf(speedOfSoundError.getText()));
		return currParams;
	}

	@Override
	public void setParams(PamArray input) {
		//set the current params. 
		speedOfSound.setText(String.valueOf(input.getSpeedOfSound()));
		speedOfSoundError.setText(String.valueOf(input.getSpeedOfSoundError()));
	}

	@Override
	public String getName() {
		return "Propogation pane";
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
