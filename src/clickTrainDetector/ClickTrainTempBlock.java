package clickTrainDetector;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

/**
 * Temporary data block for click trains. This is used to store unconfirmed click trains so that the click train detector can paint 
 * click trains. 
 * 
 * @author Jamie Macaulay. 
 *
 */
public class ClickTrainTempBlock extends PamDataBlock<TempCTDataUnit> {

	public ClickTrainTempBlock(PamProcess parentProcess, int channelMap) {
		super(TempCTDataUnit.class, "Unconfirmed Click Trains", parentProcess, channelMap);
		// TODO Auto-generated constructor stub
	}

}
