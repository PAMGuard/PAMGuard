package clickTrainDetector.clickTrainAlgorithms.mht;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * Parameters for the MHT algorithm, contains two serializable parameter
 * classes, one for the MHTKernel and one for the MHTCHi2Provider. Although the
 * Kernel is unlikely to change the MHTChi2Params can be very different
 * depending on the type of MHTChi2Provider which is used.
 * 
 * @author Jamie Macaulay
 *
 */
public class MHTParams implements Serializable, Cloneable, ManagedParameters {

	/**
	 * 
	 */
	public static final long serialVersionUID = 2L;
	
	/**
	 * MHT Kernel params. 
	 */
	public MHTKernelParams mhtKernal = new MHTKernelParams(); 
	
	/**
	 * Parameters for chi^2 params. This can be any method to calculate a 
	 * chi^2 value from a click train. 
	 */
	public MHTChi2Params chi2Params = new StandardMHTChi2Params(StandardMHTChi2.createChi2Vars()); 
	
	@Override
	public MHTParams clone() {
		try {
			MHTParams params = (MHTParams) super.clone();
			params.mhtKernal=mhtKernal.clone(); 
			params.chi2Params=chi2Params.clone(); 
			return params; 
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}


}
