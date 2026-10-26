package difar.crossings;

import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SubdetectionInfo;
import PamguardMVC.superdet.SuperDetection;
import generalDatabase.PamConnection;
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

	/**
	 * Logs a crossing's clips in this table without changing the clips'
	 * own database indexes.
	 * <p>
	 * A data unit has one database index. A DIFAR clip has two rows: its own,
	 * in the clip table, and its link row in this table. The standard
	 * {@link SQLLogging#logSubtableData} writes and updates link rows through
	 * the clip's index, and leaves the clip holding the link row's index.
	 * The clip's next update, for example when a later crossing claims it,
	 * was then written into the clip table at the link row's index, over a
	 * different clip.
	 * <p>
	 * So while the standard method runs, each clip holds the index of its
	 * link row (zero if it has none yet), and afterwards gets its own index
	 * back. The standard method records any new link row index in the
	 * crossing's subdetection information, as before.
	 */
	@Override
	protected synchronized boolean logSubtableData(PamConnection con, SuperDetection superDetection) {
		int n = superDetection.getSubDetectionsCount();
		PamDataUnit[] clips = new PamDataUnit[n];
		int[] ownIndex = new int[n];
		for (int i = 0; i < n; i++) {
			SubdetectionInfo info = superDetection.getSubdetectionInfo(i);
			PamDataUnit clip = info == null ? null : info.getSubDetection();
			if (clip == null) {
				continue;
			}
			clips[i] = clip;
			ownIndex[i] = clip.getDatabaseIndex();
			clip.setDatabaseIndex(info.getChildDatabaseIndex());
		}
		try {
			return super.logSubtableData(con, superDetection);
		}
		finally {
			for (int i = 0; i < n; i++) {
				if (clips[i] != null) {
					clips[i].setDatabaseIndex(ownIndex[i]);
				}
			}
		}
	}
}
