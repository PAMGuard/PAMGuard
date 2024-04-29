package rawDeepLearningClassifier.dataPlotFX;

import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLDetectionDataBlock;
import rawDeepLearningClassifier.dlClassification.DLModelDataBlock;

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
	 * @param dlModelDataBlock - the dl detection data block. 
	 */
	public DLPredictionProvider(DLControl dlControl, DLModelDataBlock dlModelDataBlock) {
		super(dlModelDataBlock); 
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
