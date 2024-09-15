package IshmaelDetector;

import IshmaelDetector.tethys.IshmaelSpeciesManager;
import IshmaelDetector.tethys.IshmaelTethysProvider;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import tethys.TethysControl;
import tethys.pamdata.TethysDataProvider;
import tethys.species.DataBlockSpeciesManager;

public class IshDataBlock extends PamDataBlock<IshDetection> {
	
	private IshmaelTethysProvider ishmaelTethysProvider;
	
	private IshmaelSpeciesManager ishmaelSpeciesManager;

	public IshDataBlock(String dataName, PamProcess parentProcess, int channelMap) {
		super(IshDetection.class, dataName, parentProcess, channelMap);
		
	}

	@Override
	public TethysDataProvider getTethysDataProvider(TethysControl tethysControl) {
		if (ishmaelTethysProvider == null) {
			ishmaelTethysProvider = new IshmaelTethysProvider(tethysControl, this);
		}
		return ishmaelTethysProvider;
	}

	@Override
	public DataBlockSpeciesManager<IshDetection> getDatablockSpeciesManager() {
		if (ishmaelSpeciesManager == null) {
			ishmaelSpeciesManager = new IshmaelSpeciesManager(this);
		}
		return ishmaelSpeciesManager;
	}


}
