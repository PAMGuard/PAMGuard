package videoRangePanel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

// automatically create a cursor with a red cross hair, target circle and a transparent background.
public class VRCursor {

	private Dimension d;
	
	private Cursor cursor;
	
	private Point hotSpot;
	
	private Color cursorColour = Color.RED;

	public VRCursor(Dimension d) {
		super();
		this.d = d;
		// make the image. It's probably an even number of pixels, but I want an odd number
		// so make sure the centre is really the cnntre. 
		hotSpot = new Point(d.width/2 - 1, d.height/2 - 1);
		drawImage();
	}
	
	private void drawImage() {
		BufferedImage i = new BufferedImage(d.width, d.height, Transparency.TRANSLUCENT);
		int x0 = 0, y0 = 0;
		int x1 = hotSpot.x * 2 + 0;
		int y1 = hotSpot.y * 2 + 0;
		int gap = 2;
		Graphics g = i.getGraphics();
		g.setColor(cursorColour);
		g.drawLine(x0, y0, hotSpot.x-gap, hotSpot.y-gap);
		g.drawLine(hotSpot.x+gap, hotSpot.y+gap, x1, y1);
		g.drawLine(x0, y1, hotSpot.x-gap, hotSpot.y+gap);
		g.drawLine(hotSpot.x+gap, hotSpot.y-gap, x1, y0);
		g.drawOval(0, 0, x1, y1);
		cursor = Toolkit.getDefaultToolkit().createCustomCursor(i, hotSpot, "VR Cursor");
		
	}

	public Cursor getCursor() {
		return cursor;
	}
	
	public Point getHotpot() {
		return hotSpot;
	}

	/**
	 * @return the cursorColour
	 */
	public Color getCursorColour() {
		return cursorColour;
	}

	/**
	 * @param cursorColour the cursorColour to set
	 */
	public void setCursorColour(Color cursorColour) {
		this.cursorColour = cursorColour;
		drawImage();
	}
	
	
}
