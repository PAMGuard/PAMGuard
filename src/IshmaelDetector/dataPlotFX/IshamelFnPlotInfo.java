package IshmaelDetector.dataPlotFX;

import IshmaelDetector.IshDetControl;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.generic.GenericScaleInfo;
import dataPlotsFX.layout.TDGraphFX;

/**
 * Plots the raw Ishmael data (i.e the detection values and noise)
 * 
 * @author Jamie Macaulay
 *
 */
public class IshamelFnPlotInfo extends TDDataInfoFX {

	/**
	 * Ish det control
	 */
	private IshDetControl ishControl;
	
	private GenericScaleInfo probabilityScaleInfo;
	
	private GenericScaleInfo frequencyInfo;

	public IshamelFnPlotInfo(TDDataProviderFX tdDataProvider, IshDetControl ishControl, TDGraphFX tdGraph) {
		super(tdDataProvider, tdGraph, ishControl.getIshDetFnProcess().getOutputDataBlock());

		this.ishControl=ishControl; 

		probabilityScaleInfo = new GenericScaleInfo(-0.1, 1.1, ParameterType.PROBABILITY, ParameterUnits.PROBABILITY);

		frequencyInfo = new GenericScaleInfo(0, 1, ParameterType.FREQUENCY, ParameterUnits.HZ);

		addScaleInfo(probabilityScaleInfo);
		addScaleInfo(frequencyInfo);
	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TDSymbolChooserFX getSymbolChooser() {
		// TODO Auto-generated method stub
		return null;
	}

}
