package clickTrainDetector.clickTrainAlgorithms.mht.electricalNoiseFilter;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * 
 * Parameters for the electrical noise. 
 * 
 */
public class SimpleElectricalNoiseParams implements Serializable, ManagedParameters {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7L;

	/**
	 * The minimum chi^2 value allowed 
	 */
	public double minChi2 =  0.00001; 
	
	/**
	 * The minimum number of data units before a test can add a penalty factor.
	 */
	public int nDataUnits = 30; 

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
