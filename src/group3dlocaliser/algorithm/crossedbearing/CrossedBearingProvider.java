package group3dlocaliser.algorithm.crossedbearing;

import group3dlocaliser.algorithm.LocaliserAlgorithm3D;
import group3dlocaliser.algorithm.LocaliserAlgorithmProvider;

public class CrossedBearingProvider extends LocaliserAlgorithmProvider {

	private CrossedBearingGroupLocaliser crossedBearingLocaliser;
	
	@Override
	public LocaliserAlgorithm3D createAlgorithm() {
		return getInstance();
	}
	
	/**
	 * Private creator to make sure that there is a single version 
	 * of the algorithm, but only if needed. 
	 * @return
	 */
	private synchronized CrossedBearingGroupLocaliser getInstance() {
		if (crossedBearingLocaliser == null) {
			crossedBearingLocaliser = new CrossedBearingGroupLocaliser();
		}
		return crossedBearingLocaliser;
	}

	@Override
	public String getName() {
		return "Crossed Bearings";
	}



}
