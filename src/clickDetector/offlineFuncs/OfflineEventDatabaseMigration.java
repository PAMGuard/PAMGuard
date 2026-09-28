package clickDetector.offlineFuncs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

import javax.swing.SwingUtilities;

import PamView.dialog.warn.WarnOnce;
import clickDetector.ClickControl;
import generalDatabase.DBControlUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamConnection;
import generalDatabase.SQLTypes;

/**
 * One-off migration of click event sub detection data from the old storage
 * format to the standard sub table format.
 * <p>
 * Historically the click detector stored event membership in the
 * unitName_OfflineClicks table: one row per click carrying an EventId
 * column. Click events now use the same standard sub table system as the click
 * train detector and detection group localiser
 * (unitName_OfflineEvents_Children). This class copies the old rows
 * across, warning the user that the upgraded event data will not be readable
 * by older versions of PAMGuard. The old tables are left untouched so nothing
 * is lost.
 *
 * @author Jamie Macaulay
 */
public class OfflineEventDatabaseMigration {

	private ClickControl clickControl;

	private OfflineEventDataBlock eventDataBlock;

	private boolean checkComplete = false;

	public OfflineEventDatabaseMigration(ClickControl clickControl, OfflineEventDataBlock eventDataBlock) {
		this.clickControl = clickControl;
		this.eventDataBlock = eventDataBlock;
	}

	/**
	 * Check whether old format click event data exist which have not yet been
	 * copied into the standard sub table, and if so ask the user and migrate.
	 * Safe to call repeatedly - only does anything on the first call.
	 */
	public synchronized void checkAndMigrate() {
		if (checkComplete) {
			return;
		}
		checkComplete = true;
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			return;
		}
		PamConnection con = DBControlUnit.findConnection();
		if (con == null) {
			return;
		}
		try {
			migrate(dbControl, con);
		}
		catch (Exception e) {
			System.err.println("OfflineEventDatabaseMigration: error migrating click event data: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private String eventsTableName() {
		return EmptyTableDefinition.deblankString(clickControl.getUnitName() + "_OfflineEvents");
	}

	private String oldClicksTableName() {
		return EmptyTableDefinition.deblankString(clickControl.getUnitName() + "_OfflineClicks");
	}

	private String childrenTableName() {
		return EmptyTableDefinition.deblankString(clickControl.getUnitName() + "_OfflineEvents_Children");
	}

	private void migrate(DBControlUnit dbControl, PamConnection con) throws Exception {
		SQLTypes sqlTypes = con.getSqlTypes();

		// anything in the old table ?
		int nOld = countRows(con, oldClicksTableName());
		if (nOld <= 0) {
			return;
		}
		// any events at all ?
		int nEvents = countRows(con, eventsTableName());
		if (nEvents <= 0) {
			return;
		}
		// anything already in the new table ? (already migrated)
		int nNew = countRows(con, childrenTableName());
		if (nNew > 0) {
			return;
		}

		final int[] answer = new int[1];
		String message = String.format("<html>This database contains %d click events in the old storage format.<br><br>" +
				"PAMGuard will now upgrade these to the standard detection group format used by " +
				"the click train detector and detection group modules.<br><br>" +
				"The original tables are left in place, but any events you create or edit " +
				"after the upgrade <b>will not be readable by older versions of PAMGuard</b>.<br><br>" +
				"Press OK to upgrade the database, or Cancel to leave it alone " +
				"(event clicks will not be available in this session).</html>", nEvents);
		Runnable ask = () -> answer[0] = WarnOnce.showWarning("Click event database upgrade",
				message, WarnOnce.OK_CANCEL_OPTION);
		if (SwingUtilities.isEventDispatchThread()) {
			ask.run();
		}
		else {
			SwingUtilities.invokeAndWait(ask);
		}
		if (answer[0] != WarnOnce.OK_OPTION) {
			return;
		}

		int nCopied = copyOldEventClicks(con.getConnection(), oldClicksTableName(), eventsTableName(),
				childrenTableName(), clickControl.getClickDataBlock().getLongDataName());
		dbControl.commitChanges();
		System.out.printf("Click event database upgrade: copied %d event clicks into %s\n",
				nCopied, childrenTableName());
	}

	/**
	 * Copy the standard sub table columns of every old format event click row
	 * into the new children table, filling in parent ids from the old EventId
	 * column and parent UIDs from the events table where the newer columns are
	 * missing or empty.
	 * @param con database connection.
	 * @param oldClicksTable name of the old _OfflineClicks table.
	 * @param eventsTable name of the events table.
	 * @param childrenTable name of the new children table.
	 * @param defaultLongName long data name of the click data block, used where
	 * the old rows don't have one.
	 * @return number of rows copied.
	 */
	static int copyOldEventClicks(java.sql.Connection con, String oldClicksTable, String eventsTable,
			String childrenTable, String defaultLongName) throws Exception {
		// map of event database index to event UID for filling in missing parent UIDs.
		HashMap<Integer, Long> eventUIDs = new HashMap<>();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(String.format("SELECT Id, UID FROM %s", eventsTable));
		while (rs.next()) {
			int id = rs.getInt(1);
			long uid = rs.getLong(2);
			eventUIDs.put(id, uid);
		}
		rs.close();

		String insertSql = String.format("INSERT INTO %s (UID, UTC, UTCMilliseconds, PCLocalTime, PCTime, " +
				"ChannelBitmap, SequenceBitmap, parentID, parentUID, LongDataName, BinaryFile) " +
				"VALUES (?,?,?,?,?,?,?,?,?,?,?)", childrenTable);
		PreparedStatement insertStmt = con.prepareStatement(insertSql);

		String selectSql = String.format("SELECT UID, UTC, UTCMilliseconds, PCLocalTime, PCTime, " +
				"ChannelBitmap, SequenceBitmap, parentID, parentUID, LongDataName, BinaryFile, EventId FROM %s", oldClicksTable);
		rs = stmt.executeQuery(selectSql);
		int nCopied = 0, nBatch = 0;
		while (rs.next()) {
			Object uid = rs.getObject(1);
			if (uid == null) {
				continue; // standard loader would skip these anyway.
			}
			Object parentId = rs.getObject(8);
			Object eventId = rs.getObject(12);
			if (parentId == null) {
				parentId = eventId;
			}
			if (parentId == null) {
				continue; // no way of knowing which event this row belonged to.
			}
			Object parentUID = rs.getObject(9);
			if (parentUID == null) {
				Long evUID = eventUIDs.get(((Number) parentId).intValue());
				parentUID = evUID;
			}
			String rowLongName = rs.getString(10);
			if (rowLongName == null) {
				rowLongName = defaultLongName;
			}
			insertStmt.setObject(1, uid);
			insertStmt.setObject(2, rs.getObject(2));
			insertStmt.setObject(3, rs.getObject(3));
			insertStmt.setObject(4, rs.getObject(4));
			insertStmt.setObject(5, rs.getObject(5));
			insertStmt.setObject(6, rs.getObject(6));
			insertStmt.setObject(7, rs.getObject(7));
			insertStmt.setObject(8, ((Number) parentId).intValue());
			insertStmt.setObject(9, parentUID);
			insertStmt.setObject(10, rowLongName);
			insertStmt.setObject(11, rs.getString(11));
			insertStmt.addBatch();
			nCopied++;
			if (++nBatch >= 500) {
				insertStmt.executeBatch();
				nBatch = 0;
			}
		}
		if (nBatch > 0) {
			insertStmt.executeBatch();
		}
		rs.close();
		stmt.close();
		insertStmt.close();
		return nCopied;
	}

	/**
	 * Count rows in a table, returning -1 if the table does not exist.
	 */
	private int countRows(PamConnection con, String tableName) {
		try {
			Statement stmt = con.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery(String.format("SELECT COUNT(*) FROM %s", tableName));
			int n = -1;
			if (rs.next()) {
				n = rs.getInt(1);
			}
			rs.close();
			stmt.close();
			return n;
		}
		catch (Exception e) {
			return -1;
		}
	}

}
