package cpod.dataPlotFX;

import PamguardMVC.PamDataBlock;
import cpod.CPODClickTrainDataUnit;
import detectionPlotFX.data.DDDataProvider;
import detectionPlotFX.layout.DetectionPlotDisplay;

/**
 * Provides the detection display plots for CPOD and FPOD click trains.
 *
 * @author Jamie Macaulay
 *
 */
public class CPODClickTrainDDPlotProvider extends DDDataProvider {

	private PamDataBlock<CPODClickTrainDataUnit> clickTrainDataBlock;

	public CPODClickTrainDDPlotProvider(PamDataBlock<CPODClickTrainDataUnit> clickTrainDataBlock) {
		super(clickTrainDataBlock);
		this.clickTrainDataBlock = clickTrainDataBlock;
	}

	@Override
	public CPODClickTrainDDDataInfo createDataInfo(DetectionPlotDisplay dddisplay) {
		return new CPODClickTrainDDDataInfo(clickTrainDataBlock, dddisplay);
	}

}
