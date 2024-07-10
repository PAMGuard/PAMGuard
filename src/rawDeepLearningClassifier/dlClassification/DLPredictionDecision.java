package rawDeepLearningClassifier.dlClassification;

/**
 * Decides whether a prediction has passed a threshold to be used to create a new
 * data unit. 
 * <p>
 * Note that the majority of the time this will be a simple test of the value of 
 * predictions of a model but there will be cases when a classifier implements a 
 * more complex system. For example, a implementation could save a buffer of predictions 
 * so that previous predictions inform the latest prediction. Or results may include 
 * some sort of object detection components and frequency bounds etc could be used for 
 * classification. 
 * 
 * 
 */
public interface DLPredictionDecision {
	
	/**
	 * Check whether a single prediction passes a binary classifier. Prediction which pass decision will be
	 * passed on to create new data units. 
	 * @param result - the prediciton result to test. 
	 * @return true if the result is passed. 
	 */
	public boolean isBinaryResult(PredictionResult result); 

}
