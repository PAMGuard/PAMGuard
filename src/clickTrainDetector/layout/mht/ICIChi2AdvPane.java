package clickTrainDetector.layout.mht;

import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.BearingChi2VarParams;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.IDIChi2Params;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.ResultConverter;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.SimpleChi2VarParams;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import pamViewFX.fxNodes.PamSpinner;

/**
 * Adv IDI chi2 pane allows. 
 * @author Jamie Macaulay
 *
 */
public class ICIChi2AdvPane  extends AdvMHTVarPane {

	private PamSpinner<Double> minICISpinner;


	public ICIChi2AdvPane(SimpleChi2VarParams simpleChi2Var2, ResultConverter resultConverter) {
		super(simpleChi2Var2, resultConverter);
	}
	
	
	@Override
	protected GridPane createAdvPane() {
		
		GridPane gridPane = (GridPane) super.createAdvPane();
		
		int gridY=3; 

		Label minIDILabel = new Label("Min. IDI"); 
		gridPane.add(minIDILabel, 0, gridY); 
		minICISpinner = new PamSpinner<Double>(0.,Double.MAX_VALUE,0.,1.0); 
		minICISpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		minICISpinner.setPrefWidth(90);
		minICISpinner.valueProperty().addListener((obs,oldVal,newVal)->{
			notifySettingsListeners();
		});
		gridPane.add(minICISpinner, 1, gridY); 
		gridPane.add(new Label("ms"), 2, gridY); 
		minICISpinner.setEditable(true);

		//create tool tip
		Tooltip errorCoeffTip = new Tooltip(  "The minimum IDI defines a minimum IDI allowed in a click train \n"
											+ "If an IDI below this minimum occurs in a click train it will incur\n"
											+ "a large chi^2 penalty and so the click train is unlikely to be kept\n"
											+ "in the hypothesis mix."); 
		minIDILabel.setTooltip(errorCoeffTip);
		minICISpinner.setTooltip(errorCoeffTip);

		return gridPane; 
	}
	
	
	@Override
	public IDIChi2Params getParams(SimpleChi2VarParams currParams) {	
		
		IDIChi2Params newParams = new IDIChi2Params(super.getParams(currParams));

		newParams.minIDI = minICISpinner.getValue()/1000.;

		return newParams; 
	}


	@Override
	public void setParams(SimpleChi2VarParams currParams) {
		
		IDIChi2Params newParams = (IDIChi2Params) currParams;
//		System.out.println("Hello bearing advVarMHT: " + Math.toDegrees(currParams.error));
		
		super.setParams(newParams);
		
		minICISpinner.getValueFactory().setValue(newParams.minIDI*1000.);		
	
	}


}
