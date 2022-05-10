package clickTrainDetector.classification.simplechi2classifier;

import java.util.UUID;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import clickTrainDetector.classification.CTClassifierParams;
import clickTrainDetector.classification.CTClassifierType;

/**
 * 
 * Simple chi^2 classifier parameters
 * 
 * @author Jamie Macaulay
 *
 */
public class Chi2ThresholdParams extends CTClassifierParams implements ManagedParameters  {
	
	public Chi2ThresholdParams(){
		super();
		super.type=CTClassifierType.CHI2THRESHOLD;
	}
	
	/**
	 * 
	 */
	public static final long serialVersionUID = 3L;
	
	/**
	 * The chi2 threshold to set. This is the chi2 value divided by the number of clicks in the train.
	 * If zero then the classification always passes...A bit fo a hack for testing
	 */
	public double chi2Threshold = 1500.;

	/**
	 * The minimum number of clicks. 
	 */
	public int minClicks = 5;

	/**
	 * The minimum time in seconds.
	 */
	public double minTime = 0.; 
	
	@Override
	public Chi2ThresholdParams clone() {
			Chi2ThresholdParams clonedParams =(Chi2ThresholdParams) super.clone();
			return clonedParams;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}

}
