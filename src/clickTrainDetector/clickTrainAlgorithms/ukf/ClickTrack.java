package clickTrainDetector.clickTrainAlgorithms.ukf;

import java.util.ArrayList;

import PamguardMVC.PamDataUnit;

/**
 * A single click train track maintained by the UKF tracker.
 *
 * @author Jamie Macaulay
 */
public class ClickTrack {

	private final int id;
	private final TrackStateModel model;
	private final UnscentedKalmanFilter ukf;

	/** Number of clicks needed to promote this track from tentative to confirmed. */
	private final int confirmHits;

	/** Time (seconds) of the last click assigned to this track. */
	private double lastTimeSeconds;

	/** Number of consecutive frames with no assigned click. */
	private int coast = 0;

	/**
	 * Clicks belonging to this track, in time order. May be shared with a copied
	 * track (see {@link #clicksShared}) until one of them appends, at which point it
	 * is copied - so branching a hypothesis for N-scan search is cheap.
	 */
	private ArrayList<PamDataUnit> clicks = new ArrayList<>();

	/**
	 * Whether {@link #clicks} is currently shared with another (copied) track and so
	 * must be copied before the next append.
	 */
	private boolean clicksShared = false;

	public ClickTrack(int id, TrackStateModel model, UKFParams params, PamDataUnit firstClick, double timeSeconds,
			double amp, double bearing) {
		this.id = id;
		this.model = model;
		this.confirmHits = Math.max(2, params.confirmHits);
		double logICI0 = Math.log(Math.max(params.maxICI * 0.5, 1e-3));
		double[] x0 = model.initialState(logICI0, amp, bearing);
		double[][] p0 = model.initialCovariance();
		this.ukf = new UnscentedKalmanFilter(model, x0, p0);
		this.lastTimeSeconds = timeSeconds;
		this.clicks.add(firstClick);
	}

	/**
	 * Copy constructor - clones the UKF state and shares the click list copy-on-write.
	 */
	private ClickTrack(ClickTrack src) {
		this.id = src.id;
		this.model = src.model;
		this.confirmHits = src.confirmHits;
		this.ukf = src.ukf.copy();
		this.lastTimeSeconds = src.lastTimeSeconds;
		this.coast = src.coast;
		// share the click list; both tracks flag it shared so whichever appends first
		// makes its own copy.
		this.clicks = src.clicks;
		this.clicksShared = true;
		src.clicksShared = true;
	}

	/**
	 * An independent copy of this track for multi-hypothesis (N-scan) branching. The
	 * UKF state is cloned; the click list is shared copy-on-write.
	 */
	public ClickTrack copy() {
		return new ClickTrack(this);
	}

	/** UKF prediction step. */
	public void predict() {
		ukf.predict();
	}

	/** The time (seconds) at which the next click is expected. */
	public double predictedNextTime() {
		return lastTimeSeconds + Math.exp(model.logICI(ukf.getState()));
	}

	/** The currently expected ICI (seconds). */
	public double expectedICI() {
		return Math.exp(model.logICI(ukf.getState()));
	}

	/**
	 * Mahalanobis distance of a candidate measurement against this track's current
	 * prediction.
	 */
	public double mahalanobis(double[] z) {
		return ukf.mahalanobis(z);
	}

	/** The measurement the current state expects (for the next click). */
	public double[] measurementPrediction() {
		return ukf.predictedMeasurement();
	}

	/**
	 * Snapshot the predicted measurement and inverse innovation covariance once, to
	 * be reused (via {@link #mahalanobis(double[], UnscentedKalmanFilter.Prediction)})
	 * against many candidate detections in the same frame.
	 */
	public UnscentedKalmanFilter.Prediction predictMeasurement() {
		return ukf.predictMeasurement();
	}

	/** Mahalanobis distance of a candidate against a precomputed prediction. */
	public double mahalanobis(double[] z, UnscentedKalmanFilter.Prediction prediction) {
		return ukf.mahalanobis(z, prediction);
	}

	/** Build a measurement vector for a candidate click. */
	public double[] measurement(double timeSeconds, double amp, double bearing) {
		double ici = timeSeconds - lastTimeSeconds;
		return model.measurement(ici, amp, bearing);
	}

	/** Associate a click with this track and run the UKF update. */
	public void update(double[] z, PamDataUnit click, double timeSeconds) {
		ukf.update(z);
		lastTimeSeconds = timeSeconds;
		coast = 0;
		if (clicksShared) {
			// copy-on-write: we are about to diverge from any track we were copied from.
			clicks = new ArrayList<>(clicks);
			clicksShared = false;
		}
		clicks.add(click);
	}

	/**
	 * Coast the track forward through one missed click: advance the expected time by
	 * one ICI and run a UKF prediction (which inflates the uncertainty). Used to
	 * bridge gaps so a click resuming after a few missed detections still
	 * associates at a normal ICI.
	 */
	public void coastForward() {
		double ici = expectedICI();
		ukf.predict();
		lastTimeSeconds += ici;
		coast++;
	}

	/** Increment the coast (missed-click) counter. */
	public void coast() {
		coast++;
	}

	public int getCoast() {
		return coast;
	}

	/**
	 * Whether this track is confirmed - i.e. it has accumulated at least
	 * {@code confirmHits} clicks. Tentative (unconfirmed) tracks are only offered
	 * detections after every confirmed track has been given the chance to claim one.
	 */
	public boolean isConfirmed() {
		return clicks.size() >= confirmHits;
	}

	public double getLastTimeSeconds() {
		return lastTimeSeconds;
	}

	public ArrayList<PamDataUnit> getClicks() {
		return clicks;
	}

	public int size() {
		return clicks.size();
	}

	public int getId() {
		return id;
	}

	public TrackStateModel getModel() {
		return model;
	}

}
