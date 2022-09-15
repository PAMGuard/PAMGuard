package whistleClassifier;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.dataSelector.DataSelectorCreator;
import whistleClassifier.dataselect.WslClsDataSelectCreator;

public class WhistleClasificationDataBlock extends PamDataBlock<WhistleClassificationDataUnit> {
	
	private WslClsDataSelectCreator dataSelectCreator;
	private WhistleClassifierControl wslClassifierControl;

	public WhistleClasificationDataBlock(WhistleClassifierControl wslClassifierControl, PamProcess parentProcess, int channelMap) {
		super(WhistleClassificationDataUnit.class, "Whistle Classification", parentProcess, channelMap);
		this.wslClassifierControl = wslClassifierControl;
	}

	@Override
	public DataSelectorCreator getDataSelectCreator() {
		if (dataSelectCreator == null) {
			dataSelectCreator = new WslClsDataSelectCreator(wslClassifierControl, this);
		}
		return dataSelectCreator;
	}

}
