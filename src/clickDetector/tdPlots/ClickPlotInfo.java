package clickDetector.tdPlots;

import java.awt.Window;
import java.io.Serializable;
import java.util.ArrayList;

import pamScrollSystem.PamScroller;
import clickDetector.BTDisplayParameters;
import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import clickDetector.ClickDisplayManagerParameters;
import clickDetector.dialogs.ClickDisplayDialog;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.hidingpanel.HidingDialogComponent;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataPlots.data.DataLineInfo;
import dataPlots.data.TDDataInfo;
import dataPlots.data.TDDataProvider;
import dataPlots.data.TDSymbolChooser;
import dataPlots.layout.TDGraph;
import dataPlotsFX.data.TDScaleInfo;

public class ClickPlotInfo extends TDDataInfo {

	private ClickSymbolChooser clickSymbolChooser;
	private TDScaleInfo bearingScaleInfo, iciScaleInfo, ampScaleInfo, slantScaleInfo;
	private TDScaleInfo[] allScaleInfo;
	private ClickControl clickControl;
	private ClickDetection lastDrawnClick;
	protected BTDisplayParameters btDisplayParams = new BTDisplayParameters();
	private ClickHidingDialog clickHidingDialog;
	
	public ClickPlotInfo(TDDataProvider tdDataProvider, ClickControl clickControl, TDGraph tdGraph, 
			PamDataBlock pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);
		this.clickControl = clickControl;
		clickSymbolChooser = new ClickSymbolChooser(clickControl, this);
		addDataUnits(new DataLineInfo("Bearing", TDDataInfo.UNITS_ANGLE));
		addDataUnits(new DataLineInfo("ICI", "S"));
		addDataUnits(new DataLineInfo("Amplitude", "dB"));
		addDataUnits(new DataLineInfo("Slant Angle", TDDataInfo.UNITS_ANGLE));
		bearingScaleInfo = new TDScaleInfo(180, 0, ParameterType.BEARING, ParameterUnits.DEGREES);
		iciScaleInfo = new TDScaleInfo(0, 2, ParameterType.TIME, ParameterUnits.SECONDS);
		ampScaleInfo = new TDScaleInfo(100, 200, ParameterType.AMPLITUDE, ParameterUnits.DB);
		slantScaleInfo = new TDScaleInfo(0, 180, ParameterType.SLANTANGLE, ParameterUnits.DEGREES);
		allScaleInfo = new TDScaleInfo[4];
		allScaleInfo[0] = bearingScaleInfo;
		allScaleInfo[1] = iciScaleInfo;
		allScaleInfo[2] = ampScaleInfo;
		allScaleInfo[3] = slantScaleInfo;
		clickHidingDialog = new ClickHidingDialog(this);
	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		ClickDetection click = (ClickDetection) pamDataUnit;
		//first check we can generally plot the click
		if (!shouldPlot(click)) return null; 
		//clikc has passed the first test! Now get the correct data value; 
		Double val = null;
		switch (getCurrentDataLineIndex()) {
		case 0:
			val = getBearingValue(click);
			break;
		case 1:
			val = getICIValue(click);
			break;
		case 2:
			val = getAmplitudeValue(click);
			break;
		}
		lastDrawnClick = click;
		return val;
	}
	
	/**
	 * Get the CickControl class fro this ClickPlotInfo
	 * @return ClickControl instance fro this ClickPlotInfo. 
	 */
	public ClickControl getClickControl(){
		return clickControl; 
	} 
	
	private Double getAmplitudeValue(ClickDetection click) {
		return click.getAmplitudeDB();
	}

	private Double getICIValue(ClickDetection click) {
		if (lastDrawnClick == null) {
			return -1.;
		}
		return (double) (click.getTimeMilliseconds() - lastDrawnClick.getTimeMilliseconds()) / 1000.;
	}

	private Double getBearingValue(ClickDetection click) {
		if (click.getLocalisation() == null) {
			return null;
		}
		double[] angles = click.getLocalisation().getAngles();
		if (angles != null) {
			return Math.toDegrees(angles[0]);
		}
		else {
			return null;
		}
	}
	
	private synchronized boolean shouldPlot(ClickDetection click) {
		
		if (click == null) return false;
		if (btDisplayParams.showEchoes == false && click.isEcho()) {
			return false;
		}
		
		if (btDisplayParams.displayChannels > 0 && (btDisplayParams.displayChannels & click.getChannelBitmap()) == 0) return false;
		
		return true;
	}

	@Override
	public TDSymbolChooser getSymbolChooser() {
		return clickSymbolChooser;
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#getScaleInformation(boolean)
	 */
	@Override
	public TDScaleInfo getScaleInformation(int orientation, boolean autoScale) {
		clearDraw();
		return super.getScaleInformation(orientation, autoScale);
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#getFixedScaleInformation()
	 */
	@Override
	public TDScaleInfo getFixedScaleInformation(int orientation) {
		int iind = getCurrentDataLineIndex();
		if (iind < 0) return null;
		if (iind == 0) { 
			double min = Math.min(bearingScaleInfo.getMinVal(), bearingScaleInfo.getMaxVal());
			double max = Math.max(bearingScaleInfo.getMinVal(), bearingScaleInfo.getMaxVal());
			bearingScaleInfo.setMaxVal(max);
			bearingScaleInfo.setMinVal(min);
		}
		return allScaleInfo[iind];
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#clearDraw()
	 */
	@Override
	public void clearDraw() {
		lastDrawnClick = null;
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#hasOptions()
	 */
	@Override
	public boolean hasOptions() {
		return true;
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#editOptions(java.awt.Window)
	 */
	@Override
	public boolean editOptions(Window frame) {
		BTDisplayParameters newParams = ClickDisplayDialog.showDialog(clickControl, frame, btDisplayParams);
		if (newParams != null) {
			btDisplayParams = newParams.clone();
			updateSettings();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#getStoredSettings()
	 */
	@Override
	public Serializable getStoredSettings() {
		return btDisplayParams;
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#setStoredSettings(java.io.Serializable)
	 */
	@Override
	public boolean setStoredSettings(Serializable storedSettings) {
		if (BTDisplayParameters.class.isAssignableFrom(storedSettings.getClass())) {
			btDisplayParams = (BTDisplayParameters) storedSettings;
			updateSettings();
			return true;
		}
		return false;
	}
	
	public int getDisplayChannels() {
		return btDisplayParams.displayChannels;
	
	}
	
	public void setDisplayChannels(int displayChannels) {
		btDisplayParams.displayChannels = displayChannels;
		updateSettings();
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#getHidingDialogComponent()
	 */
	@Override
	public HidingDialogComponent getHidingDialogComponent() {
		return clickHidingDialog;
	}

	/**
	 * Called when settings have changed. 
	 */
	public void updateSettings() {
		// need to get the axis type and force that information on over to the 
		// main graph so it too can update it's own version of the dialog. 
		clickHidingDialog.selectColourButton();
		int axIndex = btDisplayParams.VScale;
		ArrayList<DataLineInfo> dataLines = getDataLineInfos();
		if (axIndex >= 0 && axIndex < dataLines.size()) {
//			String dataName = dataLines.get(axIndex).name;
			getTdGraph().selectDataLine(this, dataLines.get(axIndex));
		}
		// set the various scales depending on whats in the bt parameters. 
		if (btDisplayParams.view360) {
			bearingScaleInfo.setMinVal(-180);
			bearingScaleInfo.setMaxVal(180);
		}
		else {
			bearingScaleInfo.setMinVal(180);
			bearingScaleInfo.setMaxVal(0);
		}
		ampScaleInfo.setMinVal(btDisplayParams.amplitudeRange[0]);
		ampScaleInfo.setMaxVal(btDisplayParams.amplitudeRange[1]);
		
		iciScaleInfo.setMaxVal(btDisplayParams.maxICI);
		if (btDisplayParams.logICIScale) {
			iciScaleInfo.setMinVal(btDisplayParams.minICI);
		}
		else {
			iciScaleInfo.setMinVal(0);
		}
		getTdGraph().checkAxis();
		getTdGraph().getTdControl().repaintAll();
	}
	/*
	 * Colour type changed in the quick dialog panel. 
	 */
	public void selectColourType(int colourId) {
		btDisplayParams.colourScheme = colourId;
		getTdGraph().repaint(0);
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#selectDataLine(dataPlots.data.DataLineInfo)
	 */
	@Override
	public void selectDataLine(DataLineInfo dataLine) {
		super.selectDataLine(dataLine);
		btDisplayParams.VScale = getCurrentDataLineIndex();
	}

	

}
