package difar.offline;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import javax.swing.JOptionPane;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.uid.DataBlockUIDHandler;
import PamguardMVC.uid.DatabaseUIDFunctions;
import binaryFileStorage.BinaryDataSink;
import binaryFileStorage.BinaryFooter;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.BinaryOfflineDataMap;
import binaryFileStorage.BinaryOfflineDataMapPoint;
import binaryFileStorage.BinaryStore;
import binaryFileStorage.DataUnitFileInformation;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;
import difar.DifarControl;
import difar.DifarDataBlock;
import difar.DifarDataUnit;
import difar.offline.TimeClosure.FileSpan;
import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import generalDatabase.SQLLogging;

/**
 * Keeps DIFAR clips saved or deleted in the viewer, and writes them to the
 * binary files and the database when the viewer saves its data.
 * <p>
 * Saving or deleting a clip only records it here. Writing waits for
 * {@link #compact()}, which the data block calls whenever the viewer saves:
 * before loading new data, on File, Save, and on exit. Compaction replaces
 * every binary file overlapping a changed time slot with one file per slot,
 * so files written in the viewer never overlap. The database rows are added
 * and removed at the same time, so the two stay in step. If PAMGuard stops
 * before a save, the changes since the last one are lost from both.
 * <p>
 * Clips whose bearings or crossings change are not handled here. The core
 * viewer save already rewrites, in place, any file holding a changed clip.
 * Compaction points every clip it writes at its new file and position, so
 * that later core saves find it.
 */
public class ViewerClipStore {

	/** A file claiming to span more than this is treated as a bad footer. */
	private static final long MAX_FILE_SPAN_MILLIS = 24L * 3600L * 1000L;

	/** Added to the name of a file being replaced, until its replacement is in place. */
	private static final String OLD_SUFFIX = ".old";

	private enum Reshape { ASK, BACKUP, NO_BACKUP }

	private final DifarDataBlock dataBlock;
	private final DifarControl difarControl;

	/**
	 * Clips saved since the last compaction, by UID. Held here as well as in
	 * the data block, so a clip survives new data being loaded if a save
	 * fails, and is written by the next one.
	 */
	private final Map<Long, DifarDataUnit> saved = new LinkedHashMap<>();

	/** UID and start time of written clips deleted since the last compaction. */
	private final Map<Long, Long> deleted = new HashMap<>();

	private boolean uidsSeeded;
	private Reshape reshape = Reshape.ASK;
	private ViewerBackup backup;

	public ViewerClipStore(DifarDataBlock dataBlock, DifarControl difarControl) {
		this.dataBlock = dataBlock;
		this.difarControl = difarControl;
	}

	/**
	 * Call before a clip joins the saved data, so its UID follows every DIFAR
	 * UID already in use.
	 */
	public synchronized void prepare() {
		if (!uidsSeeded) {
			seedUIDs();
			uidsSeeded = true;
		}
	}

	/**
	 * Record a clip just saved, once it has its UID.
	 * @param unit the clip.
	 */
	public synchronized void clipSaved(DifarDataUnit unit) {
		saved.put(unit.getUID(), unit);
	}

	/**
	 * Record a saved clip deleted. A clip saved since the last compaction was
	 * never written, so it is simply forgotten.
	 * @param unit the clip.
	 */
	public synchronized void clipDeleted(DifarDataUnit unit) {
		if (saved.remove(unit.getUID()) != null) {
			return;
		}
		deleted.put(unit.getUID(), unit.getTimeMilliseconds());
	}

	/**
	 * @return true if clips have been saved or deleted since the last compaction.
	 */
	public synchronized boolean hasChanges() {
		return !saved.isEmpty() || !deleted.isEmpty();
	}

	/**
	 * Write the clips saved and deleted since the last compaction to the
	 * binary files and the database.
	 * @return true if there was nothing to do or everything was written. On
	 * false, nothing on disk has changed, apart from a backup, and the
	 * changes are kept for the next try, even if new data are loaded.
	 */
	public synchronized boolean compact() {
		if (!hasChanges()) {
			return true;
		}
		BinaryStore binaryStore = BinaryStore.findBinaryStoreControl();
		if (binaryStore == null || dataBlock.getBinaryDataSource() == null) {
			return report("no binary store, so clips saved in the viewer cannot be written");
		}
		BinaryOfflineDataMap dataMap = (BinaryOfflineDataMap) dataBlock.getOfflineDataMap(binaryStore);
		if (dataMap == null) {
			return report("no binary data map for DIFAR");
		}
		TimeGrid grid = new TimeGrid(Math.max(1, binaryStore.getBinaryStoreSettings().fileSeconds) * 1000L);

		CompactionPlan<BinaryOfflineDataMapPoint, DifarDataUnit> plan;
		try {
			plan = CompactionPlan.plan(grid, changedTimes(), fileSpans(dataMap),
					mapPoint -> readClips(binaryStore, mapPoint), memory(), deleted.keySet(),
					DifarDataUnit::getUID, DifarDataUnit::getTimeMilliseconds);
		}
		catch (RuntimeException e) {
			e.printStackTrace();
			return report("could not plan the save: " + e.getMessage());
		}
		for (DifarDataUnit clip : plan.getRelocated()) {
			System.out.printf("DIFAR: clip UID %d at %s lay outside its file's times; it moves to the file for its time\n",
					clip.getUID(), PamCalendar.formatDBDateTime(clip.getTimeMilliseconds()));
		}

		if (!backUpOffGridFiles(binaryStore, grid, plan.getFiles())) {
			return false;
		}

		List<File> oldFiles = new ArrayList<>();
		for (BinaryOfflineDataMapPoint mapPoint : plan.getFiles()) {
			File data = mapPoint.getBinaryFile(binaryStore);
			oldFiles.add(data);
			oldFiles.add(binaryStore.swapFileType(data, BinaryStore.indexFileType));
			oldFiles.add(binaryStore.swapFileType(data, BinaryStore.noiseFileType));
		}
		SlotFileWriter writer = new SlotFileWriter(binaryStore, dataBlock);
		for (Long slot : plan.getClipsBySlot().keySet()) {
			File target = writer.dataFileFor(slot);
			if (target.exists() && !contains(oldFiles, target)) {
				return report("a file not being replaced already has the name " + target);
			}
		}

		List<SlotFileWriter.Written> written = new ArrayList<>();
		for (Map.Entry<Long, List<DifarDataUnit>> entry : plan.getClipsBySlot().entrySet()) {
			SlotFileWriter.Written w = writer.write(grid, entry.getKey(), entry.getValue());
			if (w == null) {
				written.forEach(SlotFileWriter.Written::deleteTemps);
				return report("could not write the file for " + PamCalendar.formatDBDateTime(entry.getKey()));
			}
			written.add(w);
		}

		if (!swapFiles(oldFiles, written)) {
			return false;
		}

		for (BinaryOfflineDataMapPoint mapPoint : plan.getFiles()) {
			dataMap.removeMapPoint(mapPoint.getBinaryFile(binaryStore));
		}
		for (SlotFileWriter.Written w : written) {
			dataMap.addDataPoint(new BinaryOfflineDataMapPoint(binaryStore, w.dataFile, w.header, w.footer,
					w.moduleHeader, w.moduleFooter, null));
			for (int i = 0; i < w.clips.size(); i++) {
				w.clips.get(i).setDataUnitFileInformation(new DataUnitFileInformation(binaryStore, w.dataFile, i));
			}
		}
		dataMap.sortMapPoints();

		updateDatabase(written);

		System.out.printf("DIFAR: saved %d new and %d deleted clips, replacing %d files with %d\n",
				saved.size(), deleted.size(), plan.getFiles().size(), written.size());
		saved.clear();
		deleted.clear();
		return true;
	}

	private List<Long> changedTimes() {
		List<Long> times = new ArrayList<>(deleted.values());
		for (DifarDataUnit unit : saved.values()) {
			times.add(unit.getTimeMilliseconds());
		}
		return times;
	}

	private List<FileSpan<BinaryOfflineDataMapPoint>> fileSpans(BinaryOfflineDataMap dataMap) {
		List<FileSpan<BinaryOfflineDataMapPoint>> spans = new ArrayList<>();
		Iterator<BinaryOfflineDataMapPoint> it = dataMap.getListIterator();
		while (it.hasNext()) {
			BinaryOfflineDataMapPoint mapPoint = it.next();
			long start = mapPoint.getStartTime();
			long end = mapPoint.getEndTime();
			if (end - start > MAX_FILE_SPAN_MILLIS) {
				System.out.printf("DIFAR: %s claims to end at %s; treating it as a day long\n",
						mapPoint.getName(), PamCalendar.formatDBDateTime(end));
				end = start + MAX_FILE_SPAN_MILLIS;
			}
			spans.add(new FileSpan<>(mapPoint, start, end));
		}
		return spans;
	}

	/**
	 * The clips held in memory: those saved since the last compaction, and
	 * those in the data block, which win where both have a UID.
	 */
	private List<DifarDataUnit> memory() {
		Map<Long, DifarDataUnit> byUID = new LinkedHashMap<>(saved);
		List<DifarDataUnit> inBlock;
		synchronized (dataBlock.getSynchLock()) {
			inBlock = dataBlock.getDataCopy();
		}
		for (DifarDataUnit unit : inBlock) {
			byUID.put(unit.getUID(), unit);
		}
		return new ArrayList<>(byUID.values());
	}

	/**
	 * Read every clip in one file into a list, leaving the data block alone.
	 */
	private List<DifarDataUnit> readClips(BinaryStore binaryStore, BinaryOfflineDataMapPoint mapPoint) {
		List<DifarDataUnit> clips = new ArrayList<>();
		BinaryDataSink sink = new BinaryDataSink() {
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
			public boolean newDataUnit(BinaryObjectData binaryObjectData, PamDataBlock block, PamDataUnit dataUnit) {
				clips.add((DifarDataUnit) dataUnit);
				return true;
			}

			@Override
			public void newDatagram(BinaryObjectData binaryObjectData) {
			}
		};
		if (!binaryStore.loadData(dataBlock, mapPoint, Long.MIN_VALUE, Long.MAX_VALUE, sink)) {
			throw new IllegalStateException("could not read " + mapPoint.getBinaryFile(binaryStore));
		}
		return clips;
	}

	/**
	 * Files not on the grid were written in normal mode, or by an earlier
	 * version of the viewer. Replacing them changes their shape, so ask once
	 * a session whether to copy them first.
	 * @return false if the user cancelled or a copy failed.
	 */
	private boolean backUpOffGridFiles(BinaryStore binaryStore, TimeGrid grid,
			Collection<BinaryOfflineDataMapPoint> files) {
		List<File> offGrid = new ArrayList<>();
		for (BinaryOfflineDataMapPoint mapPoint : files) {
			long start = mapPoint.getStartTime();
			if (start != grid.slotStart(start) || mapPoint.getEndTime() != start + grid.getWidthMillis()) {
				offGrid.add(mapPoint.getBinaryFile(binaryStore));
			}
		}
		if (offGrid.isEmpty()) {
			return true;
		}
		if (reshape == Reshape.ASK) {
			reshape = askReshape(offGrid.size(), grid);
			if (reshape == Reshape.ASK) {
				return report("save cancelled; the clips are kept until the next save");
			}
		}
		if (reshape == Reshape.NO_BACKUP) {
			return true;
		}
		if (backup == null) {
			backup = new ViewerBackup(new File(binaryStore.getBinaryStoreSettings().getStoreLocation()));
		}
		try {
			for (File data : offGrid) {
				backup.copy(data);
				backup.copy(binaryStore.swapFileType(data, BinaryStore.indexFileType));
				backup.copy(binaryStore.swapFileType(data, BinaryStore.noiseFileType));
			}
		}
		catch (IOException e) {
			return report("backup failed, so nothing was saved: " + e);
		}
		System.out.printf("DIFAR: %d original files backed up to %s\n", offGrid.size(), backup.getBackupRoot());
		return true;
	}

	private Reshape askReshape(int nFiles, TimeGrid grid) {
		if (GraphicsEnvironment.isHeadless()) {
			return Reshape.BACKUP;
		}
		String message = String.format("<html>Saving DIFAR clips will rewrite %d existing DIFAR binary %s<br>"
				+ "into files of %d minutes each. The clips they hold are kept.<br><br>"
				+ "Copy the original files to a backup folder first?</html>",
				nFiles, nFiles == 1 ? "file" : "files", grid.getWidthMillis() / 60000L);
		String[] options = { "Back up and save", "Save without backup", "Cancel" };
		int answer = JOptionPane.showOptionDialog(difarControl.getGuiFrame(), message, "DIFAR viewer save",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		switch (answer) {
		case 0:
			return Reshape.BACKUP;
		case 1:
			return Reshape.NO_BACKUP;
		default:
			return Reshape.ASK;
		}
	}

	/**
	 * Put the new files in place of the old ones. The old files are renamed
	 * aside first and only deleted once every new file is in place, so a
	 * failure part way leaves every clip on disk under one name or another.
	 */
	private boolean swapFiles(List<File> oldFiles, List<SlotFileWriter.Written> written) {
		List<File> asideDone = new ArrayList<>();
		for (File old : oldFiles) {
			if (!old.exists()) {
				continue;
			}
			File aside = new File(old.getPath() + OLD_SUFFIX);
			if (!old.renameTo(aside)) {
				for (File a : asideDone) {
					a.renameTo(new File(a.getPath().substring(0, a.getPath().length() - OLD_SUFFIX.length())));
				}
				written.forEach(SlotFileWriter.Written::deleteTemps);
				return report("could not move " + old + " aside, so nothing was saved");
			}
			asideDone.add(aside);
		}
		for (SlotFileWriter.Written w : written) {
			if (!w.tempDataFile.renameTo(w.dataFile) || !w.tempIndexFile.renameTo(w.indexFile)) {
				System.out.println("DIFAR: SAVE FAILED PART WAY. Nothing is lost, but files need putting back by hand.");
				System.out.println("DIFAR: originals end in " + OLD_SUFFIX + ", new files may end in "
						+ SlotFileWriter.TEMP_SUFFIX + ". First failure: " + w.tempDataFile);
				return false;
			}
		}
		for (File a : asideDone) {
			if (!a.delete()) {
				System.out.println("DIFAR: could not delete " + a + "; it can be deleted by hand");
			}
		}
		return true;
	}

	/**
	 * Log each clip saved since the last compaction and remove the rows of
	 * each deleted one, matched by UID.
	 */
	private void updateDatabase(List<SlotFileWriter.Written> written) {
		SQLLogging logging = dataBlock.getLogging();
		PamConnection connection = DBControlUnit.findConnection();
		if (logging == null || connection == null) {
			if (!saved.isEmpty() || !deleted.isEmpty()) {
				System.out.println("DIFAR: no database, so saved and deleted clips are in the binary files only");
			}
			return;
		}
		for (SlotFileWriter.Written w : written) {
			for (DifarDataUnit clip : w.clips) {
				if (saved.containsKey(clip.getUID())) {
					logging.logData(connection, clip);
				}
			}
		}
		if (!deleted.isEmpty()) {
			StringJoiner uids = new StringJoiner(",", "(", ")");
			deleted.keySet().forEach(uid -> uids.add(uid.toString()));
			String sql = String.format("DELETE FROM %s WHERE UID IN %s",
					logging.getTableDefinition().getTableName(), uids);
			try (Statement statement = connection.getConnection().createStatement()) {
				statement.execute(sql);
			}
			catch (SQLException e) {
				System.out.println("DIFAR: could not delete database rows: " + sql + ": " + e.getMessage());
			}
		}
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl != null) {
			dbControl.commitChanges();
		}
	}

	/**
	 * Start saved clips' UIDs above every DIFAR UID already in use, in the
	 * binary files and in the database, as normal mode does at startup.
	 * In the viewer nothing else sets this counter.
	 */
	private void seedUIDs() {
		DataBlockUIDHandler uidHandler = dataBlock.getUidHandler();
		long highest = uidHandler.getCurrentUID();
		BinaryStore binaryStore = BinaryStore.findBinaryStoreControl();
		if (binaryStore != null) {
			BinaryOfflineDataMap dataMap = (BinaryOfflineDataMap) dataBlock.getOfflineDataMap(binaryStore);
			if (dataMap != null) {
				Iterator<BinaryOfflineDataMapPoint> it = dataMap.getListIterator();
				while (it.hasNext()) {
					Long fileHighest = it.next().getHighestUID();
					if (fileHighest != null) {
						highest = Math.max(highest, fileHighest);
					}
				}
			}
		}
		if (dataBlock.getLogging() != null) {
			long dbHighest = new DatabaseUIDFunctions(PamController.getInstance()).findMaxUIDforDataBlock(dataBlock);
			highest = Math.max(highest, dbHighest);
		}
		uidHandler.setCurrentUID(highest);
	}

	private static boolean contains(List<File> files, File file) {
		File target = file.getAbsoluteFile();
		for (File f : files) {
			if (f.getAbsoluteFile().equals(target)) {
				return true;
			}
		}
		return false;
	}

	private static boolean report(String message) {
		System.out.println("DIFAR: " + message);
		return false;
	}
}
