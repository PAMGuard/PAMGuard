package clickDetector.basicalgorithm.plot;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import dataPlotsFX.data.TDScaleInfo;

public class TriggerDataScaleInfo extends TDScaleInfo {

	public TriggerDataScaleInfo() {
		super(80, 160, ParameterType.AMPLITUDE, ParameterUnits.DB);
	}

}
