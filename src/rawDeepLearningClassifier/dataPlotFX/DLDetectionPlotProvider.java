package rawDeepLearningClassifier.dataPlotFX;

import PamguardMVC.PamDataBlock;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLDetectionDataBlock;

/**
 * 
 * The DL detection plot provider. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class DLDetectionPlotProvider extends TDDataProviderFX {

	/**
	 * Reference DL control. 
	 */
	private DLControl dlControl;

	private boolean group = false; 

	/**
	 * The DL detection plot provider. 
	 * @param dlControl - reference to DL control. 
	 * @param dlDetectionDataBlock - the dl detection data block. 
	 */
	public DLDetectionPlotProvider(DLControl dlControl, PamDataBlock dlDetectionDataBlock, boolean group) {
		super(dlDetectionDataBlock); 
		this.dlControl = dlControl;
		this.group=group;
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		if (group) {
			return new DLGroupDetectionInfoFX(this, dlControl, tdGraph, this.getDataBlock());
		}
		else {
			return new DLDetectionPlotInfoFX(this, dlControl, tdGraph, this.getDataBlock());
		}
	}

	public String getName() {
		if (group) {
			return "Deep learning group detection, " + dlControl.getUnitName();
		}
		else {
			return "Deep learning detection, " + dlControl.getUnitName();
		}

	}
}
