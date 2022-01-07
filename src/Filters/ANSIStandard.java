package Filters;

import noiseBandMonitor.BandType;

public class ANSIStandard {

	private static final double[] relFreq3Oct = {1.0000,    1.02676,    1.05594,    1.08776,    1.12246,    1.12246,
			1.29565,    1.88695,    3.06955,    5.43474};
	private static double[] relFreqOct;
	private static final double[] octGval = {0, 1./8., 1./4., 3./8., 1./2., 1./2., 1., 2., 3., 4.};
	private static final double maxAtten = 10000;//Double.POSITIVE_INFINITY;
	private static final double[] att0min = {-.15, -.15, -.15, -.15, -.15, 2.3, 18, 42.5, 62, 75};
	private static final double[] att0max = {.15, .2, .4, 1.1, 4.5, 4.5, maxAtten, maxAtten, maxAtten, maxAtten};
	private static final double[] att1min = {-.3, -.3, -.3, -.3, -.3, 2, 17.5, 42, 61, 70,};
	private static final double[] att1max = {.3, .4, .6, 1.3, 5, 5, maxAtten, maxAtten, maxAtten, maxAtten};
	private static final double[] att2min = {-.5, -.5, -.5, -.5, -.5, 1.6, 16.5, 41, 55, 60};
	private static final double[] att2max = {.5, .6, .8, 1.6, 5.5, 5.5, maxAtten, maxAtten, maxAtten, maxAtten,};
	/**
	 * @return the relative frequency vector
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
		}
		return null;
	}
	/**
	 * @param iClass filter class (0, 1 or 2)
	 * @return the minimum attenuation vector
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
	 * @return the maximum attenuation vector
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
