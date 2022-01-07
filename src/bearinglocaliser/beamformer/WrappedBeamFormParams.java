package bearinglocaliser.beamformer;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import beamformer.BeamAlgorithmParams;
import beamformer.BeamFormerParams;
import bearinglocaliser.algorithms.BearingAlgorithmParams;

public class WrappedBeamFormParams extends BearingAlgorithmParams implements Serializable, ManagedParameters {

	private static final long serialVersionUID = 1L;
	
	private BeamAlgorithmParams beamAlgorithmParams;

	public WrappedBeamFormParams(BeamAlgorithmParams beamAlgorithmParams) {
		super();
		this.setBeamAlgorithmParams(beamAlgorithmParams);
	}

	/**
	 * @return the beamAlgorithmParams
	 */
	public BeamAlgorithmParams getBeamAlgorithmParams() {
		return beamAlgorithmParams;
	}

	/**
	 * @param beamAlgorithmParams the beamAlgorithmParams to set
	 */
	public void setBeamAlgorithmParams(BeamAlgorithmParams beamAlgorithmParams) {
		this.beamAlgorithmParams = beamAlgorithmParams;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}

	

}
