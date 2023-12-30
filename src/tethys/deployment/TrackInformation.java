package tethys.deployment;

import java.util.Iterator;

import Array.HydrophoneLocator;
import GPS.GPSDataBlock;
import PamguardMVC.PamDataBlock;
import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;

/**
 * Some general information about the track: whether it exists and 
 * the frequency of GPS points. 
 * @author dg50
 *
 */
public class TrackInformation {

	private HydrophoneLocator hydrophoneLocator;
	private OfflineDataMap gpsDataMap;

	public TrackInformation(OfflineDataMap gpsDataMap, HydrophoneLocator locator) {
		this.gpsDataMap = gpsDataMap;
		this.hydrophoneLocator = locator;
	}

	public boolean haveGPSTrack() {
		if (gpsDataMap == null) {
			return false;
		}
		return (gpsDataMap.getDataCount() > 0);
	}

	/**
	 * Get an estimate of the highest GPS data rate in points per second. This is obtained from the 
	 * datamap, taking the highest rate for all data map points (typically an hour of 
	 * database data). 
	 * @return
	 */
	public double getGPSDataRate() {
		if (gpsDataMap == null) {
			return 0;
		}
		GPSDataBlock gpsDataBlock = (GPSDataBlock) gpsDataMap.getParentDataBlock();
		Iterator<OfflineDataMapPoint> mPs = gpsDataMap.getListIterator();
		double highRate = 0;
		while (mPs.hasNext()) {
			OfflineDataMapPoint mP = mPs.next();
			int n = mP.getNDatas();
			double dur = (mP.getEndTime()-mP.getStartTime())/1000.;
			double rate = n/dur;
			highRate = Math.max(highRate, rate);
		}
		return highRate;
	}

	/**
	 * @return the hydrophoneLocator
	 */
	public HydrophoneLocator getHydrophoneLocator() {
		return hydrophoneLocator;
	}

	/**
	 * @param hydrophoneLocator the hydrophoneLocator to set
	 */
	public void setHydrophoneLocator(HydrophoneLocator hydrophoneLocator) {
		this.hydrophoneLocator = hydrophoneLocator;
	}

	/**
	 * @return the gpsDataMap
	 */
	public OfflineDataMap getGpsDataMap() {
		return gpsDataMap;
	}

	/**
	 * @param gpsDataMap the gpsDataMap to set
	 */
	public void setGpsDataMap(OfflineDataMap gpsDataMap) {
		this.gpsDataMap = gpsDataMap;
	}

}
