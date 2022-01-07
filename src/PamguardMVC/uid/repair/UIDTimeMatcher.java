package PamguardMVC.uid.repair;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.SQLTypes;

public class UIDTimeMatcher implements UIDMatcher {

	private PamDataBlock dataBlock;

	public UIDTimeMatcher(PamDataBlock dataBlock) {
		this.dataBlock = dataBlock;
	}

	@Override
	public String sqlSelectString(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		/*
		 * Bit of a pain since some dbs are only to the second whereas others support milliseconds
		 * so we'll have to query into a range of values and double check on the milliseconds. 
		 */
		int millis = (int) (pamDataUnit.getTimeMilliseconds()%1000);
		long seconds = pamDataUnit.getTimeMilliseconds()-millis; 
		String sql = String.format(" WHERE UTC BETWEEN %s and %s AND UTCMilliseconds = %d", sqlTypes.formatDBDateTimeQueryString(seconds), 
				sqlTypes.formatDBDateTimeQueryString(seconds + 1000), millis);
		return sql;
	}

}
