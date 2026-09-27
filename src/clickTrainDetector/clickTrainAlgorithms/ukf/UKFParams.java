package clickTrainDetector.clickTrainAlgorithms.ukf;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.BearingChi2VarParams.BearingJumpDrctn;

/**
 * Parameters for the UKF click train algorithm.
 *
 * @author Jamie Macaulay
 */
public class UKFParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	/* ---- features the tracker uses ---- */

	/** Track amplitude (always tracks ICI). */
	public boolean useAmplitude = true;

	/** Track bearing. Automatically ignored when no bearing is available. */
	public boolean useBearing = true;

	/**
	 * Use waveform cross-correlation (similarity between a candidate click and the
	 * track's most recent click) as an association feature. Automatically ignored
	 * when no waveform data is available. More discriminating but more processor
	 * intensive, and only used by a trained affinity network (the built-in default
	 * network ignores it).
	 */
	public boolean useCorrelation = false;

	/**
	 * Use peak-frequency consistency (the difference between a candidate click's
	 * peak frequency and the track's most recent click) as an association feature.
	 * Automatically ignored when no waveform / peak-frequency data is available, and
	 * only used by a trained affinity network (the built-in default network ignores
	 * it).
	 */
	public boolean usePeakFreq = false;

	/* ---- gating / lifecycle ---- */

	/** The absolute maximum inter-click interval allowed within a train (seconds). */
	public double maxICI = 0.4;

	/**
	 * Duration of the detection batching window ("frame") in seconds. Clicks inside
	 * one frame are associated together, and each track can take at most one click
	 * per frame - so this must be shorter than the smallest inter-click interval to
	 * be resolved, otherwise a fast train (e.g. a porpoise buzz, ICI down to a few
	 * tens of ms) loses every other click to a spurious new track. Kept well below a
	 * typical minimum ICI.
	 */
	public double frameDuration = 0.02;

	/** Maximum number of consecutive missed clicks before a track is closed. */
	public int maxCoast = 4;

	/** Minimum number of clicks for a track to be saved. */
	public int minTrackLength = 3;

	/**
	 * Number of clicks a track must accumulate before it is "confirmed". Until then
	 * it is tentative and is only offered detections after every confirmed track has
	 * claimed one, so a freshly-started (possibly spurious) track cannot steal a
	 * click from an established train. Should be &gt;= 2 and no greater than
	 * {@link #minTrackLength}.
	 */
	public int confirmHits = 3;

	/* ---- two-stage (ByteTrack) association ---- */

	/**
	 * Amplitude (dB) threshold separating high-confidence from low-confidence
	 * clicks in the two-stage (ByteTrack) association. Clicks at or above this are
	 * associated in the first stage and may start new tracks; quieter clicks are
	 * only offered to existing tracks in the second stage. 0 (the default)
	 * disables the split: all clicks are high-confidence.
	 */
	public double confidenceAmplitude = 0.0;

	/* ---- UKF process noise (random-walk variances per click step) ---- */

	/** Process noise variance of log-ICI. */
	public double iciProcessNoise = 0.01;
	/** Process noise variance of the log-ICI rate (drift). */
	public double iciRateProcessNoise = 1e-4;
	/** Process noise variance of amplitude (dB^2). */
	public double ampProcessNoise = 1.0;
	/** Process noise variance of bearing (rad^2). */
	public double bearingProcessNoise = Math.toRadians(3) * Math.toRadians(3);

	/* ---- UKF measurement noise ---- */

	/** Measurement noise variance of log-ICI. */
	public double iciMeasNoise = 0.02;
	/** Measurement noise variance of amplitude (dB^2). */
	public double ampMeasNoise = 4.0;

	/* ---- maximum bearing jump ---- */

	/**
	 * Smallest bearing tolerance (DEGREES) allowed for {@link #maxBearingJumpDeg},
	 * so the derived bearing measurement noise can never reach zero and make the
	 * filter singular.
	 */
	private static final double MIN_BEARING_TOLERANCE_DEG = 0.5;

	/**
	 * The maximum bearing jump between consecutive clicks, in DEGREES. This is the
	 * single bearing tolerance control: it always sets the bearing measurement-noise
	 * floor (how much bearing change the tracker tolerates), and when
	 * {@link #bearingJumpEnable} is set it is additionally applied as a hard cutoff
	 * (in the {@link #bearingJumpDrctn} direction).
	 */
	public double maxBearingJumpDeg = 3;

	/**
	 * The bearing tolerance in RADIANS, floored at {@link #MIN_BEARING_TOLERANCE_DEG}
	 * so it is always strictly positive.
	 *
	 * @return the bearing tolerance in radians.
	 */
	public double bearingToleranceRad() {
		return Math.toRadians(Math.max(maxBearingJumpDeg, MIN_BEARING_TOLERANCE_DEG));
	}

	/**
	 * The bearing measurement-noise variance (rad^2), derived from
	 * {@link #maxBearingJumpDeg}. This is the floor added to the predicted bearing
	 * variance when forming the innovation covariance.
	 *
	 * @return the bearing measurement-noise variance in rad^2.
	 */
	public double bearingMeasNoiseRad2() {
		double rad = bearingToleranceRad();
		return rad * rad;
	}

	/**
	 * Whether the maximum bearing jump is additionally applied as a hard cutoff. When
	 * enabled, a click whose bearing jumps more than {@link #maxBearingJumpDeg} (in
	 * the {@link #bearingJumpDrctn} direction) from a track's predicted bearing cannot
	 * join that track and instead starts a new one. Useful for high-SNR, sparse
	 * clicks (e.g. sperm whales) or towed arrays where a sudden bearing jump almost
	 * certainly belongs to a different animal. Ignored when no bearing is available.
	 */
	public boolean bearingJumpEnable = false;

	/**
	 * The direction of the bearing jump that is policed. For a towed hydrophone
	 * array {@link BearingJumpDrctn#POSITIVE} is often a good choice as animals
	 * usually pass the vessel from positive to negative bearings, so a large
	 * positive jump is unlikely to belong to the same animal. Only used when
	 * {@link #bearingJumpEnable} is set.
	 */
	public BearingJumpDrctn bearingJumpDrctn = BearingJumpDrctn.POSITIVE;

	/**
	 * Minimum affinity (0-1) for a track-detection pair to be associated. Pairs
	 * below this, or beyond the ICI gate, are forbidden.
	 */
	public double minAffinity = 0.05;

	/* ---- N-scan multi-hypothesis deferral ---- */

	/**
	 * Turn on the fixed-lag multi-hypothesis (N-scan) association search. When
	 * false (the default) the tracker always uses the single-frame greedy
	 * nearest-neighbour association, regardless of {@link #nScanDepth}. When true,
	 * multi-hypothesis association is used provided {@link #nScanDepth} &gt; 0.
	 */
	public boolean useMultiHypothesis = false;

	/**
	 * Association deferral depth, in frames. Only used when
	 * {@link #useMultiHypothesis} is true (and even then, 0 falls back to the
	 * single-frame greedy nearest-neighbour association, which commits every
	 * frame's assignment immediately). A positive value turns on a fixed-lag
	 * multi-hypothesis search: the best few global assignments are kept for up to
	 * this many frames before the winner is committed, so an ambiguous association
	 * (e.g. two trains crossing) can be corrected by later evidence instead of
	 * being locked in. 4-8 is a reasonable range; larger values defer longer (more
	 * robust, more memory/CPU).
	 */
	public int nScanDepth = 7;

	/**
	 * Number of global hypotheses kept in the N-scan beam. Only used when
	 * multi-hypothesis association is active (see {@link #useMultiHypothesis} and
	 * {@link #nScanDepth}). Larger explores more association possibilities at
	 * proportionally more cost.
	 */
	public int beamWidth = 3;

	/**
	 * Number of best assignments (Murty k-best) generated per hypothesis per frame in
	 * the N-scan search. Only used when multi-hypothesis association is active (see
	 * {@link #useMultiHypothesis} and {@link #nScanDepth}).
	 */
	public int nScanKBest = 3;

	/**
	 * Cost (nats) charged in the N-scan search when an active, currently-due track is
	 * left without a detection in a frame (a missed click). Only used when
	 * multi-hypothesis association is active (see {@link #useMultiHypothesis} and
	 * {@link #nScanDepth}). Should be cheaper than accepting a clearly wrong match
	 * but dear enough that a good match is preferred.
	 */
	public double nScanCoastCost = 1.5;

	/**
	 * Cost (nats) charged in the N-scan search when a high-confidence detection is
	 * not associated to any existing track and instead starts a new one. Only used
	 * when multi-hypothesis association is active (see {@link #useMultiHypothesis}
	 * and {@link #nScanDepth}).
	 */
	public double nScanNewTrackCost = 2.5;

	/* ---- learned affinity network ---- */

	/**
	 * Use a custom affinity network loaded from {@link #affinityNetworkFile} instead
	 * of the built-in default (Gaussian-gating) network.
	 */
	public boolean useCustomAffinity = false;

	/**
	 * Path to a JSON file describing a custom affinity network (see
	 * {@code AffinityNN.fromFile}). Only used when {@link #useCustomAffinity} is set.
	 */
	public String affinityNetworkFile = null;

	@Override
	public UKFParams clone() {
		try {
			return (UKFParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		return PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
	}

}
