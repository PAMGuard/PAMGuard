package dataMap;

/**
 * Class to allow datamaps to update and notify the data map viewer each time 
 * they get additional data added (or even removed) as we start to use datamaps
 * in real time. 
 * @author dg50
 *
 */
public interface DataMapObserver {

	void updateDataMap(OfflineDataMap dataMap, OfflineDataMapPoint dataMapPoint);
	
}
