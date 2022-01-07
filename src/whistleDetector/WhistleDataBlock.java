package whistleDetector;

import PamguardMVC.PamProcess;
import whistlesAndMoans.AbstractWhistleDataBlock;

public class WhistleDataBlock extends AbstractWhistleDataBlock<ShapeDataUnit> {

	public WhistleDataBlock(Class unitClass, String dataName,
			PamProcess parentProcess, int channelMap) {
		super(unitClass, dataName, parentProcess, channelMap);
	}

}
