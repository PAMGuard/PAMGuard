package clickDetector.layoutFX;

import PamController.SettingsPane;
import clickDetector.ClickControl;
import clickDetector.ClickParameters;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;

/**
 * Pane containing controls to change echo detection parameters in the click detector. 
 * @author Jamie Macaulay 
 *
 */
public class ClickEchoPane extends SettingsPane<ClickParameters> {
	
	/**
	 * Check box on whether to run an echo detector. 
	 */
	private CheckBox runEchoCheckBox;

	/**
	 * Check box to set whether to discard echoes. 
	 */
	private CheckBox discardEchoCheckBox;

	/**
	 * The intervel in seconds for echo detector. 
	 */
	private PamSpinner<Double> echoIntervalSpinner;

	/**
	 * The main pane. 
	 */
	private PamBorderPane mainPane; 
	
	public ClickEchoPane(ClickControl clickControl){
		super(null);
		mainPane = new PamBorderPane();
		mainPane.setCenter(createEchoPane());
	}
	/**
	 * Create pane for echo detector. 
	 * @return pane to change settings on echo detector. 
	 */
	private Pane createEchoPane(){
		
		PamVBox vBox=new PamVBox();
		vBox.setSpacing(5);
		
		Label echoLable1 = new Label("Echo Detection Policy");
		PamGuiManagerFX.titleFont2style(echoLable1);
		vBox.getChildren().add(echoLable1);
		
		vBox.getChildren().add(runEchoCheckBox=new CheckBox("Run Echo Detector"));
		runEchoCheckBox.setOnAction((action)->{
			discardEchoCheckBox.setDisable(!runEchoCheckBox.isSelected());
		});
		vBox.getChildren().add(discardEchoCheckBox=new CheckBox("Discard Echoes"));

		
		Label echoLabe2 = new Label("Echo Detection Settings");
		PamGuiManagerFX.titleFont2style(echoLabe2);
		vBox.getChildren().add(echoLabe2);
		
		
		PamHBox intervalBox=new PamHBox(); 
		intervalBox.setSpacing(5);
		
		echoIntervalSpinner=new PamSpinner<Double>(0.00000, 100, 0.1, 0.05);
		echoIntervalSpinner.setEditable(true);
//		echoIntervalSpinner.getValueFactory().setConverter(new DecimalConverter());
		echoIntervalSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		
		intervalBox.getChildren().addAll(new Label("Max Interval"),echoIntervalSpinner,new Label("seconds") ); 
		
		vBox.getChildren().add(intervalBox);
	
		return vBox; 
	} 
	
	@Override
	public ClickParameters getParams(ClickParameters clickParameters) {
		// TODO Auto-generated method stub
		return clickParameters;
	}
	
	@Override
	public void setParams(ClickParameters clickParameters) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return "Click Echo Settings";
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
