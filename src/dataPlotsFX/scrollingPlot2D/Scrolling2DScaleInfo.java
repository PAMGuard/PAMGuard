package dataPlotsFX.scrollingPlot2D;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.DataBlock2D;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.spectrogramPlotFX.TDSpectrogramControlPane;

public class Scrolling2DScaleInfo extends TDScaleInfo {

	private Scrolling2DPlotInfo scrolling2dPlotInfo;

	public Scrolling2DScaleInfo(Scrolling2DPlotInfo scrolling2dPlotInfo, double minVal, double maxVal, ParameterType dataType, ParameterUnits dataUnits) {
		super(minVal, maxVal, dataType, dataUnits);
		this.scrolling2dPlotInfo = scrolling2dPlotInfo;
	}

	/**
	 * The default range is the full range of the data e.g. 0 to Nyquist for a spectrogram. 
	 */
	@Override
	public double[] getDefaultRange() {
		DataBlock2D dataBlock2D = scrolling2dPlotInfo == null ? null : scrolling2dPlotInfo.getDataBlock2D(); 
		if (dataBlock2D == null || dataBlock2D.getMaxDataValue() <= dataBlock2D.getMinDataValue()) {
			return super.getDefaultRange(); 
		}
		return new double[] {dataBlock2D.getMinDataValue(), dataBlock2D.getMaxDataValue()};
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.data.TDScaleInfo#setMinVal(double)
	 */
	@Override
	public void setMinVal(double minVal) {
		super.setMinVal(minVal);
//		TDSpectrogramControlPane cp = scrolling2dPlotInfo.getSpectrogramControlPane();
//		if (cp != null) {
//			cp.setMinFrequency(minVal);
//		}
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.data.TDScaleInfo#setMaxVal(double)
	 */
	@Override
	public void setMaxVal(double maxVal) {
		super.setMaxVal(maxVal);
//		TDSpectrogramControlPane cp = scrolling2dPlotInfo.getSpectrogramControlPane();
//		if (cp != null) {
//			cp.setMaxFrequency(maxVal);
//		}
	}


}
