package group3dlocaliser.tethys;

import PamguardMVC.PamDataBlock;
import tethys.species.FixedSpeciesManager;

public class Group3DSpeciesManager extends FixedSpeciesManager {
	private static final int itisCode = 180403;
	private static final String name = "Cetacea";
	private static final String callType = "Group Localisation";
	
	public Group3DSpeciesManager(PamDataBlock dataBlock) {
		super(dataBlock, itisCode, name, callType);
	}

}
