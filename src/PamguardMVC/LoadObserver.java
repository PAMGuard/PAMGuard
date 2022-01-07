package PamguardMVC;

/**
 * Monitor class for multi thread data loading in viewer mode. 
 * works with PamDataBlock.
 * @author Doug
 *
 */
public interface LoadObserver {

	public void setLoadStatus(int loadState);
	
}
