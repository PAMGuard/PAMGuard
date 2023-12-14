package tethys.database;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import tethys.TethysControl;

public class TethysLogDataBlock extends PamDataBlock<TethysLogDataUnit> {

	private TethysControl tethysControl;

	public TethysLogDataBlock(TethysControl tethysControl) {
		super(TethysLogDataUnit.class, "Tethys Log", null, 0);
		this.tethysControl = tethysControl;
	}

}
