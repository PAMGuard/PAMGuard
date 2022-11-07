package clickTrainDetector.layout.classification.idiClassifier;

import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.classification.CTClassifierParams;
import clickTrainDetector.classification.idiClassifier.IDIClassifier;
import clickTrainDetector.classification.idiClassifier.IDIClassifierParams;
import clickTrainDetector.layout.classification.CTClassifierGraphics;
import javafx.scene.layout.Pane;

/**
 * The IDI classifier graphics. Has controls for the IDI classifier. 
 * 
 * @author Jamie Macaulay
 *
 */
public class IDIClassifierGraphics implements CTClassifierGraphics {


	private IDIPane idiPane;
	
	/**
	 * Reference to the IDI classifier 
	 */
	private IDIClassifier idiClassifier;

	public IDIClassifierGraphics(ClickTrainControl clickTrainControl, IDIClassifier idiClassifier) {
		this.idiClassifier =  idiClassifier; 
		
	}

	@Override
	public Pane getCTClassifierPane() {
		if (idiPane == null) {
			idiPane = new IDIPane(); 
		}
		return idiPane;
	}

	@Override
	public CTClassifierParams getParams() {
		IDIClassifierParams clssfrParams = idiPane.getParams(idiClassifier.getParams()); 
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
		idiPane.setParams((IDIClassifierParams) params);
		
	}

}
