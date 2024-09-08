package d3;

import PamguardMVC.PamDataBlock;
import fileOfflineData.OfflineFileProcess;

public class D3DataBlock extends PamDataBlock<D3DataUnit> {

	public D3DataBlock(OfflineFileProcess parentProcess, String dataName) {
		super(D3DataUnit.class, dataName, parentProcess, 0);
		// TODO Auto-generated constructor stub
	}

}
