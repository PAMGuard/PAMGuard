package clickDetector.basicalgorithm;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import binaryFileStorage.BinaryDataSource;

public class TriggerBackgroundDataBlock extends PamDataBlock<TriggerBackgroundDataUnit> {

	public TriggerBackgroundDataBlock(String dataName, PamProcess parentProcess, int channelMap) {
		super(TriggerBackgroundDataUnit.class, dataName, parentProcess, channelMap);
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getBinaryDataSource()
	 */
	@Override
	public BinaryDataSource getBinaryDataSource() {
		return super.getBinaryDataSource();
	}

}
