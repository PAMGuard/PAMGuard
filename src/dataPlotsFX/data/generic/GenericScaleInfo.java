package dataPlotsFX.data.generic;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import dataPlotsFX.data.TDScaleInfo;

public class GenericScaleInfo extends TDScaleInfo {

	private boolean available = true;

	public GenericScaleInfo(double minVal, double maxVal, ParameterType dataType, ParameterUnits dataUnits) {
		super(minVal, maxVal, dataType, dataUnits);
	}

	public GenericScaleInfo(double minVal, double maxVal, ParameterType dataType, ParameterUnits dataUnits,
			int nPlots) {
		super(minVal, maxVal, dataType, dataUnits, nPlots);
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.data.TDScaleInfo#isAvailable()
	 */
	@Override
	public boolean isAvailable() {
		return available;
	}

	/**
	 * Set if this scale info is currently available. 
	 * @param available
	 */
	public void setAvailable(boolean available) {
		this.available = available;
	}
}
