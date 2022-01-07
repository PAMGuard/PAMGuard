package generalDatabase.lineplots;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import dataPlotsFX.data.TDScaleInfo;

public class LinePlotScaleInfo extends TDScaleInfo {

	private int dataIndex;

	/**
	 * @param minVal
	 * @param maxVal
	 * @param dataType
	 * @param dataUnits
	 * @param nPlots
	 */
	public LinePlotScaleInfo(double minVal, double maxVal, ParameterType dataType, ParameterUnits dataUnits,
			int nPlots, int dataIndex) {
		super(minVal, maxVal, dataType, dataUnits, nPlots);
		this.dataIndex = dataIndex;
	}

	/**
	 * @param minVal
	 * @param maxVal
	 * @param dataType
	 * @param dataUnits
	 */
	public LinePlotScaleInfo(double minVal, double maxVal, ParameterType dataType, ParameterUnits dataUnits, int dataIndex) {
		super(minVal, maxVal, dataType, dataUnits);
		this.dataIndex = dataIndex;
	}

	public int getDataIndex() {
		return dataIndex;
	}

}
