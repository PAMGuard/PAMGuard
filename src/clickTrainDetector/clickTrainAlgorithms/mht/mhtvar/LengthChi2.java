package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

import PamguardMVC.PamDataUnit;

/**
 * The length of a click. 
 * @author Jamie Macaulay
 *
 */
public class LengthChi2 extends SimpleChi2Var {
	
	public LengthChi2() {
		super();
		super.setSimpleChiVarParams(defaultSettings());
	}

	@Override
	public String getName() {
		return "Click Length";
	}

	@Override
	public String getUnits() {
		return "ms";
	}

	@Override
	public double getDiffValue(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1) {
		return pamDataUnit0.getDurationInMilliseconds() - pamDataUnit1.getDurationInMilliseconds();
	}

	@Override
	public double getErrValue(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1) {
		return getError();
	}
	
	/**
	 * Create default settings. On first instance of module these are called an saved. 
	 * @return
	 */
	private SimpleChi2VarParams defaultSettings() {
		SimpleChi2VarParams simpleChiVarParams = new SimpleChi2VarParams(getName(), getUnits()); 
		//simpleChiVarParams.errLimits=new double[] {Double.MIN_VALUE, 100}; 
		simpleChiVarParams.error=0.2;
		simpleChiVarParams.minError=0.002;
		simpleChiVarParams.errorScaleValue = SimpleChi2VarParams.SCALE_FACTOR_ICI*10; 
		return simpleChiVarParams; 
	}
	

}
