package test.difar;

import java.util.ArrayList;

import GPS.GpsData;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;
import difar.DIFARTargetMotionInformation;
import difar.SonobuoyHistory;
import difar.SonobuoyRecord;
import pamMaths.PamVector;

/**
 * Builds DIFAR localisation inputs for a known geometry, so that tests can
 * check the result against the answer.
 * <p>
 * Buoys and sources are placed on a flat local grid in metres. Each detection
 * reports the true bearing from its buoy to its source, and a time set by the
 * travel time between them. Bearing and timing errors can be added to any
 * detection. Each detection names its own source, so a group can be built from
 * two animals, which is what a wrong match looks like.
 * <p>
 * Detections can also be built against a sonobuoy history, where each one
 * carries the magnetic DIFAR angle it measured, and takes its buoy position and
 * compass correction from the record in force at its time. Changing a record
 * then changes the bearings, as it does in DIFAR, so a test can calibrate a
 * buoy and check what happens to the localisation.
 * <p>
 * Only the data units are fakes. The localisation itself is the real thing, so
 * no PamController and no audio are needed.
 */
public class DifarTestScenario {

	/** Reference position, in the Southern Ocean. */
	public static final LatLong REF = new LatLong(-60.0, 140.0);

	/** Speed of sound used by the tests, in metres per second. */
	public static final double SOUND_SPEED = 1500.;

	/** Bearing standard deviation reported by the fake detections, in degrees. */
	public static final double BEARING_SD_DEG = 5.0;

	/** Time the first source called, in milliseconds. */
	private static final long CALL_TIME = 1_000_000L;

	private final ArrayList<PamDataUnit> units = new ArrayList<>();

	private int nextChannel = 0;

	/**
	 * Add a detection of a source on a buoy.
	 * @param buoy buoy position in metres, as {x east, y north}
	 * @param source source position in metres
	 * @param bearingErrorDeg error added to the true bearing, in degrees
	 * @param timingErrorSeconds error added to the arrival time, in seconds
	 * @return this scenario, so calls can be chained.
	 */
	public DifarTestScenario addDetection(double[] buoy, double[] source,
			double bearingErrorDeg, double timingErrorSeconds) {
		double dx = source[0] - buoy[0];
		double dy = source[1] - buoy[1];
		double bearingDeg = Math.toDegrees(Math.atan2(dx, dy)) + bearingErrorDeg;
		double arrival = travelTime(buoy, source) + timingErrorSeconds;
		long timeMillis = CALL_TIME + Math.round(arrival * 1000.);
		units.add(new FakeBuoyUnit(timeMillis, nextChannel++, toLatLong(buoy), bearingDeg));
		return this;
	}

	/**
	 * A buoy record for a position on the grid.
	 * @param channel channel the buoy is on.
	 * @param buoy buoy position in metres, as {x east, y north}
	 * @param headingDeg compass correction, in degrees.
	 * @param timeMillis time the record comes into force.
	 * @return the record.
	 */
	public static SonobuoyRecord buoyRecord(int channel, double[] buoy, double headingDeg,
			long timeMillis) {
		LatLong position = toLatLong(buoy);
		return new SonobuoyRecord(channel, timeMillis, null, "buoy " + channel,
				position.getLatitude(), position.getLongitude(), headingDeg);
	}

	/**
	 * Add a detection that reads its buoy from a sonobuoy history.
	 * <p>
	 * The detection keeps the magnetic angle it measured, which is the true
	 * bearing less the buoy's compass correction at the time. Its position and
	 * bearing are worked out from whatever record is in force when they are
	 * asked for, so calibrating the buoy afterwards moves the bearing.
	 * @param history the history the detection reads.
	 * @param channel channel the detection was made on.
	 * @param buoy buoy position in metres, as {x east, y north}
	 * @param source source position in metres
	 * @param bearingErrorDeg error added to the measured angle, in degrees
	 * @param timingErrorSeconds error added to the arrival time, in seconds
	 * @return this scenario, so calls can be chained.
	 */
	public DifarTestScenario addCalibratedDetection(SonobuoyHistory history, int channel,
			double[] buoy, double[] source, double bearingErrorDeg, double timingErrorSeconds) {
		double dx = source[0] - buoy[0];
		double dy = source[1] - buoy[1];
		double trueBearingDeg = Math.toDegrees(Math.atan2(dx, dy)) + bearingErrorDeg;
		double arrival = travelTime(buoy, source) + timingErrorSeconds;
		long timeMillis = CALL_TIME + Math.round(arrival * 1000.);
		SonobuoyRecord record = history.getRecordAt(channel, timeMillis);
		double headingDeg = record == null || record.getHeading() == null ? 0 : record.getHeading();
		units.add(new CalibratedBuoyUnit(timeMillis, channel, history, trueBearingDeg - headingDeg));
		return this;
	}

	/**
	 * @return the detections added so far, in the order they were added.
	 */
	public ArrayList<PamDataUnit> getUnits() {
		return units;
	}

	/**
	 * @return the last detection added.
	 */
	public PamDataUnit getLastUnit() {
		return units.get(units.size() - 1);
	}

	/**
	 * @param timingErrorSeconds the timing error the fit should assume, in
	 * seconds. This is what the fit is told, which need not match the errors
	 * that were actually added.
	 * @return the localisation inputs for the detections added so far.
	 */
	public DIFARTargetMotionInformation build(double timingErrorSeconds) {
		DIFARTargetMotionInformation info = new DIFARTargetMotionInformation(null, units);
		info.setSpeedOfSound(SOUND_SPEED);
		info.setTimingErrorSeconds(timingErrorSeconds);
		return info;
	}

	/** @return travel time between two points, in seconds. */
	public static double travelTime(double[] from, double[] to) {
		return Math.hypot(to[0] - from[0], to[1] - from[1]) / SOUND_SPEED;
	}

	/** @return a position in metres as a latitude and longitude. */
	public static LatLong toLatLong(double[] metres) {
		return REF.addDistanceMeters(metres[0], metres[1]);
	}

	/** @return how far a latitude and longitude is from a position in metres. */
	public static double distanceFrom(LatLong latLong, double[] metres) {
		double x = REF.distanceToMetresX(latLong);
		double y = REF.distanceToMetresY(latLong);
		return Math.hypot(x - metres[0], y - metres[1]);
	}

	/** A data unit on one buoy, with a fixed position and one bearing. */
	private static class FakeBuoyUnit extends PamDataUnit {

		private final GpsData origin;

		FakeBuoyUnit(long timeMillis, int channel, LatLong position, double bearingDeg) {
			super(timeMillis);
			setChannelBitmap(1 << channel);
			origin = new GpsData(position);
			setLocalisation(new FakeBearing(this, bearingDeg));
		}

		@Override
		public GpsData getOriginLatLong(boolean recalculate) {
			return origin;
		}
	}

	/**
	 * A detection that takes its buoy position and compass correction from a
	 * sonobuoy history, and keeps only the magnetic angle it measured.
	 */
	private static class CalibratedBuoyUnit extends PamDataUnit {

		private final SonobuoyHistory history;

		private final int channel;

		private final double difarAngleDeg;

		CalibratedBuoyUnit(long timeMillis, int channel, SonobuoyHistory history, double difarAngleDeg) {
			super(timeMillis);
			this.channel = channel;
			this.history = history;
			this.difarAngleDeg = difarAngleDeg;
			setChannelBitmap(1 << channel);
			setLocalisation(new CalibratedBearing(this));
		}

		/** @return the buoy record in force when this detection was made, or null. */
		private SonobuoyRecord getRecord() {
			return history.getRecordAt(channel, getTimeMilliseconds());
		}

		/** @return the true bearing, the measured angle plus the compass correction. */
		double getTrueBearingDeg() {
			SonobuoyRecord record = getRecord();
			Double heading = record == null ? null : record.getHeading();
			return difarAngleDeg + (heading == null ? 0 : heading);
		}

		@Override
		public GpsData getOriginLatLong(boolean recalculate) {
			SonobuoyRecord record = getRecord();
			if (record == null || !record.hasPosition()) {
				return null;
			}
			return new GpsData(new LatLong(record.getLatitude(), record.getLongitude()));
		}
	}

	/** The bearing of a detection that reads its buoy from the history. */
	private static class CalibratedBearing extends AbstractLocalisation {

		private final CalibratedBuoyUnit unit;

		CalibratedBearing(CalibratedBuoyUnit unit) {
			super(unit, LocContents.HAS_BEARING, 0);
			this.unit = unit;
		}

		@Override
		public boolean bearingAmbiguity() {
			return false;
		}

		@Override
		public double[] getAngles() {
			return new double[] {Math.toRadians(unit.getTrueBearingDeg())};
		}

		@Override
		public double[] getAngleErrors() {
			return new double[] {Math.toRadians(BEARING_SD_DEG)};
		}

		@Override
		public PamVector[] getWorldVectors() {
			double radians = Math.toRadians(90 - unit.getTrueBearingDeg());
			return new PamVector[] {new PamVector(Math.cos(radians), Math.sin(radians), 0)};
		}
	}

	/** A single bearing with a fixed error, shaped like DifarLocalisation. */
	private static class FakeBearing extends AbstractLocalisation {

		private final double bearingDeg;

		FakeBearing(PamDataUnit unit, double bearingDeg) {
			super(unit, LocContents.HAS_BEARING, 0);
			this.bearingDeg = bearingDeg;
		}

		@Override
		public boolean bearingAmbiguity() {
			return false;
		}

		@Override
		public double[] getAngles() {
			return new double[] {Math.toRadians(bearingDeg)};
		}

		@Override
		public double[] getAngleErrors() {
			return new double[] {Math.toRadians(BEARING_SD_DEG)};
		}

		@Override
		public PamVector[] getWorldVectors() {
			double radians = Math.toRadians(90 - bearingDeg);
			return new PamVector[] {new PamVector(Math.cos(radians), Math.sin(radians), 0)};
		}
	}
}
