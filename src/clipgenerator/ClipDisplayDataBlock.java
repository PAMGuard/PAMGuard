package clipgenerator;

import java.util.ListIterator;

import PamguardMVC.AcousticDataBlock;
import PamguardMVC.PamDataUnit;
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
	
	/**
	 * Find the Clip data unit that has the given trigger data unit.  
	 * @param triggerDataUnit
	 * @return
	 */
	public TUnit findDataForTrigger(PamDataUnit triggerDataUnit) {
		synchronized (getSynchLock()) {
			ListIterator<TUnit> it = getListIterator(0);
			while (it.hasNext()) {
				TUnit dvU = it.next();
				if (dvU.getTriggerDataUnit() == triggerDataUnit) {
					return dvU;
				}
			}
		}
		return null;
	}
	
}
