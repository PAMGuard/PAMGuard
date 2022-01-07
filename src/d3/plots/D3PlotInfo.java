package d3.plots;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

import pamScrollSystem.PamScroller;
import Layout.PamAxis;
import PamController.PamControllerInterface;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.hidingpanel.HidingDialogComponent;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import d3.D3Control;
import d3.D3DataBlock;
import d3.D3DataUnit;
import dataPlots.data.DataLineInfo;
import dataPlots.data.SimpleSymbolChooser;
import dataPlots.data.TDDataInfo;
import dataPlots.data.TDDataProvider;
import dataPlots.data.TDSymbolChooser;
import dataPlots.layout.TDGraph;
import dataPlotsFX.data.TDScaleInfo;

public class D3PlotInfo extends TDDataInfo {

	private SimpleSymbolChooser symbolChooser = new SimpleSymbolChooser();
	private D3Control d3Control;
	protected D3DataLineInfo depthInfo;
	private D3DataLineInfo jerkInfo;
	private D3DataBlock d3DataBlock;
	private D3DataPlotProvider d3DataProvider;
	
	public D3PlotInfo(D3Control d3Control, D3DataPlotProvider tdDataProvider, TDGraph tdGraph, D3DataBlock d3DataBlock) {
		super(tdDataProvider, tdGraph, d3DataBlock);
		this.d3Control = d3Control;
		this.d3DataBlock = d3DataBlock;
		d3DataProvider = tdDataProvider;
		symbolChooser.setDrawTypes(TDSymbolChooser.DRAW_LINES | TDSymbolChooser.DRAW_SYMBOLS);
		symbolChooser.getPamSymbol(null,TDSymbolChooser.NORMAL_SYMBOL).setHeight(3);
		symbolChooser.getPamSymbol(null,TDSymbolChooser.NORMAL_SYMBOL).setWidth(3);
		symbolChooser.getPamSymbol(null,TDSymbolChooser.NORMAL_SYMBOL).setLineThickness(0.5F);
		setFixedScaleInformation(new TDScaleInfo(-500, 0, ParameterType.DEPTH, ParameterUnits.METERS));
		addDataUnits(depthInfo = new D3DataLineInfo("Depth", "metres", 0, -2));
		addDataUnits(jerkInfo = new D3DataLineInfo("Jerk", "m^2/s^4", 0, -1));
	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		D3DataUnit d3DataUnit = (D3DataUnit) pamDataUnit;
		int dataIndex = getCurrentDataLineIndex();
		D3DataLineInfo currentLine = (D3DataLineInfo) getCurrentDataLine();
		if (currentLine == null) {
			return null;
		}
		float[] sensorData = getSensorData(pamDataUnit, dataIndex);
		if (sensorData == null) return null;
		float sensSign = 1;
		if (dataIndex == 0) {
			sensSign = -1;
		}
		return (double) sensorData[0]*sensSign;
	}
	
	float[] getSensorData(PamDataUnit pamDataUnit, int sensorIndex) {
		D3DataUnit d3DataUnit = (D3DataUnit) pamDataUnit;
		int dataIndex = getCurrentDataLineIndex();
		if (dataIndex == 0) {
			float[] sensData = d3DataUnit.getDepth();
			if (sensData != null && sensData.length > 0) {
				return sensData;
			}
			else {
				return null;
			}
		}
		else if (dataIndex == 1) {
			float[] sensData = d3DataUnit.getJerk();
			if (sensData != null && sensData.length > 0) {
				return sensData;
			}
			else {
				return null;
			}
		}
		else {
			D3DataLineInfo currentLine = (D3DataLineInfo) getCurrentDataLine();
			int dataInd = currentLine.dataIndex;
			float[] sensData = d3DataUnit.getSensorData(dataInd);
			if (sensData != null) {
				return sensData;
			}
		}
		return null;

	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#drawDataUnit(PamguardMVC.PamDataUnit, java.awt.Graphics, java.awt.Rectangle, int, Layout.PamAxis, long, Layout.PamAxis, int)
	 */
	@Override
	public Polygon drawDataUnit(PamDataUnit pamDataUnit, Graphics g,
			Rectangle windowRect, int orientation, PamAxis timeAxis,
			long scrollStart, PamAxis yAxis, int type) {
		// each unit contains multiple values. Need to start by finding the
		// time gap between units. 
		int dataIndex = getCurrentDataLineIndex();
		D3DataLineInfo currentLine = (D3DataLineInfo) getCurrentDataLine();
		if (currentLine == null) {
			return null;
		}
		D3DataUnit d3DataUnit = (D3DataUnit) pamDataUnit;
		float[] sensorData = getSensorData(pamDataUnit, dataIndex);
		if (sensorData == null || sensorData.length == 0) return null;
		double d3Rate = currentLine.sampleRate;
		if (d3Rate <= 0.) {
			d3Rate = (double)sensorData.length;
		}
		double d3Step = 1000./d3Rate;
		float sensorSign = 1.f;
		if (dataIndex == 0) {
			sensorSign = -1.f;
		}
		for (int i = 0; i < sensorData.length; i++) {
			long t = pamDataUnit.getTimeMilliseconds() + (long) (i*d3Step);
			double tC = timeAxis.getPosition((t-scrollStart)/1000.);
			if (tC < 0) {
				return null;
			}
			double dataPixel = yAxis.getPosition(sensorData[i]*sensorSign);
			Point pt;
			if (orientation == PamScroller.HORIZONTAL) {
				pt = new Point((int) tC, (int) dataPixel);
			}
			else {
				pt = new Point((int) dataPixel, (int) tC);
			}
			if (pt.x < -20 || pt.x > windowRect.width + 20) return null;
			if ((getSymbolChooser().getDrawTypes() & TDSymbolChooser.DRAW_SYMBOLS) != 0) {
				getSymbolChooser().getPamSymbol(pamDataUnit,type).draw(g, pt);
			}
			if ((getSymbolChooser().getDrawTypes() & TDSymbolChooser.DRAW_LINES) != 0 && prevPoint != null && t-prevT < 2000) {
				g.setColor(getSymbolChooser().getPamSymbol(pamDataUnit,type).getLineColor());
				g.drawLine(prevPoint.x, prevPoint.y, pt.x, pt.y);
			}
			prevPoint = pt;
			prevT = t;
		}
		return null;
	}

	@Override
	public TDSymbolChooser getSymbolChooser() {
		return symbolChooser;
	}


	int dataChannels = 0;
	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		if (changeType == PamControllerInterface.OFFLINE_DATA_LOADED && dataChannels == 0) {
			dataChannels = d3DataProvider.addSensorChannels(this);
			if (dataChannels > 0) {
				getTdGraph().listAvailableAxisNames();
			}
		}
	}
	
	Point prevPoint;
	long prevT;

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#clearDraw()
	 */
	@Override
	public void clearDraw() {
		prevPoint = null;
		super.clearDraw();
	}
}
