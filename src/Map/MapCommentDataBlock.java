package Map;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;

public class MapCommentDataBlock extends PamDataBlock<MapComment> {


	public MapCommentDataBlock(String dataName, PamProcess parentProcess) {
		super(MapComment.class, dataName, parentProcess, 0);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean shouldNotify() {
		/*
		 * By overriding this, then comments should always get added, even in offline viewing mode. 
		 */
		return true;
	}

	@Override
	public boolean getShouldLog(PamDataUnit pamDataUnit) {
		return (pamDataUnit.getDatabaseIndex() == 0);
	}
}
