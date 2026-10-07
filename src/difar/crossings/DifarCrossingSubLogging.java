package difar.crossings;

import PamguardMVC.PamDataUnit;
import generalDatabase.PamSubtableDefinition;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

/**
 * Logs the clips of each DIFAR crossing, one row per clip.
 * <p>
 * The rows hold links only: PAMGuard's standard subtable columns, giving the
 * crossing's UID and the clip's UID, time, data block and binary file.
 * Everything about a clip, including what comes from its buoy, is in the
 * clip's own row, so there is one copy to keep current. The query in
 * crossing_clips.sql joins the crossing, these links and the clip rows.
 */
public class DifarCrossingSubLogging extends SQLLogging {

	/**
	 * @param tableName the subtable, named after the crossing table.
	 * @param dataBlock the crossings.
	 */
	public DifarCrossingSubLogging(String tableName, DifarCrossingDataBlock dataBlock) {
		super(dataBlock);
		setTableDefinition(new PamSubtableDefinition(tableName));
	}

	/**
	 * Nothing to add: PAMGuard fills the standard subtable columns.
	 */
	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
	}
}
