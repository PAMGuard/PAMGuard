package PamguardMVC.blockprocess;

import java.util.ArrayList;
import java.util.List;

import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;

/**
 * A list of PamDataUnits, set as a data unit itself so that it can be 
 * passed through a threaded observer. 
 * @author dg50
 *
 */
public class PamBlockDataList extends PamDataUnit {
	
	private List<PamDataUnit> unitsList = new ArrayList<>();

	public PamBlockDataList() {
		super(0);
	}
	
	public void add(PamDataUnit dataUnit) {
		if (unitsList.isEmpty()) {
			setTimeMilliseconds(dataUnit.getTimeMilliseconds());
		}
		unitsList.add(dataUnit);
		// keep track of channels included in this thing. 
		setChannelBitmap(getChannelBitmap() | dataUnit.getChannelBitmap());
		// and it's duration. This will go wrong with a reversed block list. Do we care ? 
		// consider doing a minmax of both start and end for every list ? 
		setDurationInMilliseconds(dataUnit.getEndTimeInMilliseconds()-getTimeMilliseconds());
	}
	
	/**
	 * This should return a negative number if it's a reversed list. 
	 * @return length of list in milliseconds
	 */
	public long getDurationMillis() {
		if (unitsList.size() == 0) {
			return 0;
		}
		long t1 = unitsList.get(0).getTimeMilliseconds();
		long t2 = unitsList.get(unitsList.size()-1).getEndTimeInMilliseconds();
		return t2-t1;
	}

	/**
	 * 
	 * @return the internal list of PamDataUnit's. 
	 */
	public List<PamDataUnit> getList() {
		return unitsList;
	}
	
	public int getNumDataUnits() {
		return unitsList.size();
	}


}
