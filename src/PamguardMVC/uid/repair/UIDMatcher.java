package PamguardMVC.uid.repair;

import PamguardMVC.PamDataUnit;
import generalDatabase.SQLTypes;

/**
 * Interface which can take data unit or information 
 * from either a binary file or from a database and 
 * find the corresponding data in the other type of store. 
 * @author dg50
 *
 */
public interface UIDMatcher {

	/**
	 * SQL string to identify a database record for a particular data unit.
	 * @param pamDataUnit
	 * @return SQL string which can be used in an update or select query
	 */
	public String sqlSelectString(SQLTypes sqlTypes, PamDataUnit pamDataUnit);
	
}
