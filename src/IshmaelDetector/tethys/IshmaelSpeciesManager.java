package IshmaelDetector.tethys;

import PamguardMVC.PamDataBlock;
import tethys.species.FixedSpeciesManager;

public class IshmaelSpeciesManager extends FixedSpeciesManager {
	private static final int itisCode = 180403;
	private static final String name = "Cetacea";
	private static final String callType = "Ishmael Detection";
	public IshmaelSpeciesManager(PamDataBlock dataBlock) {
		super(dataBlock, itisCode, name, callType);
	}

}
