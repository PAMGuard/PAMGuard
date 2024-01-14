package tethys.deployment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RecordingList extends ArrayList<RecordingPeriod> {

	private static final long serialVersionUID = 1L;

	/**
	 * Get the duration of the recording periods from start to end. 
	 * @return
	 */
	public long duration() {
		return getEnd()-getStart();
	}
	
	/**
	 * Get the start of the first in the list. 
	 * @return
	 */
	public long getStart() {
		if (size() == 0) {
			return 0;
		}
		return get(0).getRecordStart();
	}
	
	/**
	 * get the end of the last in the list. 
	 */
	public long getEnd() {
		if (size() == 0) {
			return 0;
		}
		return get(size()-1).getRecordStop();
	}
	
	/**
	 * Sort the list in ascending order. 
	 */
	public void sort() {
		Collections.sort(this, new Comparator<RecordingPeriod>() {

			@Override
			public int compare(RecordingPeriod o1, RecordingPeriod o2) {
				return (int) Math.signum(o1.getRecordStart()-o2.getRecordStart());
			}
		});
	}
}
