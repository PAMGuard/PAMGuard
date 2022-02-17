package clickTrainDetector.layout.classification.standardClassifier;

import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.classification.CTClassifierParams;
import clickTrainDetector.classification.bearingClassifier.BearingClassifier;
import clickTrainDetector.classification.bearingClassifier.BearingClassifierParams;
import clickTrainDetector.classification.standardClassifier.StandardClassifier;
import clickTrainDetector.classification.standardClassifier.StandardClassifierParams;
import clickTrainDetector.layout.classification.CTClassifierGraphics;
import javafx.scene.layout.Pane;


/**
 * Handles the GUI for the standard click train classifier. 
 * @author Jamie Macaulay 
 *
 */
public class StandardClassifierGraphics implements CTClassifierGraphics {

	/**
	 * Reference to the pane which hold controls for standard classifier params. 
	 */
	private StandardClassifierPane standardClassifierPane;
	
	/**
	 * Reference to the click train control. 
	 */
	private ClickTrainControl clickTrainControl;

	/**
	 * Reference to the standard classifier
	 */
	private StandardClassifier standardClassifier;
	
	public StandardClassifierGraphics(ClickTrainControl clickTrainControl, StandardClassifier standardClassifier) {
		this.clickTrainControl=clickTrainControl; 
		this.standardClassifier= standardClassifier; 
	}


	@Override
	public Pane getCTClassifierPane() {
		if (standardClassifierPane ==null) {
			standardClassifierPane = new StandardClassifierPane(standardClassifier); 
		}
		
		return (Pane) standardClassifierPane.getContentNode();
	}

	@Override
	public CTClassifierParams getParams() {
		StandardClassifierParams clssfrParams = standardClassifierPane.getParams(standardClassifier.getParams()); 
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
		standardClassifierPane.setParams((StandardClassifierParams) params);
		
	}

}
