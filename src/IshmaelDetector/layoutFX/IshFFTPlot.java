package IshmaelDetector.layoutFX;

import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.plots.RawFFTPlot;
import detectionPlotFX.projector.DetectionPlotProjector;
import detectionPlotFX.whistleDDPlot.WhistleSettingsPane;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import whistlesAndMoans.ConnectedRegionDataUnit;
import whistlesAndMoans.WhistleMoanControl;

/**
* Plots a whistle contour over.  
* @author Jamie Macaulay
*
*/
public class IshFFTPlot extends  RawFFTPlot<ConnectedRegionDataUnit> {

	/*
	 * Reference to the whistle control.
	 */
	private WhistleMoanControl whistleMoanControl;
	
	/**
	 * Line colour
	 */
	private Color lineColor=Color.GREEN;
	
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
	public IshFFTPlot(DetectionPlotDisplay displayPlot) {
		super(displayPlot, displayPlot.getDetectionPlotProjector());
	}

	@Override
	public void paintDetections(ConnectedRegionDataUnit whistleDataUnit, 
			GraphicsContext graphicsContext, Rectangle windowRect, DetectionPlotProjector projector) {
		
	
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
