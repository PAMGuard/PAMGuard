package difar;

import java.util.ArrayList;

import Localiser.algorithms.genericLocaliser.Chi2TimeDelays;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;
import difar.targetmotion.TargetMotionInformation;
import pamMaths.PamVector;

/**
 * How far a localisation sits from the measurements it was fitted to.
 * <p>
 * Chi2 answers how unlikely a fit is. These residuals answer how wrong it is,
 * in degrees and seconds, which is easier to picture and is the same quantity
 * whether two buoys were used or three.
 */
public class DifarLocalisationResiduals {

	private final double maxBearingErrorDegrees;

	private final double maxTimeDelayErrorSeconds;

	/** Largest error of a delay measured by correlation at both ends, or NaN if none. */
	private final double maxCorrelatedDelayErrorSeconds;

	/** Largest error of a delay taken from detection times, or NaN if none. */
	private final double maxDetectionDelayErrorSeconds;

	private DifarLocalisationResiduals(double maxBearingErrorDegrees, double[] delayErrors) {
		this.maxBearingErrorDegrees = maxBearingErrorDegrees;
		this.maxTimeDelayErrorSeconds = delayErrors[0];
		this.maxCorrelatedDelayErrorSeconds = delayErrors[1];
		this.maxDetectionDelayErrorSeconds = delayErrors[2];
	}

	/**
	 * Compare the measurements with the ones a source at the given location
	 * would have produced.
	 * @param info the detections and buoy positions used for the fit.
	 * @param location the fitted source position.
	 * @return the largest bearing and time delay errors. Either is NaN if that
	 * measurement was not available.
	 */
	public static DifarLocalisationResiduals calculate(TargetMotionInformation info, LatLong location) {
		if (info == null || location == null) {
			return new DifarLocalisationResiduals(Double.NaN, new double[] {Double.NaN, Double.NaN, Double.NaN});
		}
		PamVector source = info.latLongToMetres(location);
		return new DifarLocalisationResiduals(maxBearingError(info, source), maxTimeDelayErrors(info, source));
	}

	/**
	 * @return the largest difference between a measured bearing and the bearing
	 * to the fitted position, in degrees, or NaN if there were no bearings.
	 */
	private static double maxBearingError(TargetMotionInformation info, PamVector source) {
		ArrayList<PamDataUnit> detections = info.getCurrentDetections();
		PamVector[] origins = info.getOrigins();
		if (detections == null || origins == null) {
			return Double.NaN;
		}
		double worst = Double.NaN;
		for (int i = 0; i < detections.size() && i < origins.length; i++) {
			if (detections.get(i).getLocalisation() == null) {
				continue;
			}
			double[] angles = detections.get(i).getLocalisation().getAngles();
			if (angles == null || angles.length < 1) {
				continue;
			}
			double dx = source.getElement(0) - origins[i].getElement(0);
			double dy = source.getElement(1) - origins[i].getElement(1);
			double predicted = Math.toDegrees(Math.atan2(dx, dy));
			double error = Math.abs(wrapDegrees(Math.toDegrees(angles[0]) - predicted));
			if (Double.isNaN(worst) || error > worst) {
				worst = error;
			}
		}
		return worst;
	}

	/**
	 * @return the largest difference between a measured time delay and the
	 * delay from the fitted position, in seconds: over all delays, over the
	 * delays measured by correlation, and over the delays from detection
	 * times. Each is NaN if there were no such delays.
	 */
	private static double[] maxTimeDelayErrors(TargetMotionInformation info, PamVector source) {
		double[] worst = {Double.NaN, Double.NaN, Double.NaN};
		ArrayList<ArrayList<Double>> measured = info.getTimeDelays();
		ArrayList<ArrayList<double[]>> positions = info.getDelayHydrophonePositions();
		double speedOfSound = info.getSpeedOfSound();
		if (measured == null || positions == null || speedOfSound <= 0) {
			return worst;
		}
		ArrayList<Boolean> correlated = info instanceof DIFARTargetMotionInformation
				? ((DIFARTargetMotionInformation) info).getCorrelatedDelays() : null;
		double[] sourcePos = new double[] {source.getElement(0), source.getElement(1), 0};
		ArrayList<ArrayList<Double>> predicted =
				Chi2TimeDelays.calcTimeDelays(sourcePos, positions, speedOfSound);
		for (int k = 0; k < measured.size() && k < predicted.size(); k++) {
			for (int m = 0; m < measured.get(k).size() && m < predicted.get(k).size(); m++) {
				Double delay = measured.get(k).get(m);
				if (delay == null || delay.isNaN()) {
					continue;
				}
				double error = Math.abs(delay - predicted.get(k).get(m));
				// DIFAR delays are all in one row, so m indexes the flags
				boolean byCorrelation = correlated != null && m < correlated.size() && correlated.get(m);
				worst[0] = larger(worst[0], error);
				if (byCorrelation) {
					worst[1] = larger(worst[1], error);
				}
				else {
					worst[2] = larger(worst[2], error);
				}
			}
		}
		return worst;
	}

	private static double larger(double worst, double error) {
		return Double.isNaN(worst) || error > worst ? error : worst;
	}

	/**
	 * @param degrees an angle difference
	 * @return the same difference, between -180 and 180 degrees.
	 */
	private static double wrapDegrees(double degrees) {
		double wrapped = degrees % 360.;
		if (wrapped > 180.) {
			wrapped -= 360.;
		}
		if (wrapped < -180.) {
			wrapped += 360.;
		}
		return wrapped;
	}

	/**
	 * @return the largest bearing error in degrees, or NaN if unavailable.
	 */
	public double getMaxBearingErrorDegrees() {
		return maxBearingErrorDegrees;
	}

	/**
	 * @return the largest time delay error in seconds, or NaN if unavailable.
	 */
	public double getMaxTimeDelayErrorSeconds() {
		return maxTimeDelayErrorSeconds;
	}

	/**
	 * @return the largest error of a time delay measured by correlation at
	 * both ends, in seconds, or NaN if there were none.
	 */
	public double getMaxCorrelatedDelayErrorSeconds() {
		return maxCorrelatedDelayErrorSeconds;
	}

	/**
	 * @return the largest error of a time delay taken from detection times,
	 * in seconds, or NaN if there were none.
	 */
	public double getMaxDetectionDelayErrorSeconds() {
		return maxDetectionDelayErrorSeconds;
	}

	/**
	 * Check the residuals against the largest errors that are acceptable. A
	 * measurement that was not available cannot fail.
	 * @param maxBearingDegrees largest acceptable bearing error, in degrees.
	 * @param maxDelaySeconds largest acceptable time delay error, in seconds.
	 * @return true if the fit is close enough to its measurements.
	 */
	public boolean isWithin(double maxBearingDegrees, double maxDelaySeconds) {
		if (!Double.isNaN(maxBearingErrorDegrees) && maxBearingErrorDegrees > maxBearingDegrees) {
			return false;
		}
		if (!Double.isNaN(maxTimeDelayErrorSeconds) && maxTimeDelayErrorSeconds > maxDelaySeconds) {
			return false;
		}
		return true;
	}
}
