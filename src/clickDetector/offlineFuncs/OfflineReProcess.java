package clickDetector.offlineFuncs;

/**
 * Processes that run offline. Will be passed one click
 * at a time, but may be able to do other things that use
 * the history of all clicks too. 
 * <p>
 * Something else will work out if this gets called for clicks
 * in one file or in multiple files. 
 * @author Doug Gillespie
 *
 */
public interface OfflineReProcess {

	String getName();
	
	void getReady();
	
	void processClick();
	
}
