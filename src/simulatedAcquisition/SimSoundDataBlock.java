package simulatedAcquisition;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class SimSoundDataBlock extends PamDataBlock {

	public SimSoundDataBlock(String dataName,
			PamProcess parentProcess, int channelMap) {
		super(SimSoundDataUnit.class, dataName, parentProcess, channelMap);
		SetLogging(new SimSoundsLogging(this));
	}

}
