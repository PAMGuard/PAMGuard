package whistleClassifier;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class WhistleClasificationDataBlock extends PamDataBlock<WhistleClassificationDataUnit> {

	public WhistleClasificationDataBlock(PamProcess parentProcess, int channelMap) {
		super(WhistleClassificationDataUnit.class, "Whistle Classification", parentProcess, channelMap);

	}

}
