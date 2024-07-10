package rawDeepLearningClassifier.dlClassification;

import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;

/**
 * Make a decision based on a simple binary threshold for a prediction. 
 */
public class SimpleDLDecision implements DLPredictionDecision {
	
	/**
	 * Reference to the parameters. 
	 */
	private StandardModelParams params;


	@Override
	public boolean isBinaryResult(PredictionResult modelResult) {
		return  isBinaryResult(modelResult, getParams()) ;
	}
	
	
	/**
	 * Check whether a model passes a binary test...
	 * @param modelResult - the model results
	 * @return the model results. 
	 */
	private static boolean isBinaryResult(PredictionResult modelResult, StandardModelParams genericModelParams) {
		for (int i=0; i<modelResult.getPrediction().length; i++) {
						//System.out.println("Binary Classification: "  + genericModelParams.binaryClassification.length); 

			if (modelResult.getPrediction()[i]>genericModelParams.threshold && genericModelParams.binaryClassification[i]) {
				//				System.out.println("SoundSpotClassifier: prediciton: " + i + " passed threshold with val: " + modelResult.getPrediction()[i]); 
				return true; 
			}
		}
		return  false;
	}
	
	
	public StandardModelParams getParams() {
		return params;
	}
	
	
	public void setParams( StandardModelParams genericModelParams) {
		this.params = genericModelParams;
	}

}
