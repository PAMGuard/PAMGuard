package gpl.graphfx;

import java.lang.reflect.ParameterizedType;

import PamUtils.PamUtils;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.projector.TDProjectorFX;
import gpl.GPLControlledUnit;
import gpl.GPLStateDataBlock;
import gpl.GPLStateDataUnit;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import pamViewFX.fxNodes.PamColorsFX;

public class GPLStatePlotInfo extends TDDataInfoFX {

	private GPLStateDataBlock stateDataBlock;

	private ScaleInfo scaleInfo;

	private GPLStateSymbolChooser symbolChooser;

	private GPLControlledUnit gplControlledUnit;

	//	private TDSymbolChooserFX symbolChooser = new 

	public GPLStatePlotInfo(TDDataProviderFX tdDataProvider, GPLControlledUnit gplControlledUnit, TDGraphFX tdGraph, GPLStateDataBlock stateDataBlock) {
		super(tdDataProvider, tdGraph, stateDataBlock);
		this.stateDataBlock = stateDataBlock;
		this.gplControlledUnit = gplControlledUnit;
		scaleInfo = new ScaleInfo();
		getScaleInfos().add(scaleInfo);
		symbolChooser = new GPLStateSymbolChooser();

	}

	@Override
	public synchronized Double getDataValue(PamDataUnit pamDataUnit) {
		GPLStateDataUnit sdu = (GPLStateDataUnit) pamDataUnit;
		double data = sdu.getBaseline();
		if (Double.isInfinite(data)) {
			return 9.2;
		}
		if (Double.isNaN(data)) {
			return 8.;
		}
		else if (data < 0) {
			return -4.;
		}
		
		data = Math.log10(data);
		if (data > 9.) {
			return -1.;
		}
		if (data < -5) {
			return -3.;
		}
		//		System.out.printf("Data: %3.1f\n", data);
		return data;
	}

	@Override
	public TDSymbolChooserFX getSymbolChooser() {
		return symbolChooser;
	}

//	/* (non-Javadoc)
//	 * @see dataPlotsFX.data.TDDataInfoFX#getScaleInfo()
//	 */
//	@Override
//	public TDScaleInfo getScaleInfo() {
//		return scaleInfo;
//	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.data.TDDataInfoFX#drawData(int, javafx.scene.canvas.GraphicsContext, long, dataPlotsFX.projector.TDProjectorFX)
	 */
	@Override
	public void drawData(int plotNumber, GraphicsContext g, double scrollStart, TDProjectorFX tdProjector) {
		/**
		 * Draw a zero and a threshold line. 
		 */
		double y = tdProjector.getYPix(Math.log10(gplControlledUnit.getGplParameters().noise_ceiling));
		g.setStroke(Color.GREEN);
		g.strokeLine(0, y, tdProjector.getWidth(), y);
		y = tdProjector.getYPix(0);
		g.setStroke(Color.GREY);
		g.strokeLine(0, y, tdProjector.getWidth(), y);
		double thresh = Math.log10(gplControlledUnit.getGplParameters().thresh);
		y = tdProjector.getYPix(thresh);
		g.setStroke(Color.RED);
		g.strokeLine(0, y, tdProjector.getWidth(), y);

		g.setStroke(Color.BLACK);
		super.drawData(plotNumber, g, scrollStart, tdProjector);
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.data.TDDataInfoFX#drawDataUnit(int, PamguardMVC.PamDataUnit, javafx.scene.canvas.GraphicsContext, long, dataPlotsFX.projector.TDProjectorFX, int)
	 */
	@Override
	public Polygon drawDataUnit(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g, double scrollStart,
			TDProjectorFX tdProjector, int type) {
		GPLStateDataUnit sdu = (GPLStateDataUnit) pamDataUnit;
		g.setStroke(getLineColour(sdu));
		return super.drawDataUnit(plotNumber, pamDataUnit, g, scrollStart, tdProjector, type);
	}
	
	private Color getLineColour(GPLStateDataUnit sdu) {
//		if (PamUtils.getNumChannels(stateDataBlock.getChannelMap()) > 1) {
		if (PamUtils.getNumChannels(stateDataBlock.getSequenceMap()) > 1) {
//			return PamColorsFX.getInstance().getChannelColor(PamUtils.getLowestChannel(sdu.getChannelBitmap()));
			return PamColorsFX.getInstance().getChannelColor(PamUtils.getLowestChannel(sdu.getSequenceBitmap()));
		}
		return sdu.getPeakState() >= 2 ? Color.RED : Color.BLACK;
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.data.TDDataInfoFX#shouldDraw(int, PamguardMVC.PamDataUnit)
	 */
	@Override
	public boolean shouldDraw(int plotNumber, PamDataUnit dataUnit) {
		return true;
	} 
	
	private class ScaleInfo extends TDScaleInfo {

		public ScaleInfo() {
			super(-2, 5, ParameterType.AMPLITUDE, ParameterUnits.RAW);
		}

	}

}
