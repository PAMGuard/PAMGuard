package difar.plots;

import pamScrollSystem.PamScroller;
import difar.DifarControl;
import difar.DifarDataUnit;
import difar.dataSelector.DifarDataSelector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataPlots.data.DataLineInfo;
import dataPlots.data.SimpleSymbolChooser;
import dataPlots.data.StandardBearingInfo;
import dataPlots.data.TDDataInfo;
import dataPlots.data.TDDataProvider;
import dataPlots.data.TDSymbolChooser;
import dataPlots.layout.TDGraph;
import dataPlotsFX.data.TDScaleInfo;

public class DifarIntensityPlotInfo extends TDDataInfo {

	private DifarControl difarControl;
	
	private TDSymbolChooser symbolChooser = new DifarSymbolChooser();
	
	private TDScaleInfo amplitudeScaleInfo;
	
	public DifarIntensityPlotInfo(TDDataProvider tdDataProvider, DifarControl difarControl, TDGraph tdGraph, 
			PamDataBlock pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);
		this.difarControl = difarControl;
		addDataUnits(new DataLineInfo("Amplitude", "dB RMS"));
		amplitudeScaleInfo = new TDScaleInfo(80, 130, ParameterType.AMPLITUDE, ParameterUnits.DB);
	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		DifarDataUnit ddu = (DifarDataUnit) pamDataUnit;
		if (ddu.getLocalisation() == null) {
			return null;
		}
//		difarControl.getDifarProcess().getProcessedDifarData().getDataSelectCreator().getDataSelector(selectorName, allowScores)
		return ddu.getCalculatedAmlitudeDB();
	}

	@Override
	public TDScaleInfo getFixedScaleInformation(int orientation) {
		double min = Math.min(amplitudeScaleInfo.getMinVal(), amplitudeScaleInfo.getMaxVal());
		double max = Math.max(amplitudeScaleInfo.getMinVal(), amplitudeScaleInfo.getMaxVal());
		if (orientation == PamScroller.HORIZONTAL) {
			max = 130;
			min = 80;
			amplitudeScaleInfo.setMaxVal(max);
			amplitudeScaleInfo.setMinVal(min);
		}
		else {
			amplitudeScaleInfo.setMaxVal(max);
			amplitudeScaleInfo.setMinVal(min);
		}
		return amplitudeScaleInfo;
	}
	
	

	@Override
	public TDSymbolChooser getSymbolChooser() {
		return symbolChooser;
	}

}
