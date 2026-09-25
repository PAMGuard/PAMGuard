package difar;

import java.util.ArrayList;
import java.util.List;

import PamguardMVC.PamDataUnit;
import difar.targetmotion.Simplex2D;
import difar.targetmotion.TargetMotionResult;

/**
 * Chooses which detections on other buoys are the same call as a given one.
 * <p>
 * Each buoy may have several detections that could be the call: same species,
 * overlapping in frequency, and close enough in time given how far apart the
 * buoys are. This class localises every combination of them, throws away the
 * ones whose residuals are too large, and returns the best of what is left.
 * <p>
 * The best is the largest group that passes, with ties broken by chi2 per
 * degree of freedom. A buoy therefore joins a group only if it is consistent
 * with it, and among equally sized groups the closer fit wins.
 * <p>
 * Ranking groups of different sizes by any goodness-of-fit measure, chi2 per
 * degree of freedom included, does not work, because a pair and a triplet are
 * fitted to different measurements. A well marked pair fits almost perfectly,
 * so such a rule would prefer pairs over triplets. The principled answer is the
 * one used for data association in multi target tracking: compare the
 * likelihood of the measurements if they came from one source against the
 * likelihood if they came from separate sources. Every detection then
 * contributes its own factor, so groups of different sizes can be compared.
 * That is worth doing, and is what this rule approximates.
 * <p>
 * This class does not read data blocks, so its behaviour can be tested with
 * detections built by hand.
 */
public class DifarMatchSelector {

	/** A group of detections, localised, and whether it was accepted. */
	public static class Match {

		private final List<PamDataUnit> units;

		private final TargetMotionResult result;

		private final DifarLocalisationResiduals residuals;

		private final String rejectReason;

		private Match(List<PamDataUnit> units, TargetMotionResult result,
				DifarLocalisationResiduals residuals, String rejectReason) {
			this.units = units;
			this.result = result;
			this.residuals = residuals;
			this.rejectReason = rejectReason;
		}

		/**
		 * @return true if this group passed the residual limits. Only accepted
		 * groups are used, but the rejected ones are kept so that a display can
		 * show what else was considered.
		 */
		public boolean isAccepted() {
			return rejectReason == null;
		}

		/**
		 * @return why this group was not used, in a form fit to show a user, or
		 * null if it was accepted.
		 */
		public String getRejectReason() {
			return rejectReason;
		}

		/** @return the detections in this group, the seed first. */
		public List<PamDataUnit> getUnits() {
			return units;
		}

		/** @return the localisation of the group. */
		public TargetMotionResult getResult() {
			return result;
		}

		/** @return how far the localisation sits from the measurements. */
		public DifarLocalisationResiduals getResiduals() {
			return residuals;
		}

		/**
		 * @return chi2 per degree of freedom, or NaN if it cannot be worked
		 * out. Only meaningful between groups of the same size.
		 */
		public double getChi2PerDegreeOfFreedom() {
			if (result == null || result.getChi2() == null
					|| result.getnDegreesFreedom() == null || result.getnDegreesFreedom() < 1) {
				return Double.NaN;
			}
			return result.getChi2() / result.getnDegreesFreedom();
		}
	}

	private final DifarProcess difarProcess;

	private final double timingErrorSeconds;

	private final double maxBearingResidual;

	private final double maxTimeDelayResidual;

	private final Simplex2D simplex = new Simplex2D();

	/**
	 * @param difarProcess the process the localisations belong to. May be null
	 * outside a running configuration.
	 * @param timingErrorSeconds timing error of one detection, in seconds.
	 * @param maxBearingResidual largest acceptable bearing residual, in degrees.
	 * @param maxTimeDelayResidual largest acceptable timing residual, in seconds.
	 */
	public DifarMatchSelector(DifarProcess difarProcess, double timingErrorSeconds,
			double maxBearingResidual, double maxTimeDelayResidual) {
		this.difarProcess = difarProcess;
		this.timingErrorSeconds = timingErrorSeconds;
		this.maxBearingResidual = maxBearingResidual;
		this.maxTimeDelayResidual = maxTimeDelayResidual;
	}

	/**
	 * Find the best group of detections that includes the seed.
	 * @param seed the detection being matched.
	 * @param candidatesByBuoy candidate detections, one list per other buoy.
	 * Lists may be empty. Order within a list does not matter.
	 * @return the best match, or null if no group passed the limits.
	 */
	public Match select(PamDataUnit seed, List<List<PamDataUnit>> candidatesByBuoy) {
		return chooseMatch(selectAll(seed, candidatesByBuoy));
	}

	/**
	 * The match to use from a ranked list: the first one accepted. Everything
	 * that picks or shows the chosen match goes through here, so they agree.
	 * @param ranked matches as ranked by selectAll(), or null.
	 * @return the match to use, or null if none was accepted.
	 */
	public static Match chooseMatch(List<Match> ranked) {
		if (ranked == null) {
			return null;
		}
		for (Match match : ranked) {
			if (match.isAccepted()) {
				return match;
			}
		}
		return null;
	}

	/**
	 * Whether a detection on another buoy could be the same call, judged on
	 * timing alone.
	 * <p>
	 * A sound cannot arrive at two buoys further apart in time than it takes to
	 * travel between them. Clips are also marked by hand, so each start time
	 * can be out by up to the slack. Uses clip start times, as the localisation
	 * does. A detection outside this window would always fail the timing
	 * residual, so leaving it out changes no result, but it stops impossible
	 * candidates crowding out real ones.
	 * @param seedMillis start time of the detection being matched.
	 * @param otherMillis start time of the detection on the other buoy.
	 * @param travelMillis travel time between the two buoys, in milliseconds.
	 * @param slackMillis allowance for marking error, in milliseconds.
	 * @return true if the other detection is close enough in time.
	 */
	public static boolean couldBeSameCall(long seedMillis, long otherMillis,
			long travelMillis, long slackMillis) {
		return Math.abs(otherMillis - seedMillis) <= travelMillis + slackMillis;
	}

	/**
	 * Localise every group that includes the seed, and rank them.
	 * <p>
	 * Accepted groups come first, best first, so the first entry is the match
	 * that would be used. Rejected groups follow in the same order, each
	 * carrying the reason it was not used. Nothing is discarded, so a display
	 * can show a user what else was considered and why it lost.
	 * @param seed the detection being matched.
	 * @param candidatesByBuoy candidate detections, one list per other buoy.
	 * @return every group tried, ranked. Empty if there was nothing to try.
	 */
	public List<Match> selectAll(PamDataUnit seed, List<List<PamDataUnit>> candidatesByBuoy) {
		List<Match> matches = new ArrayList<>();
		if (seed == null || candidatesByBuoy == null) {
			return matches;
		}
		for (List<PamDataUnit> group : combinations(seed, candidatesByBuoy)) {
			Match match = localise(group);
			if (match != null) {
				matches.add(match);
			}
		}
		sortBest(matches);
		return matches;
	}

	/**
	 * Sort matches so that the one to use comes first: accepted before
	 * rejected, then larger groups, then the closer fit.
	 * @param matches the matches to sort, in place.
	 */
	private void sortBest(List<Match> matches) {
		matches.sort((a, b) -> {
			if (a.isAccepted() != b.isAccepted()) {
				return a.isAccepted() ? -1 : 1;
			}
			if (a.getUnits().size() != b.getUnits().size()) {
				return b.getUnits().size() - a.getUnits().size();
			}
			double chiA = a.getChi2PerDegreeOfFreedom();
			double chiB = b.getChi2PerDegreeOfFreedom();
			if (Double.isNaN(chiA) || Double.isNaN(chiB)) {
				return Double.isNaN(chiA) ? (Double.isNaN(chiB) ? 0 : 1) : -1;
			}
			return Double.compare(chiA, chiB);
		});
	}

	/**
	 * Every group that contains the seed and at most one detection from each
	 * other buoy, and that has at least two detections in total.
	 * @param seed the detection being matched.
	 * @param candidatesByBuoy candidate detections, one list per other buoy.
	 * @return the groups to try.
	 */
	private List<List<PamDataUnit>> combinations(PamDataUnit seed,
			List<List<PamDataUnit>> candidatesByBuoy) {
		List<List<PamDataUnit>> groups = new ArrayList<>();
		List<PamDataUnit> start = new ArrayList<>();
		start.add(seed);
		groups.add(start);
		for (List<PamDataUnit> buoyCandidates : candidatesByBuoy) {
			if (buoyCandidates == null || buoyCandidates.isEmpty()) {
				continue;
			}
			List<List<PamDataUnit>> grown = new ArrayList<>(groups);
			for (List<PamDataUnit> group : groups) {
				for (PamDataUnit candidate : buoyCandidates) {
					List<PamDataUnit> longer = new ArrayList<>(group);
					longer.add(candidate);
					grown.add(longer);
				}
			}
			groups = grown;
		}
		List<List<PamDataUnit>> usable = new ArrayList<>();
		for (List<PamDataUnit> group : groups) {
			if (group.size() > 1) {
				usable.add(group);
			}
		}
		return usable;
	}

	/**
	 * Localise one group of detections. Also used to recalculate a crossing
	 * that has lost clips.
	 * @param group the detections, the seed first.
	 * @return the localised group, or null if the fit failed.
	 */
	public Match localise(List<PamDataUnit> group) {
		ArrayList<PamDataUnit> units = new ArrayList<>(group);
		DIFARTargetMotionInformation info = new DIFARTargetMotionInformation(difarProcess, units);
		info.setTimingErrorSeconds(timingErrorSeconds);
		simplex.setStartPoint(info.getMeanPosition());
		TargetMotionResult[] results = simplex.runModel(info);
		if (results == null || results.length != 1 || results[0] == null
				|| results[0].getLatLong() == null) {
			return null;
		}
		DifarLocalisationResiduals residuals =
				DifarLocalisationResiduals.calculate(info, results[0].getLatLong());
		return new Match(units, results[0], residuals, rejectReason(residuals));
	}

	/**
	 * @param residuals how far a fit sits from its measurements.
	 * @return why the fit should not be used, in a form fit to show a user, or
	 * null if it is close enough.
	 */
	private String rejectReason(DifarLocalisationResiduals residuals) {
		double bearing = residuals.getMaxBearingErrorDegrees();
		if (!Double.isNaN(bearing) && bearing > maxBearingResidual) {
			return String.format("bearing out by %.1f deg, limit %.1f", bearing, maxBearingResidual);
		}
		double timing = residuals.getMaxTimeDelayErrorSeconds();
		if (!Double.isNaN(timing) && timing > maxTimeDelayResidual) {
			return String.format("timing out by %.1f s, limit %.1f", timing, maxTimeDelayResidual);
		}
		return null;
	}
}
