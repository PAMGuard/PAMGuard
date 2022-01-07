package videoRangePanel.importTideData;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import dataMap.OfflineDataMapPoint;

public class TideOfflineDataMapPoint extends OfflineDataMapPoint implements ManagedParameters {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TideOfflineDataMapPoint(long startTime, long endTime, int nDatas, long missingUIDs) {
		super(startTime, endTime, nDatas, missingUIDs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Tide Data";
	}

	@Override
	public Long getLowestUID() {		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLowestUID(Long uid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Long getHighestUID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHighestUID(Long uid) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}

}
