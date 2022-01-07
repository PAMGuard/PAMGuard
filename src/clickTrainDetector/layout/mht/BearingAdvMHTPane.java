package clickTrainDetector.layout.mht;

import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.BearingChi2VarParams;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.BearingChi2VarParams.BearingJumpDrctn;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.ResultConverter;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.SimpleChi2VarParams;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import pamViewFX.fxNodes.PamSpinner;


/**
 * An advanced pane for bearing measurements. Contains extra controls for maximum bearing jump
 * and direction compared to the standard advanced settings pane. 
 * 
 * @author Jamie Macaulay. 
 *
 */
public class BearingAdvMHTPane extends AdvMHTVarPane {

	/**
	 * The maximum jump box.
	 */
	private CheckBox maxJumpBox;
	
	/**
	 * The maximum value allowed for a bearing jump
	 */
	private PamSpinner<Double> maxJumpSpinner;

	/**
	 * Units label. 
	 */
	private Label maxJumpUnits;

	/**
	 * Choice box for jump direction. 
	 */
	private ChoiceBox<BearingJumpDrctn> jumpDirectionChoiceBox;

	public BearingAdvMHTPane(SimpleChi2VarParams simpleChi2Var2, ResultConverter resultConverter) {
		super(simpleChi2Var2, resultConverter);
	}
	
	@Override
	protected GridPane createAdvPane() {
		
		GridPane gridPane = (GridPane) super.createAdvPane();
		
		int gridY=3; 

		gridPane .add(maxJumpBox = new CheckBox("Maximum jump cutoff"), 0, gridY);
		maxJumpBox.setOnAction((action)->{
			enableJumpControls(maxJumpBox.isSelected()); 
		});
		GridPane.setColumnSpan(maxJumpBox, 2);

		
		maxJumpBox.setTooltip(new Tooltip("Enables a maximum allowed bearing jump for click trains \n "
										+ "If the bearing jump is greater than the allowed value then \n"
										+ "the click train is stoped. This is useful when there is a \n "
										+ "high SNR and few other clicks e.g. for sperm whales."));
		gridY++;
		
		Label maxJumpLbl = new Label("Maximum jump "); 
		gridPane.add(new Label("Maximum jump "), 0, gridY);
		gridPane.add(maxJumpSpinner = new PamSpinner<Double>(0,Double.MAX_VALUE,20,1) , 1, gridY);
		maxJumpSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);

		Tooltip maxJumpTooltip = new Tooltip("The maximum  value of the allowed jump in degrees."); 
		maxJumpLbl.setTooltip(maxJumpTooltip);
		maxJumpSpinner.setTooltip(maxJumpTooltip);
		maxJumpSpinner.setEditable(true);
		
		gridPane.add(maxJumpUnits = new Label(""), 2, gridY); 
		gridY++;
		
		Label jumpDirectionLbl = new Label("Jump direction"); 
		
		gridPane.add(jumpDirectionLbl, 0, gridY); 
		gridPane.add(jumpDirectionChoiceBox = new ChoiceBox<BearingJumpDrctn>(), 1, gridY); 
		
		Tooltip jumpDirectionTooltip = new Tooltip("The direction of the jump. POSITVE means that only  jumps in bearing \n "
				+ "which are positive in direction are penalised. BOTH means that a jump \n"
				+ "in direciton is penalised and NEGATIVE means a jump which is negative is \n"
				+ "penalised. When using a towed hydrophone array POSITIVE is often a good \n"
				+ "as animals are usually passing the vessel from postive to negative and so \n "
				+ "a large positive bearing jump unlikely belongs to the same animal"); 
		
		jumpDirectionLbl.setTooltip(jumpDirectionTooltip);
		jumpDirectionChoiceBox.setTooltip(jumpDirectionTooltip);

		jumpDirectionChoiceBox.getItems().addAll(BearingJumpDrctn.NEGATIVE, 
				BearingJumpDrctn.BOTH, BearingJumpDrctn.POSITIVE); 

		return gridPane;
	}

	
	
	private void enableJumpControls(boolean selected) {
		maxJumpSpinner.setDisable(!selected);
		jumpDirectionChoiceBox.setDisable(!selected);
	}
	
	@Override
	public BearingChi2VarParams getParams(SimpleChi2VarParams currParams) {	
		
		BearingChi2VarParams newParams = new BearingChi2VarParams(super.getParams(currParams));
		
		newParams.maxBearingJump = getResultConverter().convert2Value(maxJumpSpinner.getValue()); 
		
		newParams.bearingJumpDrctn = jumpDirectionChoiceBox.getValue(); 
		
		newParams.bearingJumpEnable = maxJumpBox.isSelected(); 

		return newParams; 
	}


	@Override
	public void setParams(SimpleChi2VarParams currParams) {
		
		BearingChi2VarParams newParams = (BearingChi2VarParams) currParams;
//		System.out.println("Hello bearing advVarMHT: " + Math.toDegrees(currParams.error));
		
		super.setParams(newParams);

		maxJumpSpinner.getValueFactory().setValue(getResultConverter().convert2Control(newParams.maxBearingJump));
		jumpDirectionChoiceBox.getSelectionModel().select(newParams.bearingJumpDrctn);
		maxJumpBox.setSelected(newParams.bearingJumpEnable);
		
		enableJumpControls(maxJumpBox.isSelected());
		
		//add new get parameters
		maxJumpUnits.setText(newParams.getUnits());
	
	}


}
