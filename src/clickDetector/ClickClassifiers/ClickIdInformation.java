package clickDetector.ClickClassifiers;

/**
 * Class for holding classification information. 
 * 
 * @author Doug Gillespie
 *
 */
public class ClickIdInformation {

	/**
	 *The first classifier in the list which the click passed. 
	 */
	public int clickType;
	
	/**
	 * True to discard thee click if classified other than 0. 
	 */
	public boolean discard;
	
	/**
	 * The classifiers in the list which the click passed. Each entry is a type flag. 
	 */
	public int[] classifiersPassed;
	

	public ClickIdInformation(int clickType, boolean discard) {
		super();
		this.clickType = clickType;
		this.discard = discard;
	}

	public ClickIdInformation(int clickType) {
		super();
		this.clickType = clickType;
	}
	
}
