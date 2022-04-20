package IshmaelDetector.dataPlotFX;

import IshmaelDetector.IshDetControl;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;

/**
 * The plot provider for Ishmael detection data. 
 * @author Jamie Macaulay
 *
 */
public class IshmaelDetPlotProvider extends TDDataProviderFX {

	private IshDetControl ishControl;
	
	public IshmaelDetPlotProvider(IshDetControl ishControl) {
		super(ishControl.getIshPeakProcess().getOutputDataBlock());
		this.ishControl = ishControl;
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new IshmaelDetPlotInfo(this, ishControl, tdGraph);
	}
	
	
	@Override
	public String getName() {
		return "Detections, " + ishControl.getUnitName();
	}

}