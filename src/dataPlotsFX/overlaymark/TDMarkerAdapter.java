package dataPlotsFX.overlaymark;

import java.util.List;

import javax.swing.JPopupMenu;

import PamView.GeneralProjector.ParameterType;
import PamView.paneloverlay.overlaymark.MarkDataSelector;
import PamView.paneloverlay.overlaymark.MarkRelationships;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMarkObserver;
import PamView.paneloverlay.overlaymark.OverlayMarkProviders;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDGraphFX.TDPlotPane;
import detectiongrouplocaliser.DetectionGroupSummary;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;

/**
 * 
 * Adapter which converts the OverlayMark class within PAMGuard to the TDOverlayMarker used in the dataPlotsFX display.  
 * 
 * @author Jamie Macaulay	
 *
 */
public class TDMarkerAdapter extends TDOverlayAdapter {
	
	/**
	 * The overlay marker with hooks into other modules which subscribe to the display.
	 */
	private TDMarkerFX pamMarker;

	public TDMarkerAdapter(TDGraphFX tdGraphFX){
		
		pamMarker = new TDMarkerFX(tdGraphFX);

		if (tdGraphFX.getGraphId() >= 0) {
			/*
			 * DG - put the >=0 to stop Graph -1 getting listed. We either need to 
			 * have this, or remove it after the bodge Jamie put in
			 * TDControlPaneFX.createcontrolsPanel where it makes a graph with ind -1. 
			 */
			OverlayMarkProviders.singleInstance().addProvider(pamMarker);
			/*
			 *  seem to need this - this display must be getting created after
			 *  initialisation complete has been called in the main AWT thread
			 *  'cos this marker doesn't get correctly subscribed there. 
			 */
			MarkRelationships.getInstance().subscribeObservers(pamMarker);
		}
		pamMarker.setProjector(tdGraphFX.getGraphProjector());
		
		pamMarker.addDetectionGroupListener((detectionGroup)->{
			tdGraphFX.getTDDisplay().getTDControl().newSelectedDetectionGroup(detectionGroup, tdGraphFX); 
		});
	}
	

	
	/**
	 * Get the selected detections
	 * @return class containing selected detections
	 */
	public DetectionGroupSummary getSelectedDetectionGroup(){
		//get the currently selected data units. 
		return pamMarker.getCurrentDetectionGroup(); 
	}
	
	@Override
	public boolean mouseClicked(MouseEvent e) {
		return pamMarker.mouseClicked(e);
	}

	@Override
	public boolean mouseDragged(MouseEvent e) {
		return pamMarker.mouseDragged(e);
	}

	@Override
	public boolean mouseEntered(MouseEvent e) {
		return pamMarker.mouseEntered(e);
	}

	@Override
	public boolean mouseExited(MouseEvent e) {
		 return pamMarker.mouseExited(e);
	}

	@Override
	public boolean mouseMoved(MouseEvent e) {
		return pamMarker.mouseMoved(e);
	}

	@Override
	public boolean mousePressed(MouseEvent e) {
		return pamMarker.mousePressed(e);
	}

	@Override
	public boolean mouseReleased(MouseEvent e) {
		return pamMarker.mouseReleased(e);
	}

	@Override
	public boolean mouseWheelMoved(ScrollEvent e) {
		return pamMarker.mouseWheelMoved(e);
	}
	
	

	@Override
	public Node getIcon() {
//		return PamGlyphDude.createPamGlyph(MaterialDesignIcon.SELECTION, Color.WHITE, PamGuiManagerFX.iconSize);
		return PamGlyphDude.createPamIcon("mdi2s-selection", Color.WHITE, PamGuiManagerFX.iconSize);
	}

	@Override
	public void subscribePanel(TDPlotPane fxPlot) {
		pamMarker.subscribePanel(fxPlot);
		
	}

	@Override
	public Tooltip getToolTip() {
		return new Tooltip("Allows marking of multiple detections");
	}
	
	@Override
	public void drawMark(GraphicsContext graphicsContext2D) {
		pamMarker.drawMark(graphicsContext2D);
	}

	@Override
	public boolean needPaused() {
		return pamMarker.needPaused();
	} 
	
	
	/**
	 * Get an overlay mark. 
	 * @return the current overlay mark. Can be null. 
	 */
	@Override
	public OverlayMark getOverlayMark() {
		return pamMarker.getCurrentMark();
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.overlaymark.ExtMouseAdapter#getPopupMenuItems(javafx.scene.input.MouseEvent)
	 */
	@Override
	public List<MenuItem> getPopupMenuItems(MouseEvent e) {
		List<MenuItem> menuItems = pamMarker.getPopupMenuItems(e);
		return menuItems;
	}

}
