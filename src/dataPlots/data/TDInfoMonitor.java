package dataPlots.data;

/**
 * Gets callbacks from the registry menu when a 
 * particular provider is selected. 
 * @author Doug Gillespie
 *
 */
public interface TDInfoMonitor {

	/**
	 * Called when a data provider is selected in the central menu
	 * @param dataProvider
	 */
	void selectProvider(TDDataProvider dataProvider);

}
