package generalDatabase.lineplots;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class LinePlotDataBlock extends PamDataBlock<LinePlotDataUnit> {

	public LinePlotDataBlock(String dataName, LinePlotControl linePlotControl, PamProcess parentProcess) {
		super(LinePlotDataUnit.class, dataName, parentProcess, 0);
	}

}
