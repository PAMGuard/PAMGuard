package difar.offline;

import java.awt.Component;
import java.awt.Point;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import dataMap.OfflineDataMapPoint;
import difar.DIFARCrossingInfo;
import difar.DifarControl;
import difar.DifarDataUnit;
import difar.DifarProcess;
import difar.DifarSqlLogging;
import difar.crossings.CrossingLocaliser;
import difar.crossings.CrossingRecorder;
import difar.crossings.DifarCrossing;
import difar.crossings.DifarCrossingDataBlock;
import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import generalDatabase.SQLLogging;
import offlineProcessing.OfflineTask;
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
	private int matched, unmatched, clipRows;

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
		keepOperatorChoices = !difarControl.getDifarParameters().rematchReplacesOperatorChoices;
		made.clear();
		pending.clear();
		relocated.clear();
		notLoaded.clear();
		clipRows = 0;
		removed.clear();
		kept.clear();
		matched = 0;
		unmatched = 0;
	}

	@Override
	public void newDataLoad(long startTime, long endTime, OfflineDataMapPoint mapPoint) {
		connection = DBControlUnit.findConnection();
		pending.clear();
	}

	/**
	 * @return false always: the clip itself is unchanged.
	 */
	@Override
	public boolean processDataUnit(DifarDataUnit clip) {
		updateBuoyColumns(clip);
		DifarCrossing old = clip.getCrossing();
		if (old != null && made.contains(old)) {
			// matched already in this run, as an earlier clip's partner
			return false;
		}
		if (old != null) {
			if (isKept(old)) {
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
		DIFARCrossingInfo proposal = difarProcess.getDifarRangeInfo(clip, this::isFreeToMatch);
		if (proposal == null) {
			recorder.forget(clip);
			unmatched++;
			return false;
		}
		recorder.record(clip);
		clip.clearTempCrossing();
		if (clip.getCrossing() != null) {
			made.add(clip.getCrossing());
			matched++;
		}
		else {
			unmatched++;
		}
		return false;
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

	@Override
	public void completeTask() {
		System.out.printf("DIFAR: rematch made crossings for %d clips, left %d clips unmatched, removed %d old crossings, "
				+ "and kept %d chosen by the operator, %d of them worked out again. %d clip rows updated\n",
				matched, unmatched, removed.size(), kept.size(), relocated.size(), clipRows);
		if (!notLoaded.isEmpty()) {
			System.out.printf("DIFAR: %d kept crossings were not worked out again, since their clips were never all loaded together: UIDs %s\n",
					notLoaded.size(), notLoaded);
		}
		SwingUtilities.invokeLater(() -> AbstractScrollManager.getScrollManager().reLoad());
	}
}
