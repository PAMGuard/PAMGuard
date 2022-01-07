package detectionPlotFX.whistleDDPlot;

import PamguardMVC.debug.Debug;
import dataPlotsFX.whistlePlotFX.WhistlePlotInfoFX;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.plots.RawFFTPlot;
import detectionPlotFX.plots.FFTSettingsPane;
import detectionPlotFX.projector.DetectionPlotProjector;
import javafx.geometry.Orientation;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;
import whistlesAndMoans.ConnectedRegionDataUnit;
import whistlesAndMoans.WhistleMoanControl;

/**
 * Plots a whistle contour over.  
 * @author Jamie Macaulay
 *
 */
public class WhistleFFTPlot extends  RawFFTPlot<ConnectedRegionDataUnit> {

	/*
	 * Reference to the whistle control.
	 */
	private WhistleMoanControl whistleMoanControl;
	
	/**
	 * Line colour
	 */
	private Color lineColor=Color.BLACK;
	
	/**
	 * The fill colour
	 */
	private Color fillColor=Color.BLACK;

	/**
	 * The whislte settings pane. 
	 */
	private WhistleSettingsPane setttingsPane; 

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
		
//		Debug.out.println("Draw whistle fragment: " + whistleDataUnit + " sR: "+ whistleDataUnit.getParentDataBlock().getSampleRate() + " Scroll start: " + getScrollStart());
		WhistlePlotInfoFX.drawWhistleFragement(whistleDataUnit, 
				whistleMoanControl, 
				//need to have fft which was used in making the detections 
				whistleMoanControl.getWhistleToneProcess().getOutputData().getFftLength(),
				whistleMoanControl.getWhistleToneProcess().getOutputData().getFftHop(),
				whistleDataUnit.getParentDataBlock().getSampleRate(), //need to use this because FFT sample rate can be unreliable  
				graphicsContext,
				super.getProjector(), getScrollStart(), 0, fillColor, lineColor, Orientation.HORIZONTAL); 

	}
	
	@Override
	public Pane getSettingsPane() {
		return super.getSettingsPane();
//		if (setttingsPane==null){
//			setttingsPane= new WhistleSettingsPane(whistleMoanControl, this); 
//			setttingsPane.setParams(super.getFFTParams()) ;
//		}
//		return (Pane) setttingsPane.getContentNode();
	}
	
	
	
}

