package difar.crossings;

import java.util.ArrayList;
import java.util.List;

import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;
import difar.DIFARCrossingInfo;
import difar.DifarControl;
import difar.DifarDataUnit;
import difar.DifarMatchSelector;
import difar.DifarParameters;
import difar.DifarProcess;
import difar.targetmotion.TargetMotionResult;

/**
 * Works out a crossing's location again from the clips it already holds.
 * <p>
 * Used when a crossing loses clips to a newer one, and when a buoy changes.
 * Membership never changes here, and neither does how the match was chosen.
 * Rematching is a separate decision.
 */
public class CrossingLocaliser {

	/** A crossing needs two bearings. */
	public static final int MIN_CLIPS = 2;

	/** What happened when a crossing was relocated after a buoy change. */
	public enum Outcome {
		/** Located again from its clips. */
		RELOCATED,
		/** Its clips cross outside the residual limits, but the result is kept. */
		RELOCATED_OUTSIDE_LIMITS,
		/** Its clips no longer cross, so its location is cleared. */
		NOT_CROSSED,
		/** Not all its clips are in memory, so it is left unchanged. */
		CLIPS_NOT_LOADED
	}

	private final DifarProcess difarProcess;
	private final DifarControl difarControl;

	public CrossingLocaliser(DifarProcess difarProcess, DifarControl difarControl) {
		this.difarProcess = difarProcess;
		this.difarControl = difarControl;
	}

	/**
	 * Cross the clips of a crossing that are in memory.
	 * @param crossing the crossing.
	 * @return the match, or null if fewer than two clips are in memory or
	 * they cannot be crossed.
	 */
	public DifarMatchSelector.Match localise(DifarCrossing crossing) {
		List<PamDataUnit> clips = new ArrayList<>();
		List<PamDataUnit<?, ?>> loaded = crossing.getSubDetections();
		if (loaded != null) {
			clips.addAll(loaded);
		}
		if (clips.size() < MIN_CLIPS) {
			return null;
		}
		DifarParameters params = difarControl.getDifarParameters();
		DifarMatchSelector selector = new DifarMatchSelector(difarProcess,
				params.detectionTimingError, params.maxBearingResidual, params.maxTimeDelayResidual);
		return selector.localise(clips);
	}

	/**
	 * Store a match's location and errors on a crossing.
	 * @param crossing the crossing.
	 * @param match the match its clips gave.
	 */
	public static void store(DifarCrossing crossing, DifarMatchSelector.Match match) {
		TargetMotionResult result = match.getResult();
		Double[] errors = result.getErrors();
		crossing.setResult(result.getLatLong(), error(errors, 0), error(errors, 1));
	}

	/**
	 * Locate a crossing again after a buoy changed. A crossing whose clips
	 * are not all in memory is left alone, since crossing a subset would
	 * give a different answer. One whose clips no longer cross keeps its
	 * clips but loses its location, so no stale position is left standing.
	 * @param crossing the crossing.
	 * @return what happened.
	 */
	public Outcome relocate(DifarCrossing crossing) {
		if (crossing.getLoadedSubDetectionsCount() < crossing.getSubDetectionsCount()) {
			System.out.printf("DIFAR: crossing UID %d has %d of %d clips in memory, so it is not recalculated\n",
					crossing.getUID(), crossing.getLoadedSubDetectionsCount(), crossing.getSubDetectionsCount());
			return Outcome.CLIPS_NOT_LOADED;
		}
		DifarMatchSelector.Match match = localise(crossing);
		if (match == null) {
			System.out.printf("DIFAR: crossing UID %d no longer crosses after the buoy change, so its location is cleared\n",
					crossing.getUID());
			crossing.setResult(null, Double.NaN, Double.NaN);
			showOnClips(crossing);
			return Outcome.NOT_CROSSED;
		}
		store(crossing, match);
		showOnClips(crossing);
		if (!match.isAccepted()) {
			System.out.printf("DIFAR: crossing UID %d recalculated after the buoy change, but %s\n",
					crossing.getUID(), match.getRejectReason());
			return Outcome.RELOCATED_OUTSIDE_LIMITS;
		}
		return Outcome.RELOCATED;
	}

	/**
	 * Until displays read crossing units (step 5), they draw the crossing
	 * held on each clip. Give those the new location so the map stays true.
	 */
	private static void showOnClips(DifarCrossing crossing) {
		Double[] errors = {crossing.getXError(), crossing.getYError(), 0d};
		LatLong location = crossing.getLocation();
		List<PamDataUnit<?, ?>> clips = crossing.getSubDetections();
		if (clips == null) {
			return;
		}
		for (PamDataUnit<?, ?> unit : clips) {
			DifarDataUnit clip = (DifarDataUnit) unit;
			DIFARCrossingInfo old = clip.getDifarCrossing();
			if (old == null) {
				continue;
			}
			clip.setDifarCrossing(location == null ? null : new DIFARCrossingInfo(old.getMatchedUnits(), location, errors));
		}
	}

	private static double error(Double[] errors, int i) {
		if (errors == null || errors.length <= i || errors[i] == null) {
			return Double.NaN;
		}
		return errors[i];
	}
}
