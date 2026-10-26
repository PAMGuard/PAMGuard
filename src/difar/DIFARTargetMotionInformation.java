package difar;

import java.util.ArrayList;
import java.util.Map;

import javax.vecmath.Point3f;

import Array.ArrayManager;
import Array.PamArray;
import GPS.GpsData;
import PamUtils.LatLong;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import pamMaths.PamQuaternion;
import pamMaths.PamVector;
import difar.targetmotion.TargetMotionInformation;

public class DIFARTargetMotionInformation implements TargetMotionInformation {

	private DifarProcess difarProcess;
	
	private ArrayList<PamDataUnit> difarDataUnits;

	private GpsData systemOrigin;
	
	private PamVector[] origins;

	private PamVector[][] worldVectors;
	
	private PamQuaternion[] eulerAngles;
	
	private ArrayList<ArrayList<Point3f>> hydrophonePos = new ArrayList<>();
	
	private double[] meanPos = new double[2];

	/** Default speed of sound, used if no array is available. */
	private static final double DEFAULT_SPEED_OF_SOUND = 1500.;

	/**
	 * Timing error of a single detection, in seconds. Detection times are the
	 * start of a manually marked clip, so this is coarse. Cross correlation of
	 * the clips would give a much smaller value.
	 */
	private double timingErrorSeconds = 1.0;

	/** Pairwise delays between buoys, in one row. Built on first use. */
	private ArrayList<ArrayList<Double>> timeDelays;

	/** Errors on those delays, in the same order. */
	private ArrayList<ArrayList<Double>> timeDelayErrors;

	/** Buoy positions in metres, in one row, matching the delays. */
	private ArrayList<ArrayList<double[]>> delayHydrophonePositions;

	/**
	 * Arrival times measured by correlation, in milliseconds, for the
	 * detections that have one; null if none were measured.
	 */
	private Map<PamDataUnit, Double> arrivalMillis;

	/** Timing error of a delay measured by correlation, in seconds. */
	private double correlationErrorSeconds;

	/** For each delay, in the order of the delays, whether both ends were measured by correlation. */
	private ArrayList<Boolean> correlatedDelays;

	private double speedOfSound = getArraySpeedOfSound();
	
	public DIFARTargetMotionInformation(DifarProcess difarProcess,
			ArrayList<PamDataUnit> difarDataUnits) {
		super();
		this.difarProcess = difarProcess;
		this.difarDataUnits = difarDataUnits;
		// need to work out quite a few things here about origins, etc. 
		// a lot of stuff will be relative to the first data unit. 
		systemOrigin = difarDataUnits.get(0).getOriginLatLong(false);
		origins = new PamVector[difarDataUnits.size()];
		worldVectors = new PamVector[difarDataUnits.size()][];
		eulerAngles = new PamQuaternion[difarDataUnits.size()];
		for (int i = 0; i < difarDataUnits.size(); i++) {
			PamDataUnit aUnit = difarDataUnits.get(i);
			GpsData anOrigin = aUnit.getOriginLatLong(false);
			origins[i] = new PamVector(systemOrigin.distanceToMetresX(anOrigin), systemOrigin.distanceToMetresY(anOrigin), 0);
			meanPos[0] += origins[i].getElement(0);
			meanPos[1] += origins[i].getElement(1);
			worldVectors[i] = aUnit.getLocalisation().getWorldVectors();
//			double[] angles = aUnit.getLocalisation().getAngles();
			eulerAngles[i] = new PamQuaternion(anOrigin.getHeading(), anOrigin.getPitch(), anOrigin.getRoll());
			ArrayList<Point3f> hPos = new ArrayList<>();
			hPos.add(new Point3f((float)origins[i].getElement(0),(float)origins[i].getElement(1),(float)origins[i].getElement(2)));
			hydrophonePos.add(hPos);
		}
		meanPos[0] /= difarDataUnits.size();
		meanPos[1] /= difarDataUnits.size();
	}

	/**
	 * 
	 * @return the mean position of all buoys in x,y coordinates. 
	 */
	public double[] getMeanPosition() {
		return meanPos;
	}
	
	@Override
	public ArrayList<PamDataUnit> getCurrentDetections() {
		return difarDataUnits;
	}

	@Override
	public int getNDetections() {
		return difarDataUnits.size();
	}

	@Override
	public ArrayList<ArrayList<Double>> getTimeDelays() {
		if (timeDelays == null) {
			calculateTimeDelays();
		}
		return timeDelays;
	}

	@Override
	public ArrayList<ArrayList<Double>> getTimeDelayErrors() {
		if (timeDelayErrors == null) {
			calculateTimeDelays();
		}
		return timeDelayErrors;
	}

	@Override
	public ArrayList<ArrayList<double[]>> getDelayHydrophonePositions() {
		if (delayHydrophonePositions == null) {
			calculateTimeDelays();
		}
		return delayHydrophonePositions;
	}

	/**
	 * Build the pairwise time delays between buoys, their errors, and the buoy
	 * positions those delays refer to.
	 * <p>
	 * All buoys of a group are recorded on one device, so their detection times
	 * share a clock and their differences are true arrival time differences. The
	 * delays are ordered by PamUtils.indexM1() and indexM2(), the convention
	 * used by Chi2TimeDelays, and each delay is the later hydrophone's arrival
	 * time minus the earlier one's.
	 * <p>
	 * Buoy positions come from the detection origins, which are also the origins
	 * of the bearings, so both terms of a fit describe the same buoys. Positions
	 * are two dimensional, with a height of zero.
	 */
	private void calculateTimeDelays() {
		int nUnits = difarDataUnits.size();
		ArrayList<double[]> buoyPositions = new ArrayList<>();
		for (int i = 0; i < nUnits; i++) {
			buoyPositions.add(new double[] {
					origins[i].getElement(0), origins[i].getElement(1), 0});
		}
		ArrayList<Integer> indexM1 = PamUtils.indexM1(nUnits);
		ArrayList<Integer> indexM2 = PamUtils.indexM2(nUnits);
		ArrayList<Double> delays = new ArrayList<>();
		ArrayList<Double> errors = new ArrayList<>();
		correlatedDelays = new ArrayList<>();
		for (int j = 0; j < indexM1.size(); j++) {
			PamDataUnit u1 = difarDataUnits.get(indexM1.get(j));
			PamDataUnit u2 = difarDataUnits.get(indexM2.get(j));
			delays.add((arrivalMillis(u2) - arrivalMillis(u1)) / 1000.);
			double e1 = arrivalErrorSeconds(u1);
			double e2 = arrivalErrorSeconds(u2);
			errors.add(Math.sqrt(e1 * e1 + e2 * e2));
			correlatedDelays.add(isCorrelated(u1) && isCorrelated(u2));
		}
		timeDelays = new ArrayList<>();
		timeDelays.add(delays);
		timeDelayErrors = new ArrayList<>();
		timeDelayErrors.add(errors);
		delayHydrophonePositions = new ArrayList<>();
		delayHydrophonePositions.add(buoyPositions);
	}

	/**
	 * Use arrival times measured by correlation in place of detection times.
	 * <p>
	 * Each correlated arrival time is the seed's time plus the delay measured
	 * between the seed and that detection, so the seed is in the map with its
	 * own time. A delay between two correlated detections then has the
	 * correlation timing error; a delay involving a detection without one has
	 * mostly the detection timing error. Each correlated arrival is given an
	 * error of the correlation error over root two, so that a delay between
	 * two of them has the correlation error.
	 * @param arrivalMillis arrival times in milliseconds, for the detections
	 * that have one. Null for none.
	 * @param correlationErrorSeconds timing error of a delay measured by
	 * correlation, in seconds.
	 */
	public void setArrivalTimes(Map<PamDataUnit, Double> arrivalMillis, double correlationErrorSeconds) {
		this.arrivalMillis = arrivalMillis;
		this.correlationErrorSeconds = correlationErrorSeconds;
		timeDelays = null;
		timeDelayErrors = null;
		delayHydrophonePositions = null;
		correlatedDelays = null;
	}

	/**
	 * @return for each delay, in the order of {@link #getTimeDelays()}, whether
	 * both of its detections had arrival times measured by correlation.
	 */
	public ArrayList<Boolean> getCorrelatedDelays() {
		if (correlatedDelays == null) {
			calculateTimeDelays();
		}
		return correlatedDelays;
	}

	/** @return true if a detection has an arrival time measured by correlation. */
	private boolean isCorrelated(PamDataUnit unit) {
		return arrivalMillis != null && arrivalMillis.containsKey(unit);
	}

	/** @return a detection's arrival time in milliseconds, by correlation if measured. */
	private double arrivalMillis(PamDataUnit unit) {
		return isCorrelated(unit) ? arrivalMillis.get(unit) : unit.getTimeMilliseconds();
	}

	/** @return the error of a detection's arrival time, in seconds. */
	private double arrivalErrorSeconds(PamDataUnit unit) {
		return isCorrelated(unit) ? correlationErrorSeconds / Math.sqrt(2.) : timingErrorSeconds;
	}

	/**
	 * @return the timing error of a single detection, in seconds.
	 */
	public double getTimingErrorSeconds() {
		return timingErrorSeconds;
	}

	/**
	 * Set the timing error of a single detection, in seconds. The error on a
	 * delay between two detections is this value times the square root of two.
	 * @param timingErrorSeconds timing error in seconds.
	 */
	public void setTimingErrorSeconds(double timingErrorSeconds) {
		this.timingErrorSeconds = timingErrorSeconds;
		timeDelays = null;
		timeDelayErrors = null;
		delayHydrophonePositions = null;
		correlatedDelays = null;
	}

	@Override
	public double getSpeedOfSound() {
		return speedOfSound;
	}

	/**
	 * Set the speed of sound in metres per second. Mostly for testing, since
	 * the value normally comes from the Array Manager.
	 * @param speedOfSound speed of sound in metres per second.
	 */
	public void setSpeedOfSound(double speedOfSound) {
		this.speedOfSound = speedOfSound;
	}

	/**
	 * @return the array's speed of sound, or a default if no array is set up.
	 */
	private static double getArraySpeedOfSound() {
		try {
			PamArray array = ArrayManager.getArrayManager().getCurrentArray();
			if (array != null) {
				return array.getSpeedOfSound();
			}
		} catch (Exception e) {
			// no array available, e.g. in a test. Fall through to the default.
		}
		return DEFAULT_SPEED_OF_SOUND;
	}

	@Override
	public PamVector[] getOrigins() {
		return origins;
	}

	@Override
	public PamVector[][] getWorldVectors() {
		return worldVectors;
	}

	@Override
	public PamQuaternion[] getEulerAngles() {
		return eulerAngles;
	}

	@Override
	public ArrayList<ArrayList<Point3f>> getHydrophonePos() {
		return hydrophonePos;
	}

	@Override
	public LatLong getGPSReference() {
		return systemOrigin;
	}

	@Override
	public PamVector latLongToMetres(LatLong ll) {
		return new PamVector(systemOrigin.distanceToMetresX(ll), systemOrigin.distanceToMetresY(ll), 0);
	}

	@Override
	public LatLong metresToLatLong(PamVector pt) {
		return systemOrigin.addDistanceMeters(pt.getElement(0), pt.getElement(1));
	}

	@Override
	public int getReferenceHydrophones() {
		return difarDataUnits.get(0).getChannelBitmap();
	}

	@Override
	public Long getTimeMillis() {
		return difarDataUnits.get(0).getTimeMilliseconds();
	}

	@Override
	public GpsData getBeamLatLong(LatLong localised) {
		// TODO Auto-generated method stub
		return new GpsData(localised);
	}

	@Override
	public long getBeamTime(GpsData beamPos) {
		// TODO Auto-generated method stub
		return 0;
	}


}
