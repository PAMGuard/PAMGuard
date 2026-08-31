package clickTrainDetector.clickTrainAlgorithms.ukf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import PamguardMVC.PamDataUnit;

/**
 * Generates labelled training examples from manually-annotated click trains and
 * trains the UKF affinity network ({@link AffinityNN}) on them.
 * <p>
 * Each annotated event is a known-true click train. Running a {@link ClickTrack}
 * through an event produces, at every step, a <b>positive</b> example (the
 * feature vector for the true next click, which should be associated) and a set
 * of <b>negative</b> examples (clicks from other events that fall within the
 * association gate at that moment and should <i>not</i> be associated). The
 * feature vectors are exactly those used during tracking
 * ({@link UKFTracker#associationFeatures}), so the trained network sees the same
 * inputs at training and inference time.
 *
 * @author Jamie Macaulay
 */
@SuppressWarnings("rawtypes")
public class AffinityTrainer {

	public static final int DEFAULT_HIDDEN = 8;
	public static final int DEFAULT_EPOCHS = 400;
	private static final double DEFAULT_LR = 0.05;
	private static final double DEFAULT_MOMENTUM = 0.9;
	private static final long SEED = 42;

	/**
	 * Per-feature, per-example probability that a maskable feature (amplitude,
	 * bearing, correlation, peak frequency) is dropped during training, so the
	 * trained network still produces sensible affinities when that feature is
	 * disabled at inference. See {@link UKFTracker#maskableFeatures()}.
	 */
	public static final double DEFAULT_DROPOUT = 0.15;

	/** Maximum number of distractor (negative) examples generated per click step. */
	private static final int MAX_NEG_PER_STEP = 3;

	/** Progress callback for the (possibly long) generate + train run. */
	public interface ProgressListener {
		void onProgress(double fraction, String message);
	}

	/** The outcome of a training run. */
	public static class Result {
		public final AffinityNN network;
		public final int nPositive;
		public final int nNegative;
		public final double loss;
		public final double accuracy;

		Result(AffinityNN network, int nPositive, int nNegative, double loss, double accuracy) {
			this.network = network;
			this.nPositive = nPositive;
			this.nNegative = nNegative;
			this.loss = loss;
			this.accuracy = accuracy;
		}
	}

	/** A generated dataset of feature rows and labels. */
	public static class Dataset {
		public final double[][] x;
		public final double[] y;
		public final int nPositive;
		public final int nNegative;

		Dataset(double[][] x, double[] y, int nPositive, int nNegative) {
			this.x = x;
			this.y = y;
			this.nPositive = nPositive;
			this.nNegative = nNegative;
		}
	}

	private AffinityTrainer() {
	}

	/**
	 * Generate examples, train the network and (optionally) write it to JSON.
	 *
	 * @param events           - each list is one annotated train's clicks.
	 * @param params           - the UKF parameters.
	 * @param bearingAvailable - whether the clicks carry bearing.
	 * @param hidden           - hidden layer size.
	 * @param epochs           - training epochs.
	 * @param listener         - optional progress callback.
	 * @param out              - JSON file to write, or null to skip.
	 */
	public static Result trainAndExport(List<List<PamDataUnit>> events, UKFParams params, boolean bearingAvailable,
			int hidden, int epochs, ProgressListener listener, File out) throws IOException {
		if (listener != null) {
			listener.onProgress(0.0, "Generating training examples…");
		}
		Dataset data = generateExamples(events, params, bearingAvailable);
		if (data.x.length == 0) {
			throw new IOException("No training examples could be generated from the selected events.");
		}

		AffinityMLPTrainer trainer = new AffinityMLPTrainer(UKFTracker.FEATURE_DIM, hidden);
		trainer.train(data.x, data.y, epochs, DEFAULT_LR, DEFAULT_MOMENTUM, SEED, UKFTracker.maskableFeatures(),
				DEFAULT_DROPOUT, UKFTracker.ABSENT, (epoch, total, loss) -> {
			if (listener != null && (epoch % 10 == 0 || epoch == total)) {
				listener.onProgress(0.1 + 0.9 * epoch / total,
						String.format("Training… epoch %d/%d, loss %.4f", epoch, total, loss));
			}
		});

		AffinityNN network = trainer.toAffinityNN();
		double accuracy = trainer.accuracy(data.x, data.y);

		if (out != null) {
			trainer.writeJson(out);
			// validate the written file round-trips.
			AffinityNN.fromFile(out, UKFTracker.FEATURE_DIM);
		}
		if (listener != null) {
			listener.onProgress(1.0, String.format("Done: %d positive, %d negative examples, accuracy %.1f%%",
					data.nPositive, data.nNegative, accuracy * 100));
		}
		return new Result(network, data.nPositive, data.nNegative, trainer.getLastLoss(), accuracy);
	}

	/**
	 * Build the labelled dataset from the annotated events.
	 */
	public static Dataset generateExamples(List<List<PamDataUnit>> events, UKFParams params, boolean bearingAvailable) {
		TrackStateModel model = new TrackStateModel(params, params.useAmplitude, params.useBearing && bearingAvailable);

		// global time-sorted index of every click, tagged with its event, for distractors.
		List<Tagged> allClicks = new ArrayList<>();
		for (int e = 0; e < events.size(); e++) {
			for (PamDataUnit click : events.get(e)) {
				allClicks.add(new Tagged(e, click, UKFTracker.timeSeconds(click)));
			}
		}
		allClicks.sort(Comparator.comparingDouble(c -> c.time));

		List<double[]> features = new ArrayList<>();
		List<Double> labels = new ArrayList<>();
		int nPos = 0;
		int nNeg = 0;

		for (int e = 0; e < events.size(); e++) {
			List<PamDataUnit> clicks = sortedByTime(events.get(e));
			if (clicks.size() < 2) {
				continue;
			}
			PamDataUnit first = clicks.get(0);
			ClickTrack track = new ClickTrack(0, model, params, first, UKFTracker.timeSeconds(first),
					first.getAmplitudeDB(), UKFTracker.bearingOf(first));

			for (int i = 1; i < clicks.size(); i++) {
				PamDataUnit trueNext = clicks.get(i);
				double t = UKFTracker.timeSeconds(trueNext);
				double ici = t - track.getLastTimeSeconds();
				if (ici <= 0 || ici > params.maxICI) {
					// gap or out of order - advance the track but do not make an example.
					advance(track, trueNext, t);
					continue;
				}

				// positive: the true continuation.
				double[] zPos = track.measurement(t, trueNext.getAmplitudeDB(), UKFTracker.bearingOf(trueNext));
				features.add(UKFTracker.associationFeatures(track, trueNext, zPos, t, params));
				labels.add(1.0);
				nPos++;

				// negatives: distractor clicks from other events within the gate.
				int added = 0;
				for (Tagged d : clicksInWindow(allClicks, track.getLastTimeSeconds(), track.getLastTimeSeconds()
						+ params.maxICI)) {
					if (d.event == e) {
						continue; // same event - not a distractor
					}
					double[] zNeg = track.measurement(d.time, d.click.getAmplitudeDB(),
							UKFTracker.bearingOf(d.click));
					features.add(UKFTracker.associationFeatures(track, d.click, zNeg, d.time, params));
					labels.add(0.0);
					nNeg++;
					if (++added >= MAX_NEG_PER_STEP) {
						break;
					}
				}

				advance(track, trueNext, t);
			}
		}

		double[][] x = features.toArray(new double[0][]);
		double[] y = new double[labels.size()];
		for (int i = 0; i < y.length; i++) {
			y[i] = labels.get(i);
		}
		return new Dataset(x, y, nPos, nNeg);
	}

	private static void advance(ClickTrack track, PamDataUnit click, double t) {
		double[] z = track.measurement(t, click.getAmplitudeDB(), UKFTracker.bearingOf(click));
		track.predict();
		track.update(z, click, t);
	}

	private static List<PamDataUnit> sortedByTime(List<PamDataUnit> clicks) {
		List<PamDataUnit> copy = new ArrayList<>(clicks);
		copy.sort(Comparator.comparingDouble(UKFTracker::timeSeconds));
		return copy;
	}

	/** Clicks whose time falls in (from, to]. allClicks must be time-sorted. */
	private static List<Tagged> clicksInWindow(List<Tagged> allClicks, double from, double to) {
		List<Tagged> out = new ArrayList<>();
		for (Tagged c : allClicks) {
			if (c.time > to) {
				break;
			}
			if (c.time > from) {
				out.add(c);
			}
		}
		return out;
	}

	private static class Tagged {
		final int event;
		final PamDataUnit click;
		final double time;

		Tagged(int event, PamDataUnit click, double time) {
			this.event = event;
			this.click = click;
			this.time = time;
		}
	}

}
