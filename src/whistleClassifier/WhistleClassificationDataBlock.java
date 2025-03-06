package whistleClassifier;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.dataSelector.DataSelectorCreator;
import tethys.TethysControl;
import tethys.pamdata.TethysDataProvider;
import tethys.species.DataBlockSpeciesManager;
import whistleClassifier.dataselect.WslClsDataSelectCreator;
import whistleClassifier.tethys.WslClassSpeciesManager;
import whistleClassifier.tethys.WslClassTethysProvider;

public class WhistleClassificationDataBlock extends PamDataBlock<WhistleClassificationDataUnit> {
	
	private WslClsDataSelectCreator dataSelectCreator;
	private WhistleClassifierControl wslClassifierControl;
	
	private WslClassSpeciesManager wslClassSpeciesManager;
	
	private WslClassTethysProvider wslClassTethysProvider;

	public WhistleClassificationDataBlock(WhistleClassifierControl wslClassifierControl, PamProcess parentProcess, int channelMap) {
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

	@Override
	public TethysDataProvider getTethysDataProvider(TethysControl tethysControl) {
		if (wslClassTethysProvider == null) {
			wslClassTethysProvider = new WslClassTethysProvider(tethysControl, wslClassifierControl, this);
		}
		return wslClassTethysProvider;
	}

	@Override
	public DataBlockSpeciesManager<WhistleClassificationDataUnit> getDatablockSpeciesManager() {
		if (wslClassSpeciesManager == null) {
			wslClassSpeciesManager = new WslClassSpeciesManager(wslClassifierControl, this);
		}
		return wslClassSpeciesManager;
	}

}
