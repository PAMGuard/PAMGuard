package dataPlots.data;

import dataPlots.layout.TDGraph;
import dataPlotsFX.data.TDScaleInfo;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class StandardDataInfo extends TDDataInfo {

	private DataLineInfo dataUnits;
	private TDScaleInfo scaleInformation;
	private int dataDimensions;
	private TDSymbolChooser symbolChooser = new SimpleSymbolChooser();
	
	public StandardDataInfo(TDDataProvider tdDataProvider, TDGraph tdGraph, PamDataBlock dataBlock, DataLineInfo dataUnits,
			TDScaleInfo scaleInformation) {
		super(tdDataProvider, tdGraph, dataBlock);
		this.dataUnits = dataUnits;
		this.scaleInformation = scaleInformation;
		dataDimensions = 1;
	}

	public StandardDataInfo(TDDataProvider tdDataProvider, TDGraph tdGraph, PamDataBlock dataBlock, DataLineInfo dataUnits,
			TDScaleInfo scaleInformation,
			int dataDimensions) {
		super(tdDataProvider, tdGraph, dataBlock);
		this.dataUnits = dataUnits;
		this.scaleInformation = scaleInformation;
		this.dataDimensions = dataDimensions;
		addDataUnits(dataUnits);
	}

	@Override
	public TDScaleInfo getScaleInformation(int orientation, boolean autoScale) {
		return scaleInformation;
	}

	@Override
	public int getDataDimensions() {
		return dataDimensions;
	}

	@Override
	public TDSymbolChooser getSymbolChooser() {
		return symbolChooser ;
	}

	/**
	 * @param symbolChooser the symbolChooser to set
	 */
	public void setSymbolChooser(TDSymbolChooser symbolChooser) {
		this.symbolChooser = symbolChooser;
	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		return null;
	}

}
