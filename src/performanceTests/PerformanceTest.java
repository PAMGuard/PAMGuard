package performanceTests;

/**
 * Interface for all performance tests. 
 * @author Doug Gillespie
 *
 */
public interface PerformanceTest {
	
	/**
	 * 
	 * @return The name of the test
	 */
	public String getName();
	
	/**
	 * Run the test
	 * @return true if executed successfully
	 */
	public boolean runTest();
	
	/**
	 * @return a text based results string to display. 
	 */
	public String getResultString(); 
	
	/**
	 * Called when dialog closes to give the test an opportunity to free 
	 * andy remaining resources (close graphics windows, etc.);
	 */
	public void cleanup();

}
