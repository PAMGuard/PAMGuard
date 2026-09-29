package difar.offline;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSink;
import binaryFileStorage.BinaryFooter;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.BinaryOfflineDataMap;
import binaryFileStorage.BinaryOfflineDataMapPoint;
import binaryFileStorage.BinaryStore;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;
import dataMap.OfflineDataMapPoint;
import difar.DifarClipPayload;
import difar.DifarControl;
import difar.DifarDataBlock;
import difar.DifarDataUnit;
import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import generalDatabase.SQLLogging;
import offlineProcessing.OfflineTask;
import offlineProcessing.TaskGroupParams;

/**
 * Upgrades a dataset written by an older version of PAMGuard, so it can be
 * edited in the viewer.
 * <p>
 * Files before DIFAR module version 3 hold each clip's crossing inside the
 * clip, and are read only. The upgrade:
 * <ol>
 * <li>reads every DIFAR file once and checks whether different clips share a
 * UID. If they do, it offers to renumber every clip, or cancel;</li>
 * <li>backs up every DIFAR binary file, beside the binary store, and copies
 * the database file into the same backup folder. It does nothing more if
 * either copy fails;</li>
 * <li>deletes every DIFAR clip row from the database;</li>
 * <li>as each file loads: drops second copies of a clip, renumbers the clips
 * if that was chosen, writes one row per clip, and rematches every clip,
 * exactly as {@link RematchTask} does, making crossing units;</li>
 * <li>marks each clip from an older file as changed, so PAMGuard rewrites its
 * file at version 3.</li>
 * </ol>
 * The old crossings were made by older code, and stay in the backup only, as a
 * record of what was thought at the time. The new crossings are the best
 * current estimate.
 * <p>
 * Run it over all data, so that matching can find partners anywhere in the
 * dataset. Replaces the old copy to database task, which rewrote the files as
 * a side effect, with no backup and no rematch.
 */
public class UpgradeTask extends OfflineTask<DifarDataUnit> {

	private final DifarControl difarControl;
	private final RematchTask rematch;

	/**
	 * True from a prepareTask that backed up the files, to completeTask. Core
	 * completes every task in a group, even those not ticked or not prepared.
	 */
	private boolean backedUp;
	private int upgraded;

	/** Whether this run gives every clip a new UID. */
	private boolean renumber;
	/** The UID the next renumbered clip gets. */
	private long nextUID;

	/** Every clip loaded so far in this run, by its UID, channel and start time as stored. */
	private final Set<String> seen = new HashSet<>();

	/** The database connection, or null if there is no database. */
	private PamConnection connection;
	private int rowsDeleted, rowsWritten;

	/**
	 * @param difarControl the DIFAR module.
	 */
	public UpgradeTask(DifarControl difarControl) {
		super(difarControl.getDifarProcess().getProcessedDifarData());
		this.difarControl = difarControl;
		this.rematch = new RematchTask(difarControl, null);
		addRequiredDataBlock(difarControl.getDifarProcess().getCrossingDataBlock());
		addAffectedDataBlock(difarControl.getDifarProcess().getCrossingDataBlock());
		addAffectedDataBlock(getDataBlock());
		// the group loads the blocks this task requires, not those of the rematch it runs
		RematchTask.requireBuoyRecords(this);
	}

	@Override
	public String getName() {
		return "Upgrade old DIFAR files to version " + DifarClipPayload.CURRENT_VERSION;
	}

	@Override
	public void prepareTask() {
		upgraded = 0;
		backedUp = false;
		renumber = false;
		nextUID = 1;
		seen.clear();
		rowsDeleted = 0;
		rowsWritten = 0;
		if (getOfflineTaskGroup().getTaskGroupParams().dataChoice != TaskGroupParams.PROCESS_ALL) {
			warn("The upgrade runs over all data only, so nothing was upgraded. "
					+ "Choose all data and run it again.");
			return;
		}
		CensusSink census = takeCensus();
		if (census == null) {
			return;
		}
		System.out.println("DIFAR: " + census.uids.summary());
		connection = DBControlUnit.findConnection();
		if (!census.uids.isUnique()) {
			int crossingRows = countCrossingRows();
			if (census.currentClips > 0 || crossingRows != 0) {
				warn(String.format("%d different clips share %d UIDs, and renumbering them would cut the links of "
						+ "crossings already made: %d clips are already at version %d, and the database holds %s "
						+ "crossings. Nothing was upgraded.",
						census.uids.getClipsSharingUIDs(), census.uids.getSharedUIDs(), census.currentClips,
						DifarClipPayload.CURRENT_VERSION, crossingRows < 0 ? "an unknown number of" : "" + crossingRows));
				return;
			}
			if (!askToRenumber(census.uids)) {
				System.out.println("DIFAR: upgrade cancelled, so nothing was changed");
				return;
			}
			renumber = true;
		}
		if (census.uids.getExactDuplicates() > 0) {
			System.out.printf("DIFAR: %d clips are stored twice. The second copies will be dropped\n",
					census.uids.getExactDuplicates());
		}
		backedUp = backUp();
		if (!backedUp) {
			return;
		}
		deleteClipRows();
		getDifarDataBlock().startCollectingSecondCopies();
		rematch.prepareTask();
	}

	/**
	 * Show a warning, and print it to the console.
	 * @param message the warning.
	 */
	private void warn(String message) {
		System.out.println("DIFAR: " + message);
		// the task runs off the Swing thread
		SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(difarControl.getGuiFrame(),
				message, getName(), JOptionPane.WARNING_MESSAGE));
	}

	/**
	 * Ask whether to renumber every clip, and wait for the answer.
	 * @param census how the UIDs are used.
	 * @return true to renumber, false to cancel.
	 */
	private boolean askToRenumber(UidCensus census) {
		String message = String.format("%d different clips share %d UIDs, probably because the UID count "
				+ "restarted when PAMGuard crashed during recording.%n%n"
				+ "The upgrade can give every clip a new UID, counting from 1 in time order. The old UIDs "
				+ "will be kept only in the backup. Other records can still be paired with clips by channel "
				+ "and start time.%n%nRenumber every clip and upgrade, or cancel and change nothing?",
				census.getClipsSharingUIDs(), census.getSharedUIDs());
		System.out.println("DIFAR: " + message.replace(String.format("%n"), " "));
		Object[] options = {"Renumber and upgrade", "Cancel"};
		int[] answer = {JOptionPane.CLOSED_OPTION};
		Runnable ask = () -> answer[0] = JOptionPane.showOptionDialog(difarControl.getGuiFrame(), message,
				getName(), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
		if (SwingUtilities.isEventDispatchThread()) {
			ask.run();
		}
		else {
			try {
				SwingUtilities.invokeAndWait(ask);
			}
			catch (InterruptedException | InvocationTargetException e) {
				return false;
			}
		}
		return answer[0] == 0;
	}

	/**
	 * @return the number of crossing rows in the database, 0 if there is no
	 * database or no crossing table, or -1 if they cannot be counted.
	 */
	private int countCrossingRows() {
		SQLLogging logging = difarControl.getDifarProcess().getCrossingDataBlock().getLogging();
		if (connection == null || logging == null) {
			return 0;
		}
		String table = connection.getSqlTypes().formatTableName(logging.getTableDefinition().getTableName());
		try (Statement statement = connection.getConnection().createStatement();
				ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
			return result.next() ? result.getInt(1) : 0;
		}
		catch (SQLException e) {
			// most likely no crossing table yet, in a dataset never upgraded
			return 0;
		}
	}

	/**
	 * Read every DIFAR binary file once, without loading its clips into the
	 * data block, and count how their UIDs are used.
	 * @return the count, or null if there are no files or one cannot be read.
	 */
	private CensusSink takeCensus() {
		BinaryStore binaryStore = BinaryStore.findBinaryStoreControl();
		if (binaryStore == null) {
			System.out.println("DIFAR: no binary store, so nothing was upgraded");
			return null;
		}
		BinaryOfflineDataMap dataMap = (BinaryOfflineDataMap) getDataBlock().getOfflineDataMap(binaryStore);
		if (dataMap == null) {
			System.out.println("DIFAR: no DIFAR binary files, so nothing was upgraded");
			return null;
		}
		CensusSink sink = new CensusSink();
		for (BinaryOfflineDataMapPoint mapPoint : dataMap.getMapPoints()) {
			if (!binaryStore.loadData(getDataBlock(), mapPoint, Long.MIN_VALUE, Long.MAX_VALUE, sink)) {
				warn("Could not read " + mapPoint.getBinaryFile(binaryStore) + ", so nothing was upgraded.");
				return null;
			}
		}
		return sink;
	}

	/**
	 * Counts each clip read from a file, and keeps nothing else.
	 */
	private static class CensusSink implements BinaryDataSink {

		private final UidCensus uids = new UidCensus();

		/** Clips already at the current version. */
		private int currentClips;

		@Override
		public boolean newDataUnit(BinaryObjectData binaryObjectData, PamDataBlock dataBlock, PamDataUnit dataUnit) {
			uids.add(dataUnit.getUID(), dataUnit.getChannelBitmap(), dataUnit.getTimeMilliseconds());
			if (dataUnit instanceof DifarDataUnit
					&& ((DifarDataUnit) dataUnit).getBinaryVersion() >= DifarClipPayload.CURRENT_VERSION) {
				currentClips++;
			}
			return true;
		}

		@Override
		public void newFileHeader(BinaryHeader binaryHeader) {
		}

		@Override
		public void newModuleHeader(BinaryObjectData binaryObjectData, ModuleHeader moduleHeader) {
		}

		@Override
		public void newModuleFooter(BinaryObjectData binaryObjectData, ModuleFooter moduleFooter) {
		}

		@Override
		public void newFileFooter(BinaryObjectData binaryObjectData, BinaryFooter binaryFooter) {
		}

		@Override
		public void newDatagram(BinaryObjectData binaryObjectData) {
		}
	}

	/**
	 * Copy every DIFAR binary file, with its index and noise files, and the
	 * database file, into one backup folder beside the binary store.
	 * @return true if everything was copied.
	 */
	private boolean backUp() {
		BinaryStore binaryStore = BinaryStore.findBinaryStoreControl();
		if (binaryStore == null) {
			System.out.println("DIFAR: no binary store, so nothing was upgraded");
			return false;
		}
		BinaryOfflineDataMap dataMap = (BinaryOfflineDataMap) getDataBlock().getOfflineDataMap(binaryStore);
		if (dataMap == null) {
			System.out.println("DIFAR: no DIFAR binary files, so nothing was upgraded");
			return false;
		}
		ViewerBackup backup = new ViewerBackup(new File(binaryStore.getBinaryStoreSettings().getStoreLocation()));
		int nFiles = 0;
		try {
			for (BinaryOfflineDataMapPoint mapPoint : dataMap.getMapPoints()) {
				File data = mapPoint.getBinaryFile(binaryStore);
				backup.copy(data);
				backup.copy(binaryStore.swapFileType(data, BinaryStore.indexFileType));
				backup.copy(binaryStore.swapFileType(data, BinaryStore.noiseFileType));
				nFiles++;
			}
		}
		catch (IOException e) {
			System.out.println("DIFAR: backup failed, so nothing was upgraded: " + e);
			return false;
		}
		System.out.printf("DIFAR: %d files backed up to %s before upgrading\n", nFiles, backup.getBackupRoot());
		if (connection == null) {
			System.out.println("DIFAR: no database, so only the binary files will be upgraded");
			return true;
		}
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		File database = dbControl == null || dbControl.getDatabaseSystem() == null ? null
				: new File(dbControl.getDatabaseSystem().getDatabaseName());
		if (database == null || !database.isFile()) {
			System.out.println("DIFAR: the database is not a single file, so it cannot be backed up, and nothing was upgraded");
			return false;
		}
		try {
			// so the file on disk holds everything written so far
			dbControl.commitChanges();
			backup.copyAlongside(database);
		}
		catch (IOException e) {
			System.out.println("DIFAR: database backup failed, so nothing was upgraded: " + e);
			return false;
		}
		System.out.printf("DIFAR: database %s backed up to %s\n", database.getName(), backup.getBackupRoot());
		return true;
	}

	/**
	 * Delete every DIFAR clip row. Older tables can be incomplete, or hold
	 * duplicate rows or rows for clips from other files, so the table is
	 * rebuilt from the binary files as they load.
	 */
	private void deleteClipRows() {
		SQLLogging logging = getDataBlock().getLogging();
		if (connection == null || logging == null) {
			return;
		}
		String table = connection.getSqlTypes().formatTableName(logging.getTableDefinition().getTableName());
		try (Statement statement = connection.getConnection().createStatement()) {
			rowsDeleted = statement.executeUpdate("DELETE FROM " + table);
		}
		catch (SQLException e) {
			System.out.println("DIFAR: could not delete the old clip rows: " + e.getMessage());
		}
	}

	@Override
	public void newDataLoad(long startTime, long endTime, OfflineDataMapPoint mapPoint) {
		if (!backedUp) {
			return;
		}
		List<DifarDataUnit> clips = dropSecondCopies();
		if (renumber) {
			clips.sort(Comparator.comparingLong(DifarDataUnit::getTimeMilliseconds)
					.thenComparingInt(DifarDataUnit::getChannelBitmap));
			for (DifarDataUnit clip : clips) {
				clip.setUID(nextUID++);
			}
		}
		rematch.startLoad();
		writeClipRows(clips);
		// through this task, so the clips are upgraded as well as rematched
		rematch.addStoredLate(RematchTask.processClipsStoredLate(this, startTime));
	}

	/**
	 * Drop a clip loaded in an earlier file, with the same UID, channel and
	 * start time. The data block already skips a second copy within one file.
	 * Either way the copy is noted, so it is left out when the file is
	 * rewritten.
	 * @return the clips that remain.
	 */
	private List<DifarDataUnit> dropSecondCopies() {
		DifarDataBlock block = getDifarDataBlock();
		List<DifarDataUnit> kept = new ArrayList<>();
		for (DifarDataUnit clip : block.getDataCopy()) {
			if (seen.add(DifarDataBlock.clipKey(clip.getUID(), clip.getChannelBitmap(), clip.getTimeMilliseconds()))) {
				kept.add(clip);
				continue;
			}
			System.out.printf("DIFAR: dropped a second copy of clip UID %d, found in another file\n", clip.getUID());
			block.noteSecondCopy(clip.getUID(), clip.getChannelBitmap(), clip.getTimeMilliseconds());
			block.remove(clip);
		}
		return kept;
	}

	/**
	 * Write a row for each clip, now that it has its final UID. The rematch
	 * then updates the buoy columns of these rows.
	 * @param clips the clips just loaded.
	 */
	private void writeClipRows(List<DifarDataUnit> clips) {
		SQLLogging logging = getDataBlock().getLogging();
		if (connection == null || logging == null) {
			return;
		}
		for (DifarDataUnit clip : clips) {
			if (logging.logData(connection, clip)) {
				rowsWritten++;
			}
		}
	}

	private DifarDataBlock getDifarDataBlock() {
		return (DifarDataBlock) getDataBlock();
	}

	/**
	 * @return true for a clip from an older file, so its file is rewritten at
	 * the current version.
	 */
	@Override
	public boolean processDataUnit(DifarDataUnit clip) {
		if (!backedUp) {
			return false;
		}
		rematch.processDataUnit(clip);
		if (clip.getBinaryVersion() >= DifarClipPayload.CURRENT_VERSION && !renumber) {
			return false;
		}
		clip.setBinaryVersion(DifarClipPayload.CURRENT_VERSION);
		upgraded++;
		return true;
	}

	@Override
	public void loadedDataComplete() {
		if (backedUp) {
			rematch.loadedDataComplete();
		}
	}

	@Override
	public void completeTask() {
		if (!backedUp) {
			return;
		}
		backedUp = false;
		int dropped = getDifarDataBlock().stopCollectingSecondCopies();
		System.out.printf("DIFAR: upgrade rewrote %d clips at version %d%s; dropped %d second copies; "
				+ "replaced %d clip rows with %d\n",
				upgraded, DifarClipPayload.CURRENT_VERSION,
				renumber ? String.format(", with new UIDs 1 to %d", nextUID - 1) : "",
				dropped, rowsDeleted, rowsWritten);
		if (renumber) {
			// new clips saved in the viewer follow the new UIDs
			getDataBlock().getUidHandler().setCurrentUID(Math.max(getDataBlock().getUidHandler().getCurrentUID(), nextUID - 1));
		}
		rematch.completeTask();
	}
}
