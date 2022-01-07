package beamformer.plot;

import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import beamformer.continuous.BeamOGramDataUnit;

public class BeamOGramLocalisation extends AbstractLocalisation {

	private BeamOGramDataUnit boDataUnit;

	private double bestAngle;
	
	public static final int locCont = LocContents.HAS_BEARING | LocContents.HAS_AMBIGUITY;

	public BeamOGramLocalisation(BeamOGramDataUnit boDataUnit, int referenceHydrophones, double bestAngle) {
		super(boDataUnit, locCont, referenceHydrophones);
		this.boDataUnit = boDataUnit;
		setBestAngle(bestAngle);
	}
	//
	//	public BeamOGramLocalisation(PamDataUnit pamDataUnit, int locContents, int referenceHydrophones, int arrayType,
	//			PamVector[] arrayAxis) {
	//		super(pamDataUnit, locContents, referenceHydrophones, arrayType, arrayAxis);
	//		// TODO Auto-generated constructor stub
	//	}

	/**
	 * @return the bestAngle
	 */
	public double getBestAngle() {
		return bestAngle;
	}

	/**
	 * @param bestAngle the bestAngle to set
	 */
	public void setBestAngle(double bestAngle) {
		this.bestAngle = bestAngle;
	}
	
	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#getAngles()
	 */
	@Override
	public double[] getAngles() {
		double[] a = {bestAngle, bestAngle};
		return a;
	}

}
