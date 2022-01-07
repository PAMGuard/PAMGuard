package dataPlots.data;

import dataPlots.layout.TDGraph;
import dataPlotsFX.data.TDScaleInfo;
import binaryFileStorage.DataUnitFileInformation;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * Create a standard set of bearing information that can be used with any data block
 * capable of generating bearing information. 
 * @author doug
 *
 */
public class StandardBearingInfo extends TDDataInfo {

	
	private TDScaleInfo scaleInformation;
	
	private TDSymbolChooser symbolChooser = new SimpleSymbolChooser();

	
	public StandardBearingInfo(TDDataProvider tdDataProvider, TDGraph tdGraph, PamDataBlock dataBlock) {
		super(tdDataProvider, tdGraph, dataBlock);
		scaleInformation = new TDScaleInfo(00, 180, ParameterType.BEARING, ParameterUnits.DEGREES);
		addDataUnits(new DataLineInfo("Bearing", TDDataInfo.UNITS_ANGLE));
	}

	@Override
	public TDScaleInfo getScaleInformation(int orientation, boolean autoScale) {
		return scaleInformation;
	}

	@Override
	public int getDataDimensions() {
		return 1;
	}

	@Override
	public TDSymbolChooser getSymbolChooser() {
		return symbolChooser;
	}
	
	/**
	 * @param symbolChooser the symbolChooser to set
	 */
	public void setSymbolChooser(TDSymbolChooser symbolChooser) {
		this.symbolChooser = symbolChooser;
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

}
