package Filters;

import noiseBandMonitor.BandType;

/**
 * Acceptance limits on the relative attenuation of octave and fractional-octave
 * band filters, used to overlay the standard masks on filter Bode plots.
 * <p>
 * Octave and third octave bands use ANSI S1.11-2004 (== IEC 61260:1995), base-2,
 * classes 0, 1 and 2.
 * <p>
 * Deci-decade bands use IEC 61260-1:2014 (== ANSI/ASA S1.11-2014 Part 1). That
 * edition only specifies base-10 filters (octave ratio G = 10^(3/10)), so its
 * one-third octave bands are exactly deci-decade bands (ISO 18405). It only
 * defines classes 1 and 2, so class 0 returns null for deci-decade bands.
 * <p>
 * Each array has 10 points on the high side of the band (the low side is the
 * mirror image at 1/f). Points 4 and 5 are both at the band edge, giving the
 * step from the pass-band to the stop-band limits.
 */
public class ANSIStandard {

	private static final double maxAtten = 10000;//Double.POSITIVE_INFINITY;

	/*
	 * ANSI S1.11-2004 (base 2)
	 */
	private static final double[] relFreq3Oct = {1.0000,    1.02676,    1.05594,    1.08776,    1.12246,    1.12246,
			1.29565,    1.88695,    3.06955,    5.43474};
	private static double[] relFreqOct;
	private static final double[] octGval = {0, 1./8., 1./4., 3./8., 1./2., 1./2., 1., 2., 3., 4.};
	private static final double[] att0min = {-.15, -.15, -.15, -.15, -.15, 2.3, 18, 42.5, 62, 75};
	private static final double[] att0max = {.15, .2, .4, 1.1, 4.5, 4.5, maxAtten, maxAtten, maxAtten, maxAtten};
	private static final double[] att1min = {-.3, -.3, -.3, -.3, -.3, 2, 17.5, 42, 61, 70,};
	private static final double[] att1max = {.3, .4, .6, 1.3, 5, 5, maxAtten, maxAtten, maxAtten, maxAtten};
	private static final double[] att2min = {-.5, -.5, -.5, -.5, -.5, 1.6, 16.5, 41, 55, 60};
	private static final double[] att2max = {.5, .6, .8, 1.6, 5.5, 5.5, maxAtten, maxAtten, maxAtten, maxAtten,};

	/*
	 * IEC 61260-1:2014 / ANSI/ASA S1.11-2014 Part 1, Table 1 (base 10).
	 * Breakpoints are at the same octave normalised frequencies G^x as octGval.
	 */
	private static final double[] iec1min = {-.4, -.4, -.4, -.4, -.4, 1.2, 16.6, 40.5, 60, 70};
	private static final double[] iec1max = {.4, .5, .7, 1.4, 5.3, 5.3, maxAtten, maxAtten, maxAtten, maxAtten};
	private static final double[] iec2min = {-.6, -.6, -.6, -.6, -.6, .8, 15.6, 39.5, 54, 60};
	private static final double[] iec2max = {.6, .7, .9, 1.7, 5.8, 5.8, maxAtten, maxAtten, maxAtten, maxAtten};
	private static double[] relFreqDeciDecade;

	/**
	 * Octave frequency ratio for base-10 filters, IEC 61260-1:2014 Formula (1)
	 */
	public static final double G_BASE10 = Math.pow(10., 3./10.);

	/**
	 * @return the relative frequency vector, or null if there is no standard for this band type.
	 */
	public static double[] getRelFreq(BandType bandType) {
		switch(bandType) {
		case THIRDOCTAVE:
			return relFreq3Oct;
		case OCTAVE:
			if (relFreqOct == null) {
				relFreqOct = new double[octGval.length];
				for (int i = 0; i < octGval.length; i++) {
					relFreqOct[i] = Math.pow(2., octGval[i]);
				}
			}
			return relFreqOct;
		case DECIDECADE:
			if (relFreqDeciDecade == null) {
				relFreqDeciDecade = fractionalBreakpoints(G_BASE10, 3);
			}
			return relFreqDeciDecade;
		default:
			break;
		}
		return null;
	}

	/**
	 * Map the octave band breakpoints G^x to those of a 1/b octave band filter
	 * (IEC 61260-1:2014 Formula (9), ANSI S1.11-2004 Annex B):
	 * <br>Omega_b = 1 + (G^(1/2b) - 1) / (G^(1/2) - 1) * (G^x - 1)
	 * @param g octave frequency ratio (2 or 10^0.3)
	 * @param b bandwidth designator denominator
	 * @return breakpoint normalised frequencies
	 */
	private static double[] fractionalBreakpoints(double g, int b) {
		double scale = (Math.pow(g, 1./(2.*b)) - 1.) / (Math.pow(g, 0.5) - 1.);
		double[] rel = new double[octGval.length];
		for (int i = 0; i < octGval.length; i++) {
			rel[i] = 1. + scale * (Math.pow(g, octGval[i]) - 1.);
		}
		return rel;
	}

	/**
	 * Get the name of the standard used for the given band type.
	 * @param bandType band type
	 * @return name of standard, or null if there is none.
	 */
	public static String getStandardName(BandType bandType) {
		switch (bandType) {
		case OCTAVE:
		case THIRDOCTAVE:
			return "ANSI S1.11-2004";
		case DECIDECADE:
			return "IEC 61260-1:2014 (ANSI/ASA S1.11-2014)";
		default:
			return null;
		}
	}

	/**
	 * @param bandType band type
	 * @param iClass filter class (0, 1 or 2)
	 * @return the minimum attenuation vector, or null if the standard doesn't define this class
	 */
	public static double[] getMinAttenuation(BandType bandType, int iClass) {
		if (bandType == BandType.DECIDECADE) {
			switch(iClass) {
			case 1:
				return iec1min;
			case 2:
				return iec2min;
			}
			return null;
		}
		return getMinAttenuation(iClass);
	}

	/**
	 * @param bandType band type
	 * @param iClass filter class (0, 1 or 2)
	 * @return the maximum attenuation vector, or null if the standard doesn't define this class
	 */
	public static double[] getMaxAttenuation(BandType bandType, int iClass) {
		if (bandType == BandType.DECIDECADE) {
			switch(iClass) {
			case 1:
				return iec1max;
			case 2:
				return iec2max;
			}
			return null;
		}
		return getMaxAttenuation(iClass);
	}

	/**
	 * @param iClass filter class (0, 1 or 2)
	 * @return the ANSI S1.11-2004 minimum attenuation vector
	 */
	public static double[] getMinAttenuation(int iClass) {
		switch(iClass) {
		case 0:
			return att0min;
		case 1:
			return att1min;
		case 2:
			return att2min;
		}
		return null;
	}
	/**
	 * @param iClass filter class (0, 1 or 2)
	 * @return the ANSI S1.11-2004 maximum attenuation vector
	 */
	public static double[] getMaxAttenuation(int iClass) {
		switch(iClass) {
		case 0:
			return att0max;
		case 1:
			return att1max;
		case 2:
			return att2max;
		}
		return null;
	}


}
