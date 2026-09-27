package difar.offline;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

import PamguardMVC.superdet.SuperDetection;
import dataMap.OfflineDataMapPoint;
import difar.DifarControl;
import difar.DifarDataUnit;
import difar.DifarProcess;
import difar.DifarSqlLogging;
import difar.crossings.CrossingLocaliser;
import difar.crossings.DifarCrossing;
import difar.crossings.DifarCrossingDataBlock;
import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import generalDatabase.SQLLogging;
import offlineProcessing.OfflineTask;

/**
 * Brings the database up to date after a buoy changes, over a stretch of
 * data in the viewer.
 * <p>
 * For each clip, the columns of its row that come from the buoy are
 * updated. Each crossing the clips belong to is located again from the
 * clips it holds, keeping its clips and how its match was chosen, and its
 * row is updated. The crossings are loaded with the clips, and PAMGuard
 * links them after each load.
 * <p>
 * No clip is marked as changed, so no binary file is rewritten. The binary
 * files hold what was measured, which a buoy change does not alter.
 */
public class UpdateCrossingTask extends OfflineTask<DifarDataUnit> {

	private final DifarProcess difarProcess;
	private final DifarCrossingDataBlock crossingBlock;
	private final CrossingLocaliser localiser;
	private final Predicate<DifarDataUnit> filter;

	private PamConnection connection;

	/** Crossings met in the current load, in the order met. */
	private final Set<DifarCrossing> pending = new LinkedHashSet<>();
	/** Crossings relocated so far in this run, by UID, so none is done twice. */
	private final Set<Long> done = new LinkedHashSet<>();
	/** Crossings met whose clips were never all in memory together. */
	private final Set<Long> notLoaded = new LinkedHashSet<>();

	private int clipRows, clipsWithoutRow;

	/**
	 * @param difarControl the DIFAR module.
	 * @param filter which clips the change affects, or null for every clip.
	 */
	public UpdateCrossingTask(DifarControl difarControl, Predicate<DifarDataUnit> filter) {
		super(difarControl.getDifarProcess().getProcessedDifarData());
		this.difarProcess = difarControl.getDifarProcess();
		this.crossingBlock = difarProcess.getCrossingDataBlock();
		this.localiser = new CrossingLocaliser(difarProcess, difarControl);
		this.filter = filter;
		addRequiredDataBlock(crossingBlock);
	}

	@Override
	public String getName() {
		return "Update triangulations in " + getDataBlock().getDataName();
	}

	@Override
	public void prepareTask() {
		pending.clear();
		done.clear();
		notLoaded.clear();
		clipRows = 0;
		clipsWithoutRow = 0;
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
		if (connection == null || (filter != null && !filter.test(clip))) {
			return false;
		}
		SQLLogging logging = getDataBlock().getLogging();
		if (logging instanceof DifarSqlLogging) {
			if (((DifarSqlLogging) logging).updateBuoyColumns(connection, clip) > 0) {
				clipRows++;
			}
			else {
				clipsWithoutRow++;
			}
		}
		SuperDetection crossing = clip.getSuperDetection(DifarCrossing.class);
		if (crossing != null && !done.contains(crossing.getUID())) {
			pending.add((DifarCrossing) crossing);
		}
		return false;
	}

	/**
	 * Relocate the crossings met in this load, once all their clips are here.
	 */
	@Override
	public void loadedDataComplete() {
		SQLLogging crossingLogging = crossingBlock.getLogging();
		for (DifarCrossing crossing : pending) {
			CrossingLocaliser.Outcome outcome = localiser.relocate(crossing);
			if (outcome == CrossingLocaliser.Outcome.CLIPS_NOT_LOADED) {
				notLoaded.add(crossing.getUID());
				continue;
			}
			notLoaded.remove(crossing.getUID());
			done.add(crossing.getUID());
			if (crossingLogging != null && connection != null) {
				crossingLogging.reLogData(connection, crossing);
			}
		}
		pending.clear();
	}

	@Override
	public void completeTask() {
		System.out.printf("DIFAR: buoy change applied to %d clip rows and %d crossings\n", clipRows, done.size());
		if (clipsWithoutRow > 0) {
			System.out.printf("DIFAR: %d clips have no database row, so only their binary files hold them\n",
					clipsWithoutRow);
		}
		if (!notLoaded.isEmpty()) {
			System.out.printf("DIFAR: %d crossings were not recalculated, since their clips were never all loaded together: UIDs %s\n",
					notLoaded.size(), notLoaded);
		}
	}
}
