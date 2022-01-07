package AirgunDisplay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Point;

import GPS.ShipDimensionsDrawing;
import PamView.PamSymbol;
import PamView.PamSymbolType;

public class AirgunDimensionsDrawing extends ShipDimensionsDrawing {

	private double dimE = 0.1;
	static private double dimF = 0.5;
	
	PamSymbol airgunSymbol;
	
	public AirgunDimensionsDrawing() throws HeadlessException {
		super();
		airgunSymbol = new PamSymbol(PamSymbolType.SYMBOL_HEXAGRAM, 10, 10, false, Color.BLUE, Color.BLUE);
	}

	@Override
	protected int getPreferredHeight() {
		double h = super.getPreferredHeight();
		h += getScale() * dimF;
		return (int) h;
	}

	@Override
	protected int getPreferredWidth() {
		return super.getPreferredWidth();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int x, y;
		x = (int) (getBorderPixels() + getScale() * (relD + relC)/2);
		y = getHeight() - getBorderPixels() - airgunSymbol.getHeight();
		airgunSymbol.draw(g, new Point(x, y));
		Point antennaPoint = getAntennaPos();
		g.setColor(Color.BLACK);
		drawHarrow(g, antennaPoint.x, y, x, "F");
		drawVarrow(g, x, antennaPoint.y, y, "E");
		g.drawString("guns", x + g.getFontMetrics().stringWidth("A") , y);
	}
}
