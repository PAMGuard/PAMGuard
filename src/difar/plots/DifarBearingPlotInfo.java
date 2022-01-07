package difar.plots;

import pamScrollSystem.PamScroller;
import difar.DifarControl;
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

public class DifarBearingPlotInfo extends TDDataInfo {

	private DifarControl difarControl;
	
	private TDSymbolChooser symbolChooser = new DifarSymbolChooser();
	
	private TDScaleInfo bearingScaleInfo;

	public DifarBearingPlotInfo(TDDataProvider tdDataProvider, DifarControl difarControl, TDGraph tdGraph, 
			PamDataBlock pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);
		this.difarControl = difarControl;
		addDataUnits(new DataLineInfo("Bearing", TDDataInfo.UNITS_ANGLE));
		bearingScaleInfo = new TDScaleInfo(360, 0, ParameterType.BEARING, ParameterUnits.DEGREES);
	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		if (pamDataUnit.getLocalisation() == null) {
			return null;
		}
		double[] angles = pamDataUnit.getLocalisation().getAngles();
		if (angles != null && angles.length > 0) {
			return Math.toDegrees(angles[0]);
		}
		return null;
	}

	@Override
	public TDScaleInfo getFixedScaleInformation(int orientation) {
		double min = Math.min(bearingScaleInfo.getMinVal(), bearingScaleInfo.getMaxVal());
		double max = Math.max(bearingScaleInfo.getMinVal(), bearingScaleInfo.getMaxVal());
		if (orientation == PamScroller.HORIZONTAL) {
			bearingScaleInfo.setMaxVal(max);
			bearingScaleInfo.setMinVal(min);
		}
		else {
			bearingScaleInfo.setMaxVal(max);
			bearingScaleInfo.setMinVal(min);
		}
		return bearingScaleInfo;
	}

	@Override
	public TDSymbolChooser getSymbolChooser() {
		return symbolChooser;
	}

}
