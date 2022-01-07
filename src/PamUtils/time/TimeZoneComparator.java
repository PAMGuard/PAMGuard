package PamUtils.time;

import java.util.Comparator;
import java.util.TimeZone;

public class TimeZoneComparator implements Comparator<String> {

	public int compare(String id1, String id2) {
		TimeZone tz1 = TimeZone.getTimeZone(id1);
		TimeZone tz2 = TimeZone.getTimeZone(id2);
		int c = compare(tz1, tz2);
		if (c == 0) {
			return id1.compareTo(id2);
		}
		else {
			return c;
		}
	}
	public int compare(TimeZone tz1, TimeZone tz2) {
		int t1 = tz1.getRawOffset();
		int t2 = tz2.getRawOffset();
		return t1-t2;
	}


}
