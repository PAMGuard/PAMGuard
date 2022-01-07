package whistlesAndMoans.plots;

import pamScrollSystem.PamScroller;
import whistlesAndMoans.WhistleMoanControl;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataPlots.data.DataLineInfo;
import dataPlots.data.SimpleSymbolChooser;
import dataPlots.data.TDDataInfo;
import dataPlots.data.TDDataProvider;
import dataPlots.data.TDSymbolChooser;
import dataPlots.layout.TDGraph;
import dataPlotsFX.data.TDScaleInfo;

public class WhistlePlotInfo extends TDDataInfo {

	private WhistleMoanControl wmControl;

	private TDSymbolChooser symbolChooser = new SimpleSymbolChooser();

	private TDScaleInfo bearingScaleInfo;

	public WhistlePlotInfo(TDDataProvider tdDataProvider, WhistleMoanControl wmControl, TDGraph tdGraph, 
			PamDataBlock pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);
		this.wmControl = wmControl;
		addDataUnits(new DataLineInfo("Bearing", TDDataInfo.UNITS_ANGLE));
		bearingScaleInfo = new TDScaleInfo(180, 0, null, null);
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
		bearingScaleInfo.setMinVal(min);
		bearingScaleInfo.setMaxVal(max);
		return bearingScaleInfo;
	}

	@Override
	public TDSymbolChooser getSymbolChooser() {
		return symbolChooser;
	}

}
