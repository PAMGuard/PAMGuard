package clickTrainDetector.clickTrainAlgorithms.adaptive;

import java.util.Arrays;
import java.util.BitSet;

import PamguardMVC.PamDataUnit;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfo;
import clickTrainDetector.clickTrainAlgorithms.ClickFeatureUtils;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTChi2;
import clickTrainDetector.clickTrainAlgorithms.mht.TrackBitSet;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.BearingChi2VarParams;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.IDIManager;

/**
 * Adaptive predictive-residual chi^2 calculation for a click train track.
 * <p>
 * For every track hypothesis the algorithm predicts the next value of each
 * available feature (inter-click interval, amplitude and, where available,
 * bearing) from the track's recent history. When a new detection is added to
 * the hypothesis the standardised residual between the observed and predicted
 * value is scored. Crucially the scale used to standardise the residual is
 * estimated <i>from the track itself</i> (a robust median-absolute-deviation of
 * recent residuals), floored by a small physically-meaningful minimum that is
 * scaled by a single sensitivity parameter. This removes the need for the user
 * to specify a variance coefficient and minimum error for every feature.
 * <p>
 * The combined score is a robust (Huber) mean chi^2 per click per feature, so it
 * has the same meaning regardless of how many features are available - this is
 * what allows the same settings to work for single-channel data (no bearing),
 * multi-channel data (with bearing) and data without waveforms.
 *
 * @author Jamie Macaulay
 */
public class AdaptiveCTChi2 implements MHTChi2<PamDataUnit>, Cloneable {

	/**
	 * Number of recent values used to estimate the per-feature residual scale.
	 */
	private static final int WINDOW = 6;

	/**
	 * The Huber threshold (in standard deviations) beyond which a residual's
	 * influence grows only linearly. Makes the score robust to the occasional
	 * outlier or misassociated click.
	 */
	private static final double HUBER_C = 2.5;

	/**
	 * Minimum residual scale ("floor") for amplitude, in dB, at sensitivity 0.5.
	 */
	private static final double AMP_FLOOR_DB = 2.5;

	/**
	 * Smallest bearing tolerance (degrees) allowed for the bearing residual-scale
	 * floor, so a zero maximum bearing jump cannot collapse the scale to nothing.
	 */
	private static final double MIN_BEARING_TOLERANCE_DEG = 0.5;

	/**
	 * Scale (at sensitivity 0.5) for the waveform-correlation penalty. Correlation is
	 * an absolute cue: a genuine train has consistently high consecutive correlation,
	 * so the penalty is based on -log(correlation) rather than a self-calibrated
	 * residual. This scale sets how far below 1 the correlation may drift cheaply.
	 */
	private static final double CORR_LOG_SCALE = 0.35;

	/**
	 * Maximum multiple of the floor that a second-difference (bearing) feature's
	 * learned scale is allowed to reach, so a pathological pattern cannot inflate
	 * the tolerance enough to permit bearing jumps between animals.
	 */
	private static final double SECOND_DIFF_SCALE_CAP = 5.0;

	/**
	 * The reward (in nats) subtracted from the score for each well-fitting click
	 * added to a train. This is the log-likelihood-ratio "bonus" for explaining a
	 * detection as part of a train rather than as clutter: it makes longer
	 * consistent trains score lower and prevents fragmentation. A click is worth
	 * including while its residual cost (0.5 * mean Huber) stays below this value.
	 */
	private static final double INCLUDE_REWARD = 2.5;

	/**
	 * A very large chi^2 used to flag junk/aliased tracks so the kernel
	 * de-prioritises and eventually discards them.
	 */
	private static final double JUNK_CHI2 = 1e17;

	/**
	 * Returned when a track is too short to score.
	 */
	private static final double MAX_CHI2 = 1e16;

	private AdaptiveCTChi2Provider provider;

	/** The combined chi^2 value of the last calculation (lower is better). */
	private double chi2 = MAX_CHI2;

	/** Number of coasts (missed clicks) at the end of the track. */
	private int nCoasts = 0;

	/**
	 * Set once a click has been added whose bearing jumped beyond the configured
	 * maximum (in the policed direction). Once violated the track is treated as junk
	 * so the kernel discards it, mirroring the standard MHT "stop the train"
	 * behaviour.
	 */
	private boolean bearingJumpViolated = false;

	/** The last detection actually included in this hypothesis. */
	private PamDataUnit lastIncludedUnit = null;

	/** Number of detections included in this hypothesis. */
	private int includedCount = 0;

	/** Running sum of Huber-squared residual contributions. */
	private double chi2Sum = 0;

	/** Number of (click, feature) contributions making up {@link #chi2Sum}. */
	private int contribCount = 0;

	/**
	 * Running log-likelihood-ratio style score (lower is better). Each well-fitting
	 * included click lowers it by roughly {@link #INCLUDE_REWARD}; coasts raise it.
	 */
	private double totalScore = 0;

	/* ---- ICI state ---- */
	private double[] iciResiduals = new double[WINDOW];
	private int iciResCount = 0;
	private double[] iciWindow = new double[WINDOW];
	private int iciWinCount = 0;

	/* ---- which features contribute to the matching score ---- */
	/** Whether ICI consistency contributes to the score (ICI is always tracked). */
	private boolean iciActive;
	/** Non-null when the feature is enabled and available. */
	private FeatureState amplitude;
	private FeatureState bearing;
	/** Peak frequency, roughly constant along a train -> first-difference feature. */
	private FeatureState peakFrequency;
	/** Waveform correlation is scored absolutely, so it needs no per-train state. */
	private boolean correlationActive;

	public AdaptiveCTChi2(AdaptiveCTChi2Provider provider) {
		this.provider = provider;
		buildFeatures();
	}

	/**
	 * Build the active feature set from the user's enable flags and what the data
	 * actually provides.
	 */
	private void buildFeatures() {
		AdaptiveCTParams params = provider.getParams();
		iciActive = params.useICI;
		// amplitude is roughly constant -> first difference; bearing may sweep rapidly
		// but smoothly -> second difference (penalise off-trajectory jumps, not rate).
		amplitude = params.useAmplitude ? new FeatureState(false, false) : null;
		bearing = (params.useBearing && provider.isBearingAvailable()) ? new FeatureState(true, true) : null;
		// peak frequency is roughly constant along a train, like amplitude -> first
		// difference.
		peakFrequency = (params.usePeakFreq && provider.isWaveformAvailable()) ? new FeatureState(false, false) : null;
		correlationActive = params.useCorrelation && provider.isWaveformAvailable();
	}

	@Override
	public void update(PamDataUnit newDataUnit, TrackBitSet<PamDataUnit> trackBitSet, int kcount) {

		BitSet bitSet = trackBitSet.trackBitSet;
		boolean included = bitSet.get(kcount - 1);

		IDIManager idiManager = provider.getIDIManager();
		AdaptiveCTParams params = provider.getParams();
		double floorMult = floorMultiplier(params.sensitivity);
		// the maximum bearing jump doubles as the bearing residual-scale floor (radians).
		double bearingFloor = floorMult
				* Math.toRadians(Math.max(params.maxBearingJumpDeg, MIN_BEARING_TOLERANCE_DEG));
		double coastCost = -Math.log(1.0 - clamp(params.detectionProb, 0.01, 0.999));

		if (included) {
			if (lastIncludedUnit != null) {
				double clickHuber = 0;
				int clickFeatures = 0;

				// raw interval between the previous included click and this one. If the
				// click arrives after a gap it may span several ICIs (some clicks were
				// missed); estimate how many and score the effective per-interval ICI so a
				// bridged click is not penalised as a giant ICI outlier.
				double rawICI = idiManager.calcTime(lastIncludedUnit, newDataUnit);
				double pred = predictNextICI();
				int nGap = 1;
				if (!Double.isNaN(pred) && pred > 0) {
					nGap = Math.max(1, (int) Math.round(rawICI / pred));
				}
				double ici = rawICI / nGap;
				// charge the missed clicks within the gap as coasts.
				totalScore += (nGap - 1) * coastCost;

				// ICI is always tracked (for prediction/coasting/junk) but only scored when
				// the ICI feature is enabled.
				double cICI = scoreICI(ici, floorMult);
				if (iciActive && !Double.isNaN(cICI)) {
					clickHuber += cICI;
					clickFeatures++;
				}

				if (amplitude != null) {
					double c = amplitude.score(newDataUnit.getAmplitudeDB(), floorMult * AMP_FLOOR_DB);
					if (!Double.isNaN(c)) {
						clickHuber += c;
						clickFeatures++;
					}
				}

				if (bearing != null) {
					Double b = getBearing(newDataUnit);
					if (b != null) {
						// capture the previous bearing before scoring updates it, so the jump
						// cutoff can compare consecutive absolute bearings.
						double prevBearing = bearing.lastValue();
						double c = bearing.score(b, bearingFloor);
						if (!Double.isNaN(c)) {
							clickHuber += c;
							clickFeatures++;
						}
						if (params.bearingJumpEnable && !Double.isNaN(prevBearing)
								&& BearingChi2VarParams.exceedsMaxJump(
										BearingChi2VarParams.signedBearingDiff(b, prevBearing),
										Math.toRadians(params.maxBearingJumpDeg), params.bearingJumpDrctn)) {
							bearingJumpViolated = true;
						}
					}
				}

				if (peakFrequency != null) {
					double pf = ClickFeatureUtils.getPeakFrequency(newDataUnit);
					if (!Double.isNaN(pf)) {
						double c = peakFrequency.score(pf, floorMult * params.peakFreqFloorHz);
						if (!Double.isNaN(c)) {
							clickHuber += c;
							clickFeatures++;
						}
					}
				}

				if (correlationActive) {
					double corr = idiManager.getCorrelationManager()
							.getCorrelationValue(lastIncludedUnit, newDataUnit).correlationValue;
					// absolute penalty: high correlation (~1) costs nothing, low correlation
					// (different waveform) costs a lot.
					double dev = -Math.log(clamp(corr, 1e-3, 1.0));
					double z = dev / (floorMult * CORR_LOG_SCALE);
					clickHuber += huber2(z);
					clickFeatures++;
				}

				// log-likelihood-ratio style cost: every explained detection earns the
				// INCLUDE_REWARD (favouring longer consistent trains and preventing
				// fragmentation); feature residuals are summed (not averaged) so any single
				// inconsistent feature can veto a click.
				double residualCost = 0.5 * clickHuber;
				totalScore += residualCost - INCLUDE_REWARD;
				if (clickFeatures > 0) {
					chi2Sum += clickHuber;
					contribCount += clickFeatures;
				}
			} else {
				// first click in the track - just seed the feature history
				if (amplitude != null) {
					amplitude.score(newDataUnit.getAmplitudeDB(), floorMult * AMP_FLOOR_DB);
				}
				if (bearing != null) {
					Double b = getBearing(newDataUnit);
					if (b != null) {
						bearing.score(b, bearingFloor);
					}
				}
				if (peakFrequency != null) {
					double pf = ClickFeatureUtils.getPeakFrequency(newDataUnit);
					if (!Double.isNaN(pf)) {
						peakFrequency.score(pf, floorMult * params.peakFreqFloorHz);
					}
				}
				// ICI and correlation both need a previous included click - nothing to seed.
			}
			includedCount++;
			lastIncludedUnit = newDataUnit;
		}

		// the number of coasts is the time gap between the last included click and the
		// newest detection in the mix, divided by the predicted ICI.
		this.nCoasts = calcNCoasts(newDataUnit, idiManager, params);

		// too short to be a track yet.
		if (includedCount < 2) {
			this.chi2 = MAX_CHI2;
			return;
		}

		// junk / aliased track gate (also stops a train whose bearing jumped too far).
		if (isJunk(params) || bearingJumpViolated) {
			this.chi2 = JUNK_CHI2 + totalScore;
			return;
		}

		this.chi2 = totalScore + nCoasts * coastCost;
	}

	/**
	 * Score an inter-click interval against the track's recent ICI history.
	 * <p>
	 * The residual is measured against a robust <i>linear trend</i> prediction of
	 * the next ICI (see {@link #predictNextICI()}), not simply the previous ICI.
	 * This means a steadily accelerating or decelerating train - most importantly a
	 * terminal buzz, where the ICI shrinks click by click - is treated as consistent
	 * (its residual about the trend is small) rather than being penalised for a
	 * non-zero first difference at every click. When there is too little history for
	 * a slope the predictor falls back to the previous ICI, so short trains behave
	 * exactly as before.
	 *
	 * @param ici       - the ICI in seconds.
	 * @param floorMult - the sensitivity-derived floor multiplier.
	 * @return the Huber-squared contribution, or NaN if this is the first ICI.
	 */
	private double scoreICI(double ici, double floorMult) {
		double pred = predictNextICI();
		if (Double.isNaN(pred)) {
			// first ICI in the track - nothing to predict against yet.
			pushICIWindow(ici);
			return Double.NaN;
		}
		double resid = ici - pred;
		// floor proportional to the (predicted) ICI magnitude (short ICI trains
		// tolerate proportionally less absolute jitter), plus a small absolute term.
		double floor = floorMult * (0.01 + 0.05 * pred);
		double scale = Math.max(floor, robustScale(iciResiduals, iciResCount));
		double z = resid / scale;

		pushICIResidual(resid);
		pushICIWindow(ici);
		return huber2(z);
	}

	/**
	 * Calculate the number of coasts - missed clicks at the end of the track.
	 */
	private int calcNCoasts(PamDataUnit newDataUnit, IDIManager idiManager, AdaptiveCTParams params) {
		if (lastIncludedUnit == null) {
			return 0;
		}
		double gap = idiManager.calcTime(lastIncludedUnit, newDataUnit);
		if (gap <= 0) {
			return 0;
		}
		double predicted = predictNextICI();
		if (Double.isNaN(predicted) || predicted <= 0) {
			predicted = params.maxICI; // fall back to absolute max ICI for single units.
		}
		return (int) Math.floor(gap / predicted);
	}

	/**
	 * Check whether the track looks like junk - either the central ICI is over the
	 * absolute maximum or there is a wildly inconsistent ICI (aliasing).
	 */
	private boolean isJunk(AdaptiveCTParams params) {
		double central = centralICI();
		if (Double.isNaN(central)) {
			return false;
		}
		if (central > params.maxICI) {
			return true;
		}
		// any single recent ICI well over the absolute maximum suggests aliasing.
		for (int i = 0; i < iciWinCount; i++) {
			if (iciWindow[i] > params.maxICI) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The central (typical) ICI of the track - the median of recent ICI values. Used
	 * for the absolute rate / aliasing gate, where a robust central value is wanted
	 * rather than a forward extrapolation.
	 */
	private double centralICI() {
		if (iciWinCount == 0) {
			return Double.NaN;
		}
		return median(iciWindow, iciWinCount);
	}

	/**
	 * Predict the next ICI from the track's recent history by robust linear
	 * extrapolation.
	 * <p>
	 * A Theil-Sen slope (the median of the pairwise slopes) is fitted to the recent
	 * ICI values against their index and extrapolated one step. This tracks a smooth
	 * trend - a lengthening or, for a buzz, a shortening ICI - while the median-based
	 * intercept and slope keep it robust to the odd outlier. With fewer than three
	 * ICIs there is no meaningful slope, so the most recent ICI is used (a
	 * random-walk prediction, matching the previous behaviour). The result is clamped
	 * to a sane band around the central ICI so a steep local trend cannot extrapolate
	 * to an absurd (or negative) interval.
	 *
	 * @return the predicted next ICI in seconds, or NaN if there is no history.
	 */
	private double predictNextICI() {
		if (iciWinCount == 0) {
			return Double.NaN;
		}
		if (iciWinCount < 3) {
			return iciWindow[iciWinCount - 1];
		}
		double slope = theilSenSlope(iciWindow, iciWinCount);
		double intercept = robustIntercept(iciWindow, iciWinCount, slope);
		double pred = intercept + slope * iciWinCount; // value at the next index.
		double med = median(iciWindow, iciWinCount);
		// keep the extrapolation sane: never below a quarter of, or above four times,
		// the central ICI.
		return clamp(pred, 0.25 * med, 4.0 * med);
	}

	/**
	 * Map the 0-1 sensitivity onto a floor multiplier. Higher sensitivity (tighter)
	 * gives a smaller floor and so a stricter test.
	 */
	private static double floorMultiplier(double sensitivity) {
		double s = clamp(sensitivity, 0, 1);
		return 2.0 - 1.5 * s; // s=0 -> 2.0 (loose), s=0.5 -> 1.25, s=1 -> 0.5 (tight)
	}

	private Double getBearing(PamDataUnit dataUnit) {
		if (dataUnit.getLocalisation() != null && dataUnit.getLocalisation().getAngles() != null
				&& dataUnit.getLocalisation().getAngles().length > 0) {
			return dataUnit.getLocalisation().getAngles()[0];
		}
		return null;
	}

	/* ---------------- ICI ring buffers ---------------- */

	private void pushICIResidual(double resid) {
		iciResCount = push(iciResiduals, iciResCount, resid);
	}

	private void pushICIWindow(double ici) {
		iciWinCount = push(iciWindow, iciWinCount, ici);
	}

	/* ---------------- MHTChi2 contract ---------------- */

	@Override
	public double getChi2() {
		return chi2;
	}

	@Override
	public double getChi2(int pruneback) {
		return chi2;
	}

	@Override
	public int getNCoasts() {
		return nCoasts;
	}

	@Override
	public MHTChi2<PamDataUnit> cloneMHTChi2() {
		try {
			AdaptiveCTChi2 cloned = (AdaptiveCTChi2) super.clone();
			cloned.iciResiduals = Arrays.copyOf(iciResiduals, iciResiduals.length);
			cloned.iciWindow = Arrays.copyOf(iciWindow, iciWindow.length);
			cloned.amplitude = amplitude == null ? null : amplitude.copy();
			cloned.bearing = bearing == null ? null : bearing.copy();
			cloned.peakFrequency = peakFrequency == null ? null : peakFrequency.copy();
			return cloned;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void clear() {
		chi2 = MAX_CHI2;
		nCoasts = 0;
		bearingJumpViolated = false;
		includedCount = 0;
		chi2Sum = 0;
		contribCount = 0;
		totalScore = 0;
		lastIncludedUnit = null;
		iciResCount = 0;
		iciWinCount = 0;
		buildFeatures();
	}

	@Override
	public void clearKernelGarbage(int newRefIndex) {
		// Nothing to do - the IDIManager is trimmed in the provider.
	}

	@Override
	public CTAlgorithmInfo getMHTChi2Info() {
		double meanChi2 = contribCount > 0 ? chi2Sum / contribCount : 0;
		return new AdaptiveCTInfo(meanChi2, nCoasts, activeFeatureNames());
	}

	private String[] activeFeatureNames() {
		java.util.ArrayList<String> names = new java.util.ArrayList<>();
		if (iciActive) {
			names.add("ICI");
		}
		if (amplitude != null) {
			names.add("Amplitude");
		}
		if (bearing != null) {
			names.add("Bearing");
		}
		if (peakFrequency != null) {
			names.add("Peak frequency");
		}
		if (correlationActive) {
			names.add("Correlation");
		}
		return names.toArray(new String[0]);
	}

	@Override
	public void printSettings() {
		System.out.println("AdaptiveCTChi2: sensitivity=" + provider.getParams().sensitivity + " maxICI="
				+ provider.getParams().maxICI + " detectionProb=" + provider.getParams().detectionProb
				+ " bearing=" + (bearing != null));
	}

	/* ---------------- static numeric helpers ---------------- */

	/**
	 * Push a value into a fixed-length ring of recent values, shifting older values
	 * out once full. Most-recent value is at index count-1 once full.
	 */
	private static int push(double[] buffer, int count, double value) {
		if (count < buffer.length) {
			buffer[count] = value;
			return count + 1;
		}
		System.arraycopy(buffer, 1, buffer, 0, buffer.length - 1);
		buffer[buffer.length - 1] = value;
		return count;
	}

	/**
	 * A robust scale estimate (1.4826 * median absolute deviation) of the first
	 * {@code count} entries. Returns 0 if there is not enough data, in which case
	 * the caller's floor is used.
	 */
	private static double robustScale(double[] values, int count) {
		if (count < 3) {
			return 0;
		}
		double med = median(values, count);
		double[] dev = new double[count];
		for (int i = 0; i < count; i++) {
			dev[i] = Math.abs(values[i] - med);
		}
		return 1.4826 * median(dev, count);
	}

	/**
	 * The Theil-Sen robust slope of the first {@code count} values against their
	 * index (0, 1, 2, ...): the median of the slopes of every pair of points.
	 * Returns 0 if there are fewer than two points.
	 */
	private static double theilSenSlope(double[] values, int count) {
		if (count < 2) {
			return 0;
		}
		double[] slopes = new double[count * (count - 1) / 2];
		int n = 0;
		for (int i = 0; i < count; i++) {
			for (int j = i + 1; j < count; j++) {
				slopes[n++] = (values[j] - values[i]) / (j - i);
			}
		}
		return median(slopes, n);
	}

	/**
	 * The robust intercept that pairs with a Theil-Sen {@code slope}: the median of
	 * {@code value[i] - slope * i} over the first {@code count} values.
	 */
	private static double robustIntercept(double[] values, int count, double slope) {
		double[] resid = new double[count];
		for (int i = 0; i < count; i++) {
			resid[i] = values[i] - slope * i;
		}
		return median(resid, count);
	}

	private static double median(double[] values, int count) {
		double[] copy = Arrays.copyOf(values, count);
		Arrays.sort(copy);
		int mid = count / 2;
		if (count % 2 == 0) {
			return (copy[mid - 1] + copy[mid]) / 2.0;
		}
		return copy[mid];
	}

	private static double huber2(double z) {
		double a = Math.abs(z);
		if (a <= HUBER_C) {
			return z * z;
		}
		return HUBER_C * (2 * a - HUBER_C);
	}

	private static double clamp(double v, double min, double max) {
		return Math.max(min, Math.min(max, v));
	}

	/**
	 * Per-feature predictive residual state.
	 * <p>
	 * A feature can be scored on its <i>first difference</i> (suitable for a
	 * roughly-constant, slowly-changing quantity such as amplitude) or on its
	 * <i>second difference</i> - the change in the rate of change (suitable for
	 * bearing, which may sweep rapidly but smoothly). The second-difference model
	 * makes a smooth sweep free and penalises only off-trajectory jumps, and its
	 * robust self-calibrating scale automatically tolerates data whose bearings are
	 * consistently noisy (e.g. poor bearing measurements on narrowband species)
	 * while still rejecting a single jump in an otherwise clean track.
	 */
	private static class FeatureState {

		/** Treat the value as a circular quantity (bearing). */
		private final boolean circular;
		/** Score the second difference (trajectory deviation) rather than the first. */
		private final boolean secondDifference;

		private double lastValue = Double.NaN;
		private double lastDelta = Double.NaN;
		private double[] residuals = new double[WINDOW];
		private int count = 0;

		FeatureState(boolean circular, boolean secondDifference) {
			this.circular = circular;
			this.secondDifference = secondDifference;
		}

		/**
		 * Score a new feature value against recent history.
		 *
		 * @param value - the observed value (radians for bearing).
		 * @param floor - the minimum residual scale (sensitivity-derived).
		 * @return the Huber-squared contribution, or NaN while still seeding history.
		 */
		double score(double value, double floor) {
			if (Double.isNaN(lastValue)) {
				lastValue = value;
				return Double.NaN;
			}
			double delta = circular ? circularDiff(value, lastValue) : value - lastValue;

			double resid;
			if (secondDifference) {
				if (Double.isNaN(lastDelta)) {
					// need two intervals before a second difference can be formed.
					lastDelta = delta;
					lastValue = value;
					return Double.NaN;
				}
				// deviation from the straight-line extrapolation of the last two values.
				resid = delta - lastDelta;
				lastDelta = delta;
			} else {
				resid = delta;
			}

			double learned = robustScale(residuals, count);
			double scale;
			if (secondDifference) {
				// Cap how far the learned scale can grow above the floor. Without this, a
				// pathological bearing pattern (e.g. a track that hops between two animals,
				// giving an alternating jump) would self-calibrate a large scale and tolerate
				// the very jumps bearing is meant to reject. The cap keeps bearing a strong
				// separation cue while still allowing some adaptivity for genuinely
				// manoeuvring animals.
				scale = Math.max(floor, Math.min(learned, SECOND_DIFF_SCALE_CAP * floor));
			} else {
				scale = Math.max(floor, learned);
			}
			count = push(residuals, count, resid);
			lastValue = value;

			double z = resid / scale;
			return huber2(z);
		}

		/**
		 * The most recent value scored (radians for bearing), or {@code NaN} if no
		 * value has been seen yet. Read before {@link #score(double, double)} to obtain
		 * the previous value for a consecutive-difference test.
		 */
		double lastValue() {
			return lastValue;
		}

		FeatureState copy() {
			FeatureState c = new FeatureState(circular, secondDifference);
			c.lastValue = lastValue;
			c.lastDelta = lastDelta;
			c.residuals = Arrays.copyOf(residuals, residuals.length);
			c.count = count;
			return c;
		}

		private static double circularDiff(double a, double b) {
			double d = a - b;
			return Math.atan2(Math.sin(d), Math.cos(d));
		}
	}

}
