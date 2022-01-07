package clickDetector.echoDetection;

import clickDetector.ClickDetection;

/**
 * Interface to classes which do the actual work of detecting echoes. 
 * @author Doug Gillespie
 *
 */
public interface EchoDetector {

	/**
	 * Initialise the echo detector
	 */
	void initialise();
	
	/**
	 * Test to see if a single click is an echo or not. 
	 * @param clickDetection a single click detection. 
	 * @return true if the click is an echo. 
	 */
	boolean isEcho(ClickDetection clickDetection);
}
