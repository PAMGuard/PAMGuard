package bearinglocaliser.beamformer;

import beamformer.algorithms.BeamAlgorithmProvider;
import beamformer.algorithms.StaticAlgoProperties;
import bearinglocaliser.algorithms.StaticAlgorithmProperties;

public class WrappedStaticProperties extends StaticAlgorithmProperties {

	private StaticAlgoProperties beamAlgorithmProperties;

	public WrappedStaticProperties(BeamAlgorithmProvider beamAlgorithmProvider) {
		super(beamAlgorithmProvider.getStaticProperties().getName());
		beamAlgorithmProperties = beamAlgorithmProvider.getStaticProperties();
	}

	/* (non-Javadoc)
	 * @see bearinglocaliser.algorithms.StaticAlgorithmProperties#getShortName()
	 */
	@Override
	public String getShortName() {
		return beamAlgorithmProperties.getShortName();
	}

}
