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
	
//	
//	public void subscribePanel(TDPlotPanel fxPlot) {
//		
//		fxPlot.setOnMouseClicked(new MouseClicked());
//		fxPlot.setOnMousePressed(new MousePressed());
//		fxPlot.setOnMouseReleased(new MouseReleased());
//		fxPlot.setOnMouseMoved(new MouseMoved());
//		fxPlot.setOnMouseDragged(new MouseDragged());
//		fxPlot.setOnMouseEntered(new MouseEntered());
//		fxPlot.setOnMouseExited(new MouseExited());
//
//		for (TDOverlayMarker tdOverlayMarker: overlayMarkers){
//			tdOverlayMarker.subscribePanel(fxPlot);
//		}
//	}
//	
//	class MouseClicked implements EventHandler<MouseEvent> {
//		@Override
//		public void handle(MouseEvent event) {
////			System.out.println("Mouse Clicked: " + event.toString());
//			overlayMarkers.get(currentMarker).mouseClicked(event);
//		}
//	}
//	
//	class MousePressed implements EventHandler<MouseEvent> {
//		@Override
//		public void handle(MouseEvent event) {
////			System.out.println("Mouse Pressed: " + event.toString());
//			overlayMarkers.get(currentMarker).mousePressed(event);
//		}
//	}
//	class MouseReleased implements EventHandler<MouseEvent> {
//		@Override
//		public void handle(MouseEvent event) {
////			System.out.println("Mouse Released: " + event.toString());
//			overlayMarkers.get(currentMarker).mouseReleased(event);
//		}
//	}
//	class MouseMoved implements EventHandler<MouseEvent> {
//		@Override
//		public void handle(MouseEvent event) {
//			overlayMarkers.get(currentMarker).mouseMoved(event);
//		}
//	}
//	class MouseDragged implements EventHandler<MouseEvent> {
//		@Override
//		public void handle(MouseEvent event) {
//			overlayMarkers.get(currentMarker).mouseDragged(event);
//		}
//	}
//	
//	class MouseEntered implements EventHandler<MouseEvent> {
//		@Override
//		public void handle(MouseEvent event) {
////			System.out.println("Mouse Entered: " + event.toString());
//			overlayMarkers.get(currentMarker).mouseEntered(event);
//		}
//	}
//	
//	class MouseExited implements EventHandler<MouseEvent> {
//		@Override
//		public void handle(MouseEvent event) {
////			System.out.println("Mouse Exited: " + event.toString());
//			overlayMarkers.get(currentMarker).mouseExited(event);
//		}
//	}


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
