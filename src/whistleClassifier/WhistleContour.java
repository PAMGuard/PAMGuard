package whistleClassifier;

/**
 * Interface to get the basic information out of a 
 * whistle object. 
 * @author Doug Gillespie
 *
 */
public interface WhistleContour {
	
	double[] getFreqsHz();
	
	double[] getTimesInSeconds();
	
}
