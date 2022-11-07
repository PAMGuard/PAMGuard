package clickTrainDetector.layout.mht;

import org.controlsfx.control.ToggleSwitch;

import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.AmplitudeChi2Params;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.IDIChi2Params;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.ResultConverter;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.SimpleChi2VarParams;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import pamViewFX.fxNodes.PamSpinner;

public class AmplitudeChi2AdvPane extends AdvMHTVarPane {
	
	private PamSpinner<Double> ampJumpSpinner;
	private ToggleSwitch ampEnaleSwitch;


	public AmplitudeChi2AdvPane(SimpleChi2VarParams simpleChi2Var2, ResultConverter resultConverter) {
		super(simpleChi2Var2, resultConverter);
	}
	
	
	@Override
	protected GridPane createAdvPane() {
		
		GridPane gridPane = (GridPane) super.createAdvPane();
		
		int gridY=3; 

		gridPane.add(ampEnaleSwitch = new ToggleSwitch("Max jump"), 0, gridY); 
		ampEnaleSwitch.selectedProperty().addListener((obsVal, newVal, oldVal)->{
			ampJumpSpinner.setDisable(!ampEnaleSwitch.isSelected());
		});
		ampJumpSpinner = new PamSpinner<Double>(0.,Double.MAX_VALUE,0.,1.0); 
		ampJumpSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		ampJumpSpinner.setPrefWidth(90);
		ampJumpSpinner.valueProperty().addListener((obs,oldVal,newVal)->{
			notifySettingsListeners();
		});
		gridPane.add(ampJumpSpinner, 1, gridY); 
		gridPane.add(new Label("dB"), 2, gridY); 
		ampJumpSpinner.setEditable(true);
		
		//create tool tip
		Tooltip errorCoeffTip = new Tooltip(
						  "The minimum Amplitude defines the maximum decibel jump between two detection allowed in a click train"
						+ "If an IDI below this minimum occurs in a click train it will incur"
						+ "a large chi^2 penalty and so the click train is unlikely to be kept"
						+ "in the hypothesis mix.");
		errorCoeffTip.setWrapText(true);
		errorCoeffTip.setPrefWidth(200);
		ampEnaleSwitch.setTooltip(errorCoeffTip);
		ampJumpSpinner.setTooltip(errorCoeffTip);
		
		ampJumpSpinner.setDisable(!ampEnaleSwitch.isSelected());


		return gridPane; 
	}
	
	
	@Override
	public AmplitudeChi2Params getParams(SimpleChi2VarParams currParams) {	
				
		AmplitudeChi2Params newParams = new AmplitudeChi2Params(super.getParams(currParams));

		newParams.maxAmpJump = ampJumpSpinner.getValue();
		newParams.ampJumpEnable = ampEnaleSwitch.isSelected();

		return newParams; 
	}


	@Override
	public void setParams(SimpleChi2VarParams currParams) {
		
		AmplitudeChi2Params newParams;
		if (currParams instanceof AmplitudeChi2Params) {
			newParams = (AmplitudeChi2Params) currParams;
		}
		else {
			newParams = new AmplitudeChi2Params(currParams); 
		}
		
		super.setParams(newParams);
		
		ampJumpSpinner.setDisable(!ampEnaleSwitch.isSelected()); 
		ampJumpSpinner.getValueFactory().setValue(newParams.maxAmpJump);		
		ampEnaleSwitch.setSelected(newParams.ampJumpEnable);
	}

}
