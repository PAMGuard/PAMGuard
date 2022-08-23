package clickTrainDetector.layout.classification.bearingClassifier;

import PamController.SettingsPane;
import clickTrainDetector.classification.bearingClassifier.BearingClassifierParams;
import javafx.scene.Node;
import javafx.scene.control.Label;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.MinMaxPane;
import pamViewFX.fxNodes.utilityPanes.MinMaxPane.MinMaxParams;

/**
 * The Bearing classifier settings pane. 
 * 
 * @author Jamie Macaulay
 *
 */
public class BearingClassifierPane extends  SettingsPane<BearingClassifierParams> {

	/**
	 * Min and max pane for the bearing limits. 
	 */
	private MinMaxPane<Double> bearingLims;
	
	//bearing delta limits. 
	private MinMaxPane<Double> bearingDMean;
	private MinMaxPane<Double> bearingDMedian;
	private MinMaxPane<Double> bearingDStd;
	
	/**
	 * The main holder pane. 
	 */
	private PamVBox mainPane;

	public BearingClassifierPane(Object ownerWindow) {
		super(ownerWindow);
		createBearingClassifierPane(); 
	}
	
	

	/**
	 * Create the bearing classifier pane.
	 */
	private void createBearingClassifierPane() {
		
//		Label label = new Label("Bearing Limits"); 
//		label.setFont(PamGuiManagerFX.titleFontSize2);
//		PamGuiManagerFX.titleFont2style(label);

		
		bearingLims = new MinMaxPane<Double>("Bearing Limits", "\u00B0", -180., 180., 2.); 
		

//		Label label2 = new Label("\u0394 Bearing"); 
//		label2.setFont(PamGuiManagerFX.titleFontSize2);
//		PamGuiManagerFX.titleFont2style(label2);

		
		bearingDMean 	= new MinMaxPane<Double>("\u0394 Bearing Mean 	", "\u00B0/s", -180., 180., 0.001); 
		bearingDMean.setConverter(PamSpinner.createStringConverter(5));

		bearingDMedian 	= new MinMaxPane<Double>("\u0394 Bearing Median	", "\u00B0/s", -180., 180., 0.001); 
		bearingDMedian.setConverter(PamSpinner.createStringConverter(5));

		bearingDStd 	= new MinMaxPane<Double>("\u0394 Bearing Std	", "\u00B0/s", -180., 180., 0.5); 
		bearingDStd.setConverter(PamSpinner.createStringConverter(5));

		this.mainPane = new PamVBox(); 
		mainPane.setSpacing(5);
		
		mainPane.getChildren().addAll(bearingLims, bearingDMean, bearingDMedian, bearingDStd); 
	}

	@Override
	public BearingClassifierParams getParams(BearingClassifierParams currParams) {
		
		MinMaxParams params;
		
		//bearing limits
		params = bearingLims.getParams(); 
		
		currParams.bearingLimMin = Math.toRadians(params.min.doubleValue());
		currParams.bearingLimMax =  Math.toRadians(params.max.doubleValue());
		
		//bearing mean
		params = bearingDMean.getParams(); 

		currParams.minMeanBearingD =  Math.toRadians(params.min.doubleValue());
		currParams.maxMeanBearingD =  Math.toRadians(params.max.doubleValue());
		currParams.useMean = params.enabled;

		//bearing median
		params = bearingDMedian.getParams(); 

		currParams.minMedianBearingD =  Math.toRadians(params.min.doubleValue());
		currParams.maxMedianBearingD =  Math.toRadians(params.max.doubleValue());
		currParams.useMedian = params.enabled;

		
		//bearing standard deviation
		params = bearingDStd.getParams(); 

		currParams.minStdBearingD =  Math.toRadians(params.min.doubleValue());
		currParams.maxStdBearingD =  Math.toRadians(params.max.doubleValue());
		currParams.useStD = params.enabled;
		
		return currParams;
	}

	@Override
	public String getName() {
		return "Click Train Detector: Bearing Classifier";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		
	}

	@Override
	public void setParams(BearingClassifierParams input) {
		
		//bearing limits
		this.bearingLims.setParams(Math.toDegrees(input.bearingLimMin), Math.toDegrees(input.bearingLimMax), true);
		
		//bearing delta values. 
		this.bearingDMean.setParams(Math.toDegrees(input.minMeanBearingD), Math.toDegrees(input.maxMeanBearingD), input.useMean);
		this.bearingDMedian.setParams(Math.toDegrees(input.minMedianBearingD), Math.toDegrees(input.maxMedianBearingD), input.useMedian);
		this.bearingDStd.setParams(Math.toDegrees(input.minStdBearingD), Math.toDegrees(input.maxStdBearingD), input.useStD);
	}

}
