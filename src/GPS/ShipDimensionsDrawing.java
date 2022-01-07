package GPS;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Point;

import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 * Class to draw a simple ship, to show the layout of the standard AIS
 * dimensions.<p>
 * A = distance from bow to reference position <p>
 * B = distance from stern to reference position<p>
 * C = distance from port side to reference position<p>
 * D = distance from startboard side to reference position<p>
 * Make the whole thing look similar to the diagram on p61 of the AIS manual
 * @author Doug
 *
 */
public class ShipDimensionsDrawing extends JPanel {

	private int scale = 200; // overall length of plot in pixels
	protected double relA = 0.75;
	protected double relB = 0.25;
	protected double relC = 0.15;
	protected double relD = 0.35;
	double bow = 0.25;
	private Point antennaPos;
	
	public ShipDimensionsDrawing() throws HeadlessException {
		super();
		setScale(scale);
		setBorder(new BevelBorder(BevelBorder.LOWERED));
		setBackground(Color.WHITE);
	}
	
	public void setDimensions(double a, double b, double c, double d) {
		/*relA = a / (a + b); // normalise to 1
		relB = b / (a + b);
		relC = c / (a + b);
		relD = d / (a + b);
		setScale(scale);
		repaint();*/
	}
	
	private void setScale(int newScale) {
		scale = newScale;
		setPreferredSize(new Dimension(getPreferredWidth(), getPreferredHeight()));
		invalidate();
	}
	
	public int getScale() {
		return scale;
	}

	protected int getPreferredWidth() {
		return (int) (getBorderPixels() * 2 + scale * (relC + relD));
	}
	
	protected int getPreferredHeight() {
		return (int) (getBorderPixels() * 2 + scale * (relA + relB));
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// ship outline
		int x = getBorderPixels();
		int stern = getBorderPixels() + scale;
		int y = stern;//getHeight() - getBorderPixels();
		int dx, dy;
		dy = (int) (scale * (1-bow));
		g.drawLine(x, y, x, y-dy); // port side
		dx = (int) (scale * (relC + relD));
		g.drawLine(x, y, x+dx, y); // stern
		g.drawLine(x+dx, y, x + dx, y-dy); // stbd side
		y = y - dy;
		dy = (int) (bow * scale);
		g.drawLine(x, y, x + dx/2, y - dy); // port bow
		g.drawLine(x + dx/2, y - dy, x + dx, y); // stbd bow
		int refX, refY;
		int refSize = 2;
		refX = (int) (getBorderPixels() + scale * relC);
		refY = (int) (getBorderPixels() + scale * relA);
		g.drawOval(refX-refSize, refY-refSize, refSize*2, refSize*2);
		drawHarrow(g, refX, refY, refX + (int) (scale * relD), "D");
		drawHarrow(g, refX, refY, refX - (int) (scale * relC), "C");
		drawVarrow(g, refX, refY, refY - (int) (scale * relA), "A");
		drawVarrow(g, refX, refY, refY + (int) (scale * relB), "B");
		antennaPos = new Point(refX, refY);
	}
	
	private int arrowHead = 5;
	public void drawHarrow(Graphics g, int x1, int y, int x2, String txt) {
		g.drawLine(x1, y, x2, y);
		int dir = 1;
		if (x2 < x1) dir = -1;
		g.drawLine(x1, y, x1 + arrowHead * dir, y + arrowHead);
		g.drawLine(x1, y, x1 + arrowHead * dir, y - arrowHead);
		g.drawLine(x2, y, x2 - arrowHead * dir, y + arrowHead);
		g.drawLine(x2, y, x2 - arrowHead * dir, y - arrowHead);
		g.drawString(txt, (x1 + x2 - g.getFontMetrics().stringWidth(txt))/2 , y-2);
	}
	public void drawVarrow(Graphics g, int x, int y1, int y2, String txt) {
		g.drawLine(x, y1, x, y2);
		int dir = 1;
		if (y1 < y2) dir = -1;
		g.drawLine(x, y1, x + arrowHead, y1 - arrowHead * dir);
		g.drawLine(x, y1, x - arrowHead, y1 - arrowHead * dir);
		g.drawLine(x, y2, x + arrowHead, y2 + arrowHead * dir);
		g.drawLine(x, y2, x - arrowHead, y2 + arrowHead * dir);
		g.drawString(txt, x+2, (y1 + y2 + g.getFontMetrics().getHeight())/2);
		
	}
	public int getBorderPixels() {
		return scale / 10;
	}

	public Point getAntennaPos() {
		return antennaPos;
	}

	public int getArrowHead() {
		return arrowHead;
	}

	
}
