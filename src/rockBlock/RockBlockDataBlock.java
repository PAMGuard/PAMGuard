package rockBlock;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class RockBlockDataBlock<T extends RockBlockMessage> extends PamDataBlock<T> {

	/**
	 * @param unitClass
	 * @param dataName
	 * @param parentProcess
	 * @param channelMap
	 */
	public RockBlockDataBlock(Class unitClass, String dataName, PamProcess parentProcess, int channelMap) {
		super(unitClass, dataName, parentProcess, channelMap);
	}

	
	
}
