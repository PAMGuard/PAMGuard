package rawDeepLearningClassifier.ddPlotFX;

import PamguardMVC.PamDataBlock;
import detectionPlotFX.data.DDDataProvider;
import detectionPlotFX.layout.DetectionPlotDisplay;
import rawDeepLearningClassifier.DLControl;

public class RawDLDDPlotProvider extends DDDataProvider {

	/**
	 * DLControl. 
	 */
	private DLControl dlControl;

	public RawDLDDPlotProvider(DLControl dlControl, @SuppressWarnings("rawtypes") PamDataBlock parentDataBlock) {
		super(parentDataBlock);
		this.dlControl= dlControl; 
	}

	@Override
	public RawDLDDDataInfo createDataInfo(DetectionPlotDisplay dddisplay) {
		return new RawDLDDDataInfo(dlControl, dddisplay);
	}

}

