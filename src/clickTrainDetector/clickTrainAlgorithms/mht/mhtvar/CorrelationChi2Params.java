package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import fftFilter.FFTFilterParams;

/**
 * Parameters for correlation MHT variable 
 * @author Jamie Macaulay 
 *
 */
public class CorrelationChi2Params extends SimpleChi2VarParams implements ManagedParameters {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4L; 
	
	/**
	 * Use a filter for correlation calculations. 
	 */
	public boolean useFilter = false; 
	
	/**
	 * The filter parameters. How the waveforms are filtered before corss correlation 
	 * values are calculated.  
	 */
	public FFTFilterParams fftFilterParams = new FFTFilterParams(); 

	/**
	 * Parameters for the correlation chi^2
	 * @param name
	 */
	public CorrelationChi2Params(String name) {
		super(name);
		this.error=1;
		this.minError=0.01; 
	}
	
	public CorrelationChi2Params(String name, String unitString, double error, double minError, double errorScaleValue) {
		super(name, unitString, error, minError, errorScaleValue);
	}

	public CorrelationChi2Params(SimpleChi2VarParams params) {
		this(params.name, params.getUnits(), params.error, params.minError, params.errorScaleValue);
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}

}
 