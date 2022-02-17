package clickTrainDetector.classification.idiClassifier;

import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.classification.CTClassification;
import clickTrainDetector.classification.CTClassifierType;
import clickTrainDetector.classification.ClassifierJSONLogging;

/**
 * Classification result from the bearing classifier.
 * 
 * @author Jamie Macaulay
 *
 */
public class IDIClassification implements CTClassification {

	/**
	 * Mean bearing derivative. 
	 */
	private double medianIDI;

	/**
	 * Median bearing derivative.
	 */
	private double meanIDI; 

	/**
	 * Standard bearing derivative.
	 */
	private double stdIDI;

	/**
	 * The current species ID.
	 */
	private int speciesID;

	/**
	 * Bearing classifier JSON logging. 
	 */
	private IDIClassifierJSON idiClassifierJSONLogging; 

	/**
	 * Constructor for the bearing classifier. 
	 * @param speciesID 		- the speciesID flag. 
	 * @param medianIDI 		-  the median IDI in seconds. 
	 * @param meanIDI 			-  the mean IDI in seconds.
	 * @param stdIDI 			-  the standard deviation in IDI. 

	 */
	public IDIClassification(int speciesID, double medianIDI, double meanIDI, double stdIDI) {
		this.medianIDI 	= medianIDI;
		this.meanIDI	= meanIDI; 
		this.stdIDI		= stdIDI; 
		this.speciesID			= speciesID; 
		idiClassifierJSONLogging = new IDIClassifierJSON(); 
	}

	/**
	 * Bearing classification from a JSON string.
	 * @param jsonstring 		- JSON string containing the bearing data. 
	 */
	public IDIClassification(String jsonstring) {
		idiClassifierJSONLogging = new IDIClassifierJSON(); 
		CTClassification classification  = idiClassifierJSONLogging.createClassification(jsonstring); 
		this.speciesID			=classification.getSpeciesID(); 
		this.medianIDI	=((IDIClassification) classification).getMeanIDI();
		this.meanIDI	=((IDIClassification) classification).getMedianIDI();
		this.stdIDI		=((IDIClassification) classification).getStdIDI();
	}



	@Override
	public CTClassifierType getClassifierType() {
		return CTClassifierType.IDICLASSIFIER;
	}

	@Override
	public int getSpeciesID() {
		return speciesID;
	}

	@Override
	public String getSummaryString() {
		return  String.format("IDI Classifier: Mean IDI %.4f\u00B0/s Median IDI %.4f\u00B0/s Std IDI%.4f\u00B0/s",
				meanIDI, medianIDI, stdIDI);
	}

	@Override
	public ClassifierJSONLogging getJSONLogging() {
		return idiClassifierJSONLogging;
	}

	/**
	 * Get the median of bearing delta values in radians per second. 
	 * @return the median bearing delta in rad/s.
	 */
	public double getMedianIDI() {
		return this.medianIDI;
	}

	/**
	 * Get the mean of bearing delta values in radians per second. 
	 * @return the mean bearing delta in rad/s
	 * 
	 */
	public double getMeanIDI() {
		return this.meanIDI;
	}

	/**
	 * Get the standard deviation in bearing delta values in radians per second. 
	 * @return the standard deviation in bearing delta in rad/s
	 */
	public double getStdIDI() {
		return this.stdIDI;
	}

}