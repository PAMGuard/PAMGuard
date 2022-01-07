package dataPlotsFX.overlaymark;

import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDGraphFX.TDPlotPane;
import dataPlotsFX.projector.TDProjectorFX;
import detectiongrouplocaliser.DetectionGroupSummary;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

/**
 * A TDMarker which uses the standard PamGuard behaviours from OverlayMarker; 
 * 
 * @author Jamie Macaulay
 *
 */
public class StandardOverlayMarker extends OverlayMarker {
	

	protected TDGraphFX tdGraphFX;

	protected TDProjectorFX tdGraphProjector;
	
	protected MarkPainterFX markPainter;

	public StandardOverlayMarker(TDGraphFX tdGraphFX) {
		super(tdGraphFX, 0, tdGraphFX.getGraphProjector());
		this.tdGraphFX = tdGraphFX;
		this.tdGraphProjector = tdGraphFX.getGraphProjector();
		markPainter = new MarkPainterFX();
	}
	
	/**
	 * Subscribe to all required mouse events from 
	 * a panel. 
	 * @param fxPlot
	 */
	public void subscribePanel(TDPlotPane fxPlot) {		
		super.setMarkChannels(super.getMarkChannels() | fxPlot.getChannels());
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.overlaymark.OverlayMarker#notifyObservers(int, PamView.paneloverlay.overlaymark.OverlayMarker, PamView.paneloverlay.overlaymark.OverlayMark)
	 */
	@Override
	public boolean notifyObservers(int markStatus, MouseEvent mouseEvent, OverlayMarker overlayMarker, OverlayMark overlayMark) {
		boolean ans = super.notifyObservers(markStatus, mouseEvent, overlayMarker, overlayMark);
		tdGraphFX.repaintMarks(); 
		return ans;
	}


	@Override
	public String getMarkerName() {
		return tdGraphFX.getUniqueName();
	}
	
	/**
	 * Draw the mark on an fx display using it's current projector
	 * @param graphicsContext2D
	 */
	public void drawMark(GraphicsContext graphicsContext2D) {
//		System.out.println(" PamMarkerFX. currentMark " + this.getCurrentMark());
		OverlayMark currentMark = this.getCurrentMark();
		if (currentMark == null) {
			return;
		}
		if (currentMark.isHidden()) {
			return;
		}
		markPainter.drawMark(graphicsContext2D, currentMark, getProjector(), this.isNowMarking());
	}

	/**
	 * Get the currently selected detections
	 * @return a class containing info on selected detections
	 */
	public DetectionGroupSummary getCurrentDetectionGroup() {
		return null; 
	}
	
	
	/* (non-Javadoc)
	 * @see PamView.paneloverlay.overlaymark.OverlayMarker#isCanMark(javafx.scene.input.MouseEvent)
	 * Change behaviour here so that it doesn't require Ctrl key down. 
	 */
	@Override
	public boolean isCanMark(MouseEvent e) {
		if (getObserverCount() == 0) return false;
		return true;
	}
	
	public TDGraphFX getTdGraphFX() {
		return tdGraphFX;
	}


}
