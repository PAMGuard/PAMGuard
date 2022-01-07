package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;


import PamguardMVC.PamDataUnit;

/**
 * Chi^2 value for dB amplitude of tracks. Measures the chnage in track delta between 
 * subsequent detections. 
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public class AmplitudeChi2 extends SimpleChi2VarDelta {
	
	public AmplitudeChi2() {
		super();
		super.setSimpleChiVarParams(defaultSettings());
	}
	
	/**
	 * Create default settings. On first instance of module these are called an saved. 
	 * @return
	 */
	private SimpleChi2VarParams defaultSettings() {
		SimpleChi2VarParams simpleChiVarParams = new SimpleChi2VarParams(getName(), getUnits()); 
		//simpleChiVarParams.errLimits=new double[] {Double.MIN_VALUE, 100}; 
		simpleChiVarParams.error=30;
		simpleChiVarParams.minError=1;
		simpleChiVarParams.errorScaleValue = SimpleChi2VarParams.SCALE_FACTOR_AMPLITUDE; 
		return simpleChiVarParams; 
	}
	
	
	public String getName() {
		return "Amplitude";
	}

	@Override
	public double getDiffValue(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1) {
		//System.out.println("DB: " + pamDataUnit0.getAmplitudeDB());
		return pamDataUnit0.getAmplitudeDB()-pamDataUnit1.getAmplitudeDB();
	}

	@Override
	public double getErrValue(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1) {
		//just a simple static error coefficient. 
		return super.getError();
	}


	@Override
	public String getUnits() {
		return "dB";
	}
	
}


