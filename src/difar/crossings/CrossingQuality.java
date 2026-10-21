package difar.crossings;

import java.util.List;

import PamUtils.LatLong;

/**
 * How far a crossing's location can be trusted, from its geometry alone.
 * Pure arithmetic: no PAMGuard state, so it can be checked by hand.
 * <p>
 * Two measures, both flags rather than rules (see "Crossing quality" in
 * crossings_design.md):
 * <ul>
 * <li>The crossing angle: the largest angle between any two of its bearings,
 * from 0 to 180 degrees, not folded. Near 0 the bearings point the same way
 * and the location is unreliable. Near 180 they point at each other, and the
 * time delay places the whale between the buoys.</li>
 * <li>On a buoy: the location lies within a radius of one of the crossing's
 * own buoys. One buoy's bearing then runs through another buoy, where that
 * buoy's own bearing has no meaning, and the fit settles on the buoy with an
 * error near zero that reflects none of the real sources of error.</li>
 * </ul>
 */
public final class CrossingQuality {

	/** One clip's contribution: where its buoy was, and its true bearing. */
	public static final class Bearing {
		private final LatLong origin;
		private final double degrees;

		/**
		 * @param origin the buoy's position when the clip was made, or null if unknown
		 * @param degrees the clip's true bearing, in degrees
		 */
		public Bearing(LatLong origin, double degrees) {
			this.origin = origin;
			this.degrees = degrees;
		}
	}

	private final double angle;
	private final boolean onBuoy;

	private CrossingQuality(double angle, boolean onBuoy) {
		this.angle = angle;
		this.onBuoy = onBuoy;
	}

	/**
	 * Assess a crossing.
	 * @param bearings the bearings of its clips
	 * @param location where they cross, or null if they do not
	 * @param onBuoyRadius metres from a buoy within which a crossing is on it
	 * @return the assessment
	 */
	public static CrossingQuality of(List<Bearing> bearings, LatLong location, double onBuoyRadius) {
		double largest = Double.NaN;
		for (int i = 0; i < bearings.size(); i++) {
			for (int j = i + 1; j < bearings.size(); j++) {
				double a = angleBetween(bearings.get(i).degrees, bearings.get(j).degrees);
				if (Double.isNaN(largest) || a > largest) {
					largest = a;
				}
			}
		}
		boolean near = false;
		if (location != null) {
			for (Bearing b : bearings) {
				if (b.origin != null && location.distanceToMetres(b.origin) <= onBuoyRadius) {
					near = true;
					break;
				}
			}
		}
		return new CrossingQuality(largest, near);
	}

	/**
	 * The angle between two bearings, 0 to 180 degrees: 0 when they point the
	 * same way, 180 when they point opposite ways.
	 */
	public static double angleBetween(double a, double b) {
		if (Double.isNaN(a) || Double.isNaN(b)) {
			return Double.NaN;
		}
		double d = Math.abs(a - b) % 360.;
		return d > 180. ? 360. - d : d;
	}

	/** @return the largest angle between any two bearings, degrees, or NaN if unknown */
	public double getAngle() {
		return angle;
	}

	/** @return true if the crossing lies within the radius of one of its buoys */
	public boolean isOnBuoy() {
		return onBuoy;
	}
}
