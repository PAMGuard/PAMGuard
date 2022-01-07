package Map.gridbaselayer;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

@Deprecated
public class GridDataBlock extends PamDataBlock<GridDataUnit> {

	public GridDataBlock(PamProcess parentProcess) {
		super(GridDataUnit.class, "Map Grid", parentProcess, 0);
		this.setClearAtStart(false);
	}

	@Override
	protected int removeOldUnitsT(long currentTimeMS) {
		// TODO Auto-generated method stub
//		return super.removeOldUnitsT(currentTimeMS);
		return 0;
	}

}
