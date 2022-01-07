package clickDetector.basicalgorithm.plot;

import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import clickDetector.basicalgorithm.TriggerBackgroundDataBlock;
import clickDetector.basicalgorithm.TriggerBackgroundDataUnit;
import clickDetector.basicalgorithm.TriggerBackgroundHandler;
import dataPlotsFX.SimpleSymbolChooserFX;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.projector.TDProjectorFX;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Polygon;
import pamViewFX.fxNodes.PamColorsFX;

public class TriggerDataPlotFX extends TDDataInfoFX {

	private SimpleSymbolChooserFX symbolChooser = new SimpleSymbolChooserFX();
	
	private double lastX;
	
	private double[] lastY;
	
	private TriggerDataScaleInfo triggerDataScaleInfo = new TriggerDataScaleInfo();
	
	public TriggerDataPlotFX(TDDataProviderFX tdDataProvider, TDGraphFX tdGraph, 
			TriggerBackgroundHandler triggerBackgroundHandler, TriggerBackgroundDataBlock triggerBackgroundDataBlock) {
		super(tdDataProvider, tdGraph, triggerBackgroundDataBlock);
	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		return null;
	}

	@Override
	public TDSymbolChooserFX getSymbolChooser() {
		return symbolChooser;
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.data.TDDataInfoFX#getScaleInfo(boolean)
	 */
	@Override
	public TDScaleInfo getScaleInfo(boolean autoScale) {
		return triggerDataScaleInfo;
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.data.TDDataInfoFX#getScaleInfo()
	 */
	@Override
	public TDScaleInfo getScaleInfo() {
		return triggerDataScaleInfo;
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.data.TDDataInfoFX#clearDraw()
	 */
	@Override
	public void clearDraw() {
		super.clearDraw();
		lastY = null;
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.data.TDDataInfoFX#drawDataUnit(int, PamguardMVC.PamDataUnit, javafx.scene.canvas.GraphicsContext, long, dataPlotsFX.projector.TDProjectorFX, int)
	 */
	@Override
	public Polygon drawDataUnit(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g, double scrollStart,
			TDProjectorFX tdProjector, int type) {
		TriggerBackgroundDataUnit tbdu = (TriggerBackgroundDataUnit) pamDataUnit;
		int channelMap = tbdu.getChannelBitmap();
		int nChan = PamUtils.getNumChannels(channelMap);
		if (lastY != null && lastY.length != nChan) {
			lastY = null;
		}
		double[] y = new double[nChan];
		double[] amplitudeDB = tbdu.getAbsoluteAmplitudes();
		for (int i = 0; i < nChan; i++) {
			y[i] = tdProjector.getYPix(amplitudeDB[i]);
		}
		double x = tdProjector.getTimePix(tbdu.getTimeMilliseconds() - scrollStart);
		if (lastY != null) {
			for (int i = 0; i < nChan; i++) {
				int chanInd = PamUtils.getNthChannel(i, channelMap);
				g.setStroke(PamColorsFX.getInstance().getChannelColor(chanInd));
				g.strokeLine(lastX, lastY[i], x, y[i]);
			}
		}
		lastY = y;
		lastX = x;
	
		return null;
	}

}
