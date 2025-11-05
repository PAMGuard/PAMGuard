package detectionview;

import clipgenerator.ClipDisplayDataBlock;

public class DVDataBlock extends ClipDisplayDataBlock<DVDataUnit> {

	public DVDataBlock(DVControl dvControl, DVProcess dvProcess, int channelMap) {
		super(DVDataUnit.class, dvControl.getUnitName() + " clips", dvProcess, channelMap);
	}


}
