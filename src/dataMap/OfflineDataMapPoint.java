package dataMap;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamUtils.PamCalendar;

/**
 * Map points to go into an OfflineDataMap. 
 * @author Doug Gillespie
 *
 */
abstract public class OfflineDataMapPoint implements Comparable<OfflineDataMapPoint>, Serializable,Cloneable, ManagedParameters {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @return a name for the map point
	 */
	abstract public String getName();

	/**
	 * Start time of map point data
	 */
	private long startTime;

	/**
	 * End time of map point data
	 */
	private long endTime;

	/**
	 * Number of data points. 
	 */
	private int nDatas;

	/**
	 * Flag used when matching up data maps with serialised ones to 
	 * see if they are matched to the other list or not. 
	 */
	private transient OfflineDataMapPoint matchedPoint;

	private long missingUIDs;

	public OfflineDataMapPoint(long startTime, long endTime, int nDatas, long missingUIDs) {
		super();
		this.startTime = startTime;
		this.endTime = endTime;
		this.nDatas = nDatas;
		this.missingUIDs = missingUIDs;
	}

	@Override
	public OfflineDataMapPoint clone(){
		OfflineDataMapPoint a;
		try{
			a= (OfflineDataMapPoint) super.clone();
		}catch(Exception e){
			a= null;
			e.printStackTrace();
		}
		return a;
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the endTime
	 */
	public long getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	/**
	 * Add a uid to this map point which will keep track of the lowest and largest
	 * values. 
	 * @param uid can be null (will be null for old data). 
	 */
	public void setMinMaxUID(Long uid) {
		if (uid == null) {
			return;
		}
		if (getLowestUID() == null) {
			setLowestUID(uid);
		}
		else {
			setLowestUID(Math.min(getLowestUID(), uid));
		}
		if (getHighestUID() == null) {
			setHighestUID(uid);
		}
		else {
			setHighestUID(Math.max(getHighestUID(), uid));
		}

	}

	/**
	 * @return the nDatas
	 */
	public int getNDatas() {
		return nDatas;
	}

	/**
	 * @param datas the nDatas to set
	 */
	public void setNDatas(int datas) {
		nDatas = datas;
	}

	/**
	 * Test whether or not this map point overlaps with 
	 * a pair of times. 
	 * @param startTime start time in milliseconds
	 * @param endTime end time in milliseconds. 
	 * @return true if there is any overlap.
	 */
	public boolean coincides(long startTime, long endTime) {
		if (endTime < getStartTime() || startTime > getEndTime()) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(OfflineDataMapPoint o) {
		/**
		 * Need to return an int - avoid long wrap around, so
		 * don't just do a cast !
		 */
		long tDiff = startTime - o.getStartTime();
		if (tDiff < 0) {
			return -1;
		}
		else if (tDiff > 0) {
			return 1;
		}
		else {
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		return startTime == ((OfflineDataMapPoint) obj).startTime;
	}

	/**
	 * @return the matchedPoint
	 */
	public OfflineDataMapPoint getMatchedPoint() {
		return matchedPoint;
	}

	/**
	 * @param matchedPoint the matchedPoint to set
	 */
	public void setMatchedPoint(OfflineDataMapPoint matchedPoint) {
		this.matchedPoint = matchedPoint;
	}

	/**
	 * @return the lowestUId
	 */
	public abstract Long getLowestUID();
	
	public abstract void setLowestUID(Long uid);

	/**
	 * @return the highestUID
	 */
	public abstract Long getHighestUID();
	
	public abstract void setHighestUID(Long uid);

	/**
	 * @return the missingUIDs
	 */
	public long getMissingUIDs() {
		if (getHighestUID() == null || getLowestUID() == null) {
			missingUIDs = nDatas;
		}

		return missingUIDs;
	}

	/**
	 * @param missingUIDs the missingUIDs to set
	 */
	public void setMissingUIDs(long missingUIDs) {
		this.missingUIDs = missingUIDs;
	}

	public void addMissingUID() {
		missingUIDs++;
	}

	@Override
	public String toString() {
		String str = String.format("%s - %s", PamCalendar.formatDateTime(startTime, true),
				PamCalendar.formatDateTime(endTime, true));
		if (getLowestUID() != null) {
			str += String.format("; UIDs %d-%d", getLowestUID(), getHighestUID());
		}
		str += String.format("; %d datas", getNDatas());
		return str;
	}

	public void addOneData(long timeMilliseconds) {
		nDatas++;
		if (endTime < 0) {
			endTime = timeMilliseconds;
		}
		else {
			endTime = Math.max(endTime, timeMilliseconds);
		}
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
