package alfa.clickmonitor.eventaggregator;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import clickDetector.offlineFuncs.OfflineClickLogging;
import clickDetector.offlineFuncs.OfflineEventDataUnit;

public class ClickAggregateDataBlock extends PamDataBlock<ClickEventAggregate> {

	public ClickAggregateDataBlock(String dataName, PamProcess parentProcess, int channelMap) {
		super(ClickEventAggregate.class, dataName, parentProcess, channelMap);
	}

}
