package rawDeepLearningClassifier.dlClassification.genericModel;

import PamUtils.PamArrayUtils;
import rawDeepLearningClassifier.dlClassification.PredictionResult;

/**
 * Result from the SoundSpot classifier.
 * 
 * @author Jamie Macaulay 
 *
 */
public class StandardPrediction implements PredictionResult {
	

	/**
	 * The time in millis
	 */
	private long timeMillis = -1L; 

	/**
	 * Create a result for the Sound Spot classifier. 
	 * @param prob - the probability of each class. 
	 */
	private float[]  prob;
	
	/**
	 * The class name IDs
	 */
	private short[]  classNameID;

	/**
	 * True if has passed binary classification. 
	 */
	private boolean binaryPass; 
	
	/**
	 * Analysis time in seconds. 
	 */
	public double analysisTime=0;

	/**
	 * Frequency limits of the prediction result. Use for bounding boxes. 
	 * Can be null if not set. 
	 */
	private double[] freqLimits; 


	private int durationSamples = -1; //duration in samples, can be -1 if not set.

	
	private long startSample = -1L; //start sample, can be -1 if not set.
	
	


	/**
	 * Constructor for a typical generic prediciton. 
	 * @param prob - the probability for each class. 
	 * @param classNameID - the ID's of the class names.
	 * @param isBinary - true if the model result passed a binary test (usually one species above a threshold)
	 */
	public StandardPrediction(float[] prob, short[] classNameID, boolean isBinary) {
		this.prob=prob; 
		this.classNameID = classNameID;
		this.binaryPass= isBinary; 
	}

	
	public StandardPrediction(float[] prob, boolean isBinary) {
		this(prob, null, isBinary); 
	}

	/**
	 * Create a result for the Sound Spot classifier. 
	 * @param prob - the probability of each class. 
	 */
	public StandardPrediction(float[] prob) {
		this(prob, null, false); 
	}

	@Override
	public float[] getPrediction() {
		return prob;
	}

	@Override
	public boolean isBinaryClassification() {
		return binaryPass;
	}

	@Override
	public double getAnalysisTime() {
		return analysisTime;
	}

	public void setAnalysisTime(double analysisTime) {
		this.analysisTime = analysisTime;
	}

	@Override
	public String getResultString() {
		//the classification results. 
		return PamArrayUtils.array2String(prob, 1, "/n"); 
	}

	@Override
	public short[] getClassNames() {
		return classNameID;
	}

	/**
	 * Set the IDs of the class names. Use a class name manager to retrieve the 
	 * actual String names. 
	 * @param classNameID - the class name IDs. 
	 */
	public void setClassNameID(short[] classNameID) {
		this.classNameID = classNameID; 
	}

	/**
	 * Set the binary classification. 
	 * @param binaryResult - the binary classification. 
	 */
	public void setBinaryClassification(boolean binaryResult) {
		this.binaryPass=binaryResult; 
		
	}
	
	@Override
	public long getTimeMillis() {
		return timeMillis;
	}


	public void setTimeMillis(long timeMillis) {
		this.timeMillis = timeMillis;
	}


	@Override
	public int getSampleDuration() {
		return durationSamples;
	}


	@Override
	public double[] getFreqLimits() {
		return freqLimits;
	}
	
	/**
	 * Set the frequency limits of the prediction result. 
	 * @param freqLimits - the frequency limits of the prediction result.
	 */
	public void setFreqLimits(double[] freqLimits) {
		this.freqLimits = freqLimits;
	}

	/**
	 * Set the duration in millis. 
	 * @param durationMillis - the duration in millis. 
	 */
	public void setDuratioSamples(int durationMillis) {
		this.durationSamples = durationMillis;
	}


	public void setStartSample(Long startSample) {
		this.startSample = startSample;
	}
	

	public long getStartSample() {
		return startSample;
	}


}
