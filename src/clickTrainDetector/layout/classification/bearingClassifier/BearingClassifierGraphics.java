package clickTrainDetector.layout.classification.bearingClassifier;

import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.classification.CTClassifierParams;
import clickTrainDetector.classification.bearingClassifier.BearingClassifier;
import clickTrainDetector.classification.bearingClassifier.BearingClassifierParams;
import clickTrainDetector.layout.classification.CTClassifierGraphics;
import javafx.scene.layout.Pane;

/**
 * Manager for UI stuff for the bearing classifier. 
 * @author Jamie Macaulay
 *
 */
public class BearingClassifierGraphics implements CTClassifierGraphics {
	
	/**
	 * Click train control. 
	 */
	private ClickTrainControl clickTrainControl;
	
	/**
	 * The bearing classifier parameters. 
	 */
	private BearingClassifier bearingClassifer;

	private BearingClassifierPane bearingClassifierPane;

	/**
	 * The bearing classifier graphics. 
	 * @param clickTrainControl - reference to the click train control. 
	 */
	public BearingClassifierGraphics(ClickTrainControl clickTrainControl, BearingClassifier bearingClassifier) {
		this.clickTrainControl=clickTrainControl; 
		this.bearingClassifer= bearingClassifier; 
	}

	@Override
	public Pane getCTClassifierPane() {
		if (bearingClassifierPane==null) {
			bearingClassifierPane = new BearingClassifierPane(bearingClassifer);
		}
		//params are set here. 
		bearingClassifierPane.setParams(bearingClassifer.getParams());
		return (Pane) bearingClassifierPane.getContentNode();
	}

	@Override
	public CTClassifierParams getParams() {
		BearingClassifierParams clssfrParams = bearingClassifierPane.getParams(bearingClassifer.getParams()); 
		if (clssfrParams==null) {
			System.err.print("Bearing classifier returned null params");
			return null;
		}
//		else {
//			simpleChi2Classifier.setParams(clssfrParams); 
//			return clssfrParams;
//		}
		return clssfrParams; 
	}

	@Override
	public void setParams(CTClassifierParams params) {
		bearingClassifierPane.setParams((BearingClassifierParams) params);
		
	}

	

}
