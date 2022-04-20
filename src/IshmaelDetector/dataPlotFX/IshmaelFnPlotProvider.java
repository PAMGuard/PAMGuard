package IshmaelDetector.dataPlotFX;

import IshmaelDetector.IshDetControl;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;


/**
 * The Ishmael raw detection output plot provider. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class IshmaelFnPlotProvider extends TDDataProviderFX {

	private IshDetControl ishControl;
	
	public IshmaelFnPlotProvider(IshDetControl ishControl) {
		super(ishControl.getIshDetFnProcess().getOutputDataBlock());
		this.ishControl = ishControl;
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new IshmaelFnPlotInfo(this, ishControl, tdGraph);
	}

	@Override
	public String getName() {
		return "Raw Detection Output, " + ishControl.getUnitName();
	}
}
