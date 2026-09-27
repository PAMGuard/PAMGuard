package clickDetector.offlineFuncs;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickControl;
import generalDatabase.PamSubtableDefinition;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

/**
 * Standard sub detection logging for click detector offline events. This
 * replaces the old system whereby the clicks table
 * (unitName_OfflineClicks) doubled as the event sub table using an EventId
 * column. Click events now use exactly the same sub table system as the click
 * train detector and detection group localiser: a table of pointers matching
 * sub detections in the binary store to their super detections.
 * <p>
 * Old databases are migrated by {@link OfflineEventDatabaseMigration}.
 *
 * @author Jamie Macaulay
 */
public class OfflineEventSubLogging extends SQLLogging {

	public OfflineEventSubLogging(ClickControl clickControl, PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		setTableDefinition(new PamSubtableDefinition(clickControl.getUnitName()+"_OfflineEvents_Children"));
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		// nothing beyond the standard sub table columns.
	}

}
