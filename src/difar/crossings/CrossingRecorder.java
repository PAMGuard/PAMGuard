package difar.crossings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SubdetectionInfo;
import difar.DIFARCrossingInfo;
import difar.DifarControl;
import difar.DifarDataUnit;
import difar.DifarMatchSelector;
import difar.DifarParameters;
import difar.DifarProcess;
import difar.targetmotion.TargetMotionResult;

/**
 * Turns the crossing made for a clip into a {@link DifarCrossing} when the
 * clip is saved.
 * <p>
 * A clip belongs to one crossing. The new crossing takes its clips from any
 * earlier crossings. An earlier crossing left with two or more clips is
 * recalculated from them, unless the settings say to delete it; one left
 * with fewer is deleted. Which crossings are affected, and what each keeps,
 * is decided by {@link CrossingSets}.
 * <p>
 * The crossing a clip was given is still held on the clip as a
 * {@link DIFARCrossingInfo}, which displays and logging use for now. This
 * class only adds the crossing units alongside.
 */
public class CrossingRecorder {

	/** A crossing needs two bearings. */
	private static final int MIN_CLIPS = 2;

	private final DifarProcess difarProcess;
	private final DifarControl difarControl;

	/** How each clip's current match was chosen, until the clip is saved or dropped. */
	private final Map<DifarDataUnit, DifarCrossing.MatchChoice> choices = new WeakHashMap<>();

	public CrossingRecorder(DifarProcess difarProcess, DifarControl difarControl) {
		this.difarProcess = difarProcess;
		this.difarControl = difarControl;
	}

	/**
	 * Note how a clip's match was chosen, each time a match is applied to it.
	 * @param clip the clip being matched.
	 * @param choice how, or null if the clip was left with no match.
	 */
	public synchronized void noteChoice(DifarDataUnit clip, DifarCrossing.MatchChoice choice) {
		if (choice == null) {
			choices.remove(clip);
		}
		else {
			choices.put(clip, choice);
		}
	}

	/**
	 * Forget a clip saved without its crossing, or deleted.
	 * @param clip the clip.
	 */
	public synchronized void forget(DifarDataUnit clip) {
		choices.remove(clip);
	}

	/**
	 * Record the crossing of a clip just saved, if it has one.
	 * @param clip the clip, already in the saved clips, with its UID.
	 */
	public synchronized void record(DifarDataUnit clip) {
		DifarCrossing.MatchChoice choice = choices.remove(clip);
		DIFARCrossingInfo info = clip.getDifarCrossing();
		if (info == null || info.getCrossLocation() == null) {
			return;
		}
		List<DifarDataUnit> clips = savedClips(info);
		if (clips.size() < MIN_CLIPS) {
			return;
		}

		Set<Long> newUIDs = uids(clips);
		Map<DifarCrossing, Set<Long>> existing = new LinkedHashMap<>();
		for (DifarDataUnit c : clips) {
			DifarCrossing old = (DifarCrossing) c.getSuperDetection(DifarCrossing.class);
			if (old != null && !existing.containsKey(old)) {
				existing.put(old, childUIDs(old));
			}
		}
		Map<DifarCrossing, Set<Long>> affected = CrossingSets.remainders(newUIDs, existing);

		Double[] errors = info.getErrors();
		DifarCrossing crossing = new DifarCrossing(clips, info.getCrossLocation(),
				error(errors, 0), error(errors, 1), choice == null ? DifarCrossing.MatchChoice.AUTO : choice);
		// adding the clips has taken them out of their earlier crossings
		DifarCrossingDataBlock block = difarProcess.getCrossingDataBlock();
		block.addPamData(crossing);

		DifarParameters params = difarControl.getDifarParameters();
		for (Map.Entry<DifarCrossing, Set<Long>> hit : affected.entrySet()) {
			DifarCrossing old = hit.getKey();
			if (hit.getValue().size() >= MIN_CLIPS && !params.alwaysDeleteTrimmedCrossings
					&& recalculate(old)) {
				block.updatePamData(old, System.currentTimeMillis());
			}
			else {
				old.removeAllSubDetections();
				block.remove(old, true);
			}
		}
	}

	/**
	 * Cross the clips a crossing still holds, and store the result on it.
	 * @return false if they could not be crossed, so it should be deleted.
	 */
	private boolean recalculate(DifarCrossing crossing) {
		List<PamDataUnit> clips = new ArrayList<>();
		for (PamDataUnit<?, ?> clip : crossing.getSubDetections()) {
			clips.add(clip);
		}
		if (clips.size() < MIN_CLIPS) {
			return false;
		}
		DifarParameters params = difarControl.getDifarParameters();
		DifarMatchSelector selector = new DifarMatchSelector(difarProcess,
				params.detectionTimingError, params.maxBearingResidual, params.maxTimeDelayResidual);
		DifarMatchSelector.Match match = selector.localise(clips);
		if (match == null) {
			System.out.printf("DIFAR: crossing UID %d could not be recalculated after losing clips, so it is deleted\n",
					crossing.getUID());
			return false;
		}
		if (!match.isAccepted()) {
			System.out.printf("DIFAR: crossing UID %d recalculated after losing clips, but %s\n",
					crossing.getUID(), match.getRejectReason());
		}
		TargetMotionResult result = match.getResult();
		Double[] errors = result.getErrors();
		LatLong location = result.getLatLong();
		crossing.setResult(location, error(errors, 0), error(errors, 1));
		return true;
	}

	/**
	 * The clips of a crossing that are saved. Partners always are; the clip
	 * itself has just been saved.
	 */
	private List<DifarDataUnit> savedClips(DIFARCrossingInfo info) {
		List<DifarDataUnit> clips = new ArrayList<>();
		DifarDataUnit[] matched = info.getMatchedUnits();
		if (matched == null) {
			return clips;
		}
		for (DifarDataUnit c : matched) {
			if (c != null && c.getParentDataBlock() == difarProcess.getProcessedDifarData() && !clips.contains(c)) {
				clips.add(c);
			}
		}
		return clips;
	}

	private static Set<Long> uids(List<DifarDataUnit> clips) {
		Set<Long> set = new HashSet<>();
		for (DifarDataUnit c : clips) {
			set.add(c.getUID());
		}
		return set;
	}

	private static Set<Long> childUIDs(DifarCrossing crossing) {
		Set<Long> set = new HashSet<>();
		for (Object info : crossing.getSubDetectionInfo()) {
			set.add(((SubdetectionInfo<?>) info).getChildUID());
		}
		return set;
	}

	private static double error(Double[] errors, int i) {
		if (errors == null || errors.length <= i || errors[i] == null) {
			return Double.NaN;
		}
		return errors[i];
	}
}
