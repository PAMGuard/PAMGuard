package clickDetector.tethys;

import clickDetector.ClickDataBlock;
import nilus.GranularityEnumType;
import tethys.TethysControl;
import tethys.pamdata.AutoTethysProvider;

public class ClickTethysDataProvider extends AutoTethysProvider {

	private ClickDataBlock clickDataBlock;

	public ClickTethysDataProvider(TethysControl tethysControl, ClickDataBlock clickDataBlock) {
		super(tethysControl, clickDataBlock);
		this.clickDataBlock = clickDataBlock;
	}

	@Override
	public GranularityEnumType[] getAllowedGranularities() {
		return GranularityEnumType.values(); // everything !
	}

}
