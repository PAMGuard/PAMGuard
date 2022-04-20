package rawDeepLearningClassifier.dataPlotFX;

import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLDetectionDataBlock;

/**
 * The DL detection plot provider. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class DLPredictionProvider extends TDDataProviderFX {

	/**
	 * Reference DL control. 
	 */
	private DLControl dlControl;

	/**
	 * The DL detection plot provider. 
	 * @param dlControl - reference to DL control. 
	 * @param dlDetectionDataBlock - the dl detection data block. 
	 */
	public DLPredictionProvider(DLControl dlControl, DLDetectionDataBlock dlDetectionDataBlock) {
		super(dlDetectionDataBlock); 
		this.dlControl = dlControl;
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new DLPredictionPlotInfoFX(this, dlControl, tdGraph, dlControl.getDLClassifyProcess().getDLPredictionDataBlock());
	}
	
	public String getName() {
		return "Prediction probability, " + dlControl.getUnitName();
	}
}
