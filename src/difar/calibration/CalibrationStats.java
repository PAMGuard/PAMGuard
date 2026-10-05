package difar.calibration;

/**
 * Statistics for a set of compass calibration angles.
 * <p>
 * A pure class with no PAMGuard dependencies, so it can be tested alone.
 */
public final class CalibrationStats {

	private CalibrationStats() {
	}

	/**
	 * The circular mean of the angles within a window around a centre. Used
	 * with the centre of the histogram's fullest bin, so clips that disagree
	 * are ignored, as with the mode, but the result is not tied to bin
	 * centres.
	 * @param angles angles in degrees, in any range.
	 * @param centre centre of the window, in degrees.
	 * @param halfWidth half the width of the window, in degrees.
	 * @return the mean in degrees, in (-180, 180], or NaN if no angle is in
	 * the window.
	 */
	public static double meanNear(double[] angles, double centre, double halfWidth) {
		double x = 0, y = 0;
		int n = 0;
		for (double a : angles) {
			if (Math.abs(difference(a, centre)) > halfWidth) {
				continue;
			}
			x += Math.cos(Math.toRadians(a));
			y += Math.sin(Math.toRadians(a));
			n++;
		}
		if (n == 0) {
			return Double.NaN;
		}
		return Math.toDegrees(Math.atan2(y, x));
	}

	/**
	 * @return a minus b, in degrees, in [-180, 180).
	 */
	static double difference(double a, double b) {
		double d = (a - b) % 360.;
		if (d < -180.) {
			d += 360.;
		}
		else if (d >= 180.) {
			d -= 360.;
		}
		return d;
	}
}
