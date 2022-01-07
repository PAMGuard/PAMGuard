package beamformer.algorithms.nullalgo;

import PamController.SettingsPane;
import beamformer.BeamAlgorithmParams;
import beamformer.BeamFormerBaseProcess;
import beamformer.BeamFormerParams;
import beamformer.algorithms.BeamAlgorithmProvider;
import beamformer.algorithms.BeamFormerAlgorithm;
import beamformer.algorithms.StaticAlgoProperties;
import beamformer.continuous.BeamFormerProcess;

/**
 * Provider for the null beam former. 
 * @author dg50
 *
 */
public class NullBeamProvider implements BeamAlgorithmProvider {


	private StaticAlgoProperties staticProperties;
	
	public NullBeamProvider() {
		super();
		staticProperties = new StaticAlgoProperties("Null Beamformer", "null", false);
	}

	@Override
	public StaticAlgoProperties getStaticProperties() {
		return staticProperties;
	}

	@Override
	public BeamFormerAlgorithm makeAlgorithm(BeamFormerBaseProcess beamFormerProcess, BeamAlgorithmParams parameters, int firstSeqNum, int beamogramNum) {
		return new NullBeamFormer(this, beamFormerProcess, parameters, firstSeqNum);
	}

	/* (non-Javadoc)
	 * @see beamformer.algorithms.BeamAlgorithmProvider#createNewParams(java.lang.String, int, int)
	 */
	@Override
	public BeamAlgorithmParams createNewParams(String algorithmName, int groupNumber, int channelMap) {
		return (new NullBeamParams(algorithmName, groupNumber, channelMap));
	}

	/* (non-Javadoc)
	 * @see beamformer.algorithms.BeamAlgorithmProvider#getParamsDialog(beamformer.BeamAlgorithmParams)
	 */
	@Override
	public SettingsPane<?> getParamsDialog(BeamFormerParams overallParams, BeamAlgorithmParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see beamformer.algorithms.BeamAlgorithmProvider#getCurrentParams()
	 */
	@Override
	public BeamAlgorithmParams getCurrentParams() {
		// TODO Auto-generated method stub
		return null;
	}

}
