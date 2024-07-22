package Localiser.algorithms.timeDelayLocalisers.bearingLoc;

import PamDetection.LocContents;
import pamMaths.PamVector;

public class CombinedBearingLocaliser implements BearingLocaliser {
	
	private BearingLocaliser firstLocaliser;
	
	private SimplexBearingLocaliser simplexBearingLocaliser;
	
	private double[] firstSimplexStep = new double[2];


	public CombinedBearingLocaliser(BearingLocaliser firstLocaliser, int hydrophoneBitMap, long timeMillis, double timingError) {
		this.firstLocaliser = firstLocaliser;
		simplexBearingLocaliser = new SimplexBearingLocaliser(hydrophoneBitMap, timeMillis, timingError);
		firstSimplexStep[0] = firstSimplexStep[1] = Math.PI/180.;
	}

	public CombinedBearingLocaliser(int hydrophoneBitMap, long timeMillis, double timingError) {
		firstLocaliser = new LSQBearingLocaliser(hydrophoneBitMap, timeMillis, timingError);
		simplexBearingLocaliser = new SimplexBearingLocaliser(hydrophoneBitMap, timeMillis, timingError);
		firstSimplexStep[0] = firstSimplexStep[1] = Math.PI/180.;
	}

	@Override
	public int getLocalisationContents() {
		return LocContents.HAS_BEARING | LocContents.HAS_AMBIGUITY;
	}

	@Override
	public void prepare(int[] arrayElements, long timeMillis, double timingError) {
		firstLocaliser.prepare(arrayElements, timeMillis, timingError);
		simplexBearingLocaliser.prepare(arrayElements, timeMillis, timingError);
	}

	@Override
	public int getArrayType() {
		return simplexBearingLocaliser.getArrayType();
	}

	@Override
	public int getHydrophoneMap() {
		return simplexBearingLocaliser.getHydrophoneMap();
	}

	@Override
	public PamVector[] getArrayAxis() {
		return simplexBearingLocaliser.getArrayAxis();
	}

	@Override
	public double[][] localise(double[] delays, long timeMillis) {
		double[][] res = firstLocaliser.localise(delays, timeMillis);
		if (res != null) {
			simplexBearingLocaliser.setStartValue(res[0]);
		}
		simplexBearingLocaliser.setFirstStep(firstSimplexStep);
		double[][] res2 = simplexBearingLocaliser.localise(delays, timeMillis);
		double r2d = 180./Math.PI;
//		System.out.printf("Simplex moved from %3.1f,%3.1f to %3.1f,%3.1f (Change %3.2f,%3.2f, chi from %3.2f to %3.2f change %3.4f)\n",
//				r2d*res[0][0], r2d*res[0][1], r2d*res2[0][0], r2d*res2[0][1],
//				r2d*res2[0][0]-r2d*res[0][0], r2d*res2[0][1]-r2d*res[0][1],
//				simplexBearingLocaliser.getChiVal(delays, res[0]), simplexBearingLocaliser.getChiVal(delays, res2[0]),
//				simplexBearingLocaliser.getChiVal(delays, res[0])- simplexBearingLocaliser.getChiVal(delays, res2[0]));
		return res2;
	}

}
