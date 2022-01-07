package clickTrainDetector.classification.bearingClassifier;

import clickTrainDetector.classification.CTClassification;
import clickTrainDetector.classification.CTClassifierType;
import clickTrainDetector.classification.ClassifierJSONLogging;

/**
 * Classification result from the bearing classifier.
 * 
 * @author Jamie Macaulay
 *
 */
public class BearingClassification implements CTClassification {

	/**
	 * Mean bearing derivative. 
	 */
	private double meanBearingDiff;

	/**
	 * Median bearing derivative.
	 */
	private double medianBearingDiff; 

	/**
	 * Standard bearing derivative.
	 */
	private double stdBearingDiff;

	/**
	 * The current species ID.
	 */
	private int speciesID;

	/**
	 * Bearing classifier JSON logging. 
	 */
	private BearingClassifierJSON bearingClassifierJSONLogging; 

	/**
	 * Constructor for the bearing classifier. 
	 * @param speciesID 		- the speciesID flag. 
	 * @param meanBearingDiff 	-  the mean bearing derivative of a click train in RADIANS/SECOND
	 * @param medianBearingDiff -  the median bearing derivative of a click train in RADIANS/SECOND
	 * @param stdBearingDiff 	-  the standard deviation of the bearing derivative of a click train in RADIANS/SECOND

	 */
	public BearingClassification(int speciesID, double meanBearingDiff, double medianBearingDiff, double stdBearingDiff) {
		this.meanBearingDiff 	= meanBearingDiff;
		this.medianBearingDiff	= medianBearingDiff; 
		this.stdBearingDiff		= stdBearingDiff; 
		this.speciesID			= speciesID; 
		bearingClassifierJSONLogging = new BearingClassifierJSON(); 
	}

	/**
	 * Bearing classification from a JSON string.
	 * @param jsonstring 		- JSON string containing the bearing data. 
	 */
	public BearingClassification(String jsonstring) {
		bearingClassifierJSONLogging = new BearingClassifierJSON(); 
		CTClassification classification  = bearingClassifierJSONLogging.createClassification(jsonstring); 
		this.speciesID			=classification.getSpeciesID(); 
		this.meanBearingDiff	=((BearingClassification) classification).getMeanDelta();
		this.medianBearingDiff	=((BearingClassification) classification).getMedianDelta();
		this.stdBearingDiff		=((BearingClassification) classification).getStdDelta();
	}


	@Override
	public CTClassifierType getClassifierType() {
		return CTClassifierType.BEARINGCLASSIFIER;
	}

	@Override
	public int getSpeciesID() {
		return speciesID;
	}

	@Override
	public String getSummaryString() {
		return  String.format("Bearing Classifier: Mean %.4f\u00B0/s Median %.4f\u00B0/s Std %.4f\u00B0/s",
				Math.toDegrees(meanBearingDiff), Math.toDegrees(medianBearingDiff), Math.toDegrees(stdBearingDiff));
	}

	@Override
	public ClassifierJSONLogging getJSONLogging() {
		return bearingClassifierJSONLogging;
	}

	/**
	 * Get the median of bearing delta values in radians per second. 
	 * @return the median bearing delta in rad/s.
	 */
	public double getMedianDelta() {
		return this.medianBearingDiff;
	}

	/**
	 * Get the mean of bearing delta values in radians per second. 
	 * @return the mean bearing delta in rad/s
	 * 
	 */
	public double getMeanDelta() {
		return this.meanBearingDiff;
	}

	/**
	 * Get the standard deviation in bearing delta values in radians per second. 
	 * @return the standard deviation in bearing delta in rad/s
	 */
	public double getStdDelta() {
		return this.stdBearingDiff;
	}

}
