package clipgenerator;

import PamguardMVC.AcousticDataBlock;
import PamguardMVC.PamProcess;

/**
 * Really basic class for clip data blocks, which can be used by the 
 * Clip display (which is now incorporated into other modules, such as the 
 * DIFAR module).  
 * @author Doug Gillespie
 *
 * @param <TUnit>
 */
abstract public class ClipDisplayDataBlock<TUnit extends ClipDataUnit> extends AcousticDataBlock<TUnit> {
	
	public ClipDisplayDataBlock(Class unitClass, String dataName,
			PamProcess clipProcess, int channelMap) {
		super(ClipDataUnit.class, dataName, clipProcess, 0);
	}

}
