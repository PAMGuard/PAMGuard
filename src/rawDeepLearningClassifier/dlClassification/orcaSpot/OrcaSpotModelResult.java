package rawDeepLearningClassifier.dlClassification.orcaSpot;

import rawDeepLearningClassifier.dlClassification.PredictionResult;

/**
 * Stores results from an OrcaSpot classification 
 * 
 * @author Jamie Macaulay
 *
 */
public class OrcaSpotModelResult implements PredictionResult {

	/**
	 * The time in seconds. 
	 */
	public double timeSeconds = 0; 

	/**
	 * The detection confidence 
	 */
	public float[] detectionConfidence = null; 

	/**
	 * The call type confidence
	 */
	public Float calltypeConfidence = null;

	/**
	 * Do we call this a yes/no classification
	 */
	public boolean binaryClassification = false;

	/**
	 * Description of the predicted class
	 */
	public String predictedClass = "none";

	/**
	 * Constructor for an OrcaSpot result if only a detection has occurred. 
	 * @param detConf - the confidence. 
	 * @param time - the time in seconds. 
	 */
	public OrcaSpotModelResult(Double detConf, Double time) {
		this.detectionConfidence = new float[] {detConf.floatValue()};
		this.timeSeconds = time; 
	}

	public OrcaSpotModelResult() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Get the detection confidence. 
	 * @return the detection confidence
	 */
	@Override
	public float[] getPrediction() {
		if (calltypeConfidence!=null) return new float[] {calltypeConfidence}; 
		else return detectionConfidence;
	}


	@Override
	public boolean isBinaryClassification() {
		return binaryClassification;
	}


	/**
	 * Set whether the binary classification has passed. 
	 * @param binaryClassification - true if the binary classification has passed. 
	 */
	public void setBinaryClassification(boolean binaryClassification) {
		this.binaryClassification = binaryClassification;
	}

	@Override
	public double getAnalysisTime() {
		return timeSeconds;
	}

	/**
	 * Set the analysis time in seconds. 
	 * @param timeSeconds - the analysis time in seconds. 
	 */
	public void setAnlaysisTime(double timeSeconds) {
		this.timeSeconds=timeSeconds; 
	}

	@Override
	public String getResultString() {
		String newString ="";

		if (detectionConfidence!=null) {
			newString += String.format("Det_conf: %.2f\n ", detectionConfidence); 
		}
		else {
			newString += "Det_conf: none\n"; 
		}

		if (calltypeConfidence!=null) {
			newString += String.format("Class_conf: %.2f\n ", calltypeConfidence); 
		}
		else {
			newString += "Class_conf: none\n"; 
		}
		
		newString += String.format("Class_pred: %s\n ", predictedClass); 
		newString += "Binary_class: " + binaryClassification + "\n ";
		return newString;
	}

	@Override
	public short[] getClassNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTimeMillis() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getDurationMillis() {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public double[] getFreqLimits() {
		// TODO Auto-generated method stub
		return null;
	}


}

