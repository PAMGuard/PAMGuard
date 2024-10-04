package generalDatabase;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamUtils.PamCalendar;
import dataMap.OfflineDataMapPoint;

public class DBOfflineDataMapPoint extends OfflineDataMapPoint implements ManagedParameters {

	private Long lowestUId;

	private Long highestUID;
	
	public DBOfflineDataMapPoint(long startTime, long endTime, int datas, Long lowestUId, Long highestUID, long missingUIDs) {
		super(startTime, endTime, datas,  missingUIDs);
		this.lowestUId = lowestUId;
		this.highestUID = highestUID;
	}

	/**
	 * Add a new end time to the data map
	 * @param utcMillis time in millis
	 * @param uid 
	 */
	public void addNewEndTime(long utcMillis, Long uid) {
		setEndTime(utcMillis);
		setNDatas(getNDatas()+1);
		setMinMaxUID(uid);
		if (uid == null) {
			addMissingUID();
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.OfflineDataMapPoint#getName()
	 */
	@Override
	public String getName() {
		return String.format("Database %s to %s", 
				PamCalendar.formatDateTime(getStartTime()), 
				PamCalendar.formatTime(getEndTime()));
	}

	@Override
	public Long getLowestUID() {
		return lowestUId;
	}

	@Override
	public void setLowestUID(Long uid) {
		this.lowestUId = uid;
	}

	@Override
	public Long getHighestUID() {
		return highestUID;
	}

	@Override
	public void setHighestUID(Long uid) {
		this.highestUID = uid;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}

}
