package rawDeepLearningClassifier.dlClassification;

import PamView.GroupedDataSource;
import PamView.GroupedSourceParameters;
import PamguardMVC.AcousticDataBlock;
import PamguardMVC.dataSelector.DataSelectorCreator;
import clickTrainDetector.dataselector.CTDataSelectCreator;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dataSelector.DLDataSelectCreator;
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

	/**
	 * Reference to the deep learning classifier process. 
	 */
	private DLClassifyProcess dlClassifyProcess;
	
	/**
	 * Reference to the Tethys data provider for the deep learning module (provides standardised metadata). 
	 */
	private DLTethysDataProvider dlTethysDataProvider;
	
	/**
	 * A species manager for metadata. 
	 */
	private DLSpeciesManager dlSpeciesManager;
	
	/**
	 * Reference to the deep learning control. 
	 */
	private DLControl dlControl;
	
	/**
	 * Data selector for the deep learning detections. 
	 */
	private DLDataSelectCreator dlDataSelectCreator;

	public DLDetectionDataBlock(String dataName, DLClassifyProcess parentProcess, int channelMap) {
		super(DLDetection.class, dataName, parentProcess, channelMap);
		this.dlClassifyProcess = parentProcess; 
		dlControl = dlClassifyProcess.getDLControl();
	}

	@Override
	public GroupedSourceParameters getGroupSourceParameters() {
		return dlClassifyProcess.getDLParams().groupedSourceParams;
	}

	@Override
	public TethysDataProvider getTethysDataProvider(TethysControl tethysControl) {
		if (dlTethysDataProvider == null) {
			dlTethysDataProvider = new DLTethysDataProvider(tethysControl, dlControl, this);
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
	
	
	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getDataSelectCreator()
	 */
	@Override
	public synchronized  DataSelectorCreator getDataSelectCreator() {
		if (dlDataSelectCreator == null) {
			dlDataSelectCreator = new DLDataSelectCreator(dlControl, this);
		}
		return dlDataSelectCreator;
	
	}


}
