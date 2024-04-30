package rawDeepLearningClassifier.dlClassification;

import PamView.GroupedDataSource;
import PamView.GroupedSourceParameters;
import PamguardMVC.AcousticDataBlock;
import rawDeepLearningClassifier.tethys.DLSpeciesManager;
import rawDeepLearningClassifier.tethys.DLTethysDataProvider;
import tethys.TethysControl;
import tethys.pamdata.TethysDataProvider;
import tethys.species.DataBlockSpeciesManager;

/**
 * Holds classified data units from deep learning model. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DLDetectionDataBlock extends AcousticDataBlock<DLDetection> implements GroupedDataSource {

	private DLClassifyProcess dlClassifyProcess;
	private DLTethysDataProvider dlTethysDataProvider;
	private DLSpeciesManager dlSpeciesManager;

	public DLDetectionDataBlock(String dataName, DLClassifyProcess parentProcess, int channelMap) {
		super(DLDetection.class, dataName, parentProcess, channelMap);
		this.dlClassifyProcess = parentProcess; 
	}

	@Override
	public GroupedSourceParameters getGroupSourceParameters() {
		return dlClassifyProcess.getDLParams().groupedSourceParams;
	}

	@Override
	public TethysDataProvider getTethysDataProvider(TethysControl tethysControl) {
		if (dlTethysDataProvider == null) {
			dlTethysDataProvider = new DLTethysDataProvider(tethysControl, this);
		}
		return dlTethysDataProvider;
	}

	@Override
	public DataBlockSpeciesManager<DLDetection> getDatablockSpeciesManager() {
		if (dlSpeciesManager == null) {
			dlSpeciesManager = new DLSpeciesManager(dlClassifyProcess.getDLControl(), this);
		}
		return dlSpeciesManager;
	}


}
