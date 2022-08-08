package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

import PamModel.parametermanager.ManagedParameters;

public class AmplitudeChi2Params extends SimpleChi2VarParams  implements ManagedParameters {


	
	public AmplitudeChi2Params(String name, String unitString, double error, double minError, double errorScaleValue) {
		super(name, unitString, error, minError, errorScaleValue);
		// TODO Auto-generated constructor stub
	}
	public AmplitudeChi2Params(String name, String unitString, double error, double minError) {
		super(name, unitString, error, minError);
		// TODO Auto-generated constructor stub
	}
	public AmplitudeChi2Params(String name, String unitString) {
		super(name, unitString);
		// TODO Auto-generated constructor stub
	}
	public AmplitudeChi2Params(String name) {
		super(name);
	}

	public AmplitudeChi2Params(SimpleChi2VarParams params) {
		this(params.name, params.getUnits(), params.error, params.minError, params.errorScaleValue);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Whether the bearing jump is used. 
	 */
	public boolean ampJumpEnable = true;
	
	/**
	 * The maximum allowed bearing bearing jump in a click train in RADIANS
	 */
	public double maxAmpJump = 10; //dB


	
}