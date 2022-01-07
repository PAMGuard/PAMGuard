package videoRangePanel.layoutFX;

import PamController.SettingsPane;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;
import videoRangePanel.RefractionParameters;

/**
 * Pane for changing settings of the refraction method
 * @author Jamie Macaulay
 *
 */
public class RefractionMethodPane extends DynamicSettingsPane<RefractionParameters>{
	
	/**
	 * The main holder pane. 
	 */
	private PamBorderPane mainPane;
	private TextField temp;
	private TextField tempGradient;
	private TextField pressure;

	public RefractionMethodPane(Object ownerWindow) {
		super(ownerWindow);
		mainPane=new PamBorderPane(); 
		mainPane.setCenter(createRefractionPane());
	}
	
	/**
	 * Create the pane for changing refraction method settings
	 * @return pane with controls to change refraction method settings. 
	 */
	private Pane createRefractionPane() {
		
		PamVBox holderVBox = new PamVBox(); 
		holderVBox.setSpacing(5);
	
		Label titleLabel = new Label("Refraction parameters");
		PamGuiManagerFX.titleFont2style(titleLabel);
		//titleLabel.setFont(PamGuiManagerFX.titleFontSize2);
		
		PamGridPane gridPane = new PamGridPane();
		gridPane.setHgap(5);
		gridPane.setVgap(5);
		
		gridPane.add(new Label("Temperature"), 0, 0);
		gridPane.add(temp = new TextField(), 1, 0);
		temp.setOnAction((action)->{
			this.notifySettingsListeners();
		});
		gridPane.add(new Label("\u00B0C"), 2, 0);
		
		gridPane.add(new Label("Temperature Gradient"), 0, 1);
		gridPane.add(tempGradient = new TextField(), 1, 1);
		tempGradient.setOnAction((action)->{
			this.notifySettingsListeners();
		});
		gridPane.add(new Label("\u00B0C/m"), 2, 1);
	
		gridPane.add(new Label("Atmospheric Pressure"), 0, 2);
		gridPane.add(pressure = new TextField(), 1, 2);
		pressure.setOnAction((action)->{
			this.notifySettingsListeners();
		});
		gridPane.add(new Label("millibar"), 2, 2);
		
		holderVBox.getChildren().addAll(titleLabel, gridPane); 
		holderVBox.setPadding(new Insets(5,0,5,0));
		
		return holderVBox; 
	}

	@Override
	public RefractionParameters getParams(RefractionParameters refractionParameters) {
		try {
			refractionParameters.seaSurfactCelcius = Double.valueOf(temp.getText());
			refractionParameters.tempGradient = Double.valueOf(tempGradient.getText());
			refractionParameters.atmosphericPressure = Double.valueOf(pressure.getText());
		}
		catch (NumberFormatException ex) {
			return null;
		}
		return refractionParameters;
	}

	@Override
	public void setParams(RefractionParameters refractionParameters) {
		temp.setText(String.format("%.1f", refractionParameters.seaSurfactCelcius));
		tempGradient.setText(String.format("%.4f", refractionParameters.tempGradient));
		pressure.setText(String.format("%.1f", refractionParameters.atmosphericPressure));
	}

	@Override
	public String getName() {
		return "Refraction Method Settings";
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
