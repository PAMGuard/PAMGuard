package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * The IDIChi2Parmas. 
 * @author Jamie Macaulay
 *
 */
public class IDIChi2Params extends SimpleChi2VarParams implements ManagedParameters {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	
	/**
	 * The minimum IDI before a large Chi2 penalty is added. 
	 */
	public double minIDI = 0.0005; 
	
	public IDIChi2Params(String name, String unitString, double error, double minError, double errorScaleValue) {
		super(name, unitString, error, minError, errorScaleValue);
	}

	public IDIChi2Params(String name, String unitString, double error, double minError) {
		super(name, unitString, error, minError);
	}

	public IDIChi2Params(String name, String unitString) {
		super(name, unitString);
	}

	public IDIChi2Params(String name) {
		super(name);
	}
	
	public IDIChi2Params(SimpleChi2VarParams params) {
		this(params.name, params.getUnits(), params.error, params.minError, params.errorScaleValue);
	}

	
	@Override
	public IDIChi2Params clone() {
		return (IDIChi2Params) super.clone();
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}



}
