package PamView.paneloverlay.overlaymark;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Window;

import PamUtils.Coordinate3d;
import PamUtils.PamCoordinate;
import PamView.GeneralProjector;
import PamView.HoverData;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PanelOverlayDraw;
import PamView.TransformShape;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.PamDataUnit;
//import gov.nasa.worldwindx.applications.sar.segmentplane.SegmentPlaneController;
import mapgrouplocaliser.MarkGroupDataUnit;

public class MarkOverlayDraw extends PanelOverlayDraw {

	private MarkManager markManager;

	private static Color fillCol = new Color(255,255,255,100);
	private static Color lineCol = new Color(255,255,255,200);

	private Stroke plainStroke = new BasicStroke();

	final static float dash1[] = {3.0f};

	final static BasicStroke dashed =
			new BasicStroke(1.0f,
					BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_MITER,
					5.0f, dash1, 0.0f);

	/**
	 * @param mapMarker
	 */
	public MarkOverlayDraw(MarkManager markManager) {
		super(new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, fillCol, lineCol));
		this.markManager = markManager;
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		/*
		 * Data unit is always null for these constant marks, just work out if it's making a mark or not
		 * and draw it. 
		 * Data unit willNOT be null if this is used for an existing dataunit mark !
		 */
		OverlayMark mark = null;
		if (pamDataUnit == null && markManager != null) {
			mark = markManager.getCurrentMark();
		}
		else if (MarkGroupDataUnit.class.isAssignableFrom(pamDataUnit.getClass())){
			MarkGroupDataUnit mgdu = (MarkGroupDataUnit) pamDataUnit;
			mark = mgdu.getOverlayMark();
		}
		if (mark == null) {
			return null;
		}
		Shape drawnShape = drawMark(g, mark, generalProjector);
		if (drawnShape != null && pamDataUnit != null) {
			TransformShape t = new TransformShape(drawnShape, null);
			HoverData hoverData = new HoverData();
			hoverData.setTransformShape(t);
			generalProjector.addHoverData(t, pamDataUnit);
		}
		if (drawnShape == null) {
			return null;
		}
		return drawnShape.getBounds();
	}

	private Shape drawMark(Graphics g, OverlayMark mark, GeneralProjector generalProjector) {
		Graphics2D g2d = (Graphics2D) g;
		Shape drawnShape = null;
		switch(mark.getMarkType()) {
		case POLYGON:
			return drawPolygon(g2d, mark, generalProjector);
		case RECTANGLE:
			return drawRectangle(g2d, mark, generalProjector);
		}
		//		g2d.setStroke(new BasicStroke());
		return null;
	}

	private Shape drawPolygon(Graphics2D g2d, OverlayMark mark, GeneralProjector generalProjector) {
		if (mark.size() < 1) {
			//			System.out.println("Dont draw mar size " + mark.size());
			return null;
		}
		PamCoordinate c;
		Point p;
		int nPoints = mark.size();
		PamCoordinate currentMouse = mark.getCurrentMouseCoordinate();
		if (currentMouse != null) {
			nPoints++;
		}
		int[] xPoints = new int[nPoints];
		int[] yPoints = new int[nPoints];
		try {
		for (int i = 0; i < mark.size(); i++) {
			c = mark.getCoordinate(i);
			Coordinate3d c3d = generalProjector.getCoord3d(c);
			if (c3d == null) {
				return null;
			}
			p = c3d.getXYPoint();
			xPoints[i] = p.x;
			yPoints[i] = p.y;
		}
		}
		catch (Exception e) {
			return null;
		}
		if (currentMouse != null) {
			p = generalProjector.getCoord3d(currentMouse).getXYPoint();
			xPoints[nPoints-1] = p.x;
			yPoints[nPoints-1] = p.y;
		}
		Polygon pgon = new Polygon(xPoints, yPoints, nPoints);
		Color fillCol = getFillCol();
		if (fillCol != null) {
			g2d.setColor(getFillCol());
			g2d.fillPolygon(pgon);
		}
		// draw the line, but with dashes at the end, back to the start. 
		g2d.setStroke(getLineStroke());
		g2d.setColor(getLineCol());
		for (int i = 1; i < nPoints; i++) {
			g2d.drawLine(xPoints[i-1], yPoints[i-1], xPoints[i], yPoints[i]);
		}
		g2d.setStroke(getFinalLineStroke());
		g2d.drawLine(xPoints[0], yPoints[0], xPoints[nPoints-1], yPoints[nPoints-1]);

		return pgon;
	}

	private Shape drawRectangle(Graphics2D g2d, OverlayMark mark, GeneralProjector generalProjector) {
		if (mark.size() != 2) {
			return null;
		}
		/*
		 * To allow for things like the rotated map which no longer show rectangles as
		 * rectangles, we now need to draw rectangles are irregular polygons too !
		 */
		try {
			int[] x = new int[4];
			int[] y = new int[4];
			PamCoordinate m0 = mark.getCoordinate(0);
			PamCoordinate m1 = mark.getCoordinate(1);
			if (m0 == null || m1 == null) {
				return null;
			}
			Point p;
			p = generalProjector.getCoord3d(m0.getCoordinate(0), m0.getCoordinate(1), 0).getXYPoint();
			x[0] = p.x;
			y[0] = p.y;
			p = generalProjector.getCoord3d(m0.getCoordinate(0), m1.getCoordinate(1), 0).getXYPoint();
			x[1] = p.x;
			y[1] = p.y;
			p = generalProjector.getCoord3d(m1.getCoordinate(0), m1.getCoordinate(1), 0).getXYPoint();
			x[2] = p.x;
			y[2] = p.y;
			p = generalProjector.getCoord3d(m1.getCoordinate(0), m0.getCoordinate(1), 0).getXYPoint();
			x[3] = p.x;
			y[3] = p.y;
			Polygon pgon = new Polygon(x, y, 4);
			Color fillCol = getFillCol();
			if (fillCol != null) {
				g2d.setColor(getFillCol());
				g2d.fillPolygon(pgon);
			}
			return pgon;
		}
		catch (NullPointerException e) {
			return null;
		}

		//		
		//		Point p0 = generalProjector.getCoord3d(m0).getXYPoint();
		//		Point p1 = generalProjector.getCoord3d(m1).getXYPoint();
		//		g2d.setColor(getLineCol());
		//		g2d.setStroke(getLineStroke());
		//		g2d.drawLine(p0.x, p0.y, p1.x, p0.y);
		//		g2d.drawLine(p1.x, p0.y, p1.x, p1.y);
		//		g2d.drawLine(p1.x, p1.y, p0.x, p1.y);
		//		g2d.drawLine(p0.x, p1.y, p0.x, p0.y);
		//		int x = Math.min(p0.x, p1.x);
		//		int y = Math.min(p0.y, p1.y);
		//		int w = Math.abs(p0.x - p1.x);
		//		int h = Math.abs(p0.y - p1.y);
		//
		//		Color fillCol = getFillCol();
		//		if (fillCol != null) {
		//			g2d.setColor(getFillCol());
		//			g2d.fillRect(x, y, w, h);
		//		}
		//		Rectangle r = new Rectangle(x, y, w, h);
		//		return r;
	}

	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector, int keyType) {
		return null;
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		return dataUnit.getSummaryString();
	}

	@Override
	public boolean hasOptionsDialog(GeneralProjector generalProjector) {
		return false;
	}

	@Override
	public boolean showOptions(Window parentWindow, GeneralProjector generalProjector) {
		return false;
	}

	/**
	 * @return the stroke for the shape border. 
	 */
	public Stroke getLineStroke() {
		return plainStroke ;
	}

	public Stroke getFinalLineStroke() {
		return dashed;
	}

	/**
	 * @return the fill Colour
	 */
	public Color getFillCol() {
		return fillCol;
	}

	/**
	 * @return the line Colour
	 */
	public Color getLineCol() {
		return lineCol;
	}

}
