package Array.plot;

import Array.Streamer;
import Array.StreamerDataBlock;
import Array.StreamerDataUnit;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.SimpleSymbolChooserFX;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.layout.TDGraphFX;

public class ArrayTDDataInfo extends TDDataInfoFX {

	private TDScaleInfo headingScaleInfo;
	private TDScaleInfo pitchScaleInfo;
	private TDScaleInfo rollScaleInfo;
	private TDScaleInfo depthScaleInfo;
	private SimpleSymbolChooserFX symbolChooser = new SimpleSymbolChooserFX();

	public ArrayTDDataInfo(TDDataProviderFX tdDataProvider, TDGraphFX tdGraph, StreamerDataBlock streamerDataBlock) {
		super(tdDataProvider, tdGraph, streamerDataBlock);
		headingScaleInfo = new TDScaleInfo(-180,180, ParameterType.BEARING, ParameterUnits.DEGREES);
		pitchScaleInfo = new TDScaleInfo(-90, 90, ParameterType.BEARING, ParameterUnits.DEGREES);
		rollScaleInfo = new TDScaleInfo(-90, 90, ParameterType.BEARING, ParameterUnits.DEGREES);
		depthScaleInfo = new TDScaleInfo(0, 100, ParameterType.DEPTH, ParameterUnits.METERS);
		depthScaleInfo.setReverseAxis(true);
		addScaleInfo(depthScaleInfo);
//		addScaleInfo(pitchScaleInfo);
//		addScaleInfo(rollScaleInfo);
		addScaleInfo(headingScaleInfo);
//		this.set
		
	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		StreamerDataUnit streamerDataUnit = (StreamerDataUnit) pamDataUnit;
		Streamer streamerData = streamerDataUnit.getStreamerData();
		if (streamerData == null) {
			return null;
		}
		TDScaleInfo csi = getCurrentScaleInfo();
		if (csi == null) {
			return null;
		}
		if (csi == depthScaleInfo) {
			return -streamerData.getZ();
		}
		else if (csi == pitchScaleInfo) {
			return streamerData.getPitch();
		}
		else if (csi == rollScaleInfo) {
			return streamerData.getRoll();
		}
		else if (csi == headingScaleInfo) {
			return streamerData.getHeading();
		}
		
		return null;
	}

	@Override
	public TDSymbolChooserFX getSymbolChooser() {
		return symbolChooser;
	}

}
