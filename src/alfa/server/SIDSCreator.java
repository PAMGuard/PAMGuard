package alfa.server;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;

public class SIDSCreator extends DataSelectorCreator {

	private ServerIntervalDataBlock serverIntervalDataBlock;

	public SIDSCreator(ServerIntervalDataBlock serverIntervalDataBlock) {
		super(serverIntervalDataBlock);
		this.serverIntervalDataBlock = serverIntervalDataBlock;
	}

	@Override
	public DataSelector createDataSelector(String selectorName, boolean allowScores, String selectorType) {
		// TODO Auto-generated method stub
		return new ServerIntervalDataSelector(serverIntervalDataBlock, selectorName);
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		return new SIDSParams();
	}

}
