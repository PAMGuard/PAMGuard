package clickTrainDetector.layout.classification.simplechi2classifier;

import clickTrainDetector.layout.classification.CTClassifierGraphics;
import clickTrainDetector.classification.CTClassifierParams;
import clickTrainDetector.classification.simplechi2classifier.Chi2ThresholdClassifier;
import clickTrainDetector.classification.simplechi2classifier.Chi2ThresholdParams;

import javafx.scene.layout.Pane;

/**
 * 
 * Simple classifier graphics. 
 * 
 * @author Jamie Macaulay
 *
 */
public class SimpleCTClassifierGraphics implements CTClassifierGraphics {
	
	/**
	 * Reference to the chi2 threshold. 
	 */
	private Chi2ThresholdClassifier simpleChi2Classifier;
	
	/**
	 * The ct classifier settings pane. 
	 */
	private SimpleCTClassifierPane simpleCTClassiferPane;

	public SimpleCTClassifierGraphics(Chi2ThresholdClassifier simpleChi2Classifier) {
		this.simpleChi2Classifier=simpleChi2Classifier; 
	}

	@Override
	public Pane getCTClassifierPane() {
		if (simpleCTClassiferPane==null) {
			simpleCTClassiferPane = new SimpleCTClassifierPane(simpleChi2Classifier);
		}
		//System.out.println("SimpleCTClassifierGraphics getCTClassifierPane: " + simpleChi2Classifier.getParams().chi2Threshold); 
		//params are set here. <- do not do this because you may wish to store the classifier params somewhere else. 
		//simpleCTClassiferPane.setParams(simpleChi2Classifier.getParams());
		return (Pane) simpleCTClassiferPane.getContentNode();
	}

	@Override
	public CTClassifierParams getParams() {
		Chi2ThresholdParams clssfrParams = simpleCTClassiferPane.getParams(simpleChi2Classifier.getParams()).clone(); 
		if (clssfrParams==null) {
			System.err.print("Simple Chi2 Classifier returned null params");
			return null;
		}
//		else {
//			simpleChi2Classifier.setParams(clssfrParams); 
//			return clssfrParams;
//		}
		//System.out.println("SimpleCTClassifierGraphics - getParams: " + clssfrParams.chi2Threshold);
		return clssfrParams; 
	}

	@Override
	public void setParams(CTClassifierParams params) {
		simpleCTClassiferPane.setParams((Chi2ThresholdParams) params);
		//System.out.println("SimpleCTClassifierGraphics - setParams: " + ((Chi2ThresholdParams) params).chi2Threshold);
	}

}
