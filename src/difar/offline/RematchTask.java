package difar.offline;

import java.awt.Component;
import java.awt.Point;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import Array.ArrayManager;
import Array.StreamerDataBlock;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import binaryFileStorage.DataUnitFileInformation;
import dataMap.OfflineDataMapPoint;
import difar.dataSelector.CrossingSelectParams;
import difar.DIFARCrossingInfo;
import difar.DifarControl;
import difar.DifarDataUnit;
import difar.DifarMatchSelector;
import difar.DifarProcess;
import difar.DifarSqlLogging;
import difar.crossings.CrossingLocaliser;
import difar.crossings.CrossingRecorder;
import difar.crossings.DifarCrossing;
import difar.crossings.DifarCrossingDataBlock;
import difar.targetmotion.Simplex2D;
import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import generalDatabase.SQLLogging;
import offlineProcessing.OfflineTask;
import offlineProcessing.TaskGroupParams;
import pamScrollSystem.AbstractScrollManager;

/**
 * Matches clips again over a stretch of data in the viewer, and records the
 * crossings they make.
 * <p>
 * Each clip in turn, in time order, is matched as it would be on the queue,
 * unless it was already matched in this run as an earlier clip's partner:
 * the best viable match is chosen automatically, and the crossing is recorded
 * by the same rules as saving a clip, including a new crossing claiming clips
 * from older ones. A clip's old crossing is removed before it is matched.
 * <p>
 * The result depends on the order. A clip takes its best match among the
 * clips still free, and a clip matched earlier in the run is not free, so
 * the first match stands. Of two clips of the same call on one buoy, the
 * earlier is matched. Matching all clips together would avoid the order
 * dependence, and is noted in viewer_editing_design.md.
 * <p>
 * Crossings chosen by the operator can be kept. Their clips are then left out
 * of the rematch, as clips to match and as partners, and each kept crossing
 * is worked out again from its clips, since its buoys may have changed.
 * <p>
 * Each clip's database row also gets its buoy's current position, heading
 * and true bearing. After a buoy edit this runs over the buoy's period, for
 * the clips the edit affects.
 * <p>
 * Clips are loaded one file at a time, so a clip near a file boundary cannot
 * see partners in the next file. No clip is marked as changed, so no binary
 * file is rewritten.
 */
public class RematchTask extends OfflineTask<DifarDataUnit> {

	private final DifarControl difarControl;
	private final DifarProcess difarProcess;
	private final DifarCrossingDataBlock crossingBlock;
	private final CrossingRecorder recorder;
	private final CrossingLocaliser localiser;
	private final Predicate<DifarDataUnit> buoyChanged;

	private PamConnection connection;

	/** Whether operator choices are kept, for the run in progress. */
	private boolean keepOperatorChoices;

	/** Crossings made in this run, so their clips are not matched twice. */
	private final Set<DifarCrossing> made = Collections.newSetFromMap(new IdentityHashMap<>());
	/** Kept crossings met in the current load, to be worked out again. */
	private final Set<DifarCrossing> pending = new LinkedHashSet<>();
	private final Set<Long> relocated = new LinkedHashSet<>();
	private final Set<Long> notLoaded = new LinkedHashSet<>();
	private final Set<Long> removed = new LinkedHashSet<>();
	private final Set<Long> kept = new LinkedHashSet<>();
	/** Clips processed, and those that belong to a kept crossing. */
	private int processed, keptClips, noBuoy, clipRows;

	/** Clips starting outside their file's time span, which core skips. */
	private int outsideFile;

	/** The residuals of the crossings made in this run, for a CSV file. */
	private TriangulationResidualLog residualLog = new TriangulationResidualLog();

	/** End of the file just loaded. */
	private long fileEnd;

	/**
	 * Clips with no buoy in force, by channel: how many, and the first and
	 * last of their start times. A wrong deploy or end time shows up as a run
	 * of these on one channel.
	 */
	private final Map<Integer, long[]> noBuoyByChannel = new TreeMap<>();

	/**
	 * True from prepareTask to completeTask. Core completes every task in a
	 * group, even those not ticked, which it never prepared.
	 */
	private boolean running;

	/**
	 * @param difarControl the DIFAR module.
	 * @param buoyChanged the clips whose buoy has changed, whose database rows
	 * need its new values; null to refresh every clip's row.
	 */
	public RematchTask(DifarControl difarControl, Predicate<DifarDataUnit> buoyChanged) {
		super(difarControl.getDifarProcess().getProcessedDifarData());
		this.difarControl = difarControl;
		this.difarProcess = difarControl.getDifarProcess();
		this.crossingBlock = difarProcess.getCrossingDataBlock();
		this.recorder = difarProcess.getCrossingRecorder();
		this.localiser = new CrossingLocaliser(difarProcess, difarControl);
		this.buoyChanged = buoyChanged;
		addRequiredDataBlock(crossingBlock);
		addAffectedDataBlock(crossingBlock);
		requireBuoyRecords(this);
	}

	/**
	 * Have core load the buoy records with each chunk of clips, as the Viewer
	 * does for a view. Without this a task sees only the buoys loaded for the
	 * current view, and clips outside it have no buoy position. Core also
	 * loads records from before each chunk, for buoys deployed earlier.
	 * @param task a task that matches or locates clips.
	 */
	static void requireBuoyRecords(OfflineTask<?> task) {
		StreamerDataBlock buoyRecords = ArrayManager.getArrayManager().getStreamerDatabBlock();
		if (buoyRecords != null) {
			task.addRequiredDataBlock(buoyRecords);
		}
	}

	@Override
	public String getName() {
		return "Rematch clips in " + getDataBlock().getDataName();
	}

	@Override
	public boolean hasSettings() {
		return true;
	}

	@Override
	public boolean callSettings(Component component, Point point) {
		JCheckBox replace = new JCheckBox("Replace crossings chosen by the operator",
				difarControl.getDifarParameters().rematchReplacesOperatorChoices);
		int answer = JOptionPane.showConfirmDialog(component, replace, getName(),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (answer != JOptionPane.OK_OPTION) {
			return false;
		}
		difarControl.getDifarParameters().rematchReplacesOperatorChoices = replace.isSelected();
		return true;
	}

	@Override
	public void prepareTask() {
		running = true;
		keepOperatorChoices = !difarControl.getDifarParameters().rematchReplacesOperatorChoices;
		made.clear();
		pending.clear();
		relocated.clear();
		notLoaded.clear();
		clipRows = 0;
		removed.clear();
		kept.clear();
		processed = 0;
		keptClips = 0;
		noBuoy = 0;
		outsideFile = 0;
		Simplex2D.takeFailedErrorEstimates();
		noBuoyByChannel.clear();
		// start counting correlations afresh for this run's summary
		difarProcess.getClipDelays().takeSummary();
		residualLog = new TriangulationResidualLog();
	}

	@Override
	public void newDataLoad(long startTime, long endTime, OfflineDataMapPoint mapPoint) {
		startLoad(endTime);
		int before = processClipsBeforeFile(this, startTime);
		outsideFile += before;
		if (before > 0 && noClipsInFile(this, startTime, endTime)) {
			// core will stop before finishing this load; finish it here
			loadedDataComplete();
			saveAffectedBlocks(this);
		}
	}

	/**
	 * @param task a task on clips.
	 * @param fileStart start of the loaded file.
	 * @param fileEnd end of the loaded file.
	 * @return true if no loaded clip starts within the file's time span.
	 * Core then processes nothing, and returns before calling
	 * loadedDataComplete and before saving, so any clip processed outside
	 * core's loop would never reach its file.
	 */
	static boolean noClipsInFile(OfflineTask<DifarDataUnit> task, long fileStart, long fileEnd) {
		for (DifarDataUnit clip : task.getDataBlock().getDataCopy()) {
			long time = clip.getTimeMilliseconds();
			if (time >= fileStart && time <= fileEnd) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Save a task's affected data blocks and commit the database, as core
	 * does at the end of each load it processes.
	 * @param task the task.
	 */
	static void saveAffectedBlocks(OfflineTask<?> task) {
		for (int i = 0; i < task.getNumAffectedDataBlocks(); i++) {
			task.getAffectedDataBlock(i).saveViewerData();
		}
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl != null) {
			dbControl.commitChanges();
		}
	}

	/**
	 * Get ready for a newly loaded chunk of clips. Used by the upgrade too,
	 * which runs this task's steps.
	 */
	void startLoad(long fileEnd) {
		this.fileEnd = fileEnd;
		connection = DBControlUnit.findConnection();
		pending.clear();
		// core has just loaded this chunk's buoy records
		difarControl.buoyRecordsReloaded();
	}

	/**
	 * Process the loaded clips that start before their file does.
	 * <p>
	 * A clip is stored when it is saved, not when the call starts. A call
	 * marked just before a file rolls over and saved just after sits in the
	 * next file, with a start time before that file begins. Core processes
	 * only units that start within their file's time span, so it skips these.
	 * They are processed here, before core processes the rest, so clips are
	 * still taken in time order. A clip the task changes is marked for
	 * rewriting, as core does.
	 * @param task the task to run on each clip.
	 * @param fileStart start of the loaded file.
	 * @return how many clips were processed.
	 */
	static int processClipsBeforeFile(OfflineTask<DifarDataUnit> task, long fileStart) {
		return processClips(task, time -> time < fileStart);
	}

	/**
	 * Process the loaded clips that start after their file's end time. Core
	 * stops at the first of these, so they are never processed. Files cut
	 * short by a crash can have an end time before their last clip. Call
	 * from loadedDataComplete, which runs before the files are saved.
	 * @param task the task to run on each clip.
	 * @param fileEnd end of the loaded file.
	 * @return how many clips were processed.
	 */
	static int processClipsAfterFile(OfflineTask<DifarDataUnit> task, long fileEnd) {
		return processClips(task, time -> time > fileEnd);
	}

	/**
	 * Process the loaded clips whose start time passes a test, marking each
	 * clip the task changes for rewriting, as core does. When the task runs
	 * over a chosen period, clips outside that period are left alone.
	 * @param task the task to run on each clip.
	 * @param outside the test on a clip's start time.
	 * @return how many clips were processed.
	 */
	private static int processClips(OfflineTask<DifarDataUnit> task, LongPredicate outside) {
		long notBefore = Long.MIN_VALUE, notAfter = Long.MAX_VALUE;
		TaskGroupParams params = task.getOfflineTaskGroup().getTaskGroupParams();
		if (params.dataChoice == TaskGroupParams.PROCESS_SPECIFICPERIOD) {
			notBefore = params.startRedoDataTime;
			notAfter = params.endRedoDataTime;
		}
		int n = 0;
		for (DifarDataUnit clip : task.getDataBlock().getDataCopy()) {
			long time = clip.getTimeMilliseconds();
			if (!outside.test(time) || time < notBefore || time > notAfter) {
				continue;
			}
			n++;
			if (task.processDataUnit(clip)) {
				DataUnitFileInformation fileInfo = clip.getDataUnitFileInformation();
				if (fileInfo != null) {
					fileInfo.setNeedsUpdate(true);
				}
				clip.updateDataUnit(System.currentTimeMillis());
			}
		}
		return n;
	}

	/**
	 * Add clips processed outside core's loop, when the upgrade runs this
	 * task's steps.
	 * @param n how many.
	 */
	void addOutsideFile(int n) {
		outsideFile += n;
	}

	/**
	 * @return end of the file just loaded.
	 */
	long getFileEnd() {
		return fileEnd;
	}

	/**
	 * @return false always: the clip itself is unchanged.
	 */
	@Override
	public boolean processDataUnit(DifarDataUnit clip) {
		processed++;
		updateBuoyColumns(clip);
		DifarCrossing old = clip.getCrossing();
		if (old != null && made.contains(old)) {
			// matched already in this run, as an earlier clip's partner
			return false;
		}
		if (old != null) {
			if (isKept(old)) {
				keptClips++;
				kept.add(old.getUID());
				if (!relocated.contains(old.getUID())) {
					pending.add(old);
				}
				return false;
			}
			removed.add(old.getUID());
			old.removeAllSubDetections();
			crossingBlock.remove(old, true);
		}
		if (clip.getOriginLatLong(false) == null) {
			// no buoy with a known position was in force when the clip was made
			noBuoy++;
			countNoBuoy(clip);
			recorder.forget(clip);
			return false;
		}
		DIFARCrossingInfo proposal = difarProcess.getDifarRangeInfo(clip, this::isFreeToMatch);
		if (proposal == null) {
			recorder.forget(clip);
			return false;
		}
		recorder.record(clip);
		clip.clearTempCrossing();
		DifarCrossing crossing = clip.getCrossing();
		if (crossing != null && made.add(crossing)) {
			residualLog.add(crossing, DifarMatchSelector.chooseMatch(difarProcess.getMatchLog().get(clip)),
					difarControl.getSonobuoyHistory());
		}
		return false;
	}

	/** Write the residuals of the crossings made, beside the database. */
	private void writeResiduals() {
		try {
			java.io.File file = residualLog.write();
			if (file != null) {
				System.out.printf("DIFAR: residuals of %d crossings, %d time delays, written to %s\n",
						residualLog.getTriangulationCount(), residualLog.getDelayCount(), file);
			}
			else if (residualLog.getDelayCount() > 0) {
				System.out.println("DIFAR: the database is not a single file, so the crossing residuals were not written");
			}
		}
		catch (java.io.IOException e) {
			System.out.println("DIFAR: the crossing residuals could not be written: " + e);
		}
	}

	private void updateBuoyColumns(DifarDataUnit clip) {
		if (connection == null || (buoyChanged != null && !buoyChanged.test(clip))) {
			return;
		}
		SQLLogging logging = getDataBlock().getLogging();
		if (logging instanceof DifarSqlLogging && ((DifarSqlLogging) logging).updateBuoyColumns(connection, clip) > 0) {
			clipRows++;
		}
	}

	/**
	 * Work out the kept crossings met in this load again, once all their clips
	 * are here. The task group then saves them with the other crossing changes.
	 */
	@Override
	public void loadedDataComplete() {
		outsideFile += processClipsAfterFile(this, fileEnd);
		relocatePending();
	}

	/**
	 * Work out again the kept crossings met in this load. Used by the upgrade
	 * too, which runs this task's steps.
	 */
	void relocatePending() {
		for (DifarCrossing crossing : pending) {
			if (localiser.relocate(crossing) == CrossingLocaliser.Outcome.CLIPS_NOT_LOADED) {
				notLoaded.add(crossing.getUID());
				continue;
			}
			notLoaded.remove(crossing.getUID());
			relocated.add(crossing.getUID());
			crossingBlock.updatePamData(crossing, System.currentTimeMillis());
		}
		pending.clear();
	}

	private boolean isKept(DifarCrossing crossing) {
		return keepOperatorChoices && crossing.getMatchChoice() == DifarCrossing.MatchChoice.OPERATOR;
	}

	/**
	 * A clip can be a partner unless it belongs to a crossing being kept, or
	 * to one made earlier in this run.
	 */
	private boolean isFreeToMatch(DifarDataUnit clip) {
		DifarCrossing crossing = clip.getCrossing();
		return crossing == null || (!isKept(crossing) && !made.contains(crossing));
	}

	/**
	 * Add a clip with no buoy in force to its channel's count and time range.
	 * @param clip the clip.
	 */
	private void countNoBuoy(DifarDataUnit clip) {
		int channel = PamUtils.getSingleChannel(clip.getChannelBitmap());
		long time = clip.getTimeMilliseconds();
		long[] entry = noBuoyByChannel.get(channel);
		if (entry == null) {
			noBuoyByChannel.put(channel, new long[] {1, time, time});
			return;
		}
		entry[0]++;
		entry[1] = Math.min(entry[1], time);
		entry[2] = Math.max(entry[2], time);
	}

	@Override
	public void completeTask() {
		if (!running) {
			return;
		}
		running = false;
		// counted from the crossings, since a clip left unmatched can later be taken as a partner
		int inCrossings = 0;
		for (DifarCrossing crossing : made) {
			inCrossings += crossing.getSubDetectionsCount();
		}
		System.out.printf("DIFAR: rematch went through %d clips, made %d crossings holding %d clips, left %d clips unmatched, "
				+ "removed %d old crossings, and kept %d chosen by the operator, %d of them worked out again. %d clip rows updated\n",
				processed, made.size(), inCrossings, processed - inCrossings - keptClips,
				removed.size(), kept.size(), relocated.size(), clipRows);
		if (outsideFile > 0) {
			System.out.printf("DIFAR: %d of those clips start outside their file's time span, and were processed separately\n",
					outsideFile);
		}
		int narrow = 0;
		int onBuoy = 0;
		CrossingSelectParams defaults = new CrossingSelectParams();
		for (DifarCrossing crossing : made) {
			if (crossing.isOnBuoy()) {
				onBuoy++;
			}
			else if (crossing.getCrossingAngle() < defaults.minAngle) {
				narrow++;
			}
		}
		if (narrow + onBuoy > 0) {
			System.out.printf("DIFAR: of the crossings made, %d lie on one of their own buoys and %d more cross at under %.0f degrees; "
					+ "the crossing data selector hides both by default\n", onBuoy, narrow, defaults.minAngle);
		}
		String correlations = difarProcess.getClipDelays().takeSummary();
		if (correlations != null) {
			System.out.println(correlations);
		}
		writeResiduals();
		int failedErrors = Simplex2D.takeFailedErrorEstimates();
		if (failedErrors > 0) {
			System.out.printf("DIFAR: %d error estimates failed while locating crossings, so some errors are unknown\n",
					failedErrors);
		}
		if (noBuoy > 0) {
			System.out.printf("DIFAR: %d of the unmatched clips had no buoy with a known position in force when they were made\n",
					noBuoy);
			for (Map.Entry<Integer, long[]> entry : noBuoyByChannel.entrySet()) {
				long[] v = entry.getValue();
				System.out.printf("DIFAR:   channel %d: %d clips, from %s to %s\n", entry.getKey(), v[0],
						PamCalendar.formatDateTime(v[1]), PamCalendar.formatDateTime(v[2]));
			}
		}
		if (!notLoaded.isEmpty()) {
			System.out.printf("DIFAR: %d kept crossings were not worked out again, since their clips were never all loaded together: UIDs %s\n",
					notLoaded.size(), notLoaded);
		}
		SwingUtilities.invokeLater(() -> AbstractScrollManager.getScrollManager().reLoad());
	}
}
