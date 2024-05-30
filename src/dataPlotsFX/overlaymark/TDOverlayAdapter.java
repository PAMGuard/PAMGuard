package dataPlotsFX.overlaymark;

import PamView.paneloverlay.overlaymark.ExtMouseAdapter;
import PamView.paneloverlay.overlaymark.OverlayMark;
import dataPlotsFX.layout.TDGraphFX.TDPlotPane;
import detectiongrouplocaliser.DetectionGroupSummary;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;

/**
 * An overlay adapter allows user mouse and touch interactions with the display. 
 * 
 * @author Jamie Macaulay
 *
 */
public abstract class TDOverlayAdapter extends ExtMouseAdapter {
		
	/**
	 * The icon for the marker adapter
	 * @return get the icon
	 */
	public abstract Node getIcon(); 
	
	/**
	 * Called when the adapter is subscribed to a panel. 
	 * @param fxPlot - the plot panel.  
	 */
	public abstract void subscribePanel(TDPlotPane fxPlot); 
	
	/*
	 * Called when the adapter is subscribed to a panel. 
	 */
	public abstract Tooltip getToolTip();
	
//	/**
//	 * Get action items. These can be shown in the menu graph.
//	 * @return action items
//	 */
//	//public abstract ArrayList<TDGraphAction> getActionItems();
	
	/**
	 * Get the selected detections
	 * @return the selected detections. 
	 */
	public DetectionGroupSummary getSelectedDetectionGroup(){
		return null;
	}


	/**
	 * Draw the mark
	 * @param graphicsContext2D - the graphics context to draw on. 
	 */
	public void drawMark(GraphicsContext graphicsContext2D) {
		
	}
	
	/**
	 * Get an overlay mark
	 */
	public OverlayMark getOverlayMark() {
		return null;
	}
	
	/**
	 * Any displays using this manager need to be paused. 
	 * @return
	 */
	public abstract boolean needPaused();
	

}
