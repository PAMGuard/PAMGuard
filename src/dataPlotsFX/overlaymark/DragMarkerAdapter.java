package dataPlotsFX.overlaymark;


import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDGraphFX.TDPlotPane;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;

/**
 * A mouse adapter which allows panning of the display.
 * @author Jamie Macaulay 
 *
 */
public class DragMarkerAdapter extends TDOverlayAdapter {

	private TDGraphFX tdGraphFX;

	final Delta dragDelta = new Delta();

	public DragMarkerAdapter(TDGraphFX tdGraphFX) {
		this.tdGraphFX=tdGraphFX; 
	}

	@Override
	public Node getIcon() {
//		Node icon = PamGlyphDude.createPamGlyph(MaterialIcon.PAN_TOOL, Color.WHITE, PamGuiManagerFX.iconSize);
		//mdi2h-hand-right
		Node icon = PamGlyphDude.createPamIcon("mdi2h-hand-right", PamGuiManagerFX.iconSize);
		return icon;
	}

	@Override
	public void subscribePanel(TDPlotPane fxPlot) {
		// TODO Auto-generated method stub

	}

	@Override
	public Tooltip getToolTip() {
		return new Tooltip("Allows panning of display");
	}


	@Override
	public boolean mouseClicked(MouseEvent e) {
		tdGraphFX.getScene().setCursor(Cursor.OPEN_HAND);
		return true;
	}

	@Override
	public boolean mouseDragged(MouseEvent e) {
		tdGraphFX.getScene().setCursor(Cursor.CLOSED_HAND);
		//move the display.     
		double dragDiffx = dragDelta.x-e.getX(); 
		double dragDiffy = dragDelta.y-e.getY(); 
		
		double timeDiff=(1/tdGraphFX.getTDDisplay().getTimePixPerMillis())*dragDiffx; 
		
		//System.out.println("Time diff: "+timeDiff + " dragDiffx: "+dragDiffx); 
		
		
		tdGraphFX.getTDDisplay().getTimeScroller().setValueMillis((long) (dragDelta.time+timeDiff));

		return true;
	}

	@Override
	public boolean mouseEntered(MouseEvent e) {
//		System.out.println("Dragging: Mouse entered");
		tdGraphFX.getScene().setCursor(Cursor.OPEN_HAND);
		return true;
	}

	@Override
	public boolean mouseExited(MouseEvent e) {
		tdGraphFX.getScene().setCursor(Cursor.DEFAULT);
		return true;

	}

	@Override
	public boolean mouseMoved(MouseEvent e) {
		return true;
	}

	@Override
	public boolean mousePressed(MouseEvent e) {
		tdGraphFX.getScene().setCursor(Cursor.CLOSED_HAND);
		dragDelta.x = e.getX();
		dragDelta.y = e.getY();
		dragDelta.time = tdGraphFX.getTDDisplay().getTimeStart(); 

		return true;
	}

	@Override
	public boolean mouseReleased(MouseEvent e) {
		return true;
	}

	@Override
	public boolean mouseWheelMoved(ScrollEvent e) {
		return true;
	}

	// records relative x and y co-ordinates.
	class Delta {long amplitude; long time; double x, y;}

	@Override
	public boolean needPaused() {
		// TODO Auto-generated method stub
		return false;
	}

}
