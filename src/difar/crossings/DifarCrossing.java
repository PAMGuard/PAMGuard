package difar.crossings;

import java.util.List;

import PamUtils.LatLong;
import PamguardMVC.superdet.SuperDetection;
import difar.DifarDataUnit;

/**
 * A crossing of DIFAR bearings: one acoustic event heard on two or more
 * buoys, and the position where their bearings cross.
 * <p>
 * The clips are the crossing's sub-detections. A clip belongs to at most
 * one crossing: adding it to a new one removes it from any other, which
 * {@link SuperDetection} does by default. The crossing's start, end and
 * channel map follow from its clips.
 * <p>
 * Bearings are two-dimensional, so the position has no depth and the error
 * has only x and y parts, in metres.
 */
public class DifarCrossing extends SuperDetection<DifarDataUnit> {

	/** How the clips of a crossing were chosen. */
	public enum MatchChoice {
		/** The best match found automatically. */
		AUTO,
		/** A match the operator picked in the match selector. */
		OPERATOR,
		/** Converted from a crossing stored inside clips by an older version. */
		LEGACY
	}

	private LatLong location;
	private double xError = Double.NaN;
	private double yError = Double.NaN;
	private MatchChoice matchChoice;

	/**
	 * A crossing with no clips yet, as read back from the database. Its clips
	 * are attached as they load.
	 * @param timeMilliseconds time of the earliest clip.
	 */
	public DifarCrossing(long timeMilliseconds) {
		super(timeMilliseconds);
	}

	/**
	 * A crossing of the given clips.
	 * @param clips the clips crossed, at least two.
	 * @param location where the bearings cross.
	 * @param xError error in x, metres.
	 * @param yError error in y, metres.
	 * @param matchChoice how the clips were chosen.
	 */
	public DifarCrossing(List<DifarDataUnit> clips, LatLong location, double xError, double yError,
			MatchChoice matchChoice) {
		super(earliest(clips));
		addSubDetections(clips);
		setResult(location, xError, yError);
		this.matchChoice = matchChoice;
	}

	private static long earliest(List<DifarDataUnit> clips) {
		long t = Long.MAX_VALUE;
		for (DifarDataUnit clip : clips) {
			t = Math.min(t, clip.getTimeMilliseconds());
		}
		return t;
	}

	/**
	 * Set the result of crossing the bearings, when first made or when
	 * recalculated after a clip or buoy changes.
	 * @param location where the bearings cross.
	 * @param xError error in x, metres.
	 * @param yError error in y, metres.
	 */
	public void setResult(LatLong location, double xError, double yError) {
		this.location = location;
		this.xError = xError;
		this.yError = yError;
	}

	/** @return where the bearings cross, or null if not known. */
	public LatLong getLocation() {
		return location;
	}

	/** @return error in x, metres, or NaN if not known. */
	public double getXError() {
		return xError;
	}

	/** @return error in y, metres, or NaN if not known. */
	public double getYError() {
		return yError;
	}

	/** @return how the clips were chosen, or null if not known. */
	public MatchChoice getMatchChoice() {
		return matchChoice;
	}

	/** @param matchChoice how the clips were chosen. */
	public void setMatchChoice(MatchChoice matchChoice) {
		this.matchChoice = matchChoice;
	}
}
