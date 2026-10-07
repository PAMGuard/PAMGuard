package test.difar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import difar.calibration.CalibrationStats;

/**
 * Tests for the mean of calibration angles near the mode.
 * No PAMGuard runtime is needed.
 */
public class CalibrationStatsTest {

	private static final double TOL = 1e-9;

	@Test
	public void identicalAnglesGiveThatAngle() {
		double[] a = new double[20];
		java.util.Arrays.fill(a, 120.0);
		assertEquals(120.0, CalibrationStats.meanNear(a, 120.5, 5), TOL);
	}

	@Test
	public void resultIsNotTiedToTheBinCentre() {
		double[] a = { 119.8, 120.0, 120.2 };
		assertEquals(120.0, CalibrationStats.meanNear(a, 120.5, 5), 1e-6);
	}

	@Test
	public void anglesOutsideTheWindowAreIgnored() {
		double[] a = { 120.0, 120.0, 120.0, 150.0, 60.0 };
		assertEquals(120.0, CalibrationStats.meanNear(a, 120.5, 5), TOL);
	}

	@Test
	public void windowWrapsAroundPlusMinus180() {
		double[] a = { 179.0, -179.0 };
		assertEquals(180.0, Math.abs(CalibrationStats.meanNear(a, 179.5, 5)), 1e-6);
	}

	@Test
	public void windowWrapsAroundZero() {
		double[] a = { 359.0, 1.0 };
		assertEquals(0.0, CalibrationStats.meanNear(a, 0.5, 5), 1e-6);
	}

	@Test
	public void emptyWindowGivesNaN() {
		double[] a = { 10.0, 20.0 };
		assertTrue(Double.isNaN(CalibrationStats.meanNear(a, 120.5, 5)));
	}
}
