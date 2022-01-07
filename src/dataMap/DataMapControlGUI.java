package dataMap;

/**
 * Interface for additional functions of the datamap GUI. 
 * @author Jamie Macaulay
 *
 */
public interface DataMapControlGUI {
	
	public void createDataGraphs(); 
	
	/***
	 * Called whenever new data sources have been added to the datamap. 
	 */
	public void newDataSources();
	
	public void repaintAll(); 

}
