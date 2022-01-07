package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

/**
 * Seconds to milliseconds
 * @author Jamie Macaulay
 *
 */
public class Seconds2Millis extends ResultConverter {

	@Override
	public double convert2Control(double value) {
		return value*1000.; 
	}
	
	@Override
	public double convert2Value(double value) {
		return value/1000.; 
	}
	
}
