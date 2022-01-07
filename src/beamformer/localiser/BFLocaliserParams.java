package beamformer.localiser;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import beamformer.BeamFormerParams;

/**
 * Parameters for beam former localiser
 * @author Doug Gillespie
 *
 */
public class BFLocaliserParams extends BeamFormerParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public String detectionSource;

	public int fftLength = 512;
	
	public int fftHop = fftLength / 2;
	
	/**
	 * Say to make additional data units and beam form ALL channel groups. 
	 */
	public boolean doAllGroups;

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public BFLocaliserParams clone() {
		return (BFLocaliserParams) super.clone();
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}

}
