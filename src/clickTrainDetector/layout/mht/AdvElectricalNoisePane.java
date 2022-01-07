package clickTrainDetector.layout.mht;

import PamController.SettingsPane;
import clickTrainDetector.clickTrainAlgorithms.mht.electricalNoiseFilter.SimpleElectricalNoiseParams;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;

/**
 * Pane with controls to change advanced settings of the electrical noise pane.
 * 
 * @author Jamie Macaulay
 *
 */
public class AdvElectricalNoisePane extends SettingsPane<SimpleElectricalNoiseParams> {
	
	/**
	 * The main pane.
	 */
	private Pane mainPane;
	
	/**
	 * The minimum number of data units
	 */
	private PamSpinner<Integer> nDataUnitsSpinner;

	/**
	 * The minimum chi^2 spinner. 
	 */
	private PamSpinner<Double> minChi2Spinner; 

	public AdvElectricalNoisePane() {
		super(null);
		mainPane = createAdvancedPane(); 
	}
	
	/**
	 * Create the pane. 
	 * @return the advanced pane.
	 */
	private Pane createAdvancedPane() {
		
		int gridY = 0; 
		
		PamGridPane gridPane = new PamGridPane(); 
		gridPane.setHgap(5);
		gridPane.setVgap(5);
	
		gridPane.add(new Label("Minimum no. detections"), 0, gridY); 
		nDataUnitsSpinner = new PamSpinner<Integer>(0,10000,10,1); 
		nDataUnitsSpinner.setEditable(true);
		nDataUnitsSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		nDataUnitsSpinner.setPrefWidth(70);
		nDataUnitsSpinner.setTooltip(new Tooltip("A test for electrical noise is only carried out after a track\n"
				+ "contains the set minimum number fo data units."));
		
		gridPane.add(nDataUnitsSpinner, 1, gridY); 
		
		gridY++;
		
		gridPane.add(new Label("Minimum X\u00b2 for track"), 0, gridY); 
		minChi2Spinner = new PamSpinner<Double>(0,100.,0.00001,0.00001); 
		minChi2Spinner.setEditable(true);
		minChi2Spinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		minChi2Spinner.setPrefWidth(100);
		minChi2Spinner.setTooltip(new Tooltip("To test electricla nosie the X\u00b2 for each set varibale is tested with\n"
											+ "error removed from the calculation. This is essentially a measure of the average\n"
											+ "squared difference between all the data units in the track. If any of the X\u00b2\n "
											+ "variables return an answer below the set minimum X\u00b2 value then the track is\n "
											+ "categorised as electrical noise and junked. This value should be very low otherwise\n "
											+ "true tracks will be discarded."));
		minChi2Spinner.getValueFactory().setConverter(PamSpinner.createStringConverter(8)); 

		gridPane.add(minChi2Spinner, 1, gridY); 
		
		return gridPane; 
	}

	@Override
	public SimpleElectricalNoiseParams getParams(SimpleElectricalNoiseParams currParams) {
		
		currParams.minChi2 = minChi2Spinner.getValue(); 
		currParams.nDataUnits = nDataUnitsSpinner.getValue(); 

		return currParams;
	}

	@Override
	public void setParams(SimpleElectricalNoiseParams input) {
		minChi2Spinner.getValueFactory().setValue(input.minChi2);
		nDataUnitsSpinner.getValueFactory().setValue(input.nDataUnits);
	}

	@Override
	public String getName() {
		return "Advanced Electrical Noise Pane";
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
