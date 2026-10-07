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

	private DifarLocalisationResiduals(double maxBearingErrorDegrees, double maxTimeDelayErrorSeconds) {
		this.maxBearingErrorDegrees = maxBearingErrorDegrees;
		this.maxTimeDelayErrorSeconds = maxTimeDelayErrorSeconds;
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
			return new DifarLocalisationResiduals(Double.NaN, Double.NaN);
		}
		PamVector source = info.latLongToMetres(location);
		return new DifarLocalisationResiduals(maxBearingError(info, source), maxTimeDelayError(info, source));
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
	 * delay from the fitted position, in seconds, or NaN if there were no
	 * delays.
	 */
	private static double maxTimeDelayError(TargetMotionInformation info, PamVector source) {
		ArrayList<ArrayList<Double>> measured = info.getTimeDelays();
		ArrayList<ArrayList<double[]>> positions = info.getDelayHydrophonePositions();
		double speedOfSound = info.getSpeedOfSound();
		if (measured == null || positions == null || speedOfSound <= 0) {
			return Double.NaN;
		}
		double[] sourcePos = new double[] {source.getElement(0), source.getElement(1), 0};
		ArrayList<ArrayList<Double>> predicted =
				Chi2TimeDelays.calcTimeDelays(sourcePos, positions, speedOfSound);
		double worst = Double.NaN;
		for (int k = 0; k < measured.size() && k < predicted.size(); k++) {
			for (int m = 0; m < measured.get(k).size() && m < predicted.get(k).size(); m++) {
				Double delay = measured.get(k).get(m);
				if (delay == null || delay.isNaN()) {
					continue;
				}
				double error = Math.abs(delay - predicted.get(k).get(m));
				if (Double.isNaN(worst) || error > worst) {
					worst = error;
				}
			}
		}
		return worst;
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
