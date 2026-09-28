package clickTrainDetector.clickTrainAlgorithms.ukf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import PamguardMVC.PamDataUnit;
import clickTrainDetector.clickTrainAlgorithms.ClickFeatureUtils;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.BearingChi2VarParams;

/**
 * The core UKF tracking pipeline, independent of the PAMGuard plumbing so it can
 * be unit tested. Clicks are batched into time "frames"; each frame runs:
 * <p>
 * UKF prediction → learned affinity → two-stage Hungarian assignment → UKF
 * update. Finished tracks are reported to a {@link Listener}.
 *
 * @author Jamie Macaulay
 */
public class UKFTracker {

	/** Length of the affinity feature vector. */
	public static final int FEATURE_DIM = 7;

	/**
	 * Sentinel value written into a feature slot when that feature is disabled or
	 * cannot be measured for a pairing (e.g. amplitude tracking off, or no waveform
	 * for correlation / peak frequency). Every real feature is a non-negative
	 * distance / magnitude, so a single negative sentinel is unambiguous: the
	 * affinity network is trained (with feature dropout) to recognise it as "this
	 * feature is absent" and to fall back on the remaining features. See
	 * {@link #maskableFeatures()} and {@link AffinityMLPTrainer}.
	 */
	public static final double ABSENT = -1.0;

	/**
	 * Divisor applied to the raw peak-frequency difference (Hz) so the feature is
	 * O(1). Only affects the pre-standardisation scale (the trainer standardises
	 * features, and the default gate ignores this feature), so the exact value is
	 * not critical.
	 */
	private static final double PEAK_FREQ_NORM_HZ = 1000.0;

	/** A large cost used for forbidden (gated-out) track-detection pairs. */
	private static final double FORBIDDEN_COST = 1e6;

	/** Notified when a track finishes. */
	public interface Listener {
		void trackFinished(ClickTrack track);
	}

	private final UKFParams params;
	private final boolean bearingAvailable;
	private final TrackStateModel model;
	private final CTAffinity affinity;
	private final Listener listener;

	private final ArrayList<ClickTrack> tracks = new ArrayList<>();
	private final ArrayList<PamDataUnit<?, ?>> frameClicks = new ArrayList<>();
	private double frameEndTime = Double.NaN;
	private int nextTrackId = 0;

	/* ---- N-scan beam state (only used when useNScan() is true) ---- */
	private ArrayList<Hypothesis> beam = new ArrayList<>();
	private int framesSinceCommit = 0;

	public UKFTracker(UKFParams params, boolean bearingAvailable, Listener listener) {
		this.params = params;
		this.bearingAvailable = bearingAvailable;
		this.model = new TrackStateModel(params, params.useAmplitude, params.useBearing && bearingAvailable);
		this.affinity = buildAffinity(params);
		this.listener = listener;
		if (useNScan()) {
			initBeam();
		}
	}

	/**
	 * Whether the fixed-lag multi-hypothesis (N-scan) association search should be
	 * used, as opposed to the single-frame greedy nearest-neighbour association.
	 * True only when the user has enabled it ({@link UKFParams#useMultiHypothesis})
	 * and the deferral depth ({@link UKFParams#nScanDepth}) is positive.
	 */
	private boolean useNScan() {
		return params.useMultiHypothesis && params.nScanDepth > 0;
	}

	/**
	 * Build the affinity metric: a custom network loaded from file if the user has
	 * configured one, otherwise the built-in default (Gaussian-gating) network. Any
	 * problem loading the custom network falls back to the default with a warning.
	 */
	private static CTAffinity buildAffinity(UKFParams params) {
		if (params.useCustomAffinity && params.affinityNetworkFile != null
				&& !params.affinityNetworkFile.trim().isEmpty()) {
			try {
				return AffinityNN.fromFile(new java.io.File(params.affinityNetworkFile), FEATURE_DIM);
			} catch (Exception e) {
				System.err.println("UKFTracker: could not load custom affinity network from "
						+ params.affinityNetworkFile + " - falling back to default. " + e.getMessage());
			}
		}
		return AffinityNN.defaultGate(FEATURE_DIM);
	}

	/** Reset the tracker, discarding all active tracks and buffered clicks. */
	public void reset() {
		frameClicks.clear();
		frameEndTime = Double.NaN;
		tracks.clear();
		if (useNScan()) {
			initBeam();
		}
	}

	/** Add a click; the frame is flushed automatically when its window closes. */
	public void addClick(PamDataUnit<?, ?> click) {
		double t = timeSeconds(click);
		if (frameClicks.isEmpty()) {
			frameEndTime = t + params.frameDuration;
		} else if (t >= frameEndTime) {
			flushFrame();
			frameEndTime = t + params.frameDuration;
		}
		frameClicks.add(click);
	}

	/** Process and clear the current frame of clicks. */
	public void flushFrame() {
		if (frameClicks.isEmpty()) {
			return;
		}
		ArrayList<PamDataUnit<?, ?>> dets = new ArrayList<>(frameClicks);
		frameClicks.clear();
		double now = 0;
		for (PamDataUnit<?, ?> d : dets) {
			now = Math.max(now, timeSeconds(d));
		}
		if (useNScan()) {
			processBeam(dets, now);
		} else {
			process(dets, now);
		}
	}

	/** Finalise all remaining tracks (e.g. at the end of processing). */
	public void finaliseAll() {
		flushFrame();
		if (useNScan()) {
			finaliseBeam();
			return;
		}
		for (ClickTrack track : tracks) {
			listener.trackFinished(track);
		}
		tracks.clear();
	}

	/**
	 * Coast active tracks forward to the given time, bridging missed clicks. A track
	 * that cannot reach {@code now} within {@code maxCoast} missed clicks is closed.
	 */
	public void closeOverdueTracks(double now) {
		if (useNScan()) {
			for (Hypothesis h : beam) {
				closeOverdue(h.tracks, now, h.finished);
			}
			return;
		}
		closeOverdue(tracks, now, null);
	}

	/**
	 * Coast the tracks in {@code trackList} forward to {@code now}, closing any that
	 * cannot reach it within {@code maxCoast} missed clicks. A closed track is added
	 * to {@code finishedSink} if given, otherwise reported straight to the listener
	 * (the greedy path emits immediately; the N-scan path defers emission until the
	 * owning hypothesis is committed).
	 */
	private void closeOverdue(ArrayList<ClickTrack> trackList, double now, ArrayList<ClickTrack> finishedSink) {
		Iterator<ClickTrack> it = trackList.iterator();
		while (it.hasNext()) {
			ClickTrack track = it.next();
			if (track.size() < 2) {
				// no measured ICI yet: do not coast (a coasted prior could absorb a click
				// that is really too far away). Close if no second click arrived within the
				// maximum ICI, so a too-slow train never bootstraps.
				if (now - track.getLastTimeSeconds() > params.maxICI) {
					finishTrack(track, finishedSink);
					it.remove();
				}
				continue;
			}
			// coast forward through whole missed clicks while overdue.
			while ((now - track.getLastTimeSeconds()) > 1.5 * track.expectedICI()
					&& track.getCoast() < params.maxCoast) {
				track.coastForward();
			}
			// still overdue after the maximum number of coasts -> the track has ended.
			if ((now - track.getLastTimeSeconds()) > 1.5 * track.expectedICI()) {
				finishTrack(track, finishedSink);
				it.remove();
			}
		}
	}

	private void finishTrack(ClickTrack track, ArrayList<ClickTrack> finishedSink) {
		if (finishedSink != null) {
			finishedSink.add(track);
		} else {
			listener.trackFinished(track);
		}
	}

	/** Run the full pipeline for one frame of detections. */
	private void process(ArrayList<PamDataUnit<?, ?>> dets, double now) {
		// coast tracks forward to the frame time so a click resuming after a gap
		// associates at a normal ICI (and tracks that have ended are closed).
		closeOverdueTracks(now);

		// split detections into high / low confidence (ByteTrack two-stage). With the
		// threshold at its default of 0 every click is high-confidence and the split
		// is effectively off.
		ArrayList<PamDataUnit<?, ?>> high = new ArrayList<>();
		ArrayList<PamDataUnit<?, ?>> low = new ArrayList<>();
		for (PamDataUnit<?, ?> d : dets) {
			if (d.getAmplitudeDB() >= params.confidenceAmplitude) {
				high.add(d);
			} else {
				low.add(d);
			}
		}

		boolean[] trackMatched = new boolean[tracks.size()];
		boolean[] highMatched = new boolean[high.size()];
		boolean[] lowMatched = new boolean[low.size()];

		// Association is ordered by track maturity, then by detection confidence. A
		// confirmed (established) track gets first pick of every detection so a
		// freshly-started tentative track cannot steal a click from it; within each
		// maturity class the ByteTrack high/low-confidence split still applies.
		associate(high, highMatched, trackMatched, true); // confirmed tracks, high-conf dets
		if (!low.isEmpty()) {
			associate(low, lowMatched, trackMatched, true); // confirmed tracks, low-conf dets
		}
		associate(high, highMatched, trackMatched, false); // tentative tracks, high-conf dets
		if (!low.isEmpty()) {
			associate(low, lowMatched, trackMatched, false); // tentative tracks, low-conf dets
		}

		// unmatched high-confidence detections start new (tentative) tracks.
		for (int d = 0; d < high.size(); d++) {
			if (!highMatched[d]) {
				PamDataUnit<?, ?> click = high.get(d);
				tracks.add(new ClickTrack(nextTrackId++, model, params, click, timeSeconds(click),
						click.getAmplitudeDB(), bearingOf(click)));
			}
		}
	}

	/**
	 * Associate a set of detections to a maturity class of active tracks via
	 * Hungarian assignment, updating matched tracks. Only tracks whose confirmed
	 * state equals {@code confirmed} and that are not already matched (in
	 * {@code trackMatched}) are considered, and only detections not already matched
	 * (in {@code detMatched}) are offered. Both masks are updated in place so the
	 * method can be called repeatedly for successive maturity/confidence stages over
	 * the same detection list.
	 *
	 * @param dets         - the candidate detections.
	 * @param detMatched   - per-detection matched mask (updated in place).
	 * @param trackMatched - per-track matched mask, indexed over all tracks (updated
	 *                       in place).
	 * @param confirmed    - whether to associate confirmed (true) or tentative
	 *                       (false) tracks in this pass.
	 */
	private void associate(ArrayList<PamDataUnit<?, ?>> dets, boolean[] detMatched, boolean[] trackMatched,
			boolean confirmed) {
		if (dets.isEmpty()) {
			return;
		}

		ArrayList<Integer> candTracks = new ArrayList<>();
		for (int i = 0; i < tracks.size(); i++) {
			if (!trackMatched[i] && tracks.get(i).isConfirmed() == confirmed) {
				candTracks.add(i);
			}
		}
		if (candTracks.isEmpty()) {
			return;
		}

		ArrayList<Integer> candDets = new ArrayList<>();
		for (int c = 0; c < dets.size(); c++) {
			if (!detMatched[c]) {
				candDets.add(c);
			}
		}
		if (candDets.isEmpty()) {
			return;
		}

		double[][] cost = new double[candTracks.size()][candDets.size()];
		double[][][] measCache = new double[candTracks.size()][candDets.size()][];
		for (int r = 0; r < candTracks.size(); r++) {
			ClickTrack track = tracks.get(candTracks.get(r));
			// the unscented measurement prediction depends only on the track, so compute
			// it once and reuse it for every candidate detection.
			UnscentedKalmanFilter.Prediction prediction = track.predictMeasurement();
			for (int cc = 0; cc < candDets.size(); cc++) {
				PamDataUnit<?, ?> det = dets.get(candDets.get(cc));
				double t = timeSeconds(det);
				double ici = t - track.getLastTimeSeconds();
				if (ici <= 0 || ici > params.maxICI) {
					cost[r][cc] = FORBIDDEN_COST;
					continue;
				}
				double[] z = track.measurement(t, det.getAmplitudeDB(), bearingOf(det));
				measCache[r][cc] = z;
				if (bearingJumpExceeded(track, z, prediction)) {
					// bearing jumps too far (in the policed direction) to be the same animal.
					cost[r][cc] = FORBIDDEN_COST;
					continue;
				}
				double aff = affinity.affinity(associationFeatures(track, det, z, t, params, prediction));
				cost[r][cc] = aff < params.minAffinity ? FORBIDDEN_COST : -Math.log(aff);
			}
		}

		int[] rowToCol = HungarianAlgorithm.solve(cost);
		for (int r = 0; r < rowToCol.length; r++) {
			int cc = rowToCol[r];
			if (cc < 0 || cost[r][cc] >= FORBIDDEN_COST) {
				continue;
			}
			int trackIndex = candTracks.get(r);
			int detIndex = candDets.get(cc);
			ClickTrack track = tracks.get(trackIndex);
			PamDataUnit<?, ?> det = dets.get(detIndex);
			track.predict();
			track.update(measCache[r][cc], det, timeSeconds(det));
			trackMatched[trackIndex] = true;
			detMatched[detIndex] = true;
		}
	}

	/* -------------------- N-scan multi-hypothesis pipeline -------------------- */

	/** Start (or restart) the beam with a single empty hypothesis. */
	private void initBeam() {
		beam = new ArrayList<>();
		beam.add(new Hypothesis(new ArrayList<>(), new ArrayList<>(), 0.0, 0));
		framesSinceCommit = 0;
	}

	/**
	 * Process one frame of detections through the fixed-lag multi-hypothesis beam.
	 * Each hypothesis is expanded by the Murty k-best global assignments of its
	 * tracks to the frame's detections (with miss and new-track options folded into
	 * an augmented cost matrix); the resulting children are pruned to the beam width,
	 * and the best hypothesis is committed once every {@code nScanDepth} frames.
	 */
	private void processBeam(ArrayList<PamDataUnit<?, ?>> dets, double now) {
		// coast/close overdue tracks in every hypothesis (deferred emission).
		for (Hypothesis h : beam) {
			closeOverdue(h.tracks, now, h.finished);
		}

		boolean[] isHigh = new boolean[dets.size()];
		for (int c = 0; c < dets.size(); c++) {
			isHigh[c] = dets.get(c).getAmplitudeDB() >= params.confidenceAmplitude;
		}

		ArrayList<Hypothesis> children = new ArrayList<>();
		for (Hypothesis h : beam) {
			double[][] cost = buildAssignmentMatrix(h, dets, isHigh, now);
			if (cost.length == 0) {
				children.add(h); // no tracks and no detections - carry the hypothesis forward.
				continue;
			}
			List<int[]> assignments = MurtyKBest.kBest(cost, params.nScanKBest);
			if (assignments.isEmpty()) {
				children.add(h);
				continue;
			}
			for (int[] assignment : assignments) {
				children.add(applyAssignment(h, assignment, dets, isHigh, now, cost));
			}
		}

		// prune the beam to the lowest-scoring hypotheses.
		children.sort((a, b) -> Double.compare(a.score, b.score));
		int keep = Math.min(params.beamWidth, children.size());
		beam = new ArrayList<>(children.subList(0, keep));

		// commit the best hypothesis once the deferral window has elapsed.
		framesSinceCommit++;
		if (framesSinceCommit >= params.nScanDepth) {
			commitBeam();
		}
	}

	/**
	 * Build the augmented square assignment-cost matrix for a hypothesis. Rows are
	 * {@code [tracks | one miss-dummy per track]} and columns are
	 * {@code [detections | one new-track dummy per detection]}, so a complete minimum
	 * assignment chooses, at the right relative cost, whether each track takes a
	 * detection or misses and whether each detection joins a track or starts a new
	 * one.
	 */
	private double[][] buildAssignmentMatrix(Hypothesis h, ArrayList<PamDataUnit<?, ?>> dets, boolean[] isHigh,
			double now) {
		int t = h.tracks.size();
		int d = dets.size();
		int n = t + d;
		double[][] cost = new double[n][n];
		for (double[] row : cost) {
			java.util.Arrays.fill(row, MurtyKBest.FORBIDDEN);
		}

		// block A: track <-> detection edge costs.
		for (int r = 0; r < t; r++) {
			ClickTrack track = h.tracks.get(r);
			UnscentedKalmanFilter.Prediction prediction = track.predictMeasurement();
			for (int c = 0; c < d; c++) {
				PamDataUnit<?, ?> det = dets.get(c);
				double time = timeSeconds(det);
				double ici = time - track.getLastTimeSeconds();
				if (ici <= 0 || ici > params.maxICI) {
					continue;
				}
				double[] z = track.measurement(time, det.getAmplitudeDB(), bearingOf(det));
				if (bearingJumpExceeded(track, z, prediction)) {
					// bearing jumps too far (in the policed direction): leave this pairing forbidden.
					continue;
				}
				double aff = affinity.affinity(associationFeatures(track, det, z, time, params, prediction));
				if (aff >= params.minAffinity) {
					cost[r][c] = -Math.log(aff);
				}
			}
		}
		// block B: each track's own miss (coast) dummy column, charged only if due.
		for (int r = 0; r < t; r++) {
			cost[r][d + r] = isDue(h.tracks.get(r), now) ? params.nScanCoastCost : 0.0;
		}
		// block C: each detection's own new-track / drop dummy row.
		for (int c = 0; c < d; c++) {
			cost[t + c][c] = params.nScanNewTrackCost;
		}
		// block D: leftover dummies pair at zero cost.
		for (int c = 0; c < d; c++) {
			for (int r = 0; r < t; r++) {
				cost[t + c][d + r] = 0.0;
			}
		}
		return cost;
	}

	/**
	 * Apply one global assignment to a copy of a parent hypothesis: matched tracks
	 * are updated with their detection, unmatched due tracks coast (already priced
	 * in), and unmatched high-confidence detections start new tracks. The child's
	 * cumulative score is the parent's plus this assignment's total cost.
	 */
	private Hypothesis applyAssignment(Hypothesis parent, int[] assignment, ArrayList<PamDataUnit<?, ?>> dets,
			boolean[] isHigh, double now, double[][] cost) {
		Hypothesis child = parent.copy();
		int t = parent.tracks.size();
		int d = dets.size();

		double inc = 0;
		boolean[] detMatched = new boolean[d];
		for (int r = 0; r < t; r++) {
			int col = assignment[r];
			inc += cost[r][col];
			if (col < d) {
				ClickTrack track = child.tracks.get(r);
				PamDataUnit<?, ?> det = dets.get(col);
				double time = timeSeconds(det);
				double[] z = track.measurement(time, det.getAmplitudeDB(), bearingOf(det));
				track.predict();
				track.update(z, det, time);
				detMatched[col] = true;
			}
			// col >= d is the track's miss dummy: nothing to apply (time-based coasting is
			// handled by closeOverdue on the next frame).
		}
		for (int c = 0; c < d; c++) {
			if (!detMatched[c]) {
				inc += cost[t + c][c];
				if (isHigh[c]) {
					PamDataUnit<?, ?> det = dets.get(c);
					child.tracks.add(new ClickTrack(child.nextTrackId++, model, params, det, timeSeconds(det),
							det.getAmplitudeDB(), bearingOf(det)));
				}
			}
		}
		child.score = parent.score + inc;
		return child;
	}

	/**
	 * Commit the current best hypothesis: emit its finished tracks and collapse the
	 * beam onto it, so associations older than the deferral window become final.
	 */
	private void commitBeam() {
		if (beam.isEmpty()) {
			initBeam();
			return;
		}
		Hypothesis best = bestHypothesis();
		for (ClickTrack track : best.finished) {
			listener.trackFinished(track);
		}
		best.finished.clear();
		beam = new ArrayList<>();
		beam.add(best);
		framesSinceCommit = 0;
	}

	/** Finalise the beam at end of processing: emit the best hypothesis in full. */
	private void finaliseBeam() {
		if (!beam.isEmpty()) {
			Hypothesis best = bestHypothesis();
			best.finished.addAll(best.tracks);
			best.tracks.clear();
			for (ClickTrack track : best.finished) {
				listener.trackFinished(track);
			}
		}
		initBeam();
	}

	private Hypothesis bestHypothesis() {
		Hypothesis best = beam.get(0);
		for (Hypothesis h : beam) {
			if (h.score < best.score) {
				best = h;
			}
		}
		return best;
	}

	/**
	 * Whether a track is "due" a detection at the given time - i.e. the frame has
	 * reached (within half a frame) the time the track expects its next click. A miss
	 * is only penalised for a due track, so the normal quiet frames between clicks of
	 * a train are free.
	 */
	private boolean isDue(ClickTrack track, double now) {
		return now >= track.predictedNextTime() - 0.5 * params.frameDuration;
	}

	/**
	 * One global tracking hypothesis for the N-scan beam: a set of active tracks, the
	 * tracks it has already finished (awaiting commit), a cumulative association
	 * score (lower is better) and the next track id to allocate.
	 */
	private static final class Hypothesis {
		private final ArrayList<ClickTrack> tracks;
		private final ArrayList<ClickTrack> finished;
		private double score;
		private int nextTrackId;

		private Hypothesis(ArrayList<ClickTrack> tracks, ArrayList<ClickTrack> finished, double score, int nextTrackId) {
			this.tracks = tracks;
			this.finished = finished;
			this.score = score;
			this.nextTrackId = nextTrackId;
		}

		/**
		 * A deep-ish copy: active tracks are branched (UKF cloned, clicks shared
		 * copy-on-write); the finished list is copied but its already-frozen tracks are
		 * shared.
		 */
		private Hypothesis copy() {
			ArrayList<ClickTrack> t2 = new ArrayList<>(tracks.size());
			for (ClickTrack track : tracks) {
				t2.add(track.copy());
			}
			return new Hypothesis(t2, new ArrayList<>(finished), score, nextTrackId);
		}
	}

	/**
	 * Build the affinity feature vector for a (track, detection) pairing. Used by
	 * both association (here) and by {@code AffinityTrainer} when generating
	 * training examples, so that the trained network sees exactly the same features
	 * as inference. Only feature 0 (Mahalanobis distance) is used by the default
	 * network; the rest exist for a trained network.
	 * <p>
	 * Layout (length {@link #FEATURE_DIM}):
	 *
	 * <pre>
	 * 0 Mahalanobis distance (always)
	 * 1 normalised |ICI innovation| (always)
	 * 2 normalised |amplitude innovation|  - {@link #ABSENT} if amplitude not tracked
	 * 3 normalised |bearing innovation|    - {@link #ABSENT} if bearing not tracked
	 * 4 observed/expected ICI ratio (always)
	 * 5 -log(waveform correlation)         - {@link #ABSENT} if disabled/no waveform
	 * 6 normalised |peak-frequency diff|   - {@link #ABSENT} if disabled/no waveform
	 * </pre>
	 *
	 * @param track  - the candidate track.
	 * @param det    - the candidate detection (for the waveform features); may be null.
	 * @param z      - the candidate measurement (from {@link ClickTrack#measurement}).
	 * @param t      - the candidate click's time in seconds.
	 * @param params - the UKF parameters (for the measurement-noise normalisers).
	 */
	public static double[] associationFeatures(ClickTrack track, PamDataUnit<?, ?> det, double[] z, double t,
			UKFParams params) {
		return associationFeatures(track, det, z, t, params, track.predictMeasurement());
	}

	/**
	 * As
	 * {@link #associationFeatures(ClickTrack, PamDataUnit, double[], double, UKFParams)}
	 * but against a precomputed {@link UnscentedKalmanFilter.Prediction}, so the
	 * expensive unscented transform is done once per track rather than once per
	 * candidate detection.
	 */
	public static double[] associationFeatures(ClickTrack track, PamDataUnit<?, ?> det, double[] z, double t,
			UKFParams params, UnscentedKalmanFilter.Prediction prediction) {
		double[] f = new double[FEATURE_DIM];
		f[0] = track.mahalanobis(z, prediction);
		double[] innov = track.getModel().measResidual(z, prediction.zPred());
		f[1] = Math.abs(innov[0]) / Math.sqrt(params.iciMeasNoise);
		f[2] = track.getModel().usesAmplitude()
				? Math.abs(innov[track.getModel().ampMeasIndex()]) / Math.sqrt(params.ampMeasNoise)
				: ABSENT;
		f[3] = track.getModel().usesBearing()
				? Math.abs(innov[track.getModel().bearingMeasIndex()]) / params.bearingToleranceRad()
				: ABSENT;
		f[4] = (t - track.getLastTimeSeconds()) / Math.max(track.expectedICI(), 1e-3);
		f[5] = correlationFeature(track, det, params);
		f[6] = peakFreqFeature(track, det, params);
		return f;
	}

	/**
	 * Whether associating the candidate measurement {@code z} to {@code track} would
	 * require a bearing jump beyond the configured maximum (in the policed
	 * direction). The jump is the signed bearing innovation - the wrapped difference
	 * between the candidate bearing and the track's predicted bearing. Returns false
	 * when the cutoff is disabled or the model does not track bearing.
	 *
	 * @param track      - the candidate track.
	 * @param z          - the candidate measurement (from {@link ClickTrack#measurement}).
	 * @param prediction - the track's precomputed measurement prediction.
	 * @return true if the pairing should be forbidden on bearing-jump grounds.
	 */
	private boolean bearingJumpExceeded(ClickTrack track, double[] z, UnscentedKalmanFilter.Prediction prediction) {
		if (!params.bearingJumpEnable || !track.getModel().usesBearing()) {
			return false;
		}
		double signedJump = track.getModel().measResidual(z, prediction.zPred())[track.getModel().bearingMeasIndex()];
		return BearingChi2VarParams.exceedsMaxJump(signedJump, Math.toRadians(params.maxBearingJumpDeg),
				params.bearingJumpDrctn);
	}

	/**
	 * The (track, detection) waveform-correlation feature: {@code -log(correlation)}
	 * of the candidate against the track's most recent click (so 0 is a perfect
	 * match and larger is worse), or {@link #ABSENT} when correlation is disabled or
	 * no waveform is available.
	 */
	private static double correlationFeature(ClickTrack track, PamDataUnit<?, ?> det, UKFParams params) {
		if (!params.useCorrelation || det == null) {
			return ABSENT;
		}
		PamDataUnit<?, ?> last = lastClick(track);
		if (last == null) {
			return ABSENT;
		}
		double corr = ClickFeatureUtils.waveformCorrelation(last, det);
		if (Double.isNaN(corr)) {
			return ABSENT;
		}
		return -Math.log(Math.max(corr, 1e-3));
	}

	/**
	 * The (track, detection) peak-frequency feature: the normalised absolute
	 * difference between the candidate's peak frequency and the track's most recent
	 * click, or {@link #ABSENT} when peak frequency is disabled or cannot be
	 * measured.
	 */
	private static double peakFreqFeature(ClickTrack track, PamDataUnit<?, ?> det, UKFParams params) {
		if (!params.usePeakFreq || det == null) {
			return ABSENT;
		}
		PamDataUnit<?, ?> last = lastClick(track);
		if (last == null) {
			return ABSENT;
		}
		double pfDet = ClickFeatureUtils.getPeakFrequency(det);
		double pfLast = ClickFeatureUtils.getPeakFrequency(last);
		if (Double.isNaN(pfDet) || Double.isNaN(pfLast)) {
			return ABSENT;
		}
		return Math.abs(pfDet - pfLast) / PEAK_FREQ_NORM_HZ;
	}

	/** The most recent click assigned to a track, or null if it has none. */
	private static PamDataUnit<?, ?> lastClick(ClickTrack track) {
		ArrayList<PamDataUnit> clicks = track.getClicks();
		if (clicks == null || clicks.isEmpty()) {
			return null;
		}
		return clicks.get(clicks.size() - 1);
	}

	/**
	 * Which affinity features can be absent (and so are subject to training-time
	 * dropout): amplitude, bearing, correlation and peak frequency. The always-on
	 * ICI-derived features (0, 1, 4) are never dropped.
	 *
	 * @return a length-{@link #FEATURE_DIM} mask, true where the feature is optional.
	 */
	public static boolean[] maskableFeatures() {
		boolean[] mask = new boolean[FEATURE_DIM];
		mask[2] = true; // amplitude
		mask[3] = true; // bearing
		mask[5] = true; // correlation
		mask[6] = true; // peak frequency
		return mask;
	}

	/** Number of currently active tracks (for diagnostics/tests). */
	public int activeTrackCount() {
		return tracks.size();
	}

	/* -------------------- feature extraction from a data unit -------------------- */

	public static double timeSeconds(PamDataUnit<?, ?> dataUnit) {
		return dataUnit.getTimeMilliseconds() / 1000.0;
	}

	public static double bearingOf(PamDataUnit<?, ?> dataUnit) {
		if (dataUnit.getLocalisation() != null && dataUnit.getLocalisation().getAngles() != null
				&& dataUnit.getLocalisation().getAngles().length > 0) {
			return dataUnit.getLocalisation().getAngles()[0];
		}
		return 0;
	}

}
