package difar.offline;

import java.io.File;
import java.util.Iterator;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.uid.DataBlockUIDHandler;
import PamguardMVC.uid.DatabaseUIDFunctions;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryFooter;
import binaryFileStorage.BinaryOfflineDataMap;
import binaryFileStorage.BinaryOfflineDataMapPoint;
import binaryFileStorage.BinaryOutputStream;
import binaryFileStorage.BinaryStore;
import difar.DifarDataBlock;
import difar.DifarDataUnit;

/**
 * Writes DIFAR clips saved in the viewer to binary files of their own.
 * <p>
 * The viewer normally only rewrites binary files that already exist. New
 * clips are written here the way normal mode writes them, with the same
 * calls in the same order: open a file, write the header and module header,
 * add each clip, then write the module footer and footer and close.
 * <p>
 * One file holds the clips saved while one stretch of data is loaded. Its
 * header and footer times are the start and end of that stretch, which
 * contain every clip in it, since a clip can only be cut from loaded audio.
 * The file is closed whenever DIFAR's data are saved, which the viewer does
 * before loading any new stretch, on File, Save, and on exit. So a file is
 * never read while it is still being written.
 */
public class ViewerClipWriter {

	/** How many seconds to step a file name forward before giving up. */
	private static final int MAX_NAME_STEPS = 3600;

	private final DifarDataBlock dataBlock;

	private BinaryOutputStream stream;

	private long fileStart, fileEnd;

	public ViewerClipWriter(DifarDataBlock dataBlock) {
		this.dataBlock = dataBlock;
	}

	/**
	 * Make sure a file is open whose times contain a clip. Call this before
	 * the clip joins the data block, so the file is opened before the clip is
	 * given its UID, as in normal mode.
	 * <p>
	 * A file spans the saved clips' loaded data. Each display loads its own
	 * stretch of data, so a clip can come from outside that span. Then the
	 * open file is finished, and a new one spans both the loaded data and the
	 * clip, so every file's times always contain all its clips.
	 * @param unit the clip about to be saved.
	 * @return true if a file is open.
	 */
	public synchronized boolean prepare(DifarDataUnit unit) {
		long clipStart = unit.getTimeMilliseconds();
		long clipEnd = unit.getEndTimeInMilliseconds();
		if (stream != null) {
			if (clipStart >= fileStart && clipEnd <= fileEnd) {
				return true;
			}
			close();
		}
		BinaryStore binaryStore = BinaryStore.findBinaryStoreControl();
		BinaryDataSource source = dataBlock.getBinaryDataSource();
		if (binaryStore == null || source == null) {
			System.out.println("DIFAR: no binary store, so clips saved in the viewer cannot be written");
			return false;
		}
		fileStart = Math.min(dataBlock.getCurrentViewDataStart(), clipStart);
		fileEnd = Math.max(dataBlock.getCurrentViewDataEnd(), clipEnd);
		Long nameTime = ViewerFileTimes.firstFreeTime(fileStart,
				t -> new File(fileName(binaryStore, source, t)).exists(), MAX_NAME_STEPS);
		if (nameTime == null) {
			System.out.println("DIFAR: no free binary file name near " + PamCalendar.formatDBDateTime(fileStart));
			return false;
		}
		seedUIDs(binaryStore);
		BinaryOutputStream newStream = new BinaryOutputStream(binaryStore, dataBlock);
		if (!newStream.openOutputFiles(nameTime)) {
			return false;
		}
		long now = System.currentTimeMillis();
		newStream.writeHeader(fileStart, now);
		newStream.writeModuleHeader();
		/*
		 * The file joined the binary map when it opened, before its header had
		 * a data time, so give the map its start time now.
		 */
		BinaryOfflineDataMap dataMap = (BinaryOfflineDataMap) dataBlock.getOfflineDataMap(binaryStore);
		if (dataMap != null) {
			BinaryOfflineDataMapPoint mapPoint = dataMap.findMapPoint(new File(newStream.getMainFileName()));
			if (mapPoint != null) {
				mapPoint.setStartTime(fileStart);
			}
		}
		stream = newStream;
		return true;
	}

	/**
	 * Write a saved clip to the open file, opening one if needed.
	 * @param unit the clip, already in the data block.
	 * @return true if it was written.
	 */
	public synchronized boolean write(DifarDataUnit unit) {
		if (!prepare(unit)) {
			return false;
		}
		BinaryDataSource source = dataBlock.getBinaryDataSource();
		source.setBinaryStorageStream(stream);
		try {
			return source.saveData(unit);
		}
		catch (RuntimeException e) {
			// report it and let the save finish, rather than leave the
			// DIFAR displays half way through a save
			System.out.println("DIFAR: could not write clip to binary file: " + e);
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Finish and close the open file, if there is one.
	 */
	public synchronized void close() {
		if (stream == null) {
			return;
		}
		stream.writeModuleFooter();
		stream.writeFooter(fileEnd, System.currentTimeMillis(), BinaryFooter.END_RUNSTOPPED);
		stream.closeFile();
		stream.createIndexFile();
		stream = null;
	}

	/**
	 * Start the saved clips' UIDs above every DIFAR UID already in use, in the
	 * binary files and in the database. In the viewer nothing else sets this
	 * counter, so without it each session would hand out the same UIDs again.
	 * Normal mode does the same at startup, from the same two places.
	 */
	private void seedUIDs(BinaryStore binaryStore) {
		DataBlockUIDHandler uidHandler = dataBlock.getUidHandler();
		long highest = uidHandler.getCurrentUID();
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
		if (dataBlock.getLogging() != null) {
			long dbHighest = new DatabaseUIDFunctions(PamController.getInstance()).findMaxUIDforDataBlock(dataBlock);
			highest = Math.max(highest, dbHighest);
		}
		uidHandler.setCurrentUID(highest);
	}

	/**
	 * @return true if a file is open.
	 */
	public synchronized boolean isOpen() {
		return stream != null;
	}

	/**
	 * The name a binary file for this data would get at a given time, built
	 * the same way as in BinaryOutputStream.
	 */
	private static String fileName(BinaryStore binaryStore, BinaryDataSource source, long timeMillis) {
		String folder = binaryStore.getFolderName(timeMillis, true);
		String name = PamCalendar.createFileName(timeMillis, source.createFilenamePrefix(), BinaryStore.fileType);
		return folder + name.replaceAll(" ", "_");
	}
}
