package dataMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import PamController.OfflineDataStore;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;


/**
 * Class which get's held within a PamDataBlock which 
 * provides summary information on data within that 
 * block when operating in viewer mode. 
 * <p>
 * It is possible that some datablocks may own two of these
 * things - one for database data and one for binary stored data.  
 * @author Doug Gillespie
 *
 */
abstract public class OfflineDataMap<TmapPoint extends OfflineDataMapPoint> {

	private PamDataBlock  parentDataBlock; 

	private OfflineDataStore offlineDataStore;

	private List<TmapPoint> mapPoints;

	private DataMapDrawing specialDrawing;
	
	private List<DataMapObserver> dataMapObservers;

	/**
	 * time of the first data available for this data block
	 */
	private long firstDataTime;

	/**
	 * time of the last data available for this data block
	 */
	private long lastDataTime;

	/**
	 * Point in this store with most data. 
	 */
	private long highestPoint;

	/**
	 * Lowest point in this store (can be zero)
	 */
	private long lowestPoint; 

	/**
	 * Lowest non zero point in the store (can be 
	 * zero if no data or if highestPoint is zero)
	 */
	private long lowestNonZeroPoint;
	
	private Long lowestUID;
	
	private Long highestUID;
	
	private long missingUIDs;

	/**
	 * Vertical scaling - scale each Map data point to 
	 * a data rate, or don't bother scaling at all. 
	 */
	public static final int SCALE_NONE = 0;
	public static final int SCALE_PERSECOND = 1;
	public static final int SCALE_PERMINUTE = 2;
	public static final int SCALE_PERHOUR = 3;
	public static final int SCALE_PERDAY = 4;
	
	public static final int IN_GAP = 0x0;         // 0
	public static final int BEFORE_FIRST = 0x1;   // 1
	public static final int AFTER_LAST = 0x2;     // 2
	public static final int POINT_START = 0x4;    // 4
	public static final int POINT_END = 0x8;      // 8
	public static final int IN_DATA = 0x10;       // 16
	public static final int NO_DATA = 0x20;       // 32

	public OfflineDataMap(OfflineDataStore offlineDataStore, PamDataBlock parentDataBlock) {
		super();
		this.offlineDataStore = offlineDataStore;
		this.parentDataBlock = parentDataBlock;
		mapPoints = Collections.synchronizedList(new LinkedList<TmapPoint>());
		firstDataTime = Long.MAX_VALUE;
		lastDataTime = Long.MIN_VALUE;
	}

	/**
	 * Get an iterator over the map contents. 
	 * <p>
	 * Objects using this iterator should synchronise on the map object first. 
	 * @return an iterator over the map contents.
	 *  
	 */
	public Iterator<TmapPoint> getListIterator() {
		return mapPoints.iterator();
	}

	/**
	 * @return the parentDataBlock
	 */
	public PamDataBlock getParentDataBlock() {
		return parentDataBlock;
	}
	
	/**
	 * @param parentDataBlock the parentDataBlock to set
	 */
	public void setParentDataBlock(PamDataBlock parentDataBlock) {
		this.parentDataBlock = parentDataBlock;
	}

	/**
	 * Clear the data map.
	 */
	public synchronized void clear() {
		if (mapPoints != null) {
			mapPoints.clear();
		}
	}

	/**
	 * This is an annoying little function that will cast data map points
	 * created in the offline file servers, which also use types but because 
	 * they are constructed differently, the two classes don't realise they 
	 * have the same type. 
	 * @param mapPoint 
	 * @return cast map point (throw exception if not possible)
	 */
	public TmapPoint castMapPoint(OfflineDataMapPoint mapPoint) {
		return (TmapPoint) mapPoint;
	}
	/**
	 * Add a new map point into the list. 
	 * @param mapPoint new map point to add
	 */
	synchronized public void addDataPoint(TmapPoint mapPoint) {
		boolean first = (mapPoints.size() == 0);
		if (mapPoint.getStartTime() > 0) {
			firstDataTime = Math.min(firstDataTime, mapPoint.getStartTime());
		}
		if (mapPoint.getEndTime() > 0) {
			lastDataTime = Math.max(lastDataTime, mapPoint.getEndTime());
//			if (mapPoint.getEndTime() > System.currentTimeMillis()) {
//				System.out.println("Stupid large data time in " + mapPoint.getName());
//			}
		}
		mapPoints.add(mapPoint);

		// sort out range information
		int n = mapPoint.getNDatas();
		if (first) {
			highestPoint = lowestPoint = lowestNonZeroPoint = n;
		}
		else {
			highestPoint = Math.max(highestPoint, n);
			lowestPoint = Math.min(lowestPoint, n);
			if (lowestNonZeroPoint == 0) {
				lowestNonZeroPoint = n;
			}
			else if (n > 0) {
				lowestNonZeroPoint = Math.min(lowestNonZeroPoint, n);
			}
		}
		Long uid = mapPoint.getLowestUID();
		if (uid != null) {
			lowestUID = lowestUID == null ? uid : Math.min(lowestUID,  uid);
		}
		uid = mapPoint.getHighestUID();
		if (uid != null) {
			highestUID = highestUID == null ? uid : Math.max(highestUID,  uid);
		}
		missingUIDs += mapPoint.getMissingUIDs();
	}

	/**
	 * @return the total number of map points.  
	 */
	public int getNumMapPoints() {
		return mapPoints.size();
	}
	
	/**
	 * Get the number of map points AFTER or INCLUDING a given time 
	 * @param firstTime time to start search. 
	 * @return number of map points
	 */
	public int getNumMapPoints(long firstTime, long endTime) {
		int nPoints = 0;
		ListIterator<TmapPoint> it = mapPoints.listIterator();
		TmapPoint aPoint;
		while (it.hasNext()) {
			aPoint = it.next();
			if (aPoint.getStartTime() >= firstTime && aPoint.getEndTime()<endTime ) {
				nPoints++;
			}
		}
		return nPoints;
	}

	/**
	 * Sort all map points into ascending order based on start time.
	 * <p>
	 * this should be called after all data have been loaded. 
	 */
	synchronized public void sortMapPoints() {
		Collections.sort(mapPoints);
	}

	/**
	 * Needed to sort out some damaged binary data maps
	 * which are corrupted by being opened in viewer while data still writing. 
	 */
	public int removeDuplicates() {
		if (mapPoints == null || mapPoints.size() < 2) {
			return 0;
		}
		int dups = 0;
		ListIterator<TmapPoint> li = mapPoints.listIterator(mapPoints.size()-1);
		TmapPoint prevPoint = li.previous();
		TmapPoint aPoint;
		while (li.hasPrevious()) {
			aPoint = li.previous();
			if (aPoint.getStartTime() == prevPoint.getStartTime()) {
				li.remove();
				dups ++;
			}
			prevPoint = aPoint;
		}

		return dups;
	}
	
	/**
	 * Get the name of the data map - will default
	 * to the name of the underlying data store
	 * @return datamap name
	 */
	public String getDataMapName() {
		if (offlineDataStore == null) {
			return null;
		}
		else {
			return offlineDataStore.getDataSourceName();
		}
	}
	
	/**
	 * Called after de-serailisation of a data map so that 
	 * the min and max times stored in this object (which is not
	 * serialised can be extracted from the list of data which 
	 * WAS serialised.  
	 */
	synchronized public void sortRanges() {
		if (mapPoints == null) {
			return;
		}
		ListIterator<TmapPoint> it = mapPoints.listIterator();
		TmapPoint aPoint;
		firstDataTime = Long.MAX_VALUE;
		lastDataTime = Long.MIN_VALUE;
		int n;
		highestPoint = lowestPoint = lowestNonZeroPoint = 0;

		while (it.hasNext()) {
			aPoint = it.next();
			if (aPoint.getStartTime() > 0) {
				firstDataTime = Math.min(firstDataTime, aPoint.getStartTime());
			}
			if (aPoint.getEndTime() > 0) {
				lastDataTime = Math.max(lastDataTime, aPoint.getEndTime());
			}
			n = aPoint.getNDatas();
			highestPoint = Math.max(highestPoint, n);
			lowestPoint = Math.min(lowestPoint, n);
			if (lowestNonZeroPoint == 0) {
				lowestNonZeroPoint = n;
			}
			else if (n > 0) {
				lowestNonZeroPoint = Math.min(lowestNonZeroPoint, n);
			}
		}
	}

	/**
	 * @return the firstDataTime
	 */
	public long getFirstDataTime() {
		return firstDataTime;
	}

	/**
	 * @param firstDataTime the firstDataTime to set
	 */
	public void setFirstDataTime(long firstDataTime) {
		this.firstDataTime = firstDataTime;
	}

	/**
	 * @return the lastDataTime
	 */
	public long getLastDataTime() {
		return lastDataTime;
	}

	/**
	 * @param lastDataTime the lastDataTime to set
	 */
	public void setLastDataTime(long lastDataTime) {
		this.lastDataTime = lastDataTime;
	}


	/**
	 * 
	 * @return The total amount of data available. 
	 */
	public int getDataCount() {
		synchronized (this) {
			TmapPoint mapPoint;
			int nP = 0;
			Iterator<TmapPoint> it = getListIterator();
			while (it.hasNext()) {
				mapPoint = it.next();
				nP += mapPoint.getNDatas();
			}
			return nP;
		}
	}
	/**
	 * Return the highest point on the map using the given scale 
	 * @param vScaleType scale type
	 * @return the highest data value. 
	 */
	public double getHighestPoint(int vScaleType) {
		if (vScaleType == SCALE_NONE) {
			return highestPoint;
		}
		else {
			synchronized (this) {
				TmapPoint mapPoint;
				double hp = 0;
				double ap;
				Iterator<TmapPoint> it = getListIterator();
				while (it.hasNext()) {
					mapPoint = it.next();
					ap = scaleData(mapPoint.getNDatas(), mapPoint.getEndTime()-mapPoint.getStartTime(), vScaleType);
					hp = Math.max(hp, ap);
				}
				return hp;
			}
		}
	}
	
	/**
	 * Get the start time of the first datamap point or Long.minval
	 * @return
	 */
	public long getMapStartTime() {
		if (mapPoints == null || mapPoints.size() == 0) {
			return Long.MIN_VALUE;
		}
		return mapPoints.get(0).getStartTime();
	}
	
	/**
	 * Get the start time of the first datamap point or Long.minval
	 * @return
	 */
	public long getMapEndTime() {
		if (mapPoints == null || mapPoints.size() == 0) {
			return Long.MIN_VALUE;
		}
		return mapPoints.get(mapPoints.size()-1).getEndTime();
	}
	
	/**
	 * @return the lowestPoint
	 */
	public double getLowestPoint(int vScaleType) {
		if (vScaleType == SCALE_NONE || lowestPoint == 0) {
			return lowestPoint;
		}
		else {
			synchronized (this) {
				TmapPoint mapPoint;
				double lp = Double.MAX_VALUE;
				double ap;
				Iterator<TmapPoint> it = getListIterator();
				while (it.hasNext()) {
					mapPoint = it.next();
					ap = scaleData(mapPoint.getNDatas(), mapPoint.getEndTime()-mapPoint.getStartTime(), vScaleType);
					lp = Math.min(lp, ap);
				}
				return lp;
			}
		}
	}

	/**
	 * @return the lowestNonZeroPoint
	 */
	public double getLowestNonZeroPoint(int vScaleType) {
		if (vScaleType == SCALE_NONE || lowestNonZeroPoint == 0) {
			return lowestNonZeroPoint;
		}
		else {
			synchronized (this) {
				TmapPoint mapPoint;
				double lp = Double.MAX_VALUE;
				double ap;
				Iterator<TmapPoint> it = getListIterator();
				while (it.hasNext()) {
					mapPoint = it.next();
					if (mapPoint.getNDatas() == 0) {
						continue;
					}
					ap = scaleData(mapPoint.getNDatas(), mapPoint.getEndTime()-mapPoint.getStartTime(), vScaleType);
					lp = Math.min(lp, ap);
				}
				return lp;
			}
		}
	}

	public static double scaleData(double count, long duration, int scaleType) {
		if (duration > 0) {
			double secs = duration / 1000.;
			switch (scaleType) {
			case SCALE_NONE:
				return count;
			case SCALE_PERSECOND:
				return count / secs;
			case SCALE_PERMINUTE:
				return count / secs * 60;
			case SCALE_PERHOUR:
				return count / secs * 3600;
			case SCALE_PERDAY:
				return count / secs * 3600 * 24;
			}
		}
		return count;
	}

	/**
	 * @return the offlineDataSource
	 */
	public OfflineDataStore getOfflineDataSource() {
		return offlineDataStore;
	}

	/**
	 * @return the specialDrawing
	 */
	public DataMapDrawing getSpecialDrawing() {
		return specialDrawing;
	}

	/**
	 * @param specialDrawing the specialDrawing to set
	 */
	public void setSpecialDrawing(DataMapDrawing specialDrawing) {
		this.specialDrawing = specialDrawing;
	}

	/**
	 * Work out where the time is within a map. 
	 * @param timeMillis time
	 * @return integer field. one or more of 
	 * <p>NO_DATA
	 * <p>BEFORE_FIRST
	 * <p>AFTER_LAST
	 * <p>POINT_START
	 * <p>POINT_END
	 * <P>IN_GAP
	 * <p>IN_DATA
	 */
	public int isInGap(long timeMillis) {
		if (getNumMapPoints() == 0) {
			return NO_DATA;
		}
		else if (timeMillis < getFirstDataTime()) {
			return BEFORE_FIRST;
		}
		else if (timeMillis > getLastDataTime()) {
			return AFTER_LAST;
		}
		Iterator<TmapPoint> li = getListIterator();
		int a = IN_GAP;
		TmapPoint mapPoint;
		while (li.hasNext()) {
			mapPoint = li.next();
			if (mapPoint.getStartTime() == timeMillis) {
				a |= POINT_START | IN_DATA;
			}
			if (mapPoint.getEndTime() == timeMillis) {
				a |= POINT_END | IN_DATA;				
			}
			if (timeMillis > mapPoint.getStartTime() && timeMillis < mapPoint.getEndTime()) {
				a = IN_DATA;
			}
		}
		
		return a;
	}
	
	/**
	 * find a map point for a given time in milliseconds
	 * @param timeMillis time in milliseconds
	 * @return a map point or null if none found. 
	 */
	public TmapPoint findMapPoint(long timeMillis) {

		if (getNumMapPoints() == 0) {
			return null;
		}
		else if (timeMillis < getFirstDataTime()) {
			return null;
		}
		else if (timeMillis > getLastDataTime()) {
			return null;
		}
		Iterator<TmapPoint> li = getListIterator();
		int a = IN_GAP;
		TmapPoint mapPoint;
		TmapPoint foundPoint = null;
		while (li.hasNext()) {
			mapPoint = li.next();
			if (mapPoint.getStartTime() == timeMillis) {
				a |= POINT_START | IN_DATA;
				if (foundPoint != null) {
					warnDoubleFind(timeMillis, foundPoint, mapPoint);
				}
				foundPoint = mapPoint;
			}
			if (mapPoint.getEndTime() == timeMillis) {
				a |= POINT_END | IN_DATA;		
				if (foundPoint != null) {
					warnDoubleFind(timeMillis, foundPoint, mapPoint);
				}	
				foundPoint = mapPoint;	
			}
			if (timeMillis > mapPoint.getStartTime() && timeMillis < mapPoint.getEndTime()) {
				a = IN_DATA;
				if (foundPoint != null) {
					warnDoubleFind(timeMillis, foundPoint, mapPoint);
				}
				foundPoint = mapPoint;
			}
		}
		
		return foundPoint;
	}

	private void warnDoubleFind(long timeMillis, TmapPoint foundPoint,
			TmapPoint mapPoint) {
		System.out.println(String.format("Warning - data unit at time %s found in two map points %s - %s and %s - %s in %s ",
				PamCalendar.formatDateTime(timeMillis), PamCalendar.formatDateTime(foundPoint.getStartTime()),
				PamCalendar.formatDateTime(foundPoint.getEndTime()),PamCalendar.formatDateTime(mapPoint.getStartTime()),
				PamCalendar.formatDateTime(mapPoint.getEndTime()), this.parentDataBlock.getDataName()));
	}

	/**
	 * Get the next data start point. i.e. 
	 * the time of the start of a map point which is 
	 * > timeMillis
	 * @param timeMillis current time in milliseconds
	 * @return start time of the next data start. 
	 */
	public long getNextDataStart(long timeMillis) {
		if (getNumMapPoints() == 0) {
			return NO_DATA;
		}
		if (timeMillis > getLastDataTime()) {
			return AFTER_LAST;
		}
		Iterator<TmapPoint> li = getListIterator();
		int a = IN_GAP;
		TmapPoint mapPoint;
		while (li.hasNext()) {
			mapPoint = li.next();
			if (mapPoint.getStartTime() > timeMillis) {
				return mapPoint.getStartTime();
			}
		}
		return AFTER_LAST;
	}
	
	/**
	 * Get the previous data end point. i.e. 
	 * the time of the end of a map point which is 
	 * < timeMillis
	 * @param timeMillis current time in milliseconds
	 * @return start time of the next data start. 
	 */
	public long getPrevDataEnd(long timeMillis) {
		if (getNumMapPoints() == 0) {
			return NO_DATA;
		}
		if (timeMillis > getLastDataTime()) {
			return AFTER_LAST;
		}
		Iterator<TmapPoint> li = getListIterator();
		int a = IN_GAP;
		TmapPoint mapPoint;
		TmapPoint prevPoint = null;
		while (li.hasNext()) {
			mapPoint = li.next();
			if (mapPoint.getEndTime() > timeMillis) {
				if (prevPoint != null) {
					return prevPoint.getEndTime();
				}
				else {
					return BEFORE_FIRST;
				}
			}
			prevPoint = mapPoint;
		}
		return AFTER_LAST;
	}
	
	/**
	 * @return the mapPoints
	 */
	public List<TmapPoint> getMapPoints() {
		return mapPoints;
	}
	
	/**
	 * Get a list of map points that cover the given times
	 * @param dataStart start time
	 * @param dataEnd end time
	 * @return a list or null
	 */
	public List<TmapPoint> getMapPoints(long dataStart, long dataEnd) {
		List<TmapPoint> somePoints = new LinkedList<>();
		TmapPoint mp;
		Iterator<TmapPoint> it = getListIterator();
		while (it.hasNext()) {
			mp = it.next();
			if (mp.getEndTime() < dataStart) {
				continue;
			}
			if (mp.getStartTime() > dataEnd) {
				break;
			}
			somePoints.add(mp);
		}
		return somePoints;
	}
	
	public List<TmapPoint> getRootlessMapPoints() {
		List<TmapPoint> rmps = Collections.synchronizedList(new LinkedList<TmapPoint>());
		//clone rather than alter a new ref
		
		
		
		for(TmapPoint mapPoint:mapPoints){
			OfflineDataMapPoint a = mapPoint.clone();
//			a.
//			rmps.add()
		}
		
		
		
		
		return rmps;
	}

	/**
	 * @param mapPoints the mapPoints to set
	 */
	public void setMapPoints(List<TmapPoint> mapPoints) {
		this.mapPoints = mapPoints;
	}

	/**
	 * Compare two map point lists and see which of list1 are in list 2 and which 
	 * of list 2 are in list 1. Assumes that both lists are sorted and that start
	 * times are unique. 
	 * @param list1
	 * @param list2
	 */
	public void compareLists(List<TmapPoint> list1, List<TmapPoint> list2) {
		if (list1 == null || list2 == null) {
			return;
		}
		if (list1.size() == 0 || list2.size() == 0) {
			return;
		}
		Collections.sort(list1);
		Collections.sort(list2);
		ListIterator<TmapPoint> it1 = list1.listIterator();
		ListIterator<TmapPoint> it2 = list2.listIterator();
		TmapPoint p1, p2;
		p1 = it1.next();
		p2 = it2.next();
		while (p1 != null && p2 != null) {
			if (p1.equals(p2)) {
				/*
				 * If they are equal, then match them and move both lists forward. 
				 */
				p1.setMatchedPoint(p2);
				p2.setMatchedPoint(p1);
				p1 = advanceIterator(it1);
				p2 = advanceIterator(it2);
			}
			else if (p1.getStartTime() < p2.getStartTime()) {
				/*
				 * p1 is earlier than p2, so advance p1 in hope of a match next time around
				 */
				p1 = advanceIterator(it1);
			}
			else {
				/**
				 * Otherwise advance p2 (nave to advance at least one list each loop or 
				 * we'll get stuck !
				 */
				p2 = advanceIterator(it2);
			}
		}
	}

	private TmapPoint advanceIterator(ListIterator<TmapPoint> iterator) {
		if (iterator.hasNext()) {
			return iterator.next();
		}
		else {
			return null;
		}
	}

	/**
	 * @return the lowestUID
	 */
	public Long getLowestUID() {
		return lowestUID;
	}

	/**
	 * @param lowestUID the lowestUID to set
	 */
	public void setLowestUID(Long lowestUID) {
		this.lowestUID = lowestUID;
	}

	/**
	 * @return the highestUID
	 */
	public Long getHighestUID() {
		return highestUID;
	}

	/**
	 * @param highestUID the highestUID to set
	 */
	public void setHighestUID(Long highestUID) {
		this.highestUID = highestUID;
	}

	/**
	 * @return the missingUIDs
	 */
	public long getMissingUIDs() {
		missingUIDs = 0;
		for (TmapPoint mapPoint:mapPoints) {
			missingUIDs += mapPoint.getMissingUIDs();
		}
		return missingUIDs;
	}

	/**
	 * @param missingUIDs the missingUIDs to set
	 */
	public void setMissingUIDs(long missingUIDs) {
		this.missingUIDs = missingUIDs;
	}

	public synchronized TmapPoint getLastMapPoint() {
		if (mapPoints.size() > 0) {
			return mapPoints.get(mapPoints.size()-1);
		}
		else {
			return null;
		}
	}
	/**
	 * Called when new (not reloaded) data are added to a datablock 
	 * so that the data map can update. 
	 * @param pamDataUnit
	 * @return current data map point
	 */
	public TmapPoint newPamDataUnit(PamDataUnit pamDataUnit) {
		TmapPoint lastMapPoint = getLastMapPoint();
		if (lastMapPoint != null) {
			lastMapPoint.addOneData(pamDataUnit.getTimeMilliseconds());
			notifyMapObservers(lastMapPoint);
			setLastDataTime(lastMapPoint.getEndTime());
		}
		return lastMapPoint;
	}

	/**
	 * Called when new (not reloaded) data are updated in a datablock 
	 * so that the data map can update. 
	 * @param pamDataUnit
	 * @return current data map point
	 */
	public TmapPoint updatedPamDataUnit(PamDataUnit pamDataUnit) {
		return null;
	}

	/**
	 * Add a data map observer to get notificatoins when data are updated. 
	 * @param dataMapObserver
	 */
	public void addDataMapObserver(DataMapObserver dataMapObserver) {
		if (dataMapObservers == null) {
			dataMapObservers = new ArrayList<DataMapObserver>();
		}
		dataMapObservers.add(dataMapObserver);
	}
	
	/**
	 * Notify datamap observers that a data point has changed / been added, etc. 
	 * @param mapPoint
	 */
	public void notifyMapObservers(OfflineDataMapPoint mapPoint) {
		if (dataMapObservers == null) {
			return;
		}
		for (DataMapObserver obs : dataMapObservers) {
			obs.updateDataMap(this, mapPoint);
		}
	}
}
