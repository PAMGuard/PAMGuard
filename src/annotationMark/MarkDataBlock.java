package annotationMark;

import pamScrollSystem.ViewLoadObserver;
import annotation.DataAnnotationType;
import annotation.calcs.snr.SNRAnnotationType;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class MarkDataBlock extends PamDataBlock {
		
	public MarkDataBlock(Class unitClass, String name,
			PamProcess parentProcess, int channelMap) {
		super(unitClass, name, parentProcess, channelMap);
	}


}
