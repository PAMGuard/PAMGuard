package dataPlotsFX.overlaymark;

import java.util.ArrayList;
import java.util.List;

import PamView.paneloverlay.overlaymark.ExtMouseAdapter;
import dataPlotsFX.layout.TDGraphFX;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

/**
 * Handles user mouse and touch interactions with the display. Sets which marker is currently implemented and passes
 * mouse behaviours from the display to the marker. 
 * 
 * @author Jamie Macaulay
 *
 */
public class OverlayMarkerManager extends ExtMouseAdapter {

	/**
	 * List of possible overlay markers. 
	 */
	private ArrayList<TDOverlayAdapter> markerAdapters = new ArrayList<TDOverlayAdapter>(); 
	
	/*
	 * List of possible markers for the display 
	 */
	private int currentMarker=0; 
	

	/**
	 * Reference to the display. 
	 */
	private TDGraphFX tdGraphFX;
	
	public OverlayMarkerManager(TDGraphFX tdGraphFX) {
		this.tdGraphFX=tdGraphFX; 
		
		//Add adapter heres
		markerAdapters.add(new TDMarkerAdapter(tdGraphFX));
		markerAdapters.add(new DragMarkerAdapter(tdGraphFX));
	}
	

	public ArrayList<TDOverlayAdapter> getOverlayMarkers() {
		return markerAdapters;
	}

	public TDOverlayAdapter getCurrentMarker() {
		 return this.markerAdapters.get(this.currentMarker);
	}


	public void setCurrentMarkIndex(int index) {
		currentMarker=index; 
	}

	public boolean needPaused() {
		if (markerAdapters == null) return false;
		for (TDOverlayAdapter overlayMarker:markerAdapters) {
			if (overlayMarker.needPaused()) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.overlaymark.ExtMouseAdapter#mouseClicked(javafx.scene.input.MouseEvent)
	 */
	@Override
	public boolean mouseClicked(MouseEvent e) {
		return getCurrentMarker().mouseClicked(e);
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.overlaymark.ExtMouseAdapter#mouseDragged(javafx.scene.input.MouseEvent)
	 */
	@Override
	public boolean mouseDragged(MouseEvent e) {
		return getCurrentMarker().mouseDragged(e);
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.overlaymark.ExtMouseAdapter#mouseEntered(javafx.scene.input.MouseEvent)
	 */
	@Override
	public boolean mouseEntered(MouseEvent e) {
		return getCurrentMarker().mouseEntered(e);
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.overlaymark.ExtMouseAdapter#mouseExited(javafx.scene.input.MouseEvent)
	 */
	@Override
	public boolean mouseExited(MouseEvent e) {
		return getCurrentMarker().mouseExited(e);
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.overlaymark.ExtMouseAdapter#mouseMoved(javafx.scene.input.MouseEvent)
	 */
	@Override
	public boolean mouseMoved(MouseEvent e) {
		return getCurrentMarker().mouseMoved(e);
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.overlaymark.ExtMouseAdapter#mousePressed(javafx.scene.input.MouseEvent)
	 */
	@Override
	public boolean mousePressed(MouseEvent e) {
		return getCurrentMarker().mousePressed(e);
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.overlaymark.ExtMouseAdapter#mouseReleased(javafx.scene.input.MouseEvent)
	 */
	@Override
	public boolean mouseReleased(MouseEvent e) {
		return getCurrentMarker().mouseReleased(e);
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.overlaymark.ExtMouseAdapter#mouseWheelMoved(javafx.scene.input.ScrollEvent)
	 */
	@Override
	public boolean mouseWheelMoved(ScrollEvent e) {
		return getCurrentMarker().mouseWheelMoved(e);
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.overlaymark.ExtMouseAdapter#getPopupMenuItems(javafx.scene.input.MouseEvent)
	 */
	@Override
	public List<MenuItem> getPopupMenuItems(MouseEvent e) {
		return getCurrentMarker().getPopupMenuItems(e);
	}


}
