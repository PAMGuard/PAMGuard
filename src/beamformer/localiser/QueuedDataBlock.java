package beamformer.localiser;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;

public class QueuedDataBlock extends PamDataBlock {

	public QueuedDataBlock(Class unitClass, String dataName, PamProcess parentProcess, int channelMap) {
		super(unitClass, dataName, parentProcess, channelMap);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#addPamData(PamguardMVC.PamDataUnit, java.lang.Long)
	 */
	@Override
	public void addPamData(PamDataUnit pamDataUnit, Long uid) {
		// TODO Auto-generated method stub
		super.addPamData(pamDataUnit, uid);
	}

}
