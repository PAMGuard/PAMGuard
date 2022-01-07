package bearinglocaliser;

import bearinglocaliser.algorithms.BearingAlgorithm;
import bearinglocaliser.algorithms.BearingAlgorithmProvider;
import bearinglocaliser.display.BearingDataDisplay;

public class BearingAlgorithmGroup {

	public int channelMap;
	
	public int groupIndex;

	public BearingAlgorithmProvider algorithmProvider;
	
	public BearingAlgorithm bearingAlgorithm;

	private BearingDataDisplay bearingDataDisplay;
	
	public BearingAlgorithmGroup(int groupIndex, int channelMap, BearingAlgorithmProvider algorithmProvider,
			BearingAlgorithm bearingAlgorithm) {
		super();
		this.groupIndex = groupIndex;
		this.channelMap = channelMap;
		this.algorithmProvider = algorithmProvider;
		this.bearingAlgorithm = bearingAlgorithm;
	}

	public BearingDataDisplay getDataDisplay() {
			return bearingAlgorithm.createDataDisplay();
	}

}
