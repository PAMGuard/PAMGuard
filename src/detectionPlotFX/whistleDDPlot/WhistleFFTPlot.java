package detectionPlotFX.whistleDDPlot;

import dataPlotsFX.whistlePlotFX.WhistlePlotInfoFX;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.plots.FFTPlotParams;
import detectionPlotFX.plots.FFTSettingsPane;
import detectionPlotFX.plots.RawFFTPlot;
import detectionPlotFX.projector.DetectionPlotProjector;
import javafx.geometry.Orientation;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import whistlesAndMoans.ConnectedRegionDataUnit;
import whistlesAndMoans.WhistleMoanControl;

/**
 * Plots a whistle contour over a spectrgram if one is available.
 * @author Jamie Macaulay
 *
 */
public class WhistleFFTPlot extends  RawFFTPlot<ConnectedRegionDataUnit> {

	/*
	 * Reference to the whistle control.
	 */
	private WhistleMoanControl whistleMoanControl;
	
	/**
	 * The whistle FFT plot
	 * @param displayPlot - the display plot. 
	 * @param whistleMoanControl - whistle control
	 */
	public WhistleFFTPlot(DetectionPlotDisplay displayPlot, WhistleMoanControl whistleMoanControl) {
		super(displayPlot, displayPlot.getDetectionPlotProjector());
		this.whistleMoanControl=whistleMoanControl;
	}

	@Override
	public void paintDetections(ConnectedRegionDataUnit whistleDataUnit, 
			GraphicsContext graphicsContext, Rectangle windowRect, DetectionPlotProjector projector) {
		
		
//		System.out.println("Draw whistle fragment: " + whistleDataUnit + " sR: "+ whistleDataUnit.getParentDataBlock().getSampleRate() + " Scroll start: " + getScrollStart());
		
		WhistlePlotInfoFX.drawWhistleFragement(whistleDataUnit, 
				whistleMoanControl, 
				//need to have fft which was used in making the detections 
				whistleMoanControl.getWhistleToneProcess().getOutputData().getFftLength(),
				whistleMoanControl.getWhistleToneProcess().getOutputData().getFftHop(),
				whistleDataUnit.getParentDataBlock().getSampleRate(), //need to use this because FFT sample rate can be unreliable  
				graphicsContext,
				super.getProjector(), getScrollStart(), 0, getContourColor(), getContourColor(), this.isUseKHz(), Orientation.HORIZONTAL); 

	}
	
	private Color getContourColor() {
		return ((WhistlePlotParams) this.getFFTParams()).contourColor;
	}
	
	@Override
	protected FFTSettingsPane<?> createSettingsPane(){
		return new WhistleSettingsPane(null, this);
	}
	
	@Override
	public FFTPlotParams createPlotParams() {
		return new WhistlePlotParams();
	}
	
	
}

