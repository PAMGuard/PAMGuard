package difar.crossings;

import java.util.ArrayList;
import java.util.List;

import PamguardMVC.PamDataUnit;
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
		if (params.useCorrelation && clips.get(0) instanceof DifarDataUnit) {
			// delays measured from the first clip, which need not be the seed it was matched from
			selector.setArrivalTimes(difarProcess.getCorrelatedArrivals((DifarDataUnit) clips.get(0), clips),
					params.correlationTimingError, params.maxCorrelatedDelayResidual);
		}
		return selector.localise(clips);
	}

	/**
	 * Store a match's location and errors on a crossing.
	 * @param crossing the crossing.
	 * @param match the match its clips gave.
	 */
	public static void store(DifarCrossing crossing, DifarMatchSelector.Match match, double onBuoyRadius) {
		TargetMotionResult result = match.getResult();
		Double[] errors = result.getErrors();
		crossing.setResult(result.getLatLong(), error(errors, 0), error(errors, 1));
		assess(crossing, onBuoyRadius);
	}

	/**
	 * Work out a crossing's quality from the clips it holds in memory, and
	 * store it. A crossing on one of its buoys has its errors set to the
	 * radius: the fit's own errors there are near zero and mean nothing.
	 * @param crossing the crossing, with its result set.
	 * @param onBuoyRadius metres from a buoy within which a crossing is on it.
	 */
	public static void assess(DifarCrossing crossing, double onBuoyRadius) {
		List<CrossingQuality.Bearing> bearings = new ArrayList<>();
		List<PamDataUnit<?, ?>> clips = crossing.getSubDetections();
		if (clips != null) {
			for (PamDataUnit<?, ?> unit : clips) {
				if (!(unit instanceof DifarDataUnit)) {
					continue;
				}
				DifarDataUnit clip = (DifarDataUnit) unit;
				Double trueAngle = clip.getTrueAngle();
				bearings.add(new CrossingQuality.Bearing(clip.getOriginLatLong(false),
						trueAngle == null ? Double.NaN : trueAngle));
			}
		}
		CrossingQuality quality = CrossingQuality.of(bearings, crossing.getLocation(), onBuoyRadius);
		crossing.setQuality(quality.getAngle(), quality.isOnBuoy());
		if (quality.isOnBuoy()) {
			crossing.setResult(crossing.getLocation(), onBuoyRadius, onBuoyRadius);
		}
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
			assess(crossing, difarControl.getDifarParameters().getOnBuoyRadius());
			return Outcome.NOT_CROSSED;
		}
		store(crossing, match, difarControl.getDifarParameters().getOnBuoyRadius());
		if (!match.isAccepted()) {
			System.out.printf("DIFAR: crossing UID %d recalculated after the buoy change, but %s\n",
					crossing.getUID(), match.getRejectReason());
			return Outcome.RELOCATED_OUTSIDE_LIMITS;
		}
		return Outcome.RELOCATED;
	}

	private static double error(Double[] errors, int i) {
		if (errors == null || errors.length <= i || errors[i] == null) {
			return Double.NaN;
		}
		return errors[i];
	}
}
